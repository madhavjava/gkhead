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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mifos.framework.business.AbstractEntity;

@Entity
@Table(name = "fund_code")
public class FundCodeEntity extends AbstractEntity {

    @Id
    @GeneratedValue
    private Short fundcodeId;

    private String fundcodeValue;

    public FundCodeEntity(String fundCode) {
        this.fundcodeValue = fundCode;
    }

    protected FundCodeEntity() { }

    public Short getFundCodeId() {
        return fundcodeId;
    }

    public String getFundCodeValue() {
        return fundcodeValue;
    }

    public void setFundCodeId(Short fundCodeId) {
        this.fundcodeId = fundCodeId;
    }

    public void setFundCodeValue(String fundCode) {
        this.fundcodeValue = fundCode;
    }

    @Override
    public boolean equals(Object obj) {
        FundCodeEntity rhs = (FundCodeEntity) obj;
        return new EqualsBuilder().append(this.fundcodeId, rhs.fundcodeId).append(this.fundcodeValue, rhs.fundcodeValue).isEquals();
    }

    @Override
    public int hashCode() {
        int initialNonZeroOddNumber = 7;
        int multiplierNonZeroOddNumber = 7;
        return new HashCodeBuilder(initialNonZeroOddNumber, multiplierNonZeroOddNumber).append(this.fundcodeId).append(this.fundcodeValue).hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(this.fundcodeId).append(" : ").append(this.fundcodeValue).toString();
    }
}