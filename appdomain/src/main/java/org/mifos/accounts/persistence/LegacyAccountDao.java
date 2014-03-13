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

package org.mifos.accounts.persistence;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountFeesEntity;
import org.mifos.accounts.business.AccountOverpaymentEntity;
import org.mifos.accounts.business.AccountPaymentEntity;
import org.mifos.accounts.business.AccountStateEntity;
import org.mifos.accounts.business.AccountStateFlagEntity;
import org.mifos.accounts.fees.business.FeeBO;
import org.mifos.accounts.fees.util.helpers.FeeCategory;
import org.mifos.accounts.fees.util.helpers.FeeFrequencyType;
import org.mifos.accounts.fees.util.helpers.FeeStatus;
import org.mifos.accounts.financial.business.COABO;
import org.mifos.accounts.financial.business.COAHierarchyEntity;
import org.mifos.accounts.financial.business.GLCategoryType;
import org.mifos.accounts.financial.business.GLCodeEntity;
import org.mifos.accounts.loan.business.GuarantyEntity;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.accounts.loan.business.LoanScheduleEntity;
import org.mifos.accounts.savings.business.SavingsScheduleEntity;
import org.mifos.accounts.util.helpers.AccountConstants;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.holiday.business.Holiday;
import org.mifos.core.MifosRuntimeException;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.business.CustomerScheduleEntity;
import org.mifos.customers.checklist.business.AccountCheckListBO;
import org.mifos.customers.util.helpers.CustomerSearchConstants;
import org.mifos.customers.util.helpers.Param;
import org.mifos.customers.util.helpers.QueryParamConstants;
import org.mifos.framework.exceptions.HibernateSearchException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.QueryFactory;
import org.mifos.framework.hibernate.helper.QueryInputs;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.QueryResultAccountIdSearch;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.LegacyGenericDao;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.MifosStringUtils;

/**
 * FIXME: the term "account" has two meanings in this class:
 * <ol>
 * <li>A loan or savings account</li>
 * <li>A general ledger account</li>
 * </ol>
 */
@SuppressWarnings("unchecked")
public class LegacyAccountDao extends LegacyGenericDao {

    private LegacyAccountDao() {
    }

    public AccountBO getAccount(Integer accountId) throws PersistenceException {
        return getPersistentObject(AccountBO.class, accountId);
    }

