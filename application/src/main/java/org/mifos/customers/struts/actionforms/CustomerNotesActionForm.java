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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.application.util.helpers.Methods;
import org.mifos.customers.center.util.helpers.ValidateMethods;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.framework.struts.actionforms.BaseActionForm;

public class CustomerNotesActionForm extends BaseActionForm {

    private String customerId;
    private String levelId;
    private String customerName;
    private String comment;
    private String commentDate;
    private String globalCustNum;
    private String input;

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getGlobalCustNum() {
        return globalCustNum;
    }

    public void setGlobalCustNum(String globalCustNum) {
        this.globalCustNum = globalCustNum;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        String methodCalled = request.getParameter(Methods.method.toString());
        ActionErrors errors = null;
        if (null != methodCalled) {
            if (Methods.preview.toString().equals(methodCalled)) {
                errors = handlePreviewValidations(request, errors);
            }
        }
        if (null != errors && !errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", methodCalled);
        }
        return errors;
    }

    private ActionErrors handlePreviewValidations(HttpServletRequest request, ActionErrors errors) {
        if (ValidateMethods.isNullOrBlank(getComment())) {
            if (null == errors) {
                errors = new ActionErrors();
            }
            errors.add(CustomerConstants.ERROR_MANDATORY_TEXT_AREA, new ActionMessage(
                    CustomerConstants.ERROR_MANDATORY_TEXT_AREA, this.getLocalizedMessage("Customer.notes")));
        } else if (getComment().length() > CustomerConstants.COMMENT_LENGTH) {
            if (null == errors) {
                errors = new ActionErrors();
            }
            errors.add(CustomerConstants.MAXIMUM_LENGTH, new ActionMessage(CustomerConstants.MAXIMUM_LENGTH,
                    this.getLocalizedMessage("Customer.notes"), CustomerConstants.COMMENT_LENGTH));
        }
        return errors;
    }

}
