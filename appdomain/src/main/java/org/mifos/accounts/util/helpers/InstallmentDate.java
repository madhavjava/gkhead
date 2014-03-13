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

package org.mifos.accounts.util.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.mifos.framework.util.helpers.DateUtils;

public class InstallmentDate {

    private Date installmentDueDate = null;
    private Short installmentId;

    public static List<InstallmentDate> createInstallmentDates(final List<DateTime> dueDates) {
        List<InstallmentDate> installmentDates = new ArrayList<InstallmentDate>();
        int installmentId = 1;
        for (DateTime date : dueDates) {
            installmentDates.add(new InstallmentDate((short) installmentId++, date.toDate()));
        }
        return installmentDates;
    }

    /**
     * Find the installmentId of the first installmentDate in the list that is on or after the given feeDate. If
     * there are none, return null.
     * @param installmentDates the list of {@link InstallmentDate} to search, in order.
     * @param feeDate the Date to search for
     * @return the installmentId of the first entry in the list whose date is on or after feeDate, or null if none.
     */
    public static Short findMatchingInstallmentId(List<InstallmentDate> installmentDates, Date feeDate) {
        for (InstallmentDate installmentDate : installmentDates) {
            if (DateUtils.getDateWithoutTimeStamp(installmentDate.getInstallmentDueDate().getTime()).compareTo(
                    DateUtils.getDateWithoutTimeStamp(feeDate.getTime())) >= 0) {
                return installmentDate.getInstallmentId();
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private InstallmentDate() {
    }

    public InstallmentDate(Short installmentId, Date installmentDueDate) {
        this.installmentId = installmentId;
        this.installmentDueDate = installmentDueDate;
    }

    public void setInstallmentId(Short installmentId) {
        this.installmentId = installmentId;
    }

    public Short getInstallmentId() {
        return installmentId;
    }

    public Date getInstallmentDueDate() {
        return installmentDueDate;
    }

}
