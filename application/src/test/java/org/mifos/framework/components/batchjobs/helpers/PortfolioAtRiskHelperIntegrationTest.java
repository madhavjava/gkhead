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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanBOTestUtils;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class PortfolioAtRiskHelperIntegrationTest extends MifosIntegrationTestCase {

    protected AccountBO account1 = null;

    protected AccountBO account2 = null;

    protected CustomerBO center = null;

    protected CustomerBO group = null;

    protected CustomerBO client = null;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        try {
            account2 = null;
            account1 = null;
            client = null;
            group = null;
            center = null;
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db

        }
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testExecute() throws Exception {
        LoanArrearsHelper loanArrearsHelper = new LoanArrearsHelper();
        loanArrearsHelper.execute(System.currentTimeMillis());
        createInitialObject();

        group = TestObjectFactory.getCustomer(group.getCustomerId());
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        for (AccountBO account : group.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 7);
                ((LoanBO) account).handleArrears();
            }
        }
        for (AccountBO account : client.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 7);
                ((LoanBO) account).handleArrears();
            }
        }
        TestObjectFactory.updateObject(client);
        TestObjectFactory.updateObject(group);
        StaticHibernateUtil.flushSession();

        PortfolioAtRiskHelper portfolioAtRiskHelper = new PortfolioAtRiskHelper();//(PortfolioAtRiskHelper) portfolioAtRiskTask.getTaskHelper();
        portfolioAtRiskHelper.execute(System.currentTimeMillis());

        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
        account2 = TestObjectFactory.getObject(AccountBO.class, account2.getAccountId());
    }

    private void createInitialObject() {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient(this.getClass().getSimpleName() + " Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName() + " Loan", "LOAN", startDate, meeting);
        account1 = TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
        loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName() + " Loan123", "LOAP", ApplicableTo.CLIENTS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        account2 = TestObjectFactory.createLoanAccount("42427777341", client,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering);
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

}
