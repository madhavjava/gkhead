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

import org.mifos.application.admin.servicefacade.ViewOrganizationSettingsServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

@Controller
@RequestMapping("/viewOrganizationSettings")
public class ViewOrganizationSettingsController {

    @Autowired
    private ViewOrganizationSettingsServiceFacade viewOrganizationSettingsServiceFacade;

    protected ViewOrganizationSettingsController() {
        // empty constructor for spring wiring
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_UNWRITTEN_FIELD", justification="request is not null")
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("viewOrganizationSettings");
        Properties p = viewOrganizationSettingsServiceFacade.getOrganizationSettings(request.getSession());
        modelAndView.addObject("properties", p);
        modelAndView.addObject("pluginsPropsMap", viewOrganizationSettingsServiceFacade.getDisplayablePluginsProperties());
        modelAndView.addObject("breadcrumbs", new AdminBreadcrumbBuilder().withLink("admin.viewOrganizationSettings", "viewOrganizationSettings.ftl").build());

        return modelAndView;
    }
}