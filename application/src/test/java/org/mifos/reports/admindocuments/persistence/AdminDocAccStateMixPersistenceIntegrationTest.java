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
package org.mifos.reports.admindocuments.persistence;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.reports.admindocuments.business.AdminDocAccStateMixBO;
import org.springframework.beans.factory.annotation.Autowired;

public class AdminDocAccStateMixPersistenceIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    private LegacyAdminDocAccStateMixDao legacyAdminDocAccStateMixDao;

    @Test
    public void testGetAllAdminDocuments() throws PersistenceException {
        List<AdminDocAccStateMixBO> listadmindoc = legacyAdminDocAccStateMixDao.getAllMixedAdminDocuments();
       Assert.assertEquals(0, listadmindoc.size());
    }

    @Test
    public void testGetAdminDocumentById() throws NumberFormatException, PersistenceException {
        List<AdminDocAccStateMixBO> admindoc = legacyAdminDocAccStateMixDao.getMixByAdminDocuments(Short.valueOf("1"));
       Assert.assertEquals(0, admindoc.size());
    }

}
