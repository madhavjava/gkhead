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

package org.mifos.test.acceptance.framework.center;

import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.center.CreateCenterEnterDataPage.SubmitFormParameters;
import org.mifos.test.acceptance.framework.client.EditMeetingPage;
import org.mifos.test.acceptance.framework.customer.CustomerChangeStatusPage;
import org.mifos.test.acceptance.framework.loan.AttachSurveyPage;
import org.mifos.test.acceptance.framework.loan.ClosedAccountsPage;
import org.mifos.test.acceptance.framework.questionnaire.ViewQuestionResponseDetailPage;
import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;

public class CenterViewDetailsPage extends MifosPage {

    public CenterViewDetailsPage(Selenium selenium) {
        super(selenium);
        verifyPage("CenterDetails");
    }

    public String getCenterName() {
        return selenium.getText("viewCenterDetails.text.displayName");
    }

    public String getStatus() {
        return selenium.getText("viewCenterDetails.text.status");
    }

    public String getLoanOfficer() {
        return selenium.getText("viewCenterDetails.text.loanOfficer");
    }
    
    public String getMeetingSchedule(){
    	return selenium.getText("viewCenterDetails.text.meetingSchedule");
    }
    
    public void varifyMeetingSchedule(MeetingParameters parameters){
    	Assert.assertTrue(getMeetingSchedule().contains(parameters.getWeekDay().getName()));
    }

    public void verifyActiveCenter(SubmitFormParameters formParameters) {
        Assert.assertEquals(getCenterName(), formParameters.getCenterName());
        Assert.assertEquals(getStatus(), "Active");
        // TODO: Verify this in another way. "Active" is locale dependant.
        Assert.assertEquals(getLoanOfficer(), formParameters.getLoanOfficer());
        Assert.assertEquals(getMeetingSchedule(), "Recur every 1 Week(s) on Wednesday");
        Assert.assertEquals(selenium.getText("viewCenterDetails.meeting.text.meetingplace"), "Bangalore");
    }

    public CenterViewDetailsPage verifyPage() {
        verifyPage("CenterDetails");
        return this;
    }

    public ClosedAccountsPage navigateToClosedAccountsPage() {
        selenium.click("viewCenterDetails.link.viewAllClosedAccounts");
        waitForPageToLoad();
        return new ClosedAccountsPage(selenium);
    }

    public AttachSurveyPage navigateToAttachSurveyPage() {
        selenium.click("viewCenterDetails.link.attachSurvey");
        waitForPageToLoad();
        return new AttachSurveyPage(selenium);
    }
    
    public EditMeetingPage navigateToEditMeetingPage(){
    	selenium.click("viewCenterDetails.link.meetings");
    	waitForPageToLoad();
    	return new EditMeetingPage(selenium);
    }

    public ViewQuestionResponseDetailPage navigateToViewAdditionalInformation() {
        selenium.click("groupdetail.link.questionGroups");
        waitForPageToLoad();
        return new ViewQuestionResponseDetailPage(selenium);
    }

    public ViewQuestionResponseDetailPage navigateToLatestViewQuestionResponseDetailPage(String questionGroupName) {
        int linkID = Integer.parseInt(selenium.getAttribute("link="+questionGroupName+"@id"));
        linkID++;
        if(!selenium.isElementPresent("id="+linkID)) {
            linkID--;
        }
        selenium.click("id="+linkID);
        waitForPageToLoad();
        return new ViewQuestionResponseDetailPage(selenium);
    }

    public CustomerChangeStatusPage navigateToCustomerChangeStatusPage() {
        selenium.click("viewCenterDetails.link.edit");
        waitForPageToLoad();
        return new CustomerChangeStatusPage(selenium);
    }

    public ViewCenterChargesDetailPage navigateToViewCenterChargesDetailPage(){
        selenium.click("viewCenterDetails.link.viewDetails");
        waitForPageToLoad();
        return new ViewCenterChargesDetailPage(selenium);
    }

    public String getAmountDue(){
        return selenium.getText("viewCenterDetails.text.amountDue");
    }

    public void verifyAmountDue(String amountDue){
        Assert.assertEquals(getAmountDue(), amountDue);
    }
}
