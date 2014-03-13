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

package org.mifos.reports.persistence;

import java.sql.Connection;

import junit.framework.Assert;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.reports.business.ReportsBO;
import org.mifos.reports.business.ReportsCategoryBO;

public class AddReportUpgradeIntegrationTest extends MifosIntegrationTestCase {

    private Transaction transaction;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        Session session = StaticHibernateUtil.getSessionTL();
        connection = session.connection();
    }

    private AddReport createReport() {
        return new AddReport(ReportsCategoryBO.ANALYSIS, "TestReportForUpgrade", "XYZ.rptdesign");
    }

    @Test
    public void testShouldUpgrade() throws Exception {
        AddReport addReport = createReport();
        addReport.upgrade(connection);
        ReportsBO report = new ReportsPersistence().getReport(ReportsCategoryBO.ANALYSIS);
        Assert.assertNotNull(report.getActivityId());
        Assert.assertTrue(report.getIsActive() == (short)1);
    }
}
