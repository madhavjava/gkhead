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

package org.mifos.application.meeting.business;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifos.application.meeting.MeetingTemplate;
import org.mifos.application.meeting.exceptions.MeetingException;
import org.mifos.application.meeting.util.helpers.MeetingConstants;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RankOfDay;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.config.FiscalCalendarRules;
import org.mifos.config.persistence.ConfigurationPersistence;
import org.mifos.dto.domain.MeetingDetailsDto;
import org.mifos.dto.domain.MeetingDto;
import org.mifos.dto.domain.MeetingTypeDto;
import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.schedule.ScheduledEvent;
import org.mifos.schedule.ScheduledEventFactory;

/**
 * A better name for MeetingBO would be along the lines of "ScheduledEvent". To
 * see what a "meeting" can be look at {@link MeetingType}. It encompasses not
 * only a customer meeting, but also financial events like loan installments,
 * interest posting and the like. This should be refactored, perhaps from a
 * ScheduledEvent base class with subclasses that correspond to the different
 * MeetingType entries. In this way a member like meetingPlace could be
 * associated with the CustomerMeeting rather than all MeetingTypes.
 */
public class MeetingBO extends AbstractBusinessObject {

    private Integer meetingId;
    private MeetingDetailsEntity meetingDetails;
    private MeetingTypeEntity meetingType;
    private Date meetingStartDate;
    private String meetingPlace;

    private FiscalCalendarRules fiscalCalendarRules = null;

    public FiscalCalendarRules getFiscalCalendarRules() {
        if (fiscalCalendarRules == null) {
            fiscalCalendarRules = new FiscalCalendarRules();
        }
        return this.fiscalCalendarRules;
    }

    public void setFiscalCalendarRules(FiscalCalendarRules fiscalCalendarRules) {
        this.fiscalCalendarRules = fiscalCalendarRules;
    }

    /**
     * default constructor for hibernate
     */
    protected MeetingBO() {
    }

    /**
     * minimal legal constructor
     */
    public MeetingBO(final MeetingType meetingType, final Date startDate, final String meetingLocation) {
        this.meetingType = new MeetingTypeEntity(meetingType);
        this.meetingStartDate = startDate;
        this.meetingPlace = meetingLocation;
    }

    public MeetingBO(final RecurrenceType recurrenceType, final Short recurAfter, final Date startDate, final MeetingType meetingType)
            throws MeetingException {
        this(recurrenceType, Short.valueOf("1"), WeekDay.MONDAY, null, recurAfter, startDate, meetingType, "meetingPlace");
    }

    public MeetingBO(final WeekDay weekDay, final RankOfDay rank, final Short recurAfter, final Date startDate, final MeetingType meetingType,
            final String meetingPlace) throws MeetingException {
        this(weekDay, rank, recurAfter, startDate, meetingType, meetingPlace, null);
    }

    public MeetingBO(final WeekDay weekDay, final RankOfDay rank, final Short recurAfter, final Date startDate, final MeetingType meetingType,
            final String meetingPlace, @SuppressWarnings("unused") final Locale locale) throws MeetingException {
        this(RecurrenceType.MONTHLY, null, weekDay, rank, recurAfter, startDate, meetingType, meetingPlace, null);
    }

    public MeetingBO(final Short dayNumber, final Short recurAfter, final Date startDate, final MeetingType meetingType, final String meetingPlace)
            throws MeetingException {
        this(RecurrenceType.MONTHLY, dayNumber, null, null, recurAfter, startDate, meetingType, meetingPlace);
    }

    public MeetingBO(final WeekDay weekDay, final Short recurAfter, final Date startDate, final MeetingType meetingType, final String meetingPlace)
            throws MeetingException {
        this(RecurrenceType.WEEKLY, null, weekDay, null, recurAfter, startDate, meetingType, meetingPlace);
    }

    public MeetingBO(final Short recurAfter, Date startDate, MeetingType meetingType, String meetingPlace)
            throws MeetingException {
        this(RecurrenceType.DAILY, null, null, null, recurAfter, startDate, meetingType, meetingPlace);
    }
    
