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

package org.mifos.schedule;

import java.util.List;

import org.joda.time.DateTime;

public interface ScheduledDateGeneration {

    /**
     * Create a schedule for the scheduledEvent, starting with the first scheduled date after lastScheduledDate
     * and continuing for the total number of occurrences specified.
     */
    List<DateTime> generateScheduledDates(int occurences, DateTime lastScheduledDate, ScheduledEvent scheduledEvent, boolean isCustomerSchedule);

    /**
     * Create a schedule for the scheduledEvent, starting with the first scheduled date after lastScheduledDate
     * and continuing to the latest scheduled date up to and including throughDate.
     * @return
     */
    List<DateTime> generateScheduledDatesThrough(DateTime lastScheduledDate, DateTime throughDate, ScheduledEvent scheduledEvent, boolean isCustomerSchedule);

}
