/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */
package org.mifos.platform.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;

public class DoubleMatcher extends TypeSafeMatcher<Double> {
    private Double value;
    private Double delta;

    public DoubleMatcher(Double value, Double delta) {
        this.value = value;
        this.delta = delta;
    }

    @Override
    public boolean matchesSafely(Double value) {
        Assert.assertEquals(this.value, this.value, delta);
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Double did not match");
    }
}
