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

package org.mifos.test.acceptance.framework.search;

import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.center.CenterViewDetailsPage;
import org.mifos.test.acceptance.framework.client.ClientViewDetailsPage;
import org.mifos.test.acceptance.framework.group.GroupViewDetailsPage;
import org.mifos.test.acceptance.framework.loan.LoanAccountPage;
import org.mifos.test.acceptance.framework.savings.SavingsAccountDetailPage;
import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;

/**
 * This was initially created to represent search results reached from the search box on the home page. There may be
 * other incompatible search results pages.
 */
public class SearchResultsPage extends MifosPage {
    public SearchResultsPage() {
        super();
    }

    public SearchResultsPage(Selenium selenium) {
        super(selenium);
        verifyPage("MainSearchResults");
    }

    public SearchResultsPage verifyPage() {
        verifyPage("MainSearchResults");
        return this;
    }

    public void verifySearchResults(int searchCount) {
        verifySearchResults(searchCount, "");
    }

    public void verifySearchResults(int searchCount, String searchResultID) {
        int count = countSearchResults();
        Assert.assertEquals(count, searchCount);
        if(!"".equals(searchResultID)) {
            Assert.assertTrue(selenium.isTextPresent(searchResultID));
        }
    }

    /**
     * A search result link must be explicitly clicked to land on the "view group details" page.
     * @param locator Selenium locator indicating search result leading to "view group details" page.
     */
    public GroupViewDetailsPage navigateToGroupViewDetailsPage(String locator) {
        selenium.click(locator);
        waitForPageToLoad();
        return new GroupViewDetailsPage(selenium);
    }

    public CenterViewDetailsPage navigateToCenterViewDetailsPage(String locator) {
        selenium.click(locator);
        waitForPageToLoad();
        return new CenterViewDetailsPage(selenium);
    }

    public ClientViewDetailsPage navigateToClientViewDetailsPage(String locator) {
        selenium.click(locator);
        waitForPageToLoad();
        return new ClientViewDetailsPage(selenium);
    }

    public LoanAccountPage navigateToLoanAccountDetailPage(String loanId){
      String xpath = "//a[contains(@href,'globalAccountNum=<loanId>')]";
      selenium.click(xpath.replace("<loanId>", loanId));
      waitForPageToLoad();
      return new LoanAccountPage(selenium);
    }

    public SavingsAccountDetailPage navigateToSavingsAccountDetailPage(String savingsId){
        String xpath = "//a[contains(@href,'globalAccountNum=<savingsId>')]";
        selenium.click(xpath.replace("<savingsId>", savingsId));
        waitForPageToLoad();
        return new SavingsAccountDetailPage(selenium);
      }

    public int countSearchResults() {
        // Get search result data table informations,
        // which should look like "1 to 10 of 40 results"
        String heading = null;
        int resultCount;
        try {
        	String jsScript = "selenium.browserbot.getCurrentWindow().jQuery('#mainCustomerSearchResultDataTable_info').length > 0";
        	selenium.waitForCondition(jsScript, "3000"); // wait for jquery dataTable full load
            heading = selenium.getText("//div[@id='mainCustomerSearchResultDataTable_info']" );
            String[] numbers = heading.replaceAll("\\D+", " ").split(" ");
            resultCount = Integer.parseInt(numbers[numbers.length-1]);
        } catch (Exception e) {
            // failed to retrieve result
            resultCount = -1;
        }
        return resultCount;
    }
}
