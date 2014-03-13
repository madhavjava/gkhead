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
package org.mifos.domain.builders;

import org.joda.time.DateTime;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.TestUtils;
import org.mifos.framework.business.util.Address;
import org.mifos.security.util.UserContext;

/**
 *
 */
public class GroupBuilder {

    private GroupBO group;
    private final CustomerAccountBuilder customerAccountBuilder = new CustomerAccountBuilder();
    private String name = "Test Group";
    private MeetingBO meeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
    private OfficeBO office = new OfficeBuilder().withGlobalOfficeNum("xxx-9999").withOfficeId(new Short("1")).build();
    private PersonnelBO loanOfficer;
    private CustomerStatus customerStatus = CustomerStatus.GROUP_ACTIVE;
    private CustomerBO parentCustomer = new CenterBuilder().build();
    private Address address;
    private String externalId;
    private boolean trained = false;
    private DateTime trainedOn = new DateTime();
    private PersonnelBO formedBy;
    private int numberOfChildrenUnderBranch = 0;
    private UserContext userContext = TestUtils.makeUser();
    private Integer versionNumber;
    private DateTime activationDate = new DateTime().toDateMidnight().toDateTime();


    public GroupBO build() {

        if (formedBy == null) {
            this.formedBy = this.loanOfficer;
        }

        DateTime mfiJoiningDate = new DateTime().toDateMidnight().toDateTime();
        group = GroupBO.createGroupWithCenterAsParent(userContext, name, formedBy, parentCustomer,
                address, externalId, trained, trainedOn, customerStatus, mfiJoiningDate, activationDate);

        if (this.versionNumber != null) {
            group.setVersionNo(versionNumber);
        }

        return group;
    }

    public GroupBO buildAsTopOfHierarchy() {

        if (formedBy == null) {
            this.formedBy = this.loanOfficer;
        }

        UserContext userContext = TestUtils.makeUser();
        DateTime mfiJoiningDate = new DateTime().toDateMidnight().toDateTime();
        DateTime activationDate = new DateTime().toDateMidnight().toDateTime();
        group = GroupBO.createGroupAsTopOfCustomerHierarchy(userContext, name, formedBy, meeting, loanOfficer, office,
                address, externalId, trained, trainedOn, customerStatus, numberOfChildrenUnderBranch, mfiJoiningDate, activationDate);
        for(int child=0; child < numberOfChildrenUnderBranch; ++child) {
            group.incrementChildCount();
        }
        return group;
    }

    public GroupBuilder withName(final String withName) {
        this.name = withName;
        return this;
    }

    public GroupBuilder withMeeting(final MeetingBO withMeeting) {
        this.meeting = withMeeting;
        return this;
    }

    public GroupBuilder withOffice(final OfficeBO withOffice) {
        this.office = withOffice;
        return this;
    }

    public GroupBuilder withLoanOfficer(final PersonnelBO withLoanOfficer) {
        this.loanOfficer = withLoanOfficer;
        return this;
    }

    public GroupBuilder withFee(final AmountFeeBO withFee) {
        customerAccountBuilder.withFee(withFee);
        return this;
    }

    public GroupBuilder withParentCustomer(final CustomerBO withParentCustomer) {
        this.parentCustomer = withParentCustomer;
        return this;
    }

    public GroupBuilder withStatus(CustomerStatus groupStatus) {
        this.customerStatus = groupStatus;
        return this;
    }

    public GroupBuilder isTrained() {
        this.trained = true;
        return this;
    }

    public GroupBuilder isNotTrained() {
        this.trained = false;
        return this;
    }

    public GroupBuilder trainedOn(DateTime withTrainedDate) {
        this.trainedOn = withTrainedDate;
        return this;
    }

    public GroupBuilder formedBy(PersonnelBO withFormedBy) {
        this.formedBy = withFormedBy;
        return this;
    }

    public GroupBuilder withAddress(Address withAddress) {
        this.address = withAddress;
        return this;
    }

    public GroupBuilder withSearchId(int withChildrenUnderBranch) {
        this.numberOfChildrenUnderBranch = withChildrenUnderBranch;
        return this;
    }

    public GroupBuilder inSameBranchAs(CenterBO center) {
        this.office = center.getOffice();
        return this;
    }

    public GroupBuilder with(UserContext withUserContext) {
        this.userContext = withUserContext;
        return this;
    }

    public GroupBuilder active() {
        this.customerStatus = CustomerStatus.GROUP_ACTIVE;
        return this;
    }

    /**
     * do not update this when building centers for integration tests
     */
    public GroupBuilder withVersion(int withVersionNumber) {
        this.versionNumber = withVersionNumber;
        return this;
    }

    public GroupBuilder pendingApproval() {
        this.customerStatus = CustomerStatus.GROUP_PENDING;
        return this;
    }

    public GroupBuilder withActivationDate(DateTime withActivationDate) {
        this.activationDate = withActivationDate;
        return this;
    }
}