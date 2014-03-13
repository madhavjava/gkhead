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

package org.mifos.reports.business.validator;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.reports.business.ReportParameterForm;

public class ReportParameterValidatorFactoryIntegrationTest extends MifosIntegrationTestCase {

    private static final String COLLECTION_SHEET_REPORT_FILENAME = "report/CollectionSheetReport.rptdesign";
    private static final String DETAILED_AGING_PORTFOLIO_REPORT_FILENAME = "report/DetailedAgingPortfolioAtRisk.rptdesign";
    private static final String BATCH_COLLECTION_SHEET_REPORT_FILENAME = "report/BatchCollectionSheetReport.rptdesign";
    private static final String ACTIVE_LOANS_BY_LOAN_OFFICER_REPORT_FILENAME = "report/ActiveLoansByLoanOfficer.rptdesign";
    private static final String BRANCH_REPORT_FILENAME = "report/ProgressReport.rptdesign";
    private static final String HO_CASH_CONFIRMATION_REPORT_FILENAME = "report/HOCashConfirmationReport.rptdesign";

    @Test
    public void testReturnsNullIfValidatorNotFound() throws Exception {
        ReportParameterValidator<ReportParameterForm> validator = new ReportParameterValidatorFactory()
                .getValidator("not-existing-reportname");
        Assert.assertNull(validator);
    }

    @Test
    public void testReturnsCollectionSheetReportValidator() throws Exception {
        retrieveAndAssertValidatorType(BATCH_COLLECTION_SHEET_REPORT_FILENAME,
                CollectionSheetReportParamValidator.class);
    }

    @Test
    public void testReturnsDetailedAgingPortfolioValidator() throws Exception {
        retrieveAndAssertValidatorType(DETAILED_AGING_PORTFOLIO_REPORT_FILENAME,
                DetailedAgingPortfolioReportParamValidator.class);
    }

    @Test
    public void testReturnsActiveLoansByLoanOfficerValidator() throws Exception {
        retrieveAndAssertValidatorType(ACTIVE_LOANS_BY_LOAN_OFFICER_REPORT_FILENAME,
                DetailedAgingPortfolioReportParamValidator.class);
    }

    @Test
    public void testReturnsBranchReportValidator() throws Exception {
        retrieveAndAssertValidatorType(BRANCH_REPORT_FILENAME, BranchReportParamValidator.class);
    }

    @Test
    public void testReturnsBranchCashConfirmationReportValidator() throws Exception {
        retrieveAndAssertValidatorType(HO_CASH_CONFIRMATION_REPORT_FILENAME,
                BranchCashConfirmationReportParamValidator.class);
    }

    @Test
    public void testReturnsSqlCollectionSheetReportValidator() throws Exception {
        retrieveAndAssertValidatorType(COLLECTION_SHEET_REPORT_FILENAME, SqlCollectionSheetReportParamValidator.class);
    }

    @SuppressWarnings("unchecked")
    private void retrieveAndAssertValidatorType(String reportFilename, Class validatorClass) {
        ReportParameterValidator<ReportParameterForm> validator = new ReportParameterValidatorFactory()
                .getValidator(reportFilename);
        Assert.assertNotNull("Validator not found for " + reportFilename, validator);
       Assert.assertEquals(validatorClass, validator.getClass());
    }
}
