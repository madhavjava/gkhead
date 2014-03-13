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

package org.mifos.framework.components.batchjobs.helpers;

import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.reports.branchreport.BranchReportBO;
import org.mifos.reports.branchreport.BranchReportBOFixture;
import org.mifos.reports.branchreport.BranchReportStaffingLevelSummaryBO;
import org.mifos.reports.business.service.BranchReportService;

@SuppressWarnings("unchecked")
public class BranchReportStaffingLevelSummaryHelperIntegrationTest extends MifosIntegrationTestCase {

    public static final Short BRANCH_ID = Short.valueOf("2");

    @Test
    public void testPopulateStaffingLevelSummary() throws Exception {
        BranchReportBO branchReport = BranchReportBOFixture.createBranchReport(Integer.valueOf(1), BRANCH_ID, DateUtils
                .currentDate());
        new BranchReportStaffingLevelSummaryHelper(branchReport, new BranchReportService())
                .populateStaffingLevelSummary();
        assertStaffingLevelSummaries(branchReport);
    }

    private void assertStaffingLevelSummaries(BranchReportBO branchReport) {
        Set<BranchReportStaffingLevelSummaryBO> staffingLevelSummaries = branchReport.getStaffingLevelSummaries();
       Assert.assertEquals(1, staffingLevelSummaries.size());
        Collection retrievedRolenames = CollectionUtils.collect(staffingLevelSummaries, new Transformer() {
            @Override
			public Object transform(Object input) {
                return ((BranchReportStaffingLevelSummaryBO) input).getTitleName();
            }
        });
       Assert.assertEquals(1, retrievedRolenames.size());
       Assert.assertTrue(retrievedRolenames.contains(BranchReportStaffingLevelSummaryBO.TOTAL_STAFF_ROLE_NAME));
    }
}
