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

package org.mifos.application.holiday.persistence;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.application.holiday.util.helpers.RepaymentRuleTypes;
import org.mifos.framework.MifosIntegrationTestCase;

public class AddRepaymentRuleIntegrationTest extends MifosIntegrationTestCase {

    @Test
    public void testValidateLookupValueKey() throws Exception {
        String validKey = "RepaymentRule-NewSameDay";
        String format = "RepaymentRule-";
       Assert.assertTrue(AddRepaymentRule.validateLookupValueKey(format, validKey));
        String invalidKey = "NewSameDay";
        Assert.assertFalse(AddRepaymentRule.validateLookupValueKey(format, invalidKey));
    }

    @Test
    public void testConstructor() throws Exception {
        AddRepaymentRule upgrade = null;
        String invalidKey = "SameDay";

        try {
            // use invalid lookup key format
            upgrade = new AddRepaymentRule(
                    RepaymentRuleTypes.SAME_DAY, invalidKey);
        } catch (Exception e) {
           Assert.assertEquals(e.getMessage(), AddRepaymentRule.wrongLookupValueKeyFormat);
        }

        String goodKey = "RepaymentRule-NextWorkingDayTest";
        // use valid construtor and valid key
        upgrade = new AddRepaymentRule(
                RepaymentRuleTypes.NEXT_WORKING_DAY, goodKey);
    }
}
