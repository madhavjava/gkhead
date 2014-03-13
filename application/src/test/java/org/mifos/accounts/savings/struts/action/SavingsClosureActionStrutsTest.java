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

package org.mifos.accounts.savings.struts.action;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.business.AccountPaymentEntity;
import org.mifos.accounts.business.AccountTestUtils;
import org.mifos.accounts.productdefinition.business.SavingsOfferingBO;
import org.mifos.accounts.productdefinition.util.helpers.ApplicableTo;
import org.mifos.accounts.productdefinition.util.helpers.RecommendedAmountUnit;
import org.mifos.accounts.productdefinition.util.helpers.SavingsType;
import org.mifos.accounts.savings.SavingBOTestUtils;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.persistence.SavingsDao;
import org.mifos.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.accounts.util.helpers.AccountActionTypes;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.accounts.util.helpers.AccountState;
import org.mifos.accounts.util.helpers.AccountStates;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.builders.MifosUserBuilder;
import org.mifos.config.business.Configuration;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.MifosUser;
import org.mifos.security.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;


public class SavingsClosureActionStrutsTest extends MifosMockStrutsTestCase {


    private UserContext userContext;
    private CustomerBO group;
    private CustomerBO center;
    private SavingsBO savings;
    private SavingsOfferingBO savingsOffering;
    private CustomerBO client1;
    private CustomerBO client2;
    private CustomerBO client3;
    private CustomerBO client4;
    private SavingsTestHelper helper = new SavingsTestHelper();
    private String flowKey;

