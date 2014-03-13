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

package org.mifos.application.meeting.persistence;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;

public class MeetingPersistenceIntegrationTest extends MifosIntegrationTestCase {

    @Test
    public void testGetMeeting() throws Exception {
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, Short.valueOf("5"), new Date(), MeetingType.CUSTOMER_MEETING,
                "Delhi");

        IntegrationTestObjectMother.saveMeeting(meeting);

        meeting = IntegrationTestObjectMother.getMeeting(meeting.getMeetingId());
        Assert.assertNotNull(meeting);
        Assert.assertEquals(Short.valueOf("5"), meeting.getMeetingDetails().getRecurAfter());
        Assert.assertEquals(WeekDay.MONDAY, meeting.getMeetingDetails().getWeekDay());
    }
}
