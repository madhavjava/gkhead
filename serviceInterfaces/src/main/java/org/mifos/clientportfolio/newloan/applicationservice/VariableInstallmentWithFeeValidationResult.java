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

package org.mifos.clientportfolio.newloan.applicationservice;

public class VariableInstallmentWithFeeValidationResult {

    private final boolean feeCanBeAppliedToVariableInstallmentLoan;
    private final String feeName;

    public VariableInstallmentWithFeeValidationResult(boolean feeCanBeAppliedToVariableInstallmentLoan, String feeName) {
        this.feeCanBeAppliedToVariableInstallmentLoan = feeCanBeAppliedToVariableInstallmentLoan;
        this.feeName = feeName;
    }

    public boolean isFeeCanBeAppliedToVariableInstallmentLoan() {
        return feeCanBeAppliedToVariableInstallmentLoan;
    }

    public String getFeeName() {
        return feeName;
    }
}