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

package org.mifos.accounts.savings.persistence;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.mifos.accounts.business.AccountBO;
import org.mifos.accounts.business.AccountStateEntity;
import org.mifos.accounts.exceptions.AccountException;
import org.mifos.accounts.savings.business.SavingsBO;
import org.mifos.accounts.savings.business.SavingsTrxnDetailEntity;
import org.mifos.accounts.util.helpers.AccountStates;
import org.mifos.accounts.util.helpers.AccountTypes;
import org.mifos.accounts.util.helpers.PaymentStatus;
import org.mifos.application.NamedQueryConstants;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.persistence.LegacyGenericDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsPersistence extends LegacyGenericDao {

    private static final Logger logger = LoggerFactory.getLogger(SavingsPersistence.class);

    public SavingsTrxnDetailEntity retrieveLastTransaction(Integer accountId, Date date) throws PersistenceException {

        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("accountId", accountId);
        queryParameters.put("date", date);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.RETRIEVE_LAST_TRXN, queryParameters);
        return queryResult == null ? null : (SavingsTrxnDetailEntity) queryResult;
    }

    public SavingsTrxnDetailEntity retrieveFirstTransaction(Integer accountId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("accountId", accountId);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.RETRIEVE_FIRST_TRXN, queryParameters);
        return queryResult == null ? null : (SavingsTrxnDetailEntity) queryResult;
    }

    public AccountStateEntity getAccountStatusObject(Short accountStatusId) throws PersistenceException {
        logger.debug("In SavingsPersistence::getAccountStatusObject(), accountStatusId: " + accountStatusId);
        return getPersistentObject(AccountStateEntity.class, accountStatusId);
    }

    @SuppressWarnings("unchecked")
    public List<SavingsBO> getAllClosedAccount(Integer customerId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("customerId", customerId);
        List queryResult = executeNamedQuery(NamedQueryConstants.VIEWALLSAVINGSCLOSEDACCOUNTS, queryParameters);
        return queryResult;
    }

    @SuppressWarnings("unchecked")
    public int getMissedDeposits(Integer accountId, Date currentDate) throws PersistenceException {
        Integer count = 0;
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("ACCOUNT_ID", accountId);
        queryParameters.put("ACCOUNT_TYPE_ID", AccountTypes.SAVINGS_ACCOUNT.getValue());
        queryParameters.put("ACTIVE", AccountStates.SAVINGS_ACC_APPROVED);
        queryParameters.put("CHECKDATE", currentDate);
        queryParameters.put("PAYMENTSTATUS", PaymentStatus.UNPAID.getValue());

        List queryResult = executeNamedQuery(NamedQueryConstants.GET_MISSED_DEPOSITS_COUNT, queryParameters);

        if (null != queryResult && queryResult.size() > 0) {
            Object obj = queryResult.get(0);
            if (obj != null) {
                count = ((Number) obj).intValue();
            }
        }
        return count.intValue();
    }

    @SuppressWarnings("unchecked")
    public int getMissedDepositsPaidAfterDueDate(Integer accountId) throws PersistenceException {
        Long count = 0L;
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("ACCOUNT_ID", accountId);
        queryParameters.put("ACCOUNT_TYPE_ID", AccountTypes.SAVINGS_ACCOUNT.getValue());
        queryParameters.put("ACTIVE", AccountStates.SAVINGS_ACC_APPROVED);
        queryParameters.put("PAYMENTSTATUS", PaymentStatus.PAID.getValue());

        List queryResult = executeNamedQuery(NamedQueryConstants.GET_MISSED_DEPOSITS_PAID_AFTER_DUEDATE,
                queryParameters);

        if (null != queryResult && queryResult.size() > 0) {
            Object obj = queryResult.get(0);
            if (obj != null) {
                count = (Long) obj;
            }
        }
        return count.intValue();
    }

    @SuppressWarnings("unchecked")
    public AccountBO getSavingsAccountWithAccountActionsInitialized(Integer accountId) throws PersistenceException {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("accountId", accountId);
        List obj = executeNamedQuery("accounts.retrieveSavingsAccountWithAccountActions", queryParameters);
        Object[] obj1 = (Object[]) obj.get(0);
        return (AccountBO) obj1[0];
    }

    public void persistSavingAccounts(ClientBO clientBO) throws CustomerException {
        for (AccountBO account : clientBO.getAccounts()) {
            if (account.getType() == AccountTypes.SAVINGS_ACCOUNT && account.getGlobalAccountNum() == null) {
                try {
                    ((SavingsBO) account).save();
                } catch (AccountException ae) {
                    throw new CustomerException(ae);
                }
            }
        }
    }

    public void initialize(SavingsBO savings) {
        Hibernate.initialize(savings);
        Hibernate.initialize(savings.getCustomer());
        Hibernate.initialize(savings.getCustomer().getOffice());
        Hibernate.initialize(savings.getCustomer().getParentCustomer());
        if (savings.getCustomer().getParentCustomer() != null) {
            Hibernate.initialize(savings.getCustomer().getParentCustomer().getOffice());
        }
    }
}
