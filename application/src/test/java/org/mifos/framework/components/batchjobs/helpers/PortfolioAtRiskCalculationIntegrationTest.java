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

package org.mifos.framework.components.batchjobs.helpers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanBOTestUtils;
import org.mifos.accounts.loan.business.LoanScheduleEntity;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.accounts.util.helpers.PaymentData;
import org.mifos.application.master.util.helpers.PaymentTypes;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class PortfolioAtRiskCalculationIntegrationTest extends MifosIntegrationTestCase {

    private static final double DELTA = 0.00000001;

    private AccountBO account1 = null;

    private AccountBO account2 = null;

    private CenterBO center;

    private CenterBO center1 = null;

    private GroupBO group;

    private GroupBO group1;

    private ClientBO client;

    private ClientBO client1 = null;

    private ClientBO client2 = null;

    private OfficeBO officeBO;

    @Before
    public void setUp() throws Exception {
        StaticHibernateUtil.getSessionTL().clear();
    }

    @After
    public void tearDown() throws Exception {
        try {
            client1 = null;
            client2 = null;
            account1 = null;
            account2 = null;

//            account2 = null;
//            account1 = null;
//            TestObjectFactory.cleanUp(client1);
//            TestObjectFactory.cleanUp(client2);
            client = null;
            group = null;
            group1 = null;
            center = null;
            center1 = null;
            officeBO = null;
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db

        }
        StaticHibernateUtil.flushSession();
    }

    private void createInitialObject() {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loandsdasd", "fsad", startDate, meeting, TestUtils.RUPEE);
        account1 = TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
        loanOffering = TestObjectFactory.createLoanOffering("Loandfas", "dsvd", ApplicableTo.CLIENTS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        account2 = TestObjectFactory.createLoanAccount("42427777341", client,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering);
        StaticHibernateUtil.flushAndClearSession();
    }

    private void createPayment(LoanBO loan, Money amountPaid) throws Exception {
        Set<AccountActionDateEntity> actionDateEntities = loan.getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils
                .getSortedAccountActionDateEntity(actionDateEntities, 6);
        PersonnelBO personnelBO = legacyPersonnelDao.getPersonnel(TestObjectFactory.getContext().getId());
        LoanScheduleEntity loanSchedule = paymentsArray[0];
        Short paymentTypeId = PaymentTypes.CASH.getValue();
        PaymentData paymentData = PaymentData.createPaymentData(amountPaid, personnelBO, paymentTypeId, loanSchedule.getActionDate());
        IntegrationTestObjectMother.applyAccountPayment(loan, paymentData);
        paymentData = PaymentData.createPaymentData(amountPaid, personnelBO, paymentTypeId, loanSchedule.getActionDate());
        IntegrationTestObjectMother.applyAccountPayment(loan, paymentData);
    }

    private void changeFirstInstallmentDate(AccountBO accountBO, int numberOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - numberOfDays);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanBOTestUtils.setActionDate(accountActionDateEntity, new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
            break;
        }
    }

    @Test
    public void testGeneratePortfolioAtRiskForTaskNoPayment() throws Exception {
        createInitialObject();
        StaticHibernateUtil.flushSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());

        for (AccountBO account : group.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 31);
                ((LoanBO) account).handleArrears();
            }
        }
        for (AccountBO account : client.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 31);
                ((LoanBO) account).handleArrears();
            }
        }
        StaticHibernateUtil.flushSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        double portfolioAtRisk = PortfolioAtRiskCalculation.generatePortfolioAtRiskForTask(group.getCustomerId(), group
                .getOffice().getOfficeId(), group.getSearchId() + ".%");
        Assert.assertEquals(1.0, portfolioAtRisk, DELTA);

        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
        account2 = TestObjectFactory.getObject(AccountBO.class, account2.getAccountId());
    }

    @Test
    public void testGeneratePortfolioAtRiskForTaskSomePayments() throws Exception {
        createInitialObject();
        StaticHibernateUtil.flushSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());

        createPayment((LoanBO)account1, new Money(account1.getCurrency(), "200"));

        changeFirstInstallmentDate(account2, 31);
        IntegrationTestObjectMother.saveLoanAccount((LoanBO)account2);

        createPayment((LoanBO)account2, new Money(account2.getCurrency(), "200"));
        IntegrationTestObjectMother.saveLoanAccount((LoanBO)account2);
        ((LoanBO)account2).handleArrears();
        IntegrationTestObjectMother.saveLoanAccount((LoanBO)account2);

        group = TestObjectFactory.getGroup(group.getCustomerId());
        double portfolioAtRisk = PortfolioAtRiskCalculation.generatePortfolioAtRiskForTask(group.getCustomerId(), group
                .getOffice().getOfficeId(), group.getSearchId() + ".%");
//        Assert.assertEquals(0.5, portfolioAtRisk, DELTA);
    }
}