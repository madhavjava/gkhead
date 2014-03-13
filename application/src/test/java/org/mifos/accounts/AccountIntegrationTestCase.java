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

package org.mifos.accounts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.mifos.accounts.fees.business.FeeDto;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.InterestType;
import org.mifos.accounts.productdefinition.util.helpers.PrdStatus;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.application.holiday.business.Holiday;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.domain.builders.MeetingBuilder;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AccountIntegrationTestCase extends MifosIntegrationTestCase {

    protected SavingsBO savingsBO;
    protected LoanBO groupLoan;
    protected LoanBO clientLoan;
    protected CenterBO center;
    protected GroupBO group;
    protected ClientBO client;
    protected MeetingBO meeting;
    protected Holiday holiday;

    @Autowired
    protected LegacyAccountDao legacyAccountDao;


    @Before
    public void setUp() throws Exception {
        enableCustomWorkingDays();
        createInitialCustomerAccounts();
        groupLoan = createGroupLoanAccount();
        clientLoan = createClientLoanAccount();
    }

    @After
    public void tearDown() throws Exception {
        try {
            this.getBranchOffice().setHolidays(null);

            holiday = null;
            groupLoan = null;
            clientLoan = null;
            savingsBO = null;
            client = null;
            group = null;
            center = null;
            legacyAccountDao = null;
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db

        } finally {
            new DateTimeService().resetToCurrentSystemDateTime();
            StaticHibernateUtil.flushSession();
        }

    }

    private LoanBO createGroupLoanAccount() {
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("GroupLoan", ApplicableTo.GROUPS, new Date(System
                .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2, (short) 3, InterestType.FLAT, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                new Date(System.currentTimeMillis()), loanOffering);
    }

    private LoanBO createClientLoanAccount() {
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("ClientLoan", ApplicableTo.CLIENTS, new Date(System
                .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2, (short) 3, InterestType.FLAT, meeting);
        return TestObjectFactory.createLoanAccount("42423142344", client, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                new Date(System.currentTimeMillis()), loanOffering);
    }

    private void createInitialCustomerAccounts() {

        meeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
        IntegrationTestObjectMother.saveMeeting(meeting);

        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        List<FeeDto> fees = new ArrayList<FeeDto>();
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group, fees, "1034556", new DateTime(
                1986, 04, 02, 0, 0, 0, 0).toDate());
    }
}
