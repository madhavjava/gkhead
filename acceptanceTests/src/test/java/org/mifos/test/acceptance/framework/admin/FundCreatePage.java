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

package org.mifos.test.acceptance.framework.admin;

import org.mifos.test.acceptance.framework.AbstractPage;
import com.thoughtworks.selenium.Selenium;

public class FundCreatePage extends AbstractPage {

    public FundCreatePage submitPage() {
        return this;
    }

    public FundCreatePage(Selenium selenium) {
        super(selenium);
    }

    public void verifyPage() {
        this.verifyPage("fundcreate"); // dodać <span id="page.id" title="fundcreate" />

    }

    public static class CreateFundSubmitParameters {

        private String fundName;
        private String fundCode;

        public String getFundName() {
            return this.fundName;
        }

        public void setFundName(String fundName) {
            this.fundName = fundName;
        }

        public String getFundCode() {
            return this.fundCode;
        }

        public void setFundCode(String fundCode) {
            this.fundCode = fundCode;
        }

    }

    public CreateNewFundConfirmationPage submitAndNavigateToNewFundConfirmationPage(CreateFundSubmitParameters formParameters) {
        selenium.type("name",formParameters.getFundName());
        selenium.select("codeId", "label=" + formParameters.getFundCode());
        selenium.click("preview");
        waitForPageToLoad();

        return new CreateNewFundConfirmationPage(selenium);

    }

}