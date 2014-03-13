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

package org.mifos.customers.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifos.accounts.business.AccountActionDateEntity;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountFeesActionDetailEntity;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.business.AccountPaymentEntity;
import org.mifos.accounts.business.AccountTrxnEntity;
import org.mifos.accounts.business.FeesTrxnDetailEntity;
import org.mifos.accounts.exceptions.AccountException;
import org.mifos.accounts.fees.business.AmountFeeBO;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.business.FeeDto;
import org.mifos.accounts.fees.util.helpers.FeeChangeType;
import org.mifos.accounts.fees.util.helpers.FeeStatus;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.util.helpers.AccountActionTypes;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.accounts.util.helpers.AccountExceptionConstants;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.accounts.util.helpers.FeeInstallment;
import org.mifos.accounts.util.helpers.InstallmentDate;
import org.mifos.accounts.util.helpers.PaymentData;
import org.mifos.accounts.util.helpers.PaymentStatus;
import org.mifos.accounts.util.helpers.WaiveEnum;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.holiday.business.Holiday;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.calendar.CalendarEvent;
import org.mifos.clientportfolio.newloan.domain.RecurringScheduledEventFactoryImpl;
import org.mifos.core.MifosRuntimeException;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.group.util.helpers.GroupConstants;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.persistence.LegacyPersonnelDao;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.components.batchjobs.exceptions.BatchJobException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.PropertyNotFoundException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.LocalizationConverter;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;
import org.mifos.schedule.ScheduledDateGeneration;
import org.mifos.schedule.ScheduledEvent;
import org.mifos.schedule.ScheduledEventFactory;
import org.mifos.schedule.internal.DailyScheduledEvent;
import org.mifos.schedule.internal.HolidayAndWorkingDaysAndMoratoriaScheduledDateGeneration;
import org.mifos.security.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clients, groups, and centers are stored in the db as customer accounts.
 */
public class CustomerAccountBO extends AccountBO {

    private static final Logger logger = LoggerFactory.getLogger(CustomerAccountBO.class);

    private List<CustomerActivityEntity> customerActivitDetails = new ArrayList<CustomerActivityEntity>();

    private static int numberOfMeetingDatesToGenerate = 10;

    public static CustomerAccountBO createNew(CustomerBO customer, List<AccountFeesEntity> accountFees,
            MeetingBO customerMeeting, CalendarEvent applicableCalendarEvents) {

        try {
            CustomerAccountBO customerAccount = new CustomerAccountBO(customer, accountFees);
            if (customer.isActive()) {
                DateTime scheduleGenerationStartingFromDate = new DateTime(customer.getCustomerActivationDate());
                customerAccount.createSchedulesAndFeeSchedulesForFirstTimeActiveCustomer(customer, accountFees, customerMeeting, applicableCalendarEvents, scheduleGenerationStartingFromDate);
            }
            return customerAccount;
        } catch (AccountException e) {
            throw new MifosRuntimeException(e);
        }
    }

    /**
     * Create an initial meeting schedule with fees attached, if any.
     *
     * <p>PostConditions:</p>
     *
     * <ul>
     * <li> <code>numberOfMeetingDatesToGenerateOnCreation</code> {@link CustomerScheduleEntity}s are created
     *      starting with <code>customerMeeting</code>'s start date, scheduled according to <code>customerMeeting</code>'s
     *      frequency and recurrence, and subject to rules for scheduling around on working days and around holidays. See
     *      {@link HolidayAndWorkingDaysAndMoratoriaScheduledDateGeneration} for scheduling rules.</li>
     * <li> One-time upfront fees are attached to the first meeting.</li>
     * <li> Periodic fees are attached to the first meeting and subsequent meetings that match the fee's frequency
     *      and recurrence</li>
     * <li> The <code>lastAppliedDate</code> for each fee is set to the date of the latest meeting to which the fee
     *      is attached
     * </ul>
     */
    public void createSchedulesAndFeeSchedulesForFirstTimeActiveCustomer(CustomerBO customer, List<AccountFeesEntity> accountFees, MeetingBO customerMeeting, CalendarEvent applicableCalendarEvents, DateTime scheduleGenerationStartingFrom) {

        final ScheduledEvent customerMeetingEvent = new RecurringScheduledEventFactoryImpl().createScheduledEventFrom(customerMeeting);
        DateTime beginningFrom = scheduleGenerationStartingFrom;
        // synch up generated schedule for center/group/client or group/client hierarchy
        CustomerBO upmostParent = upmostParentOf(customer);
        if (upmostParent != null) {
            LocalDate parentCustomerActiviationDate = new LocalDate(upmostParent.getCustomerActivationDate());
            LocalDate childCustomerActiviationDate = new LocalDate(customer.getCustomerActivationDate());

            LocalDate validCustomerMeetingMatch = null;
            if (customerMeetingEvent instanceof DailyScheduledEvent) {
                validCustomerMeetingMatch = new LocalDate(parentCustomerActiviationDate.toDateMidnight().toDateTime());
            } else {
                validCustomerMeetingMatch = new LocalDate(customerMeetingEvent.nearestMatchNotTakingIntoAccountScheduleFrequency(
                        parentCustomerActiviationDate.toDateMidnight().toDateTime()));
            }

            while (childCustomerActiviationDate.isAfter(validCustomerMeetingMatch)) {
                validCustomerMeetingMatch = new LocalDate(customerMeetingEvent.rollFrowardDateByFrequency(validCustomerMeetingMatch.toDateMidnight().toDateTime()));
            }
            
            beginningFrom = validCustomerMeetingMatch.toDateMidnight().toDateTime();
        }
        
        DateTime meetingStartDate = new DateTime(customer.getCustomerMeetingValue().getMeetingStartDate());
        if (beginningFrom.isBefore(meetingStartDate)) {
            beginningFrom = meetingStartDate;
        }
        
        createInitialSetOfCustomerScheduleEntities(customer, beginningFrom, applicableCalendarEvents, customerMeetingEvent);

        applyFeesToInitialSetOfInstallments(new ArrayList<AccountFeesEntity>(accountFees), customerMeetingEvent);
    }

    private CustomerBO upmostParentOf(CustomerBO customer) {
        CustomerBO firstParent = customer.getParentCustomer();
        CustomerBO upmostParent = firstParent;
        if (firstParent != null) {
            CustomerBO grandParent = firstParent.getParentCustomer();
            if (grandParent != null) {
                upmostParent = grandParent;
            }
        }
        return upmostParent;
    }

    private void createInitialSetOfCustomerScheduleEntities (CustomerBO customer, DateTime meetingStartDate, CalendarEvent calendarEvents, final ScheduledEvent scheduledEvent) {

        List<InstallmentDate> withHolidayInstallmentDates = this.generateInitialInstallmentDates(meetingStartDate, calendarEvents, scheduledEvent);

        for (InstallmentDate installmentDate : withHolidayInstallmentDates) {
            this.addAccountActionDate(new CustomerScheduleEntity(
                                            this,
                                            customer,
                                            installmentDate.getInstallmentId(),
                                            new java.sql.Date(installmentDate.getInstallmentDueDate().getTime()),
                                            PaymentStatus.UNPAID));
        }

    }

