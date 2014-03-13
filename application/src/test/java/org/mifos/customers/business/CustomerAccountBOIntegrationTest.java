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

package org.mifos.customers.business;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.sampleBranchOffice;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.testUser;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountFeesActionDetailEntity;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.business.AccountPaymentEntity;
import org.mifos.accounts.business.AccountTestUtils;
import org.mifos.accounts.business.AccountTrxnEntity;
import org.mifos.accounts.business.FeesTrxnDetailEntity;
import org.mifos.accounts.exceptions.AccountException;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.business.FeeDto;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeePayment;
import org.mifos.accounts.fees.util.helpers.FeeStatus;
import org.mifos.accounts.util.helpers.AccountActionTypes;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.accounts.util.helpers.PaymentData;
import org.mifos.accounts.util.helpers.PaymentStatus;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;

public class CustomerAccountBOIntegrationTest extends MifosIntegrationTestCase {

    private static final double DELTA = 0.00000001;

    private CustomerAccountBO customerAccountBO;
    private CustomerBO center;
    private CustomerBO group;
    private CustomerBO client;
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        enableCustomWorkingDays();

        userContext = TestObjectFactory.getContext();
    }

    @After
    public void tearDown() throws Exception {
        try {
            client = null;
            group = null;
            center = null;
        } catch (Exception e) {

        }
        StaticHibernateUtil.flushAndClearSession();
    }

    @Test
    public void testSuccessfulMakePayment() throws Exception {
        createCenter();
        CustomerAccountBO customerAccount = center.getCustomerAccount();
        Assert.assertNotNull(customerAccount);
        Date transactionDate = new Date(System.currentTimeMillis());
        List<AccountActionDateEntity> dueActionDates = TestObjectFactory.getDueActionDatesForAccount(customerAccount
                .getAccountId(), transactionDate);
        Assert.assertEquals("The size of the due insallments is ", dueActionDates.size(), 1);

        PaymentData accountPaymentDataView = TestObjectFactory.getCustomerAccountPaymentDataView(dueActionDates,
                TestUtils.createMoney("100.0"), null, center.getPersonnel(), "3424324", Short
                        .valueOf("1"), transactionDate, transactionDate);

        center = TestObjectFactory.getCustomer(center.getCustomerId());
        customerAccount = center.getCustomerAccount();
        IntegrationTestObjectMother.applyAccountPayment(customerAccount, accountPaymentDataView);

        Assert.assertEquals(customerAccount.getCustomerActivitDetails().size(), 1);
        Assert.assertEquals("The size of the payments done is", customerAccount.getAccountPayments().size(), 1);
        Assert.assertEquals("The size of the due insallments after payment is", TestObjectFactory
                .getDueActionDatesForAccount(customerAccount.getAccountId(), transactionDate).size(), 0);

        for (CustomerActivityEntity activity : customerAccount.getCustomerActivitDetails()) {

            Assert.assertEquals(transactionDate, activity.getCreatedDate());
        }

        assertThat(customerAccount.getAccountPayments().size(), is(1));
        for (AccountPaymentEntity accountPayment : customerAccount.getAccountPayments()) {
            assertThat(accountPayment.getAccountTrxns().size(), is(1));
            for (AccountTrxnEntity accountTrxnEntity : accountPayment.getAccountTrxns()) {
                assertThat(accountTrxnEntity.getFinancialTransactions().size(), is(2));
            }
        }
    }

    @Test
    public void testFailureMakePayment() throws Exception {
        createCenter();
        CustomerAccountBO customerAccount = center.getCustomerAccount();
        Assert.assertNotNull(customerAccount);
        Date transactionDate = new Date(System.currentTimeMillis());
        List<AccountActionDateEntity> dueActionDates = TestObjectFactory.getDueActionDatesForAccount(customerAccount
                .getAccountId(), transactionDate);
        Assert.assertEquals("The size of the due insallments  is ", dueActionDates.size(), 1);
        PaymentData accountPaymentDataView = TestObjectFactory.getCustomerAccountPaymentDataView(dueActionDates,
                TestUtils.createMoney("100.0"), null, center.getPersonnel(), "3424324", Short
                        .valueOf("1"), transactionDate, transactionDate);
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        customerAccount = center.getCustomerAccount();

        IntegrationTestObjectMother.applyAccountPayment(customerAccount, accountPaymentDataView);
        Assert.assertEquals("The size of the payments done is", customerAccount.getAccountPayments().size(), 1);

        try {
            // one more for the next meeting
            customerAccount.applyPayment(accountPaymentDataView);
            // and the error one
            customerAccount.applyPayment(accountPaymentDataView);
            Assert.fail("Payment is done even though they are no dues");
        } catch (AccountException ae) {
            Assert.assertTrue("Payment is not allowed when there are no dues", true);
        }
    }

    @Test
    public void testIsAdjustPossibleOnLastTrxn_NotActiveState() throws Exception {
        userContext = TestUtils.makeUser();
        createInitialObjects();

        client = TestObjectFactory.createClient("Client_Active_test", CustomerStatus.CLIENT_PENDING, group);
        customerAccountBO = client.getCustomerAccount();
        Assert.assertFalse("State is not active hence adjustment is not possible", customerAccountBO
                .isAdjustPossibleOnLastTrxn());

    }

    @Test
    public void testIsAdjustPossibleOnLastTrxn_LastPaymentNull() throws Exception {
        userContext = TestUtils.makeUser();
        createInitialObjects();
        client = TestObjectFactory.createClient("Client_Active_test", CustomerStatus.CLIENT_ACTIVE, group);
        customerAccountBO = client.getCustomerAccount();
        Assert.assertFalse("Last payment was null hence adjustment is not possible", customerAccountBO
                .isAdjustPossibleOnLastTrxn());
    }

    @Test
    public void testUpdateInstallmentAfterAdjustment() throws Exception {
        userContext = TestUtils.makeUser();
        createInitialObjects();
        applyPayment();
        customerAccountBO = TestObjectFactory.getObject(CustomerAccountBO.class, customerAccountBO.getAccountId());
        client = customerAccountBO.getCustomer();
        customerAccountBO.setUserContext(userContext);
        List<AccountTrxnEntity> reversedTrxns = AccountTestUtils.reversalAdjustment("payment adjustment done",
                customerAccountBO.findMostRecentPaymentByPaymentDate());

        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        customerAccountBO.updateInstallmentAfterAdjustment(reversedTrxns, loggedInUser);
        for (AccountTrxnEntity accntTrxn : reversedTrxns) {
            CustomerTrxnDetailEntity custTrxn = (CustomerTrxnDetailEntity) accntTrxn;
            CustomerScheduleEntity accntActionDate = (CustomerScheduleEntity) customerAccountBO
                    .getAccountActionDate(custTrxn.getInstallmentId());
            Assert.assertEquals("Misc Fee Adjusted", accntActionDate.getMiscFeePaid(), TestUtils.createMoney());
            Assert.assertEquals("Misc Penalty Adjusted", accntActionDate.getMiscPenaltyPaid(),TestUtils.createMoney());
        }
        for (CustomerActivityEntity customerActivityEntity : customerAccountBO.getCustomerActivitDetails()) {
            Assert.assertEquals("Amnt Adjusted", customerActivityEntity.getDescription());
        }

    }

    @Test
    public void testUpdateAccountActivity() throws NumberFormatException, SystemException, ApplicationException {
        createInitialObjects();
        CustomerAccountBO customerAccountBO = group.getCustomerAccount();
        customerAccountBO.updateAccountActivity(null, null, new Money(getCurrency(), "222"), null, Short.valueOf("1"),
                "Mainatnence Fee removed");
        TestObjectFactory.updateObject(customerAccountBO);
        group = TestObjectFactory.getGroup(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        List<CustomerActivityEntity> customerActivitySet = customerAccountBO.getCustomerActivitDetails();
        for (CustomerActivityEntity customerActivityEntity : customerActivitySet) {
            Assert.assertEquals(1, customerActivityEntity.getPersonnel().getPersonnelId().intValue());
            Assert.assertEquals("Mainatnence Fee removed", customerActivityEntity.getDescription());
            Assert.assertEquals("222.0", customerActivityEntity.getAmount().toString());
        }
    }

    @Test
    public void testApplyMiscCharge() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);
        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));
        Money amount = new Money(getCurrency());
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                amount = customerScheduleEntity.getMiscFee();
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }
        if (customerAccountBO.getCustomerActivitDetails() != null) {
            CustomerActivityEntity customerActivityEntity = (CustomerActivityEntity) customerAccountBO
                    .getCustomerActivitDetails().toArray()[0];
            Assert.assertEquals(AccountConstants.MISC_FEES_APPLIED, customerActivityEntity.getDescription());
            Assert.assertEquals(amount, customerActivityEntity.getAmount());
        }
    }

    @Test
    public void testPayMiscChargePlusWeeklyFeeExactSameDay() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);

        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));

        CustomerScheduleEntity firstInstallment = null;
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                firstInstallment = customerScheduleEntity;
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }

        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "133.0"),
            center.getPersonnel(), (short)1, firstInstallment.getActionDate());

        customerAccountBO.applyPayment(paymentData);

        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFee());
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFeePaid());
            }
        }
    }

    @Test
    public void testPayMiscChargePlusWeeklyFeeExactPreviousDay() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);

        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));

        CustomerScheduleEntity firstInstallment = null;
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                firstInstallment = customerScheduleEntity;
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }

        DateMidnight pDate = new DateMidnight(firstInstallment.getActionDate());
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "133.0"),
            center.getPersonnel(), (short)1, pDate.minusDays(1).toDate());

        customerAccountBO.applyPayment(paymentData);

        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFee());
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFeePaid());
            }
        }
    }

    @Test
    public void testPayMiscChargePlusPartOfWeeklyFeePreviousDay() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);

        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));

        CustomerScheduleEntity firstInstallment = null;
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                firstInstallment = customerScheduleEntity;
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }

        DateMidnight pDate = new DateMidnight(firstInstallment.getActionDate());
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "50.0"),
            center.getPersonnel(), (short)1, pDate.minusDays(1).toDate());

        customerAccountBO.applyPayment(paymentData);

        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFee());
                Assert.assertEquals(new Money(getCurrency(), "0"), customerScheduleEntity.getMiscFeePaid());
                Assert.assertEquals(new Money(getCurrency(), "100"), customerScheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmount());
                Assert.assertEquals(new Money(getCurrency(), "50"), customerScheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmountPaid());
            }
        }
    }

     @Test
    public void testPayPartOfMiscChargePlusWeeklyFeePreviousDay() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);

        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));

        CustomerScheduleEntity firstInstallment = null;
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                firstInstallment = customerScheduleEntity;
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }

        DateMidnight pDate = new DateMidnight(firstInstallment.getActionDate());
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "120.0"),
            center.getPersonnel(), (short)1, pDate.minusDays(1).toDate());

        customerAccountBO.applyPayment(paymentData);

        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(new Money(getCurrency(), "33"), customerScheduleEntity.getMiscFee());
                Assert.assertEquals(new Money(getCurrency(), "20"), customerScheduleEntity.getMiscFeePaid());
                Assert.assertEquals(new Money(getCurrency(), "100"), customerScheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmount());
                Assert.assertEquals(new Money(getCurrency(), "100"), customerScheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmountPaid());
            }
        }
    }

    @Test
    public void testApplyMiscChargeWithNonActiveCustomer() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_PARTIAL, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);
        try {
            customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));
            Assert.assertFalse(false);
        } catch (AccountException e) {
            Assert.assertEquals(AccountConstants.MISC_CHARGE_NOT_APPLICABLE, e.getKey());
        }
    }

    @Test
    public void testApplyMiscChargeWithFirstInstallmentPaid() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);

        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                customerScheduleEntity.setPaymentStatus(PaymentStatus.PAID);
            }
        }
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);
        customerAccountBO.applyCharge(Short.valueOf("-1"), new Double("33"));
        Money amount = new Money(getCurrency());
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                amount = customerScheduleEntity.getMiscFee();
                Assert.assertEquals(new Money(getCurrency(), "33.0"), customerScheduleEntity.getMiscFee());
            }
        }
        if (customerAccountBO.getCustomerActivitDetails() != null) {
            CustomerActivityEntity customerActivityEntity = (CustomerActivityEntity) customerAccountBO
                    .getCustomerActivitDetails().toArray()[0];
            Assert.assertEquals(AccountConstants.MISC_FEES_APPLIED, customerActivityEntity.getDescription());
            Assert.assertEquals(amount, customerActivityEntity.getAmount());
        }
    }

    // replaced by unit tests in CustomerAccountBOTest. Remove when we're sure that unit tests cover this
    // functionality

//    public void testApplyPeriodicFee() throws Exception {
//        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
//                CUSTOMER_MEETING));
//        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
//        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
//        StaticHibernateUtil.flushSession();
//        center = TestObjectFactory.getCustomer(center.getCustomerId());
//        group = TestObjectFactory.getCustomer(group.getCustomerId());
//        customerAccountBO = group.getCustomerAccount();
//        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.ALLCUSTOMERS, "200",
//                RecurrenceType.WEEKLY, Short.valueOf("2"));
//        UserContext uc = TestUtils.makeUser();
//        customerAccountBO.setUserContext(uc);
//
//        // exercise test
//        customerAccountBO.applyCharge(periodicFee.getFeeId(), ((AmountFeeBO) periodicFee).getFeeAmount()
//                .getAmountDoubleValue());
//        StaticHibernateUtil.commitTransaction();
//
//        // verification
//        Date lastAppliedDate = null;
//        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
//            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
//            if (customerScheduleEntity.getInstallmentId() % 2 == 0) {
//                //Maintenance fee only applies to installments 2, 4, 6, ...
//                Assert.assertEquals(1, customerScheduleEntity.getAccountFeesActionDetails().size());
//            } else {
//                // Both weekly maintenance fee and weekly periodic fee apply to installments 1, 3, 5, ...
//                Assert.assertEquals(2, customerScheduleEntity.getAccountFeesActionDetails().size());
//            }
//            lastAppliedDate = customerScheduleEntity.getActionDate();
//            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : customerScheduleEntity
//                    .getAccountFeesActionDetails()) {
//                if (accountFeesActionDetailEntity.getFee().getFeeName().equals("Periodic Fee")) {
//                    Assert.assertEquals(TestUtils.createMoney("200"), accountFeesActionDetailEntity.getFeeAmount());
//                }
//            }
//        }
//        customerAccountBO.getAccountFees();
//        for (AccountFeesEntity accountFee : customerAccountBO.getAccountFees()) {
//            if (accountFee.getFees().getFeeName().equals("Periodic Fee")) {
//                Assert.assertEquals()
//            }
//        }
//        if (customerAccountBO.getCustomerActivitDetails() != null) {
//            CustomerActivityEntity customerActivityEntity = (CustomerActivityEntity) customerAccountBO
//            .getCustomerActivitDetails().toArray()[0];
//            Assert.assertEquals(periodicFee.getFeeName() + " applied", customerActivityEntity.getDescription());
//            Assert.assertEquals(new Money(getCurrency(), "1000"), customerActivityEntity.getAmount());
//            AccountFeesEntity accountFeesEntity = customerAccountBO.getAccountFees(periodicFee.getFeeId());
//            Assert.assertEquals(FeeStatus.ACTIVE.getValue(), accountFeesEntity.getFeeStatus());
//            Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(lastAppliedDate.getTime()), DateUtils
//                    .getDateWithoutTimeStamp(accountFeesEntity.getLastAppliedDate().getTime()));
//        }
//    }


    @Test
    public void testApplyUpfrontFee() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        customerAccountBO = group.getCustomerAccount();
        FeeBO upfrontFee = TestObjectFactory.createOneTimeAmountFee("Upfront Fee", FeeCategory.ALLCUSTOMERS, "20",
                FeePayment.UPFRONT);
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);
        customerAccountBO.applyCharge(upfrontFee.getFeeId(), ((AmountFeeBO) upfrontFee).getFeeAmount()
                .getAmountDoubleValue());
        StaticHibernateUtil.flushAndClearSession();
        Date lastAppliedDate = null;
        Money amount = new Money(getCurrency(), "20");

        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(2, customerScheduleEntity.getAccountFeesActionDetails().size());
                lastAppliedDate = customerScheduleEntity.getActionDate();
            }
        }

        if (customerAccountBO.getCustomerActivitDetails() != null) {
            CustomerActivityEntity customerActivityEntity = (CustomerActivityEntity) customerAccountBO
                    .getCustomerActivitDetails().toArray()[0];
            Assert.assertEquals(upfrontFee.getFeeName() + " applied", customerActivityEntity.getDescription());
            Assert.assertEquals(amount, customerActivityEntity.getAmount());
            AccountFeesEntity accountFeesEntity = customerAccountBO.getAccountFees(upfrontFee.getFeeId());
            Assert.assertEquals(FeeStatus.ACTIVE.getValue(), accountFeesEntity.getFeeStatus());
            Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(lastAppliedDate.getTime()), DateUtils
                    .getDateWithoutTimeStamp(accountFeesEntity.getLastAppliedDate().getTime()));
        }
    }

    @Test
    public void testGetNextDueAmount() throws Exception {

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        Assert.assertEquals(100.00, center.getCustomerAccount().getNextDueAmount().getAmountDoubleValue(), DELTA);
    }

    @Test
    public void testGenerateMeetingSchedule() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        List<FeeDto> feeDto = new ArrayList<FeeDto>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.ALLCUSTOMERS, "100",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeDto.add(new FeeDto(userContext, periodicFee));
        FeeBO upfrontFee = TestObjectFactory.createOneTimeAmountFee("Upfront Fee", FeeCategory.ALLCUSTOMERS, "30",
                FeePayment.UPFRONT);
        feeDto.add(new FeeDto(userContext, upfrontFee));
        center = TestObjectFactory.createCenter("Center_Active_test", meeting, feeDto);
        Date startDate = new Date(System.currentTimeMillis());
        for (AccountActionDateEntity accountActionDateEntity : center.getCustomerAccount().getAccountActionDates()) {
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(startDate.getTime()), DateUtils
                        .getDateWithoutTimeStamp(((CustomerScheduleEntity) accountActionDateEntity).getActionDate()
                                .getTime()));
                Assert.assertEquals(2, ((CustomerScheduleEntity) accountActionDateEntity).getAccountFeesActionDetails()
                        .size());
                for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : ((CustomerScheduleEntity) accountActionDateEntity)
                        .getAccountFeesActionDetails()) {

                    if (accountFeesActionDetailEntity.getFee().getFeeName().equals("Periodic Fee")) {
                        Assert.assertEquals(new Money(getCurrency(), "100.0"), accountFeesActionDetailEntity.getFeeAmount());
                    } else {
                        Assert.assertEquals("Upfront Fee", accountFeesActionDetailEntity.getFee().getFeeName());
                        Assert.assertEquals(new Money(getCurrency(), "30.0"), accountFeesActionDetailEntity.getFeeAmount());
                    }
                }
            } else if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                Assert.assertEquals(1, ((CustomerScheduleEntity) accountActionDateEntity).getAccountFeesActionDetails()
                        .size());
                Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(incrementCurrentDate(7).getTime()), DateUtils
                        .getDateWithoutTimeStamp(((CustomerScheduleEntity) accountActionDateEntity).getActionDate()
                                .getTime()));
            }
            Assert.assertFalse(((CustomerScheduleEntity) accountActionDateEntity).isPaid());
        }
    }

    @Test
    public void testActivityForMultiplePayments() throws Exception {

        // setup
        createCenter();
        CustomerAccountBO customerAccount = center.getCustomerAccount();
        Assert.assertNotNull(customerAccount);
        Date transactionDate = incrementCurrentDate(14);
        final Money paymentForThisInstallmentAndLastTwoInstallmentsInArrears = TestUtils.createMoney("300.0");
        List<AccountActionDateEntity> dueActionDates = TestObjectFactory.getDueActionDatesForAccount(customerAccount
                .getAccountId(), transactionDate);
        Assert.assertEquals("The size of the due insallments is ", dueActionDates.size(), 3);

        PaymentData accountPaymentDataView = TestObjectFactory.getCustomerAccountPaymentDataView(dueActionDates,
                paymentForThisInstallmentAndLastTwoInstallmentsInArrears, null, center.getPersonnel(), "3424324", Short
                        .valueOf("1"), transactionDate, transactionDate);

        center = TestObjectFactory.getCustomer(center.getCustomerId());
        customerAccount = center.getCustomerAccount();

        // exercise test
        customerAccount.applyPayment(accountPaymentDataView);

        // verification
        assertThat(customerAccount.getCustomerActivitDetails().size(), is(1));
        assertThat("The size of the payments done is", customerAccount.getAccountPayments().size(), is(1));
        assertThat("The size of the due insallments after payment is", TestObjectFactory.getDueActionDatesForAccount(
                customerAccount.getAccountId(), transactionDate).size(), is(0));

        assertThat(customerAccount.getAccountPayments().size(), is(1));
        for (AccountPaymentEntity accountPayment : customerAccount.getAccountPayments()) {
            assertThat(accountPayment.getAccountTrxns().size(), is(3));
            for (AccountTrxnEntity accountTrxnEntity : accountPayment.getAccountTrxns()) {
                assertThat(accountTrxnEntity.getFinancialTransactions().size(), is(2));
            }
        }
    }

    @Ignore
    @Test
    public void testGenerateMeetingScheduleWhenFirstTwoMeeingDatesOfCenterIsPassed() throws ApplicationException,
            SystemException {
        MeetingBO meetingBO = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meetingBO);
        changeAllInstallmentDateToPreviousDate(center.getCustomerAccount(), 14);
        center.update();
        StaticHibernateUtil.flushAndClearSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushAndClearSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        java.util.Date nextMeetingDate = center.getCustomerAccount().getNextMeetingDate();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        for (AccountActionDateEntity actionDateEntity : group.getCustomerAccount().getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(actionDateEntity.getActionDate().getTime()),
                        DateUtils.getDateWithoutTimeStamp(nextMeetingDate.getTime()));
            }
        }
    }


    @Ignore
    @Test
    public void testGenerateMeetingScheduleForGroupWhenMeeingDatesOfCenterIsPassed() throws ApplicationException,
            SystemException {
        MeetingBO meetingBO = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meetingBO);
        changeFirstInstallmentDateToPreviousDate(center.getCustomerAccount());
        center.update();
        StaticHibernateUtil.flushAndClearSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushAndClearSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        java.util.Date nextMeetingDate = center.getCustomerAccount().getNextMeetingDate();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        for (AccountActionDateEntity actionDateEntity : group.getCustomerAccount().getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(actionDateEntity.getActionDate().getTime()),
                        DateUtils.getDateWithoutTimeStamp(nextMeetingDate.getTime()));
            }
        }
    }

    @Test
    public void testAccountTrxnsAreZeroWhenOnlyCustomerAccountFeesAreDueForMultipleInstallments() throws Exception {
        createCenter();
        CustomerAccountBO customerAccount = center.getCustomerAccount();

        Date transactionDate = incrementCurrentDate(14);
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "300"), center.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(center);

        customerAccount.makePayment(paymentData);
        // Assert.assertEquals(expectedErrorMessage, actualErrorMessage);

    }

    @Test
    public void testAccountExceptionThrownForAZeroCustomerAccountPayment() throws Exception {
        createCenter();
        String expectedErrorMessage = "Attempting to pay a customer account balance of zero for customer: "
                + center.getGlobalCustNum();
        verifyExpectedMessageThrown(center, new Money(getCurrency(), "0.0"), 1, expectedErrorMessage);
    }

    @Test
    public void testAccountExceptionNotThrownForAPaymentNotEqualToTheTotalOutstandingCustomerAccountAmount()
            throws Exception {
        createCenter();
        verifyExpectedDetailMessageThrown(center, new Money(getCurrency(), "299.99"), 14, "No Error Message");
    }

    // Works since implementation of ERF
    @Test
    public void testPaymentBeforeMeeting() throws Exception {
        createCenter();
        verifyExpectedMessageThrown(center, new Money(getCurrency(), "8.54"), -2,
                "No Error Message");
    }

    private void verifyExpectedDetailMessageThrown(final CustomerBO customer, final Money paymentAmount,
            final Integer numberOfDaysForward, final String expectedErrorMessage) throws Exception {
        CustomerAccountBO customerAccount = customer.getCustomerAccount();

        Date transactionDate = incrementCurrentDate(numberOfDaysForward);
        PaymentData paymentData = PaymentData.createPaymentData(paymentAmount, customer.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(customer);

        String actualErrorMessage = "No Error Message";
        try {
            customerAccount.makePayment(paymentData);
        } catch (AccountException e) {
            actualErrorMessage = e.getMessage();
        }
        Assert.assertEquals(expectedErrorMessage, actualErrorMessage);
    }


    private void verifyExpectedMessageThrown(final CustomerBO customer, final Money paymentAmount,
            final Integer numberOfDaysForward, final String expectedErrorMessage) throws Exception {
        CustomerAccountBO customerAccount = customer.getCustomerAccount();

        Date transactionDate = incrementCurrentDate(numberOfDaysForward);
        PaymentData paymentData = PaymentData.createPaymentData(paymentAmount, customer.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(customer);

        String actualErrorMessage = "No Error Message";
        try {
            customerAccount.makePayment(paymentData);
        } catch (AccountException e) {
            actualErrorMessage = (String) e.getValues()[0];
        }
        Assert.assertEquals(expectedErrorMessage, actualErrorMessage);
    }


    @Test
    public void testTrxnDetailEntityObjectsForMultipleInstallmentsWhenOnlyCustomerAccountFeesAreDue() throws Exception {
        createCenter();
        CustomerAccountBO customerAccount = center.getCustomerAccount();

        Date transactionDate = incrementCurrentDate(14);
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "300"), center.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(center);
        IntegrationTestObjectMother.applyAccountPayment(customerAccount, paymentData);

        if (customerAccount.getAccountPayments() != null && customerAccount.getAccountPayments().size() == 1) {
            for (AccountPaymentEntity accountPaymentEntity : customerAccount.getAccountPayments()) {

                final Money zeroAmount = new Money(getCurrency(), "0.0");
                final Money OneHundredAmount = new Money(getCurrency(), "100.0");

                if (accountPaymentEntity.getAccountTrxns() != null
                        && accountPaymentEntity.getAccountTrxns().size() == 3) {
                    for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                        CustomerTrxnDetailEntity customerTrxnDetailEntity = (CustomerTrxnDetailEntity) accountTrxnEntity;
                        Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getAmount());
                        Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getTotalAmount());
                        Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscFeeAmount());
                        Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscPenaltyAmount());

                        if (customerTrxnDetailEntity.getFeesTrxnDetails() != null
                                && customerTrxnDetailEntity.getFeesTrxnDetails().size() == 1) {
                            for (FeesTrxnDetailEntity feesTrxnDetailEntity : customerTrxnDetailEntity
                                    .getFeesTrxnDetails()) {
                                Assert.assertEquals(OneHundredAmount, feesTrxnDetailEntity.getFeeAmount());
                            }
                        } else {
                            throw new Exception("Expected one FeesTrxnDetailEntity, found none or more than one");
                        }
                    }
                } else {
                    throw new Exception("Expected three CustomerTrxnDetailEntity, found none or not three");
                }
            }
        } else {
            throw new Exception("Expected one AccountPaymentEntity, found none or more than one");
        }
    }

    @Test
    public void testTrxnDetailEntityObjectsForMultipleInstallmentsWhenOnlyCustomerAccountChargesAreDue()
            throws Exception {

        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
        center = new CenterBuilder().with(weeklyMeeting).withName("Center").with(sampleBranchOffice())
                .withLoanOfficer(testUser()).build();
        IntegrationTestObjectMother.createCenter((CenterBO)center, weeklyMeeting);

        CustomerAccountBO customerAccount = center.getCustomerAccount();

        final Money fiftyAmount = new Money(getCurrency(), "50.0");
        final Money seventyAmount = new Money(getCurrency(), "70.0");
        final Money oneHundredTwentyAmount = new Money(getCurrency(), "120.0");
        final Money twoHundredFortyAmount = new Money(getCurrency(), "240.0");
        for (AccountActionDateEntity accountActionDateEntity : customerAccount.getAccountActionDates()) {
            CustomerScheduleEntity customerSchedule = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerSchedule.getInstallmentId() != 2) {
                customerSchedule.setMiscFee(fiftyAmount);
                customerSchedule.setMiscPenalty(seventyAmount);
            }
        }
        Date transactionDate = incrementCurrentDate(14);
        PaymentData paymentData = PaymentData.createPaymentData(twoHundredFortyAmount, center.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(center);
        IntegrationTestObjectMother.applyAccountPayment(customerAccount, paymentData);

        if (customerAccount.getAccountPayments() != null && customerAccount.getAccountPayments().size() == 1) {
            for (AccountPaymentEntity accountPaymentEntity : customerAccount.getAccountPayments()) {

                final Money zeroAmount = new Money(accountPaymentEntity.getAmount().getCurrency(), "0.0");

                if (accountPaymentEntity.getAccountTrxns() != null
                        && accountPaymentEntity.getAccountTrxns().size() == 3) {
                    for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                        CustomerTrxnDetailEntity customerTrxnDetailEntity = (CustomerTrxnDetailEntity) accountTrxnEntity;

                        if (customerTrxnDetailEntity.getInstallmentId() != 2) {
                            Assert.assertEquals(oneHundredTwentyAmount, customerTrxnDetailEntity.getAmount());
                            Assert.assertEquals(oneHundredTwentyAmount, customerTrxnDetailEntity.getTotalAmount());
                            Assert.assertEquals(fiftyAmount, customerTrxnDetailEntity.getMiscFeeAmount());
                            Assert.assertEquals(seventyAmount, customerTrxnDetailEntity.getMiscPenaltyAmount());
                        } else {
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getTotalAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscFeeAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscPenaltyAmount());
                        }
                        Assert.assertEquals(0, customerTrxnDetailEntity.getFeesTrxnDetails().size());
                    }
                } else {
                    throw new Exception("Expected three CustomerTrxnDetailEntity, found none or not three");
                }
            }
        } else {
            throw new Exception("Expected one AccountPaymentEntity, found none or more than one");
        }
    }

    @Test
    public void testTrxnDetailEntityObjectsForMultipleInstallmentsWhenBothCustomerAccountChargesAndFeesAreDue()
            throws Exception {
        createCenter();
        FeeBO extraFee = TestObjectFactory.createPeriodicAmountFee("extra fee", FeeCategory.ALLCUSTOMERS, "5.5",
                RecurrenceType.WEEKLY, Short.valueOf("1"));

        CustomerAccountBO customerAccount = center.getCustomerAccount();
        AccountFeesEntity extraAccountFeesEntity = new AccountFeesEntity(customerAccount, extraFee, 11.66);
        // FIXME: a fee is being added by exposing an internal data structure and adding it directly to it
        customerAccount.getAccountFeesIncludingInactiveFees().add(extraAccountFeesEntity);

        final Money eightAmount = new Money(getCurrency(), "8.0");
        final Money fiftyAmount = new Money(getCurrency(), "50.0");
        final Money seventyAmount = new Money(getCurrency(), "70.0");
        final Money oneHundredTwentyAmount = new Money(getCurrency(), "120.0");
        for (AccountActionDateEntity accountActionDateEntity : customerAccount.getAccountActionDates()) {
            CustomerScheduleEntity customerSchedule = (CustomerScheduleEntity) accountActionDateEntity;
            if (customerSchedule.getInstallmentId() == 2) {
                customerSchedule.setMiscFee(fiftyAmount);
                customerSchedule.setMiscPenalty(seventyAmount);
            }
            if (customerSchedule.getInstallmentId() == 3) {
                CustomerAccountBOTestUtils.applyPeriodicFees(customerSchedule, extraAccountFeesEntity.getFees()
                        .getFeeId(), new Money(getCurrency(), "8"));
            }
        }
        Date transactionDate = incrementCurrentDate(14);
        PaymentData paymentData = PaymentData.createPaymentData(new Money(getCurrency(), "428"), center.getPersonnel(), Short
                .valueOf("1"), transactionDate);
        paymentData.setCustomer(center);
        IntegrationTestObjectMother.applyAccountPayment(customerAccount, paymentData);

        if (customerAccount.getAccountPayments() != null && customerAccount.getAccountPayments().size() == 1) {
            for (AccountPaymentEntity accountPaymentEntity : customerAccount.getAccountPayments()) {

                final Money zeroAmount = new Money(accountPaymentEntity.getAmount().getCurrency(), "0.0");
                final Money OneHundredAmount = new Money(accountPaymentEntity.getAmount().getCurrency(), "100.0");

                if (accountPaymentEntity.getAccountTrxns() != null
                        && accountPaymentEntity.getAccountTrxns().size() == 3) {
                    for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                        CustomerTrxnDetailEntity customerTrxnDetailEntity = (CustomerTrxnDetailEntity) accountTrxnEntity;

                        if (customerTrxnDetailEntity.getInstallmentId() == 2) {
                            Assert.assertEquals(oneHundredTwentyAmount, customerTrxnDetailEntity.getAmount());
                            Assert.assertEquals(oneHundredTwentyAmount, customerTrxnDetailEntity.getTotalAmount());
                            Assert.assertEquals(fiftyAmount, customerTrxnDetailEntity.getMiscFeeAmount());
                            Assert.assertEquals(seventyAmount, customerTrxnDetailEntity.getMiscPenaltyAmount());
                        } else {
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getTotalAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscFeeAmount());
                            Assert.assertEquals(zeroAmount, customerTrxnDetailEntity.getMiscPenaltyAmount());
                        }
                        if (customerTrxnDetailEntity.getFeesTrxnDetails() != null
                                && customerTrxnDetailEntity.getFeesTrxnDetails().size() < 3) {
                            for (FeesTrxnDetailEntity feesTrxnDetailEntity : customerTrxnDetailEntity
                                    .getFeesTrxnDetails()) {
                                if (feesTrxnDetailEntity.getAccountFees().getAccountFeeId() == extraAccountFeesEntity
                                        .getAccountFeeId()) {
                                    Assert.assertEquals(eightAmount, feesTrxnDetailEntity.getFeeAmount());
                                } else {
                                    Assert.assertEquals(OneHundredAmount, feesTrxnDetailEntity.getFeeAmount());
                                }
                            }
                        } else {
                            throw new Exception("Expected one FeesTrxnDetailEntity, found none or more than two");
                        }
                    }
                } else {
                    throw new Exception("Expected three CustomerTrxnDetailEntity, found none or not three");
                }
            }
        } else {
            throw new Exception("Expected one AccountPaymentEntity, found none or more than one");
        }
    }

    @Ignore
    @Test
    public void testCustomerActivitDetailsSortingByDate()
            throws Exception {
        userContext = TestUtils.makeUser();
        createInitialObjects();
        client = TestObjectFactory.createClient("Client_Active_test", CustomerStatus.CLIENT_ACTIVE, group);
        customerAccountBO = client.getCustomerAccount();
        customerAccountBO.addCustomerActivity(new CustomerActivityEntity(customerAccountBO, center.getPersonnel(),
                TestUtils.createMoney(10), AccountConstants.PAYMENT_RCVD, new Date(System.currentTimeMillis() + 172800000)));
        customerAccountBO.addCustomerActivity(new CustomerActivityEntity(customerAccountBO, center.getPersonnel(),
                TestUtils.createMoney(20), AccountConstants.PAYMENT_RCVD, new Date(System.currentTimeMillis())));
        customerAccountBO.addCustomerActivity(new CustomerActivityEntity(customerAccountBO, center.getPersonnel(),
                TestUtils.createMoney(30), AccountConstants.PAYMENT_RCVD, new Date(System.currentTimeMillis() + 345600000)));
        customerAccountBO.update();
        StaticHibernateUtil.flushAndClearSession();
        StaticHibernateUtil.flushSession();
        customerAccountBO = TestObjectFactory.getCustomer(client.getCustomerId()).getCustomerAccount();
        UserContext uc = TestUtils.makeUser();
        customerAccountBO.setUserContext(uc);
        Assert.assertEquals(3, customerAccountBO.getCustomerActivitDetails().size());
        Assert.assertEquals(TestUtils.createMoney(30), customerAccountBO.getCustomerActivitDetails().get(0).getAmount());
        Assert.assertEquals(TestUtils.createMoney(10), customerAccountBO.getCustomerActivitDetails().get(1).getAmount());
        Assert.assertEquals(TestUtils.createMoney(20), customerAccountBO.getCustomerActivitDetails().get(2).getAmount());
    }

    /*
     * Create a center with a default weekly maintenance fee of 100.
     */
    private void createCenter() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
    }

    private void changeFirstInstallmentDateToPreviousDate(final CustomerAccountBO customerAccountBO) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - 1);
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                ((CustomerScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                        .getTimeInMillis()));
                break;
            }
        }
    }

    private void createInitialObjects() {

        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
        IntegrationTestObjectMother.saveMeeting(weeklyMeeting);

        center = new CenterBuilder().withNumberOfExistingCustomersInOffice(3).with(weeklyMeeting).withName("Center_Active_test").with(
                sampleBranchOffice()).withLoanOfficer(testUser()).build();
        IntegrationTestObjectMother.createCenter((CenterBO)center, weeklyMeeting);

        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
    }

    private void applyPayment() throws Exception {
        client = TestObjectFactory.createClient("Client_Active_test", CustomerStatus.CLIENT_ACTIVE, group);
        customerAccountBO = client.getCustomerAccount();
        Date currentDate = new Date(System.currentTimeMillis());
        customerAccountBO.setUserContext(userContext);

        CustomerScheduleEntity accountAction = (CustomerScheduleEntity) customerAccountBO.getAccountActionDate(Short
                .valueOf("1"));
        accountAction.setMiscFeePaid(TestUtils.createMoney(100));
        accountAction.setMiscPenaltyPaid(TestUtils.createMoney(100));
        accountAction.setPaymentDate(currentDate);
        accountAction.setPaymentStatus(PaymentStatus.PAID);

        AccountPaymentEntity accountPaymentEntity = new AccountPaymentEntity(customerAccountBO,
                TestUtils.createMoney(300), "1111", currentDate, new PaymentTypeEntity(Short.valueOf("1")),
                new Date(System.currentTimeMillis()));

        CustomerTrxnDetailEntity accountTrxnEntity = new CustomerTrxnDetailEntity(accountPaymentEntity,
                AccountActionTypes.PAYMENT, Short.valueOf("1"), accountAction.getActionDate(), TestObjectFactory
                        .getPersonnel(userContext.getId()), currentDate, TestUtils.createMoney(300),
                "payment done", null, TestUtils.createMoney(100), TestUtils.createMoney(100));

        for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountAction.getAccountFeesActionDetails()) {
            CustomerAccountBOTestUtils.setFeeAmountPaid((CustomerFeeScheduleEntity) accountFeesActionDetailEntity,
                    TestUtils.createMoney(100));
            FeesTrxnDetailEntity feeTrxn = new FeesTrxnDetailEntity(accountTrxnEntity, accountFeesActionDetailEntity
                    .getAccountFee(), accountFeesActionDetailEntity.getFeeAmount());
            accountTrxnEntity.addFeesTrxnDetail(feeTrxn);
        }
        accountPaymentEntity.addAccountTrxn(accountTrxnEntity);
        AccountTestUtils.addAccountPayment(accountPaymentEntity, customerAccountBO);
        TestObjectFactory.updateObject(customerAccountBO);
        StaticHibernateUtil.flushSession();
    }

    private Date incrementCurrentDate(final int noOfDays) {
        return new java.sql.Date(new DateTime().plusDays(noOfDays).toDate().getTime());
    }

    private void changeAllInstallmentDateToPreviousDate(final CustomerAccountBO customerAccountBO, final int noOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - noOfDays);
        for (AccountActionDateEntity accountActionDateEntity : customerAccountBO.getAccountActionDates()) {
            ((CustomerScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
        }
    }
}
