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

package org.mifos.test.acceptance.framework.customer;

import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.center.CenterViewDetailsPage;
import org.mifos.test.acceptance.framework.client.ClientViewDetailsPage;
import org.mifos.test.acceptance.framework.group.GroupViewDetailsPage;
import org.mifos.test.acceptance.framework.questionnaire.QuestionResponsePage;

import com.thoughtworks.selenium.Selenium;

public class CustomerChangeStatusPreviewPage extends MifosPage {

    public CustomerChangeStatusPreviewPage(Selenium selenium) {
        super(selenium);
        this.verifyPage("CustomerChangeStatusPreview");
    }

    public ClientViewDetailsPage submitAndGotoClientViewDetailsPage() {
        selenium.click("customerchangeStatusPreview.button.submit");
        waitForPageToLoad();
        return new ClientViewDetailsPage(selenium);
    }

    public CenterViewDetailsPage submitAndNavigateToCenterViewDetailsPage() {
        selenium.click("customerchangeStatusPreview.button.submit");
        waitForPageToLoad();
        return new CenterViewDetailsPage(selenium);
    }

    public GroupViewDetailsPage navigateToGroupDetailsPage() {
        selenium.click("customerchangeStatusPreview.button.submit");
        waitForPageToLoad();
        return new GroupViewDetailsPage(selenium);
    }

    public QuestionResponsePage navigateToEditAdditionalInformation() {
        selenium.click("editQuestionResponses_button");
        waitForPageToLoad();
        return new QuestionResponsePage(selenium);
    }

    public ClientViewDetailsPage cancelAndGotoClientViewDetailsPage() {
        selenium.click("customerchangeStatusPreview.button.cancel");
        waitForPageToLoad();
        return new ClientViewDetailsPage(selenium);
    }
}
