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

package org.mifos.accounts.productdefinition.business;

import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.framework.TestUtils;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoanAmountOptionTest {

    @Mock
    private LoanOfferingBO loanProduct;

    /**
     */
    @Ignore
    @Test
    public void testLoanAmountOption() {
        when(loanProduct.getCurrency()).thenReturn(TestUtils.RUPEE);
         LoanAmountOption l = new LoanAmountOptionImpl(123456789000.00,123456789000.0,123456789000.000,loanProduct);
         Assert.assertEquals("123456789000.0", l.getDefaultLoanAmountString());
         Assert.assertEquals("123456789000.0", l.getMaxLoanAmountString());
         Assert.assertEquals("123456789000.0", l.getMinLoanAmountString());
    }

    class LoanAmountOptionImpl extends LoanAmountOption {

        public LoanAmountOptionImpl(Double minLoanAmount, Double maxLoanAmount, Double defaultLoanAmount,
                LoanOfferingBO loanOffering) {
            super(minLoanAmount, maxLoanAmount, defaultLoanAmount, loanOffering);
        }
    }
}