    public MeetingBO(final MeetingTemplate template) throws MeetingException {
        this(template.getReccurenceType(), template.getDateNumber(), template.getWeekDay(), template.getRankType(),
                template.getRecurAfter(), template.getStartDate(), template.getMeetingType(), template
                        .getMeetingPlace());
    }

    private MeetingBO(final RecurrenceType recurrenceType, final Short dayNumber, final WeekDay weekDay, final RankOfDay rank, final Short recurAfter,
            final Date startDate, final MeetingType meetingType, final String meetingPlace) throws MeetingException {
        this(recurrenceType, dayNumber, weekDay, rank, recurAfter, startDate, meetingType, meetingPlace, null);
    }

    private MeetingBO(final RecurrenceType recurrenceType, final Short dayNumber, final WeekDay weekDay, final RankOfDay rank, final Short recurAfter,
            final Date startDate, final MeetingType meetingType, final String meetingPlace, @SuppressWarnings("unused") final Locale locale) throws MeetingException {
        this.validateFields(recurrenceType, startDate, meetingType, meetingPlace);
        this.meetingDetails = new MeetingDetailsEntity(new RecurrenceTypeEntity(recurrenceType), dayNumber, weekDay,
                rank, recurAfter, this);
        if (meetingType != null) {
            this.meetingType = new MeetingTypeEntity(meetingType);
        }
        this.meetingId = null;
        this.meetingStartDate = DateUtils.getDateWithoutTimeStamp(startDate.getTime());
        this.meetingPlace = meetingPlace;
    }

    public MeetingBO(final Short dayNumber, final Short recurAfter, final Date startDate, final MeetingType meetingType, final String meetingPlace,
            final Short weekNumber) throws MeetingException {

        this(RecurrenceType.MONTHLY, null, WeekDay.getWeekDay(dayNumber), RankOfDay.getRankOfDay(weekNumber), recurAfter,
                startDate, meetingType, meetingPlace);

    }

    public MeetingBO(final int recurrenceId, final Short dayNumber, final Short recurAfter, final Date startDate, final MeetingType meetingType,
            final String meetingPlace) throws MeetingException {

        this(RecurrenceType.WEEKLY, null, WeekDay.getWeekDay(dayNumber), null, recurAfter, startDate, meetingType,
                meetingPlace);

    }

    public MeetingDetailsEntity getMeetingDetails() {
        return meetingDetails;
    }

    public Integer getMeetingId() {
        return meetingId;
    }

    public String getMeetingPlace() {
        return meetingPlace;
    }

    public void setMeetingPlace(final String meetingPlace) {
        this.meetingPlace = meetingPlace;
    }

    public Date getMeetingStartDate() {
        return meetingStartDate;
    }

    public void setMeetingStartDate(final Date meetingStartDate) {
        this.meetingStartDate = DateUtils.getDateWithoutTimeStamp(meetingStartDate);
    }

    public void setStartDate(final Date startDate) {
        this.meetingStartDate = startDate;
    }

    public final Date getStartDate() {
        return meetingStartDate;
    }

    public MeetingTypeEntity getMeetingType() {
        return meetingType;
    }

    public MeetingType getMeetingTypeEnum() {
        return meetingType.asEnum();
    }

    public void setMeetingType(final MeetingTypeEntity meetingType) {
        this.meetingType = meetingType;
    }

    public boolean isMonthlyOnDate() {
        return getMeetingDetails().isMonthlyOnDate();
    }

    public boolean isWeekly() {
        return getMeetingDetails().isWeekly();
    }

    public boolean isMonthly() {
        return getMeetingDetails().isMonthly();
    }
    
    public boolean isDaily() {
        return getMeetingDetails().isDaily();
    }

    public void update(final WeekDay weekDay, final String meetingPlace) throws MeetingException {
        validateMeetingPlace(meetingPlace);
        getMeetingDetails().getMeetingRecurrence().updateWeekDay(weekDay);
        this.meetingPlace = meetingPlace;
    }

