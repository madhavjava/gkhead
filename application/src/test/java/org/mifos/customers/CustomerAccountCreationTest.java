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
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.calendar.CalendarEvent;
import org.mifos.calendar.CalendarUtils;
import org.mifos.customers.business.CustomerAccountBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.domain.builders.CalendarEventBuilder;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.FeeBuilder;
import org.mifos.domain.builders.GroupBuilder;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.Money;

/**
 * I test {@link CustomerAccountBO#createNew(CustomerBO, List, MeetingBO, CalendarEvent)}
 */
public class CustomerAccountCreationTest {

    // class under test
    private CustomerAccountBO customerAccount;

    // collabrators
    private CustomerBO customer;
    private MeetingBO customerMeeting;
    private List<AccountFeesEntity> accountFees;
    private CalendarEvent applicableCalendarEvents;

    private static MifosCurrency oldDefaultCurrency;

    @BeforeClass
    public static void setupDefaultCurrency() {
        oldDefaultCurrency = Money.getDefaultCurrency();
        Money.setDefaultCurrency(TestUtils.RUPEE);
    }

    @AfterClass
    public static void replaceOldDefaultCurrency() {
        Money.setDefaultCurrency(oldDefaultCurrency);
    }

    @Test
    public void customerAccountIsAlwaysCreatedInActiveState() throws Exception {

        // setup
        customer = new CenterBuilder().inActive().build();
        accountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.isActive(), is(true));
    }

    @Test
    public void customerAccountCanBeCreatedWithNoAccountFees() throws Exception {

        // setup
        accountFees = new ArrayList<AccountFeesEntity>();
        customer = new CenterBuilder().inActive().build();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.getAccountFees().isEmpty(), is(true));
    }

    @Test
    public void customerAccountCanBeCreatedWithAccountFees() throws Exception {

        // setup
        FeeBO fee = new FeeBuilder().appliesToAllCustomers().periodic().with(new MeetingBuilder().periodicFeeMeeting().weekly().every(1)).build();
        AccountFeesEntity accountFee = new AccountFeesEntity(null, fee, Double.valueOf("25.0"));
        accountFees = new ArrayList<AccountFeesEntity>();
        accountFees.add(accountFee);

        customer = new CenterBuilder().inActive().build();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.getAccountFees(), hasItem(accountFee));
        assertThat(accountFee.getAccount(), is((AccountBO)customerAccount));
    }

    @Test
    public void customerAccountIsNotCreatedWithCustomerSchedulesWhenAssociatedCustomerIsNotActive() throws Exception {

        // setup
        accountFees = new ArrayList<AccountFeesEntity>();
        customer = new CenterBuilder().inActive().build();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.getAccountActionDates().isEmpty(), is(true));
    }

    @Test
    public void customerAccountIsCreatedWithCustomerSchedulesWhenAssociatedCustomerIsActive() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();
        customerMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).build();
        accountFees = new ArrayList<AccountFeesEntity>();

        customer = new CenterBuilder().active().build();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.getAccountActionDates().isEmpty(), is(false));
    }

    @Test
    public void customerSchedulesAreCreatedFromCustomersActivationDate() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime twoWeeksAgo = new DateTime().minusWeeks(2);
        customerMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingFrom(twoWeeksAgo.toDate()).build();
        accountFees = new ArrayList<AccountFeesEntity>();

        customer = new CenterBuilder().active().withActivationDate(new DateMidnight().toDateTime()).build();

        // exercise test
        customerAccount = CustomerAccountBO.createNew(customer, accountFees, customerMeeting, applicableCalendarEvents);

        // verification
        assertThat(customerAccount.getAccountActionDates().isEmpty(), is(false));

        List<AccountActionDateEntity> customerSchedules = new ArrayList<AccountActionDateEntity>(customerAccount.getAccountActionDates());

        LocalDate activationDate = new LocalDate(CalendarUtils.nearestWorkingDay(new DateTime()));
        assertThat(new LocalDate(customerSchedules.get(0).getActionDate()), is(activationDate));
    }
    
    @Test
    public void firstCustomerScheduleIsAlwaysTheClosestMatchingDayOfWeekAndDoesNotTakeIntoAccountWeeklyMeetingFrequency() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().weekly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());

        LocalDate firstCenterDate = new LocalDate(centerSchedules.get(0).getActionDate());
        
        LocalDate mon25thOfApril = new DateTime().withDate(2011, 4, 25).toLocalDate();
        assertThat(firstCenterDate, is(mon25thOfApril));
    }

    @Test
    public void firstCustomerScheduleIsAlwaysTheClosestMatchingDayOfWeekAndDoesNotTakeIntoAccountMonthlyMeetingFrequency() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().monthly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());

        LocalDate firstCenterDate = new LocalDate(centerSchedules.get(0).getActionDate());
        
        LocalDate mon18thOfMay = new DateTime().withDate(2011, 5, 18).toLocalDate();
        assertThat(firstCenterDate, is(mon18thOfMay));
    }

    
    @Test
    public void givenBiWeeklyFrequencyFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue26thOfApril = new DateTime().withDate(2011, 4, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().weekly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().weekly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue26thOfApril.minusDays(1).toDate()).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue26thOfApril).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        LocalDate thirdCenterDate = new LocalDate(centerSchedules.get(2).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        LocalDate secondGroupDate = new LocalDate(groupSchedules.get(1).getActionDate());
        
        assertThat("group is activated after center first schedule and so should start on its second scheduled date", firstGroupDate, is(secondCenterDate));
        assertThat(secondGroupDate, is(thirdCenterDate));
    }
    
    @Test
    public void givenBiWeeklyFrequencyAndChildCreatedThreeWeeksAfterParentFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue10thOfMay = new DateTime().withDate(2011, 4, 26).plusWeeks(2);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().weekly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().weekly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue10thOfMay.minusDays(1).toDate()).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue10thOfMay).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate thirdCenterDate = new LocalDate(centerSchedules.get(2).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());

        assertThat(firstGroupDate, is(thirdCenterDate));
    }

    @Test
    public void givenTriWeeklyFrequencyFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue26thOfApril = new DateTime().withDate(2011, 4, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().weekly().every(3).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().weekly().every(3).occuringOnA(WeekDay.MONDAY).startingFrom(tue26thOfApril.minusDays(1).toDate()).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue26thOfApril).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        LocalDate thirdCenterDate = new LocalDate(centerSchedules.get(2).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        LocalDate secondGroupDate = new LocalDate(groupSchedules.get(1).getActionDate());
        
        assertThat(firstGroupDate, is(secondCenterDate));
        assertThat(secondGroupDate, is(thirdCenterDate));
    }

    @Test
    public void givenMonthlyFrequencyFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue26thOfApril = new DateTime().withDate(2011, 4, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().monthly().every(1).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().monthly().every(1).occuringOnA(WeekDay.MONDAY).startingFrom(tue26thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue26thOfApril).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate firstCenterDate = new LocalDate(centerSchedules.get(0).getActionDate());
        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        LocalDate secondGroupDate = new LocalDate(groupSchedules.get(1).getActionDate());
        
        assertThat(firstGroupDate, is(firstCenterDate));
        assertThat(secondGroupDate, is(secondCenterDate));
    }

    @Test
    public void givenMonthlyFrequencyAndChildCreatedAfterParentsFirstScheduleFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime thursday26thOfMay = new DateTime().withDate(2011, 5, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().monthly().every(1).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().monthly().every(1).occuringOnA(WeekDay.MONDAY).startingFrom(thursday26thOfMay.minusDays(1).toDate()).onDayOfMonth(18).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(thursday26thOfMay).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        
        assertThat(firstGroupDate, is(secondCenterDate));
    }

    
    @Test
    public void givenBiMonthlyFrequencyFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue26thOfApril = new DateTime().withDate(2011, 4, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().monthly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().monthly().every(2).occuringOnA(WeekDay.MONDAY).startingFrom(tue26thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue26thOfApril).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate firstCenterDate = new LocalDate(centerSchedules.get(0).getActionDate());
        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        LocalDate secondGroupDate = new LocalDate(groupSchedules.get(1).getActionDate());
        
        assertThat(firstGroupDate, is(firstCenterDate));
        assertThat(secondGroupDate, is(secondCenterDate));
    }
    
    @Test
    public void givenTriMonthlyFrequencyFirstCustomerScheduleForChildSynchsWithNearestScheduleOfParent() throws Exception {

        // setup
        applicableCalendarEvents = new CalendarEventBuilder().build();

        DateTime tue19thOfApril = new DateTime().withDate(2011, 4, 19);
        DateTime tue26thOfApril = new DateTime().withDate(2011, 4, 26);
        accountFees = new ArrayList<AccountFeesEntity>();
        
        MeetingBO centerMeeting = new MeetingBuilder().customerMeeting().monthly().every(3).occuringOnA(WeekDay.MONDAY).startingFrom(tue19thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();
        MeetingBO groupMeeting = new MeetingBuilder().customerMeeting().monthly().every(3).occuringOnA(WeekDay.MONDAY).startingFrom(tue26thOfApril.minusDays(1).toDate()).onDayOfMonth(18).build();

        CenterBO center = new CenterBuilder().active().withActivationDate(tue19thOfApril).with(centerMeeting).build();
        GroupBO group = new GroupBuilder().active().withParentCustomer(center).withActivationDate(tue26thOfApril).withMeeting(groupMeeting).build();

        // exercise test
        CustomerAccountBO centerAccount = CustomerAccountBO.createNew(center, accountFees, centerMeeting, applicableCalendarEvents);
        CustomerAccountBO groupAccount = CustomerAccountBO.createNew(group, accountFees, groupMeeting, applicableCalendarEvents);

        // verification
        List<AccountActionDateEntity> centerSchedules = new ArrayList<AccountActionDateEntity>(centerAccount.getAccountActionDates());
        List<AccountActionDateEntity> groupSchedules = new ArrayList<AccountActionDateEntity>(groupAccount.getAccountActionDates());

        LocalDate firstCenterDate = new LocalDate(centerSchedules.get(0).getActionDate());
        LocalDate secondCenterDate = new LocalDate(centerSchedules.get(1).getActionDate());
        
        LocalDate firstGroupDate = new LocalDate(groupSchedules.get(0).getActionDate());
        LocalDate secondGroupDate = new LocalDate(groupSchedules.get(1).getActionDate());
        
        assertThat(firstGroupDate, is(firstCenterDate));
        assertThat(secondGroupDate, is(secondCenterDate));
    }

}