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

package org.mifos.customers.business;

import java.util.Date;

import org.mifos.application.util.helpers.Status;
import org.mifos.framework.business.AbstractEntity;
import org.mifos.framework.util.DateTimeService;

/**
 * This class encapsulate the customer hierarchy
 */
public class CustomerHierarchyEntity extends AbstractEntity {

    @SuppressWarnings("unused")
    private final Integer hierarchyId;
    private final CustomerBO parentCustomer;
    private final CustomerBO customer;
    private Short status;
    private Date endDate;
    private Date createdDate;
    private Date updatedDate;
    private Short updatedBy;

    public CustomerHierarchyEntity(CustomerBO customer, CustomerBO parentCustomer) {
        this.customer = customer;
        this.parentCustomer = parentCustomer;
        this.status = Status.ACTIVE.getValue();
        this.hierarchyId = null;
        this.createdDate = new DateTimeService().getCurrentJavaDateTime();
    }

    /*
     * Adding a default constructor is hibernate's requirement and should not be
     * used to create a valid Object.
     */
    protected CustomerHierarchyEntity() {
        this.hierarchyId = null;
        this.parentCustomer = null;
        this.customer = null;
    }

    public CustomerBO getCustomer() {
        return customer;
    }

    public CustomerBO getParentCustomer() {
        return parentCustomer;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    void updateStatus(Status status) {
        this.status = status.getValue();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Short getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Short updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isActive() {
        return status.equals(Status.ACTIVE.getValue());
    }

    public void makeInactive(Short updatedBy) {
        updateStatus(Status.INACTIVE);
        setUpdatedBy(updatedBy);
        setUpdatedDate(new DateTimeService().getCurrentJavaDateTime());
        setEndDate(new DateTimeService().getCurrentJavaDateTime());
    }
}