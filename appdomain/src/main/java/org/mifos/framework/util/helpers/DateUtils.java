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

package org.mifos.framework.util.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifos.application.admin.servicefacade.InvalidDateException;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.framework.exceptions.FrameworkRuntimeException;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.LocalizationConverter;

public class DateUtils {

    // three different types of date direction validation
    public enum DIRECTION {
        FUTURE, PAST, NONE
    }

    /*
     * Date parsing with a pattern does not work correctly in all Locales (as
     * documented here:
     * http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat
     * .html#SimpleDateFormat%28java.lang.String%29 so when parsing with a
     * pattern force a locale that is know to work with patterns
     */
    private static final Locale dateLocale = new Locale("en", "GB");
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", dateLocale);

    private static final String dbFormat = "yyyy-MM-dd";

    private static Locale internalLocale = Locale.UK;
    private static String dateSeparator = new LocalizationConverter().getDateSeparatorForCurrentLocale();

    public static void refreshInternalLocale() {
        internalLocale = Locale.UK;
    }

    public static String getShortDateFormat(Locale locale) {
        String dateSeparator = DateUtils.getDateSeparatorByLocale(locale, DateFormat.MEDIUM);
        return String.format("dd%sMMM%syyyy", dateSeparator, dateSeparator);
    }


    public static boolean sameMonthYear(Date firstDate, Date secondDate) {
        Calendar first = Calendar.getInstance();
        first.setTime(firstDate);
        Calendar second = Calendar.getInstance();
        second.setTime(secondDate);
        return sameMonthYear(first,second);
    }

    public static boolean sameMonthYear(Calendar first, Calendar second) {
        return ((first.get(Calendar.MONTH) == second.get(Calendar.MONTH)) && (first.get(Calendar.YEAR) == second.get(Calendar.YEAR)));
    }

    public static String convertUserToDbFmt(String userDate, String userPattern) throws InvalidDateException {
        try {
            SimpleDateFormat userFormat = new SimpleDateFormat(userPattern, dateLocale);
            // userFormat.setLenient(false);
            java.util.Date date = userFormat.parse(userDate);
            return toDatabaseFormat(date);
        } catch (ParseException e) {
            throw new InvalidDateException(userDate);
        }
    }

    public static String getDateSeparator() {
        return dateSeparator;
    }

    public static String convertDbToUserFmt(String dbDate, String userPattern) throws InvalidDateException {
        try {
            SimpleDateFormat databaseFormat = new SimpleDateFormat(dbFormat, dateLocale);
            java.util.Date date = databaseFormat.parse(dbDate);
            SimpleDateFormat userFormat = new SimpleDateFormat(userPattern);
            return userFormat.format(date);
        }

        catch (ParseException e) {
            throw new InvalidDateException(dbDate, e);
        }
    }

    public static String getUserLocaleDate(Locale locale, Date databaseDate) {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        locale = internalLocale;
        SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        return shortFormat.format(databaseDate);
    }

    public static String getUserLocaleDate(Locale locale, String databaseDate) {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        locale = internalLocale;
        if (locale != null && databaseDate != null && !databaseDate.equals("")) {
            try {
                SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
                String userfmt = convertToCurrentDateFormat(shortFormat.toPattern());
                return convertDbToUserFmt(databaseDate, userfmt);
            } catch (FrameworkRuntimeException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("databaseDate=" + databaseDate + ", locale=" + locale);
                throw new FrameworkRuntimeException(e);
            }
        } else {
            return "";
        }
    }

