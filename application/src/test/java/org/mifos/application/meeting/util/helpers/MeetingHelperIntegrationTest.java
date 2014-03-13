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

package org.mifos.application.meeting.util.helpers;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;

public class MeetingHelperIntegrationTest extends MifosIntegrationTestCase {

    private MeetingHelper helper = new MeetingHelper();

    @Test
    public void testGetWeekMessage() throws Exception {
        String expected = "Recur every 5 Week(s) on Monday";
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);
        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getMessage(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testNoSave() throws Exception {
        String expected = "Recur every 5 Week(s) on Monday";
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        Assert.assertEquals(expected, helper.getMessage(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testGetMonthMessage() throws Exception {
        String expected = "Recur on First Monday of every 5 month(s)";
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, RankOfDay.FIRST, (short) 5, new Date(),
                MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getMessage(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testGetMonthlyOnDayMessage() throws Exception {
        String expected = "Recur on day 7 of every 2 month(s)";
        MeetingBO meeting = new MeetingBO((short) 7, (short) 2, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getMessage(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testGetWeekFrequency() throws Exception {
        String expected = "5 week(s)";
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getMessageWithFrequency(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testGetMonthFrequecny() throws Exception {
        String expected = "5 month(s)";
        MeetingBO meeting = new MeetingBO((short) 7, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getMessageWithFrequency(meeting, TestUtils.makeUser()));
    }

    @Test
    public void testGetDetailWeekFrequency() throws Exception {
        String expected = "Recur every 5 week(s)";
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
    }

    @Test
    public void testGetDetailMonthFrequecny() throws Exception {
        String expected = "Recur every 5 month(s)";
        MeetingBO meeting = new MeetingBO((short) 7, (short) 5, new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertEquals(expected, helper.getDetailMessageWithFrequency(meeting, TestUtils.makeUser()));
    }

}