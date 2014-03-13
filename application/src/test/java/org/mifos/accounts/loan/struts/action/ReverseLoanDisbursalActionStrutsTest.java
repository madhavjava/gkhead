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

package org.mifos.accounts.loan.struts.action;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.exceptions.AccountException;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.Methods;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;

public class ReverseLoanDisbursalActionStrutsTest extends MifosMockStrutsTestCase {

    private UserContext userContext;
    private LoanBO loan = null;
    private CenterBO center = null;
    protected GroupBO group = null;
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
        flowKey = createFlow(request, MultipleLoanAccountsCreationAction.class);
    }

    @After
    public void tearDown() throws Exception {
        loan = null;
        group = null;
        center = null;
        loan = null;
    }

    @Test
    public void testSearch() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "search");
        performNoErrors();
        verifyForward(ActionForwards.search_success.toString());
    }

    @Test
    public void testLoadWithoutAccountGlobalNum() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        actionPerform();
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.ERROR_LOAN_ACCOUNT_ID });
        verifyInputForward();
    }

    @Test
    public void testLoadForInvalidAccountNum() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", "123");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.NOSEARCHRESULTS });
        verifyForward(ActionForwards.search_success.toString());
    }

    @Ignore
    @Test
    public void testLoadForInvalidAccountState() {
        createLoanAccount();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.NOSEARCHRESULTS });
        verifyForward(ActionForwards.search_success.toString());
    }

    @Ignore
    @Test
    public void testLoad() throws AccountException, PageExpiredException, NumberFormatException, PersistenceException {
        createLoanAccount();
        disburseLoan();

        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());

        Assert.assertNotNull(SessionUtils.getAttribute(Constants.BUSINESS_KEY, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.PAYMENTS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.PAYMENTS_SIZE, request));
        StaticHibernateUtil.flushSession();
    }

    @Ignore
    @Test
    public void testPreviewWithoutNotes() throws AccountException, NumberFormatException, PersistenceException {
        createLoanAccount();
        disburseLoan();

        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        StaticHibernateUtil.flushSession();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        verifyActionErrors(new String[] { LoanConstants.MANDATORY });
        verifyInputForward();
    }

    @Ignore
    @Test
    public void testPreviewWithNoteGretaerThanMax() throws AccountException, NumberFormatException, PersistenceException {
        createLoanAccount();
        disburseLoan();

        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        StaticHibernateUtil.flushSession();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("note", "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789" + "0123456789012345678901234567890123456789");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();

        verifyActionErrors(new String[] { LoanConstants.MAX_LENGTH });
        verifyInputForward();

    }

    @Ignore
    @Test
    public void testPreview() throws AccountException, NumberFormatException, PersistenceException {
        createLoanAccount();
        disburseLoan();

        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        StaticHibernateUtil.flushSession();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("note", "0123456789012345678901234567890123456789");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        performNoErrors();
        verifyForward(ActionForwards.preview_success.toString());
    }

    @Ignore
    @Test
    public void testUpdate() throws AccountException, NumberFormatException, PersistenceException {
        createLoanAccount();
        disburseLoan();

        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "load");
        addRequestParameter("searchString", loan.getGlobalAccountNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        StaticHibernateUtil.flushSession();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("note", "0123456789012345678901234567890123456789");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        performNoErrors();
        verifyForward(ActionForwards.update_success.toString());

        StaticHibernateUtil.flushSession();
        loan = (LoanBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, loan.getAccountId());
    }

    @Test
    public void testCancel() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        performNoErrors();
        verifyForward(ActionForwards.cancel_success.toString());
    }

    @Test
    public void testValidate() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        performNoErrors();
        verifyForward(ActionForwards.search_success.toString());
    }

    @Test
    public void testValidateForPreview() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute("methodCalled", Methods.preview.toString());

        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
    }

    @Test
    public void testVaildateForUpdate() throws Exception {
        setRequestPathInfo("/reverseloandisbaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute("methodCalled", Methods.update.toString());

        performNoErrors();
        verifyForward(ActionForwards.preview_success.toString());
    }

    private void createInitialCustomers() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter(this.getClass().getSimpleName() + " Group",
                CustomerStatus.GROUP_ACTIVE, center);
    }

    private void createLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        createInitialCustomers();
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName() + " LOAN",
                "L", startDate, center.getCustomerMeeting().getMeeting());
        loan = TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_APPROVED, startDate,
                loanOffering);
    }

    private void disburseLoan() throws AccountException, NumberFormatException, PersistenceException {
        loan.setUserContext(userContext);
        loan.disburseLoan("4534", new Date(), Short.valueOf("1"), group.getPersonnel(), new Date(), Short.valueOf("1"));
        StaticHibernateUtil.flushSession();
    }
}
