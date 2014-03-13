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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.mifos.framework.business.AbstractEntity;

/**
 * This class denotes the currency object. It contains information such as the
 * currency name , the display symbol, Whether this currency has been chosen as
 * the default currency for the MFI. This class is immutable and hence all the
 * setter methods are private. The class is final and the mapping for this class
 * specifies lazy=false so that hibernate doesn't initialize a proxy
 */
@NamedQueries(
 {
  @NamedQuery(
    name="getCurrency",
    query="from MifosCurrency currency where currency.currencyCode = :currencyCode"
  )
 }
)
@Entity
@Table(name = "CURRENCY")
public class MifosCurrency extends AbstractEntity {

    public static final short CEILING_MODE = 1;
    public static final short FLOOR_MODE = 2;
    public static final short HALF_UP_MODE = 3;

    @Id
    @GeneratedValue
    @Column(name = "CURRENCY_ID", nullable = false)
    private Short currencyId;

    /** English multiple-word descriptive name. */
    @Column(name = "CURRENCY_NAME")
    private String currencyName;

    @Column(name = "ROUNDING_AMOUNT")
    private BigDecimal roundingAmount;

    /** ISO 4217 currency code. */
    @Column(name = "CURRENCY_CODE")
    private String currencyCode;

    public MifosCurrency(Short currencyId, String currencyName, BigDecimal roundingAmount, String currencyCode) {
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.roundingAmount = roundingAmount;
        this.currencyCode = currencyCode;
    }

    protected MifosCurrency() {
    }

    public Short getCurrencyId() {
        return this.currencyId;
    }

    public String getCurrencyName() {
        return this.currencyName;
    }

    public BigDecimal getRoundingAmount() {
        return this.roundingAmount;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MifosCurrency other = (MifosCurrency) obj;
        if (currencyId == null) {
            if (other.currencyId != null) {
                return false;
            }
        } else if (!currencyId.equals(other.currencyId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return currencyId == null ? 0 : currencyId.hashCode();
    }

    @Override
    public String toString() {
        return "ID="+currencyId +";Code="+ currencyCode +";Name=" +currencyName;
    }
}