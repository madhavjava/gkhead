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

import org.mifos.accounts.productdefinition.business.PrdCategoryStatusEntity;
import org.mifos.accounts.productdefinition.business.ProductCategoryBO;
import org.mifos.accounts.productdefinition.business.ProductTypeEntity;
import org.mifos.accounts.productdefinition.persistence.LegacyProductCategoryDao;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.security.util.UserContext;

public class ProductCategoryBusinessService implements BusinessService {

    @Override
    public AbstractBusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public List<ProductTypeEntity> getProductTypes() throws ServiceException {
        try {
            return getProductCategoryPersistence().getProductTypes();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public ProductTypeEntity getProductType(Short id) throws ServiceException {
        try {
            return getProductCategoryPersistence().getProductTypes(id);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public ProductCategoryBO findByGlobalNum(String globalNum) throws ServiceException {
        try {
            return getProductCategoryPersistence().findByGlobalNum(globalNum);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    protected LegacyProductCategoryDao getProductCategoryPersistence() {
        return ApplicationContextProvider.getBean(LegacyProductCategoryDao.class);
    }

    public List<PrdCategoryStatusEntity> getProductCategoryStatusList() throws ServiceException {
        try {
            return getProductCategoryPersistence().getProductCategoryStatusList();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public List<ProductCategoryBO> getAllCategories() throws ServiceException {
        try {
            return getProductCategoryPersistence().getAllCategories();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

}
