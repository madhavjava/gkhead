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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.framework.util.helpers.Money;
import org.mifos.schedule.ScheduledEvent;
import org.mifos.schedule.ScheduledEventFactory;

public class FeeInstallment {

    private Short installmentId;
    private Money accountFee;
    private AccountFeesEntity accountFeesEntity = null;

    public static FeeInstallment buildFeeInstallment(final Short installmentId, final Money accountFeeAmount,
            final AccountFeesEntity accountFee) {
        FeeInstallment feeInstallment = new FeeInstallment();
        feeInstallment.setInstallmentId(installmentId);
        feeInstallment.setAccountFee(accountFeeAmount);
        feeInstallment.setAccountFeesEntity(accountFee);
        //accountFee.setAccountFeeAmount(accountFeeAmount);
        return feeInstallment;
    }

    public static List<FeeInstallment> createMergedFeeInstallments(ScheduledEvent masterEvent,
            Collection<AccountFeesEntity> accountFees, int numberOfInstallments) {

        return createMergedFeeInstallmentsStartingWith(masterEvent, accountFees, numberOfInstallments, 1);
    }

    public static List<FeeInstallment> createMergedFeeInstallmentsStartingWith(ScheduledEvent masterEvent,
            Collection<AccountFeesEntity> accountFees, int numberOfInstallments, int startingInstallmentNumber) {

        List<FeeInstallment> mergedFeeInstallments = new ArrayList<FeeInstallment>();
        for (AccountFeesEntity accountFeesEntity : accountFees) {
            mergedFeeInstallments
                .addAll(createMergedFeeInstallmentsForOneFeeStartingWith(masterEvent, accountFeesEntity, numberOfInstallments, startingInstallmentNumber));
            }
        return mergedFeeInstallments;
    }

    public static List<FeeInstallment> createMergedFeeInstallmentsForOneFee (
            ScheduledEvent masterEvent, AccountFeesEntity accountFeesEntity, int numberOfInstallments) {

        return createMergedFeeInstallmentsForOneFeeStartingWith(masterEvent, accountFeesEntity, numberOfInstallments, 1);
    }

    public static List<FeeInstallment> createMergedFeeInstallmentsForOneFeeStartingWith (ScheduledEvent masterEvent,
            AccountFeesEntity accountFeesEntity, int numberOfInstallments, int startingInstallmentNumber) {

        List<FeeInstallment> mergedFeeInstallments = new ArrayList<FeeInstallment>();
        if (accountFeesEntity.getFees().isOneTime()) {
            FeeInstallment feeInstallment
            = buildFeeInstallment(
                    (short) startingInstallmentNumber,  //Customer one-time fees are always up-front, due at the first meeting
                    accountFeesEntity.getAccountFeeAmount(),
                    accountFeesEntity);
            mergedFeeInstallments.add(feeInstallment);
        } else { // periodic fee
            ScheduledEvent feesEvent
            = ScheduledEventFactory
            .createScheduledEventFrom(accountFeesEntity.getFees().getFeeFrequency().getFeeMeetingFrequency());
            for (short installmentId = (short) startingInstallmentNumber; installmentId <= numberOfInstallments + startingInstallmentNumber - 1; installmentId++) {
                int numberOfFeeInstallmentsToRollup
                    = masterEvent.numberOfDependentOccurrencesRollingUpToThisOccurrenceStartingWith(feesEvent, installmentId, startingInstallmentNumber);
                if (numberOfFeeInstallmentsToRollup > 0) {
                    FeeInstallment feeInstallment
                    = buildFeeInstallment(
                            installmentId,
                            accountFeesEntity.getAccountFeeAmount().multiply(numberOfFeeInstallmentsToRollup),
                            accountFeesEntity);
                    mergedFeeInstallments.add(feeInstallment);
                }
            }
        }
        return mergedFeeInstallments;
    }

    public static List<FeeInstallment> mergeFeeInstallments(final List<FeeInstallment> feeInstallmentList) {
        List<FeeInstallment> newFeeInstallmentList = new ArrayList<FeeInstallment>();
        for (Iterator<FeeInstallment> iterator = feeInstallmentList.iterator(); iterator.hasNext();) {
            FeeInstallment feeInstallment = iterator.next();
            iterator.remove();
            FeeInstallment feeInstTemp = null;
            for (FeeInstallment feeInst : newFeeInstallmentList) {
                if (feeInst.getInstallmentId().equals(feeInstallment.getInstallmentId())
                        && feeInst.getAccountFeesEntity().equals(feeInstallment.getAccountFeesEntity())) {
                    feeInstTemp = feeInst;
                    break;
                }
            }
            if (feeInstTemp != null) {
                newFeeInstallmentList.remove(feeInstTemp);
                feeInstTemp.setAccountFee(feeInstTemp.getAccountFee().add(feeInstallment.getAccountFee()));
                newFeeInstallmentList.add(feeInstTemp);
            } else {
                newFeeInstallmentList.add(feeInstallment);
            }
        }
        return newFeeInstallmentList;
    }

    public Money getAccountFee() {
        return accountFee;
    }

    public void setAccountFee(Money accountFee) {
        this.accountFee = accountFee;
    }

    public AccountFeesEntity getAccountFeesEntity() {
        return accountFeesEntity;
    }

    public void setAccountFeesEntity(AccountFeesEntity accountFeesEntity) {
        this.accountFeesEntity = accountFeesEntity;
    }

    public Short getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(Short installmentId) {
        this.installmentId = installmentId;
    }

}