    private void applyFeesToInitialSetOfInstallments (List<AccountFeesEntity> accountFees, final ScheduledEvent scheduledEvent) {

        List<FeeInstallment> mergedFeeInstallments = FeeInstallment.createMergedFeeInstallments(scheduledEvent, accountFees, numberOfMeetingDatesToGenerate);
        for (AccountActionDateEntity accountAction : this.getAccountActionDates()) {
            this.applyFeesToScheduledEvent((CustomerScheduleEntity) accountAction, mergedFeeInstallments);
        }
        this.setLastAppliedDatesForFees(accountFees);
   }

    private List<InstallmentDate> generateInitialInstallmentDates(DateTime startingFrom, CalendarEvent calendarEvents, ScheduledEvent meetingEvent) {

        ScheduledDateGeneration dateGeneration = new HolidayAndWorkingDaysAndMoratoriaScheduledDateGeneration(calendarEvents.getWorkingDays(), calendarEvents.getHolidays());
        List<DateTime> meetingDates = dateGeneration.generateScheduledDates(numberOfMeetingDatesToGenerate, startingFrom, meetingEvent, true);
        return InstallmentDate.createInstallmentDates(meetingDates);
    }

    private void applyFeesToScheduledEvent(CustomerScheduleEntity customerScheduleEntity,
            List<FeeInstallment> mergedFeeInstallments) {

        for (FeeInstallment feeInstallment : mergedFeeInstallments) {
            if (feeInstallment.getInstallmentId().equals(customerScheduleEntity.getInstallmentId())) {
                CustomerFeeScheduleEntity customerFeeScheduleEntity = new CustomerFeeScheduleEntity(
                        customerScheduleEntity, feeInstallment.getAccountFeesEntity().getFees(), feeInstallment
                                .getAccountFeesEntity(), feeInstallment.getAccountFee());
                customerScheduleEntity.addAccountFeesAction(customerFeeScheduleEntity);
            }
        }
    }

    private void setLastAppliedDatesForFees(List<AccountFeesEntity> accountFees) {
        for (AccountFeesEntity accountFeeEntity : accountFees) {
            accountFeeEntity.setLastAppliedDate(this.getLatestAppliedDateForFee (accountFeeEntity));
        }
    }

    /**
     * Return the latest action date for which
     * the given fee was applied, if any, otherwise return null.
     *
     * @param accountFeeEntity the given fee we're searching for among all action dates
     * @return null if the fee has not been applied, otherwise the latest date to which the fee installment was applied.
     */
    private Date getLatestAppliedDateForFee (AccountFeesEntity accountFeeEntity) {
        Date latestAppliedDate = null;
        for (AccountActionDateEntity event : this.getAccountActionDates()) {
            CustomerScheduleEntity customerEvent = (CustomerScheduleEntity) event;
            for (AccountFeesActionDetailEntity feeActionDetail : customerEvent.getAccountFeesActionDetails()) {
                if (feeActionDetail.getAccountFee().equals(accountFeeEntity)) {
                    Date actionDate = customerEvent.getActionDate();
                    if ((latestAppliedDate == null) || (actionDate.compareTo(latestAppliedDate) > 0)) {
                        latestAppliedDate = actionDate;
                    }
                }
            }
        }
        return latestAppliedDate;
    }

    /**
     * default constructor for hibernate usage
     */
    protected CustomerAccountBO() {
        super();
    }

    private CustomerAccountBO(CustomerBO customer, List<AccountFeesEntity> accountFees) throws AccountException {
        super(customer.getUserContext(), customer, AccountTypes.CUSTOMER_ACCOUNT, AccountState.CUSTOMER_ACCOUNT_ACTIVE);
        for (AccountFeesEntity accountFee : accountFees) {
            accountFee.setAccount(this);
            this.addAccountFees(accountFee);
        }
    }

    /**
     * @deprecated - use static factory methods for creating {@link CustomerAccountBO}.
     */
    @Deprecated
    public CustomerAccountBO(final UserContext userContext, final CustomerBO customer, final List<FeeDto> fees)
            throws AccountException {
        super(userContext, customer, AccountTypes.CUSTOMER_ACCOUNT, AccountState.CUSTOMER_ACCOUNT_ACTIVE);
        if (fees != null) {
            for (FeeDto feeDto : fees) {
                FeeBO fee = getFeeDao().findById(feeDto.getFeeIdValue());

                this.addAccountFees(new AccountFeesEntity(this, fee, new LocalizationConverter()
                        .getDoubleValueForCurrentLocale(feeDto.getAmount())));

            }
            generateCustomerFeeSchedule(customer);
        }
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CUSTOMER_ACCOUNT;
    }

    public List<CustomerActivityEntity> getCustomerActivitDetails() {
        return customerActivitDetails;
    }

    @SuppressWarnings("unused")
    // see .hbm.xml file
    private void setCustomerActivitDetails(final List<CustomerActivityEntity> customerActivitDetails) {
        this.customerActivitDetails = customerActivitDetails;
    }

    public void addCustomerActivity(final CustomerActivityEntity customerActivityEntity) {
        customerActivitDetails.add(customerActivityEntity);
    }

    private BigDecimal dueAmountForCustomerSchedule(CustomerScheduleEntity customerSchedule) {
         BigDecimal totalAllUnpaidInstallments = customerSchedule.getMiscFeeDue().getAmount();
         totalAllUnpaidInstallments = totalAllUnpaidInstallments.add(customerSchedule.getMiscPenaltyDue().getAmount());
         for (AccountFeesActionDetailEntity accountFeesActionDetail : customerSchedule.getAccountFeesActionDetails()) {
             CustomerFeeScheduleEntity customerFeeSchedule = (CustomerFeeScheduleEntity) accountFeesActionDetail;
             totalAllUnpaidInstallments = totalAllUnpaidInstallments.add(customerFeeSchedule.getFeeDue().getAmount());
         }
         return totalAllUnpaidInstallments;
    }

