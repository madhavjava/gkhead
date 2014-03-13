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
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<fmt:setLocale value='${sessionScope["org.apache.struts.action.LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.ProductDefinitionResources"/>

<tiles:insert definition=".create">
	<tiles:put name="body" type="string">
        <span id="page.id" title="CreateLoanProductPreview"></span>
		<script language="javascript">
		<!--
			function fnCancel(form) {
				form.method.value="cancelCreate";
				form.action="loanproductaction.do";
				form.submit();
			}
			function fnEdit(form) {
				form.method.value="previous";
				form.action="loanproductaction.do";
				form.submit();
			}
			function func_disableSubmitBtn(){
				document.getElementById("submitBut").disabled=true;
			}
		//-->
		</script>

		<html-el:form action="/loanproductaction"
			onsubmit="func_disableSubmitBtn();">
			<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'GlNamesMode')}" var="GlNamesMode"/>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td height="350" align="left" valign="top" bgcolor="#FFFFFF">
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0">
						<tr>
							<td align="center" class="heading">&nbsp;</td>
						</tr>
					</table>
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0">
						<tr>
							<td class="bluetablehead">
							<table width="100%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td width="27%">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td><img src="pages/framework/images/timeline/tick.gif"
												width="17" height="17"></td>
											<td class="timelineboldgray">
												<fmt:message key="product.loanProductInfo">
												<fmt:param><mifos:mifoslabel
												name="${ConfigurationConstants.LOAN}"
												bundle="ProductDefUIResources" /></fmt:param>
												</fmt:message>
											</td>
										</tr>
									</table>
									</td>
									<td width="73%" align="right">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td><img
												src="pages/framework/images/timeline/bigarrow.gif"
												width="17" height="17"></td>
											<td class="timelineboldorange"><mifos:mifoslabel
												name="product.review" bundle="ProductDefUIResources" /></td>
										</tr>
									</table>
									</td>
								</tr>
							</table>
							</td>
						</tr>
					</table>
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0" class="bluetableborder">
						<tr>
							<td align="left" valign="top" class="paddingleftCreates">
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
                                <tr>
									<td class="headingorange"><span class="heading" id="createLoanProductPreview.heading"> 
										<fmt:message key="product.addNewLoanProduct">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message> - </span> <mifos:mifoslabel
										name="product.review" bundle="ProductDefUIResources" /></td>
								</tr>
								<tr>
									<td class="fontnormal"><mifos:mifoslabel
										name="product.previewfields" bundle="ProductDefUIResources" />
									<mifos:mifoslabel name="product.clicksubmit"
										bundle="ProductDefUIResources" /> <mifos:mifoslabel
										name="product.clickcancinfo" bundle="ProductDefUIResources" />
									</td>
								</tr>
							</table>
							<br>
							<font class="fontnormalRedBold"><html-el:errors
								bundle="ProductDefUIResources" /></font>
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td width="100%" height="23" class="fontnormalbold">
										<fmt:message key="product.loanProductDetails">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message></td>
								</tr>
								<tr>
									<td height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.prodinstname" bundle="ProductDefUIResources" 
										isColonRequired="yes"/>
									<span class="fontnormal"><c:out
										value="${sessionScope.loanproductactionform.prdOfferingName}" />
									</span> <br>
									<mifos:mifoslabel name="product.shortname"
										bundle="ProductDefUIResources" isColonRequired="yes" /> <span class="fontnormal"><c:out
										value="${sessionScope.loanproductactionform.prdOfferingShortName}" />
									</span> <br>
									<br>
									<mifos:mifoslabel name="product.desc"
										bundle="ProductDefUIResources" /> <br>
									<span class="fontnormal"> <c:if
										test="${!empty sessionScope.loanproductactionform.description}">
										<c:out
											value="${sessionScope.loanproductactionform.description}" />
										<br>
									</c:if> </span> <br>
									<mifos:mifoslabel name="product.prodcat"
										bundle="ProductDefUIResources" isColonRequired="yes" /> <span class="fontnormal">
									<c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanProductCategoryList')}"
										var="category">
										<c:if
											test="${category.productCategoryID eq sessionScope.loanproductactionform.prdCategory}">
											<c:out value="${category.productCategoryName}" />
										</c:if>
									</c:forEach> </span> <br>
									<mifos:mifoslabel name="product.startdate"
										bundle="ProductDefUIResources" isColonRequired="yes" /> <span class="fontnormal">
									<c:out value="${sessionScope.loanproductactionform.startDate}" />
									</span> <br>
									<mifos:mifoslabel name="product.enddate"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal"><c:out
										value="${sessionScope.loanproductactionform.endDate}" /> </span> <br>
									<mifos:mifoslabel name="product.applfor"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal"><c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanApplForList')}"
										var="ApplForList">
										<c:if
											test="${ApplForList.id eq sessionScope.loanproductactionform.prdApplicableMaster}">
											<c:out value="${ApplForList.name}" />

										</c:if>
									</c:forEach> </span> <br>
                                    <c:if test='${sessionScope.isMultiCurrencyEnabled}'>
                                    <span class="fontnormalbold">
                                    <mifos:mifoslabel name="product.currency"
                                        bundle="ProductDefUIResources" isColonRequired="yes" />
                                    </span> <span class="fontnormal">
                                    <c:out value="${sessionScope.currencyCode}" /></span><br>
                                    </c:if>
									<fmt:message key="product.inclInLoanCycleCounter">
									<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
									</fmt:message>:
									<span class="fontnormal"> <c:choose>
										<c:when
											test="${sessionScope.loanproductactionform.loanCounter==1}">
											<mifos:mifoslabel name="product.yes"
												bundle="ProductDefUIResources" />
										</c:when>
										<c:otherwise>
											<mifos:mifoslabel name="product.no"
												bundle="ProductDefUIResources" />
										</c:otherwise>
									</c:choose> </span> <br>
									<mifos:mifoslabel	name="product.include.interest.waiver" bundle="ProductDefUIResources" />:
                                    <span class="fontnormal">
                                    <c:choose>
										<c:when
											test="${sessionScope.loanproductactionform.waiverInterest==1}">
											<mifos:mifoslabel name="product.yes"
												bundle="ProductDefUIResources" />
										</c:when>
										<c:otherwise>
											<mifos:mifoslabel name="product.no"
												bundle="ProductDefUIResources" />
										</c:otherwise>
									</c:choose> </span> <br>
									<!--<mifos:mifoslabel name="product.max" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="${ConfigurationConstants.LOAN}" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="product.amount" bundle="ProductDefUIResources" />
                                                : <span class="fontnormal"> <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanAmount}" /></span>
												<br>
												<mifos:mifoslabel name="product.min" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="${ConfigurationConstants.LOAN}" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="product.amount" bundle="ProductDefUIResources" />
												: <span class="fontnormal"> <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanAmount}" /></span>
												<br>
												<mifos:mifoslabel name="product.default" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="${ConfigurationConstants.LOAN}" bundle="ProductDefUIResources" />
												<mifos:mifoslabel name="product.amount" bundle="ProductDefUIResources" />
												: <span class="fontnormal"> <fmt:formatNumber value="${sessionScope.loanproductactionform.defaultLoanAmount}" /></span>
											--> 
                                        <c:if test="${sessionScope.loanproductactionform.loanAmtCalcType=='2'}">
										<br>
										<table width="41.5%" border="0" cellpadding="3" cellspacing="0">
											<tr>
                                                <td>
                                                <span class="fontnormalbold">
                                            <mifos:mifoslabel name="product.calcloanamount"
												bundle="ProductDefUIResources" isColonRequired="yes" />
                                                </span>
											<span class="fontnormal"> <mifos:mifoslabel
												name="product.bylastloanamount"
												bundle="ProductDefUIResources" /></span>
                                                </td>
                                                </tr>
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.lastloanamount"
													bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt1}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt1}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt1}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt1}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt1}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt2}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt2}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt2}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt2}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt2}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt3}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt3}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt3}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt3}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt3}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt4}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt4}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt4}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt4}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt4}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt5}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt5}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt5}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt5}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt5}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startRangeLoanAmt6}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endRangeLoanAmt6}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMinLoanAmt6}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanMaxLoanAmt6}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.lastLoanDefaultLoanAmt6}" type="number" /> </td>
											</tr>
										</table>
									</c:if> <%-- by loan cycle --%> <c:if
										test="${sessionScope.loanproductactionform.loanAmtCalcType=='3'}">
										<br />
										<mifos:mifoslabel name="product.calcloanamount"
											bundle="ProductDefUIResources" />:<span class="fontnormal">
										<mifos:mifoslabel name="product.byloancycle"
											bundle="ProductDefUIResources" /></span>
										<br />

										<table width="41.5%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="15%" class="drawtablehd"><mifos:mifoslabel
													name="product.loancycleno" bundle="ProductDefUIResources" />
												</td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="0" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt1}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt1}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt1}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="1" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt2}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt2}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt2}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="2" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt3}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt3}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt3}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="3" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt4}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt4}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt4}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="4" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt5}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt5}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt5}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%">&gt;<fmt:formatNumber value="4" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMinLoanAmt6}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanMaxLoanAmt6}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.cycleLoanDefaultLoanAmt6}" type="number" /> </td>
											</tr>
										</table>

									</c:if> <c:if
										test="${sessionScope.loanproductactionform.loanAmtCalcType=='1'}">

										<mifos:mifoslabel name="product.calcloanamount"
											bundle="ProductDefUIResources" />:<span class="fontnormal">
										<mifos:mifoslabel name="product.sameforallloans"
											bundle="ProductDefUIResources" /></span>
										<br>

										<table width="41.5%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="20%" class="drawtablehd"><mifos:mifoslabel
													name="product.min" bundle="ProductDefUIResources" /> <mifos:mifoslabel
													name="product.amount" bundle="ProductDefUIResources" /></td>
												<td width="20%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.max" bundle="ProductDefUIResources" /> <mifos:mifoslabel
													name="product.amount" bundle="ProductDefUIResources" /></td>
												<td width="20%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.default" bundle="ProductDefUIResources" /> <mifos:mifoslabel
													name="product.amount" bundle="ProductDefUIResources" /></td>
                                            </tr>
                                            <tr>
												<td class="fontnormal" width="20%">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanAmount}" type="number"/> </td>
                                                <td class="fontnormal" width="20%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanAmount}" type="number"/> </td>
												<td class="fontnormal" width="20%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defaultLoanAmount}" type="number"/> </td>
											</tr>
										</table>
									</c:if></td>
								</tr>
							</table>
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
                                <tr>
									<td height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.assocedquestiongroups" bundle="ProductDefUIResources" /> : <span
										class="fontnormal"><br>
									<c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'SelectedQGList')}"
										var="questionGroup">
										<c:out value="${questionGroup.title}" />
										<br>
									</c:forEach></span> <br>
                                </tr>
							</table>
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td width="100%" height="23" class="fontnormalbold">
										<fmt:message key="product.productRate">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message></td>
								</tr>
								<tr>
									<td height="23" class="fontnormalbold">
										<fmt:message key="product.rateType">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message>: <span
										class="fontnormal"> <c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'InterestTypesList')}"
										var="InterestTypes">
										<c:if
											test="${InterestTypes.id eq sessionScope.loanproductactionform.interestTypes}">
											<c:out value="${InterestTypes.name}" />
										</c:if>
									</c:forEach> </span> <br>
									<fmt:message key="product.minRate">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message>: <span
										class="fontnormal"><fmt:formatNumber
										value="${sessionScope.loanproductactionform.minInterestRate}" />
									<mifos:mifoslabel name="product.perc"
										bundle="ProductDefUIResources" /></span> <br>
                                    <fmt:message key="product.maxRate">
                                        <fmt:param><mifos:mifoslabel
                                        name="${ConfigurationConstants.SERVICE_CHARGE}"
                                        bundle="ProductDefUIResources" /></fmt:param>
                                        </fmt:message>: <span
                                        class="fontnormal"><fmt:formatNumber
                                        value="${sessionScope.loanproductactionform.maxInterestRate}" />
                                    <mifos:mifoslabel name="product.perc"
                                        bundle="ProductDefUIResources" /></span> <br>
									<fmt:message key="product.defaultRate">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message> <span
										class="fontnormal"><fmt:formatNumber
										value="${sessionScope.loanproductactionform.defInterestRate}" />
									<mifos:mifoslabel name="product.perc"
										bundle="ProductDefUIResources" /></span> <br>
									</td>
								</tr>
							</table>

							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td width="100%" height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.repaysch" bundle="ProductDefUIResources" /></td>
								</tr>
								<tr>
								    <td height="23" class="fontnormalbold">
                                        <mifos:mifoslabel name="product.canConfigureVariableInstallments" bundle="ProductDefUIResources" isColonRequired="yes" />
                                        <span class="fontnormal">
                                            <c:choose>
                                                <c:when test="${sessionScope.loanproductactionform.canConfigureVariableInstallments == '1'}">
                                                    <mifos:mifoslabel name="product.yes" bundle="ProductDefUIResources" />
                                                </c:when>
                                                <c:otherwise>
                                                    <mifos:mifoslabel name="product.no" bundle="ProductDefUIResources" />
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
								        <br/>
								        <mifos:mifoslabel name="product.fixedRepaymentSchedule" bundle="ProductDefUIResources" isColonRequired="yes" />
								        <span class="fontnormal">
                                            <c:choose>
                                                <c:when test="${sessionScope.loanproductactionform.isFixedRepaymentSchedule == '1'}">
                                                    <mifos:mifoslabel name="product.yes" bundle="ProductDefUIResources" />
                                                </c:when>
                                                <c:otherwise>
                                                    <mifos:mifoslabel name="product.no" bundle="ProductDefUIResources" />
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <br/>
                                        <c:if test="${sessionScope.loanproductactionform.canConfigureVariableInstallments=='1'}">

                                                    <mifos:mifoslabel name="product.minimumGapBetweenInstallments" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                    <span class="fontnormal">
                                                        <c:choose>
                                                            <c:when test="${empty sessionScope.loanproductactionform.minimumGapBetweenInstallments}">
                                                                <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                 <fmt:formatNumber value="${sessionScope.loanproductactionform.minimumGapBetweenInstallments}" />
                                                                 <span id="days"> <mifos:mifoslabel name="product.days" bundle="ProductDefUIResources" /> </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <br/>
                                                    <mifos:mifoslabel name="product.maximumGapBetweenInstallments" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                    <span class="fontnormal">
                                                        <c:choose>
                                                            <c:when test="${empty sessionScope.loanproductactionform.maximumGapBetweenInstallments}">
                                                                <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <fmt:formatNumber value="${sessionScope.loanproductactionform.maximumGapBetweenInstallments}" />
                                                                <span id="days"> <mifos:mifoslabel name="product.days" bundle="ProductDefUIResources" /> </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <br/>
                                                    <mifos:mifoslabel name="product.minimumInstallmentAmount" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                    <span class="fontnormal">
                                                        <c:choose>
                                                            <c:when test="${empty sessionScope.loanproductactionform.minimumInstallmentAmount}">
                                                                <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <fmt:formatNumber value="${sessionScope.loanproductactionform.minimumInstallmentAmount}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <br/>
                                        </c:if>
                                

                                        <mifos:mifoslabel name="product.cashFlowValidation" bundle="ProductDefUIResources" isColonRequired="yes" />
                                        <span class="fontnormal">
                                            <c:choose>
                                                <c:when test="${sessionScope.loanproductactionform.cashFlowValidation}">
                                                    <mifos:mifoslabel name="product.yes" bundle="ProductDefUIResources" />
                                                </c:when>
                                                <c:otherwise>
                                                    <mifos:mifoslabel name="product.no" bundle="ProductDefUIResources" />
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
								        <br/>

                                        <c:if test="${sessionScope.loanproductactionform.cashFlowValidation}">

                                            <mifos:mifoslabel name="product.cashFlowThreshold" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                <span class="fontnormal">
                                                    <c:choose>
                                                        <c:when test="${empty sessionScope.loanproductactionform.cashFlowThreshold}">
                                                            <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${sessionScope.loanproductactionform.cashFlowThreshold}" />
                                                                <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            <br/>
                                            <mifos:mifoslabel name="product.indebtednessRatio" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                <span class="fontnormal">
                                                    <c:choose>
                                                        <c:when test="${empty sessionScope.loanproductactionform.indebtednessRatio}">
                                                            <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${sessionScope.loanproductactionform.indebtednessRatio}" />
                                                                <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            <br/>
                                            <mifos:mifoslabel name="product.repaymentCapacity" bundle="ProductDefUIResources" isColonRequired="yes" />
                                                <span class="fontnormal">
                                                    <c:choose>
                                                        <c:when test="${empty sessionScope.loanproductactionform.repaymentCapacity}">
                                                            <mifos:mifoslabel name="product.notApplicable" bundle="ProductDefUIResources" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${sessionScope.loanproductactionform.repaymentCapacity}" />
                                                                <mifos:mifoslabel name="product.perc" bundle="ProductDefUIResources" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            <br/>
                                        </c:if>

								    <mifos:mifoslabel
										name="product.freqofinst" bundle="ProductDefUIResources" isColonRequired="yes" />
									<span class="fontnormal"> <c:out
										value="${sessionScope.loanproductactionform.recurAfter}" /> <c:if
										test="${sessionScope.loanproductactionform.freqOfInstallments eq 1}">
										<mifos:mifoslabel name="product.week"
											bundle="ProductDefUIResources" />
									</c:if> <c:if
										test="${sessionScope.loanproductactionform.freqOfInstallments eq 2}">
										<mifos:mifoslabel name="product.month"
											bundle="ProductDefUIResources" />
									</c:if> <c:if
										test="${sessionScope.loanproductactionform.freqOfInstallments eq 3}">
										<mifos:mifoslabel name="product.day"
											bundle="ProductDefUIResources" />
									</c:if> </span> <br>
									<!--
									        <mifos:mifoslabel name="product.maxinst" bundle="ProductDefUIResources" />
												: <span class="fontnormal"><fmt:formatNumber value="${sessionScope.loanproductactionform.maxNoInstallments}" /></span>
												<br>
												<mifos:mifoslabel name="product.mininst" bundle="ProductDefUIResources" />
												: <span class="fontnormal"><fmt:formatNumber value="${sessionScope.loanproductactionform.minNoInstallments}" /></span>
												<br>
												<mifos:mifoslabel name="product.definst" bundle="ProductDefUIResources" />
												: <span class="fontnormal"><fmt:formatNumber value="${sessionScope.loanproductactionform.defNoInstallments}" /></span>
												 <br>
								     -->

										<c:if test="${sessionScope.loanproductactionform.calcInstallmentType=='2'}">
										<mifos:mifoslabel name="product.calcInstallment"
											bundle="ProductDefUIResources" isColonRequired="yes" /> <span class="fontnormal"><mifos:mifoslabel
											name="product.installbylastloanamount"
											bundle="ProductDefUIResources" /></span>
										<br>


										<table width="41.5%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.lastloanamount"
													bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange1}" type="number"/> </td>
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange1}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment1}" type="number" /></td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment1}" type="number" /></td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment1}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange2}" type="number" />
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange2}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment2}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment2}" type="number" />  
												</td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment2}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange3}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange3}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment3}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment3}" type="number" /> 
												</td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment3}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange4}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange4}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment4}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment4}" type="number" /> 
												</td>
                                                <td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment4}" type="number" /> </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange5}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange5}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment5}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment5}" type="number" /> 
												</td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment5}" type="number" /> 
                                                </td>
											</tr>
											<tr>
												<td class="fontnormal">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.startInstallmentRange6}" type="number" /> 
												- <fmt:formatNumber value="${sessionScope.loanproductactionform.endInstallmentRange6}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minLoanInstallment6}" type="number" /> </td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxLoanInstallment6}" type="number" /> 
												</td>
												<td class="fontnormal" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defLoanInstallment6}" type="number" /> </td>
											</tr>

										</table>
									</c:if> <c:if
										test="${sessionScope.loanproductactionform.calcInstallmentType=='3'}">
										<mifos:mifoslabel name="product.calcInstallment"
											bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal"><mifos:mifoslabel
											name="product.installbyloancycle"
											bundle="ProductDefUIResources" /></span>
										<br>
										<table width="41.5%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="15%" class="drawtablehd"><mifos:mifoslabel
													name="product.loancycleno" bundle="ProductDefUIResources" />
												</td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="30%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="0" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment1}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment1}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment1}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="1" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment2}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment2}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment2}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="2" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment3}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment3}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment3}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="3" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment4}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment4}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment4}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%"><fmt:formatNumber value="4" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment5}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment5}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment5}" type="number" /> </td>
											</tr>

											<tr>
												<td class="fontnormal" width="10%">&gt;<fmt:formatNumber value="4" /></td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.minCycleInstallment6}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxCycleInstallment6}" type="number" /> </td>
												<td class="fontnormal" width="30%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defCycleInstallment6}" type="number" /> </td>
											</tr>
										</table>
									</c:if> <c:if
										test="${sessionScope.loanproductactionform.calcInstallmentType=='1'}">
										<mifos:mifoslabel name="product.calcInstallment"
											bundle="ProductDefUIResources" isColonRequired="yes" /> <span class="fontnormal"><mifos:mifoslabel
											name="product.sameforallinstallment"
											bundle="ProductDefUIResources" /></span>
										<br>
										<table width="41.5%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="20%" class="drawtablehd"><mifos:mifoslabel
													name="product.mininst" bundle="ProductDefUIResources" /></td>
												<td width="20%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.maxinst" bundle="ProductDefUIResources" /></td>
												<td width="20%" class="drawtablehd" align="right"><mifos:mifoslabel
													name="product.definst" bundle="ProductDefUIResources" /></td>
											</tr>

											<tr>
												<td class="fontnormal" width="20%"><fmt:formatNumber
													value="${sessionScope.loanproductactionform.minNoInstallments}" /></td>
												<td class="fontnormal" width="20%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.maxNoInstallments}" type="number" /> </td>
												<td class="fontnormal" width="20%" align="right">
                                                    <fmt:formatNumber value="${sessionScope.loanproductactionform.defNoInstallments}" type="number" /> </td>
											</tr>

										</table>
									</c:if> <br>
									<mifos:mifoslabel name="product.gracepertype"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal">
									<c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanGracePeriodTypeList')}"
										var="LoanGracePeriodType">
										<c:if
											test="${LoanGracePeriodType.id eq sessionScope.loanproductactionform.gracePeriodType}">
											<c:out value="${LoanGracePeriodType.name}" />
										</c:if>
									</c:forEach> </span> <br>
									<mifos:mifoslabel name="product.graceperdur"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal"><fmt:formatNumber
										value="${sessionScope.loanproductactionform.gracePeriodDuration}" />
									<c:if
										test="${not empty sessionScope.loanproductactionform.gracePeriodDuration }">
										<mifos:mifoslabel name="product.installments"
											bundle="ProductDefUIResources" />
									</c:if> </span> <br>
									<!--  
									<fmt:message key="product.deductedatdis">
										<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" /></fmt:param>
									</fmt:message>:
									<span class="fontnormal"> <c:choose>
										<c:when
											test="${sessionScope.loanproductactionform.intDedDisbursementFlag==1}">
											<mifos:mifoslabel name="product.yes"
												bundle="ProductDefUIResources" />
										</c:when>
										<c:otherwise>
											<mifos:mifoslabel name="product.no"
												bundle="ProductDefUIResources" />
										</c:otherwise>
									</c:choose> </span> <br>
									<mifos:mifoslabel name="product.prinlastinst"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal">
									<c:choose>
										<c:when
											test="${sessionScope.loanproductactionform.prinDueLastInstFlag==1}">
											<mifos:mifoslabel name="product.yes"
												bundle="ProductDefUIResources" />
										</c:when>
										<c:otherwise>
											<mifos:mifoslabel name="product.no"
												bundle="ProductDefUIResources" />
										</c:otherwise>
									</c:choose> </span>
									-->
									</td>
								</tr>
							</table>

							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td width="100%" height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.fees" bundle="ProductDefUIResources" /> &amp; 
                                        <mifos:mifoslabel name="product.penalties" bundle="ProductDefUIResources" /></td>
								</tr>
								<tr>
									<td height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.feestypes" bundle="ProductDefUIResources" isColonRequired="yes"/>
									<span class="fontnormal"> <br>
									<c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfeeselectedlist')}"
										var="prdOfferingFees">
										<c:out value="${prdOfferingFees.feeName}" />
										<br>
									</c:forEach></span> <br>
									</td>
								</tr>
                                <tr>
                                    <td height="23" class="fontnormalbold"><mifos:mifoslabel
                                        name="product.penaltiestypes" bundle="ProductDefUIResources" isColonRequired="yes"/>
                                    <span class="fontnormal"> <br>
                                    <c:forEach
                                        items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdpenaltyselectedlist')}"
                                        var="prdOfferingPenalties">
                                        <c:out value="${prdOfferingPenalties.penaltyName}" />
                                        <br>
                                    </c:forEach></span> <br>
                                    </td>
                                </tr>
                            </table>
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td width="100%" height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.accounting" bundle="ProductDefUIResources" /></td>
								</tr>
								<tr>
									<td height="23" class="fontnormalbold"><mifos:mifoslabel
										name="product.srcfunds" bundle="ProductDefUIResources" /> : <span
										class="fontnormal"><br>
									<c:forEach
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfundselectedlist')}"
										var="loanOffeingFund">
										<c:out value="${loanOffeingFund.fundName}" />
										<br>
									</c:forEach></span> <br>
									<mifos:mifoslabel name="product.productglcode"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <br>
									<mifos:mifoslabel
										name="${ConfigurationConstants.SERVICE_CHARGE}"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal">
									<c:forEach var="glCode"
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'interestGLCodes')}">
										<c:if
											test="${glCode.glcodeId == sessionScope.loanproductactionform.interestGLCode}">
											<c:choose>
												<c:when test="${GlNamesMode == 1}">
												<c:out value="${glCode.glcode} - ${glCode.associatedCOA.accountName}" />
												</c:when>
												<c:when test="${GlNamesMode == 2}">
												<c:out value="${glCode.associatedCOA.accountName} (${glCode.glcode})" />
												</c:when>
												<c:when test="${GlNamesMode == 3}">
												<c:out value="${glCode.associatedCOA.accountName}" />
												</c:when>
												<c:when test="${GlNamesMode == 4}">
												<c:out value="${glCode.glcode}" />
												</c:when>
											</c:choose>
										</c:if>
									</c:forEach></span> <br>
									<mifos:mifoslabel name="product.principal"
										bundle="ProductDefUIResources" isColonRequired="yes"/> <span class="fontnormal">
									<c:forEach var="glCode"
										items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'principalGLCodes')}">
										<c:if
											test="${glCode.glcodeId == sessionScope.loanproductactionform.principalGLCode}">
											<c:choose>
												<c:when test="${GlNamesMode == 1}">
												<c:out value="${glCode.glcode} - ${glCode.associatedCOA.accountName}" />
												</c:when>
												<c:when test="${GlNamesMode == 2}">
												<c:out value="${glCode.associatedCOA.accountName} (${glCode.glcode})" />
												</c:when>
												<c:when test="${GlNamesMode == 3}">
												<c:out value="${glCode.associatedCOA.accountName}" />
												</c:when>
												<c:when test="${GlNamesMode == 4}">
												<c:out value="${glCode.glcode}" />
												</c:when>
											</c:choose>
										</c:if>
									</c:forEach></span></td>
								</tr>
							</table>
							<br>
							<table width="93%" border="0" cellpadding="0" cellspacing="0">


								<tr>
									<td class="blueline"><html-el:button property="edit"
										styleId="createLoanProductPreview.button.editLoanInfo"
										styleClass="insidebuttn" onclick="fnEdit(this.form)">
										<fmt:message key="product.editLoanInfo">
											<fmt:param><mifos:mifoslabel
											name="${ConfigurationConstants.LOAN}"
											bundle="ProductDefUIResources" /></fmt:param>
										</fmt:message>
									</html-el:button> <br>
									<br>
									</td>
								</tr>
							</table>
							<br>
							<table width="93%" border="0" cellpadding="0" cellspacing="0">
								<tr>
									<td align="center"><html-el:submit styleClass="buttn"
										styleId="createLoanProductPreview.button.submit"
										property="submitBut">
										<mifos:mifoslabel name="product.butsubmit"
											bundle="ProductDefUIResources" />
									</html-el:submit> &nbsp; <html-el:button styleId="createLoanProductPreview.button.cancel" 
										property="cancel"
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
					<html-el:hidden property="method" value="create" /> <html-el:hidden
						property="currentFlowKey" value="${requestScope.currentFlowKey}" />
					</html-el:form> </tiles:put> </tiles:insert>
