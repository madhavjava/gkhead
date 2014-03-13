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

package org.mifos.accounts.business;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.AccountIntegrationTestCase;
import org.mifos.accounts.exceptions.AccountException;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeePayment;
import org.mifos.accounts.financial.business.FinancialTransactionBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanTrxnDetailEntity;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.PaymentData;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.config.AccountingRules;
import org.mifos.config.persistence.ConfigurationPersistence;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.dto.screen.TransactionHistoryDto;
import org.mifos.framework.TestUtils;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;


public class AccountBOIntegrationTest extends AccountIntegrationTestCase {

    private static final double DELTA = 0.00000001;

    @After
    @Override
    public void tearDown() throws Exception {
        new ConfigurationPersistence().updateConfigurationKeyValueInteger("repaymentSchedulesIndependentOfMeetingIsEnabled", 0);
    }

    /*
     * When LSIM is turned on, back dated transactions should be allowed.
     */
    @Test
    public void testIsTrxnDateValidWithLSIM() throws Exception {
        new ConfigurationPersistence().updateConfigurationKeyValueInteger("repaymentSchedulesIndependentOfMeetingIsEnabled", 1);

        DateTime transactionDate = new DateTime();
        transactionDate = transactionDate.plusDays(10);
        java.util.Date trxnDate = transactionDate.toDate();

        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        groupLoan.changeStatus(AccountState.LOAN_APPROVED, null, "status changed", loggedInUser);
        Assert.assertTrue(AccountingRules.isBackDatedTxnAllowed());

        Date meetingDate = new CustomerPersistence().getLastMeetingDateForCustomer(groupLoan.getCustomer().getCustomerId());
        Assert.assertTrue(groupLoan.isTrxnDateValid(trxnDate, meetingDate, false));
    }


    /**
     * The name of this test, and some now-gone (and broken) exception-catching code, make it look like it was supposed
     * to test failure. But it doesn't (and I don't see a corresponding success test).
     */
    @Test
    public void testFailureRemoveFees() throws Exception {
        StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        UserContext uc = TestUtils.makeUser();
        Set<AccountFeesEntity> accountFeesEntitySet = groupLoan.getAccountFees();
        for (AccountFeesEntity accountFeesEntity : accountFeesEntitySet) {
            groupLoan.removeFeesAssociatedWithUpcomingAndAllKnownFutureInstallments(accountFeesEntity.getFees().getFeeId(), uc.getId());
        }
        StaticHibernateUtil.flushAndClearSession();
    }

    @Test
    public void testSuccessGetLastPmntAmntToBeAdjusted()  throws Exception {

        LoanBO loan = groupLoan;

        Date currentDate = new Date(System.currentTimeMillis());
        Date startDate = new Date(System.currentTimeMillis());

        loan.setAccountState(new AccountStateEntity(AccountState.LOAN_APPROVED));
        disburseLoan(loan, startDate);

        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData firstPaymentData = TestObjectFactory
                .getLoanAccountPaymentData(accntActionDates, TestUtils.createMoney(88), null, loan.getPersonnel(),
                        "receiptNum", Short.valueOf("1"), currentDate, currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, firstPaymentData);

        TestObjectFactory.updateObject(loan);
        StaticHibernateUtil.flushAndClearSession();
        // the loan has to be reloaded from db so that the payment list will be
        // in desc order and the
        // last payment will be the first in the payment list
        Session session = StaticHibernateUtil.getSessionTL();
        loan = (LoanBO) session.get(LoanBO.class, loan.getAccountId());

        Assert.assertEquals(88.0, loan.getLastPmntAmntToBeAdjusted(), DELTA);
        groupLoan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
    }

    @Test
    public void testSuccessUpdateAccountActionDateEntity() {
        List<Short> installmentIdList;
        installmentIdList = getApplicableInstallmentIdsForRemoveFees(groupLoan);
        Set<AccountFeesEntity> accountFeesEntitySet = groupLoan.getAccountFees();
        Iterator<AccountFeesEntity> itr = accountFeesEntitySet.iterator();
        while (itr.hasNext()) {
            groupLoan.updateAccountActionDateEntity(installmentIdList, itr.next().getFees().getFeeId());
        }

    }

    @Ignore
    @Test
    public void testSuccessUpdateAccountFeesEntity() {
        Set<AccountFeesEntity> accountFeesEntitySet = groupLoan.getAccountFees();
        Assert.assertEquals(1, accountFeesEntitySet.size());
        Iterator<AccountFeesEntity> itr = accountFeesEntitySet.iterator();
        while (itr.hasNext()) {
            AccountFeesEntity accountFeesEntity = itr.next();
            groupLoan.updateAccountFeesEntity(accountFeesEntity.getFees().getFeeId());
            Assert.assertFalse(accountFeesEntity.isActive());
        }
    }

