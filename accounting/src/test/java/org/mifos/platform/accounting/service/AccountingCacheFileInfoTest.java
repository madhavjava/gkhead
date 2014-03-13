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

package org.mifos.platform.accounting.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountingCacheFileInfoTest {
    DateTimeZone savedZone;

    @Before
    public void setUp() {
        savedZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(savedZone);
    }

    @Test
    public void testAccountingCacheFileInfo() {
        DateTime dateTime = new DateTime(2010, 05, 07, 5, 1, 20, 0);
        ExportFileInfo a = new ExportFileInfo(dateTime.toString("yyyy-MMM-dd HH:mm:sss z"),
                "Mifos Accounting Export 2010-05-05 to 2010-06-07", "2010-05-05", "2010-06-07", true);
        Assert.assertEquals(dateTime.toString("yyyy-MMM-dd HH:mm:sss z"), a.getLastModified());
        Assert.assertEquals("2010-05-05", a.getStartDate());
        Assert.assertEquals("2010-06-07", a.getEndDate());
        Assert.assertEquals("Mifos Accounting Export 2010-05-05 to 2010-06-07", a.getFileName());
        Assert.assertEquals(true, a.getIsExistInCache());
    }
}
