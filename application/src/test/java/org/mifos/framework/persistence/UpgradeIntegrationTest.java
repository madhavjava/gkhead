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

package org.mifos.framework.persistence;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mifos.framework.MifosIntegrationTestCase;


public class UpgradeIntegrationTest extends MifosIntegrationTestCase {


    @Before
    public void setUp() {
    }


    @Test
    public void testValidateLookupValueKey() throws Exception {
        String validKey = "Permissions-Groups-CanBlacklistAGroup";
        String format = "Permissions-";
       Assert.assertTrue(DummyUpgrade.validateLookupValueKey(format, validKey));
        String invalidKey = "Groups-CanBlacklistAGroup";
        Assert.assertFalse(DummyUpgrade.validateLookupValueKey(format, invalidKey));
        invalidKey = null;
        Assert.assertFalse(DummyUpgrade.validateLookupValueKey(format, invalidKey));
        invalidKey = "";
        Assert.assertFalse(DummyUpgrade.validateLookupValueKey(format, invalidKey));
    }

}
