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
import java.math.BigDecimal;

import org.joda.time.DateTime;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID"}, justification="should disable at filter level and also for pmd - not important for us")
public class MonthlyCashFlowDto implements Serializable{

    private final DateTime monthDate;
    private final BigDecimal cumulativeCashFlow;
    private final String notes;
    private final BigDecimal revenue;
    private final BigDecimal expenses;

    public MonthlyCashFlowDto(DateTime monthDate, BigDecimal cumulativeCashFlow, String notes, BigDecimal revenue, BigDecimal expenses) {
        this.monthDate = monthDate;
        this.cumulativeCashFlow = cumulativeCashFlow;
        this.notes = notes;
        this.revenue = revenue;
        this.expenses = expenses;
    }

    public BigDecimal calculateRevenueMinusExpenses() {
        return this.revenue.subtract(this.expenses);
    }
    
    public DateTime getMonthDate() {
        return monthDate;
    }

    public BigDecimal getCumulativeCashFlow() {
        return cumulativeCashFlow;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }
}