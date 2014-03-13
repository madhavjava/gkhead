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


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="/tags/date" prefix="date"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<head>

<style>
tr.even {
  background-color: #ddd;
}
tr.odd {
  background-color: #eee;
}

div.scroll {
height: 80%;
width: 100%;
overflow: auto;
float:left;
padding: 0px;
position:relative;
}
table.pkgtable td
{
border-top: 1px dotted #6699CC;
}
table.pkgtable tr.pkgtableheaderrow
{
background-color: #BEC8D1;
text-align: center;
font-family: Verdana;
font-weight: bold;
font-size: 13px;
}

table.burlywoodborder {
border-right: solid 1px #EAEBF4;
border-left : solid 1px #EAEBF4;
border-top : solid 1px #EAEBF4;
border-bottom : solid 1px #EAEBF4;
}

</style>
</head>
<tiles:insert definition=".financialAccountingLayout">
<tiles:put name="body" type="string" >
<!-- <span id="page.id" title="CustomerList"></span> -->
<script language="javascript">
$(document).ready(function() {

if(false==${sessionScope.jvsave})
{
  document.getElementById("simpleaccounting.button.preview").style.backgroundColor="FF9933";
  }
 });

function fnSubmit(form, buttonSubmit) {
	buttonSubmit.disabled=true;
	form.method.value="submit";
	form.action="multipleJournalvoucheraction.do";
	form.submit();
}

function fnCancel(form) {
	form.method.value="cancel";
	form.action="multipleJournalvoucheraction.do";
	form.submit();
}

function fnEditTransaction(form) {
	form.method.value="previous";
	form.action="multipleJournalvoucheraction.do";
	form.submit();
}
function fnSaveStage(form) {

	form.method.value="saveStageSubmit";
	form.action="multipleJournalvoucheraction.do";
	form.submit();
}
function addRow(tableID) {

            var table = document.getElementById(tableID);

            var rowCount = table.rows.length;
            var row = table.insertRow(rowCount);

            var colCount = table.rows[0].cells.length;

            for(var i=0; i<colCount; i++) {

                var newcell = row.insertCell(i);

                newcell.innerHTML = table.rows[0].cells[i].innerHTML;
                //alert(newcell.childNodes);
               switch(newcell.childNodes[0].type) {
                    case "text":
                            newcell.childNodes[0].value = "";
                           break;
                    case "checkbox":
                            newcell.childNodes[0].checked = false;
                            break;
                    case "select-one":
                            newcell.childNodes[0].selectedIndex = 0;
                            break;
                }
           }
        }

       function deleteRow(tableID) {
            try {
           var table = document.getElementById(tableID);
           var rowCount = table.rows.length;

            for(var i=0; i<rowCount; i++) {
                var row = table.rows[i];
               var chkbox = row.cells[0].childNodes[0];
                if(null != chkbox && true == chkbox.checked) {
                    if(rowCount <= 1) {
                       alert("Cannot delete all the rows.");
                        break;
                    }
                    table.deleteRow(i);
                    rowCount--;
                    i--;
                }


            }
            }catch(e) {
                alert(e);
            }
        }

</script>
</script>

