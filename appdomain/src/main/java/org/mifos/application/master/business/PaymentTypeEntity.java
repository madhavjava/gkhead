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

package org.mifos.application.master.business;

import java.util.Set;

import org.mifos.application.master.MessageLookup;
import org.mifos.application.servicefacade.ApplicationContextProvider;


public class PaymentTypeEntity extends MasterDataEntity {

    /** The composite primary key value */
    private Short id;

    /** The value of the lookupValue association. */
    private LookUpValueEntity lookUpValue;

    public PaymentTypeEntity() {
        super();
    }

    public PaymentTypeEntity(LookUpValueEntity lookUpValueEntity) {
        this.lookUpValue = lookUpValueEntity;
    }

    /**
     * Creates a PaymentTypeEntity with just an id and no associated
     * name.  Beware that this cannot be used in a situation where getName()
     * may be called.  getName() may be called by the audit logger,
     * so this should not be used if the audit logger may be called.
     *
     */
    public PaymentTypeEntity(Short id) {
        this.id = id;
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
        String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(getLookUpValue());
        return name;

    }

    @Override
    public Set<LookUpValueLocaleEntity> getNames() {
        return getLookUpValue().getLookUpValueLocales();
    }

    protected void setId(Short id) {
        this.id = id;
    }

    protected void setLookUpValue(LookUpValueEntity lookUpValue) {
        this.lookUpValue = lookUpValue;
    }


    protected void setName(String name) {
        ApplicationContextProvider.getBean(MessageLookup.class).updateLookupValue(getLookUpValue(), name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