    @Override
    protected AccountPaymentEntity makePayment(final PaymentData paymentData) throws AccountException {

        Money totalPaid = paymentData.getTotalAmount();

        if (totalPaid.isZero()) {
            throw new AccountException("errors.update",
                    new String[] { "Attempting to pay a customer account balance of zero for customer: "
                            + paymentData.getCustomer().getGlobalCustNum() });
        }

        final List<CustomerScheduleEntity> customerAccountPayments = findAllUnpaidInstallmentsUpToDatePlusNextMeeting(paymentData
                .getTransactionDate());

        if (customerAccountPayments.isEmpty()) {
            throw new AccountException(AccountConstants.NO_TRANSACTION_POSSIBLE, new String[] {"Trying to pay account charges before the due date."});
        }

        Money totalAllUnpaidInstallments = new Money(totalPaid.getCurrency(), "0.0");
        for (CustomerScheduleEntity customerSchedule : customerAccountPayments) {
           totalAllUnpaidInstallments = totalAllUnpaidInstallments.add(new Money(totalPaid.getCurrency(), dueAmountForCustomerSchedule(customerSchedule)));
        }

        if (totalAllUnpaidInstallments.compareTo(totalPaid) < 0) {
            throw new AccountException(AccountConstants.NO_TRANSACTION_POSSIBLE, new String[] {"Overpayments are not supported"});
        }

        final AccountPaymentEntity accountPayment = new AccountPaymentEntity(this, paymentData.getTotalAmount(),
                paymentData.getReceiptNum(), paymentData.getReceiptDate(), getPaymentTypeEntity(paymentData
                        .getPaymentTypeId()), paymentData.getTransactionDate());

        BigDecimal leftFromPaidIn = totalPaid.getAmount();
        for (CustomerScheduleEntity customerSchedule : customerAccountPayments) {
            if (leftFromPaidIn.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }

            final List<FeesTrxnDetailEntity> feeTrxns = new ArrayList<FeesTrxnDetailEntity>();
            for (AccountFeesActionDetailEntity accountFeesActionDetail : customerSchedule.getAccountFeesActionDetails()) {
                if (leftFromPaidIn.compareTo(BigDecimal.ZERO) > 0) {
                    CustomerFeeScheduleEntity customerFeeSchedule = (CustomerFeeScheduleEntity) accountFeesActionDetail;
                    BigDecimal feeFromScheduleToPay = leftFromPaidIn.min(customerFeeSchedule.getFeeDue().getAmount());
                    customerFeeSchedule.makePayment(new Money(totalPaid.getCurrency(), feeFromScheduleToPay));
                    final FeesTrxnDetailEntity feesTrxnDetailBO = new FeesTrxnDetailEntity(null, customerFeeSchedule
                            .getAccountFee(), new Money(totalPaid.getCurrency(), feeFromScheduleToPay));
                    feeTrxns.add(feesTrxnDetailBO);
                    leftFromPaidIn = leftFromPaidIn.subtract(feeFromScheduleToPay);
                }
            }

            BigDecimal miscPenaltyToPay = leftFromPaidIn.min(customerSchedule.getMiscPenaltyDue().getAmount());
            if (miscPenaltyToPay.compareTo(BigDecimal.ZERO) > 0) {
                customerSchedule.payMiscPenalty(new Money(totalPaid.getCurrency(), miscPenaltyToPay));
                customerSchedule.setPaymentDate(new java.sql.Date(paymentData.getTransactionDate().getTime()));
                leftFromPaidIn = leftFromPaidIn.subtract(miscPenaltyToPay);
            }

            BigDecimal miscFeeToPay = BigDecimal.ZERO;
            if (leftFromPaidIn.compareTo(BigDecimal.ZERO) > 0) {
                miscFeeToPay = leftFromPaidIn.min(customerSchedule.getMiscFeeDue().getAmount());
                if (miscFeeToPay.compareTo(BigDecimal.ZERO) > 0) {
                    customerSchedule.payMiscFee(new Money(totalPaid.getCurrency(), miscFeeToPay));
                    customerSchedule.setPaymentDate(new java.sql.Date(paymentData.getTransactionDate().getTime()));
                    leftFromPaidIn = leftFromPaidIn.subtract(miscFeeToPay);
                }
            }

            if (dueAmountForCustomerSchedule(customerSchedule).compareTo(BigDecimal.ZERO) == 0) {
                customerSchedule.setPaymentStatus(PaymentStatus.PAID);
            }

            Money customerScheduleAmountPaid = new Money(totalPaid.getCurrency(), miscFeeToPay.add(miscPenaltyToPay));

            final CustomerTrxnDetailEntity accountTrxn = new CustomerTrxnDetailEntity(accountPayment,
                    AccountActionTypes.CUSTOMER_ACCOUNT_REPAYMENT, customerSchedule.getInstallmentId(),
                    customerSchedule.getActionDate(), paymentData.getPersonnel(), paymentData.getTransactionDate(),
                    customerScheduleAmountPaid, AccountConstants.PAYMENT_RCVD, null, new Money(totalPaid.getCurrency(), miscFeeToPay),
                    new Money(totalPaid.getCurrency(), miscPenaltyToPay));

            for (FeesTrxnDetailEntity feesTrxnDetailEntity : feeTrxns) {
                accountTrxn.addFeesTrxnDetail(feesTrxnDetailEntity);
                feesTrxnDetailEntity.setAccountTrxn(accountTrxn);
            }

            accountPayment.addAccountTrxn(accountTrxn);
        }

        addCustomerActivity(new CustomerActivityEntity(this, paymentData.getPersonnel(), paymentData.getTotalAmount(),
                AccountConstants.PAYMENT_RCVD, paymentData.getTransactionDate()));

        return accountPayment;
    }

    @Override
    public boolean isAdjustPossibleOnLastTrxn() {
        if (!getCustomer().isActive()) {
            logger.debug(
                    "State is not active hence adjustment is not possible");
            return false;
        }
        logger.debug(
                "Total payments on this account is  " + getAccountPayments().size());
        if (null == findMostRecentNonzeroPaymentByPaymentDate()) {
            return false;
        }

        logger.debug("Adjustment is possible");
        return true;
    }

    @Override
    protected void updateInstallmentAfterAdjustment(final List<AccountTrxnEntity> reversedTrxns, PersonnelBO loggedInUser)
            throws AccountException {
        if (null != reversedTrxns && reversedTrxns.size() > 0) {
            Money totalAmountAdj = new Money(getCurrency());
            for (AccountTrxnEntity accntTrxn : reversedTrxns) {
                CustomerTrxnDetailEntity custTrxn = (CustomerTrxnDetailEntity) accntTrxn;
                CustomerScheduleEntity accntActionDate = (CustomerScheduleEntity) getAccountActionDate(custTrxn
                        .getInstallmentId());
                accntActionDate.setPaymentStatus(PaymentStatus.UNPAID);
                accntActionDate.setPaymentDate(null);
                accntActionDate.setMiscFeePaid(accntActionDate.getMiscFeePaid().add(custTrxn.getMiscFeeAmount()));
                totalAmountAdj = totalAmountAdj.add(removeSign(custTrxn.getMiscFeeAmount()));
                accntActionDate.setMiscPenaltyPaid(accntActionDate.getMiscPenaltyPaid().add(
                        custTrxn.getMiscPenaltyAmount()));
                totalAmountAdj = totalAmountAdj.add(removeSign(custTrxn.getMiscPenaltyAmount()));
                if (null != accntActionDate.getAccountFeesActionDetails()
                        && accntActionDate.getAccountFeesActionDetails().size() > 0) {
                    for (AccountFeesActionDetailEntity accntFeesAction : accntActionDate.getAccountFeesActionDetails()) {
                        Money feeAmntAdjusted = custTrxn.getFeesTrxn(accntFeesAction.getAccountFee().getAccountFeeId())
                                .getFeeAmount();
                        ((CustomerFeeScheduleEntity) accntFeesAction).setFeeAmountPaid(accntFeesAction
                                .getFeeAmountPaid().add(feeAmntAdjusted));
                        totalAmountAdj = totalAmountAdj.add(removeSign(feeAmntAdjusted));
                    }
                }
            }
            addCustomerActivity(buildCustomerActivity(totalAmountAdj, AccountConstants.AMNT_ADJUSTED, userContext
                    .getId()));
        }
    }

