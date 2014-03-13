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

package org.mifos.application.collectionsheet.struts.action;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.util.helpers.LoanAccountDto;
import org.mifos.accounts.loan.util.helpers.LoanAccountsProductDto;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestCalcType;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.productdefinition.util.helpers.RecommendedAmountUnit;
import org.mifos.accounts.productdefinition.util.helpers.SavingsType;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.util.helpers.SavingsAccountDto;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryDto;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryGridDto;
import org.mifos.application.collectionsheet.struts.actionforms.BulkEntryActionForm;
import org.mifos.application.collectionsheet.util.helpers.CollectionSheetEntryConstants;
import org.mifos.application.master.business.CustomValueListElementDto;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.CollectionSheetEntryFormDto;
import org.mifos.application.servicefacade.ListItem;
import org.mifos.application.servicefacade.ProductDto;
import org.mifos.application.servicefacade.TestCollectionSheetRetrieveSavingsAccountsUtils;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.config.AccountingRules;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.client.business.AttendanceType;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.client.business.service.ClientAttendanceDto;
import org.mifos.customers.office.util.helpers.OfficeConstants;
import org.mifos.customers.office.util.helpers.OfficeLevel;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerAccountDto;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.domain.CustomerDto;
import org.mifos.dto.domain.OfficeDetailsDto;
import org.mifos.dto.domain.PersonnelDto;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.login.util.helpers.LoginConstants;
import org.mifos.security.util.ActivityContext;
import org.mifos.security.util.UserContext;

/**
 * I test {@link CollectionSheetEntryAction}.
 */
public class BulkEntryActionStrutsTest extends MifosMockStrutsTestCase {

    /*
     * Setting this to true fixes the printing of stack traces to standard out, but seems to cause failures (MySQL threw
     * a "Deadlock found when trying to get lock; try restarting transaction" exception) only if
     * BulkEntryBusinessServiceIntegrationTest is run previously as part of the same suite.
     *
     * This is presumably a second problem which was always there but was masked by the first one.
     */
    private static final boolean SUPPLY_ENTERED_AMOUNT_PARAMETERS = false;
    private UserContext userContext;
    private CustomerBO center;
    private CustomerBO group;
    private ClientBO client;
    private AccountBO account;
    private LoanBO groupAccount;
    private LoanBO clientAccount;
    private SavingsBO centerSavingsAccount;
    private SavingsBO groupSavingsAccount;
    private SavingsBO clientSavingsAccount;
    private String flowKey;

    @After
    public void tearDown() throws Exception {
        centerSavingsAccount = null;
        groupSavingsAccount = null;
        clientSavingsAccount = null;
        groupAccount = null;
        clientAccount = null;
        account = null;
        client = null;
        group = null;
        center = null;
    }

    @Before
    public void setUp() throws Exception {
        enableCustomWorkingDays();
        userContext = TestUtils.makeUser();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        ActivityContext ac = new ActivityContext((short) 0, userContext.getBranchId().shortValue(), userContext.getId()
                .shortValue());
        request.getSession(false).setAttribute("ActivityContext", ac);
        flowKey = createFlow(request, CollectionSheetEntryAction.class);
    }

    @Test
    public void testSuccessfulCreate() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getSuccessfulBulkEntry();
        Calendar meetingDateCalendar = new GregorianCalendar();
        int year = meetingDateCalendar.get(Calendar.YEAR);
        int month = meetingDateCalendar.get(Calendar.MONTH);
        int day = meetingDateCalendar.get(Calendar.DAY_OF_MONTH);
        meetingDateCalendar = new GregorianCalendar(year, month, day);

        Date meetingDate = new Date(meetingDateCalendar.getTimeInMillis());
        HashMap<Integer, ClientAttendanceDto> clientAttendance = new HashMap<Integer, ClientAttendanceDto>();
        clientAttendance.put(1, getClientAttendanceDto(1, meetingDate));
        clientAttendance.put(2, getClientAttendanceDto(2, meetingDate));
        clientAttendance.put(3, getClientAttendanceDto(3, meetingDate));

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("attendanceSelected[0]", "2");

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestDateParameter("transactionDate", day + "/" + (month + 1) + "/" + year);

        if (SUPPLY_ENTERED_AMOUNT_PARAMETERS) {
            addParametersForEnteredAmount();
            addParametersForDisbursalEnteredAmount();
        }

