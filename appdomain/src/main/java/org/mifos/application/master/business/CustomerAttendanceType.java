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

package org.mifos.application.master.business;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.mifos.customers.client.business.AttendanceType;
import org.mifos.framework.business.AbstractEntity;

/**
 * @deprecated Use {@link AttendanceType} instead.
 */
@Deprecated
@Entity
@Table(name = "customer_attendance_types")
public class CustomerAttendanceType extends AbstractEntity {

    @Id
    @GeneratedValue
    private short attendanceId;

    @Column(name = "attendance_lookup_id")
    private Integer lookUpId;

    private String desciption;

    public CustomerAttendanceType() {
        super();
    }

    public Short getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(final Short attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getDesciption() {
        return desciption;
    }

    public void setDesciption(final String desciption) {
        this.desciption = desciption;
    }

    public Integer getLookUpId() {
        return lookUpId;
    }

    public void setLookUpId(final Integer lookUpId) {
        this.lookUpId = lookUpId;
    }

}
