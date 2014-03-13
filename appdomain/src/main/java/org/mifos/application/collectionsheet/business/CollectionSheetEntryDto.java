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

package org.mifos.application.collectionsheet.business;

import java.util.ArrayList;
import java.util.List;

import org.mifos.accounts.loan.persistance.LegacyLoanDao;
import org.mifos.accounts.loan.util.helpers.LoanAccountDto;
import org.mifos.accounts.loan.util.helpers.LoanAccountsProductDto;
import org.mifos.accounts.productdefinition.util.helpers.RecommendedAmountUnit;
import org.mifos.accounts.productdefinition.util.helpers.SavingsType;
import org.mifos.accounts.savings.util.helpers.SavingsAccountDto;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.customers.api.DataTransferObject;
import org.mifos.customers.client.business.service.ClientAttendanceDto;
import org.mifos.customers.util.helpers.CustomerAccountDto;
import org.mifos.dto.domain.CustomerDto;
import org.mifos.framework.util.LocalizationConverter;
import org.mifos.framework.util.helpers.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionSheetEntryDto implements DataTransferObject {

    private static final Logger logger = LoggerFactory.getLogger(CollectionSheetEntryDto.class);

    private boolean hasChild;

    private final List<CollectionSheetEntryDto> collectionSheetEntryChildren;

    private final CustomerDto customerDetail;

    private final List<LoanAccountsProductDto> loanAccountDetails;

    private final List<SavingsAccountDto> savingsAccountDetails;

    private CustomerAccountDto customerAccountDetails;

    private Short attendence;

    private final MifosCurrency currency;

    private int countOfCustomers;

    public CollectionSheetEntryDto(final CustomerDto customerDetail, final MifosCurrency currency) {
        this.customerDetail = customerDetail;
        loanAccountDetails = new ArrayList<LoanAccountsProductDto>();
        savingsAccountDetails = new ArrayList<SavingsAccountDto>();
        collectionSheetEntryChildren = new ArrayList<CollectionSheetEntryDto>();
        this.currency = currency;
    }

    public List<LoanAccountsProductDto> getLoanAccountDetails() {
        return loanAccountDetails;
    }

    public void addLoanAccountDetails(final LoanAccountDto loanAccount) {
        for (LoanAccountsProductDto loanAccountProductView : loanAccountDetails) {
            if (isProductExists(loanAccount, loanAccountProductView)) {
                loanAccountProductView.addLoanAccountView(loanAccount);
                return;
            }
        }
        LoanAccountsProductDto loanAccountProductView = new LoanAccountsProductDto(loanAccount.getPrdOfferingId(),
                loanAccount.getPrdOfferingShortName());
        loanAccountProductView.addLoanAccountView(loanAccount);
        this.loanAccountDetails.add(loanAccountProductView);
    }

    public List<SavingsAccountDto> getSavingsAccountDetails() {
        return savingsAccountDetails;
    }

    public void addSavingsAccountDetail(final SavingsAccountDto savingsAccount) {
        for (SavingsAccountDto savingsAccountDto : savingsAccountDetails) {
            if (savingsAccount.getSavingsOfferingId().equals(savingsAccountDto.getSavingsOfferingId())) {
                return;
            }
        }
        this.savingsAccountDetails.add(savingsAccount);
    }

    public Short getAttendence() {
        return attendence;
    }

    public void setAttendence(final Short attendence) {
        this.attendence = attendence;
    }

    public List<CollectionSheetEntryDto> getCollectionSheetEntryChildren() {
        return collectionSheetEntryChildren;
    }

    public CustomerDto getCustomerDetail() {
        return customerDetail;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void addChildNode(final CollectionSheetEntryDto leafNode) {
        collectionSheetEntryChildren.add(leafNode);
        this.hasChild = true;
    }

    public MifosCurrency getCurrency() {
        return currency;
    }

    public CustomerAccountDto getCustomerAccountDetails() {
        return customerAccountDetails;
    }

    public void setCustomerAccountDetails(final CustomerAccountDto customerAccountDetails) {
        this.customerAccountDetails = customerAccountDetails;
    }

    public void populateLoanAccountsInformation(final List<LoanAccountDto> loanAccountViewList,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final List<CollectionSheetEntryAccountFeeActionDto> collectionSheetEntryAccountFeeActionDtos) {

        final Integer customerId = customerDetail.getCustomerId();

        List<LoanAccountDto> loanAccountsForThisCustomer = new ArrayList<LoanAccountDto>();
        for (LoanAccountDto loanAccountDto : loanAccountViewList) {
            if (customerId.equals(loanAccountDto.getCustomerId())) {
                loanAccountsForThisCustomer.add(loanAccountDto);
            }
        }

        for (LoanAccountDto loanAccountDto : loanAccountsForThisCustomer) {
            addLoanAccountDetails(loanAccountDto);
            if (loanAccountDto.isDisbursalAccount()) {
                loanAccountDto.setAmountPaidAtDisbursement(getAmountPaidAtDisb(loanAccountDto, customerId,
                        collectionSheetEntryAccountActionViews, collectionSheetEntryAccountFeeActionDtos));
            } else {
                loanAccountDto.addTrxnDetails(retrieveLoanSchedule(loanAccountDto.getAccountId(), customerId,
                        collectionSheetEntryAccountActionViews, collectionSheetEntryAccountFeeActionDtos));
            }
        }
    }

    public void populateCustomerAccountInformation(final List<CustomerAccountDto> customerAccountList,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final List<CollectionSheetEntryAccountFeeActionDto> collectionSheetEntryAccountFeeActionDtos) {

        Integer accountId = Integer.valueOf(0);
        for (CustomerAccountDto customerAccountDto : customerAccountList) {
            if (customerDetail.getCustomerId().equals(customerAccountDto.getCustomerId())) {

                accountId = customerAccountDto.getAccountId();
                customerAccountDto.setAccountActionDates(retrieveCustomerSchedule(accountId, customerAccountDto
                        .getCustomerId(), collectionSheetEntryAccountActionViews,
                        collectionSheetEntryAccountFeeActionDtos));
                setCustomerAccountDetails(customerAccountDto);

            }
        }

    }

    public void populateSavingsAccountsInformation(final List<SavingsAccountDto> savingsAccountViewList) {

        final List<SavingsAccountDto> savingsAccounts = new ArrayList<SavingsAccountDto>();
        for (SavingsAccountDto savingsAccountDto : savingsAccountViewList) {
            if (savingsAccountDto.getCustomerId().equals(customerDetail.getCustomerId())) {
                savingsAccounts.add(savingsAccountDto);
            }
        }

        if (customerDetail.isCustomerCenter()) {
            for (CollectionSheetEntryDto child : collectionSheetEntryChildren) {
                addSavingsAccountViewToClients(child.getCollectionSheetEntryChildren(), savingsAccounts);
            }
        } else if (customerDetail.isCustomerGroup()) {
            addSavingsAccountViewToClients(collectionSheetEntryChildren, savingsAccounts);
        }

        for (SavingsAccountDto savingsAccountDto : savingsAccounts) {
            addSavingsAccountDetail(savingsAccountDto);
        }
    }

    public void populateSavingsAccountActions(final Integer customerId,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews) {
        if (customerDetail.isCustomerCenter()) {
            return;
        }
        for (SavingsAccountDto savingsAccountDto : savingsAccountDetails) {
            if (!(customerDetail.isCustomerGroup() && savingsAccountDto.getRecommendedAmntUnitId().equals(
                    RecommendedAmountUnit.PER_INDIVIDUAL.getValue()))) {
                addAccountActionToSavingsView(savingsAccountDto, customerId, collectionSheetEntryAccountActionViews);
            }
        }
    }

    public void populateClientAttendance(final Integer customerId,
            final List<ClientAttendanceDto> collectionSheetEntryClientAttendanceViews) {
        if (customerDetail.isCustomerCenter()) {
            return;
        }
        for (ClientAttendanceDto clientAttendanceView : collectionSheetEntryClientAttendanceViews) {
            logger.debug("populateClientAttendance");
            logger.debug("clientAttendanceView.getCustomerId() " + clientAttendanceView.getClientId());
            logger.debug("customerId " + customerId);
            logger.debug("customerDetail.getCustomerId() " + customerDetail.getCustomerId());
            if (clientAttendanceView.getClientId().compareTo(customerId) == 0) {
                Short attendanceId = clientAttendanceView.getAttendanceId();
                setAttendence(attendanceId);
            }
        }
    }

    public void setSavinsgAmountsEntered(final Short prdOfferingId, final String depositAmountEnteredValue,
            final String withDrawalAmountEnteredValue) {
        for (SavingsAccountDto savingsAccountDto : savingsAccountDetails) {
            if (prdOfferingId.equals(savingsAccountDto.getSavingsOfferingId())) {
                savingsAccountDto.setDepositAmountEntered(depositAmountEnteredValue);
                savingsAccountDto.setWithDrawalAmountEntered(withDrawalAmountEnteredValue);
                try {
                    if (depositAmountEnteredValue != null && !"".equals(depositAmountEnteredValue.trim())) {
                        new LocalizationConverter().getDoubleValueForCurrentLocale(depositAmountEnteredValue);
                        savingsAccountDto.setValidDepositAmountEntered(true);
                    }
                } catch (NumberFormatException nfe) {
                    savingsAccountDto.setValidDepositAmountEntered(false);
                }
                try {
                    if (withDrawalAmountEnteredValue != null && !"".equals(withDrawalAmountEnteredValue.trim())) {
                        new LocalizationConverter()
                                .getDoubleValueForCurrentLocale(withDrawalAmountEnteredValue);
                        savingsAccountDto.setValidWithDrawalAmountEntered(true);
                    }
                } catch (NumberFormatException nfe) {
                    savingsAccountDto.setValidWithDrawalAmountEntered(false);
                }
            }
        }
    }

    public void setLoanAmountsEntered(final Short prdOfferingId, final String enteredAmountValue, final String disbursementAmount) {
        for (LoanAccountsProductDto loanAccountView : loanAccountDetails) {
            if (prdOfferingId.equals(loanAccountView.getPrdOfferingId())) {
                loanAccountView.setEnteredAmount(enteredAmountValue);
                loanAccountView.setDisBursementAmountEntered(disbursementAmount);
                try {
                    if (null != enteredAmountValue) {
                        new LocalizationConverter().getDoubleValueForCurrentLocale(enteredAmountValue);
                    }
                    loanAccountView.setValidAmountEntered(true);
                } catch (NumberFormatException ne) {
                    loanAccountView.setValidAmountEntered(false);
                }
                try {
                    if (null != disbursementAmount) {
                        new LocalizationConverter().getDoubleValueForCurrentLocale(disbursementAmount);
                    }
                    loanAccountView.setValidDisbursementAmount(true);
                } catch (NumberFormatException ne) {
                    loanAccountView.setValidDisbursementAmount(false);
                }

            }
        }
    }

    public void setCustomerAccountAmountEntered(final String customerAccountAmountEntered) {
        customerAccountDetails.setCustomerAccountAmountEntered(customerAccountAmountEntered);
        try {
            new LocalizationConverter().getDoubleValueForCurrentLocale(customerAccountAmountEntered);
            customerAccountDetails.setValidCustomerAccountAmountEntered(true);
        } catch (NumberFormatException nfe) {
            customerAccountDetails.setValidCustomerAccountAmountEntered(false);
        }
    }

    public int getCountOfCustomers() {
        return this.countOfCustomers;
    }

    public void setCountOfCustomers(final int countOfCustomers) {
        this.countOfCustomers = countOfCustomers;
    }

    private boolean isProductExists(final LoanAccountDto loanAccount, final LoanAccountsProductDto loanAccountProductView) {
        if (loanAccountProductView.getPrdOfferingId() != null
                && loanAccountProductView.getPrdOfferingId().equals(loanAccount.getPrdOfferingId())) {
            return true;
        }
        return false;

    }

    private void addAccountActionToSavingsView(final SavingsAccountDto savingsAccountDto, final Integer customerId,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews) {
        boolean isMandatory = false;
        if (savingsAccountDto.getSavingsTypeId().equals(SavingsType.MANDATORY.getValue())) {
            isMandatory = true;
        }
        List<CollectionSheetEntryInstallmentDto> accountActionDetails = retrieveSavingsAccountActions(
                savingsAccountDto.getAccountId(), customerId, collectionSheetEntryAccountActionViews, isMandatory);
        if (accountActionDetails != null) {
            for (CollectionSheetEntryInstallmentDto accountAction : accountActionDetails) {
                savingsAccountDto.addAccountTrxnDetail(accountAction);
            }
        }
    }

    private List<CollectionSheetEntryInstallmentDto> retrieveSavingsAccountActions(final Integer accountId,
            final Integer customerId, final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final boolean isMandatory) {
        int index = collectionSheetEntryAccountActionViews.indexOf(new CollectionSheetEntrySavingsInstallmentDto(
                accountId, customerId));
        if (!isMandatory && index != -1) {
            return collectionSheetEntryAccountActionViews.subList(index, index + 1);
        }
        int lastIndex = collectionSheetEntryAccountActionViews
                .lastIndexOf(new CollectionSheetEntrySavingsInstallmentDto(accountId, customerId));
        if (lastIndex != -1 && index != -1) {
            return collectionSheetEntryAccountActionViews.subList(index, lastIndex + 1);
        }
        return null;
    }

    private SavingsAccountDto createSavingsAccountViewFromExisting(final SavingsAccountDto savingsAccountDto) {
        return new SavingsAccountDto(savingsAccountDto.getAccountId(), savingsAccountDto.getCustomerId(),
                savingsAccountDto.getSavingsOfferingShortName(), savingsAccountDto.getSavingsOfferingId(),
                savingsAccountDto.getSavingsTypeId(), savingsAccountDto.getRecommendedAmntUnitId());
    }

    private void addSavingsAccountViewToClients(final List<CollectionSheetEntryDto> clientCollectionSheetEntryViews,
            final List<SavingsAccountDto> savingsAccountDtos) {
        for (CollectionSheetEntryDto collectionSheetEntryDto : clientCollectionSheetEntryViews) {
            for (SavingsAccountDto savingsAccountDto : savingsAccountDtos) {
                collectionSheetEntryDto.addSavingsAccountDetail(createSavingsAccountViewFromExisting(savingsAccountDto));
            }
        }
    }

    private Double getAmountPaidAtDisb(final LoanAccountDto loanAccountDto, final Integer customerId,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final List<CollectionSheetEntryAccountFeeActionDto> collectionSheetEntryAccountFeeActionDtos) {

        if (loanAccountDto.isInterestDeductedAtDisbursement()) {
            return getInterestAmountDedAtDisb(retrieveLoanSchedule(loanAccountDto.getAccountId(), customerId,
                    collectionSheetEntryAccountActionViews, collectionSheetEntryAccountFeeActionDtos));
        }

        return ApplicationContextProvider.getBean(LegacyLoanDao.class).getFeeAmountAtDisbursement(loanAccountDto.getAccountId(), Money.getDefaultCurrency()).getAmountDoubleValue();
    }

    private Double getInterestAmountDedAtDisb(final List<CollectionSheetEntryInstallmentDto> installments) {
        for (CollectionSheetEntryInstallmentDto collectionSheetEntryAccountAction : installments) {
            if (collectionSheetEntryAccountAction.getInstallmentId().shortValue() == 1) {
                return ((CollectionSheetEntryLoanInstallmentDto) collectionSheetEntryAccountAction)
                        .getTotalDueWithFees().getAmountDoubleValue();
            }
        }
        return 0.0;
    }

    private List<CollectionSheetEntryInstallmentDto> retrieveLoanSchedule(final Integer accountId, final Integer customerId,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final List<CollectionSheetEntryAccountFeeActionDto> collectionSheetEntryAccountFeeActionDtos) {
        int index = collectionSheetEntryAccountActionViews.indexOf(new CollectionSheetEntryLoanInstallmentDto(
                accountId, customerId, getCurrency()));
        int lastIndex = collectionSheetEntryAccountActionViews.lastIndexOf(new CollectionSheetEntryLoanInstallmentDto(
                accountId, customerId, getCurrency()));
        if (lastIndex != -1 && index != -1) {
            List<CollectionSheetEntryInstallmentDto> applicableInstallments = collectionSheetEntryAccountActionViews
                    .subList(index, lastIndex + 1);
            for (CollectionSheetEntryInstallmentDto collectionSheetEntryAccountActionView : applicableInstallments) {
                int feeIndex = collectionSheetEntryAccountFeeActionDtos
                        .indexOf(new CollectionSheetEntryAccountFeeActionDto(collectionSheetEntryAccountActionView
                                .getActionDateId()));
                int feeLastIndex = collectionSheetEntryAccountFeeActionDtos
                        .lastIndexOf(new CollectionSheetEntryAccountFeeActionDto(collectionSheetEntryAccountActionView
                                .getActionDateId()));
                if (feeIndex != -1 && feeLastIndex != -1) {
                    ((CollectionSheetEntryLoanInstallmentDto) collectionSheetEntryAccountActionView)
                            .setCollectionSheetEntryAccountFeeActions(collectionSheetEntryAccountFeeActionDtos
                                    .subList(feeIndex, feeLastIndex + 1));
                }
            }
            return applicableInstallments;
        }
        return null;
    }

    private List<CollectionSheetEntryInstallmentDto> retrieveCustomerSchedule(final Integer accountId, final Integer customerId,
            final List<CollectionSheetEntryInstallmentDto> collectionSheetEntryAccountActionViews,
            final List<CollectionSheetEntryAccountFeeActionDto> collectionSheetEntryAccountFeeActionDtos) {
        int index = collectionSheetEntryAccountActionViews
                .indexOf(new CollectionSheetEntryCustomerAccountInstallmentDto(accountId, customerId, getCurrency()));
        int lastIndex = collectionSheetEntryAccountActionViews
                .lastIndexOf(new CollectionSheetEntryCustomerAccountInstallmentDto(accountId, customerId, getCurrency()));
        if (lastIndex != -1 && index != -1) {
            List<CollectionSheetEntryInstallmentDto> applicableInstallments = collectionSheetEntryAccountActionViews
                    .subList(index, lastIndex + 1);
            for (CollectionSheetEntryInstallmentDto collectionSheetEntryAccountActionView : applicableInstallments) {
                int feeIndex = collectionSheetEntryAccountFeeActionDtos
                        .indexOf(new CollectionSheetEntryAccountFeeActionDto(collectionSheetEntryAccountActionView
                                .getActionDateId()));
                int feeLastIndex = collectionSheetEntryAccountFeeActionDtos
                        .lastIndexOf(new CollectionSheetEntryAccountFeeActionDto(collectionSheetEntryAccountActionView
                                .getActionDateId()));
                if (feeIndex != -1 && feeLastIndex != -1) {
                    ((CollectionSheetEntryCustomerAccountInstallmentDto) collectionSheetEntryAccountActionView)
                            .setCollectionSheetEntryAccountFeeActions(collectionSheetEntryAccountFeeActionDtos
                                    .subList(feeIndex, feeLastIndex + 1));
                }
            }
            return applicableInstallments;
        }
        return null;
    }
}
