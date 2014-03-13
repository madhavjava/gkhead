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

package org.mifos.schedule.internal;

import org.joda.time.DateTime;
import org.mifos.calendar.CalendarUtils;

public class DailyScheduledEvent extends AbstractScheduledEvent {

    private final int every;

    public DailyScheduledEvent(final int every) {
        this.every = every;
    }

    @Override
	public DateTime nextEventDateAfter(final DateTime startDate) {
        return CalendarUtils.getNextDateForDay(startDate, every);
    }

    @Override
    public DateTime nearestMatchNotTakingIntoAccountScheduleFrequency(DateTime startDate) {
        return CalendarUtils.getNextDateForDay(startDate, every);
    }
    
    @Override
	public DateTime nearestMatchingDateBeginningAt(final DateTime startDate) {

        return CalendarUtils.getNextDateForDay(startDate, every);
    }

    @Override
    public int getEvery() {
        return every;
    }

    @Override
    public DateTime rollFrowardDateByFrequency(DateTime date) {
        return date.plusDays(this.every);
    }
}