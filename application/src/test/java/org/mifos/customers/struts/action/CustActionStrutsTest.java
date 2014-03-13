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

package org.mifos.customers.struts.action;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStateFlag;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;

@SuppressWarnings("unchecked")
public class CustActionStrutsTest extends MifosMockStrutsTestCase {

    private CenterBO center;

    private GroupBO group;

    private ClientBO client;

    private MeetingBO meeting;

    private String flowKey;

    private SavingsTestHelper helper = new SavingsTestHelper();

    private SavingsOfferingBO savingsOffering;

    private LoanBO loan1;

    private SavingsBO savings1;

    private LoanBO loan2;

    private SavingsBO savings2;

    private LoanBO loan3;

    private SavingsBO savings3;

    private UserContext userContext;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/customer-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {
        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");

        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, CustAction.class);
    }

    @After
    public void tearDown() throws Exception {
        loan1 = null;
        loan2 = null;
        loan3 = null;
        savings1 = null;
        savings2 = null;
        savings3 = null;
        client = null;
        group = null;
        center = null;
        userContext = null;
    }

    @Test
    public void testGetClosedAccounts() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createCustomers();
        createAccounts();
        setRequestPathInfo("/custAction.do");
        addRequestParameter("method", "getClosedAccounts");
        addRequestParameter("customerId", group.getCustomerId().toString());
        addRequestParameter("globalCustNum", group.getGlobalCustNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.getAllClosedAccounts.toString());

       Assert.assertEquals("Size of closed savings accounts should be 1 for group", 1, ((List<AccountBO>) SessionUtils
                .getAttribute(AccountConstants.CLOSEDSAVINGSACCOUNTSLIST, request)).size());
       Assert.assertEquals("Size of closed loan accounts should be 1 for group", 1, ((List<AccountBO>) SessionUtils
                .getAttribute(AccountConstants.CLOSEDLOANACCOUNTSLIST, request)).size());
    }

    @Test
    public void testGetBackToGroupDetailsPage() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createCustomers();
        setRequestPathInfo("/custAction.do");
        addRequestParameter("method", "getBackToDetailsPage");
        addRequestParameter("input", "group");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.group_detail_page.toString());
    }

    @Test
    public void testGetBackToCenterDetailsPage() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createCustomers();
        setRequestPathInfo("/custAction.do");
        addRequestParameter("method", "getBackToDetailsPage");
        addRequestParameter("input", "center");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.center_detail_page.toString());
    }

    @Test
    public void testGetBackToClientDetailsPage() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createCustomers();
        setRequestPathInfo("/custAction.do");
        addRequestParameter("method", "getBackToDetailsPage");
        addRequestParameter("input", "client");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.client_detail_page.toString());
    }

    private void createCustomers() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter(this.getClass().getSimpleName() + " Group",
                CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient(this.getClass().getSimpleName() + " Client",
                CustomerStatus.CLIENT_ACTIVE, group);
    }

    private LoanBO getLoanAccount(CustomerBO customerBO, String offeringName, String shortName) {
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName()
                + offeringName, shortName, startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", customerBO, AccountState.LOAN_APPROVED, startDate,
                loanOffering);
    }

    private SavingsBO getSavingsAccount(CustomerBO customerBO, String offeringName, String shortName) throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        savingsOffering = helper.createSavingsOffering(this.getClass().getSimpleName() + offeringName, shortName);
        return TestObjectFactory.createSavingsAccount("000100000000017", customerBO,
                AccountState.SAVINGS_PARTIAL_APPLICATION.getValue(), startDate, savingsOffering);
    }

    private void createAccounts() throws Exception {
        savings1 = getSavingsAccount(group, "fsaf6", "ads6");
        PersonnelBO loggedInUser = IntegrationTestObjectMother.testUser();
        savings1.changeStatus(AccountState.SAVINGS_CANCELLED, AccountStateFlag.SAVINGS_BLACKLISTED
                .getValue(), "status changed for savings", loggedInUser);
        savings1.update();
        loan1 = getLoanAccount(group, "fdsfsdf", "2cvs");
        loan1.update();
        loan1.changeStatus(AccountState.LOAN_CANCELLED, AccountStateFlag.LOAN_OTHER.getValue(),
                "status changed for loan", loggedInUser);
        StaticHibernateUtil.flushSession();
        savings2 = getSavingsAccount(group, "fsaf65", "ads5");
        loan2 = getLoanAccount(client, "rtwetrtwert", "5rre");
        savings3 = getSavingsAccount(center, "fsaf26", "ads2");
        loan3 = getLoanAccount(client, "fsdsdfqwq234", "13er");
    }
}
