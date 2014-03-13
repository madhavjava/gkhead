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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.application.holiday.persistence.HolidayDao;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.servicefacade.CustomerStatusUpdate;
import org.mifos.config.util.helpers.ConfigurationConstants;
import org.mifos.core.MifosRuntimeException;
import org.mifos.customers.business.CustomerStatusEntity;
import org.mifos.customers.business.service.CustomerAccountFactory;
import org.mifos.customers.business.service.CustomerService;
import org.mifos.customers.business.service.CustomerServiceImpl;
import org.mifos.customers.business.service.MessageLookupHelper;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficeDao;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.persistence.PersonnelDao;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.ClientBuilder;
import org.mifos.domain.builders.CustomerStatusUpdateBuilder;
import org.mifos.domain.builders.GroupBuilder;
import org.mifos.domain.builders.OfficeBuilder;
import org.mifos.domain.builders.PersonnelBuilder;
import org.mifos.dto.domain.CustomerDto;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.HibernateTransactionHelper;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.Money;
import org.mifos.security.util.SecurityConstants;
import org.mifos.security.util.UserContext;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * I test implementation of {@link CustomerService} for update of status of {@link CenterBO}'s.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerStatusUpdateTest {

    // class under test
    private CustomerService customerService;

    // collaborators
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

    @Mock
    private CustomerAccountFactory customerAccountFactory;

    @Mock
    private MessageLookupHelper messageLookupHelper;

    @Mock
    private CenterBO mockedCenter;

    private static MifosCurrency oldDefaultCurrency;

    @BeforeClass
    public static void initialiseCurrencyForMoney() {
        oldDefaultCurrency = Money.getDefaultCurrency();
        Money.setDefaultCurrency(TestUtils.RUPEE);
    }

    @AfterClass
    public static void resetCurrency() {
        Money.setDefaultCurrency(oldDefaultCurrency);
    }

    @Before
    public void setupAndInjectDependencies() {
        customerService = new CustomerServiceImpl(customerDao, personnelDao, officeDao, holidayDao, hibernateTransaction);
        ((CustomerServiceImpl)customerService).setCustomerAccountFactory(customerAccountFactory);
        ((CustomerServiceImpl)customerService).setMessageLookupHelper(messageLookupHelper);
    }

    @Test(expected=CustomerException.class)
    public void throwsCheckedExceptionWhenVersionOfCustomerForUpdateIsDifferentToPersistedCustomerVersion() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().build();

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(mockedCenter);
        doThrow(new CustomerException(Constants.ERROR_VERSION_MISMATCH)).when(mockedCenter).validateVersion(anyInt());

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);
    }

    @Test(expected=CustomerException.class)
    public void throwsCheckedExceptionWhenUserDoesNotHavePermissionToUpdateCustomerStatus() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().build();
        CenterBO existingCenter = new CenterBuilder().withVersion(customerStatusUpdate.getVersionNum()).build();

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingCenter);
        doThrow(new CustomerException(SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED)).when(customerDao).checkPermissionForStatusChange(anyShort(), eq(userContext), anyShort(), anyShort(), anyShort());

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(customerDao).checkPermissionForStatusChange(customerStatusUpdate.getNewStatus().getValue(), userContext, customerStatusUpdate.getCustomerStatusFlag().getValue(), existingCenter.getOffice().getOfficeId(), existingCenter.getPersonnel().getPersonnelId());
    }

    @Test
    public void updateDetailsAreSetWhenUpdatingCustomerStatus() throws Exception {

        // setup
        OfficeBO office = new OfficeBuilder().withGlobalOfficeNum("xxx-9999").build();
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().build();

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(mockedCenter);
        when(mockedCenter.getOffice()).thenReturn(office);
        when(mockedCenter.getCustomerStatus()).thenReturn(new CustomerStatusEntity(CustomerStatus.CENTER_ACTIVE));

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(mockedCenter).updateDetails(userContext);
    }

    @Test(expected = CustomerException.class)
    public void throwsCheckedExceptionWhenCenterTransitionsToInActiveStateAndFailsValidation() throws Exception {

        // setup
        OfficeBO office = new OfficeBuilder().withGlobalOfficeNum("xxx-9999").build();
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.CENTER_INACTIVE).build();

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(mockedCenter);
        when(mockedCenter.getOffice()).thenReturn(office);
        when(mockedCenter.getCustomerStatus()).thenReturn(new CustomerStatusEntity(CustomerStatus.CENTER_ACTIVE));
        doThrow(new CustomerException(CustomerConstants.CENTER_STATE_CHANGE_EXCEPTION)).when(mockedCenter).validateChangeToInActive();

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(mockedCenter).validateChangeToInActive();
    }

    @Test(expected = CustomerException.class)
    public void throwsCheckedExceptionWhenCenterTransitionsToInActiveStateAndCenterHasActiveClients() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.CENTER_INACTIVE).build();
        CenterBO existingCenter = new CenterBuilder().withVersion(customerStatusUpdate.getVersionNum()).active().build();

        CustomerDto customer1 = new CustomerDto();
        List<CustomerDto> clientsThatAreNotCancelledOrClosed = new ArrayList<CustomerDto>();
        clientsThatAreNotCancelledOrClosed.add(customer1);

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingCenter);
        when(customerDao.findClientsThatAreNotCancelledOrClosed(existingCenter.getSearchId(), existingCenter.getOffice().getOfficeId())).thenReturn(clientsThatAreNotCancelledOrClosed);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(messageLookupHelper).lookupLabel(ConfigurationConstants.GROUP);
    }

    @Test(expected = CustomerException.class)
    public void throwsCheckedExceptionWhenCenterTransitionsToInActiveStateAndCenterHasActiveGroups() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.CENTER_INACTIVE).build();
        CenterBO existingCenter = new CenterBuilder().withVersion(customerStatusUpdate.getVersionNum()).active().build();

        CustomerDto customer1 = new CustomerDto();
        List<CustomerDto> clientsThatAreNotCancelledOrClosed = new ArrayList<CustomerDto>();
        clientsThatAreNotCancelledOrClosed.add(customer1);

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingCenter);
        when(customerDao.findGroupsThatAreNotCancelledOrClosed(existingCenter.getSearchId(), existingCenter.getOffice().getOfficeId())).thenReturn(clientsThatAreNotCancelledOrClosed);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(messageLookupHelper).lookupLabel(ConfigurationConstants.GROUP);
    }

    @Test(expected = MifosRuntimeException.class)
    public void rollsbackTransactionClosesSessionAndThrowsRuntimeExceptionWhenExceptionOccurs() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.CENTER_INACTIVE).build();
        CenterBO existingCenter = new CenterBuilder().withVersion(customerStatusUpdate.getVersionNum()).active().build();

        CustomerDto customer1 = new CustomerDto();
        List<CustomerDto> clientsThatAreNotCancelledOrClosed = new ArrayList<CustomerDto>();
        clientsThatAreNotCancelledOrClosed.add(customer1);

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingCenter);
        doThrow(new RuntimeException()).when(customerDao).save(existingCenter);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        verify(hibernateTransaction).rollbackTransaction();
        verify(hibernateTransaction).closeSession();
    }

    @Test(expected=CustomerException.class)
    public void throwsCheckedExceptionWhenValidationFailsForTransitioningToActive() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.GROUP_ACTIVE).build();

        CenterBO existingCenter = new CenterBuilder().build();
        GroupBO existingGroup = new GroupBuilder().active().withParentCustomer(existingCenter).withVersion(customerStatusUpdate.getVersionNum()).build();
        existingGroup.setLoanOfficer(null);

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingGroup);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);
    }

    @Test
    public void setsActivationDateAndUpdatesCustomerHierarchyWhenTransitioningToActiveForFirstTime() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.GROUP_ACTIVE).build();

        PersonnelBO loanOfficer = PersonnelBuilder.anyLoanOfficer();
        CenterBO existingCenter = new CenterBuilder().withLoanOfficer(loanOfficer).active().build();
        GroupBO existingGroup = new GroupBuilder().pendingApproval().withParentCustomer(existingCenter).withVersion(customerStatusUpdate.getVersionNum()).build();

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingGroup);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        assertThat(new LocalDate(existingGroup.getCustomerActivationDate()), is(new LocalDate()));
        assertThat((CenterBO)existingGroup.getActiveCustomerHierarchy().getParentCustomer(), is(existingCenter));
        assertThat((GroupBO)existingGroup.getActiveCustomerHierarchy().getCustomer(), is(existingGroup));
    }

    @Test
    public void updatesPendingChildrenToPartialWhenTransitioningGroupFromPendingToCancelled() throws Exception {

        // setup
        UserContext userContext = TestUtils.makeUser();
        CustomerStatusUpdate customerStatusUpdate = new CustomerStatusUpdateBuilder().with(CustomerStatus.GROUP_CANCELLED).build();

        PersonnelBO loanOfficer = PersonnelBuilder.anyLoanOfficer();
        CenterBO existingCenter = new CenterBuilder().withLoanOfficer(loanOfficer).active().build();
        GroupBO existingGroup = new GroupBuilder().pendingApproval().withParentCustomer(existingCenter).withVersion(customerStatusUpdate.getVersionNum()).build();
        ClientBO existingClient = new ClientBuilder().withParentCustomer(existingGroup).pendingApproval().buildForUnitTests();
        existingGroup.addChild(existingClient);

        // stubbing
        when(customerDao.findCustomerById(customerStatusUpdate.getCustomerId())).thenReturn(existingGroup);

        // exercise test
        customerService.updateCustomerStatus(userContext, customerStatusUpdate);

        // verification
        assertThat(existingGroup.getStatus(), is(CustomerStatus.GROUP_CANCELLED));
        assertThat(existingClient.getUserContext(), is(userContext));
        assertThat(existingClient.getStatus(), is(CustomerStatus.CLIENT_PARTIAL));
    }
}