    @Autowired
    private SavingsDao savingsDao;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/accounts-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {
        userContext = TestUtils.makeUser();
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        request.getSession().setAttribute(Constants.USER_CONTEXT_KEY, userContext);
        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, SavingsClosureAction.class);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);

        SecurityContext securityContext = new SecurityContextImpl();
        MifosUser principal = new MifosUserBuilder().build();
        Authentication authentication = new TestingAuthenticationToken(principal, principal);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        SecurityContext securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);
        savings = null;
        client1 = null;
        client2 = null;
        client3 = null;
        client4 = null;
        group = null;
        center = null;
    }

    @Test
    public void testSuccessfulLoad_Client() throws Exception {
        createInitialObjects();
        createClients();
        savingsOffering = TestObjectFactory.createSavingsProduct("Offering1", "s1", SavingsType.MANDATORY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        savings = createSavingsAccount("000X00000000017", savingsOffering, client1, AccountState.SAVINGS_ACTIVE);
        StaticHibernateUtil.flushSession();
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, savings, request);
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "load");
        actionPerform();
        verifyForward("load_success");
        savings = (SavingsBO) SessionUtils.getAttribute(Constants.BUSINESS_KEY, request);

        savings = (SavingsBO) StaticHibernateUtil.getSessionTL().get(SavingsBO.class, savings.getAccountId());
        Hibernate.initialize(savings.getAccountPayments());
        Hibernate.initialize(savings.getAccountFees());
        Hibernate.initialize(savings.getAccountActionDates());
        Assert.assertNotNull(SessionUtils.getAttribute(MasterConstants.PAYMENT_TYPE, request));

        group = new CustomerPersistence().getCustomer(group.getCustomerId());
        center = new CustomerPersistence().getCustomer(center.getCustomerId());
        client1 = new CustomerPersistence().getCustomer(client1.getCustomerId());
        client2 = new CustomerPersistence().getCustomer(client2.getCustomerId());
        client3 = new CustomerPersistence().getCustomer(client3.getCustomerId());
        client4 = new CustomerPersistence().getCustomer(client4.getCustomerId());
    }

    @Test
    public void testSuccessfullLoad() throws Exception {
        createInitialObjects();
        createClients();
        savingsOffering = createSavingsOffering();
        savings = createSavingsAccount("000X00000000017", savingsOffering, group, AccountState.SAVINGS_ACTIVE);
        StaticHibernateUtil.flushSession();
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, savings, request);
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "load");
        actionPerform();
        verifyForward("load_success");
        savings = (SavingsBO) SessionUtils.getAttribute(Constants.BUSINESS_KEY, request);
        savings = (SavingsBO) StaticHibernateUtil.getSessionTL().get(SavingsBO.class, savings.getAccountId());
        Hibernate.initialize(savings.getAccountPayments());
        Hibernate.initialize(savings.getAccountFees());
        Hibernate.initialize(savings.getAccountActionDates());
        Assert.assertNotNull(SessionUtils.getAttribute(MasterConstants.PAYMENT_TYPE, request));

        group = savings.getCustomer();
        center = group.getParentCustomer();
        client1 = new CustomerPersistence().getCustomer(client1.getCustomerId());
        client2 = new CustomerPersistence().getCustomer(client2.getCustomerId());
        client3 = new CustomerPersistence().getCustomer(client3.getCustomerId());
        client4 = new CustomerPersistence().getCustomer(client4.getCustomerId());
    }

    @Test
    public void testSuccessfullPreview() throws Exception {
        AccountPaymentEntity payment = new AccountPaymentEntity(null, new Money(Configuration.getInstance()
                .getSystemConfig().getCurrency(), "500"), null, null, null, new Date(System.currentTimeMillis()));
        SessionUtils.setAttribute(SavingsConstants.ACCOUNT_PAYMENT, payment, request);
        addRequestParameter("receiptId", "101");
        addRequestParameter("receiptDate", DateUtils.makeDateAsSentFromBrowser());
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "notes");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();
        verifyNoActionErrors();
        verifyForward("preview_success");
    }

    @Test
    public void testSuccessfullPreviewWithBlankPaymentId() throws Exception {
        AccountPaymentEntity payment = new AccountPaymentEntity(null, new Money(Configuration.getInstance()
                .getSystemConfig().getCurrency(), "500"), null, null, null, new Date(System.currentTimeMillis()));
        SessionUtils.setAttribute(SavingsConstants.ACCOUNT_PAYMENT, payment, request);
        addRequestParameter("receiptId", "101");
        addRequestParameter("receiptDate", "");
        addRequestParameter("paymentTypeId", "");
        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "notes");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();
        verifyNoActionErrors();
        verifyForward("preview_success");
    }

    @Test
    public void testSuccessfullPreviewWithNullPaymentId() throws Exception {
        AccountPaymentEntity payment = new AccountPaymentEntity(null, new Money(Configuration.getInstance()
                .getSystemConfig().getCurrency(), "500"), null, null, null, new Date(System.currentTimeMillis()));
        SessionUtils.setAttribute(SavingsConstants.ACCOUNT_PAYMENT, payment, request);
        addRequestParameter("receiptId", "101");
        addRequestParameter("receiptDate", "");

        // paymentTypeId is left null

        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "notes");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();
        verifyNoActionErrors();
        verifyForward("preview_success");
    }

    @Test
    public void testPreviewDateValidation() throws Exception {
        AccountPaymentEntity payment = new AccountPaymentEntity(null, new Money(Configuration.getInstance()
                .getSystemConfig().getCurrency(), "500"), null, null, null, new Date(System.currentTimeMillis()));
        SessionUtils.setAttribute(SavingsConstants.ACCOUNT_PAYMENT, payment, request);
        addRequestParameter("receiptId", "101");
        String badDate = "3/20/2005"; // an invalid date
        addRequestParameter("receiptDate", badDate);
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "notes");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();
        verifyActionErrors(new String[] { AccountConstants.ERROR_INVALIDDATE });
    }

    @Test
    public void testSuccessfullPreview_withoutReceipt() throws Exception {
        AccountPaymentEntity payment = new AccountPaymentEntity(null, new Money(Configuration.getInstance()
                .getSystemConfig().getCurrency(), "500"), null, null, null, new Date(System.currentTimeMillis()));
        SessionUtils.setAttribute(SavingsConstants.ACCOUNT_PAYMENT, payment, request);
        addRequestParameter("receiptId", "");
        addRequestParameter("receiptDate", "");
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "notes");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();
        verifyForward("preview_success");
    }

    @Test
    public void testSuccessfullPrevious() {
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "previous");
        actionPerform();
        verifyForward("previous_success");
    }

    @Test
    public void testSuccessfullCancel() throws Exception {
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "cancel");
        actionPerform();
        verifyForward("close_success");
    }

    @Test
    public void testSuccessfullCloseAccount() throws Exception {
        createInitialObjects();
        createClients();
        savingsOffering = helper.createSavingsOffering("asfddsf", "213a");
        savings = helper.createSavingsAccount("000X00000000017", savingsOffering, group,
                AccountStates.SAVINGS_ACC_APPROVED, userContext);
        SavingBOTestUtils.setActivationDate(savings, helper.getDate("20/05/2006"));
        PersonnelBO createdBy = legacyPersonnelDao.getPersonnel(userContext.getId());
        AccountPaymentEntity payment1 = helper.createAccountPaymentToPersist(savings,
                TestUtils.createMoney( "1000.0"), TestUtils.createMoney("1000.0"), helper
                .getDate("30/05/2006"), AccountActionTypes.SAVINGS_DEPOSIT.getValue(), savings, createdBy, group);
        AccountTestUtils.addAccountPayment(payment1, savings);
        savings.update();
        StaticHibernateUtil.flushSession();

        Money balanceAmount = TestUtils.createMoney("1500.0");
        AccountPaymentEntity payment2 = helper.createAccountPaymentToPersist(savings,
                TestUtils.createMoney("500.0"), balanceAmount, helper.getDate("15/06/2006"),
                AccountActionTypes.SAVINGS_DEPOSIT.getValue(), savings, createdBy, group);
        AccountTestUtils.addAccountPayment(payment2, savings);
        savings.update();
        StaticHibernateUtil.flushSession();

        Money interestAmount = TestUtils.createMoney("40");
        SavingBOTestUtils.setInterestToBePosted(savings, interestAmount);
        SavingBOTestUtils.setBalance(savings, balanceAmount);
        savings.update();
        StaticHibernateUtil.flushSession();

        savings =  savingsDao.findById(savings.getAccountId());
        savings.setUserContext(userContext);

        SessionUtils.setAttribute(Constants.BUSINESS_KEY, savings, request);
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "load");
        actionPerform();
        verifyForward("load_success");

        addRequestParameter("receiptId", "101");
        addRequestParameter("receiptDate", DateUtils.makeDateAsSentFromBrowser());
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("customerId", "1");
        addRequestParameter("notes", "closing account");
        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "preview");
        actionPerform();

        setRequestPathInfo("/savingsClosureAction.do");
        addRequestParameter("method", "close");
        actionPerform();

        verifyNoActionErrors();
        verifyNoActionMessages();
        verifyForward("close_success");
        savings = TestObjectFactory.getObject(SavingsBO.class, savings.getAccountId());

       Assert.assertEquals(new Money(getCurrency()), savings.getSavingsBalance());
       Assert.assertEquals(AccountState.SAVINGS_CLOSED.getValue(), savings.getAccountState().getId());
    }

    private void createInitialObjects() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group_Active_test", CustomerStatus.GROUP_ACTIVE, center);
    }

    private void createClients() {
        client1 = TestObjectFactory.createClient("client1", CustomerStatus.CLIENT_CLOSED, group);
        client2 = TestObjectFactory.createClient("client2", CustomerStatus.CLIENT_ACTIVE, group);
        client3 = TestObjectFactory.createClient("client3", CustomerStatus.CLIENT_PARTIAL, group);
        client4 = TestObjectFactory.createClient("client4", CustomerStatus.CLIENT_HOLD, group);
    }

    private SavingsOfferingBO createSavingsOffering() {
        Date currentDate = new Date(System.currentTimeMillis());
        return TestObjectFactory.createSavingsProduct("SavingPrd1", "S", currentDate, RecommendedAmountUnit.PER_INDIVIDUAL);
    }

    private SavingsBO createSavingsAccount(String globalAccountNum, SavingsOfferingBO savingsOffering,
            CustomerBO group, AccountState state) throws Exception {
        return TestObjectFactory.createSavingsAccount(globalAccountNum, group, state, new Date(), savingsOffering,
                userContext);
    }
}
