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

package org.mifos.dto.screen;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mifos.dto.domain.CustomerDetailDto;
import org.mifos.dto.domain.LoanAccountDetailsDto;
import org.mifos.dto.domain.PrdOfferingDto;
import org.mifos.platform.validations.Errors;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification="")
public class LoanCreationProductDetailsDto implements Serializable {

    private final List<PrdOfferingDto> loanProductDtos;
    private Map<String, String> productOptions = new LinkedHashMap<String, String>();
	private final CustomerDetailDto customerDetailDto;
    private final Date nextMeetingDate;
    private final boolean isGroup;
    private final boolean isGlimEnabled;
    private final LoanCreationGlimDto loanCreationGlimDto;
    private final List<LoanAccountDetailsDto> clientDetails;
    private final String recurMonth;
    private final Errors errors;

    public LoanCreationProductDetailsDto(List<PrdOfferingDto> loanProductDtos, CustomerDetailDto customerDetailDto,
            Date nextMeetingDate, String recurMonth, boolean isGroup, boolean isGlimEnabled, LoanCreationGlimDto loanCreationGlimDto, List<LoanAccountDetailsDto> clientDetails, Errors errors) {
        this.loanProductDtos = loanProductDtos;
        this.customerDetailDto = customerDetailDto;
        this.nextMeetingDate = nextMeetingDate;
        this.recurMonth = recurMonth;
        this.isGroup = isGroup;
        this.isGlimEnabled = isGlimEnabled;
        this.loanCreationGlimDto = loanCreationGlimDto;
        this.clientDetails = clientDetails;
        this.errors = errors;
        populateProductOptions(loanProductDtos);
    }

    private void populateProductOptions(List<PrdOfferingDto> loanProducts) {
    	for (PrdOfferingDto product : loanProducts) {
    		this.productOptions.put(product.getPrdOfferingId().toString(), product.getPrdOfferingName());			
		}
	}
    
	public List<PrdOfferingDto> getLoanProductDtos() {
        return this.loanProductDtos;
    }

    public CustomerDetailDto getCustomerDetailDto() {
        return this.customerDetailDto;
    }

    public Date getNextMeetingDate() {
        return this.nextMeetingDate;
    }

    public boolean isGroup() {
        return this.isGroup;
    }

    public boolean isGlimEnabled() {
        return this.isGlimEnabled;
    }

    public LoanCreationGlimDto getLoanCreationGlimDto() {
        return this.loanCreationGlimDto;
    }

    public List<LoanAccountDetailsDto> getClientDetails() {
        return this.clientDetails;
    }

    public String getRecurMonth() {
        return this.recurMonth;
    }
    
    public Map<String, String> getProductOptions() {
		return productOptions;
	}
    
    public Errors getErrors() {
        return errors;
    }
}