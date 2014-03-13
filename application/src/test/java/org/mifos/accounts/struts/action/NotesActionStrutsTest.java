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
import java.sql.Date;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStates;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.MifosUser;
import org.mifos.security.util.ActivityContext;
import org.mifos.security.util.UserContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;


public class NotesActionStrutsTest extends MifosMockStrutsTestCase {



    private SavingsBO savingsBO;

    private LoanBO loanBO;

    private UserContext userContext;

    private CustomerBO client;

    private CustomerBO group;

    private CustomerBO center;

    private MeetingBO meeting;

    private SavingsTestHelper helper = new SavingsTestHelper();

    private SavingsOfferingBO savingsOffering;

    private String flowKey;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/accounts-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {
        userContext = TestUtils.makeUser();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        ActivityContext ac = new ActivityContext((short) 0, userContext.getBranchId().shortValue(), userContext.getId()
                .shortValue());
        request.getSession(false).setAttribute("ActivityContext", ac);
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);

        flowKey = createFlow(request, NotesAction.class);

        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
    }

    @After
    public void tearDown() throws Exception {
        savingsBO = null;
        loanBO = null;
        client = null;
        group = null;
        center = null;
    }

    @Test
    public void testLoad_Savings() throws Exception {
        savingsBO = getSavingsAccount("fsaf1", "ads1");
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", savingsBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Savings");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("load_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testPreview_Savings() throws Exception {
        savingsBO = getSavingsAccount("fsaf2", "ads2");
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter("accountId", savingsBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("preview_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testPrevious_Savings() throws Exception {
        savingsBO = getSavingsAccount("fsaf3", "ads3");
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "previous");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("previous_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testCancel_Savings() throws Exception {
        savingsBO = getSavingsAccount("fsaf4", "ads4");
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter("accountTypeId", savingsBO.getType().getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("savings_details_page");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testCreate_Savings() throws Exception {
        savingsBO = getSavingsAccount("fsaf5", "ads5");

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", savingsBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Savings");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("comment", "Notes created");
        getRequest().getSession().setAttribute("security_param", "Savings");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("savings_details_page");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testSearch_Savings() throws Exception {
        setMifosUserFromContext();

        savingsBO = getSavingsAccount("fsaf6", "ads6");

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", savingsBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Savings");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "create");
        getRequest().getSession().setAttribute("security_param", "Savings");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        StaticHibernateUtil.flushSession();

        addRequestParameter("globalAccountNum", savingsBO.getGlobalAccountNum());
        setRequestPathInfo("/savingsAction.do");
        addRequestParameter("method", "get");
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "search");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward("search_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
       Assert.assertEquals("Size of the search result should be 2", 2, ((QueryResult) SessionUtils.getAttribute(
                Constants.SEARCH_RESULTS, request)).getSize());
        StaticHibernateUtil.flushSession();
        savingsBO = TestObjectFactory.getObject(SavingsBO.class, savingsBO.getAccountId());
        getobjects();
    }

    private void getobjects() {

        client = TestObjectFactory.getCustomer(client.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        center = TestObjectFactory.getCustomer(center.getCustomerId());

    }

    @Test
    public void testLoad_Loan() {
        loanBO = getLoanAccount();
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", loanBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Loan");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("load_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testPreview_Loan() {
        loanBO = getLoanAccount();
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter("accountId", loanBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("preview_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testPrevious_Loan() {
        loanBO = getLoanAccount();
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "previous");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("previous_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testCancel_Loan() {
        loanBO = getLoanAccount();
        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter("accountTypeId", loanBO.getType().getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("loan_detail_page");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testCreate_Loan() {
        loanBO = getLoanAccount();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", loanBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Loan");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("comment", "Notes created");
        getRequest().getSession().setAttribute("security_param", "Loan");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("loan_detail_page");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testSearch_Loan() throws Exception {
        setMifosUserFromContext();

        loanBO = getLoanAccount();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", loanBO.getAccountId().toString());
        getRequest().getSession().setAttribute("security_param", "Loan");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "create");
        getRequest().getSession().setAttribute("security_param", "Loan");
        addRequestParameter("comment", "Notes created");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        StaticHibernateUtil.flushSession();
        addRequestParameter("globalAccountNum", loanBO.getGlobalAccountNum());
        setRequestPathInfo("/loanAccountAction.do");
        addRequestParameter("method", "get");
        actionPerform();

        setRequestPathInfo("/notesAction.do");
        addRequestParameter("method", "search");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward("search_success");
        verifyNoActionErrors();
        verifyNoActionMessages();

       Assert.assertEquals("Size of the search result should be 1", 1, ((QueryResult) SessionUtils.getAttribute(
                Constants.SEARCH_RESULTS, request)).getSize());
        StaticHibernateUtil.flushSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());

        getobjects();

    }

    private void createInitialObjects() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
    }

    private SavingsBO getSavingsAccount(String offeringName, String shortName) throws Exception {
        createInitialObjects();
        savingsOffering = helper.createSavingsOffering(offeringName, shortName);
        return TestObjectFactory.createSavingsAccount("000100000000017", client, AccountStates.SAVINGS_ACC_APPROVED,
                new Date(System.currentTimeMillis()), savingsOffering);
    }

    private LoanBO getLoanAccount() {
        createInitialObjects();
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", client, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
    }

    private void setMifosUserFromContext() {
        SecurityContext securityContext = new SecurityContextImpl();
        MifosUser principal = new MifosUser(userContext.getId(), userContext.getBranchId(), userContext.getLevelId(),
                new ArrayList<Short>(userContext.getRoles()), userContext.getName(), "".getBytes(),
                true, true, true, true, new ArrayList<GrantedAuthority>(), userContext.getLocaleId());
        Authentication authentication = new TestingAuthenticationToken(principal, principal);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
