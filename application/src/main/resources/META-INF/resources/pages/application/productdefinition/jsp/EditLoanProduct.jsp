<%--
Copyright (c) 2005-2011 Grameen Foundation USA
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.

See also http://www.apache.org/licenses/LICENSE-2.0.html for an
explanation of the license and how it is applied.
--%>

<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="/tags/date" prefix="date"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<fmt:setLocale value='${sessionScope["org.apache.struts.action.LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.ProductDefinitionResources"/>

<tiles:insert definition=".view">
	<tiles:put name="body" type="string">
		<span id="page.id" title="EditLoanProduct"></span>
		<script src="pages/framework/js/date.js"></script>
		<script type="text/javascript" src="pages/application/loan/js/EditLoanProduct.js"></script>
        <html-el:form action="/loanproductaction"
			onsubmit="return (validateMyForm(startDate,startDateFormat,startDateYY) &&
				validateMyForm(endDate,endDateFormat,endDateYY))"
			focus="prdOfferingName">
            <c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'repaymentSchedulesIndependentOfMeetingIsEnabled')}"
                var="repaymentSchedulesIndependentOfMeetingIsEnabled" />
            <c:set value="${requestScope.GlNamesMode}" var="GlNamesMode"/>
			<c:choose>
				<c:when test="${param.prdOfferName == null}">
					<c:set var="prdOfferName"
						value="${sessionScope.loanproductactionform.prdOfferingName}" />
				</c:when>
				<c:otherwise>
					<c:set var="prdOfferName" value="${param.prdOfferName}" />
				</c:otherwise>
			</c:choose>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt">
					<html-el:link styleId="EditLoanProduct.link.admin"
						href="loanproductaction.do?method=cancelCreate&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
						<mifos:mifoslabel name="product.admin"
							bundle="ProductDefUIResources" />
					</html-el:link> / <html-el:link styleId="EditLoanProduct.link.viewLoanProducts"
						href="loanproductaction.do?method=viewAllLoanProducts&recordOfficeId=${UserContext.branchId}&recordLoanOfficerId=${UserContext.id}&randomNUm=${sessionScope.randomNUm}">
						<fmt:message key="product.viewLoanProducts">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
							bundle="ProductDefUIResources" /></fmt:param>
						</fmt:message>
					</html-el:link> / <html-el:link styleId="EditLoanProduct.link.viewLoanProduct"
						href="loanproductaction.do?method=get&prdOfferingId=${sessionScope.loanproductactionform.prdOfferingId}&randomNUm=${sessionScope.randomNUm}">
						<c:out value="${prdOfferName}" />
					</html-el:link></span></td>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" align="left" valign="top" class="paddingL15T15">
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td class="headingorange"><span class="heading"><c:out
								value="${prdOfferName}" /> - </span> 
								<fmt:message key="product.editLoanInfo">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message></td>
						</tr>
						<tr>
							<td class="fontnormal">
								<fmt:message key="product.editPreviewSubmitLoan">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message>
							<br>
							<mifos:mifoslabel name="product.fieldsrequired" mandatory="yes"
								bundle="ProductDefUIResources" /></td>
						</tr>
					</table>
					<br>
					<font class="fontnormalRedBold"><span id="EditLoanProduct.error.message"><html-el:errors
						bundle="ProductDefUIResources" /></span></font>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold">
								<fmt:message key="product.loanProductDetails">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message><br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right"><span id="EditLoanProduct.label.name"><mifos:mifoslabel
								name="product.prodinstname" mandatory="yes"
								bundle="ProductDefUIResources" isColonRequired="yes"/></span></td>
							<td width="70%" valign="top"><mifos:mifosalphanumtext
								styleId="EditLoanProduct.input.name"
								property="prdOfferingName" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><span id="EditLoanProduct.label.shortName"><mifos:mifoslabel name="product.shortname"
								mandatory="yes" bundle="ProductDefUIResources" isColonRequired="yes"/></span></td>
							<td valign="top"><mifos:mifosalphanumtext
								styleId="EditLoanProduct.input.shortName"
								property="prdOfferingShortName" maxlength="4" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right" valign="top"><span class="mandatorytext"></span>
							<span id="EditLoanProduct.label.description">
							<mifos:mifoslabel name="product.desc"
								bundle="ProductDefUIResources" isColonRequired="yes"/></span></td>
							<td valign="top"><html-el:textarea styleId="EditLoanProduct.input.description" property="description"
								style="width:320px; height:110px;">
							</html-el:textarea></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel name="product.prodcat"
								mandatory="yes" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td valign="top"><mifos:select  styleId="EditLoanProduct.input.category" property="prdCategory">
								<c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanProductCategoryList')}"
									var="category">
									<html-el:option value="${category.productCategoryID}">${category.productCategoryName}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel name="product.startdate"
								mandatory="yes" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td valign="top"><date:datetag property="startDate" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><span class="mandatorytext"></span> <mifos:mifoslabel
								name="product.enddate" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td valign="top"><date:datetag property="endDate" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel mandatory="yes"
								name="product.applfor" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td valign="top"><mifos:select
								styleId="EditLoanProduct.input.applicableFor" property="prdApplicableMaster">
								<c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanApplForList')}"
									var="prdAppl">
									<html-el:option value="${prdAppl.id}">${prdAppl.name}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
                        <tr class="fontnormal">
                            <c:if test='${sessionScope.isMultiCurrencyEnabled}'>
                                <td align="right"><mifos:mifoslabel name="product.currency"
                                    bundle="ProductDefUIResources" isColonRequired="yes" /></td>
                                <td><html:text property="currencyId" name="notAllowedToEdit" 
                                    disabled="true" readonly="true"
                                    value="${sessionScope.currencyCode}" /></td>
                                <br />
                            </c:if>
                        </tr>
                        <tr class="fontnormal">
							<td align="right">
								<span id="EditLoanProduct.label.includeInLoanCycleCounter">
								<fmt:message key="product.inclInLoanCycleCounter">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message></span>:
							</td>
							<td valign="top"><html-el:checkbox styleId="EditLoanProduct.input.includeInLoanCycleCounter" property="loanCounter"
								value="1" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right">
								<span id="EditLoanProduct.label.includeInterestWaiver">
								<mifos:mifoslabel	name="product.include.interest.waiver" bundle="ProductDefUIResources" /></span>:
							</td>
							<td valign="top"><html-el:checkbox styleId="EditLoanProduct.input.includeInterestWaiver" property="waiverInterest"
								value="1" /></td>
						</tr>
						</table>
                        <table width="93%" border="0" cellpadding="3" cellspacing="0">
                            <tr>
                                <td colspan="2" class="fontnormalbold"><mifos:mifoslabel
                                    name="product.questiongroups" bundle="ProductDefUIResources" />
                                    <br>
                                    <br>
                                </td>
                            </tr>
                            <tr class="fontnormal">
                                <td width="30%" align="right" valign="top"><mifos:mifoslabel
                                    name="product.assocquestiongroups" bundle="ProductDefUIResources"
                                    isColonRequired="yes" /></td>
                                <td width="70%" valign="top">
                                <table width="80%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td class="fontnormal"><mifos:mifoslabel
                                            name="product.clickquestiongroups" bundle="ProductDefUIResources" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><img src="pages/framework/images/trans.gif"
                                            width="1" height="1"></td>
                                    </tr>
                                </table>
                                <c:set var="SrcQGList" scope="request"
                                    value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'SrcQGList')}" />
                                <c:set var="SelectedQGList" scope="request"
                                    value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'SelectedQGList')}" />
                                <mifos:MifosSelect property="loanOfferingQGs"
                                    input="SrcQGList" output="SelectedQGList"
                                    property1="id" property2="title" multiple="true">
                                </mifos:MifosSelect></td>
                            </tr>

                        </table>
						<!--<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel mandatory="yes"
								name="product.max" bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="product.amount" bundle="ProductDefUIResources" /> :</td>
							<td valign="top"><mifos:decimalinput
								property="maxLoanAmount" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel mandatory="yes"
								name="product.min" bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="product.amount" bundle="ProductDefUIResources" /> :</td>
							<td valign="top"><mifos:decimalinput
								property="minLoanAmount" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel name="product.default"
								bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}"
								bundle="ProductDefUIResources" /> <mifos:mifoslabel
								name="product.amount" bundle="ProductDefUIResources" /> :</td>
							<td valign="top"><mifos:decimalinput
								property="defaultLoanAmount" /></td>
						</tr>
					-->
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
							<tr>
								<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
									name="product.loanamount" bundle="ProductDefUIResources" /><br>
								<br>
								</td>
							</tr>

							<tr class="fontnormal">
								<td width="30%" align="right"><mifos:mifoslabel
									name="product.calcloanamount" mandatory="yes"
									bundle="ProductDefUIResources" />:</td>
								<td width="70%" valign="top"><html-el:radio
									styleId="EditLoanProduct.input.sameForAllLoans"
									property="loanAmtCalcType" value="1" onclick="checkRow();" />
								<span id="EditLoanProduct.label.sameForAllLoans">
								<mifos:mifoslabel name="product.sameforallloans"
									bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp; <html-el:radio
									styleId="EditLoanProduct.input.byLastLoanAmount"
									property="loanAmtCalcType" value="2" onclick="checkRow();" />
								<span id="EditLoanProduct.label.byLastLoanAmount">
								<mifos:mifoslabel name="product.bylastloanamount"
									bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp; <html-el:radio
									styleId="EditLoanProduct.input.byLoanCycle"
									property="loanAmtCalcType" value="3" onclick="checkRow();" />
								<span id="EditLoanProduct.label.byLoanCycle">
								<mifos:mifoslabel name="product.byloancycle"
									bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp;</td>
							</tr>
                            <tr class="fontnormal">
                                <td width="30%" align="right"></td>
                                <td width="70%" valign="top">
					               <div id="option0" style="display:block;">
						              <table width="60%" border="0" cellpadding="3" cellspacing="0">
									   <tr>
										  <td width="33%" class="drawtablehd"><mifos:mifoslabel
											 name="product.minloanamt" bundle="ProductDefUIResources" /></td>
										  <td width="34%" class="drawtablehd"><mifos:mifoslabel
											 name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
										  <td width="33%" class="drawtablehd"><mifos:mifoslabel
											 name="product.defamt" bundle="ProductDefUIResources" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="minLoanAmount" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="maxLoanAmount" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="defaultLoanAmount" /></td>
									   </tr>
								    </table>
						          </div>
						          <div id="option1" style="display:none;">
								    <table width="60%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.lastloanamount"
													bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount1"
													size="10" property="startRangeLoanAmt1" style="border:0"
													readonly="true" value ="0"/> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount1"
													property="endRangeLoanAmt1"
													onblur="changeValue(event, this, this.value,1)" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount1"
													size="10" property="lastLoanMinLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount1"
													size="10" property="lastLoanMaxLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount1"
													size="10" property="lastLoanDefaultLoanAmt1" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount2"
													size="10" property="startRangeLoanAmt2" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount2"
													property="endRangeLoanAmt2" onblur="changeValue(event, this, this.value,2)" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount2"
													size="10" property="lastLoanMinLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount2"
													size="10" property="lastLoanMaxLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount2"
													size="10" property="lastLoanDefaultLoanAmt2" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount3"
													size="10" property="startRangeLoanAmt3" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount3"
													property="endRangeLoanAmt3" onblur="changeValue(event, this,this.value,3)"/></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount3"
													size="10" property="lastLoanMinLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount3"
													size="10" property="lastLoanMaxLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount3"
													size="10" property="lastLoanDefaultLoanAmt3" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount4"
													size="10" property="startRangeLoanAmt4" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount4"
													property="endRangeLoanAmt4" onblur="changeValue(event, this,this.value,4)" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount4"
													size="10" property="lastLoanMinLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount4"
													size="10" property="lastLoanMaxLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount4"
													size="10" property="lastLoanDefaultLoanAmt4" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount5"
													size="10" property="startRangeLoanAmt5" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount5"
													property="endRangeLoanAmt5"  onblur="changeValue(event, this,this.value,5)"/></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount5"
													size="10" property="lastLoanMinLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount5"
													size="10" property="lastLoanMaxLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount5"
													size="10" property="lastLoanDefaultLoanAmt5" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeLoanAmount6"
													size="10" property="startRangeLoanAmt6" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													styleId="EditLoanProduct.input.endRangeLoanAmount6"
													property="endRangeLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.minLoanAmount6"
													size="10" property="lastLoanMinLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.maxLoanAmount6"
													size="10" property="lastLoanMaxLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput
													styleId="EditLoanProduct.input.defaultLoanAmount6"
													size="10" property="lastLoanDefaultLoanAmt6" /></td>
											</tr>
										</table>
								    </div>
						            <div id="option2" style="display:none;">
						              <table width="60%" border="0" cellspacing="0" cellpadding="3">
									   <tr>
										  <td width="25%" class="drawtablehd"><mifos:mifoslabel
											 name="product.loancycleno" bundle="ProductDefUIResources" />
										  </td>
										  <td width="25%" class="drawtablehd"><mifos:mifoslabel
											 name="product.minloanamt" bundle="ProductDefUIResources" /></td>
										  <td width="25%" class="drawtablehd"><mifos:mifoslabel
											 name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
										  <td width="25%" class="drawtablehd"><mifos:mifoslabel
											 name="product.defamt" bundle="ProductDefUIResources" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">0</td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanMinLoanAmt1" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanMaxLoanAmt1" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanDefaultLoanAmt1" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">1</td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanMinLoanAmt2" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanMaxLoanAmt2" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											 size="10" property="cycleLoanDefaultLoanAmt2" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">2</td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMinLoanAmt3" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMaxLoanAmt3" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanDefaultLoanAmt3" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">3</td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMinLoanAmt4" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMaxLoanAmt4" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanDefaultLoanAmt4" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">4</td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMinLoanAmt5" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMaxLoanAmt5" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanDefaultLoanAmt5" /></td>
									   </tr>
									   <tr>
										  <td class="drawtablerow">&gt;4</td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMinLoanAmt6" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanMaxLoanAmt6" /></td>
										  <td class="drawtablerow"><mifos:decimalinput
											size="10" property="cycleLoanDefaultLoanAmt6" /></td>
									   </tr>
								</table>
                                </div>
                            </td>
                        </tr>
                    </table>
					<br>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="product.status" bundle="ProductDefUIResources" /> <br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right"><mifos:mifoslabel
								mandatory="yes" name="product.chgStatus"
								bundle="ProductDefUIResources" /> :</td>
							<td width="70%"><mifos:select styleId="EditLoanProduct.input.status" property="prdStatus">
								<c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanPrdStatusList')}"
									var="status">
									<html-el:option value="${status.offeringStatusId}">${status.prdState.name}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
					</table>
					<br>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold">
								<fmt:message key="product.productRate">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.SERVICE_CHARGE}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message><br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right">
								<span class="mandatorytext"> <font color="#FF0000">*</font></span>
								<fmt:message key="product.rateType">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.SERVICE_CHARGE}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message>:</td>
							<td width="70%" valign="top"><mifos:select
								styleId="EditLoanProduct.input.interestTypes" property="interestTypes">
								<c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'InterestTypesList')}"
									var="intType">
									<html-el:option value="${intType.id}">${intType.name}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
						<tr class="fontnormal">
							<td align="right">
							<span class="mandatorytext"> <font color="#FF0000">*</font></span>
							<fmt:message key="product.minRate">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.SERVICE_CHARGE}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message>:</td>
							<td valign="top"><mifos:decimalinput
								styleId="EditLoanProduct.input.minInterestRate" property="minInterestRate"  />
							<mifos:mifoslabel name="product.rate"
								bundle="ProductDefUIResources" /></td>
						</tr>
                        <tr class="fontnormal">
                            <td align="right">
                            <span class="mandatorytext"> <font color="#FF0000">*</font></span>
                            <fmt:message key="product.maxRate">
                                <fmt:param><mifos:mifoslabel
                                name="${ConfigurationConstants.SERVICE_CHARGE}"
                                bundle="ProductDefUIResources" /></fmt:param>
                                </fmt:message>:</td>
                            <td valign="top"><mifos:decimalinput
                                styleId="EditLoanProduct.input.maxInterestRate" property="maxInterestRate"  />
                            <mifos:mifoslabel name="product.rate"
                                bundle="ProductDefUIResources" /></td>
                        </tr>
						<tr class="fontnormal">
							<td align="right"><span class="mandatorytext"><font color="#FF0000">*</font></span> 
							<fmt:message key="product.defaultRate">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.SERVICE_CHARGE}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message>:</td>
							<td valign="top"><mifos:decimalinput
								styleId="EditLoanProduct.input.defaultInterestRate" property="defInterestRate"  />
							<mifos:mifoslabel name="product.rate"
								bundle="ProductDefUIResources" /></td>
						</tr>
					</table>
					<br>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="product.repaysch" bundle="ProductDefUIResources" /> <br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
                            <td width="30%" align="right" valign="top">
                                <mifos:mifoslabel name="product.fixedRepaymentSchedule" bundle="ProductDefUIResources" isColonRequired="yes" />
                            </td>
                            <td valign="top">
                                <html-el:checkbox styleId="createLoanProduct.checkbox.fixedRepaymentSchedule" property="isFixedRepaymentSchedule" value="1" />
                            </td>
                        </tr>
                        <c:if test="${repaymentSchedulesIndependentOfMeetingIsEnabled == '1'}">
                            <tr class="fontnormal">
                                <td width="30%" align="right" valign="top"><mifos:mifoslabel
                                    name="product.canConfigureVariableInstallments"
                                    bundle="ProductDefUIResources" /> :
                                </td>
                                <td valign="top"><html-el:checkbox styleId="EditLoanProduct.checkbox.canConfigureVariableInstallments"
                                    property="canConfigureVariableInstallments" onclick="showVariableInstallmentInputs();" value="1" />
                                </td>
                            </tr>
                            <tr class="fontnormal">
                                <td width="30%" align="right" valign="top">
                                    <div id="minimumGapBetweenInstallmentsLabelDiv"  style="display: none;">
                                        <mifos:mifoslabel name="product.minimumGapBetweenInstallments" mandatory="yes" bundle="ProductDefUIResources" /> :
                                    </div>
                                </td>
                                <td valign="top">
                                    <div id="minimumGapBetweenInstallmentsInputDiv" style="display: none;">
                                            <mifos:mifosnumbertext property="minimumGapBetweenInstallments" />
                                            <span id="days"> <mifos:mifoslabel name="product.days" bundle="ProductDefUIResources" /> </span>
                                    </div>
                                </td>
                           </tr>
                           <tr class="fontnormal">
                                <td width="30%" align="right" valign="top">
                                    <div id="maximumGapBetweenInstallmentsLabelDiv"  style="display: none;">
                                        <mifos:mifoslabel name="product.maximumGapBetweenInstallments" bundle="ProductDefUIResources" /> :
                                    </div>
                                </td>
                                <td valign="top">
                                    <div id="maximumGapBetweenInstallmentsInputDiv" style="display: none;">
                                            <mifos:mifosnumbertext property="maximumGapBetweenInstallments" />
                                            <span id="days"> <mifos:mifoslabel name="product.days" bundle="ProductDefUIResources" /> </span>
                                    </div>
                                </td>
                           </tr>
                           <tr class="fontnormal">
                                <td width="30%" align="right" valign="top">
                                    <div id="minimumInstallmentAmountLabelDiv"  style="display: none;">
                                        <mifos:mifoslabel name="product.minimumInstallmentAmount" bundle="ProductDefUIResources" /> :
                                    </div>
                                </td>
                                 <td valign="top">
                                     <div id="minimumInstallmentAmountInputDiv" style="display: none;">
                                        <mifos:decimalinput styleId= "EditLoanProduct.input.minimumInstallmentAmount" property="minimumInstallmentAmount" />
                                     </div>
                                </td>
                            </tr>
                        </c:if>

						<tr class="fontnormal">
							<td width="30%" align="right" valign="top"><mifos:mifoslabel
								name="product.cashFlowValidation" bundle="ProductDefUIResources"
								isColonRequired="yes" /></td>
							<td valign="top"><html-el:checkbox
								styleId="editLoanProduct.checkbox.cashFlowValidation"
								onclick="showCashFlowInputs();" property="cashFlowValidation"
								value="1" /></td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right" valign="top">
							    <div id="cashFlowThresholdDiv" style="display: none;">
							        <mifos:mifoslabel name="product.cashFlowThreshold" bundle="ProductDefUIResources" isColonRequired="yes"/>
							    </div>
							</td>
							<td valign="top">
							    <div id="cashFlowThresholdInputDiv" style="display: none;">
							        <mifos:decimalinput styleId="editLoanProduct.input.cashFlowThreshold" property="cashFlowThreshold"/>
							        ( <fmt:formatNumber value="${sessionScope.loanproductactionform.minCashFlowThreshold}" /> - <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCashFlowThreshold}" /> )
							        <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources"/></div>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right" valign="top">
							    <div id="cashFlowIndebtednessRatioDiv" style="display: none;">
							        <mifos:mifoslabel name="product.indebtednessRatio" bundle="ProductDefUIResources" isColonRequired="yes"/>
							    </div>
							</td>
							<td valign="top">
							    <div id="cashFlowIndebtednessRatioInputDiv" style="display: none;">
							        <mifos:decimalinput styleId="editLoanProduct.input.indebtednessRatio" property="indebtednessRatio"/>
							        ( <fmt:formatNumber value="${sessionScope.loanproductactionform.minIndebtednessRatio}" /> - <fmt:formatNumber value="${sessionScope.loanproductactionform.maxIndebtednessRatio}" /> )
							        <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources"/></div>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right" valign="top">
							    <div id="cashFlowRepaymentCapacityDiv" style="display: none;">
							        <mifos:mifoslabel name="product.repaymentCapacity" bundle="ProductDefUIResources" isColonRequired="yes"/>
							    </div>
							</td>
							<td valign="top">
							    <div id="cashFlowRepaymentCapacityInputDiv" style="display: none;">
							        <mifos:decimalinput styleId="editLoanProduct.input.repaymentCapacity" property="repaymentCapacity"/>
							        ( <fmt:formatNumber value="${sessionScope.loanproductactionform.minRepaymentCapacity}" /> - <fmt:formatNumber value="${sessionScope.loanproductactionform.maxRepaymentCapacity}" /> )
							        <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources"/></div>
							</td>
						</tr>

						<tr class="fontnormal">
							<td width="30%" align="right" valign="top"><mifos:mifoslabel
								name="product.freqofinst" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td width="70%" valign="top">
							<table width="60%" border="0" cellpadding="3" cellspacing="0">
								<tr class="fontnormal">
									<td align="left" valign="top"
										style="border-top: 1px solid #CECECE; border-left: 1px solid #CECECE; border-right: 1px solid #CECECE;">
									<table width="98%" border="0" cellspacing="0" cellpadding="2">
										<tr valign="top" class="fontnormal">
											<td width="24%"><html-el:radio styleId="EditLoanProduct.input.frequencyWeeks"
												property="freqOfInstallments" value="1"
												onclick="showMeetingFrequency();" /> 
												<span id="EditLoanProduct.label.frequencyWeeks">
												<mifos:mifoslabel
												name="product.weeks" bundle="ProductDefUIResources" /></span></td>
											<td width="24%"><html-el:radio styleId="EditLoanProduct.input.frequencyMonths"
												property="freqOfInstallments" value="2"
												onclick="showMeetingFrequency();" /> 
												<span id="EditLoanProduct.label.frequencyMonths">
												<mifos:mifoslabel
												name="product.months" bundle="ProductDefUIResources" /></span></td>
											<td width="24%"><html-el:radio styleId="EditLoanProduct.input.frequencyDays"
												property="freqOfInstallments" value="3"
												onclick="showMeetingFrequency();" /> 
												<span id="EditLoanProduct.label.frequencyDays">
												<mifos:mifoslabel
												name="product.days" bundle="ProductDefUIResources" /></span></td>
										</tr>
									</table>
									</td>
								</tr>
								<tr class="fontnormal">
									<td width="59%" align="left" valign="top"
										style="border: 1px solid #CECECE;">
									<div id="weekDIV" style="height:40px; width:380px;"><mifos:mifoslabel
										name="product.enterfoll" bundle="ProductDefUIResources" isColonRequired="yes"/>
									<table border="0" cellspacing="0" cellpadding="2">
										<tr class="fontnormal">
											<td colspan="3"><mifos:mifoslabel name="product.recur"
												bundle="ProductDefUIResources" /> <mifos:mifosnumbertext
												styleId="EditLoanProduct.input.weekFrequency"
												property="recurAfter" size="3" maxlength="3" /></td>
											<td><span id="week"> <mifos:mifoslabel
												name="product.week" bundle="ProductDefUIResources" /> </span> <span
												id="month"> <mifos:mifoslabel name="product.month"
												bundle="ProductDefUIResources" /> </span>
												<span id="day"> <mifos:mifoslabel name="product.day"
												bundle="ProductDefUIResources" /> </span></td>
										</tr>
									</table>
									</div>
									</td>
								</tr>
							</table>
                            <script>
                                showMeetingFrequency();
                            </script>
                          </td>
						</tr>
						<!--<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel mandatory="yes"
								name="product.maxinst" bundle="ProductDefUIResources" /> :</td>
							<td valign="top"><mifos:mifosnumbertext
								property="maxNoInstallments" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel mandatory="yes"
								name="product.mininst" bundle="ProductDefUIResources" /> :</td>
							<td valign="top"><mifos:mifosnumbertext
								property="minNoInstallments" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><span class="mandatorytext"></span> <mifos:mifoslabel
								name="product.definst" bundle="ProductDefUIResources"
								mandatory="yes" /> :</td>
							<td valign="top"><mifos:mifosnumbertext
								property="defNoInstallments" /></td>
						</tr>
						-->
						<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr class="fontnormal">
									<td width="30%" align="right"><mifos:mifoslabel
										name="product.calcInstallment" mandatory="yes"
										bundle="ProductDefUIResources" isColonRequired="yes"/></td>
									<td width="70%" valign="top"><html-el:radio
										styleId="EditLoanProduct.input.installmentsSameForAllLoans"
										property="calcInstallmentType" value="1"
										onclick="checkType();" /> 
										<span id="EditLoanProduct.label.installmentsSameForAllLoans">
										<mifos:mifoslabel
										name="product.sameforallinstallment"
										bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp; <html-el:radio
										styleId="EditLoanProduct.input.installmentsByLastLoanAmount"
										property="calcInstallmentType" value="2"
										onclick="checkType();" /> 
										<span id="EditLoanProduct.label.installmentsByLastLoanAmount">
										<mifos:mifoslabel
										name="product.installbylastloanamount"
										bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp; <html-el:radio
										styleId="EditLoanProduct.input.installmentsByLoanCycle"
										property="calcInstallmentType" value="3"
										onclick="checkType();" /> 
										<span id="EditLoanProduct.label.installmentsByLoanCycle">
										<mifos:mifoslabel
										name="product.installbyloancycle"
										bundle="ProductDefUIResources" /></span> &nbsp;&nbsp;&nbsp;</td>
								</tr>
								<tr>
									<td>&nbsp;</td><td>
									<div id="install0" style="display:block;">
										<table width="60%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="33%" class="drawtablehd"><mifos:mifoslabel
													name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="34%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="33%" class="drawtablehd"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments"
													size="10" property="minNoInstallments" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments"
													size="10" property="maxNoInstallments" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments"
													size="10" property="defNoInstallments" /></td>
											</tr>
										</table>
									</div>
									<div id="install1" style="display:none;">
										<table width="60%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.lastloanamount"
													bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments1"
													size="10" property="startInstallmentRange1"
													style="border:0" readonly="true" value ="0"/> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments1"
													size="10" property="endInstallmentRange1"
													onblur="changeInstallmentValue(event, this,this.value,1)" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments1"
													size="10" property="minLoanInstallment1" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments1"
													size="10" property="maxLoanInstallment1" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments1"
													size="10" property="defLoanInstallment1" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments2"
													size="10" property="startInstallmentRange2"
													style="border:0" readonly="true" /> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments2"	
													size="10" property="endInstallmentRange2"
													onblur="changeInstallmentValue(event, this,this.value,2)"/></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments2"
													size="10" property="minLoanInstallment2" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments2"
													size="10" property="maxLoanInstallment2" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments2"
													size="10" property="defLoanInstallment2" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments1"
													size="10" property="startInstallmentRange3"
													style="border:0" readonly="true" /> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments3"
													size="10" property="endInstallmentRange3"
													onblur="changeInstallmentValue(event, this,this.value,3)"/></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments3"
													size="10" property="minLoanInstallment3" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments3"
													size="10" property="maxLoanInstallment3" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments3"
													size="10" property="defLoanInstallment3" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments4"
													size="10" property="startInstallmentRange4"
													style="border:0" readonly="true" /> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments4"
													size="10" property="endInstallmentRange4"
													onblur="changeInstallmentValue(event, this,this.value,4)" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments4"
													size="10" property="minLoanInstallment4" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments4"
													size="10" property="maxLoanInstallment4" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments4"
													size="10" property="defLoanInstallment4" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments5"
													size="10" property="startInstallmentRange5"
													style="border:0" readonly="true" /> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments5"	
													size="10" property="endInstallmentRange5"
													onblur="changeInstallmentValue(event, this,this.value,5)"/></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments5"
													size="10" property="minLoanInstallment5" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments5"
													size="10" property="maxLoanInstallment5" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments5"
													size="10" property="defLoanInstallment5" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.startRangeInstallments6"
													size="10" property="startInstallmentRange6"
													style="border:0" readonly="true" /> - <mifos:mifosnumbertext
													styleId="EditLoanProduct.input.endRangeInstallments6"	
													size="10" property="endInstallmentRange6"/></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minInstallments6"
													size="10" property="minLoanInstallment6" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxInstallments6"
													size="10" property="maxLoanInstallment6" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultInstallments6"
													size="10" property="defLoanInstallment6" /></td>
											</tr>
										</table>
									</div>
									<div id="install2" style="display:none;">
										<table width="60%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.loancycleno" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
												    name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">0</td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minCycleInstallments1"
													size="10" property="minCycleInstallment1" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxCycleInstallments1"
													size="10" property="maxCycleInstallment1" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultCycleInstallments1"
													size="10" property="defCycleInstallment1" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">1</td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.minCycleInstallments2"
													size="10" property="minCycleInstallment2" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.maxCycleInstallments2"
													size="10" property="maxCycleInstallment2" /></td>
												<td class="drawtablerow"><mifos:mifosnumbertext
													styleId="EditLoanProduct.input.defaultCycleInstallments2"
													size="10" property="defCycleInstallment2" /></td>
												</tr>
												<tr>
													<td class="drawtablerow">2</td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.minCycleInstallments3"
														size="10" property="minCycleInstallment3" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.maxCycleInstallments3"
														size="10" property="maxCycleInstallment3" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.defaultCycleInstallments3"
														size="10" property="defCycleInstallment3" /></td>
												</tr>
												<tr>
													<td class="drawtablerow">3</td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.minCycleInstallments4"
														size="10" property="minCycleInstallment4" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.maxCycleInstallments4"
														size="10" property="maxCycleInstallment4" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.defaultCycleInstallments4"
														size="10" property="defCycleInstallment4" /></td>
												</tr>
												<tr>
													<td class="drawtablerow">4</td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.minCycleInstallments5"
														size="10" property="minCycleInstallment5" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
													    styleId="EditLoanProduct.input.maxCycleInstallments5"
														size="10" property="maxCycleInstallment5" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.defaultCycleInstallments5"
														size="10" property="defCycleInstallment5" /></td>
												</tr>
												<tr>
													<td class="drawtablerow">&gt;4</td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.minCycleInstallments6"
														size="10" property="minCycleInstallment6" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.maxCycleInstallments6"
														size="10" property="maxCycleInstallment6" /></td>
													<td class="drawtablerow"><mifos:mifosnumbertext
														styleId="EditLoanProduct.input.defaultCycleInstallments6"
													    size="10" property="defCycleInstallment6" /></td>
												</tr>
											</table>
									</div>
                            </td>
                        </tr>
						<tr class="fontnormal">
							<!-- 
							<td align="right"> 
								<fmt:message key="product.deductedatdis">
								<fmt:param><mifos:mifoslabel
								name="${ConfigurationConstants.SERVICE_CHARGE}"
								bundle="ProductDefUIResources" /></fmt:param>
								</fmt:message>:</td>
								-->
							<td valign="top"><html-el:checkbox styleId="EditLoanProduct.input.interestDeductedAtDisbursement"
								property="intDedDisbursementFlag" value="1" style="visibility:hidden"
								onclick="fnIntDesbr();" /></td>
								
						</tr>
						<tr class="fontnormal">
						<!-- 
							<td align="right"><mifos:mifoslabel
								name="product.prinlastinst" bundle="ProductDefUIResources" isColonRequired="yes"/>
							</td>
							-->
							<td valign="top"><html-el:checkbox styleId="EditLoanProduct.input.principalDueOnLastInstallment"
								property="prinDueLastInstFlag" value="1" style="visibility:hidden" /></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><mifos:mifoslabel
								name="product.gracepertype" bundle="ProductDefUIResources" isColonRequired="yes"/>
							</td>
							<td valign="top"><mifos:select property="gracePeriodType"
								onchange="fnGracePeriod();">
								<c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanGracePeriodTypeList')}"
									var="graceType">
									<html-el:option value="${graceType.id}">${graceType.name}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
						<tr class="fontnormal">
							<td align="right"><span id="EditLoanProduct.label.gracePeriod"><mifos:mifoslabel
								name="product.graceperdur" bundle="ProductDefUIResources" /></span> :
							</td>
							<td valign="top"><mifos:mifosnumbertext
								styleId="EditLoanProduct.input.gracePeriod"
								property="gracePeriodDuration" /> <mifos:mifoslabel
								name="product.installments" bundle="ProductDefUIResources" /></td>
						</tr>
					</table>
					<br>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="product.fees" bundle="ProductDefUIResources" /> <br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right" valign="top"><mifos:mifoslabel
								name="product.attachfeestypes" bundle="ProductDefUIResources" isColonRequired="yes"/>
							</td>
							<td width="70%" valign="top">
							<table width="80%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td class="fontnormal"><mifos:mifoslabel
										name="product.clickfeetypes" bundle="ProductDefUIResources" />
									</td>
								</tr>
								<tr>
									<td><img src="pages/framework/images/trans.gif" width="1"
										height="1"></td>
								</tr>
							</table>
							<c:set var="LoanFeesList" scope="request"
								value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanFeesList')}" />
							<c:set var="selectedFeeList" scope="request"
								value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfeeselectedlist')}" />
							<mifos:MifosSelect property="prdOfferinFees" input="LoanFeesList"
								output="selectedFeeList" property1="feeId" property2="feeName"
								multiple="true">
							</mifos:MifosSelect></td>
						</tr>
					</table>
                    <table width="93%" border="0" cellpadding="3" cellspacing="0">
                        <tr>
                            <td colspan="2" class="fontnormalbold"><mifos:mifoslabel
                                name="product.penalties" bundle="ProductDefUIResources" /> <br>
                            <br>
                            </td>
                        </tr>
                        <tr class="fontnormal">
                            <td width="30%" align="right" valign="top"><mifos:mifoslabel
                                name="product.attachpenaltiestypes" bundle="ProductDefUIResources" isColonRequired="yes"/>
                            </td>
                            <td width="70%" valign="top">
                            <table width="80%" border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td class="fontnormal"><mifos:mifoslabel
                                        name="product.clickpenaltytypes" bundle="ProductDefUIResources" />
                                    </td>
                                </tr>
                                <tr>
                                    <td><img src="pages/framework/images/trans.gif" width="1"
                                        height="1"></td>
                                </tr>
                            </table>
                            <c:set var="LoanPenaltiesList" scope="request"
                                value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanPenaltiesList')}" />
                            <c:set var="selectedPenaltyList" scope="request"
                                value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdpenaltyselectedlist')}" />
                            <mifos:MifosSelect property="prdOfferinPenalties" input="LoanPenaltiesList"
                                output="selectedPenaltyList" property1="penaltyId" property2="penaltyName"
                                multiple="true">
                            </mifos:MifosSelect></td>
                        </tr>
                    </table>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="product.accounting" bundle="ProductDefUIResources" /> <br>
							<br>
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="30%" align="right" valign="top"><mifos:mifoslabel
								name="product.srcfunds" bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td width="70%" valign="top">
							<table width="80%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td class="fontnormal"><mifos:mifoslabel
										name="product.clickfunds" bundle="ProductDefUIResources" /></td>
								</tr>
								<tr>
									<td><img src="pages/framework/images/trans.gif" width="1"
										height="1"></td>
								</tr>
							</table>
							<c:set var="SrcFundsList" scope="request"
								value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'SrcFundsList')}" />
							<c:set var="selectedFundList" scope="request"
								value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfundselectedlist')}" />
							<mifos:MifosSelect property="loanOfferingFunds"
								input="SrcFundsList" output="selectedFundList"
								property1="fundId" property2="fundName" multiple="true">
							</mifos:MifosSelect></td>
						</tr>
						<tr class="fontnormal">
							<td align="right" valign="top" style="padding-top:8px;"><mifos:mifoslabel
								mandatory="yes" name="product.productglcode"
								bundle="ProductDefUIResources" isColonRequired="yes"/></td>
							<td valign="top">
							<table width="80%" border="0" cellspacing="0" cellpadding="2">
								<tr class="fontnormal">
									<td width="15%"><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" isColonRequired="yes"/></td>
									<td width="85%"><mifos:select property="interestGLCode"
										style="width:136px;" disabled="true">
										<c:forEach
											items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'interestGLCodes')}"
											var="glCodes">
											<c:choose>
												<c:when test="${GlNamesMode == 1}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode} - ${glCodes.associatedCOA.accountName}</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 2}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.associatedCOA.accountName} (${glCodes.glcode})</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 3}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.associatedCOA.accountName}</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 4}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode}</html-el:option>
												</c:when>
											</c:choose>
										</c:forEach>
									</mifos:select></td>
								</tr>
								<tr class="fontnormal">
									<td><mifos:mifoslabel name="product.principal"
										bundle="ProductDefUIResources" isColonRequired="yes"/></td>
									<td><mifos:select property="principalGLCode"
										disabled="true">
										<c:forEach
											items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'principalGLCodes')}"
											var="glCodes">
											<c:choose>
												<c:when test="${GlNamesMode == 1}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode} - ${glCodes.associatedCOA.accountName}</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 2}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.associatedCOA.accountName} (${glCodes.glcode})</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 3}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.associatedCOA.accountName}</html-el:option>
												</c:when>
												<c:when test="${GlNamesMode == 4}">
													<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode}</html-el:option>
												</c:when>
											</c:choose></c:forEach>
									</mifos:select></td>
								</tr>
							</table>
							</td>
						</tr>
					</table>
					<table width="93%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class="blueline">&nbsp;</td>
						</tr>
					</table>
					<br>
					<br>
					<table width="93%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center"><html-el:submit styleId="EditLoanProduct.button.preview" styleClass="buttn"
								onclick="transferData(this.form.loanOfferingQGs);transferData(this.form.loanOfferingFunds);transferData(this.form.prdOfferinFees);transferData(this.form.prdOfferinPenalties);">
								<mifos:mifoslabel name="product.preview"
									bundle="ProductDefUIResources" />
							</html-el:submit> &nbsp; <html-el:button styleId="EditLoanProduct.button.cancel" property="cancel"
								styleClass="cancelbuttn"
								onclick="javascript:fnCancel(this.form)">
								<mifos:mifoslabel name="product.cancel"
									bundle="ProductDefUIResources" />
							</html-el:button></td>
						</tr>
					</table>
					<br>
					</td>
				</tr>
			</table>
						<script type="text/javascript">
						    showCashFlowInputs();
							fnIntDesbr();
							fnGracePeriod();
							checkRow();
						    checkType();
                            showVariableInstallmentInputs();
						</script>
			
			<html-el:hidden property="method" value="editPreview" />
			<html-el:hidden property="prdOfferName" value="${prdOfferName}" />
			<html-el:hidden property="currentFlowKey"
				value="${requestScope.currentFlowKey}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
