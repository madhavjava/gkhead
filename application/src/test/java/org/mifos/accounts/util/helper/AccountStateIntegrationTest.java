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

package org.mifos.accounts.util.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.accounts.business.AccountStateEntity;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountStateIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    private LegacyAccountDao legacyAccountDao;

    /*
     * Check that the values in the enumerated type AccountState match with the
     * Entity values in terms of ids, state names, and the text that they
     * eventually resolve to.
     */
    @Test
    public void testRetrieveAllAccountStateList() throws NumberFormatException, PersistenceException {
        List<AccountStateEntity> accountStateEntityList = legacyAccountDao
                .retrieveAllAccountStateList(ProductType.SAVINGS.getValue());
        Assert.assertNotNull(accountStateEntityList);
       Assert.assertEquals(6, accountStateEntityList.size());

        List<AccountStateEntity> accountStateEntityList2 = legacyAccountDao
                .retrieveAllAccountStateList(ProductType.LOAN.getValue());
        Assert.assertNotNull(accountStateEntityList2);
       Assert.assertEquals(12, accountStateEntityList2.size());

        accountStateEntityList.addAll(accountStateEntityList2);

        // order by id
        Collections.sort(accountStateEntityList, new Comparator<AccountStateEntity>() {
            @Override
			public int compare(AccountStateEntity state1, AccountStateEntity state2) {
                return state1.getId().compareTo(state2.getId());
            }
        });

       Assert.assertEquals(AccountState.values().length, accountStateEntityList.size());

        Iterator<AccountStateEntity> stateListIterator = accountStateEntityList.iterator();
        for (AccountState state : AccountState.values()) {
            AccountStateEntity stateInDatabase = stateListIterator.next();
           Assert.assertEquals(state.getValue(), stateInDatabase.getId());
           Assert.assertEquals(state.getPropertiesKey(), stateInDatabase.getLookUpValue().getLookUpName());
           Assert.assertEquals(ApplicationContextProvider.getBean(MessageLookup.class).lookup(state), ApplicationContextProvider.getBean(MessageLookup.class).lookup(stateInDatabase));
        }

    }

}
