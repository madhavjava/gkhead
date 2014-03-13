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

package org.mifos.application.holiday.business.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mifos.dto.domain.HolidayDetails;

public interface HolidayService {

    void create(HolidayDetails holidayDetails, List<Short> officeIds);

    boolean isWorkingDay(Calendar day, Short officeId);

    Calendar getNextWorkingDay(Calendar day, Short officeId);

    Date getNextWorkingDay(Date day, Short officeId);

    boolean isFutureRepaymentHoliday(Calendar date, Short officeId);
}
