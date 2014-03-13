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

import org.mifos.accounts.productdefinition.business.RecommendedAmntUnitEntity;
import org.mifos.customers.business.CustomerBO;

/**
 * I am responsible for providing simplified means of determining what type of savings account
 * is applicable based on the customer and reccomended amount unit.
 */
public class SavingsAccountTypeInspector {

    private final CustomerBO customer;
    private final RecommendedAmntUnitEntity recommendedAmntUnit;

    public SavingsAccountTypeInspector(CustomerBO customer, RecommendedAmntUnitEntity recommendedAmntUnit) {
        this.customer = customer;
        this.recommendedAmntUnit = recommendedAmntUnit;
    }

    public boolean isIndividualSavingsAccount() {
        return customer.isClient() || (customer.isGroup() && recommendedAmntUnit.isCompleteGroup());
    }

    public boolean isJointSavingsAccountWithClientTracking() {
        return customer.isCenter() || (customer.isGroup() && recommendedAmntUnit.isPerIndividual());
    }

}