    public static String getUserLocaleDate(String databaseDate) {
        if (internalLocale != null && databaseDate != null && !databaseDate.equals("")) {
            try {
                SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
                        internalLocale);
                String userfmt = convertToCurrentDateFormat(shortFormat.toPattern());
                return convertDbToUserFmt(databaseDate, userfmt);
            } catch (FrameworkRuntimeException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("databaseDate=" + databaseDate + ", locale=" + internalLocale);
                throw new FrameworkRuntimeException(e);
            }
        } else {
            return "";
        }
    }

    public static Date getDate(String value) {
        return getDate(value, dateLocale, "dd" + dateSeparator + "MM" + dateSeparator + "yyyy");
    }

    public static Date getDate(String value, Locale dateLocale, String formatStr) {
        if (value != null && !value.equals("")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(formatStr, dateLocale);
                format.setLenient(false);
                return format.parse(value);
            } catch (Exception e) {
                throw new FrameworkRuntimeException(e);
            }
        }

        return null;
    }

    /**
     * Converts a string of a date in DB format ("yyyy-MM-dd") to a
     * {@link java.util.Date} object.
     *
     * @param date
     *            , a String of the form "yyyy-MM-dd"
     * @return A {@link java.util.Date} object equivilant to the value of the
     *         parameter.
     */
    public static java.util.Date getDateAsRetrievedFromDb(String date) {
        if (date != null && !date.equals("")) {
            SimpleDateFormat format = new SimpleDateFormat(dbFormat, dateLocale);
            try {
                return format.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    public static String getCurrentDate() throws InvalidDateException {
        Calendar currentCalendar = getCurrentDateCalendar();
        java.sql.Date currentDate = new java.sql.Date(currentCalendar.getTimeInMillis());
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, internalLocale);
        String userfmt = convertToCurrentDateFormat(format.toPattern());
        return convertDbToUserFmt(currentDate.toString(), userfmt);
    }

    private static Calendar getCurrentDateCalendar() {
        return new DateTimeService().getCurrentDateMidnight().toGregorianCalendar();
    }

    public static Date currentDate() {
        return getCurrentDateCalendar().getTime();
    }

    // should be removed and the setCurrentDate() should be used
    public static String getCurrentDate(Locale locale) throws InvalidDateException {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        locale = internalLocale;
        Calendar currentCalendar = getCurrentDateCalendar();
        java.sql.Date currentDate = new java.sql.Date(currentCalendar.getTimeInMillis());
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String userfmt = convertToCurrentDateFormat(format.toPattern());
        return convertDbToUserFmt(currentDate.toString(), userfmt);
    }

    public static String getLocalDateString(DateTime date, Locale locale) throws InvalidDateException {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        locale = internalLocale;
        Calendar calendar = date.toCalendar(locale);
        java.sql.Date currentDate = new java.sql.Date(calendar.getTimeInMillis());
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String userfmt = convertToCurrentDateFormat(format.toPattern());
        return convertDbToUserFmt(currentDate.toString(), userfmt);
    }

    public static String convertToCurrentDateFormat(String pattern) {
        char chArray[] = pattern.toCharArray();
        StringBuilder fmt = new StringBuilder();
        boolean d = false;
        boolean m = false;
        boolean y = false;
        String separator = dateSeparator;
        for (char element : chArray) {
            if ((element == 'd' || element == 'D') && !d) {
                fmt.append("dd");
                d = true;
                fmt.append(separator);
            } else if ((element == 'm' || element == 'M') && !m) {
                fmt.append("MM");
                m = true;
                fmt.append(separator);
            } else if ((element == 'y' || element == 'Y') && !y) {
                fmt.append("yyyy");
                y = true;
                fmt.append(separator);
            }
        }
        return fmt.substring(0, fmt.length() - 1);
    }

    public static String convertToMFIFormat(String date, String format) {
        String MFIString;
        String MFIfmt = getMFIFormat();
        String day = "";
        String month = "";
        String year = "";
        String token;
        String separator = new LocalizationConverter().getDateSeparatorForCurrentLocale();

        MFIfmt = convertToDateTagFormat(MFIfmt);
        StringTokenizer stfmt = new StringTokenizer(format, separator);
        StringTokenizer stdt = new StringTokenizer(date, separator);
        while (stfmt.hasMoreTokens() && stdt.hasMoreTokens()) {
            token = stfmt.nextToken();
            if (token.equalsIgnoreCase("D")) {
                day = stdt.nextToken();
            } else if (token.equalsIgnoreCase("M")) {
                month = stdt.nextToken();
            } else {
                year = stdt.nextToken();
            }
        }
        MFIString = createDateString(day, month, year, MFIfmt);
        return MFIString;
    }

    public static java.sql.Date parseBrowserDateFields(String yearStr, String monthStr, String dayStr)
            throws InvalidDateException {
        return getDateAsSentFromBrowser(dayStr + dateSeparator + monthStr + dateSeparator + yearStr);
    }

    /**
     * "as sent from browser" is a bit of a misnomer; it really is (at least in
     * many cases), as formatted by a routine on the server side like
     * ClientCustActionForm#getDateOfBirth()
     *
     * @throws InvalidDateException
     */
    public static java.sql.Date getDateAsSentFromBrowser(String value) throws InvalidDateException {
        if (value == null || value == "") {
            return null;
        }
        try {
            String formatStr = "d" + dateSeparator + "M" + dateSeparator + "yy";
            SimpleDateFormat format = new SimpleDateFormat(formatStr, internalLocale);

            format.setLenient(false);
            return new java.sql.Date(format.parse(value).getTime());
        }

        catch (ParseException e) {
            throw new InvalidDateException(value);
        }
    }

    // validate a date string according to UK D/M/Y format, our internal
    // standard
    public static boolean isValidDate(String value) {
        try {
            SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
                    internalLocale);
            shortFormat.setLenient(false);
            shortFormat.parse(value);
            return true;
        }

        catch (java.text.ParseException e) {
            return false;
        }
    }

    public static java.sql.Date getLocaleDate(String value) {
        if (internalLocale != null && value != null && !value.equals("")) {
            try {
                SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
                        internalLocale);
                shortFormat.setLenient(false);
                String userPattern = shortFormat.toPattern();
                String dbDate = convertUserToDbFmt(value, userPattern);
                return java.sql.Date.valueOf(dbDate);
            } catch (RuntimeException alreadyRuntime) {
                throw alreadyRuntime;
            } catch (Exception e) {
                throw new FrameworkRuntimeException(e);
            }
        } else {
            return null;
        }
    }

    public static java.sql.Date getLocaleDate(Locale locale, String value) throws InvalidDateException {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        locale = internalLocale;
        java.sql.Date result = null;
        if (locale != null && StringUtils.isNotBlank(value)) {
            SimpleDateFormat shortFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
            shortFormat.setLenient(false);
            String userPattern = shortFormat.toPattern();
            String dbDate = convertUserToDbFmt(value, userPattern);
            result = java.sql.Date.valueOf(dbDate);
        }
        return result;
    }

    public static String getMFIFormat() {
        // TODO change this to pick from app config
        String formatStr = "dd" + dateSeparator + "mm" + dateSeparator + "yy";

        return formatStr;
    }

    public static String getMFIShortFormat() {
        String formatStr = "dd" + dateSeparator + "mm" + dateSeparator + "yy";

        return formatStr;
    }

    public static String convertToDateTagFormat(String pattern) {
        char chArray[] = pattern.toCharArray();
        String separator = dateSeparator;

        StringBuilder fmt = new StringBuilder();
        boolean d = false;
        boolean m = false;
        boolean y = false;
        for (char element : chArray) {
            if ((element == 'd' || element == 'D') && !d) {
                fmt.append("D" + separator);
                d = true;
            } else if ((element == 'm' || element == 'M') && !m) {
                fmt.append("M" + separator);
                m = true;
            } else if ((element == 'y' || element == 'Y') && !y) {
                fmt.append("Y" + separator);
                y = true;
            }
        }

        return fmt.substring(0, fmt.length() - 1);
    }

    public static String getSeparator(String pattern) {
        char chArray[] = pattern.toCharArray();
        for (char element : chArray) {
            if (element != 'd' && element != 'D' && element != 'm' && element != 'M' && element != 'y'
                    && element != 'Y') {
                return String.valueOf(element);
            }
        }
        return "";
    }

    public static String createDateString(String day, String month, String year, String format) {
        String separator = dateSeparator;

        StringTokenizer stfmt = new StringTokenizer(format, separator);
        String token;
        StringBuilder dt = new StringBuilder();
        while (stfmt.hasMoreTokens()) {
            token = stfmt.nextToken();
            if (token.equals("D")) {
                dt.append(day + separator);
            } else if (token.equals("M")) {
                dt.append(month + separator);
            } else {
                dt.append(year + separator);
            }
        }

        return dt.deleteCharAt((dt.length() - 1)).toString();
    }

    public static int DateDiffInYears(java.sql.Date fromDate) {
        Calendar fromDateCal = new GregorianCalendar();

        fromDateCal.setTime(fromDate);

        // Create a calendar object with today's date
        Calendar today = getCurrentDateCalendar();
        // Get age based on year
        int age = today.get(Calendar.YEAR) - fromDateCal.get(Calendar.YEAR);
        int monthDiff = (today.get(Calendar.MONTH) + 1) - (fromDateCal.get(Calendar.MONTH) + 1);
        int dayDiff = today.get(Calendar.DAY_OF_MONTH) - fromDateCal.get(Calendar.DAY_OF_MONTH);
        // If this year's birthday has not happened yet, subtract one from age
        if (monthDiff < 0) {
            age--;
        } else if (monthDiff == 0) {
            if (dayDiff < 0) {
                age--;
            }
        }
        return age;
    }

    public static String getDateSeparatorByLocale(Locale dateLocale, int dateFormat) {
        return new LocalizationConverter().getDateSeparator(dateLocale, dateFormat);
    }

    public static String getDBtoUserFormatString(java.util.Date dbDate, Locale userLocale) {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        userLocale = internalLocale;
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, userLocale);
        return format.format(dbDate);
    }

    public static String getDBtoUserFormatShortString(java.util.Date dbDate, Locale userLocale) {
        // the following line is for 1.1 release and will be removed when date
        // is localized
        userLocale = internalLocale;
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, userLocale);
        return format.format(dbDate);
    }

    /**
     * This method is based on the system's time zone, not, say, the time zone
     * where the user is. That might be dubious.
     */
    public static String toDatabaseFormat(java.util.Date date) {
        DateFormat format = new SimpleDateFormat(dbFormat);
        // format.setTimeZone(TimeZone.getTimeZone("GMT+0530"));
        return format.format(date);
    }

    // is a given date before today, today, or after today?
    // value < 0 : before today
    // value == 0 : today
    // value > 0 : after today
    public static int whichDirection(Date date) {
        Calendar currentCalendar = getCurrentDateCalendar();
        java.sql.Date currentDate = new java.sql.Date(currentCalendar.getTimeInMillis());

        // check if the date is before now

        int result = date.compareTo(currentDate);
        return result;
    }

    public static String makeDateAsSentFromBrowser() {
        return makeDateAsSentFromBrowser(getCurrentJavaDateTime());
    }

    // TODO: Needs a unit test - buddy/johnvic
    public static Date getCurrentJavaDateTime() {
        return new DateTimeService().getCurrentJavaDateTime();
    }

    public static String makeDateAsSentFromBrowser(Date date) {
        String formatStr = "d" + dateSeparator + "M" + dateSeparator + "yyyy";

        SimpleDateFormat format = new SimpleDateFormat(formatStr, internalLocale);
        return format.format(date);
    }

    public static java.util.Date getCurrentDateWithoutTimeStamp() {
        return getDateWithoutTimeStamp(getCurrentTime());
    }

    public static Date getCurrentPlusWeeksDateWithoutTimeStamp(int weeks) {
        return getDateWithoutTimeStamp(getCurrentTime() + (604800000 * weeks));
    }

    private static long getCurrentTime() {
        return new DateTimeService().getCurrentDateTime().getMillis();
    }

    public static Date getDateWithoutTimeStamp(long timeInMills) {
        return getDateWithoutTimeStamp(new Date(timeInMills));
    }

    public static Date getDateWithoutTimeStamp(Date date) {
        return org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE);
    }

    public static Calendar getCalendarDate(long timeInMills) {
        Calendar dateCalendar = new GregorianCalendar();
        dateCalendar.setTimeInMillis(timeInMills);
        return dateCalendar;
    }

    public static Date getLastDayOfCurrentYear() {
        Calendar cal = getCurrentDateCalendar();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
        Calendar cal1 = getCurrentDateCalendar();
        cal1.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return new Date(cal1.getTimeInMillis());
    }

    public static Calendar getFistDayOfNextYear(Calendar cal) {
        cal.roll(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DATE));
        Calendar cal1 = new DateTimeService().getCurrentDateTime().toGregorianCalendar();
        cal1.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return cal1;
    }

    public static Date getLastDayOfNextYear() {
        Calendar cal = getCurrentDateCalendar();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
        Calendar cal1 = getCurrentDateCalendar();
        cal1.set(cal.get(Calendar.YEAR) + 1, cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return cal1.getTime();
    }

    public static Calendar getLastDayOfYearAfterNextYear() {
        Calendar cal = getCurrentDateCalendar();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
        Calendar cal1 = getCurrentDateCalendar();
        cal1.set(cal.get(Calendar.YEAR) + 2, cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return cal1;
    }

    public static Date getCurrentDateOfNextYearWithOutTimeStamp() {
        Calendar currentDateCalendar = getCurrentDateCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year + 1, month, day);
        return new Date(currentDateCalendar.getTimeInMillis());
    }

    public static Calendar getFistDayOfYearAfterNextYear() {
        Calendar cal = getCurrentDateCalendar();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DATE));
        Calendar cal1 = getCurrentDateCalendar();
        cal1.set(cal.get(Calendar.YEAR) + 2, cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 0, 0, 0);
        return cal1;
    }

    public static Calendar getCalendar(Date date) {
        Calendar calendar = getCurrentDateCalendar();
        calendar.setTime(org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE));
        return calendar;
    }

    public static long getNumberOfDaysBetweenTwoDates(Date date1, Date date2) {
        Calendar cal1 = getCurrentDateCalendar();
        Calendar cal2 = getCurrentDateCalendar();
        cal1.setTime(getDateWithoutTimeStamp(date1));
        cal2.setTime(getDateWithoutTimeStamp(date2));

        return ((cal1.getTime().getTime() - cal2.getTime().getTime()) / (24 * 60 * 60 * 1000));

    }

    public static java.sql.Date getSqlDate(int year, int month, int date) {
        return convertToSqlDate(getCalendarAsOn(year, month, date));
    }

    public static Calendar getCalendarAsOn(int year, int month, int date) {
        Calendar calendar = getCurrentDateCalendar();
        calendar.set(year, month, date, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Date getDate(int year, int month, int date) {
        return getCalendarAsOn(year, month, date).getTime();
    }

    public static java.sql.Date currentDateAsSqlDate() {
        return convertToSqlDate(currentDate());
    }

    private static java.sql.Date convertToSqlDate(Calendar calendar) {
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    public static java.sql.Date convertToSqlDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    public static java.sql.Date sqlToday() {
        Calendar calendar = getCurrentDateCalendar();
        Calendar calendarAsOnToday = getCalendarAsOn(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE));
        return convertToSqlDate(calendarAsOnToday);
    }

    public static DateFormat getLocalizedDateFormat() {
        try {
            return new LocalizationConverter().getDateFormat();
        } catch (RuntimeException e) {
            return DateUtils.DEFAULT_DATE_FORMAT;
        }
    }

    public static Date convertSqlToDate(java.sql.Date meetingDate) {
        return new Date(meetingDate.getTime());
    }

    public static Date getDateFromToday(int days) {
        return addDays(getCurrentDateWithoutTimeStamp(), days);
    }

    /**
     * returns true if the beforeDate falls before afterDate, returns false if
     * both are same dates or afterDate falls before beforeDate
     */
    public static boolean dateFallsBeforeDate(Date theDate, Date referenceDate) {
        return getDateWithoutTimeStamp(theDate.getTime()).compareTo(getDateWithoutTimeStamp(referenceDate.getTime())) < 0;
    }

    /**
     * returns true if the pastDate is equal to or falls before futureDate,
     * returns false if futureDate falls before pastDate
     */
    public static boolean dateFallsOnOrBeforeDate(Date theDate, Date referenceDate) {
        return getDateWithoutTimeStamp(theDate.getTime()).compareTo(getDateWithoutTimeStamp(referenceDate.getTime())) <= 0;
    }

    public static Date addDays(Date date, int daysToAdd) {
        return org.apache.commons.lang.time.DateUtils.addDays(date, daysToAdd);
    }

    public static Date addWeeks(Date date, int weeksToAdd) {
        return org.apache.commons.lang.time.DateUtils.addWeeks(date, weeksToAdd);
    }

    public static Date addMonths(Date date, int monthsToAdd) {
        return org.apache.commons.lang.time.DateUtils.addMonths(date, monthsToAdd);
    }

    public static String format(Date date) {
        return format(date, DEFAULT_DATE_FORMAT.toPattern());
    }

    public static String format(Date date, String pattern) {
        return format(date, new SimpleDateFormat(pattern));
    }

    public static String format(Date date, DateFormat dateFormat) {
        return dateFormat.format(date);
    }

    public static WeekDay getWeekDayForDate(Date date) {
        Calendar calendar = getCurrentDateCalendar();
        calendar.setTime(date);
        return WeekDay.getWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public static Date getDateFromLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

    public static LocalDate getLocalDateFromDate(Date date) {
        if (date == null) {
            return null;
        }
        return new LocalDate(date.getTime());
    }

    public static String convertToDbFormat(Locale locale, String givenDate) throws InvalidDateException {
        SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String userfmt = convertToCurrentDateFormat(format.toPattern());
        return convertUserToDbFmt(givenDate, userfmt);
    }

    // TODO: Add unit tests
    public static boolean firstLessOrEqualSecond(Date firstDate, Date secondDate) {
        Calendar firstCalendarDate = Calendar.getInstance();
        firstCalendarDate.setTime(firstDate);

        Calendar secondCalendarDate = Calendar.getInstance();
        secondCalendarDate.setTime(secondDate);

        firstCalendarDate.set(Calendar.DAY_OF_MONTH, 1);

        return firstCalendarDate.compareTo(secondCalendarDate) <= 0;
    }

    public static Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getShortDateFormat(dateLocale), dateLocale);
        dateFormat.setLenient(false);
        return dateFormat.parse(dateStr);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getShortDateFormat(dateLocale), dateLocale);
        dateFormat.setLenient(false);
        return dateFormat.format(date);
    }
    
    public static boolean isPastDate(Date date) {
        return whichDirection(date) < 0;
    }
}
