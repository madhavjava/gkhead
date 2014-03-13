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

package org.mifos.customers.group.struts.action;

import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.accounts.fees.business.FeeDto;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStates;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.Methods;
import org.mifos.builders.MifosUserBuilder;
import org.mifos.config.ClientRules;
import org.mifos.customers.business.CustomerPositionEntity;
import org.mifos.customers.business.PositionEntity;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.center.persistence.CenterPersistence;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.group.struts.actionforms.GroupCustActionForm;
import org.mifos.customers.group.util.helpers.GroupConstants;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.domain.ApplicableAccountFeeDto;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.dto.screen.GroupInformationDto;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.components.fieldConfiguration.util.helpers.FieldConfig;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.struts.plugin.helper.EntityMasterData;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.MifosUser;
import org.mifos.security.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class GroupActionStrutsTest extends MifosMockStrutsTestCase {
    @Autowired
    private LegacyAccountDao legacyAccountDao;

    private CenterBO center;

    private GroupBO group;

    private ClientBO client;

    private MeetingBO meeting;

    private String flowKey;

    private final SavingsTestHelper helper = new SavingsTestHelper();

    private SavingsOfferingBO savingsOffering;

    private LoanBO loanBO;

    private SavingsBO savingsBO;

    private UserContext userContext;
    private final Short officeId = 3;

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
        flowKey = createFlow(request, GroupCustAction.class);
        EntityMasterData.getInstance().init();
        FieldConfig fieldConfig = FieldConfig.getInstance();
        fieldConfig.init();
        getActionServlet().getServletContext().setAttribute(Constants.FIELD_CONFIGURATION,
                fieldConfig.getEntityMandatoryFieldMap());

        SecurityContext securityContext = new SecurityContextImpl();
        MifosUser principal = new MifosUserBuilder().nonLoanOfficer().withAdminRole().build();
        Authentication authentication = new TestingAuthenticationToken(principal, principal);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        loanBO = null;
        savingsBO = null;
        client = null;
        group = null;
        center = null;
        userContext = null;
    }

    @Test
    public void testChooseOffice() throws Exception {
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "chooseOffice");
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.chooseOffice_success.toString());
    }

    @Test
    public void testHierarchyCheck() throws Exception {
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "hierarchyCheck");
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();

        boolean isCenterHierarchyExists = ClientRules.getCenterHierarchyExists();
        if (isCenterHierarchyExists) {
            verifyForward(ActionForwards.loadCenterSearch.toString());
        } else {
            verifyForward(ActionForwards.loadCreateGroup.toString());
        }
    }

    @Test
    public void testLoad_FeeDifferentFrequecny() throws Exception {
        createCenterWithoutFee();
        List<FeeDto> fees = getFees(RecurrenceType.MONTHLY);
        StaticHibernateUtil.flushAndClearSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.load_success.toString());

        boolean isCenterHierarchyExists = ClientRules.getCenterHierarchyExists();
        if (!isCenterHierarchyExists) {
            Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.LOAN_OFFICER_LIST, request));
        }
        List<ApplicableAccountFeeDto> additionalFees = getFeesFromSession();
        Assert.assertNotNull(additionalFees);
        Assert.assertEquals(0, additionalFees.size());
        Assert.assertNotNull(SessionUtils.getAttribute(GroupConstants.CENTER_HIERARCHY_EXIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.FORMEDBY_LOAN_OFFICER_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request));
        center = TestObjectFactory.getCenter(center.getCustomerId());
        removeFees(fees);
    }

    @Test
    public void testLoad_FeeSameFrequecny() throws Exception {
        createCenterWithoutFee();
        StaticHibernateUtil.flushAndClearSession();
        List<FeeDto> fees = getFees(RecurrenceType.WEEKLY);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.load_success.toString());

        boolean isCenterHierarchyExists = ClientRules.getCenterHierarchyExists();
        if (!isCenterHierarchyExists) {
            Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.LOAN_OFFICER_LIST, request));
        }
        List<ApplicableAccountFeeDto> additionalFees = getFeesFromSession();
        Assert.assertNotNull(additionalFees);
        Assert.assertEquals(1, additionalFees.size());
        Assert.assertNotNull(SessionUtils.getAttribute(GroupConstants.CENTER_HIERARCHY_EXIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.FORMEDBY_LOAN_OFFICER_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request));
        center = TestObjectFactory.getCenter(center.getCustomerId());
        removeFees(fees);
    }

    @Test
    public void testLoadMeeting() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "loadMeeting");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        getRequest().getSession().setAttribute("security_param", "GroupCreate");
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.loadMeeting_success.toString());
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testPreviewFailure_With_Name_Null() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals("Group Name", 1, getErrorSize(CustomerConstants.NAME));
        verifyInputForward();
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testPreviewFailure_TrainedWithoutTrainedDate() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("displayName", "group");
        addRequestParameter("trained", "1");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());

        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "11");
            i++;
        }
        actionPerform();
        Assert.assertEquals("Trained Date", 1, getErrorSize(CustomerConstants.TRAINED_DATE_MANDATORY));
        verifyInputForward();
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @SuppressWarnings("unchecked")
    private List<CustomFieldDto> getCustomFieldsFromSession() throws PageExpiredException {
        return (List<CustomFieldDto>) SessionUtils.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request);
    }

    @Test
    public void testFailurePreview_WithoutMandatoryCustomField_IfAny() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        boolean isCustomFieldMandatory = false;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            if (customFieldDef.isMandatory()) {
                isCustomFieldMandatory = true;
                break;
            }
        }
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("displayName", "group");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("loanOfficerId", center.getLoanOfficerId().toString());
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "");
            i++;
        }
        actionPerform();

        if (isCustomFieldMandatory) {
            Assert.assertEquals("CustomField", 1, getErrorSize(CustomerConstants.CUSTOM_FIELD));
            verifyInputForward();
        } else {
            Assert.assertEquals("CustomField", 0, getErrorSize(CustomerConstants.CUSTOM_FIELD));
            verifyForward(ActionForwards.preview_success.toString());
        }
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testFailurePreview_WithDuplicateFee() throws Exception {
        List<FeeDto> feesToRemove = getFees(RecurrenceType.WEEKLY);
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        List<ApplicableAccountFeeDto> feeList = getFeesFromSession();
        ApplicableAccountFeeDto fee = feeList.get(0);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("selectedFee[0].feeId", fee.getFeeId().toString());
        addRequestParameter("selectedFee[0].amount", "100");
        addRequestParameter("selectedFee[1].feeId", fee.getFeeId().toString());
        addRequestParameter("selectedFee[1].amount", "150");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals("Fee", 1, getErrorSize(CustomerConstants.FEE));
        verifyInputForward();
        removeFees(feesToRemove);
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testFailurePreview_WithFee_WithoutFeeAmount() throws Exception {
        List<FeeDto> feesToRemove = getFees(RecurrenceType.WEEKLY);
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();
        List<ApplicableAccountFeeDto> feeList = getFeesFromSession();
        ApplicableAccountFeeDto fee = feeList.get(0);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("selectedFee[0].feeId", fee.getFeeId().toString());
        addRequestParameter("selectedFee[0].amount", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals("Fee", 1, getErrorSize(CustomerConstants.FEE));
        verifyInputForward();
        removeFees(feesToRemove);
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testSuccessfulPreview() throws Exception {
        List<FeeDto> feesToRemove = getFees(RecurrenceType.WEEKLY);
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<ApplicableAccountFeeDto> feeList = getFeesFromSession();
        ApplicableAccountFeeDto fee = feeList.get(0);

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("displayName", "group");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());
        addRequestParameter("loanOfficerId", center.getLoanOfficerId().toString());
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "11");
            i++;
        }
        addRequestParameter("selectedFee[0].feeId", fee.getFeeId().toString());
        addRequestParameter("selectedFee[0].amount", fee.getAmount());
        actionPerform();
        Assert.assertEquals(0, getErrorSize());
        verifyForward(ActionForwards.preview_success.toString());
        verifyNoActionErrors();
        verifyNoActionMessages();
        StaticHibernateUtil.flushAndClearSession();
        removeFees(feesToRemove);
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.PENDING_APPROVAL_DEFINED, request));
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @SuppressWarnings("unchecked")
    private List<ApplicableAccountFeeDto> getFeesFromSession() throws PageExpiredException {
        return (List<ApplicableAccountFeeDto>) SessionUtils.getAttribute(CustomerConstants.ADDITIONAL_FEES_LIST, request);
    }

    @Test
    public void testSuccessfulPrevious() throws Exception {
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previous");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyForward(ActionForwards.previous_success.toString());
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    @Test
    public void testSuccessfulCreate_UnderCenter() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("displayName", "groupUnderCenter");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "11");
            i++;
        }
        actionPerform();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "create");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();

        verifyNoActionErrors();
        verifyForward(ActionForwards.create_success.toString());

        GroupCustActionForm actionForm = (GroupCustActionForm) request.getSession().getAttribute("groupCustActionForm");

        group = TestObjectFactory.getGroup(actionForm.getCustomerIdAsInt());
        center = TestObjectFactory.getCenter(center.getCustomerId());
        actionForm.setParentCustomer(null);
    }

    @Test
    public void testSuccessfulCreate_UnderBranch() throws Exception {
        createParentCustomer();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("branchId", officeId.toString());
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("displayName", "groupUnderBranch");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "11");
            i++;
        }
        actionPerform();

        SessionUtils.setAttribute(GroupConstants.CENTER_HIERARCHY_EXIST, Boolean.FALSE, request);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "create");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();

        verifyNoActionErrors();
        verifyForward(ActionForwards.create_success.toString());

        GroupCustActionForm actionForm = (GroupCustActionForm) request.getSession().getAttribute("groupCustActionForm");

        group = TestObjectFactory.getGroup(actionForm.getCustomerIdAsInt());
        center = TestObjectFactory.getCenter(center.getCustomerId());
        actionForm.setParentCustomer(null);
    }

    @Test
    public void testFailureCreate_DuplicateName() throws Exception {
        createGroupWithCenter();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("centerSystemId", center.getGlobalCustNum());
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("displayName", "group");
        addRequestParameter("status", CustomerStatus.GROUP_PENDING.getValue().toString());
        addRequestParameter("formedByPersonnel", center.getPersonnel().getPersonnelId().toString());
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "11");
            i++;
        }
        actionPerform();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "create");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));

        actionPerform();
        verifyActionErrors(new String[] { CustomerConstants.ERRORS_DUPLICATE_CUSTOMER });
        verifyForward(ActionForwards.create_failure.toString());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());

        GroupCustActionForm actionForm = (GroupCustActionForm) request.getSession().getAttribute("groupCustActionForm");
        actionForm.setParentCustomer(null);
    }

    @Ignore
    @Test
    public void testGet() throws Exception {
        createCustomers();
        CustomerPositionEntity customerPositionEntity = new CustomerPositionEntity(new PositionEntity((short) 1),
                client, client.getParentCustomer());
        group.addCustomerPosition(customerPositionEntity);
        savingsBO = getSavingsAccount("fsaf6", "ads6");
        loanBO = getLoanAccount();
        group.update();
        StaticHibernateUtil.flushAndClearSession();

        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "get");
        addRequestParameter("globalCustNum", group.getGlobalCustNum());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.get_success.toString());

        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        loanBO = (LoanBO) legacyAccountDao.getAccount(loanBO.getAccountId());
        savingsBO = (SavingsBO) legacyAccountDao.getAccount(savingsBO.getAccountId());

        GroupInformationDto groupInformationDto = (GroupInformationDto) SessionUtils.getAttribute(
                "groupInformationDto", request);

        Assert.assertEquals("Size of active loan accounts should be 1", 1, groupInformationDto.getLoanAccountsInUse().size());
        Assert.assertEquals("Size of active savings accounts should be 1", 1,groupInformationDto.getSavingsAccountsInUse().size());
        Assert.assertEquals("No of active (or on hold) clients should be 1", "1", groupInformationDto.getGroupPerformanceHistory().getActiveClientCount());
        Assert.assertEquals("No of clients that are not closed or cancelled should be 1", 1, groupInformationDto
                .getClientsOtherThanClosedAndCancelled().size());

        for (CustomerPositionEntity customerPosition : group.getCustomerPositions()) {
            Assert.assertEquals("Center Leader", customerPosition.getPosition().getName());
            break;
        }
        TestObjectFactory.removeCustomerFromPosition(group);
        StaticHibernateUtil.flushAndClearSession();
        center = TestObjectFactory.getCenter(Integer.valueOf(center.getCustomerId()).intValue());
        group = TestObjectFactory.getGroup(Integer.valueOf(group.getCustomerId()).intValue());
        client = TestObjectFactory.getClient(Integer.valueOf(client.getCustomerId()).intValue());
        loanBO = (LoanBO) legacyAccountDao.getAccount(loanBO.getAccountId());
        savingsBO = (SavingsBO) legacyAccountDao.getAccount(savingsBO.getAccountId());
    }

    @Test
    public void testManage() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.manage_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CLIENT_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.POSITIONS, request));
    }

    @Test
    public void testManageWithoutCenterHierarchy() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.manage_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.CLIENT_LIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(CustomerConstants.POSITIONS, request));
    }

    @Test
    public void testPreviewManageFailureForName() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter("trainedDate", "20/3/2006");
        actionPerform();
        Assert.assertEquals(1, getErrorSize());
        Assert.assertEquals("Group Name not present", 1, getErrorSize(CustomerConstants.NAME));

    }

    @Test
    public void testPreviewManageFailureForTrainedDate() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", "group");
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals(1, getErrorSize());
        Assert
                .assertEquals("Group Trained date not present", 1,
                        getErrorSize(CustomerConstants.TRAINED_DATE_MANDATORY));

    }

    @Test
    public void testPreviewManageFailureForTrained() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", "group");
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trainedDate", "20/03/2006");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals(1, getErrorSize());
        Assert.assertEquals("Group Trained checkbox not checked ", 1, getErrorSize(CustomerConstants.TRAINED_CHECKED));

    }

    @Test
    public void testPreviewManageSuccess() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", "group");
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter("trainedDate", "20/3/2006");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.previewManage_success.toString());

    }

    /**
     * This test asserts that when a group name for a trained group is edited, there are no errors and the trained date
     * is present in the action form.
     */
    @Test
    public void testPreviewManageSuccessForNameChange_AfterTrainedSet() throws Exception {

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.YEAR, 2008);

        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createTrainedGroupAndSetInSession(cal.getTime());
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();

        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", "group123"); // editing group name
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter("trainedDate", "2/6/2008");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();

        GroupCustActionForm actionForm = (GroupCustActionForm) getActionForm();

        Assert.assertEquals("2/6/2008", actionForm.getTrainedDate());
        Assert.assertEquals(0, getErrorSize());
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        String newDisplayName = "group_01";
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", newDisplayName);
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldType", customFieldDef.getFieldType().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter("trainedDate", "20/03/2006");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.previewManage_success.toString());
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionMessages();
        verifyForward(ActionForwards.update_success.toString());
        group = TestObjectFactory.getGroup(Integer.valueOf(group.getCustomerId()).intValue());
        Assert.assertTrue(group.isTrained());
        Assert.assertEquals(newDisplayName, group.getDisplayName());
    }

    @Test
    public void testUpdateSuccessForLogging() throws Exception {
        String newDisplayName = "group_01";
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", newDisplayName);
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldType", customFieldDef.getFieldType().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter("trained", "1");
        addRequestParameter("trainedDate", "20/03/2006");
        addRequestParameter("externalId", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.previewManage_success.toString());
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.update_success.toString());
        group = TestObjectFactory.getGroup(Integer.valueOf(group.getCustomerId()).intValue());
        Assert.assertTrue(group.isTrained());
        Assert.assertEquals(newDisplayName, group.getDisplayName());

    }

    @Test
    public void testUpdateSuccessWithoutTrained() throws Exception {
        String newDisplayName = "group_01";
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createGroupWithCenterAndSetInSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "manage");
        addRequestParameter("officeId", "3");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        List<CustomFieldDto> customFieldDefs = getCustomFieldsFromSession();
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "previewManage");
        addRequestParameter("officeId", "3");
        addRequestParameter("displayName", newDisplayName);
        int i = 0;
        for (CustomFieldDto customFieldDef : customFieldDefs) {
            addRequestParameter("customField[" + i + "].fieldId", customFieldDef.getFieldId().toString());
            addRequestParameter("customField[" + i + "].fieldType", customFieldDef.getFieldType().toString());
            addRequestParameter("customField[" + i + "].fieldValue", "Req");
            i++;
        }
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.previewManage_success.toString());
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.update_success.toString());

        group = (GroupBO) StaticHibernateUtil.getSessionTL().get(GroupBO.class, group.getCustomerId());
        Assert.assertTrue(!group.isTrained());
        Assert.assertEquals(newDisplayName, group.getDisplayName());

    }

    @Test
    public void testCancelSuccess() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter("input", GroupConstants.PREVIEW_MANAGE_GROUP);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.cancelEdit_success.toString());
    }

    @Test
    public void testCancelSuccessForCreateGroup() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter("input", GroupConstants.CREATE_GROUP);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.cancelCreate_success.toString());
    }

    @Test
    public void testLoadSearch() throws Exception {
        addActionAndMethod(Methods.loadSearch.toString());
        addCurrentFlowKey();
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.loadSearch_success.toString());
    }

    @Test
    public void testSearch() throws Exception {
        createGroupWithCenter();
        addRequestParameter("searchString", "gr");
        addActionAndMethod(Methods.search.toString());
        addCurrentFlowKey();
        actionPerform();
        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.search_success.toString());
        QueryResult queryResult = (QueryResult) SessionUtils.getAttribute(Constants.SEARCH_RESULTS, request);
        Assert.assertNotNull(queryResult);
        Assert.assertEquals(1, queryResult.getSize());
        Assert.assertEquals(1, queryResult.get(0, 10).size());

    }

    private void addActionAndMethod(String method) {
        setRequestPathInfo("/groupCustAction.do");
        addRequestParameter("method", method);

    }

    private void addCurrentFlowKey() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
    }

    private void createGroupWithCenterAndSetInSession() throws Exception {
        createGroupWithCenter();
        center = TestObjectFactory.getCenter(Integer.valueOf(center.getCustomerId()).intValue());
        group = TestObjectFactory.getGroup(Integer.valueOf(group.getCustomerId()).intValue());
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, group, request);
    }

    private void createTrainedGroupAndSetInSession(Date trainedDate) throws Exception {
        createGroupWithCenterAndSetInSession();
        group.setTrained(true);
        group.setTrainedDate(trainedDate);
    }

    private void createGroupWithCenter() throws Exception {
        createParentCustomer();
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("group", CustomerStatus.GROUP_ACTIVE, center);
        StaticHibernateUtil.flushAndClearSession();
    }

    private void createParentCustomer() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
    }

    private void createCenterWithoutFee() throws Exception {
        meeting = new MeetingBO(WeekDay.MONDAY, EVERY_WEEK, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        center = new CenterBO(userContext, "MyCenter", null, null, null, "1234", null, TestObjectFactory
                .getBranchOffice(), meeting, TestObjectFactory.getTestUser(), new CustomerPersistence());
        new CenterPersistence().saveCenter(center);
        StaticHibernateUtil.flushAndClearSession();
    }

    private void createCustomers() {
        createParentCustomer();
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        StaticHibernateUtil.flushAndClearSession();
    }

    private LoanBO getLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);

    }

    private SavingsBO getSavingsAccount(String offeringName, String shortName) throws Exception {
        savingsOffering = helper.createSavingsOffering(offeringName, shortName);
        return TestObjectFactory.createSavingsAccount("000100000000017", group, AccountStates.SAVINGS_ACC_APPROVED,
                new Date(System.currentTimeMillis()), savingsOffering);
    }

    private List<FeeDto> getFees(RecurrenceType frequency) {
        List<FeeDto> fees = new ArrayList<FeeDto>();
        AmountFeeBO fee1 = (AmountFeeBO) TestObjectFactory.createPeriodicAmountFee("PeriodicAmountFee",
                FeeCategory.GROUP, "200", frequency, Short.valueOf("2"));
        fees.add(new FeeDto(TestObjectFactory.getContext(), fee1));
        StaticHibernateUtil.flushAndClearSession();
        return fees;
    }

    private void removeFees(List<FeeDto> feesToRemove) {
//        for (FeeDto fee : feesToRemove) {
//            TestObjectFactory.cleanUp(new FeePersistence().getFee(fee.getFeeIdValue()));
//        }
    }
}
