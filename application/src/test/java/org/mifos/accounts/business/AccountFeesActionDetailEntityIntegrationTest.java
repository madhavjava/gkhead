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

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanScheduleEntity;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.business.CustomerScheduleEntity;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class AccountFeesActionDetailEntityIntegrationTest extends MifosIntegrationTestCase {

    protected AccountBO accountBO = null;

    protected CustomerBO center = null;

    protected CustomerBO group = null;

    @Test
    public void testMakeEarlyRepaymentEnteriesForFeePayment() {
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) installment;
            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDateEntity
                    .getAccountFeesActionDetails()) {
                accountFeesActionDetailEntity.makeRepaymentEnteries(LoanConstants.PAY_FEES_PENALTY_INTEREST);
               Assert.assertEquals(accountFeesActionDetailEntity.getFeeAmount(), accountFeesActionDetailEntity
                        .getFeeAmountPaid());
            }
        }
    }

    @Test
    public void testMakeEarlyRepaymentEnteriesForFeePaymentWithInterestWaiver() {
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) installment;
            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDateEntity
                    .getAccountFeesActionDetails()) {
                accountFeesActionDetailEntity.makeRepaymentEnteries(LoanConstants.PAY_FEES_PENALTY);
               Assert.assertEquals(accountFeesActionDetailEntity.getFeeAmount(), accountFeesActionDetailEntity
                        .getFeeAmountPaid());
            }
        }
    }

    @Test
    public void testMakeEarlyRepaymentEnteriesForNotPayingFee() {
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) installment;
            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDateEntity
                    .getAccountFeesActionDetails()) {
                Money preRepaymentFeeAmount = accountFeesActionDetailEntity.getFeeAmount();
                accountFeesActionDetailEntity.makeRepaymentEnteries(LoanConstants.DONOT_PAY_FEES_PENALTY_INTEREST);
               Assert.assertEquals(accountFeesActionDetailEntity.getFeeAmount(), preRepaymentFeeAmount);
            }
        }
    }

    @Test
    public void testWaiveCharges() {
        StaticHibernateUtil.flushSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());

        CustomerScheduleEntity accountActionDate = (CustomerScheduleEntity) group.getCustomerAccount()
                .getAccountActionDates().toArray()[0];
        Money chargeWaived = new Money(getCurrency());
        for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDate
                .getAccountFeesActionDetails()) {
            chargeWaived = accountFeesActionDetailEntity.waiveCharges();
           Assert.assertEquals(new Money(getCurrency()), accountFeesActionDetailEntity.getFeeAmount());
        }
       Assert.assertEquals(new Money(getCurrency(), "100"), chargeWaived);
        StaticHibernateUtil.flushSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
    }

    private AccountBO getLoanAccount() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, new Date(System
                .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                new Date(System.currentTimeMillis()), loanOffering);
    }

    @After
    public void tearDown() throws Exception {
        accountBO = null;
        group = null;
        center = null;

//        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
//        group = (CustomerBO) StaticHibernateUtil.getSessionTL().get(CustomerBO.class, group.getCustomerId());
//        center = (CustomerBO) StaticHibernateUtil.getSessionTL().get(CustomerBO.class, center.getCustomerId());
//        accountBO = null;
//        group = null;
//        center = null;

        StaticHibernateUtil.flushSession();

    }

    @Before
    public void setUp() throws Exception {

        accountBO = getLoanAccount();
    }

}
