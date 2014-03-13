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

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.mifos.accounts.loan.util.helpers.LoanConstants;
import org.mifos.application.util.helpers.Methods;
import org.mifos.config.util.helpers.ConfigurationConstants;
import org.mifos.framework.struts.actionforms.BaseActionForm;
import org.mifos.security.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReverseLoanDisbursalActionForm extends BaseActionForm {
    private static final Logger logger = LoggerFactory.getLogger(ReverseLoanDisbursalActionForm.class);

    private String searchString;

    private String note;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        logger.debug("reset method called");
        super.reset(mapping, request);
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        logger.debug("Inside validate method");
        String method = request.getParameter(Methods.method.toString());
        ActionErrors errors = new ActionErrors();
        if (method.equals(Methods.load.toString())) {
            checkValidationForLoad(errors, getUserContext(request));
        } else if (method.equals(Methods.preview.toString())) {
            checkValidationForPreview(errors, getUserContext(request).getPreferredLocale());
        }
        if (!errors.isEmpty()) {
            request.setAttribute("methodCalled", method);
        }
        logger.debug("outside validate method");
        return errors;
    }

    private void checkValidationForLoad(ActionErrors errors, UserContext userContext) {
        if (StringUtils.isBlank(getSearchString())) {
            addError(errors, "SearchString", LoanConstants.ERROR_LOAN_ACCOUNT_ID, getLabel(ConfigurationConstants.LOAN));
        }
    }

    private void checkValidationForPreview(ActionErrors errors, Locale userLocale) {
        String note = this.getLocalizedMessage("loan.note");
        if (StringUtils.isBlank(getNote())) {
            addError(errors, LoanConstants.NOTE, LoanConstants.MANDATORY, note);
        } else if (getNote().length() > 500) {
            addError(errors, LoanConstants.NOTE, LoanConstants.MAX_LENGTH, note, String
                    .valueOf(LoanConstants.COMMENT_LENGTH));
        }
    }

}
