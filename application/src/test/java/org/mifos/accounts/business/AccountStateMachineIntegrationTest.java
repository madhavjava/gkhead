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

package org.mifos.accounts.business;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.business.service.AccountBusinessService;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStateFlag;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.exceptions.ServiceUnavailableException;
import org.mifos.framework.exceptions.StatesInitializationException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;

public class AccountStateMachineIntegrationTest extends MifosIntegrationTestCase {

    private AccountBusinessService service;

    @Before
    public void setUp() throws Exception {
        service = new AccountBusinessService();
    }

    @After
    public void tearDown() throws Exception {
        StaticHibernateUtil.getSessionTL().clear();
    }

    @Test
    public void testGetStatusList() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        List<AccountStateEntity> stateList = service.getStatusList(new AccountStateEntity(
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING), AccountTypes.LOAN_ACCOUNT, Short.valueOf("1"));
       Assert.assertEquals(2, stateList.size());
    }

    @Test
    public void testGetStatusName() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        Assert.assertNotNull(service.getStatusName(AccountState.LOAN_CLOSED_RESCHEDULED, AccountTypes.LOAN_ACCOUNT));
    }

    @Test
    public void testGetFlagName() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        Assert.assertNotNull(service.getFlagName(AccountStateFlag.LOAN_WITHDRAW, AccountTypes.LOAN_ACCOUNT));
    }

    @Test @Ignore("Convert to unit test")
    public void testStatesInitializationException() throws Exception {

        try {
            AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
            Assert.fail();
        } catch (StatesInitializationException sie) {
        } finally {
            StaticHibernateUtil.flushSession();
        }
    }

    @Test
    public void testServiceUnavailableException() throws Exception {
        try {
            service = (AccountBusinessService) ServiceFactory.getInstance().getBusinessService(null);
            Assert.fail();
        } catch (ServiceUnavailableException sue) {
        }
    }

    @Test
    public void testFlagForLoanCancelState() throws Exception {
        AccountStateMachines.getInstance().initialize(AccountTypes.LOAN_ACCOUNT, null);
        List<AccountStateEntity> stateList = service.getStatusList(new AccountStateEntity(
                AccountState.LOAN_PARTIAL_APPLICATION), AccountTypes.LOAN_ACCOUNT, Short.valueOf("1"));
        for (AccountStateEntity accountState : stateList) {
            if (accountState.getId().equals(AccountState.LOAN_CANCELLED.getValue())) {
               Assert.assertEquals(3, accountState.getFlagSet().size());
                for (AccountStateFlagEntity accountStateFlag : accountState.getFlagSet()) {
                    if (accountStateFlag.getId().equals(AccountStateFlag.LOAN_REVERSAL.getValue())) {
                        Assert.fail();
                    }
                }
            }
        }
    }
}
