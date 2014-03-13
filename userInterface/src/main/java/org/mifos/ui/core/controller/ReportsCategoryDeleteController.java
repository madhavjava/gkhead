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

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.mifos.application.admin.servicefacade.AdminServiceFacade;
import org.mifos.dto.domain.ReportCategoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/deleteReportCategory")
@SessionAttributes("reportCategory")
public class ReportsCategoryDeleteController {

    private static final String REDIRECT_TO_ADMIN_SCREEN = "redirect:/AdminAction.do?method=load";
    private static final String CANCEL_PARAM = "CANCEL";

    @Autowired
    private AdminServiceFacade adminServiceFacade;

    protected ReportsCategoryDeleteController(){
        //for spring autowiring
    }

    public ReportsCategoryDeleteController(final AdminServiceFacade adminServicefacade){
        this.adminServiceFacade = adminServicefacade;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView processFormSubmit(@RequestParam(value = CANCEL_PARAM, required = false) String cancel,
            @ModelAttribute("reportCategory") @Valid ReportCategoryFormBean reportCategory,
            SessionStatus status) {

        ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_ADMIN_SCREEN);
        if (StringUtils.isNotBlank(cancel)) {
            modelAndView.setViewName(REDIRECT_TO_ADMIN_SCREEN);
        }
        status.setComplete();

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ModelAttribute("reportCategory")
    public ReportCategoryFormBean showEmptyForm(@RequestParam(value = "categoryId", required = true) Integer reportCategoryId) {

        ReportCategoryDto reportCategoryDetails = adminServiceFacade.retrieveReportCategory(reportCategoryId);

        ReportCategoryFormBean bean = new ReportCategoryFormBean();
        bean.setName(reportCategoryDetails.getName());
        return bean;
    }
}