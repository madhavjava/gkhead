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

package org.mifos.dto.screen;

import java.io.Serializable;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID"}, justification="should disable at filter level and also for pmd - not important for us")
public class CenterSearchInput implements Serializable {

    /** Denotes for which purpose group is using the center search */
    private String groupInput;

    /** Denotes for officeId for which centers are to be searched */
    private Short officeId;

    public CenterSearchInput() {
    }

    public CenterSearchInput(Short officeId, String input) {
        this.officeId = officeId;
        this.groupInput = input;
    }

    /**
     * Return the value of the groupInput attribute.
     *
     * @return String
     */
    public String getGroupInput() {
        return groupInput;
    }

    /**
     * Sets the value of groupInput
     *
     * @param groupInput
     */
    public void setGroupInput(String groupInput) {
        this.groupInput = groupInput;
    }

    /**
     * Return the value of the officeId attribute.
     *
     * @return Short
     */
    public Short getOfficeId() {
        return officeId;
    }

    /**
     * Sets the value of officeId
     *
     * @param officeId
     */
    public void setOfficeId(Short officeId) {
        this.officeId = officeId;
    }
}
