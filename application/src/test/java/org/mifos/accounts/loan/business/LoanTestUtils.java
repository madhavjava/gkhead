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

package org.mifos.accounts.loan.business;

import java.util.Date;
import java.util.Set;

import junit.framework.Assert;

import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountFeesActionDetailEntity;
import org.mifos.accounts.util.helpers.PaymentStatus;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.Money;

public class LoanTestUtils {

    /**
     * Check amount of the account fee in the installment. Note that this only
     * works correctly if the installment has just one account fee.
     */
    public static void assertOneInstallmentFee(Money expected, LoanScheduleEntity installment) {
        Set<AccountFeesActionDetailEntity> actionDetails = installment.getAccountFeesActionDetails();
        Assert.assertFalse("expected fee > 0.0 but loan has no account fees", expected.getAmountDoubleValue() > 0.0
                & actionDetails.size() == 0);
        for (AccountFeesActionDetailEntity detail : actionDetails) {
            Assert.assertEquals(expected, detail.getFeeDue());
        }
    }

    public static void assertInstallmentDetails(LoanScheduleEntity installment, Double principal, Double interest,
            Double accountFee, Double miscFee, Double miscPenalty) {
        Assert.assertEquals(TestUtils.createMoney(principal.toString()), installment.getPrincipalDue());
        Assert.assertEquals(TestUtils.createMoney(interest.toString()), installment.getInterestDue());
        Assert.assertEquals(TestUtils.createMoney(miscFee.toString()), installment.getMiscFeeDue());
        Assert.assertEquals(TestUtils.createMoney(miscPenalty.toString()), installment.getMiscPenaltyDue());
        assertOneInstallmentFee(TestUtils.createMoney(accountFee.toString()), installment);
    }

    public static void assertInstallmentDetails(LoanBO loan, int installmentId, Double total, Double principal,
            Double interest, Double accountFee, Double miscFee, Double miscPenalty) {

        LoanScheduleEntity installment = (LoanScheduleEntity) loan.getAccountActionDate((short) installmentId);
        Assert.assertEquals(TestUtils.createMoney(total.toString()), installment.getTotalPaymentDue());
        Assert.assertEquals(TestUtils.createMoney(principal.toString()), installment.getPrincipalDue());
        Assert.assertEquals(TestUtils.createMoney(interest.toString()), installment.getInterestDue());
        Assert.assertEquals(TestUtils.createMoney(miscFee.toString()), installment.getMiscFeeDue());
        Assert.assertEquals(TestUtils.createMoney(miscPenalty.toString()), installment.getMiscPenaltyDue());
        assertOneInstallmentFee(TestUtils.createMoney(accountFee.toString()), installment);
    }

    public static void assertInstallmentDetails(LoanBO loan, int installmentId, Double principal, Double interest,
            Double accountFee, Double miscFee, Double miscPenalty) {

        LoanScheduleEntity installment = (LoanScheduleEntity) loan.getAccountActionDate((short) installmentId);
        Assert.assertEquals(TestUtils.createMoney(principal.toString()), installment.getPrincipalDue());
        Assert.assertEquals(TestUtils.createMoney(interest.toString()), installment.getInterestDue());
        Assert.assertEquals(TestUtils.createMoney(miscFee.toString()), installment.getMiscFeeDue());
        Assert.assertEquals(TestUtils.createMoney(miscPenalty.toString()), installment.getMiscPenaltyDue());
        assertOneInstallmentFee(TestUtils.createMoney(accountFee.toString()), installment);
    }

    public static LoanScheduleEntity[] getSortedAccountActionDateEntity(
            Set<AccountActionDateEntity> actionDateCollection) {

        LoanScheduleEntity[] sortedList = new LoanScheduleEntity[actionDateCollection.size()];

        // Don't know whether it will always be 6 for future tests, but
        // right now it is...
        Assert.assertEquals(6, actionDateCollection.size());

        for (AccountActionDateEntity actionDateEntity : actionDateCollection) {
            sortedList[actionDateEntity.getInstallmentId().intValue() - 1] = (LoanScheduleEntity) actionDateEntity;
        }

        return sortedList;
    }

    public static LoanScheduleEntity getLoanScheduleEntity(LoanBO loan, String installmentId, Date dueDate, PaymentStatus status,
                                                           double[] actualAmounts, double[] paidAmounts
    ) {

        return new LoanScheduleBuilder(installmentId, loan).withInstallmentId(installmentId).withDueDate(dueDate).
                withPaymentStatus(status).withPrincipal(actualAmounts[0]).withInterest(actualAmounts[1]).
                withExtraInterest(actualAmounts[2]).withMiscFees(actualAmounts[3]).withPenalty(actualAmounts[4]).
                withMiscPenalty(actualAmounts[5]).withPrincipalPaid(paidAmounts[0]).withInterestPaid(paidAmounts[1]).
                withExtraInterestPaid(paidAmounts[2]).withMiscFeesPaid(paidAmounts[3]).withPenaltyPaid(paidAmounts[4]).
                withMiscPenaltyPaid(paidAmounts[5]).addFees(1, actualAmounts[6], paidAmounts[6]).
                withPaymentDate(TestUtils.getSqlDate(12, 1, 2010)).build();
    }
}
