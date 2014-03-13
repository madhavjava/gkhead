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

package org.mifos.customers.personnel.business;

import java.util.Set;

import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.customers.personnel.util.helpers.PersonnelLevel;

/**
 * See also {@link PersonnelLevel}.
 */
public class PersonnelLevelEntity extends MasterDataEntity {

    /** The composite primary key value */
    private Short id;

    private Short interactionFlag;

    /** The value of the lookupValue association. */
    private LookUpValueEntity lookUpValue;

    private final PersonnelLevelEntity parent;

    private String name;

    protected PersonnelLevelEntity() {
        super();
        this.parent = null;

    }

    public PersonnelLevelEntity(PersonnelLevel level) {
        this.id = level.getValue();
        this.parent = null;
    }

    @Override
    public Short getId() {
        return id;
    }

    @Override
    public LookUpValueEntity getLookUpValue() {
        return lookUpValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<LookUpValueLocaleEntity> getNames() {
        return getLookUpValue().getLookUpValueLocales();
    }

    public PersonnelLevelEntity getParent() {
        return parent;
    }

    public boolean isInteractionFlag() {
        return this.interactionFlag > 0;
    }

    protected void setId(Short id) {
        this.id = id;
    }

    protected void setLookUpValue(LookUpValueEntity lookUpValue) {
        this.lookUpValue = lookUpValue;
    }

    @Override
    public String toString() {
        return PersonnelLevel.fromInt(getId()).toString();
    }
}
