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

package org.mifos.accounts.acceptedpaymenttype.persistence;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mifos.accounts.AccountIntegrationTestCase;
import org.mifos.accounts.acceptedpaymenttype.business.AcceptedPaymentType;
import org.mifos.accounts.acceptedpaymenttype.business.TransactionTypeEntity;
import org.mifos.accounts.acceptedpaymenttype.persistence.helper.TransactionAcceptedPaymentTypes;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.master.util.helpers.PaymentTypes;
import org.mifos.application.util.helpers.TrxnTypes;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AcceptedPaymentTypePersistenceIntegrationTest extends AccountIntegrationTestCase {

    private List<TransactionAcceptedPaymentTypes> currentAcceptedPaymentTypes = null;

    @Autowired
    private LegacyAcceptedPaymentTypeDao legacyAcceptedPaymentTypeDao;

    private List<TransactionAcceptedPaymentTypes> allAcceptedPaymentTypes = null;
    private Short DEFAULT_LOCALE_ID = 1;

    @Override
    @Before
    public void setUp() throws Exception {
        StaticHibernateUtil.startTransaction();
    }

    @Test
    public void testGetAcceptedPaymentTypes() throws Exception {
        // get all accepted payment types and store in
        // currentAcceptedPaymentTypes
        currentAcceptedPaymentTypes = new ArrayList<TransactionAcceptedPaymentTypes>();
        allAcceptedPaymentTypes = new ArrayList<TransactionAcceptedPaymentTypes>();
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            List<AcceptedPaymentType> acceptedPaymentTypes = legacyAcceptedPaymentTypeDao
                    .getAcceptedPaymentTypesForATransaction(transactionType.getValue(), TrxnTypes.loan_repayment);
            TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes = new TransactionAcceptedPaymentTypes();
            transactionAcceptedPaymentTypes.setAcceptedPaymentTypes(acceptedPaymentTypes);
            transactionAcceptedPaymentTypes.setTransactionType(transactionType);
            currentAcceptedPaymentTypes.add(transactionAcceptedPaymentTypes);
            TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes2 = new TransactionAcceptedPaymentTypes();
            List<AcceptedPaymentType> acceptedPaymentTypes2 = new ArrayList<AcceptedPaymentType>(acceptedPaymentTypes);
            transactionAcceptedPaymentTypes2.setAcceptedPaymentTypes(acceptedPaymentTypes2);
            transactionAcceptedPaymentTypes2.setTransactionType(transactionType);
            allAcceptedPaymentTypes.add(transactionAcceptedPaymentTypes2);
        }
    }

    private boolean Find(PaymentTypes paymentType, List<AcceptedPaymentType> acceptedPaymentTypes) {
        for (AcceptedPaymentType acceptedPaymentType : acceptedPaymentTypes) {
            if (acceptedPaymentType.getPaymentTypeEntity().getId().shortValue() == paymentType.getValue().shortValue()) {
                return true;
            }
        }
        return false;
    }

    private List<AcceptedPaymentType> GetSavePaymentTypes(TrxnTypes transType) {
        for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : allAcceptedPaymentTypes) {
            if (transType.equals(transactionAcceptedPaymentTypes.getTransactionType())) {
                return transactionAcceptedPaymentTypes.getAcceptedPaymentTypes();
            }
        }
        return null;
    }

    private void addAcceptedPaymentTypeForATransaction(List<AcceptedPaymentType> addAcceptedPaymentTypes,
            TrxnTypes transactionType) throws Exception {

        for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : currentAcceptedPaymentTypes) {
            TrxnTypes transType = transactionAcceptedPaymentTypes.getTransactionType();
            if (transType.equals(transactionType)) {
                List<AcceptedPaymentType> paymentTypes = GetSavePaymentTypes(transType);
                List<AcceptedPaymentType> acceptedPaymentTypes = transactionAcceptedPaymentTypes
                        .getAcceptedPaymentTypes();
                if ((acceptedPaymentTypes != null) && (acceptedPaymentTypes.size() > 0)) {
                    for (PaymentTypes paymentType : PaymentTypes.values()) {
                        if (Find(paymentType, acceptedPaymentTypes) == false) {
                            AcceptedPaymentType acceptedPaymentType = new AcceptedPaymentType();
                            Short paymentTypeId = paymentType.getValue();
                            PaymentTypeEntity paymentTypeEntity = new PaymentTypeEntity(paymentTypeId);
                            acceptedPaymentType.setPaymentTypeEntity(paymentTypeEntity);
                            TransactionTypeEntity transactionEntity = new TransactionTypeEntity();
                            transactionEntity.setTransactionId(transType.getValue());
                            acceptedPaymentType.setTransactionTypeEntity(transactionEntity);
                            addAcceptedPaymentTypes.add(acceptedPaymentType);
                            paymentTypes.add(acceptedPaymentType);
                        }
                    }
                }
            }
        }
    }

    private void verify(List<AcceptedPaymentType> savedAcceptedPaymentTypes,
            List<AcceptedPaymentType> acceptedPaymentTypes) {
       Assert.assertTrue(savedAcceptedPaymentTypes.size() == acceptedPaymentTypes.size());
        for (AcceptedPaymentType acceptedPaymentType : savedAcceptedPaymentTypes) {
           Assert.assertTrue(FindAcceptedPaymentType(acceptedPaymentType, acceptedPaymentTypes));
        }
    }

    private boolean FindAcceptedPaymentType(AcceptedPaymentType acceptedPaymentType,
            List<AcceptedPaymentType> acceptedPaymentTypes) {
        for (AcceptedPaymentType newAcceptedPaymentType : acceptedPaymentTypes) {
            if ((newAcceptedPaymentType.getTransactionTypeEntity().getTransactionId().shortValue() == acceptedPaymentType
                    .getTransactionTypeEntity().getTransactionId().shortValue())
                    && (newAcceptedPaymentType.getPaymentTypeEntity().getId().shortValue()) == acceptedPaymentType
                            .getPaymentTypeEntity().getId().shortValue()) {
                return true;
            }
        }
        return false;
    }

    private List<AcceptedPaymentType> GetBeforeTestPaymentTypes(TrxnTypes transType) {
        for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : currentAcceptedPaymentTypes) {
            if (transType.equals(transactionAcceptedPaymentTypes.getTransactionType())) {
                return transactionAcceptedPaymentTypes.getAcceptedPaymentTypes();
            }
        }
        return null;
    }

    private boolean IsDeleted(AcceptedPaymentType a, List<AcceptedPaymentType> list) {
        if ((list == null) || (list.size() == 0)) {
            return true;
        }
        for (AcceptedPaymentType type : list) {
            if ((type.getTransactionTypeEntity().getTransactionId().shortValue() == a.getTransactionTypeEntity()
                    .getTransactionId().shortValue())
                    && (type.getPaymentTypeEntity().getId().shortValue() == a.getPaymentTypeEntity().getId()
                            .shortValue())) {
                return false;
            }
        }
        return true;
    }

    private void deleteAcceptedPaymentTypeForATransaction(List<AcceptedPaymentType> deleteAcceptedPaymentTypes,
            TrxnTypes transactionType) throws Exception {

        List<AcceptedPaymentType> acceptedPaymentTypesFromDB = legacyAcceptedPaymentTypeDao
                .getAcceptedPaymentTypesForATransaction(transactionType.getValue(), TrxnTypes.loan_repayment);
        List<AcceptedPaymentType> acceptedPaymentTypes = GetBeforeTestPaymentTypes(transactionType);
        for (AcceptedPaymentType a : acceptedPaymentTypesFromDB) {
            if (IsDeleted(a, acceptedPaymentTypes)) {
                deleteAcceptedPaymentTypes.add(a);
            }
        }

    }

    @Test
    public void testAddDeleteAcceptedPaymentTypes() throws Exception {
        testGetAcceptedPaymentTypes();
        List<AcceptedPaymentType> addAcceptedPaymentTypes = new ArrayList<AcceptedPaymentType>();
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            addAcceptedPaymentTypeForATransaction(addAcceptedPaymentTypes, transactionType);
        }
        if (addAcceptedPaymentTypes.size() > 0) {
            legacyAcceptedPaymentTypeDao.addAcceptedPaymentTypes(addAcceptedPaymentTypes);
        }
        // verify results
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            List<AcceptedPaymentType> acceptedPaymentTypes = legacyAcceptedPaymentTypeDao
                    .getAcceptedPaymentTypesForATransaction(transactionType.getValue(), TrxnTypes.loan_repayment);
            List<AcceptedPaymentType> savedAcceptedPaymentTypes = GetSavePaymentTypes(transactionType);
            verify(savedAcceptedPaymentTypes, acceptedPaymentTypes);
        }

        // delete the added records to get back to before tests
        List<AcceptedPaymentType> deleteAcceptedPaymentTypes = new ArrayList<AcceptedPaymentType>();
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            deleteAcceptedPaymentTypeForATransaction(deleteAcceptedPaymentTypes, transactionType);
        }
        if (deleteAcceptedPaymentTypes.size() > 0) {
            legacyAcceptedPaymentTypeDao.deleteAcceptedPaymentTypes(deleteAcceptedPaymentTypes);
        }
        // verify results
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            List<AcceptedPaymentType> acceptedPaymentTypes = legacyAcceptedPaymentTypeDao
                    .getAcceptedPaymentTypesForATransaction(transactionType.getValue(), TrxnTypes.loan_repayment);
            List<AcceptedPaymentType> savedAcceptedPaymentTypes = GetBeforeTestPaymentTypes(transactionType);
            verify(savedAcceptedPaymentTypes, acceptedPaymentTypes);
        }

    }

    private boolean FindEntity(List<PaymentTypeEntity> entityList, PaymentTypeEntity entity) {
        for (PaymentTypeEntity e : entityList) {
            if ((e.getId().shortValue() == entity.getId().shortValue()) && (e.getName().equals(entity.getName()))) {
                return true;
            }
        }
        return false;
    }

    private void compare(List<PaymentTypeEntity> entityList, List<AcceptedPaymentType> acceptedPaymentTypeList) {
       Assert.assertTrue(entityList.size() == acceptedPaymentTypeList.size());
        for (AcceptedPaymentType acceptedPaymentType : acceptedPaymentTypeList) {
            Assert.assertTrue(FindEntity(entityList, acceptedPaymentType.getPaymentTypeEntity()));
        }

    }

    @Test
    public void testRetrieveAcceptedPaymentTypes() throws Exception {
        for (TrxnTypes transactionType : TrxnTypes.values()) {
            Short transactionId = transactionType.getValue();
            List<AcceptedPaymentType> acceptedPaymentTypes = legacyAcceptedPaymentTypeDao
                    .getAcceptedPaymentTypesForATransaction(transactionId, TrxnTypes.loan_repayment);
            List<PaymentTypeEntity> paymentTypeEntities = legacyAcceptedPaymentTypeDao
                    .getAcceptedPaymentTypesForATransaction(DEFAULT_LOCALE_ID, transactionId);
            compare(paymentTypeEntities, acceptedPaymentTypes);
        }

    }

}
