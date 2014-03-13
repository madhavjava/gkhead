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

package org.mifos.config;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import junit.framework.Assert;

import org.joda.time.Days;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.calendar.DayOfWeek;
import org.mifos.config.business.MifosConfigurationManager;
import org.mifos.framework.util.LocalizationConverter;

public class FiscalCalendarRulesTest {


    private static MifosConfigurationManager configMgr = null;

    @BeforeClass
    public static void init() {
        configMgr = MifosConfigurationManager.getInstance();
    }

    @AfterClass
    public static void destroy(){
        configMgr.clear();
    }

    private void setNewWorkingDays(final String newWorkingDays) {
        configMgr.setProperty("FiscalCalendarRules.WorkingDays", newWorkingDays);
        new FiscalCalendarRules().reloadConfigWorkingDays();
    }

    @Test
    public void shouldConvertWorkingDaysOfMifosWeekDayToJodaTimeDays() {

        // setup
        String configWorkingDays = "MONDAY,WEDNESDAY,SATURDAY,SUNDAY";
        setNewWorkingDays(configWorkingDays);

        // exercise test
        List<Days> workingDays = new FiscalCalendarRules().getWorkingDaysAsJodaTimeDays();

        assertThat(workingDays, hasItem(DayOfWeek.mondayAsDay()));
        assertThat(workingDays, hasItem(DayOfWeek.wednesdayAsDay()));
        assertThat(workingDays, hasItem(DayOfWeek.saturdayAsDay()));
        assertThat(workingDays, hasItem(DayOfWeek.sundayAsDay()));
    }

    @Test
    public void testGetWorkingDays() {
        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY";
        setNewWorkingDays(configWorkingDays);
        List<WeekDay> workingDays = new FiscalCalendarRules().getWorkingDays();
       Assert.assertEquals(workingDays.size(), 6);
        WeekDay[] weekDays = WeekDay.values();
        for (int i = 0; i < workingDays.size(); i++) {
            Assert.assertEquals(workingDays.get(i).toString(), weekDays[i + 1].name());
        }
        configWorkingDays = "TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        workingDays = new FiscalCalendarRules().getWorkingDays();
       Assert.assertEquals(workingDays.size(), 4);
        for (int i = 0; i < workingDays.size(); i++) {
            Assert.assertEquals(workingDays.get(i).toString().toUpperCase(), weekDays[i + 2].name().toUpperCase());
        }
    }

    @Test
    public void testGetWeekDaysList() {
        List<WeekDay> weekDaysFromFiscalCalendarRules = new FiscalCalendarRules().getWeekDaysList();
        WeekDay[] weekDays = WeekDay.values();
        for (int i = 0; i < weekDays.length; i++) {
            Assert.assertEquals(weekDaysFromFiscalCalendarRules.get(i).toString(), weekDays[i].name());
        }
    }

    @Test
    public void testGetWeekDayOffList() {
        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        List<Short> list = new FiscalCalendarRules().getWeekDayOffList();
       Assert.assertEquals(list.size(), 2);
        Short dayOff = 1;
       Assert.assertEquals(list.get(0), dayOff);
    }

    @Test
    public void testIsWorkingDay() {
        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        // get the supported ids for GMT-08:00 (Pacific Standard Time)
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        // if no ids were returned, something is wrong. get out.
        // Otherwise get an id for Pacific Standard Time
        String pstId = ids[0];

        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, pstId);

        // set up rules for daylight savings time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        Calendar calendar = new GregorianCalendar(pdt);
        try {
            Locale savedLocale = Localization.getInstance().getConfiguredLocale();
            new LocalizationConverter().setCurrentLocale(Locale.US);
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
            // Keith: DateFormat must be set to the same timezone as the
            // calendar
            // Otherwise dates don't roll over at the same exact time, causing
            // this and several other unit tests to fail
            df.setTimeZone(TimeZone.getTimeZone(pstId));
            df.applyPattern("yyyy/MM/dd");
            Date thursday = df.parse("2007/10/11");
            calendar.setTime(thursday);
            String out = thursday.toString();
            out.contains("A");
            new LocalizationConverter().setCurrentLocale(savedLocale);
        } catch (Exception e) {

        }

       Assert.assertTrue(new FiscalCalendarRules().isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Friday
       Assert.assertTrue(new FiscalCalendarRules().isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Sat
       Assert.assertTrue(!new FiscalCalendarRules().isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Sunday
       Assert.assertTrue(!new FiscalCalendarRules().isWorkingDay(calendar));
    }

    @Test
    public void testGetStartOfWeek() {
        Short startOfWeekDay = new FiscalCalendarRules().getStartOfWeek();
        Short start = 2;
       Assert.assertEquals(startOfWeekDay, start);
    }

    @Test
    public void testGetScheduleTypeForMeetingOnHoliday() {
        String scheduleType = new FiscalCalendarRules().getScheduleMeetingIfNonWorkingDay();
       Assert.assertEquals(scheduleType.toUpperCase(), "same_day".toUpperCase());
    }

}
