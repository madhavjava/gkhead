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
import org.mifos.application.holiday.business.Holiday;
import org.mifos.application.holiday.business.HolidayBO;
import org.mifos.application.holiday.util.helpers.RepaymentRuleTypes;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.dto.domain.HolidayDetails;

public class HolidayBuilder {

    private DateTime from;
    private DateTime to;
    private RepaymentRuleTypes repaymentRule = RepaymentRuleTypes.NEXT_WORKING_DAY;
    private String name = "builderCreatedHoliday";
    private OfficeBO office;

    public Holiday build() {

        HolidayBO holiday = new HolidayBO(new HolidayDetails(this.name, from.toDate(), to.toDate(), repaymentRule.getValue()));

        if (office != null) {
            office.addHoliday(holiday);
        }

        return holiday;
    }

    public HolidayDetails buildDto() {
        return ((HolidayBO) build()).toDto();
    }

    public HolidayBuilder from(final DateTime withFrom) {
        this.from = withFrom;
        return this;
    }

    public HolidayBuilder to(final DateTime withTo) {
        this.to = withTo;
        return this;
    }

    public HolidayBuilder withRepaymentRule(RepaymentRuleTypes rule) {
        this.repaymentRule = rule;
        return this;
    }

    public HolidayBuilder withNextMeetingRule() {
        repaymentRule = RepaymentRuleTypes.NEXT_MEETING_OR_REPAYMENT;
        return this;
    }

    public HolidayBuilder withNextWorkingDayRule() {
        repaymentRule = RepaymentRuleTypes.NEXT_WORKING_DAY;
        return this;
    }

    public HolidayBuilder withSameDayAsRule() {
        repaymentRule = RepaymentRuleTypes.SAME_DAY;
        return this;
    }

    public HolidayBuilder withRepaymentMoratoriumRule() {
        repaymentRule = RepaymentRuleTypes.REPAYMENT_MORATORIUM;
        return this;
    }

    public HolidayBuilder withName(String holidayName) {
        this.name = holidayName;
        return this;
    }

    public HolidayBuilder appliesTo(OfficeBO withOffice) {
        this.office = withOffice;
        return this;
    }
}
