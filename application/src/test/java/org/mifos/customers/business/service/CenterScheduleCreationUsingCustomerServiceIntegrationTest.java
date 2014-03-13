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

package org.mifos.customers.business.service;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.sampleBranchOffice;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.testUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.application.holiday.util.helpers.RepaymentRuleTypes;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.config.FiscalCalendarRules;
import org.mifos.customers.business.CustomerAccountBO;
import org.mifos.customers.business.CustomerScheduleEntity;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.FeeBuilder;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.dto.domain.HolidayDetails;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.StandardTestingService;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.Money;
import org.mifos.service.test.TestMode;
import org.mifos.test.framework.util.DatabaseCleaner;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FIXME - completely rerwite/fix these tests
 */
public class CenterScheduleCreationUsingCustomerServiceIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private static MifosCurrency oldDefaultCurrency;

    @Autowired
    private CustomerDao customerDao;

    private FiscalCalendarRules fiscalCalendarRules = new FiscalCalendarRules();
    private List<WeekDay> savedWorkingDays = fiscalCalendarRules.getWorkingDays();

    @BeforeClass
    public static void initialiseHibernateUtil() {
        oldDefaultCurrency = Money.getDefaultCurrency();
        Money.setDefaultCurrency(TestUtils.RUPEE);
        new StandardTestingService().setTestMode(TestMode.INTEGRATION);
    }

   @AfterClass
    public static void resetCurrency() {
        Money.setDefaultCurrency(oldDefaultCurrency);
    }

    @Before
    public void cleanDatabaseTables() {
        databaseCleaner.clean();
        fiscalCalendarRules.setWorkingDays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY");
    }

    @After
    public void cleanDatabaseTablesAfterTest() {
        // NOTE: - only added to stop older integration tests failing due to brittleness
        databaseCleaner.clean();
        DateTimeUtils.setCurrentMillisSystem();
        fiscalCalendarRules.setWorkingDays(weekDaysToPropertyString(savedWorkingDays));
   }

    /***********************************************
     * Weekly meeting, no fees
     ***********************************************/

    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesNoHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, new ArrayList<AccountFeesEntity>());

        // verification
        StaticHibernateUtil.flushAndClearSession();
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdDateInMoratorium() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(date(2010, 4, 19), date(2010, 4, 23), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(3), startDate.plusWeeks(4),
                startDate.plusWeeks(5), startDate.plusWeeks(6), startDate.plusWeeks(7), startDate.plusWeeks(8),
                startDate.plusWeeks(9), startDate.plusWeeks(10));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdAndFourthDatesInMoratorium() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(3).plusDays(4), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(4), startDate.plusWeeks(5),
                startDate.plusWeeks(6), startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9),
                startDate.plusWeeks(10), startDate.plusWeeks(11));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdDateInNextMeetingHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(date(2010, 4, 19), date(2010, 4, 23), RepaymentRuleTypes.NEXT_MEETING_OR_REPAYMENT);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);
        StaticHibernateUtil.flushSession();

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(3), startDate.plusWeeks(3),
                startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6), startDate.plusWeeks(7),
                startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdAndFourthDatesInNextMeetingHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(3).plusDays(4), RepaymentRuleTypes.NEXT_MEETING_OR_REPAYMENT);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(4),
                startDate.plusWeeks(4), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdDateInSameDayHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(2).plusDays(3), RepaymentRuleTypes.SAME_DAY);
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdAndFourthDatesInSameDayHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(2).plusDays(3), RepaymentRuleTypes.SAME_DAY);
        saveHoliday(startDate.plusWeeks(3), startDate.plusWeeks(3).plusDays(3), RepaymentRuleTypes.SAME_DAY);
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdDateInOneDayNextWorkingDayHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(2), RepaymentRuleTypes.NEXT_WORKING_DAY);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2).plusDays(1),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingNoFeesThirdDateInOneWeekNextWorkingDayHolidayShouldPushOutToNextMonday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(2).plusDays(4), RepaymentRuleTypes.NEXT_WORKING_DAY);
        StaticHibernateUtil.flushSession();
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting().weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();
        List<AccountFeesEntity> noAccountFees = new ArrayList<AccountFeesEntity>();

        // exercise test
        customerService.createCenter(center, weeklyMeeting, noAccountFees);

        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2).plusWeeks(1),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
    }

    /***********************************************
     * Weekly meeting, with fees
     ***********************************************/


    @Test
    public void createCenterScheduleWithWeeklyMeetingWithOnePeriodicFeeNoHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        MeetingBuilder weeklyMeetingForFees = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).startingToday();

        AmountFeeBO weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                        .withFeeAmount("100.0")
                                                        .withName("Center Weekly Periodic Fee")
                                                        .with(weeklyMeetingForFees)
                                                        .with(sampleBranchOffice())
                                                        .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        IntegrationTestObjectMother.createCenter(center, weeklyMeeting, weeklyPeriodicFeeForCenterOnly);

        // verification
        StaticHibernateUtil.flushAndClearSession();
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
        validateOnePeriodicFee(center.getGlobalCustNum(), "Center Weekly Periodic Fee",
                             100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0);
    }

    @Test
    public void createCenterScheduleWithWeeklyMeetingWithOnePeriodicFeeAndOneTimeFeeNoHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        MeetingBuilder weeklyMeetingForFees = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).startingToday();

        AmountFeeBO weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                        .withFeeAmount("100.0")
                                                        .withName("Center Weekly Periodic Fee")
                                                        .with(weeklyMeetingForFees)
                                                        .with(sampleBranchOffice())
                                                        .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);
        AmountFeeBO oneTimeFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                              .withFeeAmount("25.0")
                                                              .withName("Center UpfrontFee")
                                                              .oneTime()
                                                              .build();
        IntegrationTestObjectMother.saveFee(oneTimeFeeForCenterOnly);

        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        IntegrationTestObjectMother.createCenter(center, weeklyMeeting, weeklyPeriodicFeeForCenterOnly, oneTimeFeeForCenterOnly);

        // verification
        StaticHibernateUtil.flushAndClearSession();
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(2),
                startDate.plusWeeks(3), startDate.plusWeeks(4), startDate.plusWeeks(5), startDate.plusWeeks(6),
                startDate.plusWeeks(7), startDate.plusWeeks(8), startDate.plusWeeks(9));
        validateOnePeriodicFeeAndOneOneTimeFee(center.getGlobalCustNum(), "Center Weekly Periodic Fee","Center UpfrontFee",
                             25.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0);
    }

    @Ignore
    @Test
    public void createCenterScheduleWithWeeklyMeetingWithOnePeriodicFeeAndOneTimeFeeWithThirdAndFourthMeetingsInMoratorium() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(1)
                                                      .startingToday()
                                                      .build();
        MeetingBuilder weeklyMeetingForFees = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).startingToday();

        AmountFeeBO weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                        .withFeeAmount("100.0")
                                                        .withName("Center Weekly Periodic Fee")
                                                        .with(weeklyMeetingForFees)
                                                        .with(sampleBranchOffice())
                                                        .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);
        AmountFeeBO oneTimeFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                              .withFeeAmount("25.0")
                                                              .withName("Center UpfrontFee")
                                                              .oneTime()
                                                              .build();
        IntegrationTestObjectMother.saveFee(oneTimeFeeForCenterOnly);

        saveHoliday(startDate.plusWeeks(2), startDate.plusWeeks(3).plusDays(4), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        StaticHibernateUtil.flushSession();
        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        IntegrationTestObjectMother.createCenter(center, weeklyMeeting, weeklyPeriodicFeeForCenterOnly, oneTimeFeeForCenterOnly);

        // verification
        StaticHibernateUtil.flushAndClearSession();
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(1), startDate.plusWeeks(4),
                startDate.plusWeeks(5), startDate.plusWeeks(6), startDate.plusWeeks(7), startDate.plusWeeks(8),
                startDate.plusWeeks(9), startDate.plusWeeks(10), startDate.plusWeeks(11));
        validateOnePeriodicFeeAndOneOneTimeFee(center.getGlobalCustNum(), "Center Weekly Periodic Fee","Center UpfrontFee",
                             25.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0);
    }

    @Test
    public void createCenterScheduleWithWeeklyMeetingWithOnePeriodicFeeAndOneTimeFeeWithFirstMeetingInMoratorium() throws Exception {

        DateTime today = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(today.getMillis()); //Today is a Monday
        DateTime tomorrow = today.plusDays(1); // Start tomorrow, Tuesday

        // setup
        MeetingBO weeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(1)
                                                      .startingFrom(tomorrow.toDate())
                                                      .build();
        MeetingBuilder weeklyMeetingForFees = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).startingToday();

        AmountFeeBO weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                        .withFeeAmount("100.0")
                                                        .withName("Center Weekly Periodic Fee")
                                                        .with(weeklyMeetingForFees)
                                                        .with(sampleBranchOffice())
                                                        .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);
        AmountFeeBO oneTimeFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                              .withFeeAmount("25.0")
                                                              .withName("Center UpfrontFee")
                                                              .oneTime()
                                                              .build();
        IntegrationTestObjectMother.saveFee(oneTimeFeeForCenterOnly);

        // Declare a one-day moratorium on the first meeting day
        saveHoliday(tomorrow, tomorrow, RepaymentRuleTypes.REPAYMENT_MORATORIUM);

        CenterBO center = new CenterBuilder().with(weeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .active()
                                            .withActivationDate(tomorrow)
                                            .build();

        // exercise test
        IntegrationTestObjectMother.createCenter(center, weeklyMeeting, weeklyPeriodicFeeForCenterOnly, oneTimeFeeForCenterOnly);

        // verification
        StaticHibernateUtil.flushAndClearSession();
        validateDates(center.getGlobalCustNum(), today.plusWeeks(1), today.plusWeeks(2),
                today.plusWeeks(3), today.plusWeeks(4), today.plusWeeks(5), today.plusWeeks(6),
                today.plusWeeks(7), today.plusWeeks(8), today.plusWeeks(9), today.plusWeeks(10));
        validateOnePeriodicFeeAndOneOneTimeFee(center.getGlobalCustNum(), "Center Weekly Periodic Fee","Center UpfrontFee",
                             25.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0);
    }


    /***********************************************
     * Biweekly meeting, no fees
     ***********************************************/

    @Ignore
    @Test
    public void createCenterScheduleWithBiWeeklyMeetingNoFeesNoHoliday() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        MeetingBO biWeeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(2)
                                                      .startingToday()
                                                      .build();

        CenterBO center = new CenterBuilder().with(biWeeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        customerService.createCenter(center, biWeeklyMeeting, new ArrayList<AccountFeesEntity>());

        // verification
        validateDates(center.getGlobalCustNum(), startDate.plusWeeks(1), startDate.plusWeeks(3), startDate.plusWeeks(5), startDate.plusWeeks(7),
                startDate.plusWeeks(9), startDate.plusWeeks(11), startDate.plusWeeks(13), startDate.plusWeeks(15),
                startDate.plusWeeks(17), startDate.plusWeeks(19));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithBiWeeklyMeetingNoFeesThirdDateInMoratorium() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(4), startDate.plusWeeks(4).plusDays(3), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        StaticHibernateUtil.flushSession();
        MeetingBO biWeeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(2)
                                                      .startingToday()
                                                      .build();

        CenterBO center = new CenterBuilder().with(biWeeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        customerService.createCenter(center, biWeeklyMeeting, new ArrayList<AccountFeesEntity>());

        // verification
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(2), startDate.plusWeeks(6), startDate.plusWeeks(8),
                startDate.plusWeeks(10), startDate.plusWeeks(12), startDate.plusWeeks(14), startDate.plusWeeks(16),
                startDate.plusWeeks(18), startDate.plusWeeks(20));
    }

    @Ignore
    @Test
    public void createCenterScheduleWithBiWeeklyMeetingNoFeesThirdAndFifthDatesInMoratorium() throws Exception {

        DateTime startDate = date(2010, 4, 5);
        DateTimeUtils.setCurrentMillisFixed(startDate.getMillis()); //Monday

        // setup
        saveHoliday(startDate.plusWeeks(4), startDate.plusWeeks(4).plusDays(3), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        saveHoliday(startDate.plusWeeks(8), startDate.plusWeeks(8).plusDays(3), RepaymentRuleTypes.REPAYMENT_MORATORIUM);
        StaticHibernateUtil.flushSession();
        MeetingBO biWeeklyMeeting = new MeetingBuilder().customerMeeting()
                                                      .weekly()
                                                      .every(2)
                                                      .startingToday()
                                                      .build();

        CenterBO center = new CenterBuilder().with(biWeeklyMeeting)
                                            .withName("Center-IntegrationTest")
                                            .with(sampleBranchOffice())
                                            .withLoanOfficer(testUser()).withUserContext()
                                            .build();

        // exercise test
        customerService.createCenter(center, biWeeklyMeeting, new ArrayList<AccountFeesEntity>());

        // verification
        validateDates(center.getGlobalCustNum(), startDate, startDate.plusWeeks(2), startDate.plusWeeks(6),
                startDate.plusWeeks(10), startDate.plusWeeks(12), startDate.plusWeeks(14), startDate.plusWeeks(16),
                startDate.plusWeeks(18), startDate.plusWeeks(20), startDate.plusWeeks(22));
    }

    /****************************************
     * Helper methods
     ****************************************/


    private DateTime date (int year, int month, int day) {
        return new DateTime().withDate(year, month, day).toDateMidnight().toDateTime();
    }

    private String weekDaysToPropertyString(List<WeekDay> weekDays) {
        String propertyString = "";
        Boolean first = true;
        for (WeekDay day : weekDays) {
            if (!first) {
                propertyString = propertyString + ",";
            } else {
                first = false;
            }
            propertyString = propertyString + day.toString();
        }
        return propertyString;
    }

    private void validateDates (String centerSystemId, DateTime... expectedDates) {
        CenterBO retrievedCenter = customerDao.findCenterBySystemId(centerSystemId);
        Set<AccountActionDateEntity> actionDateEntities = retrievedCenter.getCustomerAccount().getAccountActionDates();
        List<DateTime> actualDates = new ArrayList<DateTime>();
        for (AccountActionDateEntity entity : actionDateEntities) {
            actualDates.add(new DateTime(entity.getActionDate()));
        }
        Collections.sort(actualDates);
        Assert.assertEquals(expectedDates.length, actualDates.size());
        for (short i = 0; i < actualDates.size(); i++) {

            Assert.assertEquals("Date " + (i+1) + "'s date is wrong.",
                                expectedDates[i], actualDates.get(i));
        }
    }

    private void validateOnePeriodicFee (String centerSystemId, String expectedPeriodicFeeName, double...expectedFees) {
        CenterBO retrievedCenter = customerDao.findCenterBySystemId(centerSystemId);
        for (AccountActionDateEntity accountActionDate : getActionDatesSortedByDate(retrievedCenter.getCustomerAccount())) {
            CustomerScheduleEntity scheduleEntity = (CustomerScheduleEntity) accountActionDate;
            assertThat(scheduleEntity.getAccountFeesActionDetails().size(), is(1));
            assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFee().getFeeName(),
                    is(expectedPeriodicFeeName));
            assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmount().getAmountDoubleValue(),
                    is(expectedFees[scheduleEntity.getInstallmentId()-1]));
        }
    }

    private void validateOnePeriodicFeeAndOneOneTimeFee (String centerSystemId, String expectedPeriodicFeeName,
            String expectedOneTimeFeeName, double expectedOneTimeFee, double...expectedFees) {
        CenterBO retrievedCenter = customerDao.findCenterBySystemId(centerSystemId);
        for (AccountActionDateEntity accountActionDate : getActionDatesSortedByDate(retrievedCenter.getCustomerAccount())) {
            CustomerScheduleEntity scheduleEntity = (CustomerScheduleEntity) accountActionDate;
            if (scheduleEntity.getInstallmentId() == 1) {
                assertThat(scheduleEntity.getAccountFeesActionDetails().size(), is(2));
                assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(1).getFee().getFeeName(),
                        is(expectedOneTimeFeeName));
                assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(1).getFeeAmount().getAmountDoubleValue(),
                        is(expectedOneTimeFee));
            } else {
                assertThat(scheduleEntity.getAccountFeesActionDetails().size(), is(1));
            }
            assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFee().getFeeName(),
                    is(expectedPeriodicFeeName));
            assertThat(scheduleEntity.getAccountFeesActionDetailsSortedByFeeId().get(0).getFeeAmount().getAmountDoubleValue(),
                    is(expectedFees[scheduleEntity.getInstallmentId()-1]));
        }
    }

    private void saveHoliday (DateTime start, DateTime through, RepaymentRuleTypes rule) throws Exception {
        HolidayDetails holidayDetails = new HolidayDetails("testHoliday", start.toDate(), through.toDate(), rule.getValue());
        List<Short> officeIds = new LinkedList<Short>();
        officeIds.add((short)1);
        IntegrationTestObjectMother.createHoliday(holidayDetails, officeIds);
    }

    private List<AccountActionDateEntity> getActionDatesSortedByDate(CustomerAccountBO customerAccount) {
        List<AccountActionDateEntity> sortedList = new ArrayList<AccountActionDateEntity>();
        sortedList.addAll(customerAccount.getAccountActionDates());
        Collections.sort(sortedList);
        return sortedList;
    }

}
