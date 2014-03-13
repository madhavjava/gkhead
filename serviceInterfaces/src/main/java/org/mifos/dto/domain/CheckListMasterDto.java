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

package org.mifos.dto.domain;

import java.io.Serializable;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification="should disable at filter level and also for pmd - not important for us")
public class CheckListMasterDto implements Serializable {

    private boolean customer;
    private String lookupKey;
    private Short masterTypeId;
    private String masterTypeName;

    public CheckListMasterDto(Short id, String lookupKey) {
        this.masterTypeId = id;
        this.lookupKey = lookupKey;
    }

    public Short getMasterTypeId() {
        return masterTypeId;
    }

    public String getMasterTypeName() {
        return this.masterTypeName;
    }

    public void setMasterTypeName(String masterTypeName) {
        this.masterTypeName = masterTypeName;
    }

    public boolean isCustomer() {
        return this.customer;
    }

    public void setCustomer(boolean customer) {
        this.customer = customer;
    }

    public String getLookupKey() {
        return this.lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    public void setMasterTypeId(Short masterTypeId) {
        this.masterTypeId = masterTypeId;
    }
}