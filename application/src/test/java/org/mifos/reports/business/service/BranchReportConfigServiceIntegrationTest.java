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

package org.mifos.reports.business.service;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.util.helpers.FilePaths;
import org.springframework.core.io.ClassPathResource;

public class BranchReportConfigServiceIntegrationTest extends MifosIntegrationTestCase {

    private BranchReportConfigService branchReportConfigService;

    public void testGetDaysInArrears() throws ServiceException {
        Integer daysInArrears = branchReportConfigService.getGracePeriodDays();
        Assert.assertNotNull(daysInArrears);
    }

    @Test
    public void testGetLoanCyclePeriod() throws Exception {
        Integer loanCyclePeriod = branchReportConfigService.getLoanCyclePeriod();
        Assert.assertNotNull(loanCyclePeriod);
    }

    @Test
    public void testGetReplacementFieldId() throws Exception {
        Short replacementFieldId = branchReportConfigService.getReplacementFieldId();
        Assert.assertNotNull(replacementFieldId);
    }

    @Test
    public void testGetReplacementFieldValue() throws Exception {
        String replacementFieldValue = branchReportConfigService.getReplacementFieldValue();
        Assert.assertNotNull(replacementFieldValue);
    }

    @Test
    public void testGetCurrency() throws Exception {
        MifosCurrency currency = branchReportConfigService.getCurrency();
        Assert.assertNotNull(currency);
    }

    @Test
    public void testGetDaysInArrearsForRisk() throws Exception {
        Integer daysInArrearsForRisk = branchReportConfigService.getDaysInArrearsForRisk();
        Assert.assertNotNull(daysInArrearsForRisk);
    }

    @Before
    public void setUp() throws Exception {
        ClassPathResource branchReportConfig = new ClassPathResource(FilePaths.BRANCH_REPORT_CONFIG);
        branchReportConfigService = new BranchReportConfigService(branchReportConfig);
    }
}
