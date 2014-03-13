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

package org.mifos.reports.cashconfirmationreport.persistence;

import static org.mifos.framework.util.helpers.MoneyUtils.zero;

import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.AssertionUtils;
import org.mifos.framework.util.CollectionUtils;
import org.mifos.reports.business.service.BranchReportIntegrationTestCase;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationCenterRecoveryBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationDisbursementBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationInfoBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationIssueBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationReportBO;
import org.springframework.beans.factory.annotation.Autowired;

public class BranchCashConfirmationReportPersistenceIntegrationTest extends BranchReportIntegrationTestCase {

    private Session session;

    @Autowired
    private LegacyBranchCashConfirmationReportDao persistence;
    private BranchCashConfirmationReportBO reportBO;
    private BranchCashConfirmationReportBO firstJanReportBO;

    @Override
    @Before
    public void setUp() throws Exception {
        session = StaticHibernateUtil.getSessionTL();
//        transaction = session.beginTransaction();

        reportBO = new BranchCashConfirmationReportBO(BRANCH_ID_SHORT, RUN_DATE);
        firstJanReportBO = new BranchCashConfirmationReportBO(BRANCH_ID_SHORT, FIRST_JAN_2008);
    }

    @Test
    public void testReturnsEmptyListIfReportForGivenDateAndBranchDoesNotExist() throws Exception {
        List<BranchCashConfirmationReportBO> report = persistence.getBranchCashConfirmationReportsForDateAndBranch(
                Short.valueOf("3"), FIRST_JAN_2008);
        Assert.assertNotNull(report);
        Assert.assertTrue(report.isEmpty());
    }

    @Test
    public void testReturnsReportListIfReportForGivenDateAndBranchExist() throws Exception {
        session.save(firstJanReportBO);
        List<BranchCashConfirmationReportBO> report = persistence.getBranchCashConfirmationReportsForDateAndBranch(
                BRANCH_ID_SHORT, FIRST_JAN_2008);
        Assert.assertNotNull(report);
        Assert.assertEquals(1, report.size());
    }

    @Test
    public void testRetrieveIssueReportForGivenDateAndBranch() throws Exception {
        BranchCashConfirmationInfoBO issueBO = new BranchCashConfirmationIssueBO("SOME PRODUCT", zero());
        reportBO.addCenterIssue(issueBO);
        BranchCashConfirmationInfoBO anotherIssue = new BranchCashConfirmationIssueBO("SOMEMORE", zero());
        reportBO.addCenterIssue(anotherIssue);
        session.save(reportBO);
        List<BranchCashConfirmationInfoBO> retrievedIssues = persistence.getCenterIssues(BRANCH_ID_SHORT, RUN_DATE);
        Assert.assertNotNull(retrievedIssues);
        Assert.assertEquals(2, retrievedIssues.size());
        Assert.assertTrue(retrievedIssues.contains(issueBO));
        Assert.assertTrue(retrievedIssues.contains(anotherIssue));
    }

    @Test
    public void testRetrievesCenterRecoveryReport() throws Exception {
        BranchCashConfirmationCenterRecoveryBO recoveryReport = new BranchCashConfirmationCenterRecoveryBO("PRDOFF1",
                zero(), zero(), zero());
        reportBO.addCenterRecovery(recoveryReport);
        session.save(reportBO);
        List<BranchCashConfirmationCenterRecoveryBO> retrievedRecoveryReport = persistence.getCenterRecoveries(
                BRANCH_ID_SHORT, RUN_DATE);
        Assert.assertNotNull(retrievedRecoveryReport);
        Assert.assertEquals(1, retrievedRecoveryReport.size());
        Assert.assertEquals(recoveryReport, retrievedRecoveryReport.get(0));
    }

    @Test
    public void testRetrievesDisbursementsReturnsEmptyListIfNoneExists() throws Exception {
        List<BranchCashConfirmationDisbursementBO> disbursements = persistence.getDisbursements(BRANCH_ID_SHORT,
                RUN_DATE);
        Assert.assertNotNull("retrieved disbursements should not be null", disbursements);
        Assert.assertTrue("retrieved disbursements should be empty", disbursements.isEmpty());
    }

    @Test
    public void testRetrievesDisbursementsForGivenDateAndBranch() throws Exception {
        BranchCashConfirmationDisbursementBO disbursement = new BranchCashConfirmationDisbursementBO("SOME PRODUCT",
                zero());
        BranchCashConfirmationDisbursementBO anotherDisbursement = new BranchCashConfirmationDisbursementBO(
                "ANOTHER PRODUCT", zero());
        reportBO.addDisbursement(disbursement);
        reportBO.addDisbursement(anotherDisbursement);
        session.save(reportBO);
        List<BranchCashConfirmationDisbursementBO> retrievedDisbursements = persistence.getDisbursements(
                BRANCH_ID_SHORT, RUN_DATE);
        Assert.assertNotNull("retrieved disbursements should not be null", retrievedDisbursements);
        Assert.assertFalse("retrieved disbursements should not be empty", retrievedDisbursements.isEmpty());
        AssertionUtils.assertSameCollections(CollectionUtils.asList(disbursement, anotherDisbursement),
                retrievedDisbursements);
    }
}