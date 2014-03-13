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

package org.mifos.accounts.loan.struts.actionforms;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.accounts.struts.actionforms.AccountApplyPaymentActionForm;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.application.admin.servicefacade.InvalidDateException;
import org.mifos.application.questionnaire.struts.QuestionResponseCapturer;
import org.mifos.framework.business.util.helpers.MethodNameConstants;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;

public class LoanDisbursementActionForm extends AccountApplyPaymentActionForm implements QuestionResponseCapturer{

    private String loanAmount;

    private String paymentModeOfPayment;

    private List<QuestionGroupDetail> questionGroups;

    @Override
    public void setQuestionGroups(List<QuestionGroupDetail> questionGroups) {
        this.questionGroups = questionGroups;
    }

    @Override
    public List<QuestionGroupDetail> getQuestionGroups() {
        return this.questionGroups;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        ActionErrors errors1 = super.validate(mapping, request);
        if (errors1 != null) {
            errors.add(errors1);
        }

        String method = request.getParameter(MethodNameConstants.METHOD);
        if (isPreviewMethod(method) && getPaymentAmountGreaterThanZero()
                && StringUtils.isBlank(paymentModeOfPayment)) {
            String errorMessage = this.getLocalizedMessage("loan.paymentid");
            errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                    errorMessage));
        }

        if (!errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", method);
        }
        return errors;
    }

    private boolean isPreviewMethod(String methodName) {
        return methodName != null && methodName.equals(MethodNameConstants.PREVIEW);
    }

    private boolean isValueGreaterThanZero(String value) {
        double doubleValue = (StringUtils.isNotBlank(value) && !value.trim().equals(".")) ? Double.parseDouble(value) : 0.0;
        return doubleValue > 0.0;
    }

    public String getPaymentModeOfPayment() {
        return paymentModeOfPayment;
    }

    public void setPaymentModeOfPayment(String paymentModeOfPayment) {
        this.paymentModeOfPayment = paymentModeOfPayment;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public boolean getLoanAmountGreaterThanZero() {
        return isValueGreaterThanZero(getLoanAmount());
    }

    public boolean getPaymentAmountGreaterThanZero() {
        return isValueGreaterThanZero(getAmount());
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    @Override
    public void clear() throws InvalidDateException {
        super.clear();
        this.loanAmount = null;
        this.paymentModeOfPayment = null;
        this.questionGroups = null;
    }

}
