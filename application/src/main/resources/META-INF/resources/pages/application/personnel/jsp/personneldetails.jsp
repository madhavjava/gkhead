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
<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom"%>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/personnel/personnelfunctions" prefix="personnelfn"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>

<tiles:insert definition=".view">
	<tiles:put name="body" type="string">
	<span id="page.id" title="personneldetails"></span>
		<script language="javascript">

  function goToCancelPage(){
	personActionForm.action="PersonAction.do?method=cancel";
	personActionForm.submit();
  }
  function viewChangeLog(){
	  personActionForm.action="PersonnelAction.do?method=search&input=UserChangeLog";
	  personActionForm.submit();
  }
</script>
		<html-el:form action="PersonAction.do?method=preview">

			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"> <a id="personneldetails.link.admin"
						href="AdminAction.do?method=load"> <mifos:mifoslabel
						name="Personnel.Admin" bundle="PersonnelUIResources"></mifos:mifoslabel>
					</a> / <a id="personneldetails.link.viewUsers" href="PersonAction.do?method=loadSearch"> <mifos:mifoslabel
						name="Personnel.ViewUsers" bundle="PersonnelUIResources"></mifos:mifoslabel>
					</a> / </span> <c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,
												'personnelInformationDto')}" var="personnelInformationDto" />					
					<span class="fontnormal8ptbold"><c:out
						value="${personnelInformationDto.displayName}" /></span></td>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">

				<td width="70%" align="left" valign="top" class="paddingL15T15">
				<table width="96%" border="0" cellpadding="3" cellspacing="0">
					<tr>
						<td width="50%" height="23" class="headingorange"><span id="personneldetails.text.fullName"><c:out
							value="${personnelInformationDto.displayName}" /></span></td>
						<td width="50%" align="right"><a id="personneldetails.link.editUser"
							href="PersonAction.do?method=manage&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}"> <mifos:mifoslabel
							name="Personnel.EditUserInformation"
							bundle="PersonnelUIResources"></mifos:mifoslabel> </a></td>
					</tr>
					<tr>
						<td colspan="2"><font class="fontnormalRedBold"> <span id="personneldetails.error.message"><html-el:errors
							bundle="PersonnelUIResources" /></span> </font></td>
					</tr>
					<tr>
						<td height="23" class="fontnormalbold"><span class="fontnormal"> <c:choose>
							<%-- Active State --%>
							<c:when test="${personnelInformationDto.status.id == 1}">
								<mifos:MifosImage id="active" moduleName="org.mifos.customers.personnel.util.resources.personnelImages" />
							</c:when>
							<c:when test="${personnelInformationDto.status.id  == 2}">
								<mifos:MifosImage id="inactive" moduleName="org.mifos.customers.personnel.util.resources.personnelImages" />
							</c:when>
							<c:otherwise>
							</c:otherwise>
						</c:choose> <span id="personneldetails.text.status"><c:out value="${personnelInformationDto.status.name}" /></span> </span>
						<c:if test="${personnelInformationDto.locked == 'true'}">
							<span class="fontnormalRed"> <mifos:mifoslabel
								name="Personnel.Locked" bundle="PersonnelUIResources"></mifos:mifoslabel>
							</span>
						</c:if> &nbsp;<br>
						</td>
					</tr>

					<tr id="Personnel.GovernmentId">
						<td class="fontnormalbold"><span class="fontnormal"> <mifos:mifoslabel
							name="${ConfigurationConstants.GOVERNMENT_ID}"
							bundle="PersonnelUIResources" keyhm="Personnel.GovernmentId"
							isColonRequired="yes" isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel>
						<c:out value="${personnelInformationDto.personnelDetails.governmentIdNumber}" />
						<br></td>
					</tr>

					<tr>
						<td class="fontnormalbold"><span class="fontnormal"> <mifos:mifoslabel
							name="Personnel.Email" bundle="PersonnelUIResources"></mifos:mifoslabel>
						<span id="personneldetails.text.email"><c:out value="${personnelInformationDto.emailId}" /></span> <br>

						<mifos:mifoslabel name="Personnel.DOB" bundle="PersonnelUIResources" /> 
						<c:out value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,personnelInformationDto.personnelDetails.dob)}" />;
						<c:out value="${personnelInformationDto.age}" />
						<mifos:mifoslabel name="Personnel.YearsOld" bundle="PersonnelUIResources" />
						<br>

						<c:forEach
							items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'genderList')}" var="item">
							<c:if test="${personnelInformationDto.personnelDetails.gender == item.id}">${item.name}</c:if>
						</c:forEach> 
						<c:if test="${!empty personnelInformationDto.personnelDetails.maritalStatus}">
							<c:out value=";" />
							<c:forEach
								items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'maritalStatusList')}"
								var="item">
								<c:if
									test="${personnelInformationDto.personnelDetails.maritalStatus == item.id}">
											${item.name}
								</c:if>
							</c:forEach>
						</c:if>
						<br>
						<mifos:mifoslabel name="Personnel.DOJMFI"
							bundle="PersonnelUIResources"></mifos:mifoslabel> <c:out
							value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,personnelInformationDto.personnelDetails.dateOfJoiningMFI)}" />

						<br>
						<mifos:mifoslabel name="Personnel.DOJBranch"
							bundle="PersonnelUIResources"></mifos:mifoslabel> <c:out
							value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,personnelInformationDto.personnelDetails.dateOfJoiningBranch)}" />

						</span><br>
						<br>
						<mifos:mifoslabel name="Personnel.Address"
							bundle="PersonnelUIResources"></mifos:mifoslabel><br>
							<c:if test="${ empty personnelInformationDto.personnelDetails.address.displayAddress
									&&  empty personnelInformationDto.personnelDetails.address.city
									&&  empty personnelInformationDto.personnelDetails.address.state
									&&  empty personnelInformationDto.personnelDetails.address.country
									&&  empty personnelInformationDto.personnelDetails.address.zip
									&&  empty personnelInformationDto.personnelDetails.address.phoneNumber}">
									<br>
									<span
										class="fontnormal"><mifos:mifoslabel name="Personnel.addressnotentered"
							bundle="PersonnelUIResources"></mifos:mifoslabel></span>
										<br>
										</c:if>
						<span class="fontnormal"> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.displayAddress}">
							<c:out
								value="${personnelInformationDto.personnelDetails.address.displayAddress}" />
							<br>
						</c:if> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.city}">
							<c:out value="${personnelInformationDto.personnelDetails.address.city}" />
							<br>
						</c:if> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.state}">
							<c:out value="${personnelInformationDto.personnelDetails.address.state}" />
							<br>
						</c:if> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.country}">
							<c:out value="${personnelInformationDto.personnelDetails.address.country}" />
							<br>
						</c:if> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.zip}">
							<c:out value="${personnelInformationDto.personnelDetails.address.zip}" />
							<br>
						</c:if> <br>
						</span> <c:if
							test="${!empty personnelInformationDto.personnelDetails.address.phoneNumber}">
							<span class="fontnormal"> <mifos:mifoslabel
								name="Personnel.Telephone" bundle="PersonnelUIResources"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <c:out
								value="${personnelInformationDto.personnelDetails.address.phoneNumber}" />
							</span>
							<br>
							<br>
						</c:if>
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td width="71%" class="fontnormalbold"><mifos:mifoslabel
									name="Personnel.OfficePermissions"
									bundle="PersonnelUIResources"></mifos:mifoslabel> <br>
								<span class="fontnormal"> <mifos:mifoslabel
									name="Personnel.Office" bundle="PersonnelUIResources"></mifos:mifoslabel>
								<c:out value="${personnelInformationDto.officeName}" /> <br>
								<mifos:mifoslabel name="Personnel.UserTitle"
									bundle="PersonnelUIResources"></mifos:mifoslabel> <c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'titleList')}"
									var="item">

									<c:if test="${personnelInformationDto.title == item.id}">
											${item.name}
								</c:if>
								</c:forEach> <br>
								<mifos:mifoslabel name="Personnel.UserHierarchy"
									bundle="PersonnelUIResources"></mifos:mifoslabel> <c:forEach
									items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'personnelLevelList')}"
									var="item">

									<c:if test="${personnelInformationDto.levelId == item.id}">
											${item.name}
								</c:if>
								</c:forEach> <br>
								<mifos:mifoslabel name="Personnel.Roles"
									bundle="PersonnelUIResources"></mifos:mifoslabel> <c:forEach
									var="personnelRole" items="${personnelInformationDto.personnelRoles}"
									varStatus="loopStatus">
									<bean:define id="ctr">
										<c:out value="${loopStatus.index}" />
									</bean:define>
									<c:choose>
										<c:when test="${ctr == 0}">
											<c:out value="${personnelRole.name}" />
										</c:when>
										<c:otherwise>
					 						, <c:out value="${personnelRole.name}" />
										</c:otherwise>
									</c:choose>
								</c:forEach> </span></td>
							</tr>
						</table>
						<br>
						<mifos:mifoslabel name="Personnel.LoginInformation"
							bundle="PersonnelUIResources"></mifos:mifoslabel><br>
						<span class="fontnormal"> <mifos:mifoslabel
							name="Personnel.UserName" bundle="PersonnelUIResources"></mifos:mifoslabel>
						<c:out value="${personnelInformationDto.userName}" /> </span><br>
                        <span class="fontnormal"> <mifos:mifoslabel
                            name="Personnel.PasswordExpirationDate" bundle="PersonnelUIResources" />
                            <c:if test="${empty personnelInformationDto.passwordExpirationDate}">
                            <mifos:mifoslabel
                            name="Personnel.PasswordExpirationIndefinite" bundle="PersonnelUIResources"/>
                            </c:if>
                            <c:if test="${not empty personnelInformationDto.passwordExpirationDate}">
                                <c:out value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,personnelInformationDto.passwordExpirationDateTime)}" />
                        </c:if> </span><br>
                        <br>
						<span class="fontnormal">
						<c:set var="questionnaireFor" scope="session" value="${personnelInformationDto.displayName}"/>
                        <c:remove var="urlMap" />
                        <jsp:useBean id="urlMap" class="java.util.LinkedHashMap"  type="java.util.HashMap" scope="session"/>
					<c:url value="viewAndEditQuestionnaire.ftl" var="viewAndEditQuestionnaireMethodUrl" >
						<c:param name="creatorId" value="${sessionScope.UserContext.id}" />
						<c:param name="entityId" value="${personnelInformationDto.personnelId}" />
						<c:param name="event" value="Create" />
						<c:param name="source" value="Personnel" />
						<c:param name="backPageUrl" value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'currentPageUrl')}&method=get" />
					</c:url >
                        <c:set target="${urlMap}" property="${personnelInformationDto.displayName}" value="PersonAction.do?method=get&globalPersonnelNum=${personnelInformationDto.globalPersonnelNum}"/>
						<a id="personnelDetail.link.questionGroups" href="${viewAndEditQuestionnaireMethodUrl}">
                        	<mifos:mifoslabel name="client.ViewQuestionGroupResponsesLink" bundle="ClientUIResources" />
                        </a> 
						<br/>
						<html-el:link styleId="personneldetails.link.viewChangeLog"
							href="PersonAction.do?method=loadChangeLog&entityType=Personnel&entityId=${personnelInformationDto.personnelId}&currentFlowKey=${requestScope.currentFlowKey}">

							<mifos:mifoslabel name="Personnel.ViewChangeLog"
								bundle="PersonnelUIResources"></mifos:mifoslabel>
						</html-el:link> </span>
						</td>
						<td height="23" align="right" valign="top" class="fontnormal"><c:if
							test="${personnelInformationDto.locked == 'true'}">
							<a id="personneldetails.link.unlockUser" href="PersonAction.do?method=loadUnLockUser&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
							 <mifos:mifoslabel
								name="Personnel.UnlockUser" bundle="PersonnelUIResources"></mifos:mifoslabel>
							</a>
						</c:if></td>
					</tr>
				</table>
				<br>
				</td>
				<td width="30%" align="left" valign="top" class="paddingleft1">
				<table width="100%" border="0" cellpadding="2" cellspacing="0"
					class="bluetableborder">
					<tr>
						<td class="bluetablehead05"><span class="fontnormalbold"> <mifos:mifoslabel
							name="Personnel.RecentNotes" bundle="PersonnelUIResources"></mifos:mifoslabel>
						</span></td>
					</tr>
					<tr>
						<td class="paddingL10"><img src="pages/framework/images/trans.gif" width="10"
							height="2"></td>
					</tr>
					<tr>
						<td class="paddingL10"><c:choose>
							<c:when test="${!empty personnelInformationDto.recentPersonnelNotes}">
								<c:forEach var="note" items="${personnelInformationDto.recentPersonnelNotes}">
									<span class="fontnormal8ptbold"> <c:out
										value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,note.commentDate)}" />:
									</span>
									<span class="fontnormal8pt"> <c:out value="${note.comment}" />
									-<em><c:out value="${note.personnelName}" /></em><br>
									<br>
									</span>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<span class="fontnormal"> <mifos:mifoslabel
									name="Personnel.NoNotesAvailable" bundle="PersonnelUIResources" />
								</span>
							</c:otherwise>
						</c:choose></td>

					</tr>
					<tr>
						<td align="right" class="paddingleft05"><span
							class="fontnormal8pt"> <c:if test="${!empty personnelInformationDto.recentPersonnelNotes}">
							<a id="personneldetails.link.seeAllNotes" href="personnelNoteAction.do?method=search&personnelId=${personnelInformationDto.personnelId}&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}&personnelName=${personnelInformationDto.displayName}"> <mifos:mifoslabel
								name="Personnel.SeeAllNotes" bundle="PersonnelUIResources"></mifos:mifoslabel>
							</a>
							<br>
						</c:if> <a id="personneldetails.link.addNote" href="personnelNoteAction.do?method=load&personnelId=${personnelInformationDto.personnelId}&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}"> <mifos:mifoslabel
							name="Personnel.AddNote" bundle="PersonnelUIResources"></mifos:mifoslabel>
						</a> </span></td>
					</tr>
				</table>
				<p class="paddingleft1">&nbsp;</p>
				</td>

			</table>
			<br>

		</html-el:form>
	</tiles:put>
</tiles:insert>
