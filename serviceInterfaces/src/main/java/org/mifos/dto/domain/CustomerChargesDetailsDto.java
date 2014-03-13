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

package org.mifos.dto.domain;

import org.mifos.dto.screen.AccountFeesDto;
import org.mifos.dto.screen.CustomerRecentActivityDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification="should disable at filter level and also for pmd - not important for us")
public class CustomerChargesDetailsDto implements Serializable {

    private final String nextDueAmount;
    private final String totalAmountInArrears;
    private final String totalAmountDue;
    private final Date upcomingChargesDate;
    private final CustomerScheduleDto upcomingInstallment;
    private final List<CustomerRecentActivityDto> recentActivities = new ArrayList<CustomerRecentActivityDto>();
    private final List<AccountFeesDto> accountFees;

    public CustomerChargesDetailsDto(String nextDueAmount, String totalAmountInArrears, String totalAmountDue, Date upcomingChargesDate, CustomerScheduleDto upcomingInstallment, List<AccountFeesDto> accountFees) {
        this.nextDueAmount = nextDueAmount;
        this.totalAmountInArrears = totalAmountInArrears;
        this.totalAmountDue = totalAmountDue;
        this.upcomingChargesDate = upcomingChargesDate;
        this.upcomingInstallment = upcomingInstallment;
        this.accountFees = accountFees;
    }

    public void addActivities (List<CustomerRecentActivityDto> activities) {
        recentActivities.addAll(activities);
    }

    public String getNextDueAmount() {
        return nextDueAmount;
    }

    public String getTotalAmountInArrears() {
        return totalAmountInArrears;
    }

    public String getTotalAmountDue() {
        return totalAmountDue;
    }

    public Date getUpcomingChargesDate() {
        return upcomingChargesDate;
    }

    public CustomerScheduleDto getUpcomingInstallment() {
        return upcomingInstallment;
    }

    public List<CustomerRecentActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public List<AccountFeesDto> getAccountFees() {
        return accountFees;
    }
}