<fmt:setLocale value='${sessionScope["org.apache.struts.action.LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.SimpleAccountingUIResources"/>
<html-el:form action="/multipleJournalvoucheraction.do">

		<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td height="350" align="left" valign="top" bgcolor="#FFFFFF">

							<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
									<tr>
										<td align="center" class="heading">
											&nbsp;
										</td>
									</tr>
							</table>


					<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0" class="burlywoodborder">
							<tr>
								<td align="left" valign="top" class="paddingleftCreates">

									<table width="93%" border="0" cellpadding="3" cellspacing="0">
										<tr>
											<td class="headingorange">
												
												<mifos:mifoslabel name="simpleAccounting.review" />

											</td>
										</tr>
										<tr>
											<td class="fontnormal">
												<br>
												<mifos:mifoslabel name="simpleAccounting.reviewNotes" />
											</td>
										</tr>
									</table>

									<logic:messagesPresent>
										<font class="fontnormalRedBold"><span id="BulkEntry.error.message"> <html-el:errors bundle="simpleAccountingUIResources" /> </span> </font>
										<br>
									</logic:messagesPresent>
									<br>

				<table width="93%" border="0" cellpadding="3" cellspacing="0">
					<tr class="fontnormal">
							<th align="right">
			                   <mifos:mifoslabel name="simpleAccounting.voucherDate" mandatory="yes" isColonRequired="Yes"/>
			                </th>
			                <td align="left">
			                   ${JournalVoucherActionForm.voucherDate}
			                </td>
					</tr>
					<tr class="fontnormal">
						<th align="right"><mifos:mifoslabel name="simpleAccounting.officeHeirarchy" mandatory="yes" isColonRequired="Yes"/></th>
						<td align="left">
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==1}"> 
						 <mifos:mifoslabel name="simpleAccounting.headOffice" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==2}"> 
						 <mifos:mifoslabel name="simpleAccounting.regionalOffice" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==3}"> 
						 <mifos:mifoslabel name="simpleAccounting.divisionalOffice" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==4}"> 
						 <mifos:mifoslabel name="simpleAccounting.areaOffice" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==5}"> 
						 <mifos:mifoslabel name="simpleAccounting.branchOffice" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==6}"> 
						 <mifos:mifoslabel name="simpleAccounting.center" />
						</c:if>
						<c:if test="${multiplejournalvoucheractionform.officeHierarchy==7}"> 
						 <mifos:mifoslabel name="simpleAccounting.group" />
						</c:if>
					   </td>
					   <th align="right"><mifos:mifoslabel name="simpleAccounting.office" mandatory="yes" isColonRequired="Yes"/></th>
					   <td align="left">
				     <c:forEach items="${sessionScope.DynamicOfficesOnHierarchy}" var="offices">
								<c:if test="${offices.globalOfficeNumber==multiplejournalvoucheractionform.office}">
					                          ${offices.displayName}
								</c:if>  
						 </c:forEach>
					</td>
			 </tr>

			<tr class="fontnormal">

			   <th width="15%" align="right"><mifos:mifoslabel name="simpleAccounting.debitAccount" mandatory="yes" isColonRequired="Yes"/></th>
					<td width="25%" align="left">
						  <c:forEach items="${sessionScope.DebitAccountGlCodes}" var="debitglAccounts">
							 	<c:if test="${debitglAccounts.glcode==multiplejournalvoucheractionform.debitAccountHead}"> 
					                  ${debitglAccounts.glname}
								</c:if>  
					      </c:forEach>
			       </td>
          
                </tr>

			    <!--  
				<th align="right"><mifos:mifoslabel name="simpleAccounting.creditAccount" mandatory="yes" isColonRequired="Yes"/></th>
				<td align="left">
					     <c:forEach items="${sessionScope.CreditAccounts}" var="creditglAccounts">
							 	<c:if test="${creditglAccounts.glcode==multiplejournalvoucheractionform.creditAccountHead}"> 
					                  ${creditglAccounts.glname}
   ${multiplejournalvoucheractionform.creditAccountHead}
								</c:if>  
					      </c:forEach>			
				</td>

			</tr>

			<tr class="fontnormal">
				<th align="right"><mifos:mifoslabel name="simpleAccounting.amount" mandatory="yes" isColonRequired="Yes"/></th>
				<td align="left">
						<fmt:formatNumber value="${JournalVoucherActionForm.amount}"/>
				</td>
			</tr>

		<tr class="fontnormal">
		   <th align="right" colspan="1"><mifos:mifoslabel name="simpleAccounting.voucherNotes" mandatory="yes" isColonRequired="Yes"/></th>
		     <td align="left">
		        ${multiplejournalvoucheractionform.voucherNotes}
             </td>
		</tr>
		
   -->
  <tr class="fontnormal" >
                <th>
                  Credit Account Heads
                </th>
                <th>
                    Amount
                </th>
                <th align="left">
                    Transaction Notes
                </th>
            </tr>
<table border="1">
<c:forEach items="${sessionScope.accounHeadValues}" var="accountHeadvalues">

                <tr class="fontnormal">
                <td><c:forEach items="${sessionScope.CreditAccounts}" var="accountHead">
							<c:if test="${accountHead.glcode==accountHeadvalues.accountHead}">
				            <html-el:text property="creditAccountHead"  styleClass="separatedNumber" styleId="simpleaccounting.input.coaname" readonly="true" value="${accountHead.glname}"/>
							</c:if>
					 </c:forEach></td>


                    <td>
                        <html-el:text property="amount"  styleClass="separatedNumber" value="${accountHeadvalues.amounts}" styleId="${accountHead.sno}" readonly="true" />
                    </td>
                    <td>
                        <html-el:text property="voucherNotes"  styleClass="separatedNumber"  readonly="true" value="${accountHeadvalues.trannotes}" styleId="simpleaccounting.input.transactionnotes" />
                    </td>
                    <td>

                    </td>
                </tr>

           </c:forEach>
</table>
		<tr class="fontnormal">
		 <td align="left" colspan='2'>
		   	 <html-el:button property="editButton" styleClass="insidebuttn" onclick="fnEditTransaction(this.form)">
					<mifos:mifoslabel name="simpleAccounting.editTransaction" />                   		
			 </html-el:button>
         </td>
		</tr>
	



		</table>

			<br>
			<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center" class="blueline">
												&nbsp;
											</td>
										</tr>
			</table>

					<html-el:hidden property="method" value="preview" />
					<html-el:hidden property="input" value="preview" />
				<br>

					<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center">
												 <html-el:submit styleId="simpleaccounting.button.preview"   onclick="fnSubmit(this.form, this)">
													<mifos:mifoslabel name="simpleAccounting.save"/>
											    </html-el:submit>
												&nbsp;
												<html-el:button  styleId="bulkentry.button.cancel" property="cancel" styleClass="cancelbuttn"  onclick="fnCancel(this.form);">
													<mifos:mifoslabel name="simpleAccounting.cancel"/>
												</html-el:button>
												<html-el:submit  styleId="bulkentry.button.savestage"  styleClass="buttn"  onclick="fnSaveStage(this.form);">
													<mifos:mifoslabel name="simpleAccounting.saveStage"/>
												</html-el:submit>
											</td>
										</tr>
									</table>

<br>
								</td>
							</tr>
						</table>
						<br>
					</td>
				</tr>
			</table>
		</html-el:form>
		</body>
	</tiles:put>
</tiles:insert>
