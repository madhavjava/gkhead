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

package org.mifos.accounts.productdefinition.persistence;

import java.util.List;

import org.mifos.accounts.productdefinition.business.InterestCalcTypeEntity;
import org.mifos.accounts.productdefinition.business.ProductTypeEntity;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.InterestCalcType;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.application.meeting.business.RecurrenceTypeEntity;
import org.mifos.customers.business.CustomerLevelEntity;
import org.mifos.dto.domain.PrdOfferingDto;

public interface SavingsProductDao {

    SavingsOfferingBO findById(Integer productId);

    SavingsOfferingBO findBySystemId(String globalPrdOfferingNum);

    ProductTypeEntity findSavingsProductConfiguration();

    void save(ProductTypeEntity savingsProductConfiguration);

    List<Object[]> findAllSavingsProducts();

    List<SavingsBO> retrieveSavingsAccountsForPrd(Short prdOfferingId);

    List<RecurrenceTypeEntity> getSavingsApplicableRecurrenceTypes();

    List<SavingsOfferingBO> getAllActiveSavingsProducts();

    List<SavingsOfferingBO> getSavingsOfferingsNotMixed(Short localeId);

    InterestCalcTypeEntity retrieveInterestCalcType(InterestCalcType interestCalcType);

    List<InterestCalcTypeEntity> retrieveInterestCalculationTypes();

    List<PrdOfferingDto> findSavingsProductByCustomerLevel(CustomerLevelEntity customerLevel);

    void save(SavingsOfferingBO savingsProduct);

    void validateProductWithSameNameDoesNotExist(String name);

    void validateProductWithSameShortNameDoesNotExist(String shortName);

    boolean activeOrInactiveSavingsAccountsExistForProduct(Integer productId);
}
