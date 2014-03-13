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

package org.mifos.test.acceptance.framework.savings;

import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.loan.RepayLoanParameters;
import org.testng.Assert;

import com.thoughtworks.selenium.Selenium;

public class SavingsDepositWithdrawalPage  extends MifosPage{

    public SavingsDepositWithdrawalPage(Selenium selenium) {
        super(selenium);
    }

    public void verifyPage() {
        this.verifyPage("applypayment_savingsaccount");
    }

    public SavingsDepositWithdrawalConfirmationPage submitAndNavigateToDepositWithdrawalConfirmationPage(DepositWithdrawalSavingsParameters params)
    {
        selectClientId(params);
        typeTextIfNotEmpty("trxnDateDD", params.getTrxnDateDD());
        typeTextIfNotEmpty("trxnDateMM", params.getTrxnDateMM());
        typeTextIfNotEmpty("trxnDateYY", params.getTrxnDateYYYY());

        selenium.select("applypayment_savingsaccount.input.trxnType", "value=" + params.getTrxnTypeValue() );
        waitForPageToLoad();

        this.typeTextIfNotEmpty("applypayment_savingsaccount.input.amount", params.getAmount());

        selenium.select("applypayment_savingsaccount.input.paymentType", "value=" + params.getPaymentTypeValue() );

        this.typeTextIfNotEmpty("applypayment_savingsaccount.input.receiptId", params.getReceiptId());

        this.typeTextIfNotEmpty("receiptDateDD", params.getReceiptDateDD());
        this.typeTextIfNotEmpty("receiptDateMM", params.getReceiptDateMM());
        this.typeTextIfNotEmpty("receiptDateYY", params.getReceiptDateYYYY());
        selenium.click("applypayment_savingsaccount.button.submit");
        waitForPageToLoad();

        return new SavingsDepositWithdrawalConfirmationPage(selenium);
    }

    private void selectClientId(DepositWithdrawalSavingsParameters params) {
        String clientId = params.getClientId();
        if (clientId != null) {
            selenium.select("applypayment_savingsaccount.input.customerId", "value=" + clientId);
            waitForPageToLoad();
        }
    }

    public void verifyModeOfPayments(){
        waitForPageToLoad();
        waitForElementToPresent("applypayment_savingsaccount.input.paymentType");
        String[] modesOfPayment=selenium.getSelectOptions("applypayment_savingsaccount.input.paymentType");
        Assert.assertEquals(RepayLoanParameters.CASH,modesOfPayment[1]);
        Assert.assertEquals(RepayLoanParameters.CHEQUE,modesOfPayment[2]);
        Assert.assertEquals(RepayLoanParameters.VOUCHER,modesOfPayment[3]);
    }

    public void selectPaymentType(String paymentType) {
        selenium.waitForCondition("selenium.isElementPresent(\"" + "applypayment_savingsaccount.input.trxnType" + "\")","30000");
        if ("Deposit".equals(paymentType)) {
            selenium.select("applypayment_savingsaccount.input.trxnType", "value=6");
        }else if ("Withdrawal".equals(paymentType)) {
            selenium.select("applypayment_savingsaccount.input.trxnType", "value=7");
        }
    }


}
