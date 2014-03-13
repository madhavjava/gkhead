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

package org.mifos.customers.center.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.customers.api.CustomerLevel;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.center.CenterTemplate;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.exceptions.CustomerException;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.persistence.LegacyPersonnelDao;
import org.mifos.customers.personnel.util.helpers.PersonnelConstants;
import org.mifos.customers.util.helpers.CustomerConstants;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.customers.util.helpers.Param;
import org.mifos.framework.exceptions.HibernateSearchException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.QueryFactory;
import org.mifos.framework.hibernate.helper.QueryInputs;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.LegacyGenericDao;
import org.mifos.security.util.UserContext;

@Deprecated
public class CenterPersistence extends LegacyGenericDao {
    private final LegacyPersonnelDao legacyPersonnelDao = ApplicationContextProvider.getBean(LegacyPersonnelDao.class);
    private final OfficePersistence officePersistence = new OfficePersistence();

    public CenterPersistence() {
        super();
    }

    public CenterBO getCenter(Integer customerId) throws PersistenceException {
        return getPersistentObject(CenterBO.class, customerId);
    }

    @SuppressWarnings("unchecked")
    public CenterBO findBySystemId(String globalCustNum) throws PersistenceException {
        Map<String, String> queryParameters = new HashMap<String, String>();
        CenterBO center = null;
        queryParameters.put("globalCustNum", globalCustNum);
        List<CenterBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_CENTER_BY_SYSTEMID, queryParameters);
        if (null != queryResult && queryResult.size() > 0) {
            center = queryResult.get(0);
        }
        return center;
    }

    /**
     * @deprecated - use {@link CustomerDao#search(String, PersonnelBO)}.
     */
    @Deprecated
    public QueryResult search(String searchString, Short userId) throws PersistenceException {
        String[] namedQuery = new String[2];
        List<Param> paramList = new ArrayList<Param>();

        QueryInputs queryInputs = new QueryInputs();
        QueryResult queryResult = QueryFactory.getQueryResult(PersonnelConstants.USER_LIST);

        PersonnelBO user = legacyPersonnelDao.getPersonnel(userId);
        String officeSearchId = user.getOffice().getSearchId();

        namedQuery[0] = NamedQueryConstants.CENTER_SEARCH_COUNT;
        namedQuery[1] = NamedQueryConstants.CENTER_SEARCH;

        paramList.add(typeNameValue("String", "SEARCH_ID", officeSearchId + "%"));
        paramList.add(typeNameValue("String", "CENTER_NAME", searchString + "%"));
        paramList.add(typeNameValue("Short", "LEVEL_ID", CustomerLevel.CENTER.getValue()));
        paramList.add(typeNameValue("Short", "STATUS_ID", CustomerStatus.CENTER_ACTIVE.getValue()));
        paramList.add(typeNameValue("Short", "USER_ID", userId));
        paramList.add(typeNameValue("Short", "USER_LEVEL_ID", user.getLevelEnum().getValue()));
        paramList.add(typeNameValue("Short", "LO_LEVEL_ID", PersonnelConstants.LOAN_OFFICER));

        String[] aliasNames = { "parentOfficeId", "parentOfficeName", "centerSystemId", "centerName" };

        queryInputs.setQueryStrings(namedQuery);
        queryInputs.setPath("org.mifos.customers.center.util.helpers.CenterSearchResultsDto");
        queryInputs.setAliasNames(aliasNames);
        queryInputs.setParamList(paramList);
        try {
            queryResult.setQueryInputs(queryInputs);
        } catch (HibernateSearchException e) {
            throw new PersistenceException(e);
        }
        return queryResult;
    }

    /**
     * @deprecated use {@link CustomerDao#save(org.mifos.customers.business.CustomerBO)} with {@link CustomerBO} static
     *             factory methods.
     */
    @Deprecated
    public CenterBO createCenter(UserContext userContext, CenterTemplate template) throws Exception {

        OfficeBO centerOffice = officePersistence.getOffice(template.getOfficeId());
        PersonnelBO loanOfficer = legacyPersonnelDao.getPersonnel(template.getLoanOfficerId());
        MeetingBO meeting = template.getMeeting();

        CenterBO center = CenterBO.createNew(userContext, template.getDisplayName(), new DateTime(template.getMfiJoiningDate()), meeting, loanOfficer,
                centerOffice, template.getAddress(), template.getExternalId(), new DateMidnight().toDateTime());

        CustomerDao customerDao = ApplicationContextProvider.getBean(CustomerDao.class);

        try {
        StaticHibernateUtil.startTransaction();
        customerDao.save(center);
        center.generateGlobalCustomerNumber();
        customerDao.save(center);
        StaticHibernateUtil.commitTransaction();
        } catch (Exception e) {
            StaticHibernateUtil.rollbackTransaction();
        } finally {
            StaticHibernateUtil.closeSession();
        }

        return center;
    }

    /**
     * @deprecated use {@link CustomerDao#save(org.mifos.customers.business.CustomerBO)}.
     */
    @Deprecated
    public void saveCenter(CenterBO center) throws CustomerException {
        try {
            createOrUpdate(center);
            center.generateGlobalCustomerNumber();
            createOrUpdate(center);
        } catch (PersistenceException e) {
            throw new CustomerException(CustomerConstants.CREATE_FAILED_EXCEPTION, e);
        }
    }
}
