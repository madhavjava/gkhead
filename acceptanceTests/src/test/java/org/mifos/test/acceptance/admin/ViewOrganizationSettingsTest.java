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

package org.mifos.test.acceptance.admin;


import org.mifos.test.acceptance.framework.AppLauncher;
import org.mifos.test.acceptance.framework.HomePage;
import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.UiTestCaseBase;
import org.mifos.test.acceptance.framework.admin.AdminPage;
import org.mifos.test.acceptance.framework.admin.ViewOrganizationSettingsPage;
import org.mifos.test.acceptance.framework.testhelpers.CustomPropertiesHelper;
import org.mifos.test.acceptance.util.ApplicationDatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

@ContextConfiguration(locations = { "classpath:ui-test-context.xml" })
@Test(singleThreaded = true, groups = {"acceptance","ui","no_db_unit"})

public class ViewOrganizationSettingsTest extends UiTestCaseBase {

    private AppLauncher appLauncher;
    @Autowired
    private ApplicationDatabaseOperation applicationDatabaseOperation;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        appLauncher = new AppLauncher(selenium);
        applicationDatabaseOperation.updateGLIM(0);
        applicationDatabaseOperation.updateLSIM(0);
    }

    @AfterMethod
    public void tearDown() throws SQLException {
        (new MifosPage(selenium)).logout();
    }

    @Test
    //http://mifosforge.jira.com/browse/MIFOSTEST-259
    public void verifyViewOrganizationSettingsPage() {
        //When
        AdminPage adminPage = loginAndGoToAdminPage();
        ViewOrganizationSettingsPage viewOrganizationSettingsPage = adminPage.navigateToViewOrganizationSettingsPage();
        viewOrganizationSettingsPage.verifyPage();
        //Then
        viewOrganizationSettingsPage.verifyDefaultConfiguration();
        //When
        CustomPropertiesHelper customPropertiesHelper = new CustomPropertiesHelper(selenium);
        customPropertiesHelper.setDigitsAfterDecimal(3);
        customPropertiesHelper.setGroupCanApplyLoans("false");

        adminPage = loginAndGoToAdminPage();
        viewOrganizationSettingsPage = adminPage.navigateToViewOrganizationSettingsPage();
        //Then
        viewOrganizationSettingsPage.verifyClientRules(new String[]{"Groups allowed to apply for loans: No"});
        viewOrganizationSettingsPage.verifyCurrencies(new String[]{"Number of digits after decimal: 3"});

        customPropertiesHelper.setDigitsAfterDecimal(1);
        customPropertiesHelper.setGroupCanApplyLoans("true");
    }
    private AdminPage loginAndGoToAdminPage() {
        HomePage homePage = appLauncher.launchMifos().loginSuccessfullyUsingDefaultCredentials();
        homePage.verifyPage();
        AdminPage adminPage = homePage.navigateToAdminPage();
        adminPage.verifyPage();
        return adminPage;
    }

}

