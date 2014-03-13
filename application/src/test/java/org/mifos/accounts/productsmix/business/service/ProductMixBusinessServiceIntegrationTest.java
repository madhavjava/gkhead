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

package org.mifos.accounts.productsmix.business.service;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.sql.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.productdefinition.business.LoanOfferingBO;
import org.mifos.accounts.productdefinition.business.PrdOfferingBO;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ProductType;
import org.mifos.accounts.productdefinition.util.helpers.RecommendedAmountUnit;
import org.mifos.accounts.productsmix.business.ProductMixBO;
import org.mifos.accounts.productsmix.persistence.LegacyProductMixDao;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.BusinessServiceName;
import org.mifos.framework.util.helpers.TestObjectFactory;


public class ProductMixBusinessServiceIntegrationTest extends MifosIntegrationTestCase {

    private SavingsOfferingBO savingsOffering;
    private SavingsOfferingBO secondSavingsOffering;
    private LoanOfferingBO loanOffering;
    private LoanOfferingBO loanOffering2;
    private CustomerBO center;
    private CustomerBO center2;
    private ProductMixBusinessService service;
    MeetingBO meetingIntPost;
    MeetingBO meetingIntCalc;
    MeetingBO meetingIntPost2;
    MeetingBO meetingIntCalc2;
    ProductMixBO prdmix;
    ProductMixBO prdmix2;

    @Before
    public void setUp() throws Exception {
        createSavingProduct();
        service = (ProductMixBusinessService) ServiceFactory.getInstance().getBusinessService(
                BusinessServiceName.PrdMix);
    }

    @After
    public void tearDown() throws Exception {
        TestObjectFactory.removeObject(prdmix);
        TestObjectFactory.removeObject(prdmix2);
        TestObjectFactory.removeObject(loanOffering);
        TestObjectFactory.removeObject(loanOffering2);
        savingsOffering = null;
        TestObjectFactory.removeObject(secondSavingsOffering);
        center = null;
        center2 = null;
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testGetBusinessObject() throws ServiceException {
        Assert.assertNull(service.getBusinessObject(null));
    }

    @Test
    public void testGetAllPrdOfferingsByType_Success() throws ServiceException {
        Assert.assertEquals(1, service.getAllPrdOfferingsByType(ProductType.SAVINGS.getValue().toString()).size());
        StaticHibernateUtil.flushSession();

    }

    @Test
    public void testGetAllowedPrdOfferingsForMixProduct_Success() throws ServiceException {
        Assert.assertEquals(1, service.getAllowedPrdOfferingsForMixProduct(
                savingsOffering.getPrdOfferingId().toString(), ProductType.SAVINGS.getValue().toString()).size());
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testGetAllPrdOfferingsByType_failure() throws ServiceException {
        Assert.assertEquals(0, service.getAllPrdOfferingsByType(ProductType.LOAN.getValue().toString()).size());
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testGetAllowedPrdOfferingsByType() throws ServiceException {
        createSecondSavingProduct();
        Assert.assertEquals(2, service.getAllowedPrdOfferingsByType(savingsOffering.getPrdOfferingId().toString(),
                ProductType.SAVINGS.getValue().toString()).size());

        Assert.assertEquals("A_SavingPrd", service.getAllowedPrdOfferingsByType(
                savingsOffering.getPrdOfferingId().toString(), ProductType.SAVINGS.getValue().toString()).get(0)
                .getPrdOfferingName());

        Assert.assertTrue("Savings products should be in alphabitical order:", (service.getAllowedPrdOfferingsByType(
                savingsOffering.getPrdOfferingId().toString(), ProductType.SAVINGS.getValue().toString()).get(0)
                .getPrdOfferingName().compareToIgnoreCase(service.getAllowedPrdOfferingsByType(
                savingsOffering.getPrdOfferingId().toString(), ProductType.SAVINGS.getValue().toString()).get(1)
                .getPrdOfferingName())) < 0);
        StaticHibernateUtil.flushSession();

    }

    @Test
    public void testGetNotAllowedPrdOfferingsForMixProduct() throws ServiceException {

        createSecondSavingProduct();
        prdmix2 = createNotAllowedProductForAProductOffering(savingsOffering, savingsOffering);
        prdmix = createNotAllowedProductForAProductOffering(savingsOffering, secondSavingsOffering);

        Assert.assertEquals(1, service.getNotAllowedPrdOfferingsForMixProduct(
                savingsOffering.getPrdOfferingId().toString(), ProductType.SAVINGS.getValue().toString()).size());

    }

    @Test
    public void testGetNotAllowedPrdOfferingsByType_success() throws ServiceException {
        createSecondSavingProduct();
        prdmix2 = createNotAllowedProductForAProductOffering(savingsOffering, savingsOffering);
        prdmix = createNotAllowedProductForAProductOffering(savingsOffering, secondSavingsOffering);

        Assert.assertEquals(2, service.getNotAllowedPrdOfferingsByType(savingsOffering.getPrdOfferingId().toString())
                .size());

        Assert.assertTrue("Savings products should be in alphabitical order:", (service
                .getNotAllowedPrdOfferingsByType(savingsOffering.getPrdOfferingId().toString()).get(0)
                .getPrdOfferingName().compareToIgnoreCase(service.getNotAllowedPrdOfferingsByType(
                savingsOffering.getPrdOfferingId().toString()).get(1).getPrdOfferingName())) < 0);

        StaticHibernateUtil.flushSession();
    }

    private ProductMixBO createNotAllowedProductForAProductOffering(PrdOfferingBO prdOffering,
            PrdOfferingBO prdOfferingNotAllowedId) {
        return TestObjectFactory.createNotAllowedProductForAProductOffering(prdOffering, prdOfferingNotAllowedId);

    }

    private CenterBO createCenter() {
        return createCenter("Center_Active_test");
    }

    private CenterBO createCenter(String name) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        return TestObjectFactory.createWeeklyFeeCenter(name, meeting);
    }

    private void createSavingProduct() {
        Date startDate = new Date(System.currentTimeMillis());

        meetingIntCalc = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        meetingIntPost = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));

        center = createCenter();
        savingsOffering = TestObjectFactory.createSavingsProduct("SavingPrd1", "S", startDate,
                RecommendedAmountUnit.COMPLETE_GROUP, meetingIntCalc, meetingIntPost);

    }

