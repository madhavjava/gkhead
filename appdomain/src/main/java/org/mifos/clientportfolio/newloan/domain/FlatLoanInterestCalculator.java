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

package org.mifos.clientportfolio.newloan.domain;

import java.math.BigDecimal;

import org.mifos.framework.util.helpers.Money;

public class FlatLoanInterestCalculator implements LoanInterestCalculator {

    @Override
    public Money calculate(LoanInterestCalculationDetails loanInterestCalculationDetails) {
        Money loanAmount = loanInterestCalculationDetails.getLoanAmount();
        Double interestRate = loanInterestCalculationDetails.getInterestRate();
        Double durationInYears = loanInterestCalculationDetails.getDurationInYears();

        // FIXME - keithw - the calls to Money.multiply() and Money.divide() round prematurely
        return loanAmount.multiply(interestRate).multiply(durationInYears).divide(new BigDecimal("100"));
    }

}
