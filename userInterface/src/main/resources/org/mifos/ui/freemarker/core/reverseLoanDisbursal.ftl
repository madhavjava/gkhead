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
[#include "layout.ftl"]
[@adminLeftPaneLayout]  <!--  Main Content Begins-->
  <div class="content">
      <form method="" action="" name="formname">
      <p class="bluedivs paddingLeft"><a href="admin.html">[@spring.message "admin"/]</a>&nbsp;/&nbsp;<span class="fontBold">[@spring.message "manageLoanAccounts.reverseLoansdisbursal"/]</span></p>
    <p class="orangeheading font15">[@spring.message "manageLoanAccounts.reverseLoansdisbursal"/]</p>
    <ul><li class="error" > </li></ul>
    <p class="paddingLeft">[@spring.message "manageLoanAccounts.reverseLoansdisbursal.searchLoansaccountbyID"/]<br />
      <input type="text" id="txtid"/>&nbsp;&nbsp;<input class="buttn" type="button" name="search" value="[@spring.message "admin.search"/]" onclick="#" />
    </p>

       </form>
  </div><!--Main Content Ends-->
   [/@adminLeftPaneLayout]