    private void createSecondSavingProduct() {
        Date startDate = new Date(System.currentTimeMillis());
        meetingIntCalc2 = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        meetingIntPost2 = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));

        center2 = createCenter("Center_Active_test2");

        secondSavingsOffering = TestObjectFactory.createSavingsProduct("A_SavingPrd", "AS", startDate,
                RecommendedAmountUnit.COMPLETE_GROUP, meetingIntCalc2, meetingIntPost2);

    }

    @Test
    public void testGetPrdOfferingMix() throws ServiceException, PersistenceException {
        createLoanProductMixed();
        createsecondLoanProductMixed();
        prdmix = createNotAllowedProductForAProductOffering(loanOffering, loanOffering);
        Assert.assertEquals(2, service.getPrdOfferingMix().size());
        Assert
                .assertTrue("Products Mix should be in alphabitical order:", (service.getPrdOfferingMix().get(0)
                        .getPrdOfferingName().compareToIgnoreCase(service.getPrdOfferingMix().get(1)
                        .getPrdOfferingName())) < 0);
        StaticHibernateUtil.flushSession();

    }

    private void createLoanProductMixed() throws PersistenceException {

        Date startDate = new Date(System.currentTimeMillis());

        meetingIntCalc = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));

        loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", startDate, meetingIntCalc);
        loanOffering.updatePrdOfferingFlag();

    }

    private void createsecondLoanProductMixed() throws PersistenceException {

        Date startDate = new Date(System.currentTimeMillis());

        meetingIntCalc = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));

        loanOffering2 = TestObjectFactory.createLoanOffering("aLoan", "aL", startDate, meetingIntCalc);
        loanOffering2.updatePrdOfferingFlag();

    }

    @Test
    public void testCanProductsExist() throws Exception {
        LegacyProductMixDao productMixPersistenceMock = createMock(LegacyProductMixDao.class);
        short PRD_OFFERING_ID_ONE = (short) 1;
        short PRD_OFFERING_ID_TWO = (short) 2;
        LoanOfferingBO loanOfferingMock1 = createMock(LoanOfferingBO.class);
        LoanOfferingBO loanOfferingMock2 = createMock(LoanOfferingBO.class);

        expect(productMixPersistenceMock.doesPrdOfferingsCanCoexist(PRD_OFFERING_ID_ONE, PRD_OFFERING_ID_TWO))
                .andReturn(true);
        expect(loanOfferingMock1.getPrdOfferingId()).andReturn(PRD_OFFERING_ID_ONE);
        expect(loanOfferingMock2.getPrdOfferingId()).andReturn(PRD_OFFERING_ID_TWO);
        replay(loanOfferingMock1, loanOfferingMock2, productMixPersistenceMock);

        new ProductMixBusinessService(productMixPersistenceMock).canProductsCoExist(loanOfferingMock1,
                loanOfferingMock2);
        verify(loanOfferingMock1, loanOfferingMock2, productMixPersistenceMock);
    }

}