    public void update(final WeekDay weekDay, final RankOfDay rank, final String meetingPlace) throws MeetingException {
        validateMeetingPlace(meetingPlace);
        getMeetingDetails().getMeetingRecurrence().update(weekDay, rank);
        this.meetingPlace = meetingPlace;
    }

    public void update(final Short dayNumber, final String meetingPlace) throws MeetingException {
        validateMeetingPlace(meetingPlace);
        getMeetingDetails().getMeetingRecurrence().updateDayNumber(dayNumber);
        this.meetingPlace = meetingPlace;
    }

    public void update(final String meetingPlace) throws MeetingException {
        validateMeetingPlace(meetingPlace);
        this.meetingPlace = meetingPlace;
    }
    
    private void validateFields(final RecurrenceType recurrenceType, final Date startDate, final MeetingType meetingType,
            final String meetingPlace) throws MeetingException {
        if (recurrenceType == null) {
            throw new MeetingException(MeetingConstants.INVALID_RECURRENCETYPE);
        }
        if (startDate == null) {
            throw new MeetingException(MeetingConstants.INVALID_STARTDATE);
        }
        if (meetingType == null) {
            throw new MeetingException(MeetingConstants.INVALID_MEETINGTYPE);
        }
        validateMeetingPlace(meetingPlace);
    }

    private void validateMeetingPlace(final String meetingPlace) throws MeetingException {
        if (StringUtils.isBlank(meetingPlace)) {
            throw new MeetingException(MeetingConstants.INVALID_MEETINGPLACE);
        }
    }

    public boolean isValidMeetingDateUntilNextYear(final Date meetingDate) throws MeetingException {
        return isValidMeetingDate(meetingDate, DateUtils.getLastDayOfNextYear());
    }

    public boolean isValidMeetingDate(final Date meetingDate, final Date endDate) throws MeetingException {
        validateMeetingDate(meetingDate);
        validateEndDate(endDate);
        DateTime currentScheduleDateTime = findNearestMatchingDate(new DateTime(this.meetingStartDate));

        Date currentScheduleDate = currentScheduleDateTime.toDate();
        Calendar c = Calendar.getInstance();
        c.setTime(currentScheduleDate);
        currentScheduleDate = getNextWorkingDay(c).getTime();

        Date meetingDateWOTimeStamp = DateUtils.getDateWithoutTimeStamp(meetingDate.getTime());
        Date endDateWOTimeStamp = DateUtils.getDateWithoutTimeStamp(endDate.getTime());
        if (meetingDateWOTimeStamp.compareTo(endDateWOTimeStamp) > 0) {
            return false;
        }

        while (currentScheduleDate.compareTo(meetingDateWOTimeStamp) < 0
                && currentScheduleDate.compareTo(endDateWOTimeStamp) < 0) {
            currentScheduleDate = findNextMatchingDate(new DateTime(currentScheduleDate)).toDate();
            c.setTime(currentScheduleDate);
            currentScheduleDate = getNextWorkingDay(c).getTime();
        }

        boolean isRepaymentIndepOfMeetingEnabled = new ConfigurationPersistence().isRepaymentIndepOfMeetingEnabled();
        if (isRepaymentIndepOfMeetingEnabled) {
            return currentScheduleDate.compareTo(endDateWOTimeStamp) <= 0;
        }
        // If repayment date is dependend on meeting date, then they need to
        // match
        return currentScheduleDate.compareTo(endDateWOTimeStamp) <= 0
                && currentScheduleDate.compareTo(meetingDateWOTimeStamp) == 0;
    }

    private Calendar getNextWorkingDay(final Calendar day) {
        while (!new FiscalCalendarRules().isWorkingDay(day)) {
            day.add(Calendar.DATE, 1);
        }
        return day;
    }

    public boolean isValidMeetingDate(final Date meetingDate, final int occurrences) throws MeetingException {
        validateMeetingDate(meetingDate);
        validateOccurences(occurrences);

        DateTime currentScheduleDateTime = findNearestMatchingDate(new DateTime(this.meetingStartDate));

        Date currentScheduleDate = currentScheduleDateTime.toDate();
        Date meetingDateWOTimeStamp = DateUtils.getDateWithoutTimeStamp(meetingDate.getTime());

        for (int currentNumber = 1; currentScheduleDate.compareTo(meetingDateWOTimeStamp) < 0
                && currentNumber < occurrences; currentNumber++) {
            currentScheduleDate = findNextMatchingDate(new DateTime(currentScheduleDate)).toDate();
        }

        boolean isRepaymentIndepOfMeetingEnabled = new ConfigurationPersistence().isRepaymentIndepOfMeetingEnabled();
        if (!isRepaymentIndepOfMeetingEnabled) {
            // If repayment date is dependend on meeting date, then they need to
            // match
            return currentScheduleDate.compareTo(meetingDateWOTimeStamp) == 0;
        }

        return true;
    }

    public Date getNextScheduleDateAfterRecurrenceWithoutAdjustment(final Date afterDate) throws MeetingException {
        validateMeetingDate(afterDate);
        DateTime from = findNearestMatchingDate(new DateTime(this.meetingStartDate));
        DateTime currentScheduleDate = findNextMatchingDate(from);
        while (currentScheduleDate.toDate().compareTo(afterDate) <= 0) {
            currentScheduleDate = findNextMatchingDate(currentScheduleDate);
        }
        return currentScheduleDate.toDate();
    }

    private DateTime findNearestMatchingDate(DateTime startingFrom) {
        ScheduledEvent scheduledEvent = ScheduledEventFactory.createScheduledEventFrom(this);
        return scheduledEvent.nearestMatchingDateBeginningAt(startingFrom);
    }

    public Date getPrevScheduleDateAfterRecurrence(final Date meetingDate) throws MeetingException {
        validateMeetingDate(meetingDate);
        DateTime prevScheduleDate = null;
        /*
         * Current schedule date as next meeting date after start date till this
         * date is after given meeting date or increment current schedule date
         * to next meeting date from current schedule date return the last but
         * one current schedule date as prev schedule date
         */
        DateTime currentScheduleDate = findNextMatchingDate(new DateTime(this.meetingStartDate));
        while (currentScheduleDate.toDate().compareTo(meetingDate) < 0) {
            prevScheduleDate = currentScheduleDate;
            currentScheduleDate = findNextMatchingDate(currentScheduleDate);
        }
        if (prevScheduleDate != null) {
            return prevScheduleDate.toDate();
        }
        return null;
    }

    private void validateMeetingDate(final Date meetingDate) throws MeetingException {
        if (meetingDate == null) {
            throw new MeetingException(MeetingConstants.INVALID_MEETINGDATE);
        }
    }

    private void validateOccurences(final int occurrences) throws MeetingException {
        if (occurrences <= 0) {
            throw new MeetingException(MeetingConstants.INVALID_OCCURENCES);
        }
    }

    private void validateEndDate(final Date endDate) throws MeetingException {
        if (endDate == null || endDate.compareTo(getStartDate()) < 0) {
            throw new MeetingException(MeetingConstants.INVALID_ENDDATE);
        }
    }

    private DateTime findNextMatchingDate(DateTime startingFrom) {
        ScheduledEvent scheduledEvent = ScheduledEventFactory.createScheduledEventFrom(this);
        return scheduledEvent.nextEventDateAfter(startingFrom);
    }

    /*
     * This seems like it is trying to answer the question of whether meetings
     * for meetingToBeMatched and meetingToBeMatchedWith overlap. For example a
     * weekly meeting occurring every 2 weeks potentially overlaps with a
     * meeting occurring every 4 weeks.
     */
    public static boolean isMeetingMatched(final MeetingBO meetingToBeMatched, final MeetingBO meetingToBeMatchedWith) {
        return meetingToBeMatched != null
                && meetingToBeMatchedWith != null
                && meetingToBeMatched.getMeetingDetails().getRecurrenceType().getRecurrenceId().equals(
                        meetingToBeMatchedWith.getMeetingDetails().getRecurrenceType().getRecurrenceId())
                && isMultiple(meetingToBeMatchedWith.getMeetingDetails().getRecurAfter(), meetingToBeMatched
                        .getMeetingDetails().getRecurAfter());
    }

    private static boolean isMultiple(final Short valueToBeChecked, final Short valueToBeCheckedWith) {
        return valueToBeChecked % valueToBeCheckedWith == 0;
    }

    public void setMeetingDetails(final MeetingDetailsEntity meetingDetails) {
        this.meetingDetails = meetingDetails;
    }

    public RecurrenceType getRecurrenceType() {
        return meetingDetails.getRecurrenceTypeEnum();
    }

    public Short getRecurAfter() {
        return meetingDetails.getRecurAfter();
    }

    /*
     * Get the start date of the "interval" surrounding a given date
     * For example assume March 1 is a Monday and that weeks are defined to start on
     * Monday.  If this is a weekly meeting on a Wednesday then the "interval"
     * for Wednesday March 10 is the week from Monday March 8 to Sunday March 14,
     * and this method would return March 8.
     */
    public LocalDate startDateForMeetingInterval(LocalDate date) {
        LocalDate startOfMeetingInterval = date;
        if (isWeekly()) {
            int weekDay = WeekDay.getJodaDayOfWeekThatMatchesMifosWeekDay(getFiscalCalendarRules().getStartOfWeekWeekDay().getValue());
            while (startOfMeetingInterval.getDayOfWeek() != weekDay) {
                startOfMeetingInterval = startOfMeetingInterval.minusDays(1);
            }
        } else if (isMonthly()) {
            int dayOfMonth = date.getDayOfMonth();
            startOfMeetingInterval = startOfMeetingInterval.minusDays(dayOfMonth - 1);
        } else {
            // for days we return the same day
            startOfMeetingInterval =  date;
        }
        return startOfMeetingInterval;
    }

    public boolean queryDateIsInMeetingIntervalForFixedDate(LocalDate queryDate, LocalDate fixedDate) {
        LocalDate startOfMeetingInterval = startDateForMeetingInterval(fixedDate);
        LocalDate endOfMeetingInterval;
        if (isWeekly()) {
            endOfMeetingInterval = startOfMeetingInterval.plusWeeks(getRecurAfter());
        } else if (isMonthly()) {
            endOfMeetingInterval = startOfMeetingInterval.plusMonths(getRecurAfter());
        } else {
            // we don't handle meeting intervals in days
            return false;
        }
        return (queryDate.isEqual(startOfMeetingInterval) ||
                queryDate.isAfter(startOfMeetingInterval)) &&
                queryDate.isBefore(endOfMeetingInterval);
    }

    public boolean hasSameRecurrenceAs(MeetingBO customerMeetingValue) {
        return this.getRecurrenceType().equals(customerMeetingValue.getRecurrenceType());
    }
    
    public boolean recursOnMultipleOf(MeetingBO meeting) {
        return meeting.getMeetingDetails().getRecurAfter().intValue() % this.meetingDetails.getRecurAfter().intValue() == 0;
    }

    public boolean isDayOfWeekDifferent(WeekDay dayOfWeek) {
        return !dayOfWeek.equals(this.getMeetingDetails().getWeekDay());
    }

    public boolean isDayOfMonthDifferent(Short dayOfMonth) {
        return !dayOfMonth.equals(this.getMeetingDetails().getDayNumber());
    }

    public boolean isWeekOfMonthDifferent(RankOfDay weekOfMonth, WeekDay dayOfWeekInWeekOfMonth) {

        boolean isDifferent = false;
        if (!weekOfMonth.equals(this.getMeetingDetails().getWeekRank())) {
            isDifferent = true;
        }

        if (!dayOfWeekInWeekOfMonth.equals(this.getMeetingDetails().getWeekDay())) {
            isDifferent = true;
        }

        return isDifferent;
    }

    public MeetingDto toDto() {
        MeetingTypeDto meetingType = this.meetingType.toDto();
        MeetingDetailsDto meetingDetailsDto = this.meetingDetails.toDto();
        return new MeetingDto(new LocalDate(this.meetingStartDate), this.meetingPlace, meetingType, meetingDetailsDto);
    }
}