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

package org.mifos.customers.office.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.holiday.business.HolidayBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.util.helpers.OfficeConstants;
import org.mifos.customers.office.util.helpers.OfficeLevel;
import org.mifos.customers.office.util.helpers.OfficeStatus;
import org.mifos.customers.util.helpers.CustomerSearchConstants;
import org.mifos.dto.domain.OfficeDetailsDto;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.HibernateProcessException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SecurityException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.LegacyGenericDao;
import org.mifos.security.authorization.HierarchyManager;
import org.mifos.security.util.OfficeCacheDto;
import org.mifos.security.util.OfficeSearch;
import org.mifos.security.util.SecurityConstants;

@SuppressWarnings("unchecked")
public class OfficePersistence extends LegacyGenericDao {

    public OfficePersistence() {
        super();
    }

    /**
     * @deprecated see {@link OfficeDao}.
     */
    @Deprecated
    public List<OfficeDetailsDto> getActiveOffices(Short officeId) throws PersistenceException {
        String searchId = HierarchyManager.getInstance().getSearchId(officeId);
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("levelId", OfficeConstants.BRANCHOFFICE);
        queryParameters.put("OFFICESEARCHID", searchId);
        queryParameters.put("OFFICE_LIKE_SEARCHID", searchId + "%.");
        queryParameters.put("statusId", OfficeConstants.ACTIVE);
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.MASTERDATA_ACTIVE_BRANCHES,
                queryParameters);
        return queryResult;
    }

    public List<OfficeCacheDto> getAllOffices() throws PersistenceException {
        return executeNamedQuery(NamedQueryConstants.GET_ALL_OFFICES, null);
    }

    public String getSearchId(Short officeId) throws PersistenceException {
        String searchId = "";
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OFFICE_ID", officeId);
        List<String> queryResult = executeNamedQuery(NamedQueryConstants.OFFICE_GET_SEARCHID, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            searchId = queryResult.get(0);
        }
        return searchId;
    }

    /**
     * @return The office, or null if not found (TODO: wouldn't we rather have
     *         an exception if not found? The usual idiom seems to be to just
     *         dereference the returned office without any checking for null)
     */
    public OfficeBO getOffice(Short officeId) throws PersistenceException {
        if (officeId == null) {
            return null;
        }
        return getPersistentObject(OfficeBO.class, officeId);
    }

    public void addHoliday(Short officeId, HolidayBO holiday) throws PersistenceException {
        OfficeBO office = getOffice(officeId);
        office.addHoliday(holiday);
        createOrUpdate(holiday);
        for (OfficeBO childOffice : office.getChildren()) {
            addHoliday(childOffice.getOfficeId(), holiday);
        }
    }

    public OfficeBO getHeadOffice() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("LEVEL_ID", OfficeConstants.HEADOFFICE);
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.OFFICE_GET_HEADOFFICE, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult.get(0);
        }
        return null;
    }

    public Short getMaxOfficeId() throws PersistenceException {
        List queryResult = executeNamedQuery(NamedQueryConstants.GETMAXOFFICEID, null);
        if (queryResult != null && queryResult.size() != 0) {
            return (Short) queryResult.get(0);
        }
        return null;
    }

    public Integer getChildCount(Short officeId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OFFICE_ID", officeId);
        List queryResult = executeNamedQuery(NamedQueryConstants.GETCHILDCOUNT, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return ((Number) queryResult.get(0)).intValue();
        }
        return null;

    }

    /**
     * use {@link OfficeDao#validateOfficeNameIsNotTaken(String)}
     */
    @Deprecated
    public boolean isOfficeNameExist(String officeName) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OFFICE_NAME", officeName);
        List queryResult = executeNamedQuery(NamedQueryConstants.CHECKOFFICENAMEUNIQUENESS, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return ((Number) queryResult.get(0)).longValue() > 0;
        }
        return false;
    }

    /**
     * use {@link OfficeDao#validateOfficeShortNameIsNotTaken(String)}
     */
    @Deprecated
    public boolean isOfficeShortNameExist(String shortName) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("SHORT_NAME", shortName);
        List queryResult = executeNamedQuery(NamedQueryConstants.CHECKOFFICESHORTNAMEUNIQUENESS, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return ((Number) queryResult.get(0)).longValue() > 0;
        }
        return false;
    }

    public boolean isBranchInactive(short officeId) throws PersistenceException {

        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OFFICE_ID", officeId);
        queryParameters.put("STATUS_ID", OfficeConstants.INACTIVE);
        List queryResult = executeNamedQuery(NamedQueryConstants.GETOFFICEINACTIVE, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return ((Number) queryResult.get(0)).longValue() > 0;
        }
        return false;
    }

    /**
     * see {@link OfficeDao}
     */
    @Deprecated
    public List<OfficeDetailsDto> getActiveParents(OfficeLevel level) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("LEVEL_ID", level.getValue());
        queryParameters.put("STATUS_ID", OfficeStatus.ACTIVE.getValue());
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.GETACTIVEPARENTS, queryParameters);
        return queryResult;

    }

    public List<OfficeDetailsDto> getActiveLevels() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.GETACTIVELEVELS, queryParameters);
        if (queryResult == null) {
            queryResult = new ArrayList<OfficeDetailsDto>();
        }

        return queryResult;

    }
    public List<OfficeDetailsDto> getActiveLevelsForfa() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.GETOFFICESTATUSFORFA, queryParameters);
        if (queryResult == null) {
            queryResult = new ArrayList<OfficeDetailsDto>();
        }

        return queryResult;

    }

    public List<OfficeDetailsDto> getStatusList() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.GETOFFICESTATUS, queryParameters);
        if (queryResult == null) {
            queryResult = new ArrayList<OfficeDetailsDto>();
        }
        return queryResult;
    }

    public List<OfficeBO> getChildern(Short officeId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("OFFICE_ID", officeId);
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GETCHILDERN, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return null;
    }

    public List<OfficeBO> getOfficesTillBranchOffice() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("branchOffice", OfficeLevel.BRANCHOFFICE.getValue());
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_OFFICES_TILL_BRANCHOFFICE,
                queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return null;
    }

    public List<OfficeBO> getBranchOffices() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("branchOffice", OfficeLevel.BRANCHOFFICE.getValue());
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_BRANCH_OFFICES, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return null;
    }

    public List<OfficeBO> getOfficesTillBranchOffice(String searchId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("LEVEL_ID", OfficeLevel.BRANCHOFFICE.getValue());
        queryParameters.put("SEARCH_ID", searchId + "%");
        queryParameters.put("STATUS_ID", OfficeStatus.ACTIVE.getValue());
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_OFFICES_TILL_BRANCH, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return new ArrayList<OfficeBO>();
    }

    public List<OfficeBO> getBranchParents(String searchId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("LEVEL_ID", OfficeLevel.BRANCHOFFICE.getValue());
        queryParameters.put("SEARCH_ID", searchId + "%");
        queryParameters.put("STATUS_ID", OfficeStatus.ACTIVE.getValue());
        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_BRANCH_PARENTS, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return new ArrayList<OfficeBO>();
    }

    public List<OfficeDetailsDto> getChildOffices(String searchId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("STATUS_ID", OfficeStatus.ACTIVE.getValue());
        queryParameters.put("OFFICE_LIKE_SEARCHID", searchId + "%");
        List<OfficeDetailsDto> queryResult = executeNamedQuery(NamedQueryConstants.GETOFFICE_CHILDREN, queryParameters);
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult;
        }
        return null;
    }

    public List<OfficeBO> getActiveBranchesUnderUser(String officeSearchId) throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put(CustomerSearchConstants.OFFICELEVELID, OfficeLevel.BRANCHOFFICE.getValue());
        queryParameters.put(CustomerSearchConstants.OFFICESEARCHID, officeSearchId + "%");
        queryParameters.put(OfficeConstants.OFFICE_ACTIVE, OfficeStatus.ACTIVE.getValue());

        return executeNamedQuery(NamedQueryConstants.GET_ACTIVE_BRANCHES, queryParameters);
    }

    public List<OfficeBO> getAllofficesForCustomFIeld() throws PersistenceException {
        HashMap<String, Object> queryParameters = new HashMap<String, Object>();

        List<OfficeBO> queryResult = executeNamedQuery(NamedQueryConstants.GET_ALL_OFFICES_FOR_CUSTOM_FIELD,
                queryParameters);
        return queryResult;
    }



    /**
     * This function is used to get the list of the offices under the given
     * personnel office under any user at any time
     *
     * @param officeid
     *            office id of the person
     * @return List list of the offices under him
     * @throws HibernateProcessException
     */
    public List<OfficeSearch> getPersonnelOffices(Short officeid) throws SystemException, ApplicationException {
        HierarchyManager hm = HierarchyManager.getInstance();
        String pattern = hm.getSearchId(officeid) + "%";
        List<OfficeSearch> lst = null;
        try {
            Session session = StaticHibernateUtil.getSessionTL();
            Query officeSearch = session.getNamedQuery(NamedQueryConstants.GETOFFICESEARCH);
            officeSearch.setString(SecurityConstants.PATTERN, pattern);
            lst = officeSearch.list();
        } catch (HibernateException he) {
            throw new SecurityException(SecurityConstants.GENERALERROR, he);
        }
        return lst;
    }

    /**
     * This function is used to initialise the the hirerchy manager which is
     * paert of the security module which keeps the cache of officeid to office
     * search id so that it can find office under given person without going to
     * database every time
     *
     * @return List of OfficeSearch objects which contains office is and
     *         associated searchid
     * @throws HibernateProcessException
     */
    public List<OfficeSearch> getOffices() throws SystemException, ApplicationException {
        List<OfficeSearch> lst = null;
        try {
            Session session = StaticHibernateUtil.getSessionTL();
            Query queryOfficeSearchList = session.getNamedQuery(NamedQueryConstants.GETOFFICESEARCHLIST);
            lst = queryOfficeSearchList.list();
        } catch (HibernateException he) {
            throw new SecurityException(SecurityConstants.GENERALERROR, he);
        }
        return lst;
    }
}
