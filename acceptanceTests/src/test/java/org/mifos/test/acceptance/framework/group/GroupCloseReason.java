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

package org.mifos.test.acceptance.framework.group;

public enum GroupCloseReason {

    TRANSFERRED("Transferred", 16),
    DUPLICATE("Duplicate", 17),
    BLACKLISTED("Blacklisted", 18),
    LEFT_PROGRAM("Left Program", 19),
    OTHER("Other", 20);

    private final String purposeText;
    private final Integer id;

    private GroupCloseReason(String purposeText, Integer id) {
        this.purposeText = purposeText;
        this.id = id;
    }

    public String getPurposeText() {
        return this.purposeText;
    }

    public Integer getId() {
        return this.id;
    }
}
