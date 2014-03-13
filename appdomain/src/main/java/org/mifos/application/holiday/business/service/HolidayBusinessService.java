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

package org.mifos.application.holiday.business.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.mifos.application.holiday.business.HolidayBO;
import org.mifos.config.FiscalCalendarRules;
import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.security.util.UserContext;

/**
 * remove usage of this in favour of {@link HolidayService}.
 */
@Deprecated
public class HolidayBusinessService implements BusinessService {

    @Override
    public AbstractBusinessObject getBusinessObject(final UserContext userContext) {
        return null;
    }

    public List<HolidayBO> getAllPushOutHolidaysContaining (Date date) {
        //TODO: implement this
        return null;
    }

    public HolidayBO findNonPushOutHolidayContaining (Date date) {
        //TODO: implement
        return null;
    }

    public boolean isWorkingDay(Date day) throws RuntimeException {
        return new FiscalCalendarRules().isWorkingDay(DateUtils.getCalendar(day));
    }

    public boolean isWorkingDay(Calendar day) throws RuntimeException {
        return new FiscalCalendarRules().isWorkingDay(day);
    }

    public Date getNextWorkingDay(Date day) {
        Calendar calendarDay = DateUtils.getCalendar(day);
        do {
            calendarDay.add(Calendar.DATE, 1);
        } while (!isWorkingDay(calendarDay));
        return calendarDay.getTime();
    }

    /**
     * Get the first working day of the week that the given day is in.
     *
     * Precondition: The given day is a working day.
     *
     * @return the given day, if it's the first working day of the week, otherwise
     * back up to the first working day of the week.
     */
    public Date getFirstWorkingDayOfWeekForDate (Date day) {
        if (!isWorkingDay(day)) {
            throw new RuntimeException("Day must be a working day");
        }
        final GregorianCalendar firstDateForWeek = new GregorianCalendar();
        firstDateForWeek.setTime(day);
        //back up to first non-working day
        while (isWorkingDay (firstDateForWeek)) {
            firstDateForWeek.add(Calendar.DAY_OF_WEEK, -1);
        }
        //then move forward to first working day
        firstDateForWeek.add(Calendar.DAY_OF_WEEK, 1);
        return firstDateForWeek.getTime();
    }

}
