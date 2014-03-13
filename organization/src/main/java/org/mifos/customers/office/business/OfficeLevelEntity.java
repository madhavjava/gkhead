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

package org.mifos.customers.office.business;

import java.util.Set;

import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.customers.office.util.helpers.OfficeLevel;

/**
 * As with the other *Entity classes, this one corresponds to
 * {@link OfficeLevel}. The extra complication is that with office levels, the
 * user can configure which are in use (for example, are there subregional
 * offices?). The <b>configured</b> flag is set/clear for levels which
 * are/are-not in use.
 */
public class OfficeLevelEntity extends MasterDataEntity {

    private final OfficeLevelEntity parent;

    private final OfficeLevelEntity child;

    private Short configured;

    private Short interactionFlag;

    public OfficeLevelEntity(OfficeLevel level) {
        this.id = level.getValue();
        parent = null;
        child = null;
    }

    protected OfficeLevelEntity() {
        parent = null;
        child = null;
    }

    public boolean isConfigured() {
        return this.configured > 0;
    }

    private void addConfigured(boolean configured) {
        this.configured = (short) (configured ? 1 : 0);
    }

    public boolean isInteractionFlag() {
        return this.interactionFlag > 0;
    }

    public OfficeLevelEntity getParent() {
        return parent;
    }

    public OfficeLevelEntity getChild() {
        return child;
    }

    public OfficeLevel getLevel() {
        return OfficeLevel.getOfficeLevel(this.getId());
    }

    public void updateTo(boolean configuredChange) {
        addConfigured(configuredChange);
    }

    /** The composite primary key value */
    private Short id;

    /** The value of the lookupValue association. */
    private LookUpValueEntity lookUpValue;

    private String name;

    @Override
    public Short getId() {
        return id;
    }

    protected void setId(Short id) {
        this.id = id;
    }

    @Override
    public LookUpValueEntity getLookUpValue() {
        return lookUpValue;
    }

    protected void setLookUpValue(LookUpValueEntity lookUpValue) {
        this.lookUpValue = lookUpValue;
    }

    @Override
    public String getName() {
        return this.name;
//        return ApplicationContextProvider.getBean(MessageLookup.class).lookup(getLookUpValue());
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<LookUpValueLocaleEntity> getNames() {
        return getLookUpValue().getLookUpValueLocales();
    }
}