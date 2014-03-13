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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.sampleBranchOffice;
import static org.mifos.framework.util.helpers.IntegrationTestObjectMother.testUser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.accounts.util.helpers.InstallmentDate;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.customers.business.CustomerAccountBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.domain.builders.CenterBuilder;
import org.mifos.domain.builders.FeeBuilder;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.test.framework.util.DatabaseCleaner;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountGetFeeDatesIntegrationTest extends MifosIntegrationTestCase {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    private MeetingBO weeklyMeeting;
    private AmountFeeBO weeklyPeriodicFeeForCenterOnly;
    private CenterBO center;

    @Before
    public void cleanDatabaseTables() {
        databaseCleaner.clean();
    }

    @After
    public void cleanDatabaseTablesAfterTest() {
        // NOTE: - only added to stop older integration tests failing due to brittleness
        databaseCleaner.clean();
    }

    @Test
    public void getScheduledDatesForFeesForGivenCustomerForWeeklySchedules() throws Exception {

        // setup
        DateTime firstTuesdayInstallmentDate = new DateMidnight().toDateTime().withDate(2010, 4, 20);
        weeklyMeeting = new MeetingBuilder().customerMeeting().weekly().every(1).withStartDate(firstTuesdayInstallmentDate).build();
        IntegrationTestObjectMother.saveMeeting(weeklyMeeting);

        MeetingBuilder weeklyMeetingForFees = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).withStartDate(firstTuesdayInstallmentDate);

        weeklyPeriodicFeeForCenterOnly = new FeeBuilder().appliesToCenterOnly()
                                                        .withFeeAmount("100.0")
                                                        .withName("Center Weekly Periodic Fee")
                                                        .with(weeklyMeetingForFees)
                                                        .with(sampleBranchOffice())
                                                        .build();
        IntegrationTestObjectMother.saveFee(weeklyPeriodicFeeForCenterOnly);

        center = new CenterBuilder().with(weeklyMeeting).withName("Center").with(sampleBranchOffice())
                .withLoanOfficer(testUser()).build();
        IntegrationTestObjectMother.createCenter(center, weeklyMeeting, weeklyPeriodicFeeForCenterOnly);

        center = customerDao.findCenterBySystemId(center.getGlobalCustNum());

        DateTime meetingStartDate = firstTuesdayInstallmentDate.minusDays(7);
        MeetingBO feeMeetingFrequency = new MeetingBuilder().periodicFeeMeeting().weekly().every(1).occuringOnA(WeekDay.MONDAY).startingFrom(meetingStartDate.toDate()).build();

        InstallmentDate installment1 = new InstallmentDate(Short.valueOf("1"), firstTuesdayInstallmentDate.toDate());
        InstallmentDate installment2 = new InstallmentDate(Short.valueOf("2"), firstTuesdayInstallmentDate.plusWeeks(1).toDate());
        InstallmentDate installment3 = new InstallmentDate(Short.valueOf("3"), firstTuesdayInstallmentDate.plusWeeks(2).toDate());
        InstallmentDate installment4 = new InstallmentDate(Short.valueOf("4"), firstTuesdayInstallmentDate.plusWeeks(3).toDate());

        List<InstallmentDate> installmentDates = Arrays.asList(installment1, installment2, installment3, installment4);

        // exercise test
        CustomerAccountBO customerAccount = center.getCustomerAccount();
        List<Date> feeDates = customerAccount.getFeeDates(feeMeetingFrequency, installmentDates);

        // verification
        assertThat(feeDates.get(0), is(firstTuesdayInstallmentDate.toDate()));
        assertThat(feeDates.get(1), is(firstTuesdayInstallmentDate.plusWeeks(1).toDate()));
        assertThat(feeDates.get(2), is(firstTuesdayInstallmentDate.plusWeeks(2).toDate()));
        assertThat(feeDates.get(3), is(firstTuesdayInstallmentDate.plusWeeks(3).toDate()));
    }
}
