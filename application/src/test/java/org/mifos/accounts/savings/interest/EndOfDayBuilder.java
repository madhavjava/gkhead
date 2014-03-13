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

package org.mifos.accounts.savings.interest;

import org.joda.time.LocalDate;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.Money;

public class EndOfDayBuilder {

    private LocalDate date = new LocalDate();
    private Money deposits = TestUtils.createMoney("0");
    private Money withdrawals = TestUtils.createMoney("0");
    private Money interest = TestUtils.createMoney("0");

    public EndOfDayDetail build() {
        return new EndOfDayDetail(date, deposits, withdrawals, interest);
    }

    public EndOfDayBuilder on(LocalDate localDate) {
        this.date = localDate;
        return this;
    }

    public EndOfDayBuilder withDespoitsOf(String depositsOf) {
        this.deposits = TestUtils.createMoney(depositsOf);
        return this;
    }

    public EndOfDayBuilder withWithdrawalsOf(String withdrawalsOf) {
        this.withdrawals = TestUtils.createMoney(withdrawalsOf);
        return this;
    }

    public EndOfDayBuilder withInterestOf(String interestOf) {
        this.interest = TestUtils.createMoney(interestOf);
        return this;
    }

}
