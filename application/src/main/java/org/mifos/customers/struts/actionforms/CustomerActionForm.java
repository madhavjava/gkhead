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

package org.mifos.customers.struts.actionforms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.application.admin.servicefacade.InvalidDateException;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.Methods;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.customers.center.util.helpers.ValidateMethods;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.domain.ApplicableAccountFeeDto;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.dto.domain.CustomerPositionDto;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.struts.actionforms.BaseActionForm;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.ExceptionConstants;
import org.mifos.framework.util.helpers.FilePaths;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;
import org.mifos.security.login.util.helpers.LoginConstants;
import org.mifos.security.util.UserContext;

/**
 * What's the difference between this and {@link CustActionForm} ?
 */
public abstract class CustomerActionForm extends BaseActionForm {

    protected String input;

    private String searchString;

    private String customerId;

    private String globalCustNum;

    private String displayName;

    private Address address;

    private String officeId;

    private String officeName;

    private String loanOfficerId;

    private String externalId;

    private String status;

    private String mfiJoiningDate;

    private String formedByPersonnel;

    private String trained;

    private String trainedDate;

    private List<ApplicableAccountFeeDto> defaultFees;
    private List<ApplicableAccountFeeDto> additionalFees;

    private String selectedFeeAmntList;

    private List<CustomFieldDto> customFields;

    private List<CustomerPositionDto> customerPositions;

    protected List<QuestionGroupDetail> questionGroups;
    private String nextOrPreview;

    public CustomerActionForm() {
        address = new Address();
        defaultFees = new ArrayList<ApplicableAccountFeeDto>();
        additionalFees = new ArrayList<ApplicableAccountFeeDto>();
        customFields = new ArrayList<CustomFieldDto>();
    }

    public List<ApplicableAccountFeeDto> getAdditionalFees() {
        return additionalFees;
    }

