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

package org.mifos.security.rolesandpermission.struts.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.security.rolesandpermission.RoleTestUtil;
import org.mifos.security.rolesandpermission.business.ActivityEntity;
import org.mifos.security.rolesandpermission.persistence.LegacyRolesPermissionsDao;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivityTagIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    LegacyRolesPermissionsDao legacyRolesPermissionsDao;

    @Test
    public void testConvertToIdSet() throws Exception {
        Set<Short> activities = new ActivityTag().convertToIdSet(getActivities());
        Assert.assertNotNull(activities);
       Assert.assertEquals(RoleTestUtil.EXPECTED_ACTIVITY_COUNT, activities.size());
    }

    @Test
    public void testGetActivities() throws Exception {
        List<ActivityEntity> activities = new ActivityTag().getActivities(getActivities(), getUiActivities());
        Assert.assertNotNull(activities);
       Assert.assertEquals(2, activities.size());
    }

    private List<ActivityEntity> getActivities() throws Exception {
        return legacyRolesPermissionsDao.getActivities();
    }

    private Map<String, String> getUiActivities() {
        Map<String, String> uiActivities = new HashMap<String, String>();
        uiActivities.put("1", "checkbox");
        uiActivities.put("2", "3");
        uiActivities.put("3", "4");
        return uiActivities;
    }
}