    public Integer getAccountRunningNumber() throws PersistenceException {
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.GET_MAX_ACCOUNT_ID, null);
        Integer accountRunningNumber = queryResult == null ? Integer.valueOf(0) : (Integer) queryResult;
        return accountRunningNumber + 1;
    }

    public AccountBO findBySystemId(String accountGlobalNum) throws PersistenceException {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("globalAccountNumber", accountGlobalNum);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.FIND_ACCOUNT_BY_SYSTEM_ID, queryParameters);
        return queryResult == null ? null : (AccountBO) queryResult;
    }

    public AccountBO findSavingsByClientGovernmentIdAndProductShortName(String clientGovernmentId,
            String productShortName) throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("clientGovernmentId", clientGovernmentId);
        queryParameters.put("savingsAccountTypeId", AccountTypes.SAVINGS_ACCOUNT.getValue());
        queryParameters.put("productShortName", productShortName);
        Object queryResult = execUniqueResultNamedQuery(
                NamedQueryConstants.FIND_SAVINGS_ACCOUNT_BY_CLIENT_GOVERNMENT_ID_AND_PRODUCT_SHORT_NAME,
                queryParameters);
        return queryResult == null ? null : (AccountBO) queryResult;
    }

    public AccountBO findSavingsByClientPhoneNumberAndProductShortName(String phoneNumber,
            String productShortName) throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("phoneNumberStripped", MifosStringUtils.removeNondigits(phoneNumber));
        queryParameters.put("savingsAccountTypeId", AccountTypes.SAVINGS_ACCOUNT.getValue());
        queryParameters.put("productShortName", productShortName);
        Object queryResult = execUniqueResultNamedQuery(
                NamedQueryConstants.FIND_SAVINGS_ACCOUNT_BY_CLIENT_PHONE_NUMBER_AND_PRODUCT_SHORT_NAME,
                queryParameters);
        return queryResult == null ? null : (AccountBO) queryResult;
    }

    public AccountBO findLoanByClientGovernmentIdAndProductShortName(String clientGovernmentId, String productShortName)
            throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("clientGovernmentId", clientGovernmentId);
        queryParameters.put("loanAccountTypeId", AccountTypes.LOAN_ACCOUNT.getValue());
        queryParameters.put("productShortName", productShortName);
        Object queryResult = execUniqueResultNamedQuery(
                NamedQueryConstants.FIND_LOAN_ACCOUNT_BY_CLIENT_GOVERNMENT_ID_AND_PRODUCT_SHORT_NAME, queryParameters);
        return queryResult == null ? null : (AccountBO) queryResult;
    }

    public AccountBO findLoanByClientPhoneNumberAndProductShortName(String phoneNumber, String productShortName)
            throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("phoneNumberStripped", MifosStringUtils.removeNondigits(phoneNumber));
        queryParameters.put("loanAccountTypeId", AccountTypes.LOAN_ACCOUNT.getValue());
        queryParameters.put("productShortName", productShortName);
        Object queryResult = execUniqueResultNamedQuery(
                NamedQueryConstants.FIND_LOAN_ACCOUNT_BY_CLIENT_PHONE_NUMBER_AND_PRODUCT_SHORT_NAME, queryParameters);
        return queryResult == null ? null : (AccountBO) queryResult;
    }

    public List<AccountBO> findApprovedLoansForClientWithPhoneNumber(String phoneNumber) throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("phoneNumberStripped", MifosStringUtils.removeNondigits(phoneNumber));
        queryParameters.put("loanAccountTypeId", AccountTypes.LOAN_ACCOUNT.getValue());
        return executeNamedQuery(
                NamedQueryConstants.FIND_APPROVED_LOANS_FOR_CLIENT_WITH_PHONE_NUMBER, queryParameters);
    }

    public AccountFeesEntity getAccountFeeEntity(Integer accountFeesEntityId) throws PersistenceException {
        return getPersistentObject(AccountFeesEntity.class, accountFeesEntityId);
    }

    public List<AccountStateEntity> getAccountStates(Short optionalFlag) throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OPTIONAL_FLAG", optionalFlag);
        return executeNamedQuery(NamedQueryConstants.GET_ACCOUNT_STATES, queryParameters);
    }

    public QueryResult getAllAccountNotes(Integer accountId) throws PersistenceException {
        QueryResult notesResult = null;
        try {
            Session session = null;
            notesResult = QueryFactory.getQueryResult("NotesSearch");
            session = StaticHibernateUtil.getSessionTL();
            Query query = session.getNamedQuery(NamedQueryConstants.GETALLACCOUNTNOTES);
            query.setInteger("accountId", accountId);
            notesResult.executeQuery(query);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        return notesResult;
    }

    public List<Integer> getActiveCustomerAndSavingsAccountIdsForGenerateMeetingTask() throws PersistenceException {

        LocalDate date = new LocalDate();
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("DATE", date.toString());
        List<Integer> customerIds = executeNamedQuery("getActiveCustomerAccountIdsForGenerateMeetingsTask",
                queryParameters);
        List<Integer> savingsIds = executeNamedQuery("getActiveSavingsAccountIdsForGenerateMeetingsTask",
                queryParameters);

        customerIds.addAll(savingsIds);
        return customerIds;
    }

    public List<AccountStateEntity> retrieveAllAccountStateList(Short prdTypeId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("prdTypeId", prdTypeId);
        List<AccountStateEntity> queryResult = executeNamedQuery(NamedQueryConstants.RETRIEVEALLACCOUNTSTATES,
                queryParameters);
        initializeAccountStates(queryResult);
        return queryResult;
    }

    public List<AccountStateEntity> retrieveAllActiveAccountStateList(Short prdTypeId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("prdTypeId", prdTypeId);
        queryParameters.put("OPTIONAL_FLAG", Short.valueOf("1"));
        List<AccountStateEntity> queryResult = executeNamedQuery(NamedQueryConstants.RETRIEVEALLACTIVEACCOUNTSTATES,
                queryParameters);
        initializeAccountStates(queryResult);
        return queryResult;
    }

    private void initializeAccountStates(List<AccountStateEntity> queryResult) {
        for (AccountStateEntity accountStateEntity : queryResult) {
            for (AccountStateFlagEntity accountStateFlagEntity : accountStateEntity.getFlagSet()) {
                Hibernate.initialize(accountStateFlagEntity);
                Hibernate.initialize(accountStateFlagEntity.getNames());
            }
            Hibernate.initialize(accountStateEntity.getNames());
        }
    }

    public List<AccountCheckListBO> getStatusChecklist(Short accountStatusId, Short accountTypeId)
            throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("accountTypeId", accountTypeId);
        queryParameters.put("accountStatus", accountStatusId);
        queryParameters.put("checklistStatus", (short) 1);
        return executeNamedQuery(NamedQueryConstants.STATUSCHECKLIST, queryParameters);
    }

    public List<FeeBO> getAllApplicableFees(Integer accountId, FeeCategory categoryType) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("accountId", accountId);

        queryParameters.put("active", FeeStatus.ACTIVE.getValue());
        if (categoryType.getValue().equals(FeeCategory.LOAN.getValue())) {
            queryParameters.put("category", FeeCategory.LOAN.getValue());
            return executeNamedQuery(NamedQueryConstants.GET_ALL_APPLICABLE_LOAN_FEE, queryParameters);
        }
        queryParameters.put("feeFrequencyTypeId", FeeFrequencyType.PERIODIC.getValue());
        queryParameters.put("category1", FeeCategory.ALLCUSTOMERS.getValue());
        queryParameters.put("category2", categoryType.getValue());
        return executeNamedQuery(NamedQueryConstants.GET_ALL_APPLICABLE_CUSTOMER_FEE, queryParameters);
    }

    public QueryResult search(String queryString, Short officeId) throws PersistenceException {

        AccountBO accountBO = findBySystemId(queryString);

        if (accountBO == null) {
            return null;
        }
        if (accountBO.getType() == AccountTypes.CUSTOMER_ACCOUNT
                || accountBO.getType() == AccountTypes.INDIVIDUAL_LOAN_ACCOUNT) {
            return null;
        }
        QueryResult queryResult = QueryFactory.getQueryResult(CustomerSearchConstants.LOANACCOUNTIDSEARCH);
        ((QueryResultAccountIdSearch) queryResult).setSearchString(queryString);
        String[] namedQuery = new String[2];
        List<Param> paramList = new ArrayList<Param>();
        QueryInputs queryInputs = new QueryInputs();
        String[] aliasNames = { "customerId", "centerName", "centerGlobalCustNum", "customerType", "branchGlobalNum",
                "branchName", "loanOfficerName", "loanOffcerGlobalNum", "customerStatus", "groupName",
                "groupGlobalCustNum", "clientName", "clientGlobalCustNum", "loanGlobalAccountNumber" };
        queryInputs.setPath("org.mifos.customers.business.CustomerSearchDto");
        queryInputs.setAliasNames(aliasNames);
        if (officeId != null) {
            if (officeId.shortValue() == 0) {
                namedQuery[0] = NamedQueryConstants.ACCOUNT_ID_SEARCH_NOOFFICEID_COUNT;
                namedQuery[1] = NamedQueryConstants.ACCOUNT_ID_SEARCH_NOOFFICEID;
            } else {
                namedQuery[0] = NamedQueryConstants.ACCOUNT_ID_SEARCH_COUNT;
                namedQuery[1] = NamedQueryConstants.ACCOUNT_ID_SEARCH;
                paramList.add(typeNameValue("Short", "OFFICEID", officeId));
            }
            paramList.add(typeNameValue("String", "SEARCH_STRING", queryString));
        }
        queryInputs.setQueryStrings(namedQuery);
        queryInputs.setParamList(paramList);
        try {
            queryResult.setQueryInputs(queryInputs);
        } catch (HibernateSearchException e) {
            throw new PersistenceException(e);
        }
        return queryResult;

    }

    /*
     * Execute a named query during initialization that does not include logging or other dependencies. This is a
     * workaround for issues related to interdependencies between initialization routines in {@link
     * ApplicationInitializer}
     */
    private List executeNamedQueryAtInit(String queryName, Map queryParameters) throws PersistenceException {
        Session session = null;
        try {
            session = StaticHibernateUtil.getSessionTL();
            Query query = session.getNamedQuery(queryName);
            query.setProperties(queryParameters);
            return query.list();
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Return the COABO (general ledger account) id that corresponds to the GLCode (general ledger code) passed in or
     * return null if no account is found for the glCode.
     */
    public Short getAccountIdFromGlCode(String glCode) {
        return getAccountIdFromGlCode(glCode, false);
    }

    /**
     * This method is equivalent to {@link LegacyAccountDao#getAccountIdFromGlCode(String)} and is only for use during
     * initialization as a workaround for avoiding dependencies on auditing & string resolution during application
     * startup. We should try to refactor the startup code so that this method can be eliminated.
     */
    public Short getAccountIdFromGlCodeDuringInitialization(String glCode) {
        return getAccountIdFromGlCode(glCode, true);
    }

    private Short getAccountIdFromGlCode(String glCode, boolean duringInitialization) {
        // Without this check, Mayfly will fail to execute the query
        // since the query parameter is a "null literal". This affects
        // SurveyIntegrationTest* unit tests.
        // Even though this check is only required for Mayfly, it is
        // good practice anyway to *not* execute a query that will never
        // have any results.
        if (null == glCode) {
            return null;
        }

        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put(AccountConstants.GL_CODE, glCode);
        List queryResult;
        try {

            if (duringInitialization) {
                queryResult = executeNamedQueryAtInit(NamedQueryConstants.GET_ACCOUNT_ID_FOR_GL_CODE, queryParameters);
            } else {
                queryResult = executeNamedQuery(NamedQueryConstants.GET_ACCOUNT_ID_FOR_GL_CODE, queryParameters);
            }
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }

        if (queryResult.size() == 0) {
            return null;
        }
        if (queryResult.size() > 1) {
            throw new RuntimeException("Multiple Account IDs found for GLCode: " + glCode);
        }
        return (Short) queryResult.get(0);
    }

    private static final String GL_ACCOUNT_TAG = ".GLAccount";
    private static final String ASSETS_GL_ACCOUNT_TAG = ".GLAssetsAccount";
    private static final String LIABILITIES_GL_ACCOUNT_TAG = ".GLLiabilitiesAccount";
    private static final String INCOME_GL_ACCOUNT_TAG = ".GLIncomeAccount";
    private static final String EXPENDITURE_GL_ACCOUNT_TAG = ".GLExpenditureAccount";

    private void addAccountSubcategories(XMLConfiguration config, COABO coa, String path) {
        for (COABO subcat : coa.getSubCategoryCOABOs()) {
            config.addProperty(path + "(-1)[@code]", subcat.getAssociatedGlcode().getGlcode());
            config.addProperty(path + "[@name]", subcat.getAccountName());
            addAccountSubcategories(config, subcat, path + GL_ACCOUNT_TAG);
        }

    }
    
    public List<COABO> getCOAlist() {
        Query topLevelAccounts = getSession().getNamedQuery(NamedQueryConstants.GET_TOP_LEVEL_ACCOUNTS);
        return topLevelAccounts.list();
    }
    
    public List<COABO> getCOAChildList(int id) {
        Query accounts = getSession().getNamedQuery(NamedQueryConstants.GET_CHILD_ACCOUNTS);
        accounts.setInteger("id", id);
        return accounts.list();
    }

    public String dumpChartOfAccounts() throws ConfigurationException {
        XMLConfiguration config = new XMLConfiguration();
        Query topLevelAccounts = getSession().getNamedQuery(NamedQueryConstants.GET_TOP_LEVEL_ACCOUNTS);
        List listAccounts = topLevelAccounts.list();
        Iterator it = listAccounts.iterator();
        LegacyAccountDao ap = new LegacyAccountDao();
        String assetsAccountGLCode = ap.getCategory(GLCategoryType.ASSET).getGlCode();
        String liabilitiesAccountGLCode = ap.getCategory(GLCategoryType.LIABILITY).getGlCode();
        String incomeAccountGLCode = ap.getCategory(GLCategoryType.INCOME).getGlCode();
        String expenditureAccountGLCode = ap.getCategory(GLCategoryType.EXPENDITURE).getGlCode();
        while (it.hasNext()) {
            COABO coa = (COABO) it.next();
            String name = coa.getAccountName();
            String glCode = coa.getAssociatedGlcode().getGlcode();
            String path = "ChartOfAccounts";
            if (glCode.equals(assetsAccountGLCode)) {
                path = path + ASSETS_GL_ACCOUNT_TAG;
            } else if (glCode.equals(liabilitiesAccountGLCode)) {
                path = path + LIABILITIES_GL_ACCOUNT_TAG;
            } else if (glCode.equals(incomeAccountGLCode)) {
                path = path + INCOME_GL_ACCOUNT_TAG;
            } else if (glCode.equals(expenditureAccountGLCode)) {
                path = path + EXPENDITURE_GL_ACCOUNT_TAG;
            } else {
                throw new RuntimeException("Unrecognized top level GLCode: " + glCode);
            }
            config.addProperty(path + "(-1)[@code]", glCode);
            config.addProperty(path + "[@name]", name);
            addAccountSubcategories(config, coa, path + GL_ACCOUNT_TAG);
        }
        StringWriter stringWriter = new StringWriter();
        config.save(stringWriter);
        String chart = stringWriter.toString();

        return chart;
    }

    /**
     * Persists given entities required to represent a new general ledger account. Does not confirm that given account
     * does not already exist in database. It appears that accounts are unique only by GL code.
     */
    public COABO addGeneralLedgerAccount(String name, String glCode, String parentGLCode, GLCategoryType categoryType) {
        return addGeneralLedgerAccount(name, glCode, getAccountIdFromGlCode(parentGLCode), categoryType);
    }

    /**
     * @see #addGeneralLedgerAccount(String, String, String, GLCategoryType)
     */
    public COABO addGeneralLedgerAccount(String name, String glcode, Short parent_id, GLCategoryType categoryType) {
        Short id = getAccountIdFromGlCode(glcode);
        if (id != null) {
            throw new MifosRuntimeException("An account already exists with glcode: " + glcode + ". id was " + id);
        }

        GLCodeEntity glCodeEntity = new GLCodeEntity(null, glcode);
        try {
            createOrUpdate(glCodeEntity);
            COABO newAccount = new COABO(name, glCodeEntity);
            newAccount.setCategoryType(categoryType);
            createOrUpdate(newAccount);

            COABO parentCOA;
            COAHierarchyEntity coaHierarchyEntity = null;
            if (null == parent_id) {
                coaHierarchyEntity = new COAHierarchyEntity(newAccount, null);
            } else {
                parentCOA = (COABO) StaticHibernateUtil.getSessionTL().load(COABO.class, parent_id.shortValue());
                coaHierarchyEntity = new COAHierarchyEntity(newAccount, parentCOA.getCoaHierarchy());
            }

            createOrUpdate(coaHierarchyEntity);
            newAccount.setCoaHierarchy(coaHierarchyEntity);
            StaticHibernateUtil.commitTransaction();
            return newAccount;
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    public int getCountForGlCode(Short glCodeId) throws PersistenceException {
        Session session = StaticHibernateUtil.getSessionTL();

        int count = -1;
        try {
            Query query = session.getNamedQuery(NamedQueryConstants.COUNT_GL_CODE_REFERENCES);
            query.setShort("glCodeId", glCodeId);
            count = (Integer) query.uniqueResult();
        } catch (HibernateException ex) {
            throw new PersistenceException(ex);
        }
        
        return count;
    }
    
    /**
     * A "category" is a top-level general ledger account. Use this method to fetch a single, specific category from the
     * database.
     */
    public COABO getCategory(GLCategoryType categoryType) {
        Query topLevelAccount = getSession().getNamedQuery(NamedQueryConstants.GET_TOP_LEVEL_ACCOUNT);
        topLevelAccount.setParameter("categoryType", categoryType.toString());
        return (COABO) topLevelAccount.uniqueResult();
    }

    public void updateAccountName(COABO account, String newName) {
        account.setAccountName(newName);
        try {
            createOrUpdate(account);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CustomerBO> getCoSigningClientsForGlim(Integer accountId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put(QueryParamConstants.ACCOUNT_ID, accountId);
        List<LoanBO> loans = executeNamedQuery(NamedQueryConstants.GET_COSIGNING_CLIENTS_FOR_GLIM, queryParameters);
        List<CustomerBO> clients = new ArrayList<CustomerBO>();
        for (LoanBO loanBO : loans) {
            clients.add(loanBO.getCustomer());
        }
        return clients;
    }

    public void save(List<AccountBO> customerAccounts) {

        Session session = StaticHibernateUtil.getSessionTL();
        for (AccountBO account : customerAccounts) {
            session.save(account);
        }
    }

    public void save(AccountOverpaymentEntity overpaymentEntity) {
        StaticHibernateUtil.getSessionTL().save(overpaymentEntity);
    }
    
    public void save(GuarantyEntity guarantyEntity) {
        StaticHibernateUtil.getSessionTL().save(guarantyEntity);
    }

    private List<Object[]> getListOfAccountIdsHavingSchedulesWithinAHoliday(final String queryName, final Holiday holiday)
            throws PersistenceException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("HOLIDAY_ID", holiday.getId());
        parameters.put("HOLIDAY_FROM_DATE", DateUtils.getLocalDateFromDate(holiday.getFromDate().toDate()).toString());
        parameters.put("HOLIDAY_THRU_DATE", DateUtils.getLocalDateFromDate(holiday.getThruDate().toDate()).toString());

        List<Object[]> queryResult = executeNamedQuery(queryName, parameters);
        if (queryResult != null) {
            return queryResult;
        }

        return new ArrayList<Object[]>();
    }

    public List<Object[]> getListOfAccountIdsHavingLoanSchedulesWithinAHoliday(final Holiday holiday)
            throws PersistenceException {

        return getListOfAccountIdsHavingSchedulesWithinAHoliday("getListOfAccountIdsHavingLoanSchedulesWithinAHoliday",
                holiday);
    }

    public List<Object[]> getListOfAccountIdsHavingSavingsSchedulesWithinAHoliday(final Holiday holiday)
            throws PersistenceException {

        return getListOfAccountIdsHavingSchedulesWithinAHoliday(
                "getListOfAccountIdsHavingSavingsSchedulesWithinAHoliday", holiday);
    }

    public List<Object[]> getListOfAccountIdsHavingCustomerSchedulesWithinAHoliday(final Holiday holiday)
            throws PersistenceException {

        return getListOfAccountIdsHavingSchedulesWithinAHoliday(
                "getListOfAccountIdsHavingCustomerSchedulesWithinAHoliday", holiday);
    }

    public List<LoanScheduleEntity> getLoanSchedulesForAccountThatAreWithinDates(Integer accountId, DateTime fromDate,
            DateTime thruDate) throws PersistenceException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ACCOUNT_ID", accountId);
        parameters.put("FROM_DATE", DateUtils.getLocalDateFromDate(fromDate.toDate()).toString());
        parameters.put("THRU_DATE", DateUtils.getLocalDateFromDate(thruDate.toDate()).toString());

        return executeNamedQuery("getLoanSchedulesForAccountThatAreWithinDates", parameters);

    }

    public List<SavingsScheduleEntity> getSavingsSchedulesForAccountThatAreWithinDates(Integer accountId,
            DateTime fromDate, DateTime thruDate) throws PersistenceException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ACCOUNT_ID", accountId);
        parameters.put("FROM_DATE", DateUtils.getLocalDateFromDate(fromDate.toDate()).toString());
        parameters.put("THRU_DATE", DateUtils.getLocalDateFromDate(thruDate.toDate()).toString());

        return executeNamedQuery("getSavingsSchedulesForAccountThatAreWithinDates", parameters);
    }

    public List<CustomerScheduleEntity> getCustomerSchedulesForAccountThatAreWithinDates(Integer accountId,
            DateTime fromDate, DateTime thruDate) throws PersistenceException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ACCOUNT_ID", accountId);
        parameters.put("FROM_DATE", DateUtils.getLocalDateFromDate(fromDate.toDate()).toString());
        parameters.put("THRU_DATE", DateUtils.getLocalDateFromDate(thruDate.toDate()).toString());

        return executeNamedQuery("getCustomerSchedulesForAccountThatAreWithinDates", parameters);
    }

    public List<AccountPaymentEntity> findAccountPaymentsByReceiptNumber(String receiptNumber) throws PersistenceException {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("RECEIPT_NUMBER", receiptNumber);

         return executeNamedQuery("findAccountPaymentsByReceiptNumber", parameters);
    }

    public AccountOverpaymentEntity findOverpaymentById(Integer overpaymentId) throws PersistenceException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("overpaymentId", overpaymentId);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.FIND_OVERPAYMENT_BY_ID, parameters);
        return queryResult == null ? null : (AccountOverpaymentEntity) queryResult;
    }

    public AccountPaymentEntity findPaymentById(Integer paymentId) throws PersistenceException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("paymentId", paymentId);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.FIND_PAYMENT_BY_ID, parameters);
        return (queryResult == null) ? null : (AccountPaymentEntity) queryResult;
    }

    public void updatePayment(AccountPaymentEntity payment) {
        StaticHibernateUtil.getSessionTL().update(payment);
    }
    
    public void deleteLedgerAccount(Short accountId) throws PersistenceException {
        Session session = StaticHibernateUtil.getSessionTL();
        Transaction transaction = session.beginTransaction();
        try {

            COABO coa = (COABO) session.load(COABO.class, accountId);
            COABO parent = coa.getCoaHierarchy().getParentAccount().getCoa();

            Short parentId = parent.getAccountId();
            Short glCodeId = coa.getAssociatedGlcode().getGlcodeId();
            
            Query query = session.getNamedQuery(NamedQueryConstants.REMOVE_COA_PARENT);
            query.setShort("coa_id", coa.getAccountId().shortValue());
            query.setShort("parent_id", parentId);
            query.executeUpdate();
            
            query = session.getNamedQuery(NamedQueryConstants.REMOVE_COA);
            query.setShort("coa_id", coa.getAccountId().shortValue());
            query.executeUpdate();
            
            query = session.getNamedQuery(NamedQueryConstants.REMOVE_GLCODE);
            query.setShort("glcode_id", glCodeId);
            query.executeUpdate();
            
            transaction.commit();
            session.flush();

        } catch (HibernateException e) {
            transaction.rollback();
            throw new PersistenceException(e);
        }
    }

    public void updateLedgerAccount(COABO coaBo, String accountName, String glCode, String parentGlCode) throws PersistenceException {
        Session session = StaticHibernateUtil.getSessionTL();
        Transaction transaction = session.beginTransaction();
        
        try {
            Short newParentId = getAccountIdFromGlCode(parentGlCode);
                    
            coaBo.setAccountName(accountName);
            GLCodeEntity glCodeEntity = coaBo.getAssociatedGlcode();
           
            createOrUpdate(coaBo);
            
            glCodeEntity.setGlcode(glCode);
            createOrUpdate(glCodeEntity);
            
            Query query = session.getNamedQuery(NamedQueryConstants.SET_COA_PARENT);
            query.setShort("parentId", newParentId);
            query.setShort("id", coaBo.getAccountId());
            query.executeUpdate();
            
            transaction.commit();
        } catch (HibernateException ex) {
            transaction.rollback();
            throw new PersistenceException(ex);
        }
        
    }
    
    public List<GuarantyEntity> getGuarantyByLoanIdAndGuarantorId(Integer guarantorId, Integer loanId) throws PersistenceException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("loanId", loanId);
        parameters.put("guarantorId", guarantorId);
        return executeNamedQuery(NamedQueryConstants.FIND_GUARANTY_BY_LOANID_AND_GUARANTORID, parameters);
    }
    
    public List<GuarantyEntity> getGuarantyByLoanId (Integer loanId) throws PersistenceException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("loanId", loanId);
        return executeNamedQuery(NamedQueryConstants.FIND_GUARANTY_BY_LOANID, parameters);
    }
    
    public List<GuarantyEntity> getGuarantyByGurantorId (Integer guarantorId) throws PersistenceException {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("guarantorId", guarantorId);
        return executeNamedQuery(NamedQueryConstants.FIND_GUARANTY_BY_GUARANTORID, parameters);
    }
}
