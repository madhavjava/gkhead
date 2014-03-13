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

package org.mifos.accounts.loan.util.helpers;

import org.mifos.dto.domain.LoanAccountDetailsDto;

public class LoanAccountActionFormTestConstants {

    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_NULL = LoanAccountDetailsDto.createInstanceForTest("1", null, null, null);

    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_ZERO = new LoanAccountDetailsDto("1", null, "0.0");
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_100 = new LoanAccountDetailsDto("1", null, "100.0");
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_PURPOSE_NULL = new LoanAccountDetailsDto("1", null, null);
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_VALID_PURPOSE = new LoanAccountDetailsDto("1", "Valid loan purpose", null);
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_PURPOSE_EMPTY = new LoanAccountDetailsDto("1", "", null);
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_AND_PURPOSE_NULL = new LoanAccountDetailsDto("2", null, null);
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_AND_PURPOSE_NULL2 = new LoanAccountDetailsDto("3", null, null);
    public static final LoanAccountDetailsDto LOAN_ACCOUNT_DETAILS_WITH_LOAN_AMOUNT_200 = new LoanAccountDetailsDto("2", "1", "200.0", "1");
}