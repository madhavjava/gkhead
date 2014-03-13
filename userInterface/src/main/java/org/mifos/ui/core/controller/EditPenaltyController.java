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

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.mifos.accounts.penalty.servicefacade.PenaltyServiceFacade;
import org.mifos.application.admin.servicefacade.ViewOrganizationSettingsServiceFacade;
import org.mifos.config.servicefacade.ConfigurationServiceFacade;
import org.mifos.config.servicefacade.dto.AccountingConfigurationDto;
import org.mifos.dto.domain.PenaltyDto;
import org.mifos.dto.screen.PenaltyParametersDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/editPenalty")
@SessionAttributes("formBean")
public class EditPenaltyController {
    private static final String REDIRECT_TO_VIEW_PENALTY = "redirect:/viewPenalty.ftl?penaltyId=";
    private static final String CANCEL_PARAM = "CANCEL";
    @Autowired
    private PenaltyServiceFacade penaltyServiceFacade;
    
    @Autowired
    private ViewOrganizationSettingsServiceFacade viewOrganizationSettingsServiceFacade;
    
    @Autowired
    private ConfigurationServiceFacade configurationServiceFacade;
    
    private PenaltyParametersDto parametersDto;
    private AccountingConfigurationDto configurationDto;
    
    protected EditPenaltyController() {
        //spring autowiring
    }
    
    public EditPenaltyController(final PenaltyServiceFacade penaltyServiceFacade,
            final ViewOrganizationSettingsServiceFacade viewOrganizationSettingsServiceFacade, final ConfigurationServiceFacade configurationServiceFacade) {
        this.penaltyServiceFacade = penaltyServiceFacade;
        this.viewOrganizationSettingsServiceFacade = viewOrganizationSettingsServiceFacade;
        this.configurationServiceFacade = configurationServiceFacade;
    }
    
    @InitBinder
    protected void initBinder(WebDataBinder binder, HttpSession session) {
        binder.setValidator(new PenaltyFormValidator(viewOrganizationSettingsServiceFacade.getOrganizationSettings(session)));
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm(@RequestParam Integer penaltyId) {
        ModelAndView modelAndView = new ModelAndView("editPenalty");
        
        PenaltyDto dto = this.penaltyServiceFacade.getPenalty(penaltyId);
        parametersDto = this.penaltyServiceFacade.getPenaltyParameters();
        configurationDto = this.configurationServiceFacade.getAccountingConfiguration();
        PenaltyFormBean bean = new PenaltyFormBean(dto);
        
        modelAndView.addObject("formBean", bean);
        modelAndView.addObject("param", parametersDto);
        modelAndView.addObject("GLCodeMode",configurationDto.getGlCodeMode());
        
        return modelAndView;
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView showPreview(@RequestParam(value = CANCEL_PARAM, required = false) String cancel,
            @ModelAttribute("formBean") @Valid PenaltyFormBean formBean, BindingResult result, SessionStatus status) {
        ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_VIEW_PENALTY + formBean.getId());
        configurationDto = this.configurationServiceFacade.getAccountingConfiguration();
        modelAndView.addObject("GLCodeMode", configurationDto.getGlCodeMode());
        
        if (StringUtils.isNotBlank(cancel)) {
            modelAndView.setViewName(REDIRECT_TO_VIEW_PENALTY + formBean.getId());
            status.setComplete();
        } else {
            if (result.hasErrors()) {
                parametersDto = this.penaltyServiceFacade.getPenaltyParameters();
                
                modelAndView.setViewName("editPenalty");
                modelAndView.addObject("param", parametersDto);
            } else {
                modelAndView.setViewName("penaltyPreview");
                
                modelAndView.addObject("period", this.parametersDto.getPeriodType().get(formBean.getPeriodTypeId()));
                modelAndView.addObject("formula", this.parametersDto.getFormulaType().get(formBean.getFormulaId()));
                modelAndView.addObject("frequency", this.parametersDto.getFrequencyType().get(formBean.getFrequencyId()));
                modelAndView.addObject("glCode", this.parametersDto.getGlCodes().get(formBean.getGlCodeId()));
                modelAndView.addObject("status", this.parametersDto.getStatusType().get(formBean.getStatusId()));
            }
            
            modelAndView.addObject("formBean", formBean);
        }
        
        return modelAndView;
    }
}
