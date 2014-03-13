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

package org.mifos.customers.client.business;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.accounts.loan.business.LoanBO;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.Money;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientPerformanceHistoryEntityTest {

    private ClientPerformanceHistoryEntity clientPerformanceHistoryEntity;

    @Mock
    private LoanBO loan;

    @Mock
    private ClientBO client;

    private static MifosCurrency oldDefaultCurrency;

    @BeforeClass
    public static void initialiseHibernateUtil() {

        oldDefaultCurrency = Money.getDefaultCurrency();
        Money.setDefaultCurrency(TestUtils.RUPEE);
    }

    @AfterClass
    public static void resetCurrency() {
        Money.setDefaultCurrency(oldDefaultCurrency);
    }


    @Test
    public void shouldUpdateLastLoanAmountWhenLoanIsFullyPaid() throws Exception {

        Money loanAmount = new Money(Money.getDefaultCurrency(), "55.6");
        // setup
        clientPerformanceHistoryEntity = new ClientPerformanceHistoryEntity(client);

        when(loan.getLoanAmount()).thenReturn(loanAmount);

        // exercise test
        clientPerformanceHistoryEntity.updateOnFullRepayment(loanAmount);

        // verification
        assertThat(clientPerformanceHistoryEntity.getLastLoanAmount(), is(notNullValue()));
        assertThat(clientPerformanceHistoryEntity.getLastLoanAmount().getAmountDoubleValue(), is(loanAmount.getAmountDoubleValue()));
    }

    @Test
    public void shouldAddOneToNoOfActiveLoansWhenIncremented() throws Exception {

        // setup
        clientPerformanceHistoryEntity = new ClientPerformanceHistoryEntity(client);
        clientPerformanceHistoryEntity.setNoOfActiveLoans(5);
        // exercise test
        clientPerformanceHistoryEntity.incrementNoOfActiveLoans();

        // verification
        assertThat(clientPerformanceHistoryEntity.getNoOfActiveLoans(), is(6));
    }

    @Test
    public void shouldSubtractOneFromNoOfActiveLoansWhenDecremented() throws Exception {

        // setup
        clientPerformanceHistoryEntity = new ClientPerformanceHistoryEntity(client);
        clientPerformanceHistoryEntity.setNoOfActiveLoans(5);
        // exercise test
        clientPerformanceHistoryEntity.decrementNoOfActiveLoans();

        // verification
        assertThat(clientPerformanceHistoryEntity.getNoOfActiveLoans(), is(4));
    }
}