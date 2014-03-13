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

package org.mifos.customers.group.persistence;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.config.ClientRules;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.business.service.CustomerService;
import org.mifos.customers.center.persistence.CenterPersistence;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.persistence.CustomerPersistence;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.persistence.LegacyPersonnelDao;
import org.mifos.customers.personnel.util.helpers.PersonnelLevel;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerSearchConstants;
import org.mifos.customers.util.helpers.Param;
import org.mifos.framework.exceptions.HibernateSearchException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.QueryFactory;
import org.mifos.framework.hibernate.helper.QueryInputs;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.LegacyGenericDao;
import org.mifos.framework.util.DateTimeService;

/**
 * @deprecated - use {@link CustomerDao}
 */
@Deprecated
public class GroupPersistence extends LegacyGenericDao {
    private final CenterPersistence centerPersistence = new CenterPersistence();
    private final LegacyPersonnelDao legacyPersonnelDao = ApplicationContextProvider.getBean(LegacyPersonnelDao.class);

    @SuppressWarnings("unchecked")
    public GroupBO findBySystemId(String globalCustNum) throws PersistenceException {
        Map<String, String> queryParameters = new HashMap<String, String>();
        GroupBO group = null;
        queryParameters.put("globalCustNum", globalCustNum);
        List<GroupBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_GROUP_BY_SYSTEMID, queryParameters);
        if (null != queryResult && queryResult.size() > 0) {
            group = queryResult.get(0);
        }
        return group;
    }

    public QueryResult search(String searchString, Short userId) throws PersistenceException {
        String[] namedQuery = new String[2];
        List<Param> paramList = new ArrayList<Param>();
        QueryInputs queryInputs = new QueryInputs();
        QueryResult queryResult = QueryFactory.getQueryResult(CustomerSearchConstants.GROUPLIST);

        PersonnelBO personnel = legacyPersonnelDao.getPersonnel(userId);
        String officeSearchId = personnel.getOffice().getSearchId();
        if (ClientRules.getCenterHierarchyExists()) {
            namedQuery[0] = NamedQueryConstants.GROUP_SEARCH_COUNT_WITH_CENTER;
            namedQuery[1] = NamedQueryConstants.GROUP_SEARCHWITH_CENTER;
            String[] aliasNames = { "officeName", "groupName", "centerName", "groupId" };
            queryInputs.setAliasNames(aliasNames);
        } else {
            namedQuery[0] = NamedQueryConstants.GROUP_SEARCH_COUNT_WITHOUT_CENTER;
            namedQuery[1] = NamedQueryConstants.GROUP_SEARCH_WITHOUT_CENTER;
            String[] aliasNames = { "officeName", "groupName", "groupId" };
            queryInputs.setAliasNames(aliasNames);
        }
        paramList.add(typeNameValue("String", "SEARCH_ID", officeSearchId + "%"));
        paramList.add(typeNameValue("String", "SEARCH_STRING", "%" + searchString + "%"));
        paramList.add(typeNameValue("Short", "LEVEL_ID", CustomerLevel.GROUP.getValue()));
        paramList.add(typeNameValue("Short", "USER_ID", userId));
        paramList.add(typeNameValue("Short", "USER_LEVEL_ID", personnel.getLevelEnum().getValue()));
        paramList.add(typeNameValue("Short", "LO_LEVEL_ID", PersonnelLevel.LOAN_OFFICER.getValue()));
        queryInputs.setQueryStrings(namedQuery);
        queryInputs.setPath("org.mifos.customers.group.util.helpers.GroupSearchResults");
        queryInputs.setParamList(paramList);
        try {
            queryResult.setQueryInputs(queryInputs);
        } catch (HibernateSearchException e) {
            throw new PersistenceException(e);
        }
        return queryResult;
    }

    public QueryResult searchForAddingClientToGroup(String searchString, Short userId) throws PersistenceException {
        String[] namedQuery = new String[2];
        List<Param> paramList = new ArrayList<Param>();
        QueryInputs queryInputs = new QueryInputs();
        QueryResult queryResult = QueryFactory.getQueryResult(CustomerSearchConstants.GROUPLIST);

        PersonnelBO personnel = legacyPersonnelDao.getPersonnel(userId);
        String officeSearchId = personnel.getOffice().getSearchId();
        if (ClientRules.getCenterHierarchyExists()) {
            namedQuery[0] = NamedQueryConstants.GROUP_SEARCH_COUNT_WITH_CENTER_FOR_ADDING_GROUPMEMBER;
            namedQuery[1] = NamedQueryConstants.GROUP_SEARCHWITH_CENTER_FOR_ADDING_GROUPMEMBER;
            String[] aliasNames = { "officeName", "groupName", "centerName", "groupId" };
            queryInputs.setAliasNames(aliasNames);
        } else {
            namedQuery[0] = NamedQueryConstants.GROUP_SEARCH_COUNT_WITHOUT_CENTER_FOR_ADDING_GROUPMEMBER;
            namedQuery[1] = NamedQueryConstants.GROUP_SEARCH_WITHOUT_CENTER_FOR_ADDING_GROUPMEMBER;
            String[] aliasNames = { "officeName", "groupName", "groupId" };
            queryInputs.setAliasNames(aliasNames);
        }
        paramList.add(typeNameValue("String", "SEARCH_ID", officeSearchId + "%"));
        paramList.add(typeNameValue("String", "SEARCH_STRING", searchString + "%"));
        paramList.add(typeNameValue("Short", "LEVEL_ID", CustomerLevel.GROUP.getValue()));
        paramList.add(typeNameValue("Short", "USER_ID", userId));
        paramList.add(typeNameValue("Short", "USER_LEVEL_ID", personnel.getLevelEnum().getValue()));
        paramList.add(typeNameValue("Short", "LO_LEVEL_ID", PersonnelLevel.LOAN_OFFICER.getValue()));
        queryInputs.setQueryStrings(namedQuery);
        queryInputs.setPath("org.mifos.customers.group.util.helpers.GroupSearchResults");
        queryInputs.setParamList(paramList);
        try {
            queryResult.setQueryInputs(queryInputs);
        } catch (HibernateSearchException e) {
            throw new PersistenceException(e);
        }
        return queryResult;
    }

    public CenterPersistence getCenterPersistence() {
        return centerPersistence;
    }

    public GroupBO getGroupByCustomerId(Integer customerId) throws PersistenceException {
        return getPersistentObject(GroupBO.class, customerId);
    }

    // this code is used in the PAR task to improve performance
    public boolean updateGroupInfoAndGroupPerformanceHistoryForPortfolioAtRisk(double portfolioAtRisk, Integer groupId)
            throws Exception {
        boolean result = false;
        Session session = StaticHibernateUtil.getSessionTL();
        try {

            session.beginTransaction();

            short userId = 1; // this is bach job, so no user
            Date currentDate = new DateTimeService().getCurrentJavaSqlDate();
            Query query = session.createSQLQuery("update customer set updated_by = " + userId + ", updated_date='" + currentDate + "' where customer_id=" + groupId.toString());
            int rows = query.executeUpdate();

            if (rows != 1) {
                throw new PersistenceException("Unable to update group table for group id " + groupId.toString());
            }

            query = session.createSQLQuery("update group_perf_history set portfolio_at_risk = " + portfolioAtRisk + " where customer_id=" + groupId.toString());
            rows = query.executeUpdate();

            if (rows != 1) {
                throw new PersistenceException("Unable to update group performance history for group id " + groupId.toString());
            }
            result = true;

        } catch (Exception ex) {
            session.getTransaction().rollback();
            throw new PersistenceException(ex);
        } finally {
                StaticHibernateUtil.closeSession();
        }
        return result;
    }

    /**
     * @deprecated - use {@link CustomerService#createGroup(GroupBO, org.mifos.application.meeting.business.MeetingBO, List)}.
     * use {@link CustomerDao#save(org.mifos.customers.business.CustomerBO)}.
     */
    @Deprecated
    public void saveGroup(GroupBO groupBo) throws CustomerException {
        CustomerPersistence customerPersistence = new CustomerPersistence();
        customerPersistence.saveCustomer(groupBo);
        try {
            if (groupBo.getParentCustomer() != null) {
                customerPersistence.createOrUpdate(groupBo.getParentCustomer());
            }
        } catch (PersistenceException pe) {
            throw new CustomerException(CustomerConstants.CREATE_FAILED_EXCEPTION, pe);
        }
    }
}
