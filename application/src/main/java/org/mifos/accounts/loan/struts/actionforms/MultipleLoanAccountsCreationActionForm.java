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

import static org.mifos.framework.util.CollectionUtils.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.accounts.loan.util.helpers.LoanExceptionConstants;
import org.mifos.accounts.loan.util.helpers.MultipleLoanCreationDto;
import org.mifos.application.collectionsheet.util.helpers.CollectionSheetEntryConstants;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.Methods;
import org.mifos.config.util.helpers.ConfigurationConstants;
import org.mifos.framework.components.fieldConfiguration.business.FieldConfigurationEntity;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.struts.actionforms.BaseActionForm;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DoubleConversionResult;
import org.mifos.framework.util.helpers.ExceptionConstants;
import org.mifos.framework.util.helpers.Predicate;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.security.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleLoanAccountsCreationActionForm extends BaseActionForm {
    private static final Logger logger = LoggerFactory.getLogger(MultipleLoanAccountsCreationActionForm.class);

    private String branchOfficeId;

    private String loanOfficerId;

    private String centerId;

    private String centerSearchId;

    private String prdOfferingId;

    private List<MultipleLoanCreationDto> clientDetails;

    private String stateSelected;

    public MultipleLoanAccountsCreationActionForm() {
        clientDetails = new ArrayList<MultipleLoanCreationDto>();
    }

    public List<MultipleLoanCreationDto> getClientDetails() {
        return clientDetails;
    }

    public void setClientDetails(List<MultipleLoanCreationDto> clientDetails) {
        this.clientDetails = clientDetails;
    }

    public List<MultipleLoanCreationDto> getApplicableClientDetails() {
        try {
            return select(clientDetails,
                    new Predicate<MultipleLoanCreationDto>() {
                        @Override
						public boolean evaluate(MultipleLoanCreationDto clientDetail) {
                            return clientDetail.isApplicable();
                        }
                    });
        } catch (Exception e) {
            return new ArrayList<MultipleLoanCreationDto>();
        }
    }

    public String getBranchOfficeId() {
        return branchOfficeId;
    }

    public void setBranchOfficeId(String branchOfficeId) {
        this.branchOfficeId = branchOfficeId;
    }

    public String getLoanOfficerId() {
        return loanOfficerId;
    }

    public void setLoanOfficerId(String loanOfficerId) {
        this.loanOfficerId = loanOfficerId;
    }

    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
    }

    public String getPrdOfferingId() {
        return prdOfferingId;
    }

    public void setPrdOfferingId(String prdOfferingId) {
        this.prdOfferingId = prdOfferingId;
    }

    public String getCenterSearchId() {
        return centerSearchId;
    }

    public void setCenterSearchId(String centerSearchId) {
        this.centerSearchId = centerSearchId;
    }

    public String getStateSelected() {
        return stateSelected;
    }

    public void setStateSelected(String stateSelected) {
        this.stateSelected = stateSelected;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        logger.debug("Inside validate method");
        String method = request.getParameter(Methods.method.toString());
        ActionErrors errors = new ActionErrors();
        try {
            if (method.equals(Methods.get.toString())) {
                request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));
                checkValidationForLoad(errors, getUserContext(request), (Short) SessionUtils.getAttribute(
                        CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, request));
            } else if (method.equals(Methods.create.toString())) {
                request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));
                validateLoanAmounts(errors, this.getUserContext(request).getPreferredLocale(), clientDetails);
                checkValidationForCreate(errors, request);
            } else if (method.equals(Methods.getLoanOfficers.toString())) {
                checkValidationForBranchOffice(errors, getUserContext(request));
            } else if (method.equals(Methods.getCenters.toString())) {
                checkValidationForBranchOffice(errors, getUserContext(request));
                checkValidationForLoanOfficer(errors);
            } else if (method.equals(Methods.getPrdOfferings.toString())) {
                request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));
                checkValidationForBranchOffice(errors, getUserContext(request));
                checkValidationForLoanOfficer(errors);
                checkValidationForCenter(errors, getUserContext(request), (Short) SessionUtils.getAttribute(
                        CollectionSheetEntryConstants.ISCENTERHIERARCHYEXISTS, request));
            }
        } catch (PageExpiredException e) {
            errors.add(ExceptionConstants.PAGEEXPIREDEXCEPTION, new ActionMessage(
                    ExceptionConstants.PAGEEXPIREDEXCEPTION));
        } catch (ServiceException e) {
            errors.add(ExceptionConstants.SERVICEEXCEPTION, new ActionMessage(ExceptionConstants.SERVICEEXCEPTION));
        }
        if (!errors.isEmpty()) {
            request.setAttribute("methodCalled", method);
        }
        logger.debug("outside validate method");
        return errors;
    }

    protected void validateLoanAmounts(ActionErrors errors, Locale locale, List<MultipleLoanCreationDto> clientDetails) {
        for (MultipleLoanCreationDto clientDetail : clientDetails) {
            DoubleConversionResult conversionResult = validateAmount(clientDetail.getLoanAmount(),
                    LoanConstants.LOAN_AMOUNT_KEY, errors);
            if (conversionResult.getErrors().size() == 0 && !(conversionResult.getDoubleValue() > 0.0)) {
                addError(errors, LoanConstants.LOAN_AMOUNT_KEY, LoanConstants.ERRORS_MUST_BE_GREATER_THAN_ZERO,
                        getLocalizedMessage(LoanConstants.LOAN_AMOUNT_KEY));
            }
        }
    }

    private void checkValidationForCreate(ActionErrors errors, HttpServletRequest request) throws PageExpiredException,
            ServiceException {
        logger.debug("inside checkValidationForCreate method");
        List<MultipleLoanCreationDto> applicableClientDetails = getApplicableClientDetails();
        if (CollectionUtils.isEmpty(applicableClientDetails)) {
            addError(errors, LoanConstants.APPL_RECORDS, LoanExceptionConstants.SELECT_ATLEAST_ONE_RECORD, getLabel(
                    ConfigurationConstants.CLIENT));
            return;
        }
        for (MultipleLoanCreationDto clientDetail : applicableClientDetails) {
            try {
                if (!clientDetail.isLoanAmountInRange()) {
                    addError(errors, LoanConstants.LOANAMOUNT, LoanExceptionConstants.INVALIDMINMAX,
                            this.getLocalizedMessage("loan.loanAmountFor")
                            + clientDetail.getClientName(), clientDetail.getMinLoanAmount().toString(), clientDetail
                            .getMaxLoanAmount().toString());
                }
            } catch (NumberFormatException nfe) {
                addError(errors, LoanConstants.LOANAMOUNT, LoanExceptionConstants.INVALIDMINMAX,
                        this.getLocalizedMessage("loan.loanAmountFor")
                        + clientDetail.getClientName(), clientDetail.getMinLoanAmount().toString(), clientDetail
                        .getMaxLoanAmount().toString());
            }
            if (StringUtils.isEmpty(clientDetail.getBusinessActivity()) && isPurposeOfLoanMandatory(request)) {
                addError(errors, LoanConstants.PURPOSE_OF_LOAN, LoanExceptionConstants.CUSTOMER_PURPOSE_OF_LOAN_FIELD);
            }
        }
        logger.debug("outside checkValidationForCreate method");
    }

    private void checkValidationForLoad(ActionErrors errors, UserContext userContext, short isCenterHierarchyExists) {
        logger.debug("Inside checkValidationForLoad method");
        checkValidationForBranchOffice(errors, userContext);
        checkValidationForLoanOfficer(errors);
        checkValidationForCenter(errors, userContext, isCenterHierarchyExists);
        checkValidationForPrdOfferingId(errors, userContext);
        logger.debug("outside checkValidationForLoad method");
    }

    private void checkValidationForBranchOffice(ActionErrors errors, UserContext userContext) {
        if (StringUtils.isBlank(branchOfficeId)) {
            addError(errors, ConfigurationConstants.BRANCHOFFICE, LoanConstants.MANDATORY_SELECT, getMessageText(
                    ConfigurationConstants.BRANCHOFFICE));
        }
    }

    private void checkValidationForLoanOfficer(ActionErrors errors) {
        if (StringUtils.isBlank(loanOfficerId)) {
            addError(errors, LoanConstants.LOANOFFICERS, LoanConstants.MANDATORY_SELECT, LoanConstants.LOANOFFICERS);
        }
    }

    private void checkValidationForCenter(ActionErrors errors, UserContext userContext, short isCenterHierarchyExists) {
        String customerLabel = isCenterHierarchyExists == Constants.YES ? ConfigurationConstants.CENTER
                : ConfigurationConstants.GROUP;
        if (StringUtils.isBlank(centerId)) {
            addError(errors, ConfigurationConstants.CENTER, LoanConstants.MANDATORY_SELECT, getLabel(customerLabel));
        }
    }

    private void checkValidationForPrdOfferingId(ActionErrors errors, UserContext userContext) {
        if (StringUtils.isBlank(prdOfferingId)) {
            addError(errors, LoanConstants.PRDOFFERINGID, LoanConstants.LOANOFFERINGNOTSELECTEDERROR, getLabel(
                    ConfigurationConstants.LOAN), LoanConstants.INSTANCENAME);
        }
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        CollectionUtils.forAllDo(clientDetails, new Closure() {
            @Override
			public void execute(Object arg0) {
                ((MultipleLoanCreationDto) arg0).resetSelected();
            }
        });
    }

    /**
     * Returns true or false depending on whether the purpose of the loan is
     * mandatory or not
     */
    private boolean isPurposeOfLoanMandatory(HttpServletRequest request) {
        logger.debug("Checking if purpose of loan is Mandatory");

        Map<Short, List<FieldConfigurationEntity>> entityMandatoryFieldMap = (Map<Short, List<FieldConfigurationEntity>>) request
                .getSession().getServletContext().getAttribute(Constants.FIELD_CONFIGURATION);
        List<FieldConfigurationEntity> mandatoryfieldList = entityMandatoryFieldMap.get(EntityType.LOAN.getValue());

        boolean isMandatory = false;

        for (FieldConfigurationEntity entity : mandatoryfieldList) {
            if (entity.getFieldName().equalsIgnoreCase(LoanConstants.PURPOSE_OF_LOAN)) {
                isMandatory = true;
                logger.debug("Returning true");
                break;
            }
        }

        logger.debug("Returning " + isMandatory);
        return isMandatory;
    }
}
