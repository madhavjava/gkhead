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

package org.mifos.accounts.loan.struts.action;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.application.meeting.util.helpers.WeekDay.MONDAY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeePayment;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.VariableInstallmentDetailsBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.util.UserContext;

public abstract class AbstractLoanActionTestCase extends MifosMockStrutsTestCase {

    protected UserContext userContext;
    protected CustomerBO center = null;
    protected CustomerBO group = null;
    protected CustomerBO client = null;
    protected List<FeeBO> fees;
    protected MeetingBO meeting;
    protected String flowKey;
    protected LoanOfferingBO loanOffering;
    protected AccountBO accountBO;

    @Before
    public void setup() {
        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, LoanAccountAction.class);
        fees = getFee();
        createInitialObjects();
        loanOffering = getLoanOffering("fdfsdfsd", "ertg", ApplicableTo.GROUPS, WEEKLY, EVERY_WEEK);
    }

    private List<FeeBO> getFee() {
        List<FeeBO> fees = new ArrayList<FeeBO>();
        FeeBO fee1 = TestObjectFactory.createOneTimeAmountFee("One Time Amount Fee", FeeCategory.LOAN, "120.0",
                FeePayment.TIME_OF_DISBURSEMENT);
        FeeBO fee3 = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "10.0",
                RecurrenceType.WEEKLY, (short) 1);
        StaticHibernateUtil.flushSession();
        fees.add(fee1);
        fees.add(fee3);
        return fees;
    }

    private void createInitialObjects() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
    }

    protected LoanOfferingBO getLoanOffering(String name, String shortName, ApplicableTo applicableTo,
            RecurrenceType meetingFrequency, short recurAfter) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeeting(meetingFrequency,
                recurAfter, CUSTOMER_MEETING, MONDAY));
        Date currentDate = new Date(System.currentTimeMillis());
        return TestObjectFactory.createLoanOffering(name, shortName, applicableTo, currentDate, PrdStatus.LOAN_ACTIVE,
                300.0, 1.2, 3, InterestType.FLAT, meeting);
    }

    protected LoanOfferingBO getLoanOffering(String name, String shortName, ApplicableTo applicableTo,
            RecurrenceType meetingFrequency, short recurAfter, VariableInstallmentDetailsBO variableInstallmentDetailsBO) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeeting(meetingFrequency,
                recurAfter, CUSTOMER_MEETING, MONDAY));
        Date currentDate = new Date(System.currentTimeMillis());
        return TestObjectFactory.createLoanOffering(name, shortName, applicableTo, currentDate, PrdStatus.LOAN_ACTIVE,
                300.0, 1.2, 3, InterestType.FLAT, meeting, variableInstallmentDetailsBO);
    }


    protected void tearDown() throws Exception {
        clearRequestParameters();
        TestObjectFactory.removeObject((LoanOfferingBO) TestObjectFactory.getObject(LoanOfferingBO.class, loanOffering
                .getPrdOfferingId()));
        fees = null;
//        for (FeeBO fee : fees) {
//            TestObjectFactory.cleanUp((FeeBO) TestObjectFactory.getObject(FeeBO.class, fee.getFeeId()));
//        }
        try {
//            reloadMembers();
            accountBO = null;
            client = null;
            group = null;
            center = null;
        } catch (Exception e) {

        } finally {
            StaticHibernateUtil.flushSession();
        }
    }

    private void reloadMembers() {
        if (accountBO != null) {
            accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        }
        if (group != null) {
            group = (GroupBO) StaticHibernateUtil.getSessionTL().get(GroupBO.class, group.getCustomerId());
        }
        if (center != null) {
            center = (CenterBO) StaticHibernateUtil.getSessionTL().get(CenterBO.class, center.getCustomerId());
        }
        if (client != null) {
            client = (CustomerBO) StaticHibernateUtil.getSessionTL().get(CustomerBO.class, client.getCustomerId());
        }
    }
}
