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

package org.mifos.application.meeting.struts.action;

import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.exceptions.MeetingException;
import org.mifos.application.meeting.struts.actionforms.MeetingActionForm;
import org.mifos.application.meeting.util.helpers.MeetingConstants;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RankOfDay;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.MifosUser;
import org.mifos.security.util.UserContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class MeetingActionStrutsTest extends MifosMockStrutsTestCase {

    private UserContext userContext;

    private String flowKey;
    private CenterBO center;
    private GroupBO group;
    private ClientBO client1;
    private ClientBO client2;

    @Before
    public void setUp() throws Exception {
        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");

        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, MeetingAction.class);
    }

    @After
    public void tearDown() throws Exception {
        client1 = null;
        client2 = null;
        group = null;
        center = null;
    }

    @Test
    public void testLoadForCenter() throws Exception {
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("input", "create");
        actionPerform();
        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();
        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");
        Assert.assertEquals(CustomerLevel.CENTER, actionForm.getCustomerLevelValue());
    }

    @Test
    public void testLoadForGroup() throws Exception {
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.GROUP.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("input", "create");
        actionPerform();

        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();
        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");
        Assert.assertEquals(CustomerLevel.GROUP, actionForm.getCustomerLevelValue());
    }

    @Test
    public void testLoadForClient() throws Exception {
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CLIENT.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("input", "create");
        actionPerform();

        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();
        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");
        Assert.assertEquals(CustomerLevel.CLIENT, actionForm.getCustomerLevelValue());
    }

    @Test
    public void testLoad_WeeklyMeetingExists() throws Exception {
        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("officeId", "3");
        actionPerform();
        Short recurAfter = Short.valueOf("1");
        MeetingBO meeting = createWeeklyMeeting(WeekDay.MONDAY, recurAfter, new Date());
        SessionUtils.setAttribute(CustomerConstants.CUSTOMER_MEETING, meeting, request);

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();

        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");

        Assert.assertEquals(CustomerLevel.CENTER, actionForm.getCustomerLevelValue());
        Assert.assertEquals(WeekDay.MONDAY, actionForm.getWeekDayValue());
        Assert.assertEquals(recurAfter, actionForm.getRecurWeekValue());
        Assert.assertEquals(RecurrenceType.WEEKLY, actionForm.getRecurrenceType());
    }

    @Test
    public void testLoad_MonthlyOnDateMeetingExists() throws Exception {
        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("officeId", "3");
        actionPerform();
        Short recurAfter = Short.valueOf("2");
        Short dayNumber = Short.valueOf("5");
        MeetingBO meeting = createMonthlyMeetingOnDate(dayNumber, recurAfter, new Date());
        SessionUtils.setAttribute(CustomerConstants.CUSTOMER_MEETING, meeting, request);

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();

        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");

        Assert.assertEquals(CustomerLevel.CENTER, actionForm.getCustomerLevelValue());
        Assert.assertEquals("1", actionForm.getMonthType());
        Assert.assertEquals(dayNumber, actionForm.getMonthDayValue());
        Assert.assertEquals(recurAfter, actionForm.getDayRecurMonthValue());
        Assert.assertEquals(RecurrenceType.MONTHLY, actionForm.getRecurrenceType());
    }

    @Test
    public void testLoad_MonthlyOnWeekMeetingExists() throws Exception {
        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("officeId", "3");
        actionPerform();
        Short recurAfter = Short.valueOf("2");

        MeetingBO meeting = createMonthlyMeetingOnWeekDay(WeekDay.FRIDAY, RankOfDay.SECOND, recurAfter, new Date());
        SessionUtils.setAttribute(CustomerConstants.CUSTOMER_MEETING, meeting, request);

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyForward(ActionForwards.load_success.toString());
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();

        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");

        Assert.assertEquals(CustomerLevel.CENTER, actionForm.getCustomerLevelValue());
        Assert.assertEquals("2", actionForm.getMonthType());
        Assert.assertEquals(WeekDay.FRIDAY, actionForm.getMonthWeekValue());
        Assert.assertEquals(RankOfDay.SECOND, actionForm.getMonthRankValue());
        Assert.assertEquals(recurAfter, actionForm.getRecurMonthValue());
        Assert.assertEquals(RecurrenceType.MONTHLY, actionForm.getRecurrenceType());
    }

    @Test
    public void testFailureCreateMeeting_RecurrenceIsNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", "");
        addRequestParameter("meetingPlace", "Delhi");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("RecurrenceType", 1, getErrorSize(MeetingConstants.INVALID_RECURRENCETYPE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateWeeklyMeeting_WeekDayAndRecurAfterIsNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.WEEKLY.getValue().toString());
        addRequestParameter("weekDay", "");
        addRequestParameter("recurWeek", "");
        addRequestParameter("meetingPlace", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");

        actionPerform();
        Assert.assertEquals("Week Recurrence", 1, getErrorSize(MeetingConstants.ERRORS_SPECIFY_WEEKDAY_AND_RECURAFTER));
        Assert.assertEquals("Meeting Place", 1, getErrorSize(MeetingConstants.INVALID_MEETINGPLACE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateWeeklyMeeting_WeekDayIsNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.WEEKLY.getValue().toString());
        addRequestParameter("weekDay", "");
        addRequestParameter("recurWeek", "2");
        addRequestParameter("meetingPlace", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Week Recurrence", 1, getErrorSize(MeetingConstants.ERRORS_SPECIFY_WEEKDAY_AND_RECURAFTER));
        Assert.assertEquals("Meeting Place", 1, getErrorSize(MeetingConstants.INVALID_MEETINGPLACE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateWeeklyMeeting_MeetingPlaceIsNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.WEEKLY.getValue().toString());
        addRequestParameter("weekDay", WeekDay.MONDAY.getValue().toString());
        addRequestParameter("recurWeek", "2");
        addRequestParameter("meetingPlace", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Meeting Place", 1, getErrorSize(MeetingConstants.INVALID_MEETINGPLACE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testSuccessfulCreateWeeklyMeeting() throws Exception {
        loadMeetingPage();
        String meetingPlace = "Delhi";
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.WEEKLY.getValue().toString());
        addRequestParameter("weekDay", WeekDay.MONDAY.getValue().toString());
        addRequestParameter("recurWeek", "2");
        addRequestParameter("meetingPlace", meetingPlace);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.loadCreateCenter.toString());
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertTrue(meeting.isWeekly());
        Assert.assertEquals(meetingPlace, meeting.getMeetingPlace());
        Assert.assertEquals(Short.valueOf("2"), meeting.getMeetingDetails().getRecurAfter());
        Assert.assertEquals(WeekDay.MONDAY, meeting.getMeetingDetails().getWeekDay());
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnDate_DayNumAndRecurAfterIsNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_DATE);
        addRequestParameter("monthDay", "");
        addRequestParameter("dayRecurMonth", "");
        addRequestParameter("meetingPlace", "Delhi");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Month Recurrence On Date", 1,
                getErrorSize(MeetingConstants.ERRORS_SPECIFY_DAYNUM_AND_RECURAFTER));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnDate_RecurAfterIsNull() throws Exception {
        loadMeetingPage();
        Short dayNumber = Short.valueOf("1");
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_DATE);
        addRequestParameter("monthDay", dayNumber.toString());
        addRequestParameter("dayRecurMonth", "");
        addRequestParameter("meetingPlace", "Delhi");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Month Recurrence On Date", 1,
                getErrorSize(MeetingConstants.ERRORS_SPECIFY_DAYNUM_AND_RECURAFTER));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnDate_MeetingPlaceIsNull() throws Exception {
        loadMeetingPage();
        Short dayNumber = Short.valueOf("5");
        Short recurAfter = Short.valueOf("1");
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_DATE);
        addRequestParameter("monthDay", dayNumber.toString());
        addRequestParameter("dayRecurMonth", recurAfter.toString());
        addRequestParameter("meetingPlace", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        Assert.assertEquals("Month Recurrence On Date", 1, getErrorSize(MeetingConstants.INVALID_MEETINGPLACE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testSuccessfulCreateMonthlyMeetingOnDate() throws Exception {
        loadMeetingPage();
        String meetingPlace = "Delhi";
        Short dayNumber = Short.valueOf("5");
        Short recurAfter = Short.valueOf("1");
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_DATE);
        addRequestParameter("monthDay", dayNumber.toString());
        addRequestParameter("dayRecurMonth", recurAfter.toString());
        addRequestParameter("meetingPlace", meetingPlace);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.loadCreateCenter.toString());
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertTrue(meeting.isMonthly());
        Assert.assertTrue(meeting.isMonthlyOnDate());
        Assert.assertEquals(meetingPlace, meeting.getMeetingPlace());
        Assert.assertEquals(recurAfter, meeting.getMeetingDetails().getRecurAfter());
        Assert.assertEquals(dayNumber, meeting.getMeetingDetails().getDayNumber());
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnWeekDay_AllNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_WEEK_DAY);
        addRequestParameter("monthWeek", "");
        addRequestParameter("monthRank", "");
        addRequestParameter("recurMonth", "");
        addRequestParameter("meetingPlace", "Delhi");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Month Recurrence On WeekDay", 1,
                getErrorSize(MeetingConstants.ERRORS_SPECIFY_MONTHLY_MEETING_ON_WEEKDAY));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnWeekDay_RankANdRecurAfterNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_WEEK_DAY);
        addRequestParameter("monthWeek", WeekDay.MONDAY.getValue().toString());
        addRequestParameter("monthRank", "");
        addRequestParameter("recurMonth", "");
        addRequestParameter("meetingPlace", "Delhi");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Month Recurrence On WeekDay", 1,
                getErrorSize(MeetingConstants.ERRORS_SPECIFY_MONTHLY_MEETING_ON_WEEKDAY));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testFailureCreateMonthlyMeetingOnWeekDay_RecurAfterNull() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_WEEK_DAY);
        addRequestParameter("monthWeek", WeekDay.MONDAY.getValue().toString());
        addRequestParameter("monthRank", RankOfDay.FOURTH.getValue().toString());
        addRequestParameter("recurMonth", "");
        addRequestParameter("meetingPlace", "");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        Assert.assertEquals("Month Recurrence On WeekDay", 1,
                getErrorSize(MeetingConstants.ERRORS_SPECIFY_MONTHLY_MEETING_ON_WEEKDAY));
        Assert.assertEquals("Month Recurrence On Date", 1, getErrorSize(MeetingConstants.INVALID_MEETINGPLACE));
        verifyInputForward();
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testSuccessfulCreateMonthlyMeetingOnWeekDay() throws Exception {
        loadMeetingPage();
        String meetingPlace = "Delhi";
        Short recurAfter = Short.valueOf("1");
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "create");
        addRequestParameter("frequency", RecurrenceType.MONTHLY.getValue().toString());
        addRequestParameter("monthType", MeetingConstants.MONTHLY_ON_WEEK_DAY);
        addRequestParameter("monthWeek", WeekDay.MONDAY.getValue().toString());
        addRequestParameter("monthRank", RankOfDay.FOURTH.getValue().toString());
        addRequestParameter("recurMonth", recurAfter.toString());
        addRequestParameter("meetingPlace", meetingPlace);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("input", "create");
        actionPerform();

        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward(ActionForwards.loadCreateCenter.toString());
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertTrue(meeting.isMonthly());
        Assert.assertFalse(meeting.isMonthlyOnDate());
        Assert.assertEquals(meetingPlace, meeting.getMeetingPlace());
        Assert.assertEquals(recurAfter, meeting.getMeetingDetails().getRecurAfter());
        Assert.assertEquals(WeekDay.MONDAY, meeting.getMeetingDetails().getWeekDay());
        Assert.assertEquals(RankOfDay.FOURTH, meeting.getMeetingDetails().getWeekRank());
    }

    @Test
    public void testSuccessfulCancelCreate() throws Exception {
        loadMeetingPage();
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "cancelCreate");
        actionPerform();
        verifyForward(ActionForwards.loadCreateCenter.toString());
        MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNull(meeting);
    }

    @Test
    public void testSuccessfulCancelCreate_WithMeetingInSession() throws Exception {
        loadMeetingPage();
        MeetingBO meeting = createWeeklyMeeting(WeekDay.MONDAY, Short.valueOf("5"), new Date());
        SessionUtils.setAttribute(CustomerConstants.CUSTOMER_MEETING, meeting, request);
        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "cancelCreate");
        actionPerform();
        verifyForward(ActionForwards.loadCreateCenter.toString());
        meeting = (MeetingBO) SessionUtils.getAttribute(CustomerConstants.CUSTOMER_MEETING, request);
        Assert.assertNotNull(meeting);
    }

    @Test
    public void testEditForCenter() throws Exception {
        setMifosUserFromContext();

        MeetingBO meeting = createWeeklyMeeting(WeekDay.WEDNESDAY, Short.valueOf("5"), new Date());
        center = createCenter(meeting);

        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "get");
        addRequestParameter("globalCustNum", center.getGlobalCustNum());
        actionPerform();

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "edit");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward(ActionForwards.edit_success.toString());

        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKDAYSLIST, request));
        Assert.assertNotNull(SessionUtils.getAttribute(MeetingConstants.WEEKRANKLIST, request));
        verifyNoActionErrors();
        verifyNoActionMessages();
        MeetingActionForm actionForm = (MeetingActionForm) request.getSession().getAttribute("meetingActionForm");
        Assert.assertEquals(CustomerLevel.CENTER, actionForm.getCustomerLevelValue());
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    @Test
    public void testSuccessfulEditCancel() throws Exception {
        setMifosUserFromContext();

        MeetingBO meeting = createWeeklyMeeting(WeekDay.WEDNESDAY, Short.valueOf("5"), new Date());
        center = createCenter(meeting);
        StaticHibernateUtil.flushSession();
        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "get");
        addRequestParameter("globalCustNum", center.getGlobalCustNum());
        actionPerform();

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "edit");
        addRequestParameter("meetingId", center.getCustomerMeeting().getMeeting().getMeetingId().toString());
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "cancelUpdate");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward(ActionForwards.center_detail_page.toString());
        StaticHibernateUtil.flushSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        Assert.assertNotNull(center.getCustomerMeeting().getMeeting());
    }

    private CenterBO createCenter(MeetingBO meeting) throws Exception {
        return TestObjectFactory.createWeeklyFeeCenter("Center1", meeting, Short.valueOf("3"), Short.valueOf("1"));
    }

    private void loadMeetingPage() {
        setRequestPathInfo("/centerCustAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("officeId", "3");
        actionPerform();

        setRequestPathInfo("/meetingAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("customerLevel", CustomerLevel.CENTER.getValue().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
    }

    private MeetingBO createWeeklyMeeting(WeekDay weekDay, Short recurAfer, Date startDate) throws MeetingException {
        return new MeetingBO(weekDay, recurAfer, startDate, MeetingType.CUSTOMER_MEETING, "MeetingPlace");
    }

    private MeetingBO createMonthlyMeetingOnDate(Short dayNumber, Short recurAfer, Date startDate)
            throws MeetingException {
        return new MeetingBO(dayNumber, recurAfer, startDate, MeetingType.CUSTOMER_MEETING, "MeetingPlace");
    }

    private MeetingBO createMonthlyMeetingOnWeekDay(WeekDay weekDay, RankOfDay rank, Short recurAfer, Date startDate)
            throws MeetingException {
        return new MeetingBO(weekDay, rank, recurAfer, startDate, MeetingType.CUSTOMER_MEETING, "MeetingPlace");
    }

    private void setMifosUserFromContext() {
        SecurityContext securityContext = new SecurityContextImpl();
        MifosUser principal = new MifosUser(userContext.getId(), userContext.getBranchId(), userContext.getLevelId(),
                new ArrayList<Short>(userContext.getRoles()), userContext.getName(), "".getBytes(),
                true, true, true, true, new ArrayList<GrantedAuthority>(), userContext.getLocaleId());
        Authentication authentication = new TestingAuthenticationToken(principal, principal);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
