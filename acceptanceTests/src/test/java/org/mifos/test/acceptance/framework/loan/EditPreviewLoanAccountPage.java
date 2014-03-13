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

package org.mifos.test.acceptance.framework.loan;

import org.mifos.test.acceptance.framework.MifosPage;
import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;


public class EditPreviewLoanAccountPage extends MifosPage {
    public EditPreviewLoanAccountPage(Selenium selenium) {
        super(selenium);
        this.verifyPage("EditPreviewLoanAccount");
    }

    public LoanAccountPage submitAndNavigateToLoanAccountPage() {

        selenium.click("editPreviewLoanAccount.button.submit");

        waitForPageToLoad();
        return new LoanAccountPage(selenium);
    }

    public void verifyErrorInForm(String error) {
        Assert.assertTrue(selenium.isTextPresent(error));
    }
    
    public void verifyGLIMPurpose(String purpose, int index) {
        Assert.assertEquals(selenium.getText("xpath=//table[@id='loanAccountDetailsView'][1]/tbody[1]/tr[" + (index+1) + "]/td[5]"), purpose);
    }

    public void verifyGLIMPurpose(String[] purpose) {
        for (int i = 0; i < purpose.length; i++){
            verifyGLIMPurpose(purpose[i], i + 1);
        }
    } 
}