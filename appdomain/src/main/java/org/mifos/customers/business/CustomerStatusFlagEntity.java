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

package org.mifos.customers.business;

import java.util.Set;

import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.customers.util.helpers.CustomerStatusFlag;

/**
 * For most purposes, should be replaced by {@link CustomerStatusFlag}.
 */
public class CustomerStatusFlagEntity extends MasterDataEntity {
    private Short blacklisted;
    private CustomerStatusEntity customerStatus;
    private String flagDescription;

    /** The composite primary key value */
    private Short id;

    /** The value of the lookupValue association. */
    private LookUpValueEntity lookUpValue;

    private String statusFlagMessageText = "";

    /*
     * Adding a default constructor is hibernate's requirement and should not be
     * used to create a valid Object.
     */
    protected CustomerStatusFlagEntity() {
        super();
    }

    public CustomerStatusEntity getCustomerStatus() {
        return customerStatus;
    }
    public String getFlagDescription() {
        return flagDescription;
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
        return ApplicationContextProvider.getBean(MessageLookup.class).lookup(getLookUpValue());
    }

    @Override
    public Set<LookUpValueLocaleEntity> getNames() {
        return getLookUpValue().getLookUpValueLocales();
    }

    public boolean isBlackListed() {
        return (blacklisted.equals(YesNoFlag.YES.getValue()));
    }

    public void setFlagDescription(String flagDescription) {
        this.flagDescription = flagDescription;
    }

    protected void setId(Short id) {
        this.id = id;
    }

    protected void setLookUpValue(LookUpValueEntity lookUpValue) {
        this.lookUpValue = lookUpValue;
    }

    public String getStatusFlagMessageText() {
        return this.statusFlagMessageText;
    }

    public void setStatusFlagMessageText(String statusFlagMessageText) {
        this.statusFlagMessageText = statusFlagMessageText;
    }
}
