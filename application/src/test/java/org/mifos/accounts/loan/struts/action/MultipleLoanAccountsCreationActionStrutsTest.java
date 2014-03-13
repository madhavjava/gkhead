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
import static org.mifos.application.meeting.util.helpers.RecurrenceType.MONTHLY;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_MONTH;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.service.LoanBusinessService;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.accounts.loan.util.helpers.LoanExceptionConstants;
import org.mifos.accounts.productdefinition.business.LoanAmountSameForAllLoanBO;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.Methods;
import org.mifos.builders.MifosUserBuilder;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.components.fieldConfiguration.util.helpers.FieldConfig;
import org.mifos.framework.struts.plugin.helper.EntityMasterData;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.MifosUser;
import org.mifos.security.util.UserContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class MultipleLoanAccountsCreationActionStrutsTest extends MifosMockStrutsTestCase {

    private UserContext userContext;

    protected AccountBO accountBO = null;

    protected CustomerBO center = null;

    protected CustomerBO group = null;

    @SuppressWarnings("unused")
    private CustomerBO client = null;

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
        accountBO = null;
        client = null;
        group = null;
        center = null;
    }

    @Test
    public void testLoad() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "load");
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_OFFICES_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, request));
    }

    @Test
    public void testGetLoanOfficersWithoutOffice() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getLoanOfficers");
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.MANDATORY_SELECT });
        verifyInputForward();
    }

    @Test
    public void testGetLoanOfficers() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "load");
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getLoanOfficers");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_OFFICES_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_LOAN_OFFICERS_LIST, request));
    }

    @Test
    public void testGetCentersWithoutOfficeAndLoanOfficer() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getCenters");
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.MANDATORY_SELECT, LoanConstants.MANDATORY_SELECT });
        verifyInputForward();
    }

    @Test
    public void testGetCenters() throws Exception {
        createInitialCustomers();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "load");
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getLoanOfficers");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getCenters");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_OFFICES_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_LOAN_OFFICERS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_CENTERS_LIST, request));
    }

    @Test
    public void testGetPrdOfferingsWithoutOfficeAndLoanOfficerAndCenter() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getPrdOfferings");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.MANDATORY_SELECT, LoanConstants.MANDATORY_SELECT,
                LoanConstants.MANDATORY_SELECT });
        verifyInputForward();
    }

    @Test
    public void testGetPrdOfferings() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering1 = getLoanOffering("Loan Offering123", "LOOF", ApplicableTo.CLIENTS, WEEKLY,
                EVERY_WEEK);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "load");
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getLoanOfficers");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getCenters");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getPrdOfferings");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_OFFICES_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_LOAN_OFFICERS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.MULTIPLE_LOANS_CENTERS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.LOANPRDOFFERINGS, request));
        TestObjectFactory.removeObject(loanOffering1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPrdOfferingsApplicableForCustomer() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialCustomers();
        LoanOfferingBO loanOffering1 = getLoanOffering("fdfsdfsd", "ertg", ApplicableTo.GROUPS, WEEKLY, EVERY_WEEK);
        LoanOfferingBO loanOffering2 = getLoanOffering("rwrfdb", "1qsd", ApplicableTo.GROUPS, WEEKLY, EVERY_WEEK);
        LoanOfferingBO loanOffering3 = getLoanOffering("mksgfgfd", "9u78", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);

        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getPrdOfferings");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertEquals(1, ((List<LoanOfferingBO>) SessionUtils.getAttribute(LoanConstants.LOANPRDOFFERINGS,
                request)).size());

        TestObjectFactory.removeObject(loanOffering1);
        TestObjectFactory.removeObject(loanOffering2);
        TestObjectFactory.removeObject(loanOffering3);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPrdOfferingsApplicableForCustomersWithMeeting() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialCustomers();
        LoanOfferingBO loanOffering1 = getLoanOffering("vcxvxc", "a123", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        LoanOfferingBO loanOffering2 = getLoanOffering("fgdsghdh", "4fdh", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        LoanOfferingBO loanOffering3 = getLoanOffering("mgkkkj", "6tyu", ApplicableTo.GROUPS, WEEKLY, EVERY_WEEK);
        LoanOfferingBO loanOffering4 = getLoanOffering("aq12sfdsf", "456j", ApplicableTo.CLIENTS, MONTHLY, EVERY_MONTH);
        LoanOfferingBO loanOffering5 = getLoanOffering("bdfhgfh", "6yu7", ApplicableTo.CLIENTS, WEEKLY, (short) 3);

        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "getPrdOfferings");
        addRequestParameter("branchOfficeId", "1");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        /*
         * Why two calls to actionPerform? Are we trying to test the case where the user clicks twice or is this just a
         * mistake?
         */
        actionPerform();
        performNoErrors();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertEquals(3, ((List<LoanOfferingBO>) SessionUtils.getAttribute(LoanConstants.LOANPRDOFFERINGS,
                request)).size());

        TestObjectFactory.removeObject(loanOffering1);
        TestObjectFactory.removeObject(loanOffering2);
        TestObjectFactory.removeObject(loanOffering3);
        TestObjectFactory.removeObject(loanOffering4);
        TestObjectFactory.removeObject(loanOffering5);
    }

    @Test
    public void testGetWithoutData() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "get");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.LOANOFFERINGNOTSELECTEDERROR, LoanConstants.MANDATORY_SELECT,
                LoanConstants.MANDATORY_SELECT, LoanConstants.MANDATORY_SELECT });
        verifyInputForward();
    }

    @Test
    public void testGetWithoutClients() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        LoanOfferingBO loanOffering1 = getLoanOffering("vcxvxc", "a123", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "get");
        addRequestParameter("branchOfficeId", center.getOffice().getOfficeId().toString());
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        addRequestParameter("loanOfficerId", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter("prdOfferingId", loanOffering1.getPrdOfferingId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[] { LoanConstants.NOSEARCHRESULTS });
        verifyForwardPath("/pages/application/loan/jsp/CreateMultipleLoanAccounts.jsp");
        TestObjectFactory.removeObject(loanOffering1);
    }

    @Test
    public void testGet() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialCustomers();
        LoanOfferingBO loanOffering = getLoanOffering("vcxvxc", "a123", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "get");
        addRequestParameter("branchOfficeId", center.getOffice().getOfficeId().toString());
        addRequestParameter("loanOfficerId", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("prdOfferingId", loanOffering.getPrdOfferingId().toString());
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter("centerSearchId", center.getSearchId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        performNoErrors();
        verifyForward(ActionForwards.get_success.toString());

        // this retrieve the loan purposes so this is 129 if empty lookup name
        // are removed
        Assert.assertEquals(131, ((List) SessionUtils.getAttribute(MasterConstants.BUSINESS_ACTIVITIES, request))
                .size());
        Assert.assertNotNull(SessionUtils.getAttribute(LoanConstants.LOANOFFERING, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.PENDING_APPROVAL_DEFINED, request));
        TestObjectFactory.removeObject(loanOffering);
    }

    @Test
    public void testCreateWithouSelectingClient() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialCustomers();
        LoanOfferingBO loanOffering = getLoanOffering("vcxvxc", "a123", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        // loanOffering.updateLoanOfferingSameForAllLoan(loanOffering);
        LoanAmountSameForAllLoanBO eligibleLoanAmountRange = loanOffering.getEligibleLoanAmountSameForAllLoan();

        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "get");
        addRequestParameter("branchOfficeId", center.getOffice().getOfficeId().toString());
        addRequestParameter("loanOfficerId", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("prdOfferingId", loanOffering.getPrdOfferingId().toString());
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter("centerSearchId", center.getSearchId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("clientDetails[0].loanAmount", eligibleLoanAmountRange.getDefaultLoanAmount().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("stateSelected", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("method", "create");
        addRequestParameter("stateSelected", "1");
        actionPerform();
        verifyActionErrors(new String[] { LoanExceptionConstants.SELECT_ATLEAST_ONE_RECORD });
        verifyInputForward();
        TestObjectFactory.removeObject(loanOffering);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() throws Exception {

        SecurityContext securityContext = new SecurityContextImpl();
        MifosUser principal = new MifosUserBuilder().nonLoanOfficer().withAdminRole().build();
        Authentication authentication = new TestingAuthenticationToken(principal, principal);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        EntityMasterData.getInstance().init();
        FieldConfig fieldConfig = FieldConfig.getInstance();
        fieldConfig.init();
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialCustomers();
        LoanOfferingBO loanOffering = getLoanOffering("vcxvxc", "a123", ApplicableTo.CLIENTS, WEEKLY, EVERY_WEEK);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "get");
        addRequestParameter("branchOfficeId", center.getOffice().getOfficeId().toString());
        addRequestParameter("loanOfficerId", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("prdOfferingId", loanOffering.getPrdOfferingId().toString());
        SessionUtils.setAttribute(LoanConstants.IS_CENTER_HIERARCHY_EXISTS, Constants.YES, request);
        addRequestParameter("centerId", center.getCustomerId().toString());
        addRequestParameter("centerSearchId", center.getSearchId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("clientDetails[0].loanAmount", loanOffering.getEligibleLoanAmountSameForAllLoan()
                .getDefaultLoanAmount().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("stateSelected", "1");
        addRequestParameter("clientDetails[0].selected", "true");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("method", "create");
        addRequestParameter("stateSelected", "1");
        actionPerform();
        verifyActionErrors(new String[] { LoanExceptionConstants.CUSTOMER_PURPOSE_OF_LOAN_FIELD });

        addRequestParameter("clientDetails[0].businessActivity", "0001");
        performNoErrors();
        verifyForward(ActionForwards.create_success.toString());

        List<String> accountNumbers = ((List<String>) request.getAttribute(LoanConstants.ACCOUNTS_LIST));
        Assert.assertEquals(1, accountNumbers.size());
        LoanBO loan = ApplicationContextProvider.getBean(LoanBusinessService.class).findBySystemId(accountNumbers.get(0));
        Assert.assertEquals(loanOffering.getEligibleLoanAmountSameForAllLoan().getDefaultLoanAmount().toString(), loan
                .getLoanAmount().toString());
        Assert.assertEquals(loanOffering.getDefInterestRate(), loan.getInterestRate());
        Assert.assertEquals(loanOffering.getEligibleInstallmentSameForAllLoan().getDefaultNoOfInstall(), loan
                .getNoOfInstallments());
        Assert.assertEquals(Short.valueOf("1"), loan.getGracePeriodDuration());
        Assert.assertEquals(Short.valueOf("1"), loan.getAccountState().getId());
        Assert.assertNull(request.getAttribute(Constants.CURRENTFLOWKEY));
        loan = null;
    }

    @Test
    public void testCancel() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward(ActionForwards.cancel_success.toString());
    }

    @Test
    public void testValidate() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyForward(ActionForwards.load_success.toString());
    }

    @Test
    public void testValidateForPreview() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute("methodCalled", Methods.load.toString());

        actionPerform();
        verifyNoActionErrors();
        verifyForward(ActionForwards.load_success.toString());
    }

    @Test
    public void testVaildateForCreate() throws Exception {
        setRequestPathInfo("/multipleloansaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute("methodCalled", Methods.create.toString());

        actionPerform();
        verifyNoActionErrors();
        verifyForward(ActionForwards.get_success.toString());
    }

    private LoanOfferingBO getLoanOffering(String name, String shortName, ApplicableTo applicableTo,
            RecurrenceType meetingFrequency, short recurAfter) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeeting(meetingFrequency,
                recurAfter, CUSTOMER_MEETING, WeekDay.MONDAY));
        Date currentDate = new Date(System.currentTimeMillis());
        return TestObjectFactory.createLoanOffering(name, shortName, applicableTo, currentDate, PrdStatus.LOAN_ACTIVE,
                300.0, 1.2, (short) 3, InterestType.FLAT, meeting);
    }

    private void createInitialCustomers() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
    }
}
