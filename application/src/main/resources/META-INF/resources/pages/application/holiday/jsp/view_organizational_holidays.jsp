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

<%@ taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@ taglib uri="/tags/struts-html-el" prefix="html-el"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/sessionaccess" prefix="session"%>

<%@page import="org.mifos.application.holiday.util.helpers.HolidayConstants;"%>

<tiles:insert definition=".view">
<tiles:put name="body" type="string">
<span id="page.id" title="view_organizational_holidays"></span>
<html-el:form action="/holidayAction.do">
<table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="bluetablehead05">
	          <span class="fontnormal8pt">
	          	 <html-el:link action="AdminAction.do?method=load&randomNUm=${sessionScope.randomNUm}">
					<mifos:mifoslabel name="holiday.labelLinkAdmin" bundle="HolidayUIResources"/>	
				</html-el:link> /
	          </span><span class="fontnormal8ptbold">
          			<mifos:mifoslabel name="holiday.labelLinkViewHolidays" bundle="HolidayUIResources"/>
          	   </span>
          </td>
        </tr>
      </table>
      <table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td width="70%" align="left" valign="top" class="paddingL15T15"><table width="98%" border="0" cellspacing="0" cellpadding="3">
            <tr>
              <td width="35%">
	            <span class="heading">
    	          	<mifos:mifoslabel name="holiday.labelLinkViewHolidays" bundle="HolidayUIResources"/>
    	        </span>
              </td>
            </tr>
            
            <tr>
			  <td class="fontnormalbold">
			     <span class="fontnormal">
	                <mifos:mifoslabel name="holiday.labelLinkListOrganizationalHolidays" bundle="HolidayUIResources"/>
	                <mifos:mifoslabel name="holiday.labelLinkClickHere" bundle="HolidayUIResources"/>
					<html-el:link styleId="holiday.link.defineNewHoliday" action="holidayAction.do?method=addHoliday&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
						<mifos:mifoslabel name="holiday.labelAddNewHolidayNow" bundle="HolidayUIResources"/>
					</html-el:link>
	             </span>
            	 <br><br>
            	 
              </td>
            </tr>    
          </table>
          
           <c:forEach var="item" begin="1" end="${noOfYears}">
            <span class="fontnormalbold">
			  <mifos:mifoslabel name="holiday.labelHolidaysForYear" bundle="HolidayUIResources"/>
			  <%= request.getSession().getAttribute(HolidayConstants.YEAR + pageContext.getAttribute("item")) %>
    	    </span>
            <br><br>
            <table width="98%" border="0" cellpadding="3" cellspacing="0">
              <tr>
              <font class="fontnormalRedBold">
				<html-el:errors	bundle="HolidayUIResources" /> 
			  </font>
                <td width="11%" class="drawtablehd">
               		 <mifos:mifoslabel name="holiday.HolidayFromDate" bundle="HolidayUIResources"/>
                </td>
                <td width="11%" class="drawtablehd">
               		 <mifos:mifoslabel name="holiday.HolidayThruDate" bundle="HolidayUIResources"/>
                </td>
                <td width="20%" class="drawtablehd">
                	<mifos:mifoslabel name="holiday.HolidayName" bundle="HolidayUIResources"/>
				</td>
				<td width="15%" class="drawtablehd">
                	<mifos:mifoslabel name="holiday.HolidayRepaymentRule" bundle="HolidayUIResources"/>
				</td>
				<td width="43%" class="drawtablehd">
                	<mifos:mifoslabel name="holiday.ApplicableOffices" bundle="HolidayUIResources"/>
				</td>
              </tr>
              <c:forEach var="holidayItem" items='<%= request.getSession().getAttribute(HolidayConstants.HOLIDAY_LIST + pageContext.getAttribute("item")) %>' >
	              <tr>
	                <td width="11%" class="drawtablerow">${holidayItem.holidayDetails.fromDate}</td>
	                <td width="11%" class="drawtablerow">${holidayItem.holidayDetails.thruDate}&nbsp;</td>
	                <td width="20%" class="drawtablerow">${holidayItem.holidayDetails.name}</td>
	                <td width="15%" class="drawtablerow">${holidayItem.holidayDetails.repaymentRuleName}</td>
	                <td width="43%" class="drawtablerow">${holidayItem.officeNamesAsString}</td>
	              </tr>
              </c:forEach>           
              <tr>
                <td width="11%" class="drawtablerow">&nbsp;</td>
                <td width="11%" class="drawtablerow">&nbsp;</td>
                <td width="20%" class="drawtablerow">&nbsp;</td>
                <td width="15%" class="drawtablerow">&nbsp;</td>
                <td width="43%" class="drawtablerow">&nbsp;</td>
              </tr>
             
            </table>            
            </c:forEach>
            <br>
          </td>
          </tr>
      </table>
      <br>
   	  <html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
      </html-el:form>
</tiles:put>
</tiles:insert>
