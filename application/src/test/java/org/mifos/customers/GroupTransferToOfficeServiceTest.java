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

package org.mifos.customers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.application.holiday.persistence.HolidayDao;
import org.mifos.customers.business.service.CustomerService;
import org.mifos.customers.business.service.CustomerServiceImpl;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficeDao;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.personnel.persistence.PersonnelDao;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.GroupBuilder;
import org.mifos.domain.builders.OfficeBuilder;
import org.mifos.framework.hibernate.helper.HibernateTransactionHelper;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupTransferToOfficeServiceTest {

    // class under test
    private CustomerService customerService;

    // collaborators (behaviour)
    @Mock
    private CustomerDao customerDao;

    @Mock
    private PersonnelDao personnelDao;

    @Mock
    private OfficeDao officeDao;

    @Mock
    private HolidayDao holidayDao;

    @Mock
    private HibernateTransactionHelper hibernateTransaction;

    @Before
    public void setupAndInjectDependencies() {
        customerService = new CustomerServiceImpl(customerDao, personnelDao, officeDao, holidayDao, hibernateTransaction);
    }

    @Test
    public void givenOfficeIsNullGroupTransferToBranchShouldFailValidation() {

        // setup
        GroupBO group = new GroupBuilder().build();
        OfficeBO office = null;

        // exercise test
        try {
            customerService.transferGroupTo(group, office);
            fail("should fail validation");
        } catch (CustomerException e) {
            assertThat(e.getKey(), is(CustomerConstants.INVALID_OFFICE));
        }
    }

    @Test
    public void givenOfficeIsInactiveGroupTransferToBranchShouldFailValidation() {

        // setup
        GroupBO group = new GroupBuilder().build();
        OfficeBO office = new OfficeBuilder().inActive().build();

        // exercise test
        try {
            customerService.transferGroupTo(group, office);
            fail("should fail validation");
        } catch (CustomerException e) {
            assertThat(e.getKey(), is(CustomerConstants.ERRORS_TRANSFER_IN_INACTIVE_OFFICE));
        }
    }

    @Test
    public void givenOfficeIsSameAsGroupsOfficeGroupTransferToBranchShouldFailValidation() {

        // setup
        OfficeBO office = new OfficeBuilder().build();
        CenterBO parent = new CenterBuilder().with(office).build();
        GroupBO group = new GroupBuilder().withParentCustomer(parent).build();

        // exercise test
        try {
            customerService.transferGroupTo(group, office);
            fail("should fail validation");
        } catch (CustomerException e) {
            assertThat(e.getKey(), is(CustomerConstants.ERRORS_SAME_BRANCH_TRANSFER));
        }
    }

    @Test
    public void givenOfficeAlreadyHasGroupWithSameNameThenGroupTransferToBranchShouldFailValidation() throws Exception {

        // setup
        GroupBO group = new GroupBuilder().withName("already-exists-group").build();
        OfficeBO office = new OfficeBuilder().inActive().build();

        // stubbing
        doThrow(new CustomerException(CustomerConstants.ERRORS_DUPLICATE_CUSTOMER)).when(customerDao).validateGroupNameIsNotTakenForOffice(anyString(), anyShort());

        // exercise test
        try {
            customerService.transferGroupTo(group, office);
            fail("should fail validation");
        } catch (CustomerException e) {
            assertThat(e.getKey(), is(CustomerConstants.ERRORS_TRANSFER_IN_INACTIVE_OFFICE));
        }
    }
}