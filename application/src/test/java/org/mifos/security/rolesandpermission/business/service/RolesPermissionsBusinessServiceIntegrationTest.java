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

package org.mifos.security.rolesandpermission.business.service;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.security.rolesandpermission.RoleTestUtil;
import org.mifos.security.rolesandpermission.business.RoleBO;

public class RolesPermissionsBusinessServiceIntegrationTest extends MifosIntegrationTestCase {

    RolesPermissionsBusinessService rolesPermissionsBusinessService = new RolesPermissionsBusinessService();

    @After
    public void tearDown() throws Exception {
        StaticHibernateUtil.flushSession();

    }

    @Test
    public void testGetRoles() throws Exception {
       Assert.assertEquals(3, rolesPermissionsBusinessService.getRoles().size());
    }

    @Test
    public void testGetActivities() throws Exception {
       Assert.assertEquals(RoleTestUtil.EXPECTED_ACTIVITY_COUNT, rolesPermissionsBusinessService.getActivities().size());
    }

    @Test
    public void testGetRoleForGivenId() throws Exception {
        RoleBO role = rolesPermissionsBusinessService.getRole(Short.valueOf("1"));
        Assert.assertNotNull(role);
       Assert.assertEquals(RoleTestUtil.EXPECTED_ACTIVITIES_FOR_ROLE, role.getActivities().size());
    }

}
