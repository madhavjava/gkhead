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

package org.mifos.framework.hibernate.helper;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.business.CustomerSearchDto;
import org.mifos.customers.util.helpers.CustomerSearchConstants;
import org.mifos.framework.exceptions.HibernateSearchException;

public class QueryResultAccountIdSearch extends QueryResultsMainSearchImpl {

    String searchString = null;

    @Override
    public List<Object> get(int position, int noOfObjects) throws HibernateSearchException {
        List<Object> returnList = new ArrayList<Object>();

        try {
            Session session = StaticHibernateUtil.getSessionTL();
            Query query = prepareQuery(session, queryInputs.getQueryStrings()[1]);
            list = query.list();
            this.queryInputs.setTypes(query.getReturnTypes());
            dtoBuilder.setInputs(queryInputs);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (buildDTO) {
                        Object record = buildDTO((Object[]) list.get(i));
                        CustomerSearchDto search = (CustomerSearchDto) record;
                        Integer customerId = search.getCustomerId();
                        short customerLevel = search.getCustomerType();
                        query = session.getNamedQuery(NamedQueryConstants.ACCOUNT_LIST_ID_SEARCH);
                        query.setInteger("customerId", customerId).setShort("loanAccountTypeId",
                                CustomerSearchConstants.LOAN_TYPE);
                        query.setShort("groupLoanAccountTypeId",CustomerSearchConstants.GROUP_LOAN_TYPE);
                        query.setShort("savingsAccountTypeId", CustomerSearchConstants.SAVINGS_TYPE);
                        query.setString("searchString", searchString);
                        List<?> accountNumAndTypeId = query.list();
                        Object[] obj2 = (Object[]) accountNumAndTypeId.get(0);
                        search.setLoanGlobalAccountNumber(obj2[0].toString());
                        Short accountTypeId = (Short) obj2[1];

                        search.setCustomerType(deduceCustomerType(customerLevel, accountTypeId));
                        returnList.add(search);
                    } else {
                        if (i < noOfObjects) {
                            returnList.add(list.get(i));
                        }
                    }
                }
            }
            StaticHibernateUtil.closeSession();
        } catch (Exception e) {
            throw new HibernateSearchException(HibernateConstants.SEARCH_FAILED, e);
        }
        return returnList;
    }

    private Short deduceCustomerType(short customerLevel, Short accountTypeId) {
        if (accountTypeId != null && customerLevel == CustomerLevel.CLIENT.getValue()
                && accountTypeId == CustomerSearchConstants.LOAN_TYPE) {
            return (short) 4;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.CLIENT.getValue()
                && accountTypeId == CustomerSearchConstants.SAVINGS_TYPE) {
            return (short) 6;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.GROUP.getValue()
                && accountTypeId == CustomerSearchConstants.LOAN_TYPE) {
            return (short) 5;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.GROUP.getValue()
                && accountTypeId == CustomerSearchConstants.SAVINGS_TYPE) {
            return (short) 7;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.CENTER.getValue()
                && accountTypeId == CustomerSearchConstants.SAVINGS_TYPE) {
            return (short) 8;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.GROUP.getValue()
                && accountTypeId == CustomerSearchConstants.GROUP_LOAN_TYPE) {
            return (short) 9;
        } else if (accountTypeId != null && customerLevel == CustomerLevel.CLIENT.getValue()
                && accountTypeId == CustomerSearchConstants.GROUP_LOAN_TYPE) {
            return (short) 10;
        } else {
            return null; // or exception?
        }
    }


    //---Watch out for closeSession block as the cne commendted out here is no longer in codebase
    /*
     * public List accountIdSearch(String searchString,Short officeId) throws
     * SystemException { this.searchString = searchString;
     *
     * try{ Session session=null; session= QuerySession.getSession(); Query
     * query=null; if( officeId.shortValue()==0) {
     * query=session.getNamedQuery(NamedQueryConstants
     * .ACCOUNT_ID_SEARCH_NOOFFICEID);
     * query.setString("SEARCH_STRING",searchString); } else {
     *
     * query=session.getNamedQuery(NamedQueryConstants.ACCOUNT_ID_SEARCH);
     * query.setString("SEARCH_STRING",searchString);
     * query.setShort("OFFICEID",officeId); }
     *
     *
     * list=query.list(); this.queryInputs.setTypes(query.getReturnTypes());
     * dtoBuilder.setInputs(queryInputs); QuerySession.closeSession(session); }
     * catch(HibernateProcessException hpe) { throw new SystemException(); }
     * return list; }
     */

    @Override
    public int getSize() throws HibernateSearchException {
        try {
            Session session = StaticHibernateUtil.getSessionTL();
            if (this.queryInputs == null) {
                throw new HibernateSearchException(HibernateConstants.SEARCH_INPUTNULL);
            }
            Query query = prepareQuery(session, queryInputs.getQueryStrings()[0]);
            Integer resultSetCount = ((Number) query.uniqueResult()).intValue();
            this.queryInputs.setTypes(query.getReturnTypes());
            dtoBuilder.setInputs(queryInputs);
            if (resultSetCount != null && resultSetCount > 0) {
                size = resultSetCount;
            }
            StaticHibernateUtil.closeSession();
        } catch (Exception e) {
            throw new HibernateSearchException(HibernateConstants.SEARCH_FAILED, e);
        }
        return size;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

}
