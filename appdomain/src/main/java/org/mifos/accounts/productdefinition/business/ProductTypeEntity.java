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

package org.mifos.accounts.productdefinition.business;

import org.mifos.accounts.productdefinition.exceptions.ProductDefinitionException;
import org.mifos.accounts.productdefinition.persistence.LoanPrdPersistence;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.business.AbstractEntity;
import org.mifos.framework.exceptions.PersistenceException;

public class ProductTypeEntity extends AbstractEntity {

    private Short productTypeID;

    private LookUpValueEntity lookUpValue;

    private Short latenessDays;

    private Short dormancyDays;

    private int versionNo;

    public ProductTypeEntity(Short prdTypeId) {
        super();
        this.productTypeID = prdTypeId;
    }

    protected ProductTypeEntity() {
        super();
    }

    public LookUpValueEntity getLookUpValue() {
        return lookUpValue;
    }

    public void setLookUpValue(LookUpValueEntity lookUpValue) {
        this.lookUpValue = lookUpValue;
    }

    public Short getProductTypeID() {
        return productTypeID;
    }

    public ProductType getType() {
        return ProductType.getProductType(productTypeID);
    }

    public void setProductTypeID(Short productTypeID) {
        this.productTypeID = productTypeID;
    }

    public Short getDormancyDays() {
        return dormancyDays;
    }

    public void setDormancyDays(Short dormancyDays) {
        this.dormancyDays = dormancyDays;
    }

    public Short getLatenessDays() {
        return latenessDays;
    }

    public void setLatenessDays(Short latenessDays) {
        this.latenessDays = latenessDays;
    }

    public String getName() {
        String lookupKey = lookUpValue.getLookUpName();
        return ApplicationContextProvider.getBean(MessageLookup.class).lookup(lookupKey);
    }

    /**
     * @deprecated remove after getting spring mvc form submission example working.
     */
    @Deprecated
    public void update(Short latenessDormancy) throws ProductDefinitionException {

        if (productTypeID.equals(ProductType.LOAN.getValue())) {
            this.latenessDays = latenessDormancy;
        } else {
            this.dormancyDays = latenessDormancy;
        }
        try {
            new LoanPrdPersistence().createOrUpdate(this);
        } catch (PersistenceException e) {
            throw new ProductDefinitionException(e);
        }
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    public int getVersionNo() {
        return versionNo;
    }
}
