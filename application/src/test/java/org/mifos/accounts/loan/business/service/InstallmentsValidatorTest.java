package org.mifos.accounts.loan.business.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mifos.framework.util.CollectionUtils.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.accounts.loan.business.service.validators.InstallmentFormatValidator;
import org.mifos.accounts.loan.business.service.validators.InstallmentRulesValidator;
import org.mifos.accounts.loan.business.service.validators.InstallmentValidationContext;
import org.mifos.accounts.loan.business.service.validators.InstallmentsValidator;
import org.mifos.accounts.loan.business.service.validators.InstallmentsValidatorImpl;
import org.mifos.accounts.loan.business.service.validators.ListOfInstallmentsValidator;
import org.mifos.accounts.loan.util.helpers.RepaymentScheduleInstallment;
import org.mifos.accounts.loan.util.helpers.RepaymentScheduleInstallmentBuilder;
import org.mifos.accounts.productdefinition.business.VariableInstallmentDetailsBO;
import org.mifos.application.admin.servicefacade.HolidayServiceFacade;
import org.mifos.platform.validations.Errors;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InstallmentsValidatorTest {

    private RepaymentScheduleInstallmentBuilder installmentBuilder;

    private Locale locale;

    @Mock
    private InstallmentFormatValidator installmentFormatValidator;

    @Mock
    private ListOfInstallmentsValidator listOfInstallmentsValidator;

    @Mock
    private InstallmentRulesValidator installmentRulesValidator;

    @Mock
    private HolidayServiceFacade holidayServiceFacade;

    private InstallmentsValidator installmentsValidator;

    private Short officeId;

    @Before
    public void setUp() throws Exception {
        locale = new Locale("en", "GB");
        installmentBuilder = new RepaymentScheduleInstallmentBuilder(locale);
        installmentsValidator = new InstallmentsValidatorImpl(installmentFormatValidator, listOfInstallmentsValidator, installmentRulesValidator);
        officeId = Short.valueOf("1");
    }

    @Test
    public void validateShouldCallFormatListOfAndRulesValidators() throws Exception {
        RepaymentScheduleInstallment installment1 = installmentBuilder.reset(locale).withInstallment(1).withDueDateValue("01-Nov-2010").build();
        RepaymentScheduleInstallment installment2 = installmentBuilder.reset(locale).withInstallment(2).withDueDateValue("06-Nov-2010").build();
        RepaymentScheduleInstallment installment3 = installmentBuilder.reset(locale).withInstallment(3).withDueDateValue("08-Nov-2010").build();

        List<RepaymentScheduleInstallment> installments = asList(installment1, installment2, installment3);
        Errors errors = installmentsValidator.validateInputInstallments(installments, getValidationContext(null));
        for (RepaymentScheduleInstallment installment : installments) {
            verify(installmentFormatValidator).validateDueDateFormat(installment);
            verify(installmentFormatValidator).validateTotalAmountFormat(installment);
        }
        verify(listOfInstallmentsValidator).validateDuplicateDueDates(installments);
        verify(listOfInstallmentsValidator).validateOrderingOfDueDates(installments);

        verify(installmentRulesValidator).validateForDisbursementDate(eq(installments), any(Date.class));
        verify(installmentRulesValidator).validateDueDatesForVariableInstallments(eq(installments), any(VariableInstallmentDetailsBO.class), any(Date.class));
        verify(installmentRulesValidator).validateForHolidays(eq(installments), any(HolidayServiceFacade.class), eq(officeId));
        assertThat(errors.hasErrors(), is(false));
    }

    @Test
    public void shouldValidateInstallmentSchedule() {
        RepaymentScheduleInstallment installment1 = installmentBuilder.reset(locale).withInstallment(1).withDueDateValue("01-Nov-2010").build();
        RepaymentScheduleInstallment installment2 = installmentBuilder.reset(locale).withInstallment(2).withDueDateValue("06-Nov-2010").build();
        RepaymentScheduleInstallment installment3 = installmentBuilder.reset(locale).withInstallment(3).withDueDateValue("08-Nov-2010").build();

        List<RepaymentScheduleInstallment> installments = asList(installment1, installment2, installment3);
        installmentsValidator.validateInstallmentSchedule(installments, BigDecimal.ZERO);
        verify(installmentRulesValidator).validateForMinimumInstallmentAmount(installments, BigDecimal.ZERO);
    }

    private InstallmentValidationContext getValidationContext(Date disbursementDate) {
        return new InstallmentValidationContext(disbursementDate, new VariableInstallmentDetailsBO(), BigDecimal.ZERO, holidayServiceFacade, officeId);
    }
}