    public void waiveAmountDue() throws AccountException {
        AccountActionDateEntity accountActionDateEntity = getUpcomingInstallment();
        Money chargeWaived = ((CustomerScheduleEntity) accountActionDateEntity).waiveCharges();
        if (chargeWaived != null && chargeWaived.isGreaterThanZero()) {
            addCustomerActivity(buildCustomerActivity(chargeWaived, AccountConstants.AMNT_WAIVED, userContext.getId()));
        }
    }

    @Override
    public void waiveAmountOverDue(@SuppressWarnings("unused") final WaiveEnum chargeType) throws AccountException {
        Money chargeWaived = new Money(getCurrency());
        List<AccountActionDateEntity> accountActionDateList = getApplicableIdsForNextInstallmentAndArrears();
        accountActionDateList.remove(accountActionDateList.size() - 1);
        for (AccountActionDateEntity accountActionDateEntity : accountActionDateList) {
            chargeWaived = chargeWaived.add(((CustomerScheduleEntity) accountActionDateEntity).waiveCharges());
        }
        if (chargeWaived != null && chargeWaived.isGreaterThanZero()) {
            addCustomerActivity(buildCustomerActivity(chargeWaived, AccountConstants.AMNT_WAIVED, userContext.getId()));
        }
    }

    public void applyPeriodicFeesToNewSchedule () {

        for (AccountFeesEntity accountFee : getPeriodicFeeList()) {
            applyOnePeriodicFeeToInstallments(accountFee, getAccountActionDatesSortedByInstallmentId());
        }
    }

    public void applyPeriodicFeesToNextSetOfMeetingDates () {

        for (AccountFeesEntity accountFee : getPeriodicFeeList()) {
            applyOnePeriodicFeeToInstallments(accountFee, getInstallmentsAfterLatestInstallmentThatFeeWasAppliedTo (accountFee));
        }

    }

    private void applyOnePeriodicFeeToInstallments(AccountFeesEntity accountFee, List<AccountActionDateEntity> actionDateEntities) {
        if (actionDateEntities.size() > 0) {
            ScheduledEvent scheduledEvent = ScheduledEventFactory.createScheduledEventFrom(this.getMeetingForAccount());
            List<FeeInstallment> feeInstallmentList
                = FeeInstallment
                    .createMergedFeeInstallmentsForOneFee(scheduledEvent, accountFee, getAccountActionDates().size());
            applyFeeToInstallments(feeInstallmentList, actionDateEntities);
        }

    }

    private List<AccountActionDateEntity> getInstallmentsAfterLatestInstallmentThatFeeWasAppliedTo (AccountFeesEntity accountFee) {
        List<AccountActionDateEntity> installmentsToApply = new ArrayList<AccountActionDateEntity>();
        List<AccountActionDateEntity> allInstallments = getAccountActionDatesSortedByInstallmentId();
        for (int installmentId = getLatestInstallmentFeeIsAppliedTo(accountFee) + 1; installmentId <= allInstallments.size(); installmentId++) {
            installmentsToApply.add(getAccountActionDate((short) installmentId));
        }
        return installmentsToApply;
    }

    short getLatestInstallmentFeeIsAppliedTo (AccountFeesEntity accountFee) {
        List<AccountActionDateEntity> allInstallments = getAccountActionDatesSortedByInstallmentId();
        for (int installmentId = allInstallments.size(); installmentId >=1; installmentId--) {
           CustomerScheduleEntity scheduleEntity = (CustomerScheduleEntity) getAccountActionDate((short) installmentId);
           if (feeIsAppliedTo(scheduleEntity, accountFee)) {
               return (short) installmentId;
           }
        }
        throw new MifosRuntimeException("Fee is attached to this customer but has never been applied to a scheduled event");
    }

    boolean feeIsAppliedTo(CustomerScheduleEntity scheduleEntity, AccountFeesEntity accountFee) {
        for (AccountFeesActionDetailEntity feeActionDetail : scheduleEntity.getAccountFeesActionDetails()) {
            if (feeActionDetail.getAccountFee().getAccountFeeId().equals(accountFee.getAccountFeeId())) {
//            if (feeActionDetail.getAccountFee().equals(accountFee)) {
                return true;
            }
        }
        return false;
    }

    private CustomerActivityEntity buildCustomerActivity(final Money amount, final String description,
            final Short personnelId) throws AccountException {
        try {
            PersonnelBO personnel = null;
            if (personnelId != null) {
                personnel = ApplicationContextProvider.getBean(LegacyPersonnelDao.class).getPersonnel(personnelId);
            }
            return new CustomerActivityEntity(this, personnel, amount, description, new DateTimeService()
                    .getCurrentJavaDateTime());
        } catch (PersistenceException e) {
            throw new AccountException(e);
        }
    }

    @Override
    public void updateAccountActivity(@SuppressWarnings("unused") final Money principal,
            @SuppressWarnings("unused") final Money interest, final Money fee,
            @SuppressWarnings("unused") final Money penalty, final Short personnelId, final String description)
            throws AccountException {
        addCustomerActivity(buildCustomerActivity(fee, description, personnelId));
    }

    @Override
    public final void removeFeesAssociatedWithUpcomingAndAllKnownFutureInstallments(final Short feeId, final Short personnelId) throws AccountException {
        List<Short> installmentIds = getApplicableInstallmentIdsForRemoveFees();
        if (installmentIds != null && installmentIds.size() != 0 && isFeeActive(feeId)) {
            updateAccountActionDateEntity(installmentIds, feeId);
        }
        FeeBO feesBO = getAccountFeesObject(feeId);
        updateAccountFeesEntity(feeId);
        String description = feesBO.getFeeName() + " " + AccountConstants.FEES_REMOVED;
        updateAccountActivity(null, null, null, null, personnelId, description);
    }

    @Override
    protected Money getDueAmount(final AccountActionDateEntity installment) {
        return ((CustomerScheduleEntity) installment).getTotalDueWithFees();
    }

    @Override
    protected void regenerateFutureInstallments(final AccountActionDateEntity nextInstallment, final List<Days> workingDays, final List<Holiday> holidays) throws AccountException {

            int numberOfInstallmentsToGenerate = getLastInstallmentId();

            MeetingBO meeting = getMeetingForAccount();
            ScheduledEvent scheduledEvent = ScheduledEventFactory.createScheduledEventFrom(meeting);
            LocalDate currentDate = new LocalDate();
            LocalDate thisIntervalStartDate = meeting.startDateForMeetingInterval(currentDate);
            LocalDate nextMatchingDate = new LocalDate(scheduledEvent.nextEventDateAfter(thisIntervalStartDate.toDateTimeAtStartOfDay()));
            DateTime futureIntervalStartDate = meeting.startDateForMeetingInterval(nextMatchingDate).toDateTimeAtStartOfDay();

            ScheduledDateGeneration dateGeneration = new HolidayAndWorkingDaysAndMoratoriaScheduledDateGeneration(workingDays, holidays);

            List<DateTime> meetingDates = dateGeneration.generateScheduledDates(numberOfInstallmentsToGenerate, futureIntervalStartDate, scheduledEvent, true);

            updateSchedule(nextInstallment.getInstallmentId(), meetingDates);
    }

