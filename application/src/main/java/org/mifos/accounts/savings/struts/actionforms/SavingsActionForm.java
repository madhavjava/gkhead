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

package org.mifos.accounts.savings.struts.actionforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.SavingsType;
import org.mifos.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.accounts.struts.actionforms.AccountAppActionForm;
import org.mifos.application.master.business.CustomFieldDefinitionEntity;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.questionnaire.struts.QuestionResponseCapturer;
import org.mifos.config.AccountingRules;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.util.LocalizationConverter;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.ConversionError;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.DoubleConversionResult;
import org.mifos.framework.util.helpers.ExceptionConstants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;

public class SavingsActionForm extends AccountAppActionForm implements QuestionResponseCapturer{
    private String recommendedAmount;
    private List<QuestionGroupDetail> questionGroups;

    public SavingsActionForm() {
        super();
    }

    public String getRecommendedAmount() {
        return recommendedAmount;
    }

    public void setRecommendedAmount(String recommendedAmount) {
        this.recommendedAmount = recommendedAmount;
    }

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
        String method = request.getParameter("method");
        ActionErrors errors = new ActionErrors();
        String mandatoryAmount = getLocalizedMessage(SavingsConstants.MANDATORY_AMOUNT_FOR_DEPOSIT_KEY);
        request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));

        if (method.equals("getPrdOfferings") || method.equals("create") || method.equals("edit")
                || method.equals("update") || method.equals("get") || method.equals("validate")) {
        } else {
            errors.add(super.validate(mapping, request));
            if (method.equals("preview") || method.equals("editPreview")) {
                try {
                    SavingsOfferingBO savingsOffering = (SavingsOfferingBO) SessionUtils.getAttribute(
                            SavingsConstants.PRDOFFERING, request);
                    if (savingsOffering.getSavingsType().getId().equals(SavingsType.MANDATORY.getValue())
                            && StringUtils.isBlank(getRecommendedAmount())) {
                        // check for mandatory amount
                        errors.add(SavingsConstants.MANDATORY, new ActionMessage(SavingsConstants.MANDATORY,
                                mandatoryAmount));
                    }
                    if (StringUtils.isNotBlank(getRecommendedAmount())) {
                        if (savingsOffering.getSavingsType().equals(SavingsType.MANDATORY)) {
                            validateAmount(getRecommendedAmount(),
                                    SavingsConstants.MANDATORY_AMOUNT_FOR_DEPOSIT_KEY, errors);
                        } else {
                            validateAmount(getRecommendedAmount(),
                                    SavingsConstants.RECOMMENDED_AMOUNT_FOR_DEPOSIT_KEY, errors);
                        }
                    }
                    validateCustomFields(request, errors);
                } catch (PageExpiredException e) {
                    errors.add(SavingsConstants.MANDATORY, new ActionMessage(SavingsConstants.MANDATORY,
                            mandatoryAmount));
                }
            }
        }

        if (!errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", method);
        }
        return errors;
    }

    protected DoubleConversionResult validateAmount(String amountString, String fieldPropertyKey, ActionErrors errors,
            Locale locale, String propertyfileName) {
        DoubleConversionResult conversionResult = parseDoubleForMoney(amountString);
        String arg = getLocalizedMessage(fieldPropertyKey);
        addConversionResultErrors(fieldPropertyKey, arg, errors, locale, conversionResult);
        return conversionResult;
    }



    protected DoubleConversionResult parseDoubleForMoney(String doubleString) {
        return new LocalizationConverter().parseDoubleForMoney(doubleString);

    }

    private void addConversionResultErrors(String fieldPropertyKey, String propertyName, ActionErrors errors,
            Locale locale, DoubleConversionResult conversionResult) {
        List<ConversionError> errorList = conversionResult.getErrors();
        if (errorList.size() > 0) {
            for (int i = 0; i < errorList.size(); i++) {
                addError(errors, fieldPropertyKey, "errors.generic", propertyName, getConversionErrorText(errorList
                        .get(i), locale));
            }
        }
    }

    protected void addError(ActionErrors errors, String property, String key, String... arg) {
        errors.add(property, new ActionMessage(key, arg));
    }

    protected String getConversionErrorText(ConversionError error, Locale locale) {
        String errorText = this.getLocalizedMessage(error.toString());
        if (error.equals(ConversionError.EXCEEDING_NUMBER_OF_DIGITS_BEFORE_DECIMAL_SEPARATOR_FOR_MONEY)) {
            errorText = errorText.replaceFirst("%s", AccountingRules.getDigitsBeforeDecimal().toString());
        } else if (error.equals(ConversionError.EXCEEDING_NUMBER_OF_DIGITS_AFTER_DECIMAL_SEPARATOR_FOR_MONEY)) {
            errorText = errorText.replaceFirst("%s", AccountingRules.getDigitsAfterDecimal().toString());
        }
        return errorText;
    }

    public void clear() {
        this.setAccountId(null);
        this.setSelectedPrdOfferingId(null);
        this.setAccountCustomFieldSet(new ArrayList<CustomFieldDto>());
        this.setQuestionGroups(null);
    }

    private void validateCustomFields(HttpServletRequest request, ActionErrors errors) {
        try {
            List<CustomFieldDefinitionEntity> customFieldDefs = (List<CustomFieldDefinitionEntity>) SessionUtils
                    .getAttribute(CustomerConstants.CUSTOM_FIELDS_LIST, request);
            for (CustomFieldDto customField : getAccountCustomFieldSet()) {
                for (CustomFieldDefinitionEntity customFieldDef : customFieldDefs) {
                    if (customField.getFieldId().equals(customFieldDef.getFieldId())) {
                        if (customFieldDef.isMandatory() && StringUtils.isBlank(customField.getFieldValue())) {
                            errors.add(LoanConstants.CUSTOM_FIELDS, new ActionMessage(
                                    LoanConstants.ERRORS_SPECIFY_CUSTOM_FIELD_VALUE, customFieldDef.getLabel()));
                        }
                        if (CustomFieldType.fromInt(customField.getFieldId()).equals(CustomFieldType.DATE) &&
                                (StringUtils.isNotBlank(customField.getFieldValue()))) {
                            try {
                                DateUtils.getDate(customField.getFieldValue());
                            } catch (Exception e) {
                                errors.add(LoanConstants.CUSTOM_FIELDS, new ActionMessage(
                                        LoanConstants.ERRORS_CUSTOM_DATE_FIELD, customFieldDef.getLabel()));
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
}
