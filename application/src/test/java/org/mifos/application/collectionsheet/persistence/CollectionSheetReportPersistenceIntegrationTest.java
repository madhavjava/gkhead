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

package org.mifos.application.collectionsheet.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.reports.business.dto.CollectionSheetReportData;
import org.springframework.beans.factory.annotation.Autowired;

public class CollectionSheetReportPersistenceIntegrationTest extends MifosIntegrationTestCase {

    private static final Integer CENTER_ID = Integer.valueOf("2");
    private static final Integer PERSONNEL_ID = Integer.valueOf("1");
    private static final Date MEETING_DATE = DateUtils.getDate(2007, Calendar.JUNE, 22);
    private static final Integer BRANCH_ID = Integer.valueOf("12");

    @Autowired
    BirtCollectionSheetReportDao birtCollectionSheetReportDao;

    @Test
    public void testReportQueryIsValid() throws Exception {
        try {
            birtCollectionSheetReportDao.extractReportData(BRANCH_ID, MEETING_DATE, PERSONNEL_ID, CENTER_ID);
        } catch (Exception e) {
            Assert.fail("Collection Sheet Report Query should not throw exception");
        }
    }

    @Test
    public void testConvertResultToDTO() throws Exception {
        ArrayList<Object[]> results = new ArrayList<Object[]>();
        results.add(new Object[] { "22-06-2007", 12, " Office 12", 4, " system user 4", 19, " center-19", 199,
                "group-199", 833, "client-833", "GL:11550||EL:200||SPL:4200", BigDecimal.valueOf(15950.0), "0",
                BigDecimal.ZERO, "0", BigDecimal.ZERO, "0", BigDecimal.ZERO });
        results.add(new Object[] { "22-06-2007", 14, " Office 13", 4, " system user 5", 19, " center-29", 199,
                "group-199", 833, "client-833", "GL:11550||EL:200||SPL:4200", BigDecimal.valueOf(15950.0), "0",
                BigDecimal.ZERO, "0", BigDecimal.ZERO, "0", BigDecimal.ZERO });
        List<CollectionSheetReportData> reportData = birtCollectionSheetReportDao.convertResultToDTO(results);
        Assert.assertEquals(2, reportData.size());
    }

}
