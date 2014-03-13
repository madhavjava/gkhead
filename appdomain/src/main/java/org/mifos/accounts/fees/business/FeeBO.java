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

package org.mifos.accounts.fees.business;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mifos.accounts.fees.exceptions.FeeException;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeeChangeType;
import org.mifos.accounts.fees.util.helpers.FeeConstants;
import org.mifos.accounts.fees.util.helpers.FeeFrequencyType;
import org.mifos.accounts.fees.util.helpers.FeeLevel;
import org.mifos.accounts.fees.util.helpers.FeeStatus;
import org.mifos.accounts.fees.util.helpers.RateAmountFlag;
import org.mifos.accounts.financial.business.GLCodeEntity;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.application.master.persistence.LegacyMasterDao;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.config.AccountingRules;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.dto.domain.FeeDto;
import org.mifos.dto.domain.FeeFrequencyDto;
import org.mifos.dto.domain.GLCodeDto;
import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.PropertyNotFoundException;
import org.mifos.framework.util.helpers.Money;
import org.mifos.security.util.UserContext;

public abstract class FeeBO extends AbstractBusinessObject {

    private final Short feeId;
    private final String feeName;
    private Short changeType;
    private FeeStatusEntity feeStatus;
    private final OfficeBO office;
    private final CategoryTypeEntity categoryType;
    private final FeeFrequencyEntity feeFrequency;
    private final GLCodeEntity glCode;
    private final Set<FeeLevelEntity> feeLevels;

    private OfficePersistence officePersistence;

    public OfficePersistence getOfficePersistence() {
        if(officePersistence == null){
            officePersistence = new OfficePersistence();
        }
        return officePersistence;
    }

    public void setOfficePersistence(final OfficePersistence officePersistence) {
        this.officePersistence = officePersistence;
    }

