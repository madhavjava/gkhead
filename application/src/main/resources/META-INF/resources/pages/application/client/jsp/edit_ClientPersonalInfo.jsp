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
<!-- edit_ClientPersonalInfo.jsp -->

<%@taglib uri="/tags/mifos-html" prefix="mifos"%>

<%@taglib uri="/tags/date" prefix="date"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>

<fmt:setLocale value='${sessionScope["org.apache.struts.action.LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.ClientUIResources"/>

<tiles:insert definition=".clientsacclayoutsearchmenu">
	<tiles:put name="body" type="string">
	<span id="page.id" title="EditClientPersonalInfo"></span>
		<script language="javascript" SRC="pages/framework/js/date.js"></script>
                <script language="javascript" src="pages/application/client/js/client.js"></script>
		<script language="javascript">
		function goToCancelPage(){
			goBackToViewClientDetails.submit();
		}
		</script>
	<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}"
			   var="BusinessKey" />
	<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'personalInformationOrder')}" var="personalInformationOrder" />
    <form name="goBackToViewClientDetails" method="get" action ="viewClientDetails.ftl">
		<input type="hidden" name='globalCustNum' value="${BusinessKey.globalCustNum}"/>
	</form>
	<html-el:form action="clientCustAction.do?method=previewEditPersonalInfo"
			enctype="multipart/form-data" onsubmit="return chkForValidDates()">
			<html-el:hidden property="input" value="editPersonalInfo" />
			<html-el:hidden property="status" value="${BusinessKey.customerStatus.id}" />

			<%-- <td align="left" valign="top" bgcolor="#FFFFFF" class="paddingleftmain"> --%>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"> <customtags:headerLink/> </span>
				</tr>
			</table>
			<%-- Start of information --%>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="left" valign="top" class="paddingL15T15">
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td class="headingorange"><span class="heading"><c:out
								value="${sessionScope.clientCustActionForm.clientName.displayName}" /> - </span><mifos:mifoslabel
								name="client.EditPersonalInformationLink"
								bundle="ClientUIResources"></mifos:mifoslabel></td>
						</tr>
						<tr>
							<td class="fontnormal"><mifos:mifoslabel
								name="client.PreviewEditInfoInstruction"
								bundle="ClientUIResources"></mifos:mifoslabel> 
								<fmt:message key="client.EditPageCancelInstruction">
									<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}"
										bundle="ClientUIResources"/></fmt:param>
								</fmt:message> <span
								class="mandatorytext"><font color="#FF0000">*</font></span> <mifos:mifoslabel
								name="client.FieldInstruction" bundle="ClientUIResources"></mifos:mifoslabel>
							</td>
						</tr>
					</table>
					<br>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<font class="fontnormalRedBold"><span id="edit_ClientPersonalInfo.error.message"><html-el:errors
							bundle="ClientUIResources" /></span> </font>
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="client.PersonalInformationHeading"
								bundle="ClientUIResources"></mifos:mifoslabel><br>
							<br>
							</td>
						</tr>
      
                        <html-el:hidden property="clientName.nameType" value="3" />
                        
                        <c:forEach items="${personalInformationOrder}" var="personalInformation">
                            <c:choose>
                                <c:when test="${personalInformation.name == 'salutation'}">
                                    <%-- Salutation --%>
                        <tr class="fontnormal">
                            <td width="17%" align="right"><mifos:mifoslabel
                                name="client.Salutation" mandatory="yes"
                                bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td width="83%">
                                <mifos:select   name="clientCustActionForm" property="clientName.salutation" size="1">
                                <c:forEach var="salutationEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'salutationEntity')}" >
                                    <html-el:option value="${salutationEntityList.id}">${salutationEntityList.name}</html-el:option>
                                </c:forEach>
                                </mifos:select>
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'firstName'}">
                                <%-- First Name --%>
                        <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.firstName"><mifos:mifoslabel name="client.FirstName"
                                mandatory="yes" bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.firstName" name="clientCustActionForm" property="clientName.firstName" maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'middleName'}">
                                <%-- Middle Name --%>
                        <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.middleName"><mifos:mifoslabel keyhm="Client.MiddleName" name="client.MiddleName"
                                bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.middleName" keyhm="Client.MiddleName" name="clientCustActionForm"   property="clientName.middleName" maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'lastName'}">
                                <%-- Last Name --%>
                        <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.lastName"><mifos:mifoslabel name="client.LastName"
                                mandatory="yes" bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.lastName" name="clientCustActionForm" property="clientName.lastName" maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'secondLastName'}">
                                <%-- Second Last Name --%>
                        <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.secondLastName"><mifos:mifoslabel keyhm="Client.SecondLastName" name="client.SecondLastName"
                                isColonRequired="no" bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.secondLastName" keyhm="Client.SecondLastName" name="clientCustActionForm"
                                 property="clientName.secondLastName" maxlength="200" />
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'governmentId'}">
                                <c:choose>

                            <c:when test="${BusinessKey.customerStatus.id != CustomerStatus.CLIENT_ACTIVE.value}">
                                <!-- If status is approved then display government id and date of birth as labels -->
                                <%-- Government Id --%>
                                <tr class="fontnormal">
                                    <td align="right">
                                    <span id="edit_ClientPersonalInfo.label.governmentId">
                                    <mifos:mifoslabel   keyhm="Client.GovernmentId" name="${ConfigurationConstants.GOVERNMENT_ID}" isColonRequired="yes"/>
                                    </span>
                                        </td>
                                    <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.governmentId" keyhm="Client.GovernmentId" name="clientCustActionForm"
                                        property="governmentId" maxlength="50" /></td>
                                </tr>

                            </c:when>
                            <c:otherwise>
                                <%-- Government Id Label--%>
                                <tr class="fontnormal">
                                    <td align="right"><mifos:mifoslabel keyhm="Client.GovernmentId" isColonRequired="yes"
                                        name="${ConfigurationConstants.GOVERNMENT_ID}"></mifos:mifoslabel></td>
                                    <td><c:out value="${BusinessKey.governmentId}" />
                                        <html-el:hidden name="clientCustActionForm" property="governmentId" value="${BusinessKey.governmentId}" /></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                                </c:when>
                                <c:when test="${personalInformation.name == 'dateOfBirth'}">
                                <%-- Date Of  Birth Label--%>
                       <tr class="fontnormal">
                            <td align="right">
                                <mifos:mifoslabel name="client.DateOfBirth" mandatory="yes" bundle="ClientUIResources"></mifos:mifoslabel>
                            </td>
                            <td>
                            <date:datetag renderstyle="simple" property="dateOfBirth" /></td>
                            </td>
                       </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'gender'}">
                                <%-- Gender --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel name="client.Gender"
                                mandatory="yes" bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td>
                                <mifos:select name="clientCustActionForm" property="clientDetailView.gender" size="1">
                                    <c:forEach var="genderEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'genderEntity')}" >
                                        <html-el:option value="${genderEntityList.id}">${genderEntityList.name}</html-el:option>
                                    </c:forEach>
                                </mifos:select>
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'maritalStatus'}">
                                <%-- Marital Status --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.MaritalStatus" name="client.MaritalStatus"
                                bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td>
                                <mifos:select keyhm="Client.MaritalStatus" name="clientCustActionForm" property="clientDetailView.maritalStatus" size="1">
                                    <c:forEach var="maritalStatusEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'maritalStatusEntity')}" >
                                        <html-el:option value="${maritalStatusEntityList.id}">${maritalStatusEntityList.name}</html-el:option>
                                    </c:forEach>
                                </mifos:select>
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'numberOfChildren'}">
                                <%-- Number Of Children--%>
                        <tr class="fontnormal">
                            <td align="right" class="fontnormal"><span id="edit_ClientPersonalInfo.label.customField"><mifos:mifoslabel
                                keyhm="Client.NumberOfChildren" name="client.NumberOfChildren" bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosnumbertext styleId="edit_ClientPersonalInfo.input.customField" keyhm="Client.NumberOfChildren" 
                                    name="clientCustActionForm" property="clientDetailView.numChildren" maxlength="5" size="10" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'citizenship'}">
                                <%-- Citizenship --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.Citizenship" isColonRequired="yes"
                                name="${ConfigurationConstants.CITIZENSHIP}"></mifos:mifoslabel></td>
                            <td><mifos:select keyhm="Client.Citizenship"
                                        name="clientCustActionForm"
                                        property="clientDetailView.citizenship" size="1">
                                        <c:forEach var="citizenshipEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'citizenshipEntity')}" >
                                            <html-el:option value="${citizenshipEntityList.id}">${citizenshipEntityList.name}</html-el:option>
                                        </c:forEach>
                                </mifos:select></td>

                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'ethnicity'}">
                                <%-- Ethnicity --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.Ethnicity"
                                name="${ConfigurationConstants.ETHNICITY}" isColonRequired="yes"
                                bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td><mifos:select keyhm="Client.Ethnicity"
                                        name="clientCustActionForm"
                                        property="clientDetailView.ethnicity" size="1">
                                        <c:forEach var="ethnicityEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'ethnicityEntity')}" >
                                            <html-el:option value="${ethnicityEntityList.id}">${ethnicityEntityList.name}</html-el:option>
                                        </c:forEach>
                                </mifos:select></td>

                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'educationLevel'}">
                                <%-- Education Level --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel name="client.EducationLevel" keyhm="Client.EducationLevel"
                                bundle="ClientUIResources"></mifos:mifoslabel></td>

                            <td><mifos:select keyhm="Client.EducationLevel"
                                        name="clientCustActionForm"
                                        property="clientDetailView.educationLevel" size="1">
                                        <c:forEach var="educationLevelEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'educationLevelEntity')}" >
                                            <html-el:option value="${educationLevelEntityList.id}">${educationLevelEntityList.name}</html-el:option>
                                        </c:forEach>
                                    </mifos:select></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'businessActivity'}">
                                <%-- Business Activities --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.BusinessActivities"
                                name="client.BusinessActivities" bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td><mifos:select name="clientCustActionForm" keyhm="Client.BusinessActivities"
                                        property="clientDetailView.businessActivities" size="1">
                                        <c:forEach var="businessActivitiesEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'businessActivitiesEntity')}" >
                                            <html-el:option value="${businessActivitiesEntityList.id}">${businessActivitiesEntityList.name}</html-el:option>
                                        </c:forEach>
                                    </mifos:select></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'povertyStatus'}">
                                <%-- Poverty Status --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.PovertyStatus"
                                name="client.PovertyStatus" bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td><mifos:select name="clientCustActionForm" keyhm="Client.PovertyStatus"
                                        property="clientDetailView.povertyStatus" size="1">
                                        <c:forEach var="povertyStatus" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'povertyStatus')}" >
                                            <html-el:option value="${povertyStatus.id}">${povertyStatus.name}</html-el:option>
                                        </c:forEach>
                                    </mifos:select></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'handicapped'}">
                                <%-- Handicapped --%>
                        <tr class="fontnormal">
                            <td align="right"><mifos:mifoslabel keyhm="Client.Handicapped" isColonRequired="yes"
                                name="${ConfigurationConstants.HANDICAPPED}"
                                bundle="ClientUIResources"></mifos:mifoslabel></td>
                            <td><mifos:select keyhm="Client.Handicapped"
                                        name="clientCustActionForm"
                                        property="clientDetailView.handicapped" size="1">
                                        <c:forEach var="handicappedEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'handicappedEntity')}" >
                                            <html-el:option value="${handicappedEntityList.id}">${handicappedEntityList.name}</html-el:option>
                                        </c:forEach>
                                </mifos:select>
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'photo'}">
                                <%-- Photograph ADD CUSTOMER PICTURE TO ACTION FORM AND CUSTOMER VO --%>
                        <c:if test="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'isPhotoFieldHidden') == false}" >
                        <tr class="fontnormal">
                            <td align="right">
                            <span id="edit_ClientPersonalInfo.label.file">
                            <mifos:mifoslabel keyhm="Client.Photo" name="client.Photo"
                                bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td>
                              <c:if test = "${sessionScope.clientCustActionForm.picture != null 
                              && sessionScope.clientCustActionForm.picture.fileName != null 
                              && sessionScope.clientCustActionForm.picture.fileName != '' }" >
                                <img src="clientCustAction.do?method=retrievePictureOnPreview&currentFlowKey=${requestScope.currentFlowKey}" width="150"/>
                                <br />
                                <a id="editPersonalInformation.link.deletePhoto"
                                href="clientCustAction.do?method=editPersonalInfo&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}&photoDelete=true">
                                Delete
                                </a>
                                <br />
                                Choose another file to change the picture
                            </c:if>
                            <br />
                            <mifos:file styleId="edit_ClientPersonalInfo.input.file" keyhm="Client.Photo" property="picture" maxlength="200"
                                onkeypress="return onKeyPressForFileComponent(this);" /></td>
                        </tr>
                        </c:if>
                                </c:when>
                                <c:when test="${personalInformation.name == 'spouseDetails'}">
                                <%-- Spouse/Father details --%>
                        <c:if test="${!session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'areFamilyDetailsRequired') &&
                                !session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'areFamilyDetailsHidden')}">
                        <tr class="fontnormal">
                            <td align="right" class="fontnormal"><mifos:mifoslabel keyhm="Client.SpouseFatherInformation"
                                name="client.SpouseFatherName" bundle="ClientUIResources"/></td>
                            <td>
                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                <tr class="fontnormal">
                                  <td width="14%"><mifos:mifoslabel name="client.Relationship" bundle="ClientUIResources"></mifos:mifoslabel>
                                    <mifos:select name="clientCustActionForm"
                                        property="spouseName.nameType" size="1">
                                        <c:forEach var="spouseEntityList" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'spouseEntity')}" begin="0" end="1" >
                                            <html-el:option value="${spouseEntityList.id}">${spouseEntityList.name}</html-el:option>
                                        </c:forEach>
                                    </mifos:select>
                                   </td>
                                   <td class="paddingL10">
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <span id="edit_ClientPersonalInfo.label.spouseFirstName">
                                                    <mifos:mifoslabel keyhm="Client.SpouseFatherInformation" name="client.SpouseFirstName"
                                                        bundle="ClientUIResources">
                                                    </mifos:mifoslabel>
                                                    </span>
                                                </td>
                                            </tr>
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.spouseFirstName" property="spouseName.firstName" maxlength="200"    style="width:100px;" />
                                                </td>
                                            </tr>
                                        </table>
                                    <td class="paddingL10">
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <span id="edit_ClientPersonalInfo.label.spouseMiddleName">
                                                    <mifos:mifoslabel keyhm="Client.SpouseFatherMiddleName" name="client.SpouseMiddleName" bundle="ClientUIResources">
                                                    </mifos:mifoslabel>
                                                    </span>
                                                </td>
                                            </tr>
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                  <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.spouseMiddleName" keyhm="Client.SpouseFatherMiddleName" property="spouseName.middleName" maxlength="200" style="width:100px;" />
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td class="paddingL10">
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <span id="edit_ClientPersonalInfo.label.spouseSecondLastName">
                                                    <mifos:mifoslabel keyhm="Client.SpouseFatherSecondLastName"
                                                        name="client.SpouseSecondLastName" bundle="ClientUIResources">
                                                    </mifos:mifoslabel>
                                                </td>
                                            </tr>
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.spouseSecondLastName" keyhm="Client.SpouseFatherSecondLastName"
                                                                property="spouseName.secondLastName"
                                                                    maxlength="200" style="width:100px;" />
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td class="paddingL10">
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                    <span id="edit_ClientPersonalInfo.label.spouseLastName">
                                                    <mifos:mifoslabel keyhm="Client.SpouseFatherInformation" name="client.SpouseLastName"
                                                                      bundle="ClientUIResources">
                                                       </mifos:mifoslabel>
                                                    </span>
                                                </td>
                                            </tr>
                                            <tr class="fontnormal">
                                                <td class="paddingL10">
                                                   <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.spouseLastName" property="spouseName.lastName" maxlength="200"
                                                                style="width:100px;" />
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            </td>
                        </tr>
                        </c:if>
                                </c:when>
                                <c:when test="${personalInformation.name == 'addressHeading'}">
                                <tr>
                            <td colspan="2" class="fontnormalbold"><mifos:mifoslabel
                                name="client.AddressHeading"  keyhm="Client.Address" isManadatoryIndicationNotRequired="yes" bundle="ClientUIResources"></mifos:mifoslabel><br>
                            <br>
                            </td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'address1'}">
                                <tr class="fontnormal">
                            <td width="17%" align="right"><span id="edit_ClientPersonalInfo.label.address1"><mifos:mifoslabel keyhm="Client.Address1" isColonRequired="yes"
                                name="${ConfigurationConstants.ADDRESS1}"></mifos:mifoslabel></span></td>
                            <td width="83%"><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.address1" keyhm="Client.Address1"
                                name="clientCustActionForm"
                                property="address.line1"
                                maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'address2'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.address2"><mifos:mifoslabel keyhm="Client.Address2" isColonRequired="yes"
                                name="${ConfigurationConstants.ADDRESS2}"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.address2" keyhm="Client.Address2" name="clientCustActionForm"
                                property="address.line2"
                                maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'address3'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.address3"><mifos:mifoslabel keyhm="Client.Address3" isColonRequired="yes"
                                name="${ConfigurationConstants.ADDRESS3}"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.address3" keyhm="Client.Address3" name="clientCustActionForm"
                                property="address.line3"
                                maxlength="200" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'city'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.city"><mifos:mifoslabel keyhm="Client.City" isColonRequired="yes"
                                name="${ConfigurationConstants.CITY}"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.city" keyhm="Client.City" name="clientCustActionForm"
                                property="address.city"
                                maxlength="100" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'state'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.state"><mifos:mifoslabel keyhm="Client.State" isColonRequired="yes"
                                name="${ConfigurationConstants.STATE}"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.state"  keyhm="Client.State" name="clientCustActionForm"
                                property="address.state"
                                maxlength="100" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'country'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.country"><mifos:mifoslabel keyhm="Client.Country" name="client.Country"
                                bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.country" keyhm="Client.Country" name="clientCustActionForm"
                                property="address.country"
                                maxlength="100" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'postalCode'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.postalCode"><mifos:mifoslabel keyhm="Client.PostalCode" isColonRequired="yes"
                                name="${ConfigurationConstants.POSTAL_CODE}"></mifos:mifoslabel></span></td>
                            <td><mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.postalCode" keyhm="Client.PostalCode" name="clientCustActionForm"
                                property="address.zip"
                                maxlength="20" /></td>
                        </tr>
                                </c:when>
                                <c:when test="${personalInformation.name == 'telephone'}">
                                <tr class="fontnormal">
                            <td align="right"><span id="edit_ClientPersonalInfo.label.telephone"><mifos:mifoslabel keyhm="Client.PhoneNumber" name="client.Telephone"
                                bundle="ClientUIResources"></mifos:mifoslabel></span></td>
                            <td>
                                                            <c:choose>
                                                                <c:when test="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'CanEditPhoneNumber')}">
                                                                    <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.telephone" keyhm="Client.PhoneNumber" name="clientCustActionForm"
                                                                    property="address.phoneNumber"
                                                                    maxlength="20" />
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.telephone" keyhm="Client.PhoneNumber" name="clientCustActionForm"
                                                                    property="address.phoneNumber"
                                                                    maxlength="20" readonly="true" style="background:lightgrey; color:grey"/>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                        </tr>
                                </c:when>
                            </c:choose>
                        </c:forEach>
	
					</table>
					<br>
					<!-- Custom Fields -->
					<c:if test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}">
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
								name="client.AdditionalInformationHeading"
								bundle="ClientUIResources"></mifos:mifoslabel><br>
							<br>
							</td>
						</tr>

						<!-- For each custom field definition in the list custom field entity is passed as key to mifos label -->
						<c:forEach var="customFieldDef"
							items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}" varStatus="loopStatus">
							<bean:define id="ctr">
								<c:out value="${loopStatus.index}" />
							</bean:define>
							<c:forEach var="cf"
								items="${sessionScope.clientCustActionForm.customFields}">
								<c:if test="${customFieldDef.fieldId==cf.fieldId}">
									<tr class="fontnormal">
										<td width="17%" align="right"><span id="edit_ClientPersonalInfo.label.customField"><mifos:mifoslabel
											name="${customFieldDef.lookUpEntityType}"
											mandatory="${customFieldDef.mandatoryString}"
											bundle="ClientUIResources" isColonRequired="yes"></mifos:mifoslabel></span></td>
										<td width="83%"><c:if test="${customFieldDef.fieldType == CustomFieldType.NUMERIC.value}">
											<mifos:mifosnumbertext styleId="edit_ClientPersonalInfo.input.customField" name="clientCustActionForm"
												property='customField[${ctr}].fieldValue' value="${cf.fieldValue}" maxlength="200" />
										</c:if> <c:if test="${customFieldDef.fieldType == CustomFieldType.ALPHA_NUMERIC.value}">
											<mifos:mifosalphanumtext styleId="edit_ClientPersonalInfo.input.customField" name="clientCustActionForm"
												property='customField[${ctr}].fieldValue' value="${cf.fieldValue}" maxlength="200" />
										</c:if> <c:if test="${customFieldDef.fieldType == CustomFieldType.DATE.value}">
											<date:datetag property="customField[${ctr}].fieldValue"	/>

										</c:if>
										<html-el:hidden property='customField[${ctr}].fieldId'	value="${cf.fieldId}"></html-el:hidden>
										<html-el:hidden	property='fieldTypeList' value='${customFieldDef.fieldType}' />
										</td>
									</tr>
								</c:if>
							</c:forEach>
						</c:forEach>
					</table>
				</c:if>
					<!--Custom Fields end  -->
					<table width="93%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class="blueline">&nbsp;</td>
						</tr>
					</table>
					<br>
					<table width="93%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center"><html-el:submit styleId="edit_ClientPersonalInfo.button.preview" styleClass="buttn">
								<mifos:mifoslabel name="button.preview"
									bundle="ClientUIResources"></mifos:mifoslabel>
							</html-el:submit> &nbsp; &nbsp; <html-el:button styleId="edit_ClientPersonalInfo.button.cancel"
								onclick="goToCancelPage();" property="cancelButton"
								styleClass="cancelbuttn">
								<mifos:mifoslabel name="button.cancel"
									bundle="ClientUIResources"></mifos:mifoslabel>
							</html-el:button></td>
						</tr>
					</table>
					</td>
				</tr>
			</table>
			<br>
			<td></td>
			<tr></tr>
			<table></table>
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
