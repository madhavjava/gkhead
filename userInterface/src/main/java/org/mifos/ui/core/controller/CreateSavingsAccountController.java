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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifos.application.servicefacade.SavingsServiceFacade;
import org.mifos.dto.domain.CustomerDto;
import org.mifos.dto.domain.CustomerSearchDto;
import org.mifos.dto.domain.CustomerSearchResultDto;
import org.mifos.dto.domain.PrdOfferingDto;
import org.mifos.dto.domain.SavingsAccountCreationDto;
import org.mifos.dto.domain.SavingsAccountDetailDto;
import org.mifos.dto.domain.SavingsProductDto;
import org.mifos.dto.screen.CustomerSearchResultsDto;
import org.mifos.dto.screen.SavingsProductReferenceDto;
import org.mifos.dto.screen.SearchDetailsDto;
import org.mifos.platform.questionnaire.service.QuestionGroupDetail;
import org.mifos.platform.questionnaire.service.QuestionnaireServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class CreateSavingsAccountController {

    // FIXME: grab from org.mifos.accounts.util.helpers.AccountState enum
    private static final int ACCOUNT_STATE_PARTIAL_APPLICAITON = 13;
    private static final int ACCOUNT_STATE_PENDNG_APPROVAL = 14;
    private static final int ACCOUNT_STATE_ACTIVE = 16;

    // FIXME same problem as above
    private static final int RECURRENCE_WEEKLY = 1;
    private static final int RECURRENCE_MONTHLY = 2;
    private static final int RECURRENCE_DAILY = 3;

    @Autowired
    private SavingsServiceFacade savingsServiceFacade;

    @Autowired
    private QuestionnaireServiceFacade questionnaireServiceFacade;

    protected CreateSavingsAccountController() {
        // needed by spring @Autowired
    }

    /**
     * For unit testing.
     */
    protected void setQuestionnaireServiceFacade(QuestionnaireServiceFacade questionnaireServiceFacade) {
        this.questionnaireServiceFacade = questionnaireServiceFacade;
    }

    /**
     * For unit testing.
     */
    protected void setSavingsServiceFacade(SavingsServiceFacade savingsServiceFacade) {
       this.savingsServiceFacade = savingsServiceFacade;
    }
    
    public SavingsAccountDetailDto createAccountInActiveState(
            CreateSavingsAccountFormBean formBean) {
        Short accountState = ACCOUNT_STATE_ACTIVE;
        return createAccount(formBean, accountState);
    }

    public SavingsAccountDetailDto createAccountInPartialApplicationState(
            CreateSavingsAccountFormBean formBean) {
        Short accountState = ACCOUNT_STATE_PARTIAL_APPLICAITON;
        return createAccount(formBean, accountState);
    }

    public SavingsAccountDetailDto createAccountInPendingApprovalState(
            CreateSavingsAccountFormBean formBean) {
        Short accountState = ACCOUNT_STATE_PENDNG_APPROVAL;
        return createAccount(formBean, accountState);
    }

    private SavingsAccountDetailDto createAccount(CreateSavingsAccountFormBean formBean, Short accountState) {
        
    	SavingsProductReferenceDto productReference = formBean.getProduct();
        SavingsProductDto savingsProduct = productReference.getSavingsProductDetails();
        Integer productId = savingsProduct.getProductDetails().getId();
        Integer customerId = formBean.getCustomer().getCustomerId();

        // TODO - deposit amount should be validated before reaching here...
        String recommendedOrMandatoryAmount = formBean.getMandatoryDepositAmount();
        SavingsAccountCreationDto savingsAccountCreation = new SavingsAccountCreationDto(
                productId, customerId, accountState, recommendedOrMandatoryAmount);
        
        Long savingsId = savingsServiceFacade.createSavingsAccount(savingsAccountCreation, formBean.getQuestionGroups());
        SavingsAccountDetailDto savingsAccountDetailDto = savingsServiceFacade.retrieveSavingsAccountDetails(savingsId);

        return savingsAccountDetailDto;
    }

    public void customerSelected(Integer customerId, CreateSavingsAccountFormBean formBean) {
    	
        CustomerDto customer = savingsServiceFacade.retreieveCustomerDetails(customerId); 
        formBean.setCustomer(customer);
    }

    public void loadQuestionGroups(CreateSavingsAccountFormBean formBean) {
        List<QuestionGroupDetail> questionGroups = questionnaireServiceFacade.getQuestionGroups("Create", "Savings");
        formBean.setQuestionGroups(questionGroups);
    }
    
    public void loadProduct(Integer productId,
            CreateSavingsAccountFormBean formBean) {

        SavingsProductReferenceDto product = savingsServiceFacade
                .retrieveSavingsProductReferenceData(productId);
        formBean.setProductId(productId);
        formBean.setProduct(product);
        formBean.setMandatoryDepositAmount(product.getSavingsProductDetails()
                .getAmountForDeposit().toString());
        formBean.setSavingsTypes(getSavingsTypes());
        formBean.setRecurrenceTypes(getRecurrenceTypes());
        formBean.setRecurrenceFrequencies(getRecurrenceFrequencies());
    }

    public void getProductOfferings(CreateSavingsAccountFormBean formBean) {
        List<PrdOfferingDto> savingsProducts = savingsServiceFacade.retrieveApplicableSavingsProductsForCustomer(formBean.getCustomer().getCustomerId());
        Map<String, String> offerings = new HashMap<String, String>(savingsProducts.size());
        
        for (PrdOfferingDto offering : savingsProducts) {
            offerings.put(offering.getPrdOfferingId().toString(), offering.getPrdOfferingName());
        }
        formBean.setProductOfferings(savingsProducts);
        formBean.setProductOfferingOptions(offerings);
    }

    public CustomerSearchResultsDto searchCustomers(
            CreateSavingsAccountFormBean formBean) {
        
        // Search result cap. This is needed until ajax search is implemented.
        Integer searchCap = 1000;
        
    	// FIXME - keithw - selected pageNumber and pageSize info should be passed in on bean.
    	CustomerSearchDto customerSearchDto = new CustomerSearchDto(formBean.getSearchString(), Integer.valueOf(1), searchCap);
    	List<CustomerSearchResultDto> pagedDetails = this.savingsServiceFacade.retrieveCustomerThatQualifyForSavings(customerSearchDto);
    	
    	SearchDetailsDto searchDetails = new SearchDetailsDto(pagedDetails.size(), 1, 1, searchCap);
        return new CustomerSearchResultsDto(searchDetails, pagedDetails);
    }

    /**
     * @see org.mifos.application.meeting.util.helpers.RecurrenceType
     * @return A map of recurrence type ID to message key which points to a
     *         localized name.
     */
    private Map<String, String> getRecurrenceFrequencies() {
        Map<String, String> map = new HashMap<String, String>(3);
        map.put(Integer.toString(RECURRENCE_MONTHLY),
                "createSavingsAccount.recurrenceFrequency.month");
        map.put(Integer.toString(RECURRENCE_DAILY),
                "createSavingsAccount.recurrenceFrequency.day");
        return map;
    }

    /**
     * @see org.mifos.application.meeting.util.helpers.RecurrenceType
     * @return A map of recurrence type ID to message key which points to a
     *         localized name.
     */
    private Map<String, String> getRecurrenceTypes() {
        Map<String, String> map = new HashMap<String, String>(3);
        map.put(Integer.toString(RECURRENCE_WEEKLY),
                "createSavingsAccount.recurrenceType.weekly");
        map.put(Integer.toString(RECURRENCE_MONTHLY),
                "createSavingsAccount.recurrenceType.monthly");
        map.put(Integer.toString(RECURRENCE_DAILY),
                "createSavingsAccount.recurrenceType.daily");
        return map;
    }

    /**
     * @see org.mifos.accounts.productdefinition.util.helpers.SavingsType
     * @return A map of savings type ID to message key which points to a
     *         localized name.
     */
    private Map<String, String> getSavingsTypes() {
        Map<String, String> map = new HashMap<String, String>(2);
        map.put(Integer.toString(1),
                "createSavingsAccount.savingsType.mandatory");
        map.put(Integer.toString(2),
                "createSavingsAccount.savingsType.voluntary");
        return map;
    }
}