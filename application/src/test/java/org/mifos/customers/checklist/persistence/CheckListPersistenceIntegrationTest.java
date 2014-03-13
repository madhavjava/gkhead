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

package org.mifos.customers.checklist.persistence;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.checklist.business.AccountCheckListBO;
import org.mifos.customers.checklist.business.CustomerCheckListBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.dto.domain.CheckListMasterDto;
import org.mifos.dto.screen.CheckListStatesView;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class CheckListPersistenceIntegrationTest extends MifosIntegrationTestCase {

    @After
    public void tearDown() throws Exception {
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testGetCheckListMasterData() throws Exception {
        List<CheckListMasterDto> masterCheckList = null;

        masterCheckList = new CheckListPersistence().getCheckListMasterData((short) 1);

        Assert.assertNotNull(masterCheckList);
        Assert.assertEquals(masterCheckList.size(), 5);
    }

    @Test
    public void testGetCustomerStates() throws Exception {
        List<CheckListStatesView> customerStates = new CheckListPersistence().retrieveAllCustomerStatusList(
                Short.valueOf("1"), (short) 1);
        Assert.assertEquals(customerStates.size(), 5);
        customerStates = new CheckListPersistence().retrieveAllCustomerStatusList(Short.valueOf("2"), (short) 1);
        Assert.assertNotNull(customerStates);
        Assert.assertEquals(customerStates.size(), 5);
        customerStates = new CheckListPersistence().retrieveAllCustomerStatusList(Short.valueOf("3"), (short) 1);
        Assert.assertNotNull(customerStates);
        Assert.assertEquals(customerStates.size(), 2);

    }

    @Test
    public void testGetAccountStates() throws Exception {
        List<CheckListStatesView> accountStates = new CheckListPersistence().retrieveAllAccountStateList(
                Short.valueOf("1"), (short) 1);
        Assert.assertNotNull(accountStates);
        Assert.assertEquals(6, accountStates.size());
        accountStates = new CheckListPersistence().retrieveAllAccountStateList(Short.valueOf("2"), (short) 1);
        Assert.assertNotNull(accountStates);
        Assert.assertEquals(accountStates.size(), 4);

    }

    @Test
    public void testRetreiveAllAccountCheckLists() throws Exception {
        TestObjectFactory.createAccountChecklist(ProductType.LOAN.getValue(),
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, (short) 1);
        TestObjectFactory.createCustomerChecklist(CustomerLevel.CENTER.getValue(),
                CustomerStatus.CENTER_ACTIVE.getValue(), (short) 1);
        List<AccountCheckListBO> checkLists = new CheckListPersistence().retreiveAllAccountCheckLists();
        Assert.assertNotNull(checkLists);
        Assert.assertEquals(1, checkLists.size());
    }

    @Test
    public void testRetreiveAllCustomerCheckLists() throws Exception {
        TestObjectFactory.createCustomerChecklist(CustomerLevel.CENTER.getValue(),
                CustomerStatus.CENTER_ACTIVE.getValue(), (short) 1);
        TestObjectFactory.createAccountChecklist(ProductType.LOAN.getValue(),
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, (short) 1);
        List<CustomerCheckListBO> checkLists = new CheckListPersistence().retreiveAllCustomerCheckLists();
        Assert.assertNotNull(checkLists);
        Assert.assertEquals(1, checkLists.size());
    }

    @Test
    public void testCheckListMasterView() {
        CheckListMasterDto checkListMasterDto = new CheckListMasterDto(Short.valueOf("1"), "Loan");
        checkListMasterDto.setCustomer(true);
        Assert.assertEquals(Short.valueOf("1"), checkListMasterDto.getMasterTypeId());
//        Assert.assertEquals("Loan", checkListMasterDto.getMasterTypeName());
        Assert.assertEquals(true, checkListMasterDto.isCustomer());
    }

    @Test
    public void testCheckListStatesView() {
        CheckListStatesView checkListStatesView = new CheckListStatesView(Short.valueOf("13"), "Active",
                Short.valueOf("1"));
        Assert.assertEquals(Short.valueOf("13"), checkListStatesView.getStateId());
        Assert.assertEquals("Active", checkListStatesView.getStateName());
        Assert.assertEquals(Short.valueOf("1"), checkListStatesView.getId());
    }
}
