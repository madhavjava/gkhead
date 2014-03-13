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

package org.mifos.accounts.business.service;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountActionEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountFeesActionDetailEntity;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.business.AccountStateEntity;
import org.mifos.accounts.business.AccountStateMachines;
import org.mifos.accounts.business.AccountTestUtils;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeeFormula;
import org.mifos.accounts.fees.util.helpers.FeePayment;
import org.mifos.accounts.fees.util.helpers.FeeStatus;
import org.mifos.accounts.loan.business.LoanBOTestUtils;
import org.mifos.accounts.loan.business.LoanFeeScheduleEntity;
import org.mifos.accounts.loan.business.LoanScheduleEntity;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.util.helpers.AccountActionTypes;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStateFlag;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.customers.business.CustomerAccountBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.business.CustomerFeeScheduleEntity;
import org.mifos.customers.business.CustomerScheduleEntity;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.domain.ApplicableCharge;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountServiceIntegrationTest extends MifosIntegrationTestCase {

    protected AccountBO accountBO = null;

    protected SavingsBO savingsBO = null;

    protected CustomerBO center = null;

    protected CustomerBO group = null;

    @Autowired
    protected LegacyAccountDao legacyAccountDao;

    private AccountBusinessService service;

    @Before
    public void setUp() throws Exception {
        service = new AccountBusinessService();
    }

    @After
    public void tearDown() throws Exception {
        try {
            accountBO = null;
            savingsBO = null;
            group = null;
            center = null;
//            accountBO = null;
//            savingsBO = null;
//            group = null;
//            center = null;
            legacyAccountDao = null;
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db

        }
    }

    @Test
    public void testGetAccountAction() throws Exception {
        AccountBusinessService service = new AccountBusinessService();
        AccountActionEntity accountaction = service.getAccountAction(AccountActionTypes.SAVINGS_DEPOSIT.getValue(),
                Short.valueOf("1"));
        Assert.assertNotNull(accountaction);
    }

    @Test
    public void testGetAppllicableFees() throws Exception {
        AccountBusinessService accountBusinessService = new AccountBusinessService();
        accountBO = getLoanAccount();
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        UserContext uc = TestUtils.makeUser();
        List<ApplicableCharge> applicableChargeList = accountBusinessService.getAppllicableFees(accountBO
                .getAccountId(), uc);
        Assert.assertEquals(2, applicableChargeList.size());
    }

    @Test @Ignore
    public void testGetAppllicableFeesForInstallmentStartingOnCurrentDate() throws Exception {
        AccountBusinessService accountBusinessService = new AccountBusinessService();
        accountBO = getLoanAccountWithAllTypesOfFees();
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        StaticHibernateUtil.flushAndClearSession();
        UserContext uc = TestUtils.makeUser();
        List<ApplicableCharge> applicableChargeList = accountBusinessService.getAppllicableFees(accountBO
                .getAccountId(), uc);
        Assert.assertEquals(4, applicableChargeList.size());
        for (ApplicableCharge applicableCharge : applicableChargeList) {
            if (applicableCharge.getFeeName().equalsIgnoreCase("Upfront Fee")) {
                Assert.assertEquals(new Money(getCurrency(), "20.0"), new Money(getCurrency(), applicableCharge
                        .getAmountOrRate()));
                Assert.assertNotNull(applicableCharge.getFormula());
                Assert.assertNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Periodic Fee")) {
                Assert.assertEquals(new Money(getCurrency(), "200.0"), new Money(getCurrency(), applicableCharge
                        .getAmountOrRate()));
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNotNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Misc Fee")) {
                Assert.assertNull(applicableCharge.getAmountOrRate());
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNull(applicableCharge.getPeriodicity());
            }
        }
    }

    @Test
    public void testGetAppllicableFeesForInstallmentStartingAfterCurrentDate() throws Exception {
        AccountBusinessService accountBusinessService = new AccountBusinessService();
        accountBO = getLoanAccountWithAllTypesOfFees();
        incrementInstallmentDate(accountBO, 1, Short.valueOf("1"));
        accountBO.setUserContext(TestObjectFactory.getContext());

        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        accountBO.changeStatus(AccountState.LOAN_DISBURSED_TO_LOAN_OFFICER, null, "", loggedInUser);
        TestObjectFactory.updateObject(accountBO);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        StaticHibernateUtil.flushAndClearSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        UserContext uc = TestUtils.makeUser();
        List<ApplicableCharge> applicableChargeList = accountBusinessService.getAppllicableFees(accountBO
                .getAccountId(), uc);
        Assert.assertEquals(3, applicableChargeList.size());
        for (ApplicableCharge applicableCharge : applicableChargeList) {
            if (applicableCharge.getFeeName().equalsIgnoreCase("Periodic Fee")) {
                Assert.assertEquals(new Money(getCurrency(), "200.0"), new Money(getCurrency(), applicableCharge
                        .getAmountOrRate()));
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNotNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Misc Fee")) {
                Assert.assertNull(applicableCharge.getAmountOrRate());
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNull(applicableCharge.getPeriodicity());
            }
        }
    }

    @Test
    public void testGetAppllicableFeesForMeetingStartingOnCurrentDate() throws Exception {
        // FIXME some test leaves database table (apart from CUSTOMER and
        // PRD_OFFERING) in dirty state Failures are noticed on windows xp
        // system, the execution order differs for surefire from OS to OS.

        AccountBusinessService accountBusinessService = new AccountBusinessService();
        CustomerAccountBO customerAccountBO = getCustomerAccountWithAllTypesOfFees();
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        StaticHibernateUtil.flushAndClearSession();
        UserContext uc = TestUtils.makeUser();
        List<ApplicableCharge> applicableChargeList = accountBusinessService.getAppllicableFees(customerAccountBO
                .getAccountId(), uc);
        Assert.assertEquals(4, applicableChargeList.size());
        for (ApplicableCharge applicableCharge : applicableChargeList) {
            if (applicableCharge.getFeeName().equalsIgnoreCase("Upfront Fee")) {
                // this is a rate, so we shouldn't have a trailing ".0"
                Assert.assertEquals("20", applicableCharge.getAmountOrRate());
                Assert.assertNotNull(applicableCharge.getFormula());
                Assert.assertNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Misc Fee")) {
                Assert.assertNull(applicableCharge.getAmountOrRate());
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Periodic Fee")) {
                Assert.assertEquals(new Money(getCurrency(), "200.0"), new Money(getCurrency(), applicableCharge
                        .getAmountOrRate()));
                Assert.assertNull(applicableCharge.getFormula());
                Assert.assertNotNull(applicableCharge.getPeriodicity());
            } else if (applicableCharge.getFeeName().equalsIgnoreCase("Mainatnence Fee")) {
                Assert.assertFalse(true);
            }
        }
    }

    @Test
    public void testGetStatusName() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.SAVINGS_ACCOUNT, null);
        String statusNameForSavings = service.getStatusName(AccountState.SAVINGS_PARTIAL_APPLICATION, AccountTypes.SAVINGS_ACCOUNT);
        Assert.assertNotNull(statusNameForSavings);

        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        String statusNameForLoan = service.getStatusName(AccountState.LOAN_PARTIAL_APPLICATION,AccountTypes.LOAN_ACCOUNT);
        Assert.assertNotNull(statusNameForLoan);
    }

    @Test
    public void testGetFlagName() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.SAVINGS_ACCOUNT, null);
        String flagNameForSavings = service.getFlagName(AccountStateFlag.SAVINGS_REJECTED, AccountTypes.SAVINGS_ACCOUNT);
        Assert.assertNotNull(flagNameForSavings);

        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        String flagNameForLoan = service.getFlagName(AccountStateFlag.LOAN_REJECTED, AccountTypes.LOAN_ACCOUNT);
        Assert.assertNotNull(flagNameForLoan);
    }

    @Test
    public void testGetStatusList() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.SAVINGS_ACCOUNT, null);
        List<AccountStateEntity> statusListForSavings = service.getStatusList(new AccountStateEntity(
                AccountState.SAVINGS_PARTIAL_APPLICATION), AccountTypes.SAVINGS_ACCOUNT, TestUtils.makeUser()
                .getLocaleId());
        Assert.assertEquals(2, statusListForSavings.size());

        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        List<AccountStateEntity> statusListForLoan = service.getStatusList(new AccountStateEntity(
                AccountState.LOAN_PARTIAL_APPLICATION), AccountTypes.LOAN_ACCOUNT, Short.valueOf("1"));
        Assert.assertEquals(2, statusListForLoan.size());
    }

    private AccountBO getLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
    }

    private AccountBO getLoanAccountWithAllTypesOfFees() {
        accountBO = getLoanAccount();

        LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountBO.getAccountActionDate(Short.valueOf("1"));

        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT, null);
        AccountFeesEntity accountUpfrontFee = new AccountFeesEntity(accountBO, upfrontFee, new Double("20.0"),
                FeeStatus.ACTIVE.getValue(), null, loanScheduleEntity.getActionDate());
        AccountTestUtils.addAccountFees(accountUpfrontFee, accountBO);
        AccountFeesActionDetailEntity accountUpfrontFeesaction = new LoanFeeScheduleEntity(loanScheduleEntity,
                upfrontFee, accountUpfrontFee, new Money(getCurrency(), "20.0"));
        loanScheduleEntity.addAccountFeesAction(accountUpfrontFeesaction);
        TestObjectFactory.updateObject(accountBO);

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        loanScheduleEntity = (LoanScheduleEntity) accountBO.getAccountActionDate(Short.valueOf("1"));
        FeeBO timeOfDisbursementFees = TestObjectFactory.createOneTimeAmountFee("Disbursement Fee", FeeCategory.LOAN,
                "30", FeePayment.TIME_OF_DISBURSEMENT);
        AccountFeesEntity accountDisbursementFee = new AccountFeesEntity(accountBO, timeOfDisbursementFees, new Double(
                "30.0"), FeeStatus.ACTIVE.getValue(), null, loanScheduleEntity.getActionDate());
        AccountTestUtils.addAccountFees(accountDisbursementFee, accountBO);
        AccountFeesActionDetailEntity accountDisbursementFeesaction = new LoanFeeScheduleEntity(loanScheduleEntity,
                timeOfDisbursementFees, accountDisbursementFee, new Money(getCurrency(), "30.0"));
        loanScheduleEntity.addAccountFeesAction(accountDisbursementFeesaction);
        TestObjectFactory.updateObject(accountBO);

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        loanScheduleEntity = (LoanScheduleEntity) accountBO.getAccountActionDate(Short.valueOf("1"));
        FeeBO firstLoanRepaymentFee = TestObjectFactory.createOneTimeAmountFee("First Loan Repayment Fee",
                FeeCategory.LOAN, "40", FeePayment.TIME_OF_FIRSTLOANREPAYMENT);
        AccountFeesEntity accountFirstLoanRepaymentFee = new AccountFeesEntity(accountBO, firstLoanRepaymentFee,
                new Double("40.0"), FeeStatus.ACTIVE.getValue(), null, loanScheduleEntity.getActionDate());
        AccountTestUtils.addAccountFees(accountFirstLoanRepaymentFee, accountBO);
        AccountFeesActionDetailEntity accountTimeOfFirstLoanRepaymentFeesaction = new LoanFeeScheduleEntity(
                loanScheduleEntity, firstLoanRepaymentFee, accountFirstLoanRepaymentFee, new Money(getCurrency(),
                        "40.0"));
        loanScheduleEntity.addAccountFeesAction(accountTimeOfFirstLoanRepaymentFeesaction);
        TestObjectFactory.updateObject(accountBO);

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "200",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        AccountFeesEntity accountPeriodicFee = new AccountFeesEntity(accountBO, periodicFee, new Double("200.0"),
                FeeStatus.INACTIVE.getValue(), null, null);
        AccountTestUtils.addAccountFees(accountPeriodicFee, accountBO);
        TestObjectFactory.updateObject(accountBO);

        return accountBO;
    }

    private CustomerAccountBO getCustomerAccountWithAllTypesOfFees() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        CustomerAccountBO customerAccountBO = center.getCustomerAccount();

        CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) customerAccountBO
                .getAccountActionDate(Short.valueOf("1"));

        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.CENTER, Double
                .valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT, null);
        AccountFeesEntity accountUpfrontFee = new AccountFeesEntity(customerAccountBO, upfrontFee, new Double("20.0"),
                FeeStatus.ACTIVE.getValue(), null, customerScheduleEntity.getActionDate());
        AccountTestUtils.addAccountFees(accountUpfrontFee, customerAccountBO);
        AccountFeesActionDetailEntity accountUpfrontFeesaction = new CustomerFeeScheduleEntity(customerScheduleEntity,
                upfrontFee, accountUpfrontFee, new Money(getCurrency(), "20.0"));
        customerScheduleEntity.addAccountFeesAction(accountUpfrontFeesaction);
        TestObjectFactory.updateObject(center);

        customerAccountBO = center.getCustomerAccount();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.ALLCUSTOMERS, "200",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        AccountFeesEntity accountPeriodicFee = new AccountFeesEntity(customerAccountBO, periodicFee,
                new Double("200.0"), FeeStatus.INACTIVE.getValue(), null, null);
        AccountTestUtils.addAccountFees(accountPeriodicFee, customerAccountBO);
        TestObjectFactory.updateObject(center);

        return customerAccountBO;
    }

    private void incrementInstallmentDate(AccountBO accountBO, int numberOfDays, Short installmentId) {
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            if (accountActionDateEntity.getInstallmentId().equals(installmentId)) {
                Calendar dateCalendar = new GregorianCalendar();
                dateCalendar.setTimeInMillis(accountActionDateEntity.getActionDate().getTime());
                int year = dateCalendar.get(Calendar.YEAR);
                int month = dateCalendar.get(Calendar.MONTH);
                int day = dateCalendar.get(Calendar.DAY_OF_MONTH);
                dateCalendar = new GregorianCalendar(year, month, day + numberOfDays);
                LoanBOTestUtils.setActionDate(accountActionDateEntity,
                        new java.sql.Date(dateCalendar.getTimeInMillis()));
                break;
            }
        }
    }
}
