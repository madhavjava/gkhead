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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.framework.business.AbstractEntity;

/**
 * This class depicts the different account types. Obsolete; replaced by
 * {@link AccountTypes}.
 */
@Entity
@Table(name = "account_type")
public class AccountTypeEntity extends AbstractEntity {

    @Id
    @GeneratedValue
    private Short accountTypeId;

    private String description;

    @NotNull
    private Integer lookupId;

    public AccountTypeEntity() {
    }

    public AccountTypeEntity(Integer lookUpId) {
        this.lookupId = lookUpId;
    }

    public AccountTypeEntity(String description, Integer lookUpId) {
        this.description = description;
        this.lookupId = lookUpId;
    }

    public AccountTypeEntity(Short accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public Short getAccountTypeId() {
        return this.accountTypeId;
    }

    public void setAccountTypeId(Short accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLookUpId() {
        return this.lookupId;
    }

    public void setLookUpId(Integer lookUpId) {
        this.lookupId = lookUpId;
    }

}
