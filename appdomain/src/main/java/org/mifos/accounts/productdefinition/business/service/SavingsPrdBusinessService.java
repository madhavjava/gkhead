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

package org.mifos.accounts.productdefinition.business.service;

import java.util.List;

import org.mifos.accounts.productdefinition.business.PrdStatusEntity;
import org.mifos.accounts.productdefinition.business.ProductCategoryBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.persistence.PrdOfferingPersistence;
import org.mifos.accounts.productdefinition.persistence.SavingsProductDao;
import org.mifos.accounts.productdefinition.util.helpers.PrdCategoryStatus;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.security.util.UserContext;

public class SavingsPrdBusinessService implements BusinessService {

    @Override
    public AbstractBusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public SavingsOfferingBO getSavingsProduct(Short prdOfferingId) {
        return getSavingsProductDao().findById(prdOfferingId.intValue());
    }

    public List<ProductCategoryBO> getActiveSavingsProductCategories() throws ServiceException {
        try {
            return getPrdOfferingPersistence().getApplicableProductCategories(ProductType.SAVINGS,
                    PrdCategoryStatus.ACTIVE);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    protected PrdOfferingPersistence getPrdOfferingPersistence() {
        return new PrdOfferingPersistence();
    }

    protected SavingsProductDao getSavingsProductDao() {
        return ApplicationContextProvider.getBean(SavingsProductDao.class);
    }

    public List<PrdStatusEntity> getApplicablePrdStatus(Short localeId) throws ServiceException {
        try {
            return getPrdOfferingPersistence().getApplicablePrdStatus(ProductType.SAVINGS, localeId);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }

    }
}
