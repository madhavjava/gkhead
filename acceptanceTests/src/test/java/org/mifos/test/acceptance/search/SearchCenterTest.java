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
package org.mifos.test.acceptance.search;

import org.junit.Assert;
import org.mifos.test.acceptance.framework.AppLauncher;
import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.search.SearchResultsPage;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:ui-test-context.xml"})
@Test(singleThreaded = true, groups = {"search", "acceptance", "ui", "no_db_unit"})
public class SearchCenterTest extends SearchTestBase {
    private AppLauncher appLauncher;


    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        appLauncher = new AppLauncher(selenium);
    }

    @AfterMethod
    public void tearDown() {
        (new MifosPage(selenium)).logout();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void searchCentersTest() throws Exception {
        String searchPhrase = "center";
        SearchResultsPage page = searchFor(appLauncher, searchPhrase);
        Assert.assertTrue(page.countSearchResults() >= 5);
    }

    // http://mifosforge.jira.com/browse/MIFOSTEST-473
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void searchCenterSpecificTest() throws Exception {
        String searchPhrase = "Default Center";
        SearchResultsPage page = searchFor(appLauncher, searchPhrase);
        page.verifySearchResults(1, "0002-000000001");
    }
}
