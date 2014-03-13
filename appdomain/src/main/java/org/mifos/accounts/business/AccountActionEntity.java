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

package org.mifos.accounts.business;

import java.util.Set;

import org.mifos.accounts.util.helpers.AccountActionTypes;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.business.AbstractEntity;

/**
 * Also see {@link AccountActionTypes}.
 */
public class AccountActionEntity extends AbstractEntity {

    /** The composite primary key value */
    private Short id;

    /** The value of the lookupValue association. */
    private LookUpValueEntity lookUpValue;

    public AccountActionEntity() {
    }

    public AccountActionEntity(AccountActionTypes myEnum) {
        this.id = myEnum.getValue();
    }

    public AccountActionTypes asEnum() {
        return AccountActionTypes.fromInt(getId());
    }

    public Short getId() {
        return id;
    }

    public LookUpValueEntity getLookUpValue() {
        return lookUpValue;
    }

    public String getName() {
        String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(getLookUpValue());
        return name;

    }

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
        return AccountActionTypes.fromInt(getId()).toString();
    }

    public boolean isSavingsAdjustment() {
        return AccountActionTypes.SAVINGS_ADJUSTMENT.getValue().equals(this.id);
    }

    public boolean isSavingsDeposit() {
        return AccountActionTypes.SAVINGS_DEPOSIT.getValue().equals(this.id);
    }

    public boolean isSavingsWithdrawal() {
        return AccountActionTypes.SAVINGS_WITHDRAWAL.getValue().equals(this.id);
    }
    
    public boolean isSavingsInterestPosting() {
        return AccountActionTypes.SAVINGS_INTEREST_POSTING.getValue().equals(this.id);
    }
    
    public boolean isLoanDisbursal() {
        return AccountActionTypes.DISBURSAL.getValue().equals(this.id);
    }
}