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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.mifos.framework.util.helpers.MoneyUtils.zero;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mifos.customers.office.business.service.OfficeBusinessService;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationDisbursementBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationInfoBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationIssueBO;
import org.mifos.reports.cashconfirmationreport.BranchCashConfirmationReportBO;
import org.mifos.reports.cashconfirmationreport.persistence.LegacyBranchCashConfirmationReportDao;

public class BranchCashConfirmationReportServiceIntegrationTest extends BranchReportIntegrationTestCase {

    private LegacyBranchCashConfirmationReportDao persistenceMock;
    private BranchCashConfirmationReportService service;

    @Test
    public void testGetCenterIssues() throws ServiceException {
        BranchCashConfirmationReportBO reportBO = new BranchCashConfirmationReportBO(BRANCH_ID_SHORT, RUN_DATE);
        BranchCashConfirmationInfoBO issueBO = new BranchCashConfirmationIssueBO("SOME PRODUCT", zero());
        reportBO.addCenterIssue(issueBO);
        BranchCashConfirmationInfoBO anotherIssue = new BranchCashConfirmationIssueBO("SOMEMORE", zero());
        reportBO.addCenterIssue(anotherIssue);
        Session session = StaticHibernateUtil.getSessionTL();
//        Transaction transaction = session.beginTransaction();
        session.save(reportBO);
        List<BranchCashConfirmationInfoBO> centerIssues = ReportServiceFactory.getBranchCashConfirmationReportService(
                null).getCenterIssues(BRANCH_ID, RUN_DATE_STR);
        Assert.assertNotNull(centerIssues);
       Assert.assertEquals(2, centerIssues.size());
       Assert.assertTrue(centerIssues.contains(issueBO));
       Assert.assertTrue(centerIssues.contains(anotherIssue));
//        transaction.rollback();
    }

    @Test
    public void testGetDisbursements() throws Exception {
        expect(persistenceMock.getDisbursements(BRANCH_ID_SHORT, RUN_DATE)).andReturn(
                new ArrayList<BranchCashConfirmationDisbursementBO>());
        replay(persistenceMock);
        service.getDisbursements(BRANCH_ID, RUN_DATE_STR);
        verify(persistenceMock);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        persistenceMock = createMock(LegacyBranchCashConfirmationReportDao.class);
        service = new BranchCashConfirmationReportService(persistenceMock, new OfficeBusinessService());
    }
}
