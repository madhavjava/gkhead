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

package org.mifos.customers.office.business.service;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.business.PersonnelFixture;
import org.mifos.dto.domain.OfficeDetailsDto;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class OfficeBusinessServiceIntegrationTest extends MifosIntegrationTestCase {

    private OfficeBusinessService officeBusinessService = new OfficeBusinessService();
    private String officeSearchId;
    private PersonnelBO personnel;

    @Before
    public void setUp() throws Exception {
        officeSearchId = "1.1.";
        personnel = PersonnelFixture.createPersonnel(officeSearchId);
    }

    @Test
    public void testGetOffice() throws Exception {
        Assert.assertNotNull(officeBusinessService.getOffice(TestObjectFactory.HEAD_OFFICE));
    }

    @Test
    public void testGetBranchOffices() throws ServiceException {
        Assert.assertEquals(1, officeBusinessService.getBranchOffices().size());
    }

    @Test
    public void testGetOfficesTillBranchOffice() throws ServiceException {
        Assert.assertEquals(2, officeBusinessService.getOfficesTillBranchOffice().size());
    }

    @Test
    public void testGetChildOffices() throws ServiceException {
        OfficeBO headOffice = TestObjectFactory.getOffice(TestObjectFactory.HEAD_OFFICE);
        List<OfficeDetailsDto> officeList = officeBusinessService.getChildOffices(headOffice.getSearchId());
        Assert.assertEquals(3, officeList.size());
        officeList = null;
        headOffice = null;
    }

    @Test
    public void testGetBranchesUnderUser() throws Exception {
        List<OfficeBO> officeList = officeBusinessService.getActiveBranchesUnderUser(personnel);
        Assert.assertNotNull(officeList);
        Assert.assertEquals(1, officeList.size());
    }

    @Test
    public void testGetAllofficesForCustomFIeld() throws Exception {
        List<OfficeBO> officeList = officeBusinessService.getAllofficesForCustomFIeld();
        Assert.assertNotNull(officeList);
        Assert.assertEquals(3, officeList.size());
    }
}
