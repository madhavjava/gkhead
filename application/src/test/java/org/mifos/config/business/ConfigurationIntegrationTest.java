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

package org.mifos.config.business;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.config.AccountingRules;
import org.mifos.config.ClientRules;
import org.mifos.config.FiscalCalendarRules;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.TestObjectFactory;

/**
 * Most of this class is a remnant of per-office configuration, which <a
 * href="http://article.gmane.org/gmane.comp.finance.mifos.devel/3498">is
 * deprecated and may be removed</a> (-Adam 22-JAN-2008).
 */
public class ConfigurationIntegrationTest extends MifosIntegrationTestCase {

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        configuration = Configuration.getInstance();
    }

    @After
    public void testSystemConfiguration() throws Exception {
        SystemConfiguration systemConfig = configuration.getSystemConfig();
        Assert.assertNotNull(systemConfig);
        Assert.assertNotNull(systemConfig.getCurrency());
        Assert.assertNotNull(systemConfig.getMifosTimeZone());
    }

    @Test
    public void testHeadOfficeConfiguration() throws Exception {
        OfficeBO headOffice = new OfficePersistence().getHeadOffice();
        OfficeConfig officeConfig = configuration.getOfficeConfig(headOffice.getOfficeId());
        assertForAccountConfig(officeConfig.getAccountConfig());
    }

    @Test
    public void testAreaOfficeConfiguration() throws Exception {
        OfficeBO areaOffice = new OfficePersistence().getOffice(TestObjectFactory.SAMPLE_AREA_OFFICE);
        OfficeConfig officeConfig = configuration.getOfficeConfig(areaOffice.getOfficeId());
        assertForAccountConfig(officeConfig.getAccountConfig());
    }

    @Test
    public void testBranchOfficeConfiguration() throws Exception {
        OfficeBO branchOffice = new OfficePersistence().getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        OfficeConfig officeConfig = configuration.getOfficeConfig(branchOffice.getOfficeId());
        assertForAccountConfig(officeConfig.getAccountConfig());
    }

    @Test
    public void testClientRules() throws Exception {
       Assert.assertEquals(true, ClientRules.getCenterHierarchyExists().booleanValue());
       Assert.assertEquals(true, ClientRules.getClientCanExistOutsideGroup());
       Assert.assertEquals(true, ClientRules.getGroupCanApplyLoans().booleanValue());
    }

    private void assertForAccountConfig(AccountConfig accountConfig) {
       Assert.assertEquals(Short.valueOf("10"), accountConfig.getLatenessDays());
       Assert.assertEquals(Short.valueOf("30"), accountConfig.getDormancyDays());
       Assert.assertEquals(true, AccountingRules.isBackDatedTxnAllowed());
    }

    @Test
    public void testFiscalCalendarRules() {
       Assert.assertEquals(Short.valueOf("2"), new FiscalCalendarRules().getStartOfWeek());
       Assert.assertEquals("same_day", new FiscalCalendarRules().getScheduleMeetingIfNonWorkingDay());
       enableCustomWorkingDays();
       List<Short> weekOffs = new FiscalCalendarRules().getWeekDayOffList();
       Assert.assertEquals(weekOffs.size(), 0);
    }
}
