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

package org.mifos.accounts.productdefinition.business;

import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.framework.business.AbstractEntity;

public class PrdOfferingMeetingEntity extends AbstractEntity {

    private final Short prdOfferingMeetingId;
    private MeetingBO meeting;
    private PrdOfferingBO prdOffering;
    private final Short meetingType;

    public PrdOfferingMeetingEntity(final MeetingBO meeting, final PrdOfferingBO prdOffering, final MeetingType meetingType) {
        prdOfferingMeetingId = null;
        this.meeting = meeting;
        this.prdOffering = prdOffering;
        this.meetingType = meetingType.getValue();
    }

    protected PrdOfferingMeetingEntity() {
        prdOfferingMeetingId = null;
        prdOffering = null;
        meetingType = null;
    }

    private Short getPrdOfferingMeetingId() {
        return prdOfferingMeetingId;
    }

    public MeetingType getprdOfferingMeetingType() {
        return MeetingType.fromInt(meetingType);
    }

    private PrdOfferingBO getPrdOffering() {
        return prdOffering;
    }

    public MeetingBO getMeeting() {
        return meeting;
    }

    public void setMeeting(final MeetingBO meeting) {
        this.meeting = meeting;
    }

    @Override
    public boolean equals(final Object object) {
        PrdOfferingMeetingEntity prdOfferingMeeting = null;
        boolean value = false;
        if (object != null) {
            prdOfferingMeeting = (PrdOfferingMeetingEntity) object;
            if (prdOfferingMeeting.getPrdOfferingMeetingId().equals(this.prdOfferingMeetingId)) {
                value = true;
            }
        }
        return value;
    }

    public void setPrdOffering(PrdOfferingBO prdOffering) {
        this.prdOffering = prdOffering;
    }
}