    /**
     * @param office
     * @param createdByUserId
     * @param createdDate
     *
     */
    public FeeBO(final String name, final FeeCategory category, final FeeFrequencyType feeFrequencyType,
            final GLCodeEntity feeGLCode, final MeetingBO meetingPeriodicity, final OfficeBO office,
            final Date createdDate,
            final Short createdByUserId) {
        this.feeId = null;
        this.feeName = name;
        this.categoryType = new CategoryTypeEntity(category);
        this.glCode = feeGLCode;
        this.feeLevels = new HashSet<FeeLevelEntity>();
        this.changeType = FeeChangeType.NOT_UPDATED.getValue();
        this.feeStatus = new FeeStatusEntity(FeeStatus.ACTIVE);
        this.createdDate = createdDate;
        this.createdBy = createdByUserId;
        this.office = office;

        FeePaymentEntity feePaymentEntity = null;
        FeeFrequencyTypeEntity feeFrequencyTypeEntity = new FeeFrequencyTypeEntity(feeFrequencyType);
        try {
            this.feeFrequency = new FeeFrequencyEntity(feeFrequencyTypeEntity, this, feePaymentEntity,
                    meetingPeriodicity);
        } catch (FeeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Constructor to create a valid Fee Object
     */
    protected FeeBO(final UserContext userContext, final String feeName, final CategoryTypeEntity categoryType,
            final FeeFrequencyTypeEntity feeFrequencyType, final GLCodeEntity glCodeEntity, final boolean isCustomerDefaultFee,
            final FeePaymentEntity feePayment, final MeetingBO feeMeeting) throws FeeException {

        validateFields(feeName, categoryType, glCodeEntity);
        this.feeFrequency = new FeeFrequencyEntity(feeFrequencyType, this, feePayment, feeMeeting);

        setUserContext(userContext);
        setCreateDetails();

        this.feeName = feeName;
        this.categoryType = categoryType;
        this.glCode = glCodeEntity;

        this.feeId = null;
        this.feeLevels = new HashSet<FeeLevelEntity>();
        try {
            this.office = getOfficePersistence().getHeadOffice();
        } catch (PersistenceException e) {
            throw new FeeException(e);
        }
        this.changeType = FeeChangeType.NOT_UPDATED.getValue();
        this.setFeeStatus(retrieveFeeStatusEntity(FeeStatus.ACTIVE));
        if (isCustomerDefaultFee) {
            makeFeeDefaultToCustomer();
        }
    }

    /**
     * Adding a default constructor is hibernate's requirement and should not be
     * used to create a valid Fee object.
     */
    protected FeeBO() {
        this.feeId = null;
        this.office = null;
        this.feeName = null;
        this.categoryType = null;
        this.feeFrequency = null;
        this.glCode = null;
        this.feeLevels = null;
    }

    public Short getFeeId() {
        return feeId;
    }

    public OfficeBO getOffice() {
        return office;
    }

    public String getFeeName() {
        return feeName;
    }

    public CategoryTypeEntity getCategoryType() {
        return categoryType;
    }

    public FeeFrequencyEntity getFeeFrequency() {
        return feeFrequency;
    }

    public Set<FeeLevelEntity> getFeeLevels() {
        return feeLevels;
    }

    public FeeStatusEntity getFeeStatus() {
        return feeStatus;
    }

    public GLCodeEntity getGlCode() {
        return glCode;
    }

    public FeeChangeType getFeeChangeType() throws PropertyNotFoundException {
        return FeeChangeType.getFeeChangeType(this.changeType);
    }

    public void updateFeeChangeType(final FeeChangeType updateFlag) {
        this.changeType = updateFlag.getValue();
    }

    private void setFeeStatus(final FeeStatusEntity feeStatus) {
        this.feeStatus = feeStatus;
    }

    public void updateStatus(final FeeStatus status) throws FeeException {
        if (!this.feeStatus.getId().equals(status.getValue())) {
            setFeeStatus(retrieveFeeStatusEntity(status));
        }
    }

    public void update() throws FeeException {
        try {
            setUpdateDetails();
            ApplicationContextProvider.getBean(LegacyAccountDao.class).createOrUpdate(this);
        } catch (PersistenceException e) {
            throw new FeeException(FeeConstants.FEE_UPDATE_ERROR, e);
        }
    }

    public abstract RateAmountFlag getFeeType();

    public void save() throws FeeException {
        try {
            ApplicationContextProvider.getBean(LegacyAccountDao.class).createOrUpdate(this);
        } catch (PersistenceException he) {
            throw new FeeException(FeeConstants.FEE_CREATE_ERROR, he);
        }
    }

    public boolean isActive() {
        return getFeeStatus().isActive();
    }

    public boolean isPeriodic() {
        return getFeeFrequency().isPeriodic();
    }

    public boolean isOneTime() {
        return getFeeFrequency().isOneTime();
    }

    public boolean isTimeOfDisbursement() {
        return getFeeFrequency().isTimeOfDisbursement();
    }

    /**
     * Returns true if any fees applied to an installment might be an amount
     * that exceeds the precision specified by initial or final rounding of
     * amounts in an installment. This check is required temporarily in order to
     * prevent re-rounding when a fee is added after payments have been made.
     */
    public abstract boolean doesFeeInvolveFractionalAmounts();

    public boolean isCustomerDefaultFee() {
        return getFeeLevels().size() > 0;
    }

    protected void validateFields(final String feeName, final CategoryTypeEntity categoryType, final GLCodeEntity glCode)
            throws FeeException {
        validateFeeName(feeName);
        validateFeeCateogry(categoryType);
        validateGLCode(glCode);
    }

    private void makeFeeDefaultToCustomer() throws FeeException {
        try {

            if (getCategoryType().getFeeCategory().equals(FeeCategory.CLIENT)) {
                addFeeLevel(createFeeLevel(FeeLevel.CLIENTLEVEL));
            } else if (getCategoryType().getFeeCategory().equals(FeeCategory.GROUP)) {
                addFeeLevel(createFeeLevel(FeeLevel.GROUPLEVEL));
            } else if (getCategoryType().getFeeCategory().equals(FeeCategory.CENTER)) {
                addFeeLevel(createFeeLevel(FeeLevel.CENTERLEVEL));
            } else if (getCategoryType().getFeeCategory().equals(FeeCategory.ALLCUSTOMERS)) {
                addFeeLevel(createFeeLevel(FeeLevel.CLIENTLEVEL));
                addFeeLevel(createFeeLevel(FeeLevel.GROUPLEVEL));
                addFeeLevel(createFeeLevel(FeeLevel.CENTERLEVEL));
            }
        } catch (PropertyNotFoundException pnfe) {
            throw new FeeException(pnfe);
        }
    }

    private FeeLevelEntity createFeeLevel(final FeeLevel feeLevel) {
        return new FeeLevelEntity(this, feeLevel);
    }

    private void addFeeLevel(final FeeLevelEntity feeLevel) {
        feeLevels.add(feeLevel);
    }

    private void validateFeeName(final String feeName) throws FeeException {
        if (StringUtils.isBlank(feeName)) {
            throw new FeeException(FeeConstants.INVALID_FEE_NAME);
        }
    }

    private void validateGLCode(final GLCodeEntity glCode) throws FeeException {
        if (glCode == null) {
            throw new FeeException(FeeConstants.INVALID_GLCODE);
        }
    }

    private void validateFeeCateogry(final CategoryTypeEntity categoryType) throws FeeException {
        if (categoryType == null) {
            throw new FeeException(FeeConstants.INVALID_FEE_CATEGORY);
        }
    }

    private FeeStatusEntity retrieveFeeStatusEntity(final FeeStatus status) throws FeeException {
        try {
            return ApplicationContextProvider.getBean(LegacyMasterDao.class).findMasterDataEntityWithLocale(FeeStatusEntity.class, status.getValue());
        } catch (PersistenceException pe) {
            throw new FeeException(pe);
        }
    }

    public boolean isMonthly() {
        return this.feeFrequency.getFeeMeetingFrequency().isMonthly();
    }

    public boolean isWeekly() {
        return this.feeFrequency.getFeeMeetingFrequency().isWeekly();
    }

    public FeeDto toDto() {
        FeeDto feeDto = new FeeDto();
        feeDto.setId(Short.toString(this.feeId));
        feeDto.setName(this.feeName);
        feeDto.setCategoryType(this.categoryType.getName());
        feeDto.setFeeStatus(this.feeStatus.toDto());
        feeDto.setActive(isActive());
        feeDto.setCustomerDefaultFee(this.isCustomerDefaultFee());
        feeDto.setRateBasedFee(this instanceof RateFeeBO);

        feeDto.setChangeType(this.changeType);

        FeeFrequencyDto feeFrequencyDto = this.feeFrequency.toDto();
        feeDto.setFeeFrequency(feeFrequencyDto);

        GLCodeDto glCodeDto = this.glCode.toDto();
        feeDto.setGlCodeDto(glCodeDto);
        feeDto.setGlCode(glCodeDto.getGlcode());

        feeDto.setOneTime(isOneTime());
        feeDto.setPeriodic(isPeriodic());
        feeDto.setTimeOfDisbursement(isTimeOfDisbursement());

        if (this instanceof AmountFeeBO) {
            Money amount = ((AmountFeeBO) this).getFeeAmount();
            feeDto.setCurrencyId(amount.getCurrency().getCurrencyId().intValue());
            feeDto.setAmount(amount.toString(AccountingRules.getDigitsAfterDecimal()));
            feeDto.setRateBasedFee(false);
        } else {
            RateFeeBO rateFeeBo = (RateFeeBO) this;
            feeDto.setRate(rateFeeBo.getRate());
            feeDto.setFeeFormula(rateFeeBo.getFeeFormula().toDto());
            feeDto.setRateBasedFee(true);
        }
        return feeDto;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FeeBO)) {
            return false;
        }

        FeeBO rhs = (FeeBO) obj;
        return new EqualsBuilder().append(this.getFeeId(), rhs.getFeeId()).append(this.getFeeName(), rhs.getFeeName()).isEquals();
    }

    @Override
    public int hashCode() {
        int initialNonZeroOddNumber = 7;
        int multiplierNonZeroOddNumber = 7;
        return new HashCodeBuilder(initialNonZeroOddNumber, multiplierNonZeroOddNumber).append(this.getFeeId()).append(this.getFeeName()).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}