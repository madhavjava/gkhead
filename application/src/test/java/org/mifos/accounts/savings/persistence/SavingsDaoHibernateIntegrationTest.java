/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package org.mifos.accounts.savings.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.sampleBranchOffice;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.testUser;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.interest.EndOfDayDetail;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.CollectionSheetCustomerSavingDto;
import org.mifos.application.servicefacade.CustomerHierarchyParams;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.ClientBuilder;
import org.mifos.domain.builders.FeeBuilder;
import org.mifos.domain.builders.GroupBuilder;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.domain.builders.SavingsAccountBuilder;
import org.mifos.domain.builders.SavingsProductBuilder;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;

/**
 * I test integration of {@link SavingsDaoHibernate}, hibernate mapping
 * configuration and queries against latest database schema.
 *
 * I should test the contract of {@link SavingsDao} in every way that its test
 * double (mock, stub) is used in unit tests.
 */
public class SavingsDaoHibernateIntegrationTest extends MifosIntegrationTestCase {

    // class under test
    private SavingsDao savingsDao;

    // collaborators
    private MeetingBO weeklyMeeting;
    private AmountFeeBO weeklyPeriodicFeeForCenterOnly;
    private AmountFeeBO weeklyPeriodicFeeForGroupOnly;
    private AmountFeeBO weeklyPeriodicFeeForClientsOnly;
    private CustomerBO center;
    private GroupBO group;
    private ClientBO client;
    private SavingsBO savingsAccount;
    private SavingsBO secondSavingsAccount;
    private SavingsOfferingBO savingsProduct;
    private SavingsOfferingBO secondSavingsProduct;
    private GenericDao baseDao;
    private CustomerHierarchyParams customerHierarchyParams;

    @Before
    public void setUp() throws Exception {
        enableCustomWorkingDays();
        weeklyMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
        IntegrationTestObjectMother.saveMeeting(weeklyMeeting);

        weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly().withFeeAmount("100.0").withName(
                "Center Weekly Periodic Fee").withSameRecurrenceAs(weeklyMeeting).with(sampleBranchOffice())
                .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);

        center = new CenterBuilder().with(weeklyMeeting).withName("Center").with(sampleBranchOffice())
                .withLoanOfficer(testUser()).build();
        IntegrationTestObjectMother.createCenter((CenterBO)center, weeklyMeeting);

        weeklyPeriodicFeeForGroupOnly = new FeeBuilder().appliesToGroupsOnly().withFeeAmount("50.0").withName(
                "Group Weekly Periodic Fee").withSameRecurrenceAs(weeklyMeeting).with(sampleBranchOffice())
                .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForGroupOnly);

        group = new GroupBuilder().withMeeting(weeklyMeeting).withName("Group").withOffice(sampleBranchOffice())
                .withLoanOfficer(testUser()).withFee(weeklyPeriodicFeeForGroupOnly).withParentCustomer(center).build();
        IntegrationTestObjectMother.createGroup(group, weeklyMeeting);

        weeklyPeriodicFeeForClientsOnly = new FeeBuilder().appliesToClientsOnly().withFeeAmount("10.0").withName(
                "Client Weekly Periodic Fee").withSameRecurrenceAs(weeklyMeeting).with(sampleBranchOffice())
                .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForClientsOnly);

        client = new ClientBuilder().withMeeting(weeklyMeeting).withName("Client 1").withOffice(sampleBranchOffice())
                .withLoanOfficer(testUser()).withParentCustomer(group).buildForIntegrationTests();
        IntegrationTestObjectMother.createClient(client, weeklyMeeting);

        customerHierarchyParams = new CustomerHierarchyParams(center.getCustomerId(), center.getOffice().getOfficeId(),
                center.getSearchId() + ".%", new LocalDate());

        baseDao = new GenericDaoHibernate();
        savingsDao = new SavingsDaoHibernate(baseDao);
    }

    @After
    public void tearDown() throws Exception {
        IntegrationTestObjectMother.cleanSavingsProductAndAssociatedSavingsAccounts(savingsAccount);
        IntegrationTestObjectMother.cleanSavingsProductAndAssociatedSavingsAccounts(secondSavingsAccount);
        IntegrationTestObjectMother.cleanCustomerHierarchyWithMeeting(client, group, center, weeklyMeeting);
        savingsAccount = null;
        secondSavingsAccount = null;
        client = null;
        group = null;
        center = null;
        weeklyMeeting = null;
//            TestObjectFactory.cleanUp(weeklyMeeting);

    }

    @Test
    public void shouldRetrieveAllEndOfDayActvityOnSavingsAccount() {

        // setup
        savingsProduct = new SavingsProductBuilder().voluntary().appliesToClientsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().withSavingsProduct(savingsProduct).withDepositOf("50")
                .withCustomer(client).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        // exercise test
        List<EndOfDayDetail> accountActivity = this.savingsDao.retrieveAllEndOfDayDetailsFor(Money.getDefaultCurrency(), Long.valueOf(savingsAccount.getAccountId().toString()));

        // verification
        assertFalse(accountActivity.isEmpty());
        assertThat(accountActivity.get(0).getResultantAmountForDay(), is(TestUtils.createMoney("50")));
    }

    @Test
    public void testShouldReturnEmptyListWhenNoMandatorySavingsAccountsExistForClientsOrGroupsWithCompleteGroupStatus() {

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllMandatorySavingAccountsForClientsOrGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        Assert.assertTrue(mandatorySavingAccounts.isEmpty());
    }

    @Test
    public void testShouldFindExistingMandatorySavingsAccountsForGroupsWithCompleteGroupStatusWhenCenterIsTopOfCustomerHierarchy() {

        // setup
        savingsProduct = new SavingsProductBuilder().mandatory().appliesToGroupsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().withSavingsProduct(savingsProduct).withCustomer(group)
                .withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllMandatorySavingAccountsForClientsOrGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(mandatorySavingAccounts.size(), is(1));
    }

    @Test
    public void testShouldFindExistingMandatorySavingsAccountsForClientsWhenCenterIsTopOfCustomerHierarchy() {

        // setup
        savingsProduct = new SavingsProductBuilder().mandatory().appliesToClientsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().withSavingsProduct(savingsProduct)
                .withCustomer(client).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllMandatorySavingAccountsForClientsOrGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(mandatorySavingAccounts.size(), is(1));
    }

    @Test
    public void testShouldReturnEmptyListWhenNoVoluntarySavingsAccountsForClientsOrGroupsWithCompleteGroupStatusExist() {

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllVoluntarySavingAccountsForClientsAndGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        Assert.assertTrue(mandatorySavingAccounts.isEmpty());
    }

    @Test
    public void testShouldFindExistingVoluntarySavingsAccountsForGroupsWithCompleteGroupStatusWhenCenterIsTopOfCustomerHierarchy() {

        // setup
        savingsProduct = new SavingsProductBuilder().voluntary().appliesToGroupsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().completeGroup().withSavingsProduct(savingsProduct).withCustomer(group)
                .withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllVoluntarySavingAccountsForClientsAndGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(mandatorySavingAccounts.size(), is(1));
    }

    @Test
    public void testShouldFindExistingVoluntarySavingsAccountsForClientsWhenCenterIsTopOfCustomerHierarchy() {

        // setup
        savingsProduct = new SavingsProductBuilder().voluntary().appliesToClientsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().completeGroup().withSavingsProduct(savingsProduct).withCustomer(group)
                .withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllVoluntarySavingAccountsForClientsAndGroupsWithCompleteGroupStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(mandatorySavingAccounts.size(), is(1));
    }

    @Test
    public void testShouldReturnEmptyListWhenNoMandatorySavingsAccountsForCentersOrGroupsWithPerIndividualStatusExist() {

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllMandatorySavingAccountsForIndividualChildrenOfCentersOrGroupsWithPerIndividualStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        Assert.assertTrue(mandatorySavingAccounts.isEmpty());
    }

    @Test
    public void testShouldFindOnlyMandatorySavingsAccountsForCentersOrGroupThatToBePaidIndividuallyByTheirClients() {

        // setup
        savingsProduct = new SavingsProductBuilder().voluntary().appliesToGroupsOnly().withShortName("SP1")
                .buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().completeGroup().perIndividual().withSavingsProduct(
                savingsProduct).withCustomer(group).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        secondSavingsProduct = new SavingsProductBuilder().mandatory().withShortName("SP2").appliesToCentersOnly()
                .withName("testSavingPrd2").buildForIntegrationTests();
        secondSavingsAccount = new SavingsAccountBuilder().withSavingsProduct(secondSavingsProduct)
                .withCustomer(center).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(secondSavingsProduct,
                secondSavingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllMandatorySavingAccountsForIndividualChildrenOfCentersOrGroupsWithPerIndividualStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(mandatorySavingAccounts.size(), is(1));
    }

    @Test
    public void testShouldReturnEmptyListWhenNoVoluntarySavingsAccountsForCentersOrGroupsWithPerIndividualStatusExist() {

        // exercise test
        List<CollectionSheetCustomerSavingDto> mandatorySavingAccounts = savingsDao
                .findAllVoluntarySavingAccountsForIndividualChildrenOfCentersOrGroupsWithPerIndividualStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        Assert.assertTrue(mandatorySavingAccounts.isEmpty());
    }

    @Test
    public void testShouldFindOnlyVoluntarySavingsAccountsForIndividualClientsOfTheVoluntaryCentersOrVoluntaryGroupsWithPerIndividualStatus() {

        // setup
        savingsProduct = new SavingsProductBuilder().voluntary().withShortName("SP1").appliesToGroupsOnly().buildForIntegrationTests();
        savingsAccount = new SavingsAccountBuilder().completeGroup().perIndividual().withSavingsProduct(savingsProduct)
                .withCustomer(group).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(savingsProduct, savingsAccount);

        secondSavingsProduct = new SavingsProductBuilder().mandatory().withShortName("SP2").appliesToCentersOnly()
                .withName("testSavingPrd2").buildForIntegrationTests();
        secondSavingsAccount = new SavingsAccountBuilder().withSavingsProduct(secondSavingsProduct)
                .withCustomer(center).withCreatedBy(IntegrationTestObjectMother.testUser()).build();
        IntegrationTestObjectMother.saveSavingsProductAndAssociatedSavingsAccounts(secondSavingsProduct,
                secondSavingsAccount);

        // exercise test
        List<CollectionSheetCustomerSavingDto> voluntarySavingAccountsForPaymentByIndividuals = savingsDao
                .findAllVoluntarySavingAccountsForIndividualChildrenOfCentersOrGroupsWithPerIndividualStatusForCustomerHierarchy(customerHierarchyParams);

        // verification
        assertThat(voluntarySavingAccountsForPaymentByIndividuals.size(), is(1));
    }
}
