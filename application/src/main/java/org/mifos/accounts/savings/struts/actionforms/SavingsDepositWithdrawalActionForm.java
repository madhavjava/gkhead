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

import static org.mifos.framework.util.helpers.DateUtils.dateFallsBeforeDate;
import static org.mifos.framework.util.helpers.DateUtils.getDateAsSentFromBrowser;

import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.application.admin.servicefacade.InvalidDateException;
import org.mifos.application.util.helpers.Methods;
import org.mifos.framework.struts.actionforms.BaseActionForm;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.DoubleConversionResult;

public class SavingsDepositWithdrawalActionForm extends BaseActionForm {
    String trxnTypeId;

    String paymentTypeId;

    String trxnDate;

    String receiptDate;

    String receiptId;

    String customerId;

    String amount;

    Date lastTrxnDate;

    public SavingsDepositWithdrawalActionForm() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getTrxnDate() {
        return trxnDate;
    }

    public void setTrxnDate(String trxnDate) {
        this.trxnDate = trxnDate;
    }

    public String getTrxnTypeId() {
        return trxnTypeId;
    }

    public void setTrxnTypeId(String trxnTypeId) {
        this.trxnTypeId = trxnTypeId;
    }

    public Date getLastTrxnDate() {
        return lastTrxnDate;
    }

    public void setLastTrxnDate(Date lastTrxnDate) {
        this.lastTrxnDate = lastTrxnDate;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        String method = request.getParameter("method");
        ActionErrors errors = null;

        if (method != null && method.equals(Methods.preview.toString())) {
            errors = new ActionErrors();

            if (StringUtils.isBlank(getTrxnTypeId())) {
                errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                        getLocalizedMessage("Savings.paymentType")));
            }

            if (StringUtils.isBlank(getAmount())) {
                errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                        getLocalizedMessage("Savings.amount")));
            }

            if(StringUtils.isNotBlank(getAmount())) {
                Locale locale = getUserContext(request).getPreferredLocale();
                DoubleConversionResult conversionResult = validateAmount(getAmount(), AccountConstants.ACCOUNT_AMOUNT, errors);
                if (conversionResult.getErrors().size() == 0 && !(conversionResult.getDoubleValue() > 0.0)) {
                    addError(errors, AccountConstants.ACCOUNT_AMOUNT, AccountConstants.ERRORS_MUST_BE_GREATER_THAN_ZERO,
                            getLocalizedMessage(AccountConstants.ACCOUNT_AMOUNT));
                }
            }

            if (StringUtils.isBlank(getPaymentTypeId())) {
                errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                        getLocalizedMessage("Savings.modeOfPayment")));
            }

            ActionErrors dateError = validateDate(this.trxnDate, getLocalizedMessage("Savings.dateOfTrxn"));
            if (dateError != null && !dateError.isEmpty()) {
                errors.add(dateError);
            }

            dateError = validateTrxnDate(this.trxnDate, getLocalizedMessage("Savings.dateOfTrxn"));
            if (dateError != null && !dateError.isEmpty()) {
                errors.add(dateError);
            }

            if (this.getReceiptDate() != null && !this.getReceiptDate().equals("")) {
                dateError = validateDate(getReceiptDate(), getLocalizedMessage("Savings.receiptDate"));
                if (dateError != null && !dateError.isEmpty()) {
                    errors.add(dateError);
                }
            }
            if (StringUtils.isBlank(getCustomerId())) {
                errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                        getLocalizedMessage("Savings.ClientName")));
            }
        }

        if (null != errors && !errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", method);
        }

        return errors;
    }

    private ActionErrors validateDate(String date, String fieldName) {
        ActionErrors errors = null;
        java.sql.Date sqlDate = null;
        if (date != null && !date.equals("")) {
            try {
                sqlDate = DateUtils.getDateAsSentFromBrowser(date);
                if (DateUtils.whichDirection(sqlDate) > 0) {
                    errors = new ActionErrors();
                    errors.add(AccountConstants.ERROR_FUTUREDATE, new ActionMessage(AccountConstants.ERROR_FUTUREDATE,
                            fieldName));
                }
            } catch (InvalidDateException e) {
                errors = new ActionErrors();
                errors.add(AccountConstants.ERROR_INVALIDDATE, new ActionMessage(AccountConstants.ERROR_INVALIDDATE,
                        fieldName));
            }
        } else {
            errors = new ActionErrors();
            errors.add(AccountConstants.ERROR_MANDATORY, new ActionMessage(AccountConstants.ERROR_MANDATORY,
                            fieldName));
        }
        return errors;
    }

    private ActionErrors validateTrxnDate(String date, String fieldName) {
        ActionErrors errors = new ActionErrors();
        try {
            if (lastTrxnDate != null && dateFallsBeforeDate(getDateAsSentFromBrowser(date), lastTrxnDate)) {
                errors = new ActionErrors();
                errors.add(AccountConstants.ERROR_PAYMENT_DATE_BEFORE_LAST_PAYMENT,
                        new ActionMessage(AccountConstants.ERROR_PAYMENT_DATE_BEFORE_LAST_PAYMENT,
                                fieldName));
            }
        } catch (InvalidDateException e) {
            //date format was already validated
        }
        return errors;
    }
}