    private List<CustomerScheduleEntity> findAllUnpaidInstallmentsUpToDatePlusNextMeeting(final Date transactionDate) {

        List<AccountActionDateEntity> unpaidDates = new ArrayList<AccountActionDateEntity>();
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (accountActionDateEntity != null && !accountActionDateEntity.isPaid()) {
                unpaidDates.add(accountActionDateEntity);
            }
        }

        final List<CustomerScheduleEntity> customerSchedulePayments = new ArrayList<CustomerScheduleEntity>();

        for (AccountActionDateEntity accountActionDateEntity : unpaidDates) {
            if (!accountActionDateEntity.getActionDate().after(transactionDate)) {
                customerSchedulePayments.add((CustomerScheduleEntity) accountActionDateEntity);
            }
        }

        AccountActionDateEntity nextMeeting = null;
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (accountActionDateEntity != null && accountActionDateEntity.getActionDate().after(transactionDate)) {
                if (nextMeeting == null || nextMeeting.getActionDate().after(accountActionDateEntity.getActionDate())) {
                    nextMeeting = accountActionDateEntity;
                }
            }
        }

        if (nextMeeting != null && !nextMeeting.isPaid()) {
            customerSchedulePayments.add((CustomerScheduleEntity)nextMeeting);
        }

        Collections.sort(customerSchedulePayments, new Comparator<CustomerScheduleEntity> () {
            @Override
            public int compare(CustomerScheduleEntity o1, CustomerScheduleEntity o2) {
                return o1.getActionDate().compareTo(o2.getActionDate());
            }
        });

        return customerSchedulePayments;
    }

    @Override
    public Money getTotalAmountInArrears() {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("ACTION_DATE", DateUtils.getCurrentDateWithoutTimeStamp());
        queryParameters.put("CUSTOMER_ID", getAccountId());
        Query query = StaticHibernateUtil.getSessionTL().getNamedQuery(NamedQueryConstants.RETRIEVE_TOTAL_AMOUNT_IN_ARREARS);
        query.setProperties(queryParameters);

        BigDecimal total = (BigDecimal) query.uniqueResult();

        if (total == null) {
          total = new BigDecimal(0);
        }

        return new Money(getCurrency(), total);
    }

    @Override
     public Money getTotalPaymentDue() {
        Money totalAmt = getTotalAmountInArrears();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment != null && !nextInstallment.isPaid()) {
            totalAmt = totalAmt.add(getDueAmount(nextInstallment));
        }
        return totalAmt;
    }

    public void generateNextSetOfMeetingDates(ScheduledDateGeneration scheduleGenerationStrategy) {

        Short lastInstallmentId = Short.valueOf("0");
        if (getLastInstallmentId() != null) {
            lastInstallmentId = getLastInstallmentId();
        }

        AccountActionDateEntity lastInstallment = getAccountActionDate(lastInstallmentId);
        MeetingBO meeting = getCustomer().getCustomerMeetingValue();

        ScheduledEvent scheduledEvent = ScheduledEventFactory.createScheduledEventFrom(meeting);
        Date lastInstallmentDate = new Date();
        if (lastInstallment != null) {
            lastInstallmentDate = lastInstallment.getActionDate();
        }

        /*
         * Generate more scheduled dates starting with the date of the last generated installment.
         * This ensures that the customer's meeting recurrence is taken into account. But then
         * skip the first date when adding account actions because it's already there.
         */
        DateTime dateOfLastInstallment = new DateTime(lastInstallmentDate).toDateMidnight().toDateTime();
        List<DateTime> scheduledDates = scheduleGenerationStrategy.generateScheduledDates(numberOfMeetingDatesToGenerate + 1, dateOfLastInstallment, scheduledEvent, true);

        int count = 1;
        for (DateTime installmentDate : allButFirst(scheduledDates)) {
            CustomerScheduleEntity customerScheduleEntity = new CustomerScheduleEntity(this, getCustomer(), Short
                    .valueOf(String.valueOf(count + lastInstallmentId)),
                                            new java.sql.Date(installmentDate.toDate().getTime()),
                                            PaymentStatus.UNPAID);
            count++;
            addAccountActionDate(customerScheduleEntity);
        }

        applyPeriodicFeesToNextSetOfMeetingDates();
    }

    private List<DateTime> allButFirst(List<DateTime> scheduledDates) {
        List<DateTime> scheduledDatesButFirst = new ArrayList<DateTime>();
        for (int i = 1; i < scheduledDates.size(); i++) {
            scheduledDatesButFirst.add(scheduledDates.get(i));
        }
        return scheduledDatesButFirst;
    }

    @Override
    public Money updateAccountActionDateEntity(final List<Short> intallmentIdList, final Short feeId) {
        Money totalFeeAmount = new Money(getCurrency());
        Set<AccountActionDateEntity> accountActionDateEntitySet = this.getAccountActionDates();
        for (AccountActionDateEntity accountActionDateEntity : accountActionDateEntitySet) {
            if (intallmentIdList.contains(accountActionDateEntity.getInstallmentId())) {
                totalFeeAmount = totalFeeAmount.add(((CustomerScheduleEntity) accountActionDateEntity)
                        .removeFees(feeId));
            }
        }
        return totalFeeAmount;
    }

    @Override
    public void applyCharge(final Short feeId, final Double charge) throws AccountException {
        if (!isCustomerValid()) {
            if (feeId.equals(Short.valueOf(AccountConstants.MISC_FEES))
                    || feeId.equals(Short.valueOf(AccountConstants.MISC_PENALTY))) {
                throw new AccountException(AccountConstants.MISC_CHARGE_NOT_APPLICABLE);
            }
            addFeeToAccountFee(feeId, charge);
            FeeBO fee = getFeeDao().findById(feeId);
            updateCustomerActivity(feeId,
                    new Money(((AmountFeeBO) fee).getFeeAmount().getCurrency(), charge.toString()), fee.getFeeName()
                            + AccountConstants.APPLIED);
        } else {
            Money chargeAmount = new Money(getCurrency(), String.valueOf(charge));
            List<AccountActionDateEntity> dueInstallments = null;
            if (feeId.equals(Short.valueOf(AccountConstants.MISC_FEES))
                    || feeId.equals(Short.valueOf(AccountConstants.MISC_PENALTY))) {
                dueInstallments = getTotalDueInstallments();
                if (dueInstallments.isEmpty()) {
                    throw new AccountException(AccountConstants.NOMOREINSTALLMENTS);
                }
                applyMiscCharge(feeId, chargeAmount, dueInstallments.get(0));
            } else {
                dueInstallments = getTotalDueInstallments();
                if (dueInstallments.isEmpty()) {
                    throw new AccountException(AccountConstants.NOMOREINSTALLMENTS);
                }
                FeeBO fee = getFeeDao().findById(feeId);
                if (fee.getFeeFrequency().getFeePayment() != null) {
                    applyOneTimeFee(fee, chargeAmount);
                } else {
                    applyPeriodicFee(fee, chargeAmount);
                }
            }
        }
    }

    void applyOneTimeFee(final FeeBO fee, final Money chargeAmount) throws AccountException {
        applyOneTimeFee (fee, chargeAmount, getTotalDueInstallments().get(0));
    }

    void applyPeriodicFee (final FeeBO fee, Money chargeAmount) throws AccountException {
        applyPeriodicFee (fee, chargeAmount, getTotalDueInstallments());
    }

    public Date getUpcomingChargesDate() {
        AccountActionDateEntity nextAccountAction = getNextUnpaidDueInstallment();
        return nextAccountAction != null ? nextAccountAction.getActionDate() : new DateTimeService()
                .getCurrentJavaSqlDate();
    }

    @Override
    public Money getTotalAmountDue() {
        Money totalAmt = getTotalAmountInArrears();
        List<AccountActionDateEntity> dueActionDateList = getTotalDueInstallments();
        if (dueActionDateList.size() > 0) {
            AccountActionDateEntity nextInstallment = dueActionDateList.get(0);
            totalAmt = totalAmt.add(getDueAmount(nextInstallment));
        }
        return totalAmt;
    }

    public AccountActionDateEntity getUpcomingInstallment() {
        List<AccountActionDateEntity> dueActionDateList = getTotalDueInstallments();
        if (dueActionDateList.size() > 0) {
            return dueActionDateList.get(0);
        }
        return null;
    }

    private void addFeeToAccountFee(final Short feeId, final Double charge) {
        FeeBO fee = getFeeDao().findById(feeId);
        AccountFeesEntity accountFee = null;
        if (fee.isPeriodic() && !isFeeAlreadyApplied(fee) || !fee.isPeriodic()) {
            accountFee = new AccountFeesEntity(this, fee, charge, FeeStatus.ACTIVE.getValue(), new DateTimeService()
                    .getCurrentJavaDateTime(), null);
            addAccountFees(accountFee);
        } else {
            accountFee = getAccountFees(fee.getFeeId());
            accountFee.setFeeAmount(charge);
            accountFee.setFeeStatus(FeeStatus.ACTIVE);
            accountFee.setStatusChangeDate(new DateTimeService().getCurrentJavaDateTime());
        }
    }

    /*
     * Package-level visibility for testing
     */
    void applyPeriodicFee(final FeeBO fee, final Money charge,
            final List<AccountActionDateEntity> dueInstallments) throws AccountException {
        AccountFeesEntity accountFee = getAccountFee(fee, charge.getAmountDoubleValue());
        accountFee.setAccountFeeAmount(charge);
        List<InstallmentDate> installmentDates = new ArrayList<InstallmentDate>();
        for (AccountActionDateEntity accountActionDateEntity : dueInstallments) {
            installmentDates.add(new InstallmentDate(accountActionDateEntity.getInstallmentId(),
                    accountActionDateEntity.getActionDate()));
        }
//        List<FeeInstallment> feeInstallmentList = mergeFeeInstallments(handlePeriodic(accountFee, installmentDates));
        ScheduledEvent loanScheduledEvent = ScheduledEventFactory.createScheduledEventFrom(this.getMeetingForAccount());
        List<FeeInstallment> feeInstallmentList
            = FeeInstallment.createMergedFeeInstallmentsForOneFeeStartingWith(loanScheduledEvent,
                                                                              accountFee,
                                                                              dueInstallments.size(),
                                                                              dueInstallments.get(0).getInstallmentId());
        // MIFOS-3701: we want to display only fee charge, not the totalFeeAmountApplied
        applyFeeToInstallments(feeInstallmentList, dueInstallments);
        updateCustomerActivity(fee.getFeeId(), charge, fee.getFeeName() + AccountConstants.APPLIED);
        accountFee.setFeeStatus(FeeStatus.ACTIVE);
    }

    private void applyOneTimeFee(final FeeBO fee, final Money charge,
            final AccountActionDateEntity accountActionDateEntity) throws AccountException {
        CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
        AccountFeesEntity accountFee = new AccountFeesEntity(this, fee, charge.getAmountDoubleValue(), FeeStatus.ACTIVE
                .getValue(), new DateTimeService().getCurrentJavaDateTime(), null);
        List<AccountActionDateEntity> customerScheduleList = new ArrayList<AccountActionDateEntity>();
        customerScheduleList.add(customerScheduleEntity);
        List<InstallmentDate> installmentDates = new ArrayList<InstallmentDate>();
        installmentDates.add(new InstallmentDate(accountActionDateEntity.getInstallmentId(), accountActionDateEntity
                .getActionDate()));
        List<FeeInstallment> feeInstallmentList = new ArrayList<FeeInstallment>();
        feeInstallmentList.add(handleOneTime(accountFee, installmentDates));
        Money totalFeeAmountApplied = applyFeeToInstallments(feeInstallmentList, customerScheduleList);
        updateCustomerActivity(fee.getFeeId(), totalFeeAmountApplied, fee.getFeeName() + AccountConstants.APPLIED);
        accountFee.setFeeStatus(FeeStatus.ACTIVE);
    }

    private void applyMiscCharge(final Short chargeType, final Money charge,
            final AccountActionDateEntity accountActionDateEntity) throws AccountException {
        CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
        customerScheduleEntity.applyMiscCharge(chargeType, charge);
        updateCustomerActivity(chargeType, charge, "");
    }

    private void updateCustomerActivity(final Short chargeType, final Money charge, final String comments)
            throws AccountException {
        try {
            PersonnelBO personnel = ApplicationContextProvider.getBean(LegacyPersonnelDao.class).getPersonnel(getUserContext().getId());
            CustomerActivityEntity customerActivityEntity = null;
            if (chargeType != null && chargeType.equals(Short.valueOf(AccountConstants.MISC_PENALTY))) {
                customerActivityEntity = new CustomerActivityEntity(this, personnel, charge,
                        AccountConstants.MISC_PENALTY_APPLIED, new DateTimeService().getCurrentJavaDateTime());
            } else if (chargeType != null && chargeType.equals(Short.valueOf(AccountConstants.MISC_FEES))) {
                customerActivityEntity = new CustomerActivityEntity(this, personnel, charge,
                        AccountConstants.MISC_FEES_APPLIED, new DateTimeService().getCurrentJavaDateTime());
            } else {
                customerActivityEntity = new CustomerActivityEntity(this, personnel, charge, comments,
                        new DateTimeService().getCurrentJavaDateTime());
            }
            addCustomerActivity(customerActivityEntity);
        } catch (PersistenceException e) {
            throw new AccountException(e);
        }
    }

    private Money applyFeeToInstallments(final List<FeeInstallment> feeInstallmentList,
            final List<AccountActionDateEntity> accountActionDateList) {
        Date lastAppliedDate = null;
        Money totalFeeAmountApplied = new Money(getCurrency());
        AccountFeesEntity accountFeesEntity = null;
        for (AccountActionDateEntity accountActionDateEntity : accountActionDateList) {
            CustomerScheduleEntity customerScheduleEntity = (CustomerScheduleEntity) accountActionDateEntity;
            for (FeeInstallment feeInstallment : feeInstallmentList) {
                if (feeInstallment.getInstallmentId().equals(customerScheduleEntity.getInstallmentId())) {
                    lastAppliedDate = customerScheduleEntity.getActionDate();
                    totalFeeAmountApplied = totalFeeAmountApplied.add(feeInstallment.getAccountFee());
                    AccountFeesActionDetailEntity accountFeesActionDetailEntity = new CustomerFeeScheduleEntity(
                            customerScheduleEntity, feeInstallment.getAccountFeesEntity().getFees(), feeInstallment
                                    .getAccountFeesEntity(), feeInstallment.getAccountFee());
                    customerScheduleEntity.addAccountFeesAction(accountFeesActionDetailEntity);
                    accountFeesEntity = feeInstallment.getAccountFeesEntity();
                }
            }
        }
        if (accountFeesEntity != null) {
            accountFeesEntity.setLastAppliedDate(lastAppliedDate);
            addAccountFees(accountFeesEntity);
        }
        return totalFeeAmountApplied;
    }

    private boolean isCustomerValid() {
        if (getCustomer().getCustomerStatus().getId().equals(CustomerStatus.CENTER_ACTIVE.getValue())
                || getCustomer().getCustomerStatus().getId().equals(CustomerConstants.GROUP_ACTIVE_STATE)
                || getCustomer().getCustomerStatus().getId().equals(GroupConstants.HOLD)
                || getCustomer().getCustomerStatus().getId().equals(CustomerConstants.CLIENT_APPROVED)
                || getCustomer().getCustomerStatus().getId().equals(CustomerConstants.CLIENT_ONHOLD)) {
            return true;
        }
        return false;
    }

    private AccountActionDateEntity getNextUnpaidDueInstallment() {
        AccountActionDateEntity accountAction = null;
        for (AccountActionDateEntity accountActionDate : getAccountActionDates()) {
            if (!accountActionDate.isPaid()) {
                if (accountActionDate.compareDate(DateUtils.getCurrentDateWithoutTimeStamp()) >= 0) {
                    if (accountAction == null) {
                        accountAction = accountActionDate;
                    } else {
                        if (accountAction.getInstallmentId() > accountActionDate.getInstallmentId()) {
                            accountAction = accountActionDate;
                        }
                    }
                }
            }
        }
        return accountAction;
    }

    /*
     * This is currently (Jan 2010) used by jsp pages.
     */
    public Money getNextDueAmount() {
        AccountActionDateEntity accountAction = getNextUnpaidDueInstallment();
        if (accountAction != null) {
            return getDueAmount(accountAction);
        }

        return new Money(getCurrency(), "0.0");
    }

    public void generateCustomerAccountSystemId() throws CustomerException {
        try {
            if (getGlobalAccountNum() == null) {
                this.setGlobalAccountNum(generateId(userContext.getBranchGlobalNum()));
            } else {
                throw new CustomerException(AccountExceptionConstants.IDGenerationException);
            }
        } catch (AccountException e) {
            throw new CustomerException(e);
        }
    }

    @Override
    protected final List<FeeInstallment> handlePeriodic(final AccountFeesEntity accountFees,
            final List<InstallmentDate> installmentDates, final List<InstallmentDate> nonAdjustedInstallmentDates)
            throws AccountException {
        Money accountFeeAmount = accountFees.getAccountFeeAmount();
        MeetingBO feeMeetingFrequency = accountFees.getFees().getFeeFrequency().getFeeMeetingFrequency();
        List<Date> feeDates = getFeeDates(feeMeetingFrequency, nonAdjustedInstallmentDates);
        ListIterator<Date> feeDatesIterator = feeDates.listIterator();
        List<FeeInstallment> feeInstallmentList = new ArrayList<FeeInstallment>();
        while (feeDatesIterator.hasNext()) {
            Date feeDate = feeDatesIterator.next();
            logger.debug("Handling periodic fee.." + feeDate);

            Short installmentId = getMatchingInstallmentId(installmentDates, feeDate);
            feeInstallmentList.add(buildFeeInstallment(installmentId, accountFeeAmount, accountFees));

        }
        return feeInstallmentList;
    }

    /**
     * @deprecated - use static factory methods for creating {@link CustomerAccountBO} and inject in installment dates.
     */
    @Deprecated
    private void generateMeetingSchedule() throws AccountException {
        // generate dates that adjust for holidays
        List<InstallmentDate> installmentDates = getInstallmentDates(getCustomer().getCustomerMeeting().getMeeting(),
                (short) 10, (short) 0);
        // generate dates without adjusting for holidays
        List<InstallmentDate> nonAdjustedInstallmentDates = getInstallmentDates(getCustomer().getCustomerMeeting()
                .getMeeting(), (short) 10, (short) 0, false, true);
        logger.debug(
                "RepamentSchedular:getRepaymentSchedule , installment dates obtained ");
        List<FeeInstallment> feeInstallmentList = mergeFeeInstallments(getFeeInstallments(installmentDates,
                nonAdjustedInstallmentDates));
        logger.debug(
                "RepamentSchedular:getRepaymentSchedule , fee installment obtained ");
        for (InstallmentDate installmentDate : installmentDates) {
            CustomerScheduleEntity customerScheduleEntity = new CustomerScheduleEntity(this, getCustomer(),
                    installmentDate.getInstallmentId(), new java.sql.Date(installmentDate.getInstallmentDueDate()
                            .getTime()), PaymentStatus.UNPAID);
            addAccountActionDate(customerScheduleEntity);
            for (FeeInstallment feeInstallment : feeInstallmentList) {
                if (feeInstallment.getInstallmentId().equals(installmentDate.getInstallmentId())) {
                    CustomerFeeScheduleEntity customerFeeScheduleEntity = new CustomerFeeScheduleEntity(
                            customerScheduleEntity, feeInstallment.getAccountFeesEntity().getFees(), feeInstallment
                                    .getAccountFeesEntity(), feeInstallment.getAccountFee());
                    customerScheduleEntity.addAccountFeesAction(customerFeeScheduleEntity);
                }
            }
        }
        logger.debug(
                "RepamentSchedular:getRepaymentSchedule , repayment schedule generated  ");
    }

    public void updateFee(final AccountFeesEntity fee, final FeeBO feeBO) throws BatchJobException {
        boolean feeApplied = isFeeAlreadyApplied(fee, feeBO);
        if (!feeApplied) {
            // update this account fee
            try {
                if (feeBO.getFeeChangeType().equals(FeeChangeType.AMOUNT_AND_STATUS_UPDATED)) {
                    if (!feeBO.isActive()) {
                        removeFeesAssociatedWithUpcomingAndAllKnownFutureInstallments(feeBO.getFeeId(), Short.valueOf("1"));
                        fee.changeFeesStatus( FeeStatus.INACTIVE, new DateTimeService().getCurrentJavaDateTime());
                        updateAccountFee(fee, (AmountFeeBO)feeBO);
                    } else {
                        // generate repayment schedule and enable fee
                        fee.changeFeesStatus(FeeStatus.ACTIVE, new DateTimeService().getCurrentJavaDateTime());
                        updateAccountFee(fee, (AmountFeeBO)feeBO);
                        associateFeeWithAllKnownFutureInstallments(fee);
                    }

                } else if (feeBO.getFeeChangeType().equals(FeeChangeType.STATUS_UPDATED)) {
                    if (!feeBO.isActive()) {
                        removeFeesAssociatedWithUpcomingAndAllKnownFutureInstallments(feeBO.getFeeId(), Short.valueOf("1"));
                    } else {
                        fee.changeFeesStatus(FeeStatus.ACTIVE, new DateTimeService().getCurrentJavaDateTime());
                        associateFeeWithAllKnownFutureInstallments(fee);
                    }

                } else if (feeBO.getFeeChangeType().equals(FeeChangeType.AMOUNT_UPDATED)) {
                    updateAccountFee(fee, (AmountFeeBO)feeBO);
                    updateUpcomingAndFutureInstallments(fee);
                }
            } catch (PropertyNotFoundException e) {
                throw new BatchJobException(e);
            } catch (AccountException e) {
                throw new BatchJobException(e);
            }
        }
    }

    /**
     * @deprecated - use static factory methods for creating {@link CustomerAccountBO} and inject in installment dates
     */
    @Deprecated
    private void generateCustomerFeeSchedule(final CustomerBO customer) throws AccountException {
        if (customer.getCustomerMeeting() != null && customer.isActiveViaLevel()) {
            Date meetingStartDate = customer.getCustomerMeeting().getMeeting().getMeetingStartDate();
            if (customer.getParentCustomer() != null) {
                Date nextMeetingDate = customer.getParentCustomer().getCustomerAccount().getNextMeetingDate();
                customer.getCustomerMeeting().getMeeting().setMeetingStartDate(nextMeetingDate);
            }
            generateMeetingSchedule();
            customer.getCustomerMeeting().getMeeting().setMeetingStartDate(meetingStartDate);
        }
    }

    private boolean isFeeAlreadyApplied(final AccountFeesEntity fee, final FeeBO feeBO) {
        boolean feeApplied = false;
        if (feeBO.isOneTime()) {
            for (AccountActionDateEntity accountActionDateEntity : getPastInstallments()) {
                CustomerScheduleEntity installment = (CustomerScheduleEntity) accountActionDateEntity;
                if (installment.getAccountFeesAction(fee.getAccountFeeId()) != null) {
                    feeApplied = true;
                    break;
                }
            }
        }
        return feeApplied;

    }

    private void updateAccountFee(final AccountFeesEntity fee, final AmountFeeBO feeBO) {
        fee.setFeeAmount(feeBO.getFeeAmount().getAmountDoubleValue());
        fee.setAccountFeeAmount(feeBO.getFeeAmount());
    }

    private void updateUpcomingAndFutureInstallments(final AccountFeesEntity fee) {

        CustomerScheduleEntity nextInstallment = (CustomerScheduleEntity) getDetailsOfNextInstallment();
        AccountFeesActionDetailEntity nextAccountFeesActionDetail = null;

        if(nextInstallment != null) {
        nextAccountFeesActionDetail = nextInstallment.getAccountFeesAction(fee.getAccountFeeId());
        }

        if (nextAccountFeesActionDetail != null) {
            ((CustomerFeeScheduleEntity) nextAccountFeesActionDetail).setFeeAmount(fee.getAccountFeeAmount());
        }

        List<AccountActionDateEntity> futureInstallments = getFutureInstallments();
        for (AccountActionDateEntity accountActionDateEntity : futureInstallments) {
            CustomerScheduleEntity installment = (CustomerScheduleEntity) accountActionDateEntity;
            AccountFeesActionDetailEntity accountFeesActionDetail = installment.getAccountFeesAction(fee.getAccountFeeId());
            if (accountFeesActionDetail != null) {
                ((CustomerFeeScheduleEntity) accountFeesActionDetail).setFeeAmount(fee.getAccountFeeAmount());
            }
        }
    }

    private void associateFeeWithAllKnownFutureInstallments(final AccountFeesEntity fee) throws AccountException {

        CustomerScheduleEntity nextInstallment = (CustomerScheduleEntity) getDetailsOfNextInstallment();
        createCustomerFeeScheduleForInstallment(fee, nextInstallment);

        List<AccountActionDateEntity> futureInstallments = getFutureInstallments();
        for (AccountActionDateEntity accountActionDateEntity : futureInstallments) {
            CustomerScheduleEntity installment = (CustomerScheduleEntity) accountActionDateEntity;
            createCustomerFeeScheduleForInstallment(fee, installment);
        }
    }

    private void createCustomerFeeScheduleForInstallment(final AccountFeesEntity fee,
            CustomerScheduleEntity nextInstallment) throws AccountException {
        CustomerFeeScheduleEntity accountFeesaction = new CustomerFeeScheduleEntity(nextInstallment, fee.getFees(), fee, fee.getAccountFeeAmount());
        accountFeesaction.setFeeAmountPaid(new Money(fee.getAccountFeeAmount().getCurrency(),"0.0"));
        nextInstallment.addAccountFeesAction(accountFeesaction);
        String description = fee.getFees().getFeeName() + " " + AccountConstants.FEES_APPLIED;
        try {
            addCustomerActivity(new CustomerActivityEntity(this, ApplicationContextProvider.getBean(LegacyPersonnelDao.class).getPersonnel(Short
                    .valueOf("1")), fee.getAccountFeeAmount(), description, new DateTimeService()
                    .getCurrentJavaDateTime()));
        } catch (PersistenceException e) {
            throw new AccountException(e);
        }
    }
    /*
     * In order to do audit logging, we need to get the name of the PaymentTypeEntity.
     * A new instance constructed with the paymentTypeId is not good enough for this,
     * we need to get the lookup value loaded so that we can resolve the name of the
     * PaymentTypeEntity.
     */
    private PaymentTypeEntity getPaymentTypeEntity(final short paymentTypeId) {
        return ApplicationContextProvider.getBean(LegacyAccountDao.class).loadPersistentObject(PaymentTypeEntity.class, paymentTypeId);
    }

    @Override
    public MeetingBO getMeetingForAccount() {
        return getCustomer().getCustomerMeetingValue();
    }

    public boolean isActive() {
        return AccountState.CUSTOMER_ACCOUNT_ACTIVE.equals(getState());
    }
}
