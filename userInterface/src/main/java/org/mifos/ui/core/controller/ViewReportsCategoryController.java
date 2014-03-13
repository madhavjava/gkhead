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

import java.util.List;

import org.mifos.application.admin.servicefacade.AdminServiceFacade;
import org.mifos.dto.domain.ReportCategoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/viewReportsCategory")
public class ViewReportsCategoryController {

    @Autowired
    private AdminServiceFacade adminServiceFacade;

    protected ViewReportsCategoryController(){
        //for spring autowiring
    }
    public ViewReportsCategoryController(final AdminServiceFacade adminServicefacade){
        this.adminServiceFacade = adminServicefacade;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ModelAttribute("reportsCategoryList")
    public List<ReportCategoryDto> viewLoanProductsList() {
        return this.adminServiceFacade.retrieveReportCategories();
    }
}