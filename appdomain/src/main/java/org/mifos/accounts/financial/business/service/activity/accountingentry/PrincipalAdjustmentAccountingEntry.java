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

package org.mifos.accounts.financial.business.service.activity.accountingentry;

import org.mifos.accounts.financial.business.FinancialActionTypeEntity;
import org.mifos.accounts.financial.business.GLCodeEntity;
import org.mifos.accounts.financial.exceptions.FinancialException;
import org.mifos.accounts.financial.util.helpers.FinancialActionCache;
import org.mifos.accounts.financial.util.helpers.FinancialActionConstants;
import org.mifos.accounts.financial.util.helpers.FinancialConstants;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanTrxnDetailEntity;
import org.mifos.config.AccountingRules;
import org.mifos.framework.util.helpers.Money;

public class PrincipalAdjustmentAccountingEntry extends BaseAccountingEntry {

    @Override
    protected void applySpecificAccountActionEntry() throws FinancialException {
        LoanTrxnDetailEntity loanTrxn = (LoanTrxnDetailEntity) financialActivity.getAccountTrxn();
        GLCodeEntity glcodeCredit = ((LoanBO) loanTrxn.getAccount()).getLoanOffering().getPrincipalGLcode();

        FinancialActionTypeEntity finActionPrincipal = FinancialActionCache
                .getFinancialAction(FinancialActionConstants.PRINCIPALPOSTING);
        addAccountEntryDetails(loanTrxn.getPrincipalAmount(), finActionPrincipal,
                getGLcode(finActionPrincipal.getApplicableDebitCharts()), FinancialConstants.CREDIT);

        addAccountEntryDetails(loanTrxn.getPrincipalAmount(), finActionPrincipal, glcodeCredit,
                FinancialConstants.DEBIT);

        LoanBO loan = (LoanBO) loanTrxn.getAccount();
        if (!loan.isLegacyLoan()) {
            return;
        }

        // check if rounding is required
        Money roundedAmount = Money.round(loanTrxn.getPrincipalAmount(),
                loanTrxn.getPrincipalAmount().getCurrency().getRoundingAmount(), AccountingRules.getCurrencyRoundingMode());
        if (!roundedAmount.equals(loanTrxn.getPrincipalAmount())) {
            FinancialActionTypeEntity finActionRounding = FinancialActionCache
                    .getFinancialAction(FinancialActionConstants.ROUNDING);

            addAccountEntryDetails(roundedAmount.subtract(loanTrxn.getPrincipalAmount()).negate(), finActionRounding,
                    getGLcode(finActionPrincipal.getApplicableCreditCharts()), FinancialConstants.CREDIT);

            addAccountEntryDetails(roundedAmount.subtract(loanTrxn.getPrincipalAmount()), finActionRounding,
                    getGLcode(finActionRounding.getApplicableCreditCharts()), FinancialConstants.DEBIT);
        }
    }

}
