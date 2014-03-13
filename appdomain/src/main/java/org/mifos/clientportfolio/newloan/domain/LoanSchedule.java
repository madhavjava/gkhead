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

import java.util.List;

import org.mifos.accounts.loan.business.LoanScheduleEntity;
import org.mifos.framework.util.helpers.Money;

public class LoanSchedule {

    private final List<LoanScheduleEntity> roundedLoanSchedules;
    private final Money rawAmount;

    public LoanSchedule(List<LoanScheduleEntity> roundedLoanSchedules, Money rawAmount) {
        this.roundedLoanSchedules = roundedLoanSchedules;
        this.rawAmount = rawAmount;
    }
    
    public void modifyPrincipalAmounts(List<Number> installmentTotalAmounts) {
        int index = 0;
        for (LoanScheduleEntity scheduleEntity : this.roundedLoanSchedules) {
            Money newTotalAmount = new Money(scheduleEntity.getCurrency(), installmentTotalAmounts.get(index).doubleValue());
            Money feesAndInterest = scheduleEntity.getTotalDueWithoutPrincipal();
            Money newPrincipalForInstallment = newTotalAmount.subtract(feesAndInterest);
            scheduleEntity.setPrincipal(newPrincipalForInstallment);
            index++;
        }
    }

    public List<LoanScheduleEntity> getRoundedLoanSchedules() {
        return roundedLoanSchedules;
    }

    public Money getRawAmount() {
        return rawAmount;
    }
}