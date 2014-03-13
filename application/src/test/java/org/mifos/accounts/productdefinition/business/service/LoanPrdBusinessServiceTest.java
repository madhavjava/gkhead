package org.mifos.accounts.productdefinition.business.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Test;
import org.mifos.accounts.productdefinition.persistence.LoanPrdPersistence;
import org.mifos.accounts.productdefinition.persistence.PrdOfferingPersistence;
import org.mifos.accounts.productdefinition.util.helpers.PrdCategoryStatus;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.springframework.test.annotation.ExpectedException;

public class LoanPrdBusinessServiceTest {
    final PrdOfferingPersistence prdOfferingPersistence = mock(PrdOfferingPersistence.class);
    final LoanPrdPersistence loanPrdPersistence = mock(LoanPrdPersistence.class);

    LoanPrdBusinessService loanPrdBusinessService = new LoanPrdBusinessService() {
        @Override
        protected PrdOfferingPersistence getPrdOfferingPersistence() {
            return prdOfferingPersistence;
        }

        @Override
        protected LoanPrdPersistence getLoanPrdPersistence() {
            return loanPrdPersistence;
        }
    };
    private Short localeId = new Short("1");

    @Test
    @ExpectedException(value = ServiceException.class)
    public void testInvalidConnectionThrowsExceptionInGetApplicableProductCategories() throws PersistenceException {

        try {
            when(prdOfferingPersistence.getApplicableProductCategories(ProductType.LOAN, PrdCategoryStatus.ACTIVE)).
                    thenThrow(new PersistenceException("some exception"));
            loanPrdBusinessService.getActiveLoanProductCategories();
            Assert.fail("should fail because of invalid session");
        } catch (ServiceException e) {
        }
    }

    @Test
    @ExpectedException(value = ServiceException.class)
    public void testInvalidConnectionThrowsExceptionInGetApplicablePrdStatus() throws PersistenceException {

        try {
            when(prdOfferingPersistence.getApplicablePrdStatus(ProductType.LOAN, localeId)).
                    thenThrow(new PersistenceException("some exception"));
            loanPrdBusinessService.getApplicablePrdStatus(localeId);
            Assert.fail("should fail because of invalid session");
        } catch (ServiceException e) {
        }
    }

    @Test
    @ExpectedException(value = ServiceException.class)
    public void testInvalidConnectionThrowsExceptionInGetAllLoanOfferings() throws PersistenceException {

        try {
            when(loanPrdPersistence.getAllLoanOfferings(localeId)).thenThrow(new PersistenceException("some exception"));
            loanPrdBusinessService.getAllLoanOfferings(localeId);
            Assert.fail("should fail because of invalid session");
        } catch (ServiceException e) {
        }
    }

    @Test
    @ExpectedException(value = ServiceException.class)
    public void testInvalidConnectionThrowsExceptionInGetLoanOffering() throws PersistenceException {

        try {
            when(loanPrdPersistence.getLoanOffering(new Short("112"))).thenThrow(new PersistenceException("some exception"));
            loanPrdBusinessService.getLoanOffering(new Short("112"));
            Assert.fail("should fail because of invalid session");
        } catch (ServiceException e) {
        }
    }

    @Test
    @ExpectedException(value = ServiceException.class)
    public void testInvalidConnectionThrowsExceptionInGetLoanOfferingWithLocaleId() throws PersistenceException {

        try {
            when(loanPrdPersistence.getLoanOffering(new Short("112"), localeId)).thenThrow(new PersistenceException("some exception"));
            loanPrdBusinessService.getLoanOffering(new Short("112"), localeId);
            Assert.fail("should fail because of invalid session");
        } catch (ServiceException e) {
        }
    }

}
