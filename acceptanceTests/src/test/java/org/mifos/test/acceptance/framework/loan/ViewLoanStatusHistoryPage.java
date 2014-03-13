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

import org.mifos.test.acceptance.framework.AbstractPage;
import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;

public class ViewLoanStatusHistoryPage extends AbstractPage{

    public ViewLoanStatusHistoryPage(Selenium selenium) {
        super(selenium);
        this.verifyPage("ViewLoanStatusHistory");
    }

    public void verifyLastEntryInStatusHistory(String oldStatus, String newStatus) {
        int lastRowId = selenium.getXpathCount("//table[@id='statusHistory']/tbody/tr").intValue() - 1;
        Assert.assertEquals(selenium.getTable("statusHistory." + lastRowId + ".1"), oldStatus);
        Assert.assertEquals(selenium.getTable("statusHistory." + lastRowId + ".2"), newStatus);
    }

    public LoanAccountPage navigateToLoanAccount(){
        selenium.click("id=viewLoanStatusHistory.button.return");
        waitForPageToLoad();
        return new LoanAccountPage(selenium);
    }
}
