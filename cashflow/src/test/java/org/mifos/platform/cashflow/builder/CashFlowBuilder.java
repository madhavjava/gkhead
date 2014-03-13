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
package org.mifos.platform.cashflow.builder;

import org.mifos.platform.cashflow.domain.CashFlow;
import org.mifos.platform.cashflow.domain.MonthlyCashFlow;

import java.math.BigDecimal;

public class CashFlowBuilder {
    private CashFlow cashFlow;

    public CashFlowBuilder() {
        cashFlow = new CashFlow();
    }

    public CashFlowBuilder withMonthlyCashFlow(MonthlyCashFlow monthlyCashFlow){
        cashFlow.add(monthlyCashFlow);
        return this;
    }

    public CashFlow build(){
        return cashFlow;
    }

    public CashFlowBuilder withTotalCapital(Double totalCapital) {
        cashFlow.setTotalCapital(new BigDecimal(totalCapital));
        return this;
    }

    public CashFlowBuilder withTotalLiability(Double totalLiability) {
        cashFlow.setTotalLiability(new BigDecimal(totalLiability));
        return this;
    }
}