    @Test
    public void testGetLastLoanPmntAmnt() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(700), null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate,
                currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, paymentData);

        TestObjectFactory.updateObject(loan);
        StaticHibernateUtil.flushSession();
        Assert.assertEquals("The amount returned for the payment should have been 1272", 700.0, loan.getLastPmntAmnt());
        groupLoan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
    }

    @Test
    public void testLoanAdjustment() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;
        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.add(loan.getAccountActionDate(Short.valueOf("1")));
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(216), null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate,
                currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);

        loan = IntegrationTestObjectMother.findLoanBySystemId(loan.getGlobalAccountNum());
        loan.setUserContext(TestUtils.makeUser());

        IntegrationTestObjectMother.applyAccountPayment(loan, TestObjectFactory.getLoanAccountPaymentData(null, TestUtils.createMoney(600),
                null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate, currentDate));

        loan = IntegrationTestObjectMother.findLoanBySystemId(loan.getGlobalAccountNum());
        loan.setUserContext(TestUtils.makeUser());

        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        loan.adjustPmnt("loan account has been adjusted by test code", loggedInUser);

        IntegrationTestObjectMother.saveLoanAccount(loan);

        Assert.assertEquals("The amount returned for the payment should have been 0", 0.0, loan.getLastPmntAmnt());
        LoanTrxnDetailEntity lastLoanTrxn = null;
        for (AccountTrxnEntity accntTrxn : loan.findMostRecentPaymentByPaymentDate().getAccountTrxns()) {
            lastLoanTrxn = (LoanTrxnDetailEntity) accntTrxn;
            break;
        }
        AccountActionDateEntity installment = loan.getAccountActionDate(lastLoanTrxn.getInstallmentId());
        Assert.assertFalse("The installment adjusted should now be marked unpaid(due).", installment.isPaid());
    }

    @Test
    public void testAdjustmentForClosedAccnt() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;

        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(712), null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate,
                currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);
        loan.setAccountState(new AccountStateEntity(AccountState.LOAN_CLOSED_OBLIGATIONS_MET));

        TestObjectFactory.updateObject(loan);
        try {
            PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
            loan.adjustPmnt("loan account has been adjusted by test code", loggedInUser);
        } catch (AccountException e) {
            Assert.assertEquals("exception.accounts.ApplicationException.CannotAdjust", e.getKey());
        }
    }

    @Test
    public void testRetrievalOfNullMonetaryValue() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;
        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(0), null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate, currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);

        TestObjectFactory.updateObject(loan);
        StaticHibernateUtil.flushSession();
        loan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());

        List<AccountPaymentEntity> payments = loan.getAccountPayments();
        Assert.assertEquals(1, payments.size());
        AccountPaymentEntity accntPmnt = payments.iterator().next();
        StaticHibernateUtil.flushSession();

        Assert.assertEquals("Account payment retrieved should be zero with currency MFI currency", TestUtils
                .createMoney(0), accntPmnt.getAmount());
    }

    @Test
    public void testGetTransactionHistoryView() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;
        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(100), null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate,
                currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);

        loan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
        List<Integer> ids = new ArrayList<Integer>();
        for (AccountPaymentEntity accountPaymentEntity : loan.getAccountPayments()) {
            for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                for (FinancialTransactionBO financialTransactionBO : accountTrxnEntity.getFinancialTransactions()) {
                    ids.add(financialTransactionBO.getTrxnId());
                }
            }
        }
        loan.setUserContext(TestUtils.makeUser());
        List<TransactionHistoryDto> trxnHistlist = loan.getTransactionHistoryView();
        Assert.assertNotNull("Account TrxnHistoryView list object should not be null", trxnHistlist);
        Assert.assertTrue("Account TrxnHistoryView list object Size should be greater than zero",
                trxnHistlist.size() > 0);
        Assert.assertEquals(ids.size(), trxnHistlist.size());
        int i = 0;
        for (TransactionHistoryDto transactionHistoryDto : trxnHistlist) {
            Assert.assertEquals(ids.get(i), transactionHistoryDto.getAccountTrxnId());
            i++;
        }
        StaticHibernateUtil.flushSession();
        groupLoan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
    }

    @Test
    public void testGetTransactionHistoryViewByOtherUser() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = groupLoan;
        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PersonnelBO personnel = legacyPersonnelDao.getPersonnel(Short.valueOf("2"));
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(100), null, personnel, "receiptNum", Short.valueOf("1"), currentDate, currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);
        loan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
        loan.setUserContext(TestUtils.makeUser());
        List<TransactionHistoryDto> trxnHistlist = loan.getTransactionHistoryView();
        Assert.assertNotNull("Account TrxnHistoryView list object should not be null", trxnHistlist);
        Assert.assertTrue("Account TrxnHistoryView list object Size should be greater than zero",
                trxnHistlist.size() > 0);
        for (TransactionHistoryDto transactionHistoryDto : trxnHistlist) {
            Assert.assertEquals(transactionHistoryDto.getPostedBy(), personnel.getDisplayName());
        }
        StaticHibernateUtil.flushSession();
        groupLoan = TestObjectFactory.getObject(LoanBO.class, loan.getAccountId());
    }

    @Ignore
    @Test
    public void testGetPeriodicFeeList() throws PersistenceException {
        FeeBO oneTimeFee = TestObjectFactory.createOneTimeAmountFee("One Time Fee", FeeCategory.LOAN, "20",
                FeePayment.TIME_OF_DISBURSEMENT);
        AccountFeesEntity accountOneTimeFee = new AccountFeesEntity(groupLoan, oneTimeFee, new Double("1.0"));
        groupLoan.addAccountFees(accountOneTimeFee);
        legacyAccountDao.createOrUpdate(groupLoan);
        StaticHibernateUtil.flushSession();
        groupLoan = TestObjectFactory.getObject(LoanBO.class, groupLoan.getAccountId());
        Assert.assertEquals(1, groupLoan.getPeriodicFeeList().size());
    }

    @Test
    public void testIsTrxnDateValid() throws Exception {

        Calendar calendar = new GregorianCalendar();
        // Added by rajender on 24th July as test case was not passing
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        java.util.Date trxnDate = new Date(calendar.getTimeInMillis());
        Date meetingDate = new CustomerPersistence().getLastMeetingDateForCustomer(groupLoan.getCustomer().getCustomerId());
        if (AccountingRules.isBackDatedTxnAllowed()) {
            Assert.assertTrue(groupLoan.isTrxnDateValid(trxnDate, meetingDate, false));
        } else {
            Assert.assertFalse(groupLoan.isTrxnDateValid(trxnDate, meetingDate, false));
        }
    }

    @Test
    public void testDeleteFutureInstallments() throws HibernateException, SystemException, AccountException {
        StaticHibernateUtil.flushSession();
        groupLoan = TestObjectFactory.getObject(LoanBO.class, groupLoan.getAccountId());
        groupLoan.deleteFutureInstallments();
        StaticHibernateUtil.flushAndClearSession();
        groupLoan = TestObjectFactory.getObject(LoanBO.class, groupLoan.getAccountId());
        Assert.assertEquals(1, groupLoan.getAccountActionDates().size());
    }

    @Test
    public void testUpdate() throws Exception {
        StaticHibernateUtil.flushSession();
        groupLoan = (LoanBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, groupLoan.getAccountId());
        groupLoan.setUserContext(TestUtils.makeUser());

        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        PersonnelBO personnelBO = legacyPersonnelDao.getPersonnel(TestUtils.makeUser().getId());
        AccountNotesEntity accountNotesEntity = new AccountNotesEntity(currentDate, "account updated", personnelBO,
                groupLoan);
        groupLoan.addAccountNotes(accountNotesEntity);
        TestObjectFactory.updateObject(groupLoan);
        StaticHibernateUtil.flushSession();
        groupLoan = (LoanBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, groupLoan.getAccountId());
        for (AccountNotesEntity accountNotes : groupLoan.getRecentAccountNotes()) {
            Assert.assertEquals("Last note added is account updated", "account updated", accountNotes.getComment());
            Assert.assertEquals(currentDate.toString(), accountNotes.getCommentDateStr());
            Assert.assertEquals(personnelBO.getPersonnelId(), accountNotes.getPersonnel().getPersonnelId());
            Assert.assertEquals(personnelBO.getDisplayName(), accountNotes.getPersonnel().getDisplayName());
            Assert.assertEquals(currentDate.toString(), accountNotes.getCommentDate().toString());
            Assert.assertEquals(groupLoan.getAccountId(), accountNotes.getAccount().getAccountId());
            Assert.assertNotNull(accountNotes.getCommentId());
            break;
        }
    }

    @Test
    public void testGetPastInstallments() {

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        CenterBO centerBO = TestObjectFactory.createWeeklyFeeCenter("Center_Active", meeting);
        StaticHibernateUtil.flushAndClearSession();
        centerBO = TestObjectFactory.getCenter(centerBO.getCustomerId());
        for (AccountActionDateEntity actionDate : centerBO.getCustomerAccount().getAccountActionDates()) {
            actionDate.setActionDate(offSetCurrentDate(4));
            break;
        }
        List<AccountActionDateEntity> pastInstallments = centerBO.getCustomerAccount().getPastInstallments();
        Assert.assertNotNull(pastInstallments);
        Assert.assertEquals(1, pastInstallments.size());
        centerBO = null;
    }

    @Test
    public void testGetAllInstallments() {

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        CenterBO centerBO = TestObjectFactory.createWeeklyFeeCenter("Center_Active", meeting);
        StaticHibernateUtil.flushAndClearSession();
        centerBO = TestObjectFactory.getCenter(centerBO.getCustomerId());

        List<AccountActionDateEntity> allInstallments = centerBO.getCustomerAccount().getAllInstallments();
        Assert.assertNotNull(allInstallments);
        Assert.assertEquals(10, allInstallments.size());
        centerBO = null;
    }

    @Test
    public void testUpdatePerformanceHistoryOnAdjustment() throws Exception {
        Date currentDate = new Date(System.currentTimeMillis());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        PaymentData paymentData1 = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestUtils
                .createMoney(212), null, groupLoan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate,
                currentDate);
        IntegrationTestObjectMother.applyAccountPayment(groupLoan, paymentData1);
        IntegrationTestObjectMother.saveLoanAccount(groupLoan);

        LoanBO loan = IntegrationTestObjectMother.findLoanBySystemId(groupLoan.getGlobalAccountNum());

        PaymentData paymentData2 = TestObjectFactory.getLoanAccountPaymentData(null, TestUtils.createMoney(600),
                null, loan.getPersonnel(), "receiptNum", Short.valueOf("1"), currentDate, currentDate);

        IntegrationTestObjectMother.applyAccountPayment(loan, paymentData2);
        IntegrationTestObjectMother.saveLoanAccount(groupLoan);

        groupLoan = IntegrationTestObjectMother.findLoanBySystemId(groupLoan.getGlobalAccountNum());
        groupLoan.setUserContext(TestUtils.makeUser());
        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        groupLoan.adjustPmnt("loan account has been adjusted by test code", loggedInUser);

        IntegrationTestObjectMother.saveLoanAccount(groupLoan);
    }

    @Test
    public void testAccountBOClosedDate() {
        AccountBO account = new AccountBO();
        java.util.Date originalDate = new java.util.Date();
        final long TEN_SECONDS = 10000;

        // verify that after the setter is called, changes to the object
        // passed to the setter do not affect the internal state
        java.util.Date mutatingDate1 = (java.util.Date) originalDate.clone();
        account.setClosedDate(mutatingDate1);
        mutatingDate1.setTime(System.currentTimeMillis() + TEN_SECONDS);
        Assert.assertEquals(account.getClosedDate(), originalDate);

        // verify that after the getter is called, changes to the object
        // returned by the getter do not affect the internal state
        java.util.Date originalDate2 = (java.util.Date) originalDate.clone();
        account.setClosedDate(originalDate2);
        java.util.Date mutatingDate2 = account.getClosedDate();
        mutatingDate2.setTime(System.currentTimeMillis() + TEN_SECONDS);
        Assert.assertEquals(account.getClosedDate(), originalDate);
    }

    @Test
    public void testGetInstalmentDates() throws Exception {
        AccountBO account = new AccountBO();
        MeetingBO meeting = new MeetingBO(RecurrenceType.DAILY, (short) 1, getDate("18/08/2005"),
                MeetingType.CUSTOMER_MEETING);
        /*
         * make sure we can handle case where the number of installments is zero
         */
        account.getInstallmentDates(meeting, (short) 0, (short) 0);
    }

    @Test
    public void testGenerateId() throws Exception {
        AccountBO account = new AccountBO(35);
        String officeGlobalNum = "0567";
        String globalAccountNum = account.generateId(officeGlobalNum);
        Assert.assertEquals("056700000000035", globalAccountNum);
    }

    private List<Short> getApplicableInstallmentIdsForRemoveFees(final AccountBO account) {
        List<Short> installmentIdList = new ArrayList<Short>();
        for (AccountActionDateEntity accountActionDateEntity : account.getApplicableIdsForFutureInstallments()) {
            installmentIdList.add(accountActionDateEntity.getInstallmentId());
        }
        installmentIdList.add(account.getDetailsOfNextInstallment().getInstallmentId());
        return installmentIdList;
    }

    private void disburseLoan(final LoanBO loan, final Date startDate) throws Exception {
        loan.disburseLoan("receiptNum", startDate, Short.valueOf("1"), loan.getPersonnel(), startDate, Short.valueOf("1"));
        StaticHibernateUtil.flushAndClearSession();
    }

    private java.sql.Date offSetCurrentDate(final int noOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - noOfDays);
        return new java.sql.Date(currentDateCalendar.getTimeInMillis());
    }
}
