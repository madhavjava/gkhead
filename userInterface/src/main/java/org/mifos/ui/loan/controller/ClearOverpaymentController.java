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

package org.mifos.ui.loan.controller;

import java.math.BigDecimal;
import java.util.Arrays;

import org.mifos.application.servicefacade.LoanAccountServiceFacade;
import org.mifos.dto.domain.OverpaymentDto;
import org.mifos.service.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Controller;

@Controller(value="clearOverpaymentController")
public class ClearOverpaymentController {

	private final LoanAccountServiceFacade loanAccountServiceFacade;

	@Autowired
    public ClearOverpaymentController(LoanAccountServiceFacade loanAccountServiceFacade) {
		this.loanAccountServiceFacade = loanAccountServiceFacade;
    }
	
	// called by spring webflow
	public OverpaymentDto retrieveOverpayment(final String overpaymentId, ClearOverpaymentFormBean clearOverpaymentFormBean) {
		OverpaymentDto result = this.loanAccountServiceFacade.retrieveOverpayment(overpaymentId);

        clearOverpaymentFormBean.setOverpaymentId(result.getPaymentId());
        clearOverpaymentFormBean.setOriginalOverpaymentAmount(result.getActualAmount().doubleValue()); // getActualAmount is called by purpose
        clearOverpaymentFormBean.setActualOverpaymentAmount(result.getActualAmount().doubleValue());

		return result;
	}
	
	// called by spring webflow
	public String applyOverpaymentClear(final String overpaymentId, ClearOverpaymentFormBean clearOverpaymentFormBean, MessageContext messageContext) {
		String returnCode = "";
		try {
            BigDecimal overpaymentAmount = null;
            if (clearOverpaymentFormBean.getActualOverpaymentAmount() != null) {
                overpaymentAmount = BigDecimal.valueOf(clearOverpaymentFormBean.getActualOverpaymentAmount().doubleValue());
            }

			this.loanAccountServiceFacade.applyOverpaymentClear(overpaymentId, overpaymentAmount);
			returnCode = "success";
		} catch (BusinessRuleException e) {
			MessageBuilder builder = new MessageBuilder()
					.error()
					.source("overpaymentAmount")
					.codes(Arrays.asList(e.getMessageKey()).toArray(
							new String[1])).defaultText(e.getMessage())
					.args(e.getMessageValues());

			messageContext.addMessage(builder.build());
			
			returnCode = "error";
		} 
		
		return returnCode;
	}
}