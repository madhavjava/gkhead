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
package org.mifos.customers.business;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.fees.business.FeeDto;
import org.mifos.accounts.savings.persistence.GenericDao;
import org.mifos.accounts.savings.persistence.GenericDaoHibernate;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.persistence.CustomerDaoHibernate;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.security.util.UserContext;

/**
 *
 */
public class CustomerCreationDaoHibernateIntegrationTest extends MifosIntegrationTestCase {

    // class under test
    private CustomerDao customerDao;

    // collaborators
    private final GenericDao genericDao = new GenericDaoHibernate();

    // test data
    private MeetingBO weeklyMeeting;

    private CenterBO center;

    @Before
    public void setUp() throws Exception {

        // jdbc
        // delete from customer_fee_schedule
        // delete form customer_schedule
        // delete from customer_account
        // delete from account
        // delete from customer_meeting
        // delete from customer

        weeklyMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();

        IntegrationTestObjectMother.saveMeeting(weeklyMeeting);

        customerDao = new CustomerDaoHibernate(genericDao);
    }

    @After
    public void tearDown() throws Exception {
        center = null;
    }

    @Test
    public void testShouldCreateCustomerAndCustomerAccount() throws Exception {

        String displayName = "centerCascade";
        Address address = null;
        final List<CustomFieldDto> customFields = new ArrayList<CustomFieldDto>();
        final List<FeeDto> fees = new ArrayList<FeeDto>();
        final String externalId = null;
        final Date mfiJoiningDate = new DateTime().minusDays(1).toDate();
        final OfficeBO existingOffice = IntegrationTestObjectMother.sampleBranchOffice();
        final PersonnelBO loanOfficer = IntegrationTestObjectMother.testUser();
        UserContext userContext = new UserContext();
        userContext.setId(loanOfficer.getPersonnelId());
        userContext.setBranchId(existingOffice.getOfficeId());
        userContext.setBranchGlobalNum(existingOffice.getGlobalOfficeNum());

        center = new CenterBO(userContext, displayName, address, customFields, fees, externalId, mfiJoiningDate, existingOffice, weeklyMeeting, loanOfficer, new CustomerPersistence());

        StaticHibernateUtil.startTransaction();
        customerDao.save(center);
        StaticHibernateUtil.flushSession();

        assertThat(center.getCustomerAccount(), is(notNullValue()));
        assertThat(center.getGlobalCustNum(), is(nullValue()));
    }
}