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

import junit.framework.Assert;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.productdefinition.business.ProductCategoryBO;
import org.mifos.accounts.productdefinition.business.ProductTypeEntity;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.security.util.UserContext;

public class ProductCategoryBusinessServiceIntegrationTest extends MifosIntegrationTestCase {

    private ProductCategoryBusinessService productCategoryBusinessService = null;

    private UserContext userContext = null;

    @Before
    public void setUp() throws Exception {
        productCategoryBusinessService = new ProductCategoryBusinessService();
    }

    @After
    public void tearDown() throws Exception {
        productCategoryBusinessService = null;
        userContext = null;
        StaticHibernateUtil.flushSession();
    }


    @Test
    public void testGetProductTypes() throws Exception {
        UserContext context = TestUtils.makeUser();

        List<ProductTypeEntity> productTypeList = productCategoryBusinessService.getProductTypes();
       Assert.assertEquals(2, productTypeList.size());
        for (ProductTypeEntity productTypeEntity : productTypeList) {
            if (productTypeEntity.getProductTypeID().equals(ProductType.LOAN.getValue())) {
                Assert.assertEquals("Loan", productTypeEntity.getName());
            } else {
                //Assert.assertEquals("Margin Money",productTypeEntity.getName());
                   Assert.assertEquals("Savings", productTypeEntity.getName());
            }
        }
    }

    @Test
    public void testFindByGlobalNum() throws Exception {
        ProductCategoryBO productCategoryBO = createProductCategory();
        Assert.assertNotNull(productCategoryBusinessService.findByGlobalNum(productCategoryBO.getGlobalPrdCategoryNum()));
        deleteProductCategory(productCategoryBO);
    }

    @Test
    public void testGetProductCategoryStatusList() throws Exception {
       Assert.assertEquals(2, productCategoryBusinessService.getProductCategoryStatusList().size());
    }

    @Test
    public void testGetAllCategories() throws Exception {
       Assert.assertEquals(2, productCategoryBusinessService.getAllCategories().size());
        ProductCategoryBO productCategoryBO = createProductCategory();
       Assert.assertEquals(3, productCategoryBusinessService.getAllCategories().size());
        deleteProductCategory(productCategoryBO);
    }

    private void deleteProductCategory(ProductCategoryBO productCategoryBO) {
        Session session = StaticHibernateUtil.getSessionTL();
        Transaction transaction = StaticHibernateUtil.startTransaction();
        session.delete(productCategoryBO);
        session.flush();
    }

    private ProductCategoryBO createProductCategory() throws Exception {
        userContext = TestUtils.makeUser();
        ProductCategoryBO productCategoryBO = new ProductCategoryBO(userContext, productCategoryBusinessService
                .getProductTypes().get(0), "product category", "created a category");
        productCategoryBO.save();
        StaticHibernateUtil.flushSession();
        return (ProductCategoryBO) StaticHibernateUtil.getSessionTL().createQuery(
                "from " + ProductCategoryBO.class.getName() + " pcb order by pcb.productCategoryID").list().get(2);
    }

}
