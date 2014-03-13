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

package org.mifos.reports.business.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.StandardTestingService;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;
import org.mifos.reports.ui.SelectionItem;
import org.mifos.service.test.TestMode;
import org.mifos.test.framework.util.DatabaseCleaner;
import org.springframework.beans.factory.annotation.Autowired;

public class SelectionItemDaoHibernateIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    private SelectionItemDao selectionItemDao;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private static MifosCurrency oldDefaultCurrency;

    @BeforeClass
    public static void initialiseHibernateUtil() {
        oldDefaultCurrency = Money.getDefaultCurrency();
        Money.setDefaultCurrency(TestUtils.RUPEE);
        new StandardTestingService().setTestMode(TestMode.INTEGRATION);
    }

    @AfterClass
    public static void resetCurrency() {
        Money.setDefaultCurrency(oldDefaultCurrency);
    }

    @After
    public void cleanDatabaseTablesAfterTest() {
        // NOTE: - only added to stop older integration tests failing due to brittleness
        databaseCleaner.clean();
    }

    @Before
    public void cleanDatabaseTables() {
        databaseCleaner.clean();
    }

    @Ignore
    @Test
    public void shouldRetrieveActiveCentersUnderUser() {

        Integer branchId = IntegrationTestObjectMother.sampleBranchOffice().getOfficeId().intValue();
        Integer loanOfficerId = IntegrationTestObjectMother.testUser().getPersonnelId().intValue();

        List<SelectionItem> activeCenters = this.selectionItemDao.getActiveCentersUnderUser(branchId, loanOfficerId);

        assertThat(activeCenters.isEmpty(), is(true));
    }


    @Ignore
    @Test
    public void shouldRetrieveActiveBranchesByBranchSearchId() {

        String officeSearchId = IntegrationTestObjectMother.sampleBranchOffice().getSearchId();

        List<SelectionItem> activeBranches = this.selectionItemDao.getActiveBranchesUnderUser(officeSearchId);

        assertThat(activeBranches.isEmpty(), is(false));
    }

    @Ignore
    @Test
    public void shouldRetrieveActiveLoanOfficersByBranchId() {

        Integer branchId = IntegrationTestObjectMother.sampleBranchOffice().getOfficeId().intValue();

        List<SelectionItem> activeLoanOfficers = this.selectionItemDao.getActiveLoanOfficersUnderOffice(branchId);

        assertThat(activeLoanOfficers.isEmpty(), is(false));
    }
}