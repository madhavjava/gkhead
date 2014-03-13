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

package org.mifos.customers.group.struts.actionforms;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.questionnaire.struts.QuestionResponseCapturer;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.Methods;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.group.util.helpers.GroupConstants;
import org.mifos.customers.struts.actionforms.CustomerActionForm;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.dto.domain.ApplicableAccountFeeDto;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.dto.domain.CustomerPositionDto;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;

public class GroupCustActionForm extends CustomerActionForm  implements QuestionResponseCapturer{

    private CustomerBO parentCustomer;
    private String centerSystemId;
    private String parentOfficeId;
    private List<QuestionGroupDetail> questionGroups;

    public String getCenterSystemId() {
        return centerSystemId;
    }

    public void setCenterSystemId(String centerSystemId) {
        this.centerSystemId = centerSystemId;
    }

    public CustomerBO getParentCustomer() {
        return parentCustomer;
    }

    public void setParentCustomer(CustomerBO parentCustomer) {
        this.parentCustomer = parentCustomer;
    }

    public String getParentOfficeId() {
        return parentOfficeId;
    }

    public void setParentOfficeId(String parentOfficeId) {
        this.parentOfficeId = parentOfficeId;
    }

    @Override
    protected ActionErrors validateFields(HttpServletRequest request, String method) throws ApplicationException {
        ActionErrors errors = new ActionErrors();
        try {
            if (method.equals(Methods.previewManage.toString())) {
                validateName(errors);
                validateTrained(request, errors);
                validateConfigurableMandatoryFields(request, errors, EntityType.GROUP);
                validateCustomFieldsForCustomers(request, errors);
            } else if (method.equals(Methods.preview.toString()) || method.equals(Methods.previewOnly.toString())) {
                validateName(errors);
                validateLO(errors);
                validateFormedByPersonnel(errors);
                validateTrained(request, errors);
                validateConfigurableMandatoryFields(request, errors, EntityType.GROUP);
                validateCustomFieldsForCustomers(request, errors);
                validateFees(request, errors);
            }
        } catch (ApplicationException ae) {
            // Discard other errors (is that right?)
            ae.printStackTrace();
            errors = new ActionErrors();
            errors.add(ae.getKey(), new ActionMessage(ae.getKey(), ae.getValues()));
        }
        if (!errors.isEmpty()) {
            request.setAttribute(GroupConstants.METHODCALLED, method);
        }
        return errors;
    }

    @Override
    protected MeetingBO getCustomerMeeting(HttpServletRequest request) throws ApplicationException {
        if (parentCustomer != null) {
            return parentCustomer.getCustomerMeeting().getMeeting();
        }
        return (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
    }

    public void cleanForm() {
        setDefaultFees(new ArrayList<ApplicableAccountFeeDto>());
        setAdditionalFees(new ArrayList<ApplicableAccountFeeDto>());
        setCustomerPositions(new ArrayList<CustomerPositionDto>());
        setCustomFields(new ArrayList<CustomFieldDto>());
        setAddress(new Address());
        setDisplayName(null);
        setMfiJoiningDate(null);
        setGlobalCustNum(null);
        setCustomerId(null);
        setExternalId(null);
        setLoanOfficerId(null);
        setTrained(null);
        setTrainedDate(null);
        setFormedByPersonnel(null);
        setQuestionGroups(null);
    }

    @Override
    public void setQuestionGroups(List<QuestionGroupDetail> questionGroups) {
        this.questionGroups = questionGroups;
    }

    @Override
    public List<QuestionGroupDetail> getQuestionGroups() {
        return this.questionGroups;
    }
}