    public void setAdditionalFees(List<ApplicableAccountFeeDto> additionalFees) {
        this.additionalFees = additionalFees;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getCustomerIdAsInt() {
        if (customerId == null) {
            throw new NullPointerException("customerId is not set");
        }
        return Integer.parseInt(customerId);
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getGlobalCustNum() {
        return globalCustNum;
    }

    public void setGlobalCustNum(String globalCustNum) {
        this.globalCustNum = globalCustNum;
    }

    public List<CustomFieldDto> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<CustomFieldDto> customFields) {
        this.customFields = customFields;
    }

    public List<ApplicableAccountFeeDto> getDefaultFees() {
        return defaultFees;
    }

    public void setDefaultFees(List<ApplicableAccountFeeDto> defaultFees) {
        this.defaultFees = defaultFees;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getLoanOfficerId() {
        return loanOfficerId;
    }

    public void setLoanOfficerId(String loanOfficerId) {
        this.loanOfficerId = loanOfficerId;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getStatus() {
        return status;
    }

    public String getMfiJoiningDate() {
        return mfiJoiningDate;
    }

    public void setMfiJoiningDate(String mfiJoiningDate) {
        this.mfiJoiningDate = mfiJoiningDate;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelectedFeeAmntList() {
        return selectedFeeAmntList;
    }

    public void setSelectedFeeAmntList(String selectedFeeAmntList) {
        this.selectedFeeAmntList = selectedFeeAmntList;
    }

    public Short getOfficeIdValue() {
        return getShortValue(officeId);
    }

    public Short getLoanOfficerIdValue() {
        return getShortValue(loanOfficerId);
    }

    public CustomerStatus getStatusValue() {
        return StringUtils.isNotBlank(status) ? CustomerStatus.fromInt(Short.valueOf(status)) : null;
    }

    public Short getFormedByPersonnelValue() {
        return getShortValue(formedByPersonnel);
    }

    public String getTrained() {
        return trained;
    }

    public Short getTrainedValue() {
        return getShortValue(trained);
    }

    public void setTrained(String trained) {
        this.trained = trained;
    }

    public String getTrainedDate() {
        return trainedDate;
    }

    public void setTrainedDate(String trainedDate) {
        this.trainedDate = trainedDate;
    }

    public CustomFieldDto getCustomField(int i) {
        if (i >= customFields.size()) {
            customFields.add(new CustomFieldDto());
        }
        return customFields.get(i);
    }

    public String getFormedByPersonnel() {
        return formedByPersonnel;
    }

    public void setFormedByPersonnel(String formedByPersonnel) {
        this.formedByPersonnel = formedByPersonnel;
    }

    public ApplicableAccountFeeDto getDefaultFee(int i) {
        while (i >= defaultFees.size()) {
            defaultFees.add(new ApplicableAccountFeeDto());
        }
        return defaultFees.get(i);
    }

    public List<ApplicableAccountFeeDto> getFeesToApply() {
        List<ApplicableAccountFeeDto> feesToApply = new ArrayList<ApplicableAccountFeeDto>();
        for (ApplicableAccountFeeDto fee : getAdditionalFees()) {
            if (fee.getFeeId() != null) {
                feesToApply.add(fee);
            }
        }
        for (ApplicableAccountFeeDto fee : getDefaultFees()) {
            if (!fee.isRemoved()) {
                feesToApply.add(fee);
            }
        }
        return feesToApply;
    }

    public boolean isDefaultFeeRemoved() {
        for (ApplicableAccountFeeDto fee : getDefaultFees()) {
            if (fee.isRemoved()) {
                return true;
            }
        }
        return false;
    }

    public ApplicableAccountFeeDto getSelectedFee(int index) {
        while (index >= additionalFees.size()) {
            additionalFees.add(new ApplicableAccountFeeDto());
        }
        return additionalFees.get(index);
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        String method = request.getParameter(Methods.method.toString());
        if (method != null && method.equals(Methods.preview.toString())) {
            for (int i = 0; i < defaultFees.size(); i++) {
                // if an already checked fee is unchecked then the value set to
                // 0
                if (request.getParameter("defaultFee[" + i + "].feeRemoved") == null) {
                    defaultFees.get(i).setRemoved(false);
                }
            }
        }
        super.reset(mapping, request);
    }

    public List<CustomerPositionDto> getCustomerPositions() {
        return customerPositions;
    }

    public void setCustomerPositions(List<CustomerPositionDto> customerPositions) {
        this.customerPositions = customerPositions;
    }

    public CustomerPositionDto getCustomerPosition(int index) {
        while (index >= customerPositions.size()) {
            customerPositions.add(new CustomerPositionDto());
        }
        return customerPositions.get(index);
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        String method = request.getParameter("method");
        if (null == request.getAttribute(Constants.CURRENTFLOWKEY)) {
            request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));
        }
        ActionErrors errors = null;
        try {
            errors = validateFields(request, method);
        } catch (ApplicationException ae) {
            errors = new ActionErrors();
            errors.add(ae.getKey(), new ActionMessage(ae.getKey(), ae.getValues()));
        }
        if (null != errors && !errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", method);

        }
        errors.add(super.validate(mapping, request));
        return errors;
    }

    protected abstract ActionErrors validateFields(HttpServletRequest request, String method)
            throws ApplicationException;

    protected void validateName(ActionErrors errors) {
        if (StringUtils.isBlank(getDisplayName())) {
            errors.add(CustomerConstants.NAME, new ActionMessage(CustomerConstants.ERRORS_SPECIFY_NAME));
        }
    }

    protected void validateLO(ActionErrors errors) {
        if (StringUtils.isBlank(getLoanOfficerId())) {
            errors.add(CustomerConstants.LOAN_OFFICER, new ActionMessage(CustomerConstants.ERRORS_SELECT_LOAN_OFFICER));
        }
    }

    protected void validateFormedByPersonnel(ActionErrors errors) {
        if (StringUtils.isBlank(getFormedByPersonnel())) {
            errors.add(CustomerConstants.FORMED_BY_LOANOFFICER, new ActionMessage(
                    CustomerConstants.FORMEDBY_LOANOFFICER_BLANK_EXCEPTION));
        }
    }

    protected void validateMeeting(HttpServletRequest request, ActionErrors errors) throws ApplicationException {
        MeetingBO meeting = getCustomerMeeting(request);
        if (meeting == null) {
            errors.add(CustomerConstants.MEETING, new ActionMessage(CustomerConstants.ERRORS_SPECIFY_MEETING));
        }
    }

    protected void validateConfigurableMandatoryFields(HttpServletRequest request, ActionErrors errors,
            EntityType entityType) {
        checkForMandatoryFields(entityType.getValue(), errors, request);
    }

    @SuppressWarnings("unchecked")
    protected void validateCustomFieldsForCustomers(HttpServletRequest request, ActionErrors errors) {
        try {
            List<CustomFieldDto> customFieldDefs = (List<CustomFieldDto>) SessionUtils.getAttribute(
                    CustomerConstants.CUSTOM_FIELDS_LIST, request);
            for (CustomFieldDto customField : customFields) {
                for (CustomFieldDto customFieldDef : customFieldDefs) {
                    if (customField.getFieldId().equals(customFieldDef.getFieldId())) {
                        if (customFieldDef.isMandatory() && StringUtils.isBlank(customField.getFieldValue())) {
                            errors.add(CustomerConstants.CUSTOM_FIELD, new ActionMessage(
                                    CustomerConstants.ERRORS_SPECIFY_CUSTOM_FIELD_VALUE));
                        }
                        if (CustomFieldType.fromInt(customField.getFieldId()).equals(CustomFieldType.DATE) &&
                                (StringUtils.isNotBlank(customField.getFieldValue()))) {
                            try {
                                DateUtils.getDate(customField.getFieldValue());
                            } catch (Exception e) {
                                errors.add(CustomerConstants.CUSTOM_FIELD, new ActionMessage(
                                        CustomerConstants.ERRORS_CUSTOM_DATE_FIELD));
                            }
                        }
                        break;
                    }
                }
            }
        } catch (PageExpiredException pee) {
            errors.add(ExceptionConstants.PAGEEXPIREDEXCEPTION, new ActionMessage(
                    ExceptionConstants.PAGEEXPIREDEXCEPTION));
        }
    }

    protected abstract MeetingBO getCustomerMeeting(HttpServletRequest request) throws ApplicationException;

    protected void validateFees(HttpServletRequest request, ActionErrors errors) throws ApplicationException {
        Locale locale = getUserContext(request).getPreferredLocale();
        validateForFeeRecurrence(request, errors);
        validateForFeeAmount(errors, locale);
        validateForDuplicatePeriodicFee(request, errors);
    }

    @SuppressWarnings("unchecked")
    protected void validateForFeeRecurrence(HttpServletRequest request, ActionErrors errors) throws ApplicationException {
        MeetingBO meeting = getCustomerMeeting(request);
        if (meeting != null) {
            List<ApplicableAccountFeeDto> feeList = getDefaultFees();
            for (ApplicableAccountFeeDto fee : feeList) {
                if (!fee.isRemoved() && fee.isPeriodic() && !isFrequencyMatches(fee, meeting)) {
                    errors.add(CustomerConstants.ERRORS_FEE_FREQUENCY_MISMATCH, new ActionMessage(
                            CustomerConstants.ERRORS_FEE_FREQUENCY_MISMATCH));
                    return;
                }
            }
            List<ApplicableAccountFeeDto> additionalFeeList = (List<ApplicableAccountFeeDto>) SessionUtils.getAttribute(CustomerConstants.ADDITIONAL_FEES_LIST, request);
            for (ApplicableAccountFeeDto selectedFee : getAdditionalFees()) {
                for (ApplicableAccountFeeDto fee : additionalFeeList) {
                    if (selectedFee.getFeeId() != null && selectedFee.getFeeId().equals(fee.getFeeId())) {
                        if (fee.isPeriodic() && !isFrequencyMatches(fee, meeting)) {
                            errors.add(CustomerConstants.ERRORS_FEE_FREQUENCY_MISMATCH, new ActionMessage(
                                    CustomerConstants.ERRORS_FEE_FREQUENCY_MISMATCH));
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isFrequencyMatches(ApplicableAccountFeeDto fee, MeetingBO meeting) {
        String feeRecur = fee.getFeeSchedule().split(" ")[0];
        return (((fee.isMonthly() && meeting.isMonthly())
                || (fee.isWeekly() && meeting.isWeekly())) 
                && Short.valueOf(feeRecur)%meeting.getRecurAfter() == 0);
    }

    protected void validateForFeeAmount(ActionErrors errors, Locale locale) {
        List<ApplicableAccountFeeDto> feeList = getFeesToApply();
        for (ApplicableAccountFeeDto fee : feeList) {
            if (StringUtils.isBlank(fee.getAmount())) {
                errors.add(CustomerConstants.FEE, new ActionMessage(CustomerConstants.ERRORS_SPECIFY_FEE_AMOUNT));
            } else {
                validateAmount(fee.getAmount(), CustomerConstants.FEE, errors);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void validateForDuplicatePeriodicFee(HttpServletRequest request, ActionErrors errors) throws ApplicationException {
        List<ApplicableAccountFeeDto> additionalFeeList = (List<ApplicableAccountFeeDto>) SessionUtils.getAttribute(CustomerConstants.ADDITIONAL_FEES_LIST, request);
        if (additionalFeeList == null) {
            additionalFeeList = new ArrayList<ApplicableAccountFeeDto>();
        }

        Set<Integer> additionalFeeIds = new HashSet<Integer>();
        for (ApplicableAccountFeeDto additionalFee : additionalFeeList) {
            additionalFeeIds.add(additionalFee.getFeeId());
        }

        Set<Integer> periodicFeeIds = new HashSet<Integer>();
        for (ApplicableAccountFeeDto additionalFee : getAdditionalFees()) {
            if (additionalFeeIds.contains(additionalFee.getFeeId())) {
                ApplicableAccountFeeDto originalFee = find(additionalFeeList, additionalFee);
                if (originalFee.isPeriodic()) {
                    boolean added = periodicFeeIds.add(additionalFee.getFeeId());
                    if (!added) {
                        errors.add(CustomerConstants.FEE, new ActionMessage(CustomerConstants.ERRORS_DUPLICATE_PERIODIC_FEE));
                        break;
                    }
                }
            }
        }
    }

    private ApplicableAccountFeeDto find(List<ApplicableAccountFeeDto> additionalFeeList, ApplicableAccountFeeDto additionalFee) {
        ApplicableAccountFeeDto match = null;
        for (ApplicableAccountFeeDto applicableAccountFeeDto : additionalFeeList) {
            if (applicableAccountFeeDto.getFeeId().equals(additionalFee.getFeeId())) {
                match = applicableAccountFeeDto;
            }
        }
        return match;
    }

    protected void validateTrained(HttpServletRequest request, ActionErrors errors) {
        if (request.getParameter("trained") == null) {
            trained = null;
        } else if (isCustomerTrained()) {
            if (ValidateMethods.isNullOrBlank(getTrainedDate())) {
                errors.add(CustomerConstants.TRAINED_DATE_MANDATORY, new ActionMessage(
                        CustomerConstants.TRAINED_DATE_MANDATORY));
            }

            else { // if marked trained and a date is supplied
                if (!DateUtils.isValidDate(getTrainedDate())) {
                    errors.add(CustomerConstants.INVALID_TRAINED_DATE, new ActionMessage(
                            CustomerConstants.INVALID_TRAINED_DATE));
                }
            }

        }
        // if training date is entered and trained is not selected, throw an
        // error
        if (!ValidateMethods.isNullOrBlank(getTrainedDate()) && ValidateMethods.isNullOrBlank(trained)) {
            errors.add(CustomerConstants.TRAINED_CHECKED, new ActionMessage(CustomerConstants.TRAINED_CHECKED));
        }

    }

    public boolean isCustomerTrained() {
        return StringUtils.isNotBlank(trained) && Short.valueOf(trained).equals(YesNoFlag.YES.getValue());
    }

    public Date getTrainedDateValue(Locale locale) throws InvalidDateException {
        return getDateFromString(getTrainedDate(), locale);
    }

    protected Locale getUserLocale(HttpServletRequest request) {
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

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getNextOrPreview() {
        return nextOrPreview;
    }

    public void setNextOrPreview(String nextOrPreview) {
        this.nextOrPreview = nextOrPreview;
    }
}
