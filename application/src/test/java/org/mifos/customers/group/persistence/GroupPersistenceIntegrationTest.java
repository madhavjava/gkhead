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

package org.mifos.customers.group.persistence;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.center.persistence.CenterPersistence;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class GroupPersistenceIntegrationTest extends MifosIntegrationTestCase {

    private MeetingBO meeting;

    private CustomerBO center;

    private GroupBO group;

    private GroupPersistence groupPersistence;
    private OfficePersistence officePersistence;
    private CenterPersistence centerPersistence;

    @Before
    public void setUp() throws Exception {
        this.officePersistence = new OfficePersistence();
        this.centerPersistence = new CenterPersistence();
        this.groupPersistence = new GroupPersistence();
        initializeStatisticsService();
    }

    @After
    public void tearDown() throws Exception {
        group = null;
        center = null;
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testUpdateGroupInfoAndGroupPerformanceHistoryForPortfolioAtRisk() throws Exception {
        createGroup();
        StaticHibernateUtil.flushAndClearSession();
        double portfolioAtRisk = 0.567;
        Integer groupId = group.getCustomerId();
        boolean result = getGroupPersistence().updateGroupInfoAndGroupPerformanceHistoryForPortfolioAtRisk(
                portfolioAtRisk, groupId);
        Assert.assertTrue(result);
        group = TestObjectFactory.getGroup(group.getCustomerId());
        Assert.assertEquals(1, group.getUpdatedBy().intValue());
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        Assert.assertEquals(1, group.getUpdatedBy().intValue());
        Assert.assertEquals(currentDate.toString(), group.getUpdatedDate().toString());
        Assert.assertEquals(new Money(getCurrency(), "0.567"), group.getGroupPerformanceHistory().getPortfolioAtRisk());

    }

    @Test
    public void testGetGroupBySystemId() throws PersistenceException {
        createGroup();
        group = groupPersistence.findBySystemId(group.getGlobalCustNum());
        Assert.assertEquals("Group_Active_test", group.getDisplayName());
    }

    @Test
    public void testSearch() throws Exception {
        createGroup();
        QueryResult queryResult = groupPersistence.search(group.getDisplayName(), Short.valueOf("1"));
        Assert.assertNotNull(queryResult);
        Assert.assertEquals(1, queryResult.getSize());
        Assert.assertEquals(1, queryResult.get(0, 10).size());
    }

    @Test
    public void testSearchForAddingClientToGroup() throws Exception {
        createGroup_ON_HOLD_STATUS();
        QueryResult queryResult = groupPersistence.searchForAddingClientToGroup(group.getDisplayName(),
                Short.valueOf("1"));
        Assert.assertNotNull(queryResult);
        Assert.assertEquals(0, queryResult.getSize());
        Assert.assertEquals(0, queryResult.get(0, 10).size());
    }

    private CenterBO createCenter() {
        return createCenter("Center_Active_test");
    }

    private CenterBO createCenter(String name) {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        return TestObjectFactory.createWeeklyFeeCenter(name, meeting);
    }

    private void createGroup() {
        center = createCenter();
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE,
                center);
        StaticHibernateUtil.flushSession();

    }

    private void createGroup_ON_HOLD_STATUS() {
        center = createCenter();
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_ON_HOLD_test", CustomerStatus.GROUP_HOLD,
                center);
        StaticHibernateUtil.flushSession();

    }

    public OfficePersistence getOfficePersistence() {
        return officePersistence;
    }

    public CenterPersistence getCenterPersistence() {
        return centerPersistence;
    }

    public GroupPersistence getGroupPersistence() {
        return groupPersistence;
    }
}