        performNoErrors();

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "create");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("attendanceSelected[0]", "2");
        addRequestDateParameter("transactionDate", day + "/" + (month + 1) + "/" + year);
        addRequestParameter("customerId", "1");

        performNoErrors();
        verifyForward("create_success");
        Assert.assertNotNull(request.getAttribute(CollectionSheetEntryConstants.CENTER));
        Assert.assertEquals(request.getAttribute(CollectionSheetEntryConstants.CENTER), center.getDisplayName());
        StaticHibernateUtil.flushAndClearSession();
        groupAccount = TestObjectFactory.getObject(LoanBO.class, groupAccount.getAccountId());
        clientAccount = TestObjectFactory.getObject(LoanBO.class, clientAccount.getAccountId());
        centerSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, centerSavingsAccount.getAccountId());
        clientSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, clientSavingsAccount.getAccountId());
        groupSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, groupSavingsAccount.getAccountId());
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());

        Assert.assertEquals(1, client.getClientAttendances().size());
        Assert.assertEquals(AttendanceType.ABSENT, client.getClientAttendanceForMeeting(
                new java.sql.Date(meetingDateCalendar.getTimeInMillis())).getAttendanceAsEnum());
    }

    @Test
    public void testSuccessfulPreview() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getSuccessfulBulkEntry();
        Calendar meetinDateCalendar = new GregorianCalendar();
        int year = meetinDateCalendar.get(Calendar.YEAR);
        int month = meetinDateCalendar.get(Calendar.MONTH);
        int day = meetinDateCalendar.get(Calendar.DAY_OF_MONTH);
        meetinDateCalendar = new GregorianCalendar(year, month, day);

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("method", "preview");
        addRequestParameter("attendanceSelected[0]", "1");
        addRequestParameter("enteredAmount[0][0]", "212.0");
        addRequestParameter("enteredAmount[1][1]", "212.0");
        addRequestParameter("enteredAmount[0][1]", "212.0");
        addRequestParameter("enteredAmount[1][0]", "212.0");
        addRequestParameter("withDrawalAmountEntered[2][2]", "100.0");
        addRequestParameter("depositAmountEntered[2][2]", "100.0");
        addRequestParameter("withDrawalAmountEntered[0][0]", "100.0");
        addRequestParameter("depositAmountEntered[0][0]", "100.0");
        addRequestDateParameter("transactionDate", day + "/" + (month + 1) + "/" + year);
        performNoErrors();
        verifyForward("preview_success");

        groupAccount = TestObjectFactory.getObject(LoanBO.class, groupAccount.getAccountId());
        clientAccount = TestObjectFactory.getObject(LoanBO.class, clientAccount.getAccountId());
        centerSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, centerSavingsAccount.getAccountId());
        clientSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, clientSavingsAccount.getAccountId());
        groupSavingsAccount = TestObjectFactory.getObject(SavingsBO.class, groupSavingsAccount.getAccountId());
        center = TestObjectFactory.getCustomer(center.getCustomerId());
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());

    }

    @Test
    public void testFailurePreview() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getFailureBulkEntry();
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("method", "preview");
        addRequestParameter("customerAccountAmountEntered[0][6]", "");
        addRequestParameter("customerAccountAmountEntered[1][6]", "abc");
        actionPerform();

        verifyActionErrors(new String[]{"errors.invalidaccollections", "errors.invalidaccollections"});

    }

    @Test
    public void testLoad() throws PageExpiredException {
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "load");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward("load_success");
        Assert.assertEquals("The value for isBackDated Trxn Allowed", SessionUtils.getAttribute(
                CollectionSheetEntryConstants.ISBACKDATEDTRXNALLOWED, request), Constants.NO);
        Assert.assertEquals("The value for isCenter Hierarchy Exists", SessionUtils.getAttribute(
                CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, request), Constants.YES);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadPersonnel() throws PageExpiredException {
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "loadLoanOfficers");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.COLLECTION_SHEET_ENTRY_FORM_DTO,
                createDefaultCollectionSheetDto(), request);

        actionPerform();
        verifyForward("load_success");
        List<PersonnelDto> loanOfficerList = (List<PersonnelDto>) SessionUtils.getAttribute(
                CustomerConstants.LOAN_OFFICER_LIST, request);
        Assert.assertEquals(1, loanOfficerList.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadCustomers() throws PageExpiredException {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active", meeting);

        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "loadCustomerList");
        addRequestParameter("officeId", "3");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.COLLECTION_SHEET_ENTRY_FORM_DTO,
                createDefaultCollectionSheetDto(), request);

        actionPerform();
        verifyForward("load_success");
        List<CustomerDto> parentCustomerList = (List<CustomerDto>) SessionUtils.getAttribute(
                CollectionSheetEntryConstants.CUSTOMERSLIST, request);

        Assert.assertEquals(1, parentCustomerList.size());
        Assert.assertEquals("The value for isCenter Hierarchy Exists", SessionUtils.getAttribute(
                CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, request), Constants.YES);
    }

    @Test
    public void testGetLastMeetingDateForCustomer() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active", meeting);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "getLastMeetingDateForCustomer");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("officeId", "3");
        addRequestParameter("loanOfficerId", "1");
        addRequestParameter("customerId", String.valueOf(center.getCustomerId().intValue()));

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.COLLECTION_SHEET_ENTRY_FORM_DTO,
                createDefaultCollectionSheetDto(), request);
        Short officeId = center.getOfficeId();
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward("load_success");

        if (AccountingRules.isBackDatedTxnAllowed()) {
            Assert.assertEquals("The value for isBackDated Trxn Allowed", SessionUtils.getAttribute(
                    CollectionSheetEntryConstants.ISBACKDATEDTRXNALLOWED, request), Constants.YES);
            Assert.assertEquals(new java.sql.Date(DateUtils.getDateWithoutTimeStamp(getMeetingDates(officeId, meeting).getTime())
                    .getTime()).toString(), SessionUtils.getAttribute("LastMeetingDate", request).toString());
            Assert.assertEquals(new java.util.Date(DateUtils
                    .getDateWithoutTimeStamp(getMeetingDates(officeId, meeting).getTime()).getTime()), DateUtils
                    .getDate(((BulkEntryActionForm) request.getSession().getAttribute(
                    CollectionSheetEntryConstants.BULKENTRYACTIONFORM)).getTransactionDate()));
        } else {
            Assert.assertEquals("The value for isBackDated Trxn Allowed", SessionUtils.getAttribute(
                    CollectionSheetEntryConstants.ISBACKDATEDTRXNALLOWED, request), Constants.NO);
            Assert.assertEquals(new java.sql.Date(DateUtils.getDateWithoutTimeStamp(getMeetingDates(officeId, meeting).getTime())
                    .getTime()).toString(), SessionUtils.getAttribute("LastMeetingDate", request).toString());
            Assert
                    .assertEquals(DateUtils.getUserLocaleDate(getUserLocale(request), new java.sql.Date(DateUtils
                            .getCurrentDateWithoutTimeStamp().getTime()).toString()), ((BulkEntryActionForm) request
                            .getSession().getAttribute(CollectionSheetEntryConstants.BULKENTRYACTIONFORM))
                            .getTransactionDate());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSuccessfulGet() throws Exception {

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        account = getLoanAccount(group, meeting);

        // Using utility method that uses builder pattern to create savings accounts - TestObjectFactory was creating
        // installments for all savings accounts (which is wrong)
        TestCollectionSheetRetrieveSavingsAccountsUtils collectionSheetRetrieveSavingsAccountsUtils = new TestCollectionSheetRetrieveSavingsAccountsUtils();
        centerSavingsAccount = collectionSheetRetrieveSavingsAccountsUtils.createSavingsAccount(center, "cemi",
                "120.00", false, false);
        groupSavingsAccount = collectionSheetRetrieveSavingsAccountsUtils.createSavingsAccount(group, "gvcg", "180.00",
                true, false);
        clientSavingsAccount = collectionSheetRetrieveSavingsAccountsUtils.createSavingsAccount(client, "clm",
                "222.00", false, false);

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId(center.getCustomerId());
        customerDto.setCustomerSearchId(center.getSearchId());
        customerDto.setCustomerLevelId(center.getCustomerLevel().getId());

        final OfficeDetailsDto officeDetailsDto = new OfficeDetailsDto(Short.valueOf("3"), "", OfficeLevel.BRANCHOFFICE.getValue(), "levelNameKey",
                Integer.valueOf(-1));
        final PersonnelDto personnelDto = new PersonnelDto(Short.valueOf("3"), "");

        SessionUtils.setAttribute(CollectionSheetEntryConstants.COLLECTION_SHEET_ENTRY_FORM_DTO,
                createCollectionSheetDto(customerDto, officeDetailsDto, personnelDto), request);

        SessionUtils.setCollectionAttribute(CollectionSheetEntryConstants.PAYMENT_TYPES_LIST, Arrays
                .asList(getPaymentTypeView()), request);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, Constants.YES, request);

        setMasterListInSession(center.getCustomerId());
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "get");
        addRequestParameter("officeId", "3");
        addRequestParameter("loanOfficerId", "3");
        addRequestParameter("paymentId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);

        Calendar meetinDateCalendar = new GregorianCalendar();
        meetinDateCalendar.setTime(getMeetingDates(account.getOffice().getOfficeId(), meeting));
        int year = meetinDateCalendar.get(Calendar.YEAR);
        int month = meetinDateCalendar.get(Calendar.MONTH);
        int day = meetinDateCalendar.get(Calendar.DAY_OF_MONTH);
        meetinDateCalendar = new GregorianCalendar(year, month, day);
        SessionUtils.setAttribute("LastMeetingDate", new java.sql.Date(meetinDateCalendar.getTimeInMillis()), request);
        addRequestDateParameter("transactionDate", day + "/" + (month + 1) + "/" + year);
        addRequestParameter("receiptId", "1");
        addRequestDateParameter("receiptDate", "20/03/2006");
        addRequestParameter("customerId", String.valueOf(center.getCustomerId().intValue()));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward("get_success");
    }

    @Test
    public void testFailureGet() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getSuccessfulBulkEntry();
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, Constants.YES, request);
        SessionUtils.setAttribute("LastMeetingDate", bulkEntry.getTransactionDate(), request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "get");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[]{"errors.mandatoryenter", "errors.mandatoryselect", "errors.mandatoryselect",
                "errors.mandatoryselect", "errors.mandatoryselect"});
    }

    @Test
    public void testFailurePreviewForEmptyAmount() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getFailureBulkEntry();
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("customerAccountAmountEntered[0][6]", "");
        addRequestParameter("customerAccountAmountEntered[1][6]", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[]{"errors.invalidaccollections", "errors.invalidaccollections"});
    }

    @Test
    public void testFailurePreviewForCharAmount() throws Exception {
        CollectionSheetEntryGridDto bulkEntry = getFailureBulkEntry();
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SessionUtils.setAttribute(CollectionSheetEntryConstants.BULKENTRY, bulkEntry, request);
        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("customerAccountAmountEntered[0][6]", "abc");
        addRequestParameter("customerAccountAmountEntered[1][6]", "abc");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyActionErrors(new String[]{"errors.invalidaccollections", "errors.invalidaccollections"});
    }

    @Test
    public void testValidateForLoadMethod() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);

        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter("input", "load");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.load_success.toString());

    }

    @Test
    public void testValidateForGetMethod() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);

        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter("input", "get");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.get_success.toString());

    }

    @Test
    public void testValidateForPreviewMethod() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);

        setRequestPathInfo("/collectionsheetaction.do");
        addRequestParameter("method", "validate");
        addRequestParameter("input", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.preview_success.toString());

    }

    private ClientAttendanceDto getClientAttendanceDto(final Integer clientId, final Date meetingDate) {
        ClientAttendanceDto clientAttendanceDto = new ClientAttendanceDto(clientId, meetingDate, AttendanceType.ABSENT
                .getValue());
        return clientAttendanceDto;
    }

    private void addParametersForEnteredAmount() {
        for (int i = 0; i < 4; ++i) {
            addRequestParameter("enteredAmount[" + i + "][0]", "300.0");
            addRequestParameter("enteredAmount[" + i + "][1]", "300.0");
        }
    }

    private void addParametersForDisbursalEnteredAmount() {
        for (int i = 0; i < 4; ++i) {
            addRequestParameter("enteredAmount[" + i + "][5]", "300.0");
            addRequestParameter("enteredAmount[" + i + "][6]", "300.0");
        }
    }

    private CollectionSheetEntryGridDto getSuccessfulBulkEntry() throws Exception {

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        Date startDate = new Date(System.currentTimeMillis());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering1 = TestObjectFactory.createLoanOffering(startDate, meeting);
        LoanOfferingBO loanOffering2 = TestObjectFactory.createLoanOffering("Loan2345", "313f", ApplicableTo.CLIENTS,
                startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        groupAccount = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering1);
        clientAccount = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 1, loanOffering2);
        Date currentDate = new Date(System.currentTimeMillis());
        // 2010-01-18: JohnW - This use of TestObjectFactory.createSavingsProduct used to break a number of
        // business rules.
        // E.g all the savings products were set up as applicable for Groups and "per individual" which is wrong for
        // centers and clients. In the case of the client being wrongly set up as "Per Individual" it now triggers save
        // collection sheet validation (which checks that the account associated with the client marked "per individual"
        // matches its parent group or center)
        // Also, when creating the center savings accounts (which is effectively "per individual") and the group savings
        // account which is "per individual" the saving_schedule entries for the client are not written (they are in the
        // production code).
        //
        // Considered using/updating the savingsProductBuilder functionality but that doesn't deal with the
        // "per individual" aspect either (update: it does, but still problem with builder creating installments).
        // Decided not to try and fix it up (good deal of effort involved) but rather change the
        // TestObjectFactory.createSavingsProduct to accept a RecommendedAmountUnit parameter.
        // Unfortunately it wouldn't allow a null parameter (which is valid for centers and clients) through so, where
        // necessary, I picked a value that worked for the test but was wrong in a business rule sense (just as its
        // always been).
        // So the savings product test data doesn't adhere to business rules but all tests pass here and in others
        // tests.
        SavingsOfferingBO savingsOffering1 = TestObjectFactory.createSavingsProduct("SavingPrd1", "ased", currentDate,
                RecommendedAmountUnit.COMPLETE_GROUP);
        SavingsOfferingBO savingsOffering2 = TestObjectFactory.createSavingsProduct("SavingPrd2", "cvdf", currentDate,
                RecommendedAmountUnit.COMPLETE_GROUP);
        SavingsOfferingBO savingsOffering3 = TestObjectFactory.createSavingsProduct("SavingPrd3", "zxsd", currentDate,
                RecommendedAmountUnit.COMPLETE_GROUP);

        centerSavingsAccount = TestObjectFactory.createSavingsAccount("43244334", center, Short.valueOf("16"),
                startDate, savingsOffering1);
        groupSavingsAccount = TestObjectFactory.createSavingsAccount("43234434", group, Short.valueOf("16"), startDate,
                savingsOffering2);
        clientSavingsAccount = TestObjectFactory.createSavingsAccount("43245434", client, Short.valueOf("16"),
                startDate, savingsOffering3);

        CollectionSheetEntryDto bulkEntryParent = new CollectionSheetEntryDto(getCusomerView(center), null);
        SavingsAccountDto centerSavingsAccountView = getSavingsAccountView(centerSavingsAccount);
        centerSavingsAccountView.setDepositAmountEntered("100");
        centerSavingsAccountView.setWithDrawalAmountEntered("10");
        bulkEntryParent.addSavingsAccountDetail(centerSavingsAccountView);
        bulkEntryParent.setCustomerAccountDetails(getCustomerAccountView(center));

        CollectionSheetEntryDto bulkEntryChild = new CollectionSheetEntryDto(getCusomerView(group), null);
        LoanAccountDto groupLoanAccountView = getLoanAccountView(groupAccount);
        SavingsAccountDto groupSavingsAccountView = getSavingsAccountView(groupSavingsAccount);
        groupSavingsAccountView.setDepositAmountEntered("100");
        groupSavingsAccountView.setWithDrawalAmountEntered("10");
        bulkEntryChild.addLoanAccountDetails(groupLoanAccountView);
        bulkEntryChild.addSavingsAccountDetail(groupSavingsAccountView);
        bulkEntryChild.setCustomerAccountDetails(getCustomerAccountView(group));

        CollectionSheetEntryDto bulkEntrySubChild = new CollectionSheetEntryDto(getCusomerView(client), null);
        LoanAccountDto clientLoanAccountView = getLoanAccountView(clientAccount);
        clientLoanAccountView.setAmountPaidAtDisbursement(0.0);
        SavingsAccountDto clientSavingsAccountView = getSavingsAccountView(clientSavingsAccount);
        clientSavingsAccountView.setDepositAmountEntered("100");
        clientSavingsAccountView.setWithDrawalAmountEntered("10");
        bulkEntrySubChild.addLoanAccountDetails(clientLoanAccountView);
        bulkEntrySubChild.setAttendence(new Short("2"));
        bulkEntrySubChild.addSavingsAccountDetail(clientSavingsAccountView);
        bulkEntrySubChild.setCustomerAccountDetails(getCustomerAccountView(client));

        bulkEntryChild.addChildNode(bulkEntrySubChild);
        bulkEntryParent.addChildNode(bulkEntryChild);

        LoanAccountsProductDto childView = bulkEntryChild.getLoanAccountDetails().get(0);
        childView.setPrdOfferingId(groupLoanAccountView.getPrdOfferingId());
        childView.setEnteredAmount("100.0");
        LoanAccountsProductDto subchildView = bulkEntrySubChild.getLoanAccountDetails().get(0);
        subchildView.setDisBursementAmountEntered(clientAccount.getLoanAmount().toString());
        subchildView.setPrdOfferingId(clientLoanAccountView.getPrdOfferingId());

        ProductDto loanOfferingDto = new ProductDto(loanOffering1.getPrdOfferingId(), loanOffering1
                .getPrdOfferingShortName());
        ProductDto loanOfferingDto2 = new ProductDto(loanOffering2.getPrdOfferingId(), loanOffering2
                .getPrdOfferingShortName());

        List<ProductDto> loanProducts = Arrays.asList(loanOfferingDto, loanOfferingDto2);

        ProductDto savingsOfferingDto = new ProductDto(savingsOffering1.getPrdOfferingId(), savingsOffering1
                .getPrdOfferingShortName());
        ProductDto savingsOfferingDto2 = new ProductDto(savingsOffering2.getPrdOfferingId(), savingsOffering2
                .getPrdOfferingShortName());
        ProductDto savingsOfferingDto3 = new ProductDto(savingsOffering3.getPrdOfferingId(), savingsOffering3
                .getPrdOfferingShortName());
        List<ProductDto> savingsProducts = Arrays.asList(savingsOfferingDto, savingsOfferingDto2, savingsOfferingDto3);

        final PersonnelDto loanOfficer = getPersonnelView(center.getPersonnel());
        final OfficeDetailsDto officeDetailsDto = null;
        final List<CustomValueListElementDto> attendanceTypesList = new ArrayList<CustomValueListElementDto>();

        bulkEntryParent.setCountOfCustomers(3);
        final CollectionSheetEntryGridDto bulkEntry = new CollectionSheetEntryGridDto(bulkEntryParent, loanOfficer,
                officeDetailsDto, getPaymentTypeView(), startDate, "324343242", startDate, loanProducts, savingsProducts,
                attendanceTypesList);

        return bulkEntry;
    }

    private CollectionSheetEntryGridDto getFailureBulkEntry() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering1 = TestObjectFactory.createLoanOffering(startDate, meeting);
        LoanOfferingBO loanOffering2 = TestObjectFactory.createLoanOffering("Loan2345", "313f", startDate, meeting);
        groupAccount = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering1);
        clientAccount = TestObjectFactory.createLoanAccount("3243", client, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering2);
        MeetingBO meetingIntCalc = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_WEEK, CUSTOMER_MEETING));
        MeetingBO meetingIntPost = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_WEEK, CUSTOMER_MEETING));
        SavingsOfferingBO savingsOffering = TestObjectFactory.createSavingsProduct("SavingPrd123c", "ased",
                ApplicableTo.GROUPS, startDate, PrdStatus.SAVINGS_ACTIVE, 300.0, RecommendedAmountUnit.PER_INDIVIDUAL,
                1.2, 200.0, 200.0, SavingsType.VOLUNTARY, InterestCalcType.MINIMUM_BALANCE, meetingIntCalc,
                meetingIntPost);
        SavingsOfferingBO savingsOffering1 = TestObjectFactory.createSavingsProduct("SavingPrd1we", "vbgr",
                ApplicableTo.GROUPS, startDate, PrdStatus.SAVINGS_ACTIVE, 300.0, RecommendedAmountUnit.PER_INDIVIDUAL,
                1.2, 200.0, 200.0, SavingsType.VOLUNTARY, InterestCalcType.MINIMUM_BALANCE, meetingIntCalc,
                meetingIntPost);
        centerSavingsAccount = TestObjectFactory.createSavingsAccount("432434", center, Short.valueOf("16"), startDate,
                savingsOffering);
        clientSavingsAccount = TestObjectFactory.createSavingsAccount("432434", client, Short.valueOf("16"), startDate,
                savingsOffering1);

        CollectionSheetEntryDto bulkEntryParent = new CollectionSheetEntryDto(getCusomerView(center), null);
        bulkEntryParent.addSavingsAccountDetail(getSavingsAccountView(centerSavingsAccount));
        bulkEntryParent.setCustomerAccountDetails(getCustomerAccountView(center));

        CollectionSheetEntryDto bulkEntryChild = new CollectionSheetEntryDto(getCusomerView(group), null);
        LoanAccountDto groupLoanAccountView = getLoanAccountView(groupAccount);
        bulkEntryChild.addLoanAccountDetails(groupLoanAccountView);
        bulkEntryChild.setCustomerAccountDetails(getCustomerAccountView(group));
        CollectionSheetEntryDto bulkEntrySubChild = new CollectionSheetEntryDto(getCusomerView(client), null);
        LoanAccountDto clientLoanAccountView = getLoanAccountView(clientAccount);
        bulkEntrySubChild.addLoanAccountDetails(clientLoanAccountView);
        bulkEntrySubChild.addSavingsAccountDetail(getSavingsAccountView(clientSavingsAccount));
        bulkEntrySubChild.setCustomerAccountDetails(getCustomerAccountView(client));

        bulkEntryChild.addChildNode(bulkEntrySubChild);
        bulkEntryParent.addChildNode(bulkEntryChild);
        bulkEntryChild.getLoanAccountDetails().get(0).setEnteredAmount("100.0");
        bulkEntryChild.getLoanAccountDetails().get(0).setPrdOfferingId(groupLoanAccountView.getPrdOfferingId());
        bulkEntrySubChild.getLoanAccountDetails().get(0).setEnteredAmount("100.0");
        bulkEntrySubChild.getLoanAccountDetails().get(0).setPrdOfferingId(clientLoanAccountView.getPrdOfferingId());

        ProductDto loanOfferingDto = new ProductDto(loanOffering1.getPrdOfferingId(), loanOffering1
                .getPrdOfferingShortName());
        ProductDto loanOfferingDto2 = new ProductDto(loanOffering2.getPrdOfferingId(), loanOffering2
                .getPrdOfferingShortName());

        List<ProductDto> loanProducts = Arrays.asList(loanOfferingDto, loanOfferingDto2);

        ProductDto savingsOfferingDto = new ProductDto(savingsOffering.getPrdOfferingId(), savingsOffering
                .getPrdOfferingShortName());
        List<ProductDto> savingsProducts = Arrays.asList(savingsOfferingDto);

        final PersonnelDto loanOfficer = getPersonnelView(center.getPersonnel());
        final OfficeDetailsDto officeDetailsDto = null;
        final List<CustomValueListElementDto> attendanceTypesList = new ArrayList<CustomValueListElementDto>();

        bulkEntryParent.setCountOfCustomers(3);
        final CollectionSheetEntryGridDto bulkEntry = new CollectionSheetEntryGridDto(bulkEntryParent, loanOfficer,
                officeDetailsDto, getPaymentTypeView(), startDate, "324343242", startDate, loanProducts, savingsProducts,
                attendanceTypesList);

        return bulkEntry;
    }

    private CollectionSheetEntryFormDto createCollectionSheetDto(final CustomerDto customerDto,
                                                                 final OfficeDetailsDto officeDetailsDto, final PersonnelDto personnelDto) {

        List<ListItem<Short>> paymentTypesDtoList = new ArrayList<ListItem<Short>>();

        List<OfficeDetailsDto> activeBranches = Arrays.asList(officeDetailsDto);
        List<CustomerDto> customerList = Arrays.asList(customerDto);
        List<PersonnelDto> loanOfficerList = Arrays.asList(personnelDto);
        final Short reloadFormAutomatically = Constants.YES;
        final Short backDatedTransactionAllowed = Constants.NO;
        final Short centerHierarchyExists = Constants.YES;
        final Date meetingDate = new Date();

        return new CollectionSheetEntryFormDto(activeBranches, paymentTypesDtoList, loanOfficerList, customerList,
                reloadFormAutomatically, centerHierarchyExists, backDatedTransactionAllowed, meetingDate);
    }

    private CollectionSheetEntryFormDto createDefaultCollectionSheetDto() {

        List<OfficeDetailsDto> activeBranches = new ArrayList<OfficeDetailsDto>();
        List<ListItem<Short>> paymentTypesDtoList = new ArrayList<ListItem<Short>>();
        List<CustomerDto> customerList = new ArrayList<CustomerDto>();
        List<PersonnelDto> loanOfficerList = new ArrayList<PersonnelDto>();
        final Short reloadFormAutomatically = Constants.YES;
        final Short backDatedTransactionAllowed = Constants.NO;
        final Short centerHierarchyExists = Constants.YES;
        final Date meetingDate = new Date();

        return new CollectionSheetEntryFormDto(activeBranches, paymentTypesDtoList, loanOfficerList, customerList,
                reloadFormAutomatically, centerHierarchyExists, backDatedTransactionAllowed, meetingDate);
    }

    private LoanAccountDto getLoanAccountView(final LoanBO account) {
        LoanAccountDto accountView = TestObjectFactory.getLoanAccountView(account);
        List<AccountActionDateEntity> actionDates = new ArrayList<AccountActionDateEntity>();
        actionDates.add(account.getAccountActionDate((short) 1));
        accountView.addTrxnDetails(TestObjectFactory.getBulkEntryAccountActionViews(actionDates));

        return accountView;
    }

    private SavingsAccountDto getSavingsAccountView(final SavingsBO account) {
        final Integer customerId = null;
        final String savingOfferingShortName = account.getSavingsOffering().getPrdOfferingShortName();
        final Short savingOfferingId = account.getSavingsOffering().getPrdOfferingId();
        final Short savingsTypeId = account.getSavingsOffering().getSavingsType().getId();
        Short reccomendedAmountUnitId = null;
        if (account.getSavingsOffering().getRecommendedAmntUnit() != null) {
            reccomendedAmountUnitId = account.getSavingsOffering().getRecommendedAmntUnit().getId();
        }

        SavingsAccountDto accountView = new SavingsAccountDto(account.getAccountId(), customerId,
                savingOfferingShortName, savingOfferingId, savingsTypeId, reccomendedAmountUnitId);
        accountView.addAccountTrxnDetail(TestObjectFactory.getBulkEntryAccountActionView(account
                .getAccountActionDate((short) 1)));

        return accountView;
    }

    private CustomerDto getCusomerView(final CustomerBO customer) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId(customer.getCustomerId());
        customerDto.setCustomerLevelId(customer.getCustomerLevel().getId());
        customerDto.setCustomerSearchId(customer.getSearchId());
        customerDto.setDisplayName(customer.getDisplayName());
        customerDto.setGlobalCustNum(customer.getGlobalCustNum());
        customerDto.setOfficeId(customer.getOffice().getOfficeId());
        if (null != customer.getParentCustomer()) {
            customerDto.setParentCustomerId(customer.getParentCustomer().getCustomerId());
        }
        customerDto.setPersonnelId(customer.getPersonnel().getPersonnelId());
        customerDto.setStatusId(customer.getCustomerStatus().getId());
        return customerDto;
    }

    private PersonnelDto getPersonnelView(final PersonnelBO personnel) {
        PersonnelDto personnelDto = new PersonnelDto(personnel.getPersonnelId(), personnel.getDisplayName());
        return personnelDto;
    }

    private ListItem<Short> getPaymentTypeView() {
        ListItem<Short> paymentTypeView = new ListItem<Short>(Short.valueOf("1"), "displayValue");
        return paymentTypeView;
    }

    private CustomerAccountDto getCustomerAccountView(final CustomerBO customer) {
        CustomerAccountDto customerAccountDto = new CustomerAccountDto(customer.getCustomerAccount().getAccountId(),
                getCurrency());

        List<AccountActionDateEntity> accountAction = new ArrayList<AccountActionDateEntity>();
        accountAction.add(customer.getCustomerAccount().getAccountActionDate(Short.valueOf("1")));
        customerAccountDto.setAccountActionDates(TestObjectFactory.getBulkEntryAccountActionViews(accountAction));
        customerAccountDto.setCustomerAccountAmountEntered("100.0");
        customerAccountDto.setValidCustomerAccountAmountEntered(true);
        return customerAccountDto;
    }

    private AccountBO getLoanAccount(final CustomerBO group, final MeetingBO meeting) {
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
    }

    private static java.util.Date getMeetingDates(short officeId, final MeetingBO meeting) {
        java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
        List<java.util.Date> dates = TestObjectFactory.getMeetingDatesThroughTo(officeId, meeting, currentDate);
        return dates.get(dates.size() - 1);
    }

    private void setMasterListInSession(final Integer customerId) throws PageExpiredException {
        OfficeDetailsDto office = new OfficeDetailsDto(Short.valueOf("3"), "Branch", OfficeConstants.BRANCHOFFICE, Integer
                .valueOf("0"));
        List<OfficeDetailsDto> branchOfficesList = new ArrayList<OfficeDetailsDto>();
        branchOfficesList.add(office);
        SessionUtils.setCollectionAttribute(OfficeConstants.OFFICESBRANCHOFFICESLIST, branchOfficesList, request);

        PersonnelDto personnel = new PersonnelDto(Short.valueOf("3"), "John");
        List<PersonnelDto> personnelList = new ArrayList<PersonnelDto>();
        personnelList.add(personnel);
        SessionUtils.setCollectionAttribute(CustomerConstants.LOAN_OFFICER_LIST, personnelList, request);

        CustomerDto parentCustomer = new CustomerDto(customerId, "Center_Active", Short.valueOf(CustomerLevel.CENTER
                .getValue()), "1.1");
        List<CustomerDto> customerList = new ArrayList<CustomerDto>();
        customerList.add(parentCustomer);
        SessionUtils.setCollectionAttribute(CollectionSheetEntryConstants.CUSTOMERSLIST, customerList, request);
    }

    private Locale getUserLocale(final HttpServletRequest request) {
        Locale locale = null;
        HttpSession session = request.getSession();
        if (session != null) {
            UserContext userContext = (UserContext) session.getAttribute(LoginConstants.USERCONTEXT);
            if (null != userContext) {
                locale = userContext.getCurrentLocale();

            }
        }
        return locale;
    }

    private LoanBO getLoanAccount(final AccountState state, final Date startDate, final int disbursalType,
                                  final LoanOfferingBO loanOfferingBO) {
        return TestObjectFactory.createLoanAccountWithDisbursement("99999999999", client, state, startDate,
                loanOfferingBO, disbursalType);

    }

}
