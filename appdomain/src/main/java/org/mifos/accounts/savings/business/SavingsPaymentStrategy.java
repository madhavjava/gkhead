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
package org.mifos.accounts.savings.business;

import java.util.List;

import org.mifos.accounts.business.AccountPaymentEntity;
import org.mifos.accounts.productdefinition.util.helpers.SavingsType;
import org.mifos.customers.business.CustomerBO;
import org.mifos.framework.util.helpers.Money;

/**
 *
 */
public interface SavingsPaymentStrategy {

    Money makeScheduledPayments(AccountPaymentEntity payment, List<SavingsScheduleEntity> unpaidDepositsForPayingCustomer,
            CustomerBO payingCustomer, SavingsType savingsType, Money savingsBalanceBeforeDeposit);

}
