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

package org.mifos.accounts.offsetting;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class OffsetAccountBOIntegrationTest extends MifosIntegrationTestCase {

    protected LoanBO accountBO = null;
    protected CustomerBO center = null;
    protected CustomerBO group = null;

    @Before
    public void setUp() throws Exception {

        accountBO = createLoanAccount();

        /**
         * if (super.accountBO == null) { //Calling super setup on the
         * TestAccount //with a currently existing TestAccount causes //a
         * "duplicate AccountBO object insertion error". //First cleaning up if
         * a TestAccount exists then get //get a new TestAccount. super.setUp();
         * }
         */
    }

    @Test
    public void testLoanAccountBOOfsetAllowablePersistence() {
       Assert.assertTrue(accountBO.getOffsettingAllowable().intValue() == 1);
    }

    @After
    public void tearDown() throws Exception {
        try {
            accountBO = null;
            group = null;
            center = null;
        } catch (Exception e) {
            // throwing here tends to mask failures
            e.printStackTrace();
        }
    }

    private LoanBO createLoanAccount() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createWeeklyFeeCenter("OffsetCenter", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("OffsetGroup1", CustomerStatus.GROUP_ACTIVE, center);

        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("OffsetLoan1", ApplicableTo.GROUPS,
                new Date(System.currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2, (short) 3, InterestType.FLAT,
                meeting);

        return TestObjectFactory.createLoanAccount("42423142342", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                new Date(System.currentTimeMillis()), loanOffering);
    }
}
