[#ftl]
[#--
* Copyright (c) 2005-2011 Grameen Foundation USA
*  All rights reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
*  implied. See the License for the specific language governing
*  permissions and limitations under the License.
*
*  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
*  explanation of the license and how it is applied.
--]

[@layout.webflow currentTab="ClientsAndAccounts"
                 currentState="createLoanAccount.flowState.reviewInstallments" 
                 states=["createLoanAccount.flowState.selectCustomer", 
                         "createLoanAccount.flowState.enterAccountInfo",
                         "createLoanAccount.flowState.reviewInstallments", 
                         "createLoanAccount.flowState.reviewAndSubmit"]]

<span id="page.id" title="SchedulePreview"></span>

[#if loanProductReferenceData.variableInstallmentsAllowed]
<script>
$(document).ready(function() {

    $(":regex(id, .*\\.[0-9]+)").datepicker({
    	[#if loanAccountFormBean.locale.language?lower_case == "zh"]
			dateFormat: 'y-m-d',
		[/#if]
		[#if loanAccountFormBean.locale.language?lower_case == "en"]
			[#if loanAccountFormBean.locale.country?lower_case == "us"]
			dateFormat: 'mm/dd/y',
			[#else]
			dateFormat: 'dd/mm/y',
			[/#if]
		[/#if]
        showOn: "button",
        buttonImage: "pages/framework/images/mainbox/calendaricon.gif",
		buttonImageOnly: true
    });
  } 
);
	$(function() {
		[#if loanAccountFormBean.locale.language == "en"]
			$.datepicker.setDefaults($.datepicker.regional['']);
		[#else]
			$.datepicker.setDefaults($.datepicker.regional['${loanAccountFormBean.locale.language?lower_case}']);
		[/#if]
	}); 
</script>
[/#if]


[#if customerSearchFormBean.redoLoanAccount]
<p class="red standout" style="margin-bottom: 5px;">[@spring.message "redoLoanAccount.wizard.highlightedNote" /]</p>
<h1>[@spring.message "redoLoanAccount.wizard.title" /] - <span class="standout">[@spring.message "createLoanAccount.reviewInstallments.pageSubtitle" /]</span></h1>
[#else]
<h1>[@spring.message "createLoanAccount.wizard.title" /] - <span class="standout">[@spring.message "createLoanAccount.reviewInstallments.pageSubtitle" /]</span></h1>
[/#if] 
<p>[@spring.message "createLoanAccount.reviewInstallments.instructions" /]</p>
<br/>

[@form.errors "loanScheduleFormBean.*"/]
[@form.errors "cashFlowSummaryFormBean.*"/]

[#setting number_format=loanAccountFormBean.numberFormatForMonetaryAmounts]
<div class="product-summary">
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "selectProduct.accountOwnerName"/]</span></div>
	    <div class="value">${loanScheduleReferenceData.accountOwner}</div>
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "reviewInstallments.loanAmount"/]</span></div>
	    <div class="value"><span id="schedulepreview.text.loanamount">${loanAccountFormBean.amount?string.number}</span></div>
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "reviewInstallments.disbursmentDate"/]</span></div>
	    <div class="value">${loanScheduleReferenceData.disbursementDate?date?string.medium}</div>
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "productSummary.fixedRepaymentSchedule"/]</span></div>
	    [#if loanProductReferenceData.fixedRepaymentSchedule]
	        <div class="value">[@spring.message "boolean.yes"/]</div>
	    [#else]
	        <div class="value">[@spring.message "boolean.no"/]</div>
	    [/#if]
	</div>
[#if loanProductReferenceData.variableInstallmentsAllowed]
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "productSummary.variabeInstallments.minInstallmentAmount"/]</span></div>
	    [#if loanProductReferenceData.minInstallmentAmount == 0.0]
	    <div class="value">[@spring.message "productSummary.variabeInstallments.minInstallmentAmount.notapplicable"/]</div>
	    [#else]
	    <div class="value">${loanProductReferenceData.minInstallmentAmount?string.number}</div>
	    [/#if]
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "productSummary.variableInstallmentsAllowed"/]</span></div>
	    <div class="value">[@spring.message "boolean.yes"/]</div>
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "productSummary.variabeInstallments.minGap"/]</span></div>
	    <div class="value">${loanProductReferenceData.minGapInDays?string.number}<span>&nbsp;[@spring.message "productSummary.variabeInstallments.days"/]</span></div>
	</div>
	<div class="row">
	    <div class="attribute"><span class="standout">[@spring.message "productSummary.variabeInstallments.maxGap"/]</span></div>
	    [#if loanProductReferenceData.maxGapInDays??]
        <div class="value">${loanProductReferenceData.maxGapInDays?string.number}<span>&nbsp;[@spring.message "productSummary.variabeInstallments.days"/]</span></div>
        [#else]
		<div class="value">[@spring.message "productSummary.variabeInstallments.minInstallmentAmount.notapplicable"/]</div>        
        [/#if]
	</div>
[/#if]
</div>
<div class="product-summary">
	[#assign index = 0]
	[#list loanScheduleFormBean.applicableFees as defaultFee]
	    <div class="row">
	        <div class="attribute"><span class="standout">${defaultFee.name}</span></div>
	        <div class="value">
	        	[#if defaultFee.rateBasedFee]
	        		[#setting number_format="#.###"]
	        		${defaultFee.rate?string.number}%
					[#setting number_format=loanAccountFormBean.numberFormatForMonetaryAmounts]	        		
	        	[#else]
	        		${defaultFee.amountAsNumber?string.number}
	        	[/#if]
	        	
				[#if defaultFee.rateBasedFee]
		        	${defaultFee.feeFormula.name}
		       	[/#if]
		       	
		       	[#if defaultFee.feeFrequency.oneTime]
		       		<span class="standout">[@spring.message "createLoanAccount.periodicity"/]</span> 
		       		<span style="margin-left: 5px;">[#if defaultFee.feeFrequency.oneTime][@spring.message "createLoanAccount.periodicity.onetime"/][/#if]</span>
		       		<span class="standout" style="margin-left: 5px;">[@spring.message "createLoanAccount.frequency"/]</span>
		       		<span style="margin-left: 5px;">
		       			[#if defaultFee.feeFrequency.paymentId == 1][@spring.message "createLoanAccount.frequency.upfront"/][/#if]
		       			[#if defaultFee.feeFrequency.paymentId == 2][@spring.message "createLoanAccount.frequency.timeofdisbursement"/][/#if]
		       			[#if defaultFee.feeFrequency.paymentId == 3][@spring.message "createLoanAccount.frequency.timeoffirstloanrepayment"/][/#if]
		       		</span>
				[#else]		       		        	
		        	<span class="standout">[@spring.message "createLoanAccount.periodicity"/]</span> ${defaultFee.feeFrequency.recurAfterPeriod}
		        	[#if defaultFee.feeFrequency.weekly]
		        		<span>[@spring.message "createLoanAccount.weeks"/]</span>
		       		[#else]
		        		<span>[@spring.message "createLoanAccount.months"/]</span>
		       		[/#if]
		       	[/#if]
	       	</div>
	    <!-- end of row -->
	    </div>
 		[#assign index = index + 1]	    	
    [/#list]  
    [#list loanScheduleFormBean.applicablePenalties as defaultPenalty]
        <div class="row">
            <div class="attribute"><span class="standout">${defaultPenalty.penaltyName}</span></div>
            <div class="value">
                [#if defaultPenalty.rateBasedPenalty]
                    [#setting number_format="#.###"]
                    ${defaultPenalty.rate?string.number}%
                    [#setting number_format=loanAccountFormBean.numberFormatForMonetaryAmounts]
                [#else]
                    ${defaultPenalty.amountAsNumber?string.number}
                [/#if]
                
                [#if defaultPenalty.rateBasedPenalty]
                    ${defaultPenalty.penaltyFormula.name}
                [/#if]
                
                <span class="standout">[@spring.message "createLoanAccount.periodicity"/]</span> ${defaultPenalty.penaltyFrequency.name}
            </div>
        <!-- end of row -->
        </div>
    [/#list]    
<!-- end of product summary -->
</div>
<br/>
<br/>

[#if loanProductReferenceData.variableInstallmentsAllowed]
<form action="${flowExecutionUrl}" method="post" class="two-columns">
[/#if]
<h1><span class="standout">[@spring.message "reviewInstallments.heading" /]</span></h1>
<table id="installments" style="margin-bottom: 15px;">
	<thead>
		<tr>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.installmentHeading" /]</th>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.dueDateHeading" /]</th>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.principalHeading" /]</th>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.interestHeading" /]</th>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.feesHeading" /]</th>
			<th style="border-top: 1px solid grey;">[@spring.message "reviewInstallments.totalHeading" /]</th>
		</tr>
	</thead>
	<tbody>
		[#if loanProductReferenceData.compareCashflowEnabled]
			[#assign ind = 0]
			[#list cashFlowSummaryFormBean.variableInstallments as row]
			<tr>
				<td style="border-top: 1px solid grey;">${row.installmentNumber?string.number}</td>
				[#if loanProductReferenceData.variableInstallmentsAllowed]
					[@spring.bind "cashFlowSummaryFormBean.installments[${ind}]"/]
					<td style="border-top: 1px solid grey;"><input type="text" name="installments[${ind}]" size="10" value="${cashFlowSummaryFormBean.parseInstallment(ind)}" id="installment.dueDate.${ind}" class="date-pick" /></td>
				[#else]
					[@spring.bind "cashFlowSummaryFormBean.installments[${ind}]"/]
					<td style="border-top: 1px solid grey;">${cashFlowSummaryFormBean.parseInstallment(ind)}</td>
				[/#if]
				<td style="border-top: 1px solid grey;">${row.principal?string.number}</td>
				<td style="border-top: 1px solid grey;">${row.interest?string.number}</td>
				<td style="border-top: 1px solid grey;">${row.fees?string.number}</td>
				
				[#if loanProductReferenceData.variableInstallmentsAllowed]
					[@spring.bind "loanScheduleFormBean.installmentAmounts[${ind}]"/]
					[#if cashFlowSummaryFormBean.installmentAmounts[ind]??]
						[#if ind == loanAccountFormBean.numberOfInstallments - 1]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="${cashFlowSummaryFormBean.installmentAmounts[ind]}" disabled="disabled" /></td>
						[#else]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="${cashFlowSummaryFormBean.installmentAmounts[ind]}" /></td>
						[/#if]
					[#else]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="0" /></td>
					[/#if]
				[#else]
					<td style="border-top: 1px solid grey;">${row.total?string.number}</td>
				[/#if]
			</tr>
			[#assign ind = ind + 1]
			[/#list]
		[#else]
			[#assign ind = 0]
			[#list loanScheduleFormBean.variableInstallments as row]
			<tr>
				<td style="border-top: 1px solid grey;">${row.installmentNumber?string.number}</td>
				[#if loanProductReferenceData.variableInstallmentsAllowed]
					[@spring.bind "loanScheduleFormBean.installments[${ind}]"/]
					<td style="border-top: 1px solid grey;"><input type="text" name="installments[${ind}]" size="10" value="${loanScheduleFormBean.parseInstallment(ind)}" id="installment.dueDate.${ind}" class="date-pick" /></td>
				[#else]
					[@spring.bind "loanScheduleFormBean.installments[${ind}]"/]
					<td style="border-top: 1px solid grey;">${loanScheduleFormBean.parseInstallment(ind)}</td>
				[/#if]
				<td style="border-top: 1px solid grey;">${row.principal?string.number}</td>
				<td style="border-top: 1px solid grey;">${row.interest?string.number}</td>
				<td style="border-top: 1px solid grey;">${row.fees?string.number}</td>
				
				[#if loanProductReferenceData.variableInstallmentsAllowed]
					[@spring.bind "loanScheduleFormBean.installmentAmounts[${ind}]"/]
					[#if loanScheduleFormBean.installmentAmounts[ind]??]
						[#if ind == loanAccountFormBean.numberOfInstallments - 1]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="${loanScheduleFormBean.installmentAmounts[ind]}" disabled="disabled" /></td>
						[#else]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="${loanScheduleFormBean.installmentAmounts[ind]}" /></td>
						[/#if]
					[#else]
						<td style="border-top: 1px solid grey;"><input type="text" name="installmentAmounts[${ind}]" size="10" value="0" /></td>
					[/#if]
				[#else]
					<td style="border-top: 1px solid grey;">${row.total?string.number}</td>
				[/#if]
			</tr>
			[#assign ind = ind + 1]
			[/#list]
		[/#if]
	</tbody>
</table>

[#if cashflowSummaryDetails??]
<br />
<h1><span class="standout">[@spring.message "cashflow.summary.heading" /]</span></h1>
<table id="cashflow">
	<thead>
	<tr>
		<th style="border-top: 1px solid grey;">[@spring.message "cashflow.summary.column.months" /]</th>
		<th style="border-top: 1px solid grey;">[@spring.message "cashflow.summary.column.cumulative" /]</th>
		<th style="border-top: 1px solid grey;">[@spring.message "cashflow.summary.column.installmentpermonth" /]</th>
		<th style="border-top: 1px solid grey;">[@spring.message "cashflow.summary.column.installmentpermonthpercentage" /]</th>
		<th style="border-top: 1px solid grey;">[@spring.message "cashflow.summary.column.notes" /]</th>
	</tr>
	</thead>
	<tbody>
		[#list cashflowSummaryDetails as row]
		<tr>
			<td style="border-top: 1px solid grey; width: 100px;">${row.month} ${row.year}</td>
			<td style="border-top: 1px solid grey;">${row.cumulativeCashFlow?number}</td>
			<td style="border-top: 1px solid grey;">${row.diffCumulativeCashflowAndInstallment?number}</td>
			<td style="border-top: 1px solid grey;">${row.diffCumulativeCashflowAndInstallmentPercent?number}</td>
			<td style="border-top: 1px solid grey;">${row.notes}</td>
		</tr>
		[/#list]
	</tbody>
</table>
	[#if loanProductReferenceData.variableInstallmentsAllowed]
		<input type="submit" id="previewBtn" class="submit" style="margin-left: 0px;" name="_eventId_editCashflow" value='[@spring.message "widget.form.buttonLabel.editcashflowinfo" /]' />
	[#else]
	<form action="${flowExecutionUrl}" method="post">
		<input type="submit" id="previewBtn" class="submit" style="margin-left: 0px;" name="_eventId_editCashflow" value='[@spring.message "widget.form.buttonLabel.editcashflowinfo" /]' />
	</form>
	[/#if]
[/#if]

[#if loanProductReferenceData.variableInstallmentsAllowed]
	<div class="row webflow-controls">
		<input type="submit" id="schedulePreview.button.validate" class="submit" name="_eventId_validate" value=[@spring.message "widget.form.buttonLabel.validate" /] />
		<input type="submit" id="previewBtn" class="submit" style="margin-left: 1em;" name="_eventId_preview" value=[@spring.message "widget.form.buttonLabel.preview" /] />
        [@form.cancelButton id="schedulePreview.button.cancel" label="widget.form.buttonLabel.cancel" webflowEvent="cancel" /]
    </div>
</form>
[#else]
<form action="${flowExecutionUrl}" method="post" class="two-columns">
<div class="row webflow-controls">
        [@form.submitButton label="widget.form.buttonLabel.preview" id="schedulePreview.button.preview" webflowEvent="preview" /]
        [@form.cancelButton label="widget.form.buttonLabel.cancel" id="schedulePreview.button.cancel" webflowEvent="cancel" /]
    </div>
</form>
[/#if]
<br/>
<style>
.ui-widget-content {
 border: 0;
}
</style>
[/@layout.webflow]