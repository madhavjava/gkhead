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

import java.util.Set;

import junit.framework.Assert;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;

public class AccountStateEntityIntegrationTest extends MifosIntegrationTestCase {

    private String APPROVED = "Application Approved";
    private AccountStateEntity accountStateEntity;
    private Session session;

    @Before
    public void setUp() throws Exception {
        session = StaticHibernateUtil.getSessionTL();
    }

    @After
    public void tearDown() throws Exception {
        StaticHibernateUtil.flushSession();
        session = null;
    }

    @Test
    public void testGetNameFailure() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("1"));
        String name = accountStateEntity.getName();
        Assert.assertFalse("This should fail, name is Partial Application", !("Partial Application".equals(name)));
    }

    @Test
    public void testGetNameSuccess() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("1"));
        String name = accountStateEntity.getName();
       Assert.assertEquals("Partial Application", name);
    }

    @Test
    public void testGetNamesSuccess() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("1"));
        Set<LookUpValueLocaleEntity> lookUpValueLocaleEntitySet = accountStateEntity.getNames();
        int size = lookUpValueLocaleEntitySet.size();
       Assert.assertEquals(1, size);
    }

    @Test
    public void testGetNamesFailure() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("1"));
        Set<LookUpValueLocaleEntity> lookUpValueLocaleEntitySet = accountStateEntity.getNames();
        int size = lookUpValueLocaleEntitySet.size();
        Assert.assertFalse("This should fail, the size is 1", !(size == 1));
    }

    @Test
    public void testGetNameWithLocaleSuccess() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("3"));
        String name = accountStateEntity.getName();
       Assert.assertEquals(APPROVED, name);
    }

    @Test
    public void testGetNameWithLocaleFailure() {
        accountStateEntity = getAccountStateEntityObject(Short.valueOf("3"));
        String name = accountStateEntity.getName();
        Assert.assertFalse("This should fail, name is Approved", !(APPROVED.equals(name)));
    }

    private AccountStateEntity getAccountStateEntityObject(Short id) {
        Query query = session.createQuery("from org.mifos.accounts.business.AccountStateEntity ac_state "
                + "where ac_state.id = ?");
        query.setString(0, id.toString());
        return (AccountStateEntity) query.uniqueResult();
    }

}
