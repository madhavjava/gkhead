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

package org.mifos.ui.core.controller;

import org.apache.commons.lang.StringUtils;
import org.mifos.accounts.fund.servicefacade.FundDto;
import org.mifos.accounts.fund.servicefacade.FundServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("fundPreview")
@SessionAttributes("formBean")
public class ViewFundsPreviewController {

    private static final String REDIRECT_TO_ADMIN_SCREEN = "redirect:/AdminAction.do?method=load";
    private static final String REDIRECT_TO_VIEW_FUNDS = "redirect:/viewFunds.ftl";
    private static final String CANCEL_PARAM = "CANCEL";
    private static final String EDIT_PARAM = "EDIT";

    @Autowired
    private FundServiceFacade fundServiceFacade;

    protected ViewFundsPreviewController() {
        // spring auto wiring
    }

    public ViewFundsPreviewController(final FundServiceFacade fundServiceFacade) {
        this.fundServiceFacade = fundServiceFacade;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView processFormSubmit(@RequestParam(value = EDIT_PARAM, required = false) String edit,
            @RequestParam(value = CANCEL_PARAM, required = false) String cancel, FundFormBean formBean,
            BindingResult result, SessionStatus status) {
        String viewName = REDIRECT_TO_ADMIN_SCREEN;
        ModelAndView modelAndView = new ModelAndView();
        if (StringUtils.isNotBlank(edit)) {
            viewName = "editFunds";
            modelAndView.setViewName(viewName);
            modelAndView.addObject("formBean", formBean);
            modelAndView.addObject("previewView", "fundPreview");
        } else if (StringUtils.isNotBlank(cancel)) {
            viewName = REDIRECT_TO_VIEW_FUNDS;
            modelAndView.setViewName(viewName);
            status.setComplete();
        } else if (result.hasErrors()) {
            viewName = "fundPreview";
            modelAndView.setViewName(viewName);
            modelAndView.addObject("formBean", formBean);
        } else {
            Integer fundCode = Integer.parseInt(formBean.getId());
            FundDto fundUpdate = fundServiceFacade.getFund(fundCode.shortValue());
            fundUpdate.setName(formBean.getName());
            this.fundServiceFacade.updateFund(fundUpdate);
            viewName = REDIRECT_TO_ADMIN_SCREEN;
            modelAndView.setViewName(viewName);
            status.setComplete();
        }
        return modelAndView;
    }
}