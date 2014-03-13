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

public class FeeUpdateRequest {

    private final Short feeId;
    private final Short currencyId;
    private final String amount;
    private final Short feeStatusValue;
    private final Double rateValue;

    public FeeUpdateRequest(Short feeId, Short currencyId, String amount, Short feeStatusValue, Double rateValue) {
        this.feeId = feeId;
        this.currencyId = currencyId;
        this.amount = amount;
        this.feeStatusValue = feeStatusValue;
        this.rateValue = rateValue;
    }

    public Short getFeeId() {
        return feeId;
    }

    public Short getCurrencyId() {
        return currencyId;
    }

    public String getAmount() {
        return amount;
    }

    public Short getFeeStatusValue() {
        return feeStatusValue;
    }

    public Double getRateValue() {
        return rateValue;
    }
}