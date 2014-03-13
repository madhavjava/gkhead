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

package org.mifos.accounts.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.PaymentData;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.screen.TransactionHistoryDto;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;

@SuppressWarnings("unchecked")
public class AccountActionStrutsTest extends MifosMockStrutsTestCase {

    private AccountBO accountBO = null;
    private CustomerBO center = null;
    private CustomerBO group = null;
    private UserContext userContext;
    private String flowKey;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/accounts-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {
        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, AccountAppAction.class);
        accountBO = getLoanAccount();
    }

    @After
    public void tearDown() throws Exception {
        accountBO = null;
        group = null;
        center = null;
    }

    public void ignore_testSuccessfulRemoveFees() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        Short feeId = null;
        Set<AccountFeesEntity> accountFeesSet = accountBO.getAccountFees();
        for (AccountFeesEntity accountFeesEntity : accountFeesSet) {
            feeId = accountFeesEntity.getFees().getFeeId();
        }
        setRequestPathInfo("/accountAppAction");
        addRequestParameter("method", "removeFees");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter("feeId", feeId.toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward("remove_success");
        Assert.assertNull(request.getAttribute(Constants.CURRENTFLOWKEY));
    }

    private AccountBO getLoanAccount() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter(this.getClass().getSimpleName() + " Group",
                CustomerStatus.GROUP_ACTIVE, center);
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName() + " LOAN",
                "", startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
    }

    @Test
    public void testGetTrxnHistorySucess() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        Date currentDate = new Date(System.currentTimeMillis());
        setRequestPathInfo("/accountAppAction");
        addRequestParameter("method", "getTrxnHistory");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter("feeId", "123");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("globalAccountNum", accountBO.getGlobalAccountNum());
        LoanBO loan = (LoanBO) accountBO;
        loan.setUserContext(TestUtils.makeUser());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData accountPaymentDataView = TestObjectFactory.getLoanAccountPaymentData(accntActionDates,
                TestUtils.createMoney(0), null, loan.getPersonnel(), "receiptNum", Short
                        .valueOf("1"), currentDate, currentDate);
        IntegrationTestObjectMother.applyAccountPayment(loan, accountPaymentDataView);

        actionPerform();
        verifyForward("getTransactionHistory_success");
        StaticHibernateUtil.flushSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, loan.getAccountId());
        List<TransactionHistoryDto> trxnHistoryList = (List<TransactionHistoryDto>) SessionUtils.getAttribute(
                SavingsConstants.TRXN_HISTORY_LIST, request);
        for (TransactionHistoryDto transactionHistoryDto : trxnHistoryList) {
            Assert.assertEquals(accountBO.getUserContext().getName(), transactionHistoryDto.getPostedBy());
        }
    }
}
