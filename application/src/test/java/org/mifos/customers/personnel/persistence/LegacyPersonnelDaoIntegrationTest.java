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

package org.mifos.customers.personnel.persistence;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.customers.business.CustomerBO;
import org.mifos.customers.business.CustomerNoteEntity;
import org.mifos.customers.business.service.CustomerService;
import org.mifos.customers.center.business.CenterBO;
import org.mifos.customers.client.business.ClientBO;
import org.mifos.customers.group.business.GroupBO;
import org.mifos.customers.office.business.OfficeBO;
import org.mifos.customers.office.persistence.OfficePersistence;
import org.mifos.customers.office.util.helpers.OfficeLevel;
import org.mifos.customers.persistence.CustomerDao;
import org.mifos.customers.personnel.business.PersonnelBO;
import org.mifos.customers.personnel.business.PersonnelNotesEntity;
import org.mifos.customers.personnel.util.helpers.PersonnelConstants;
import org.mifos.customers.personnel.util.helpers.PersonnelLevel;
import org.mifos.customers.util.helpers.CustomerStatus;
import org.mifos.customers.util.helpers.CustomerStatusFlag;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.dto.domain.PersonnelDto;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.business.util.Name;
import org.mifos.framework.hibernate.helper.QueryResult;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.mifos.security.rolesandpermission.persistence.LegacyRolesPermissionsDao;
import org.springframework.beans.factory.annotation.Autowired;

public class LegacyPersonnelDaoIntegrationTest extends MifosIntegrationTestCase {

    private static final Short OFFICE_WITH_BRANCH_MANAGER = Short.valueOf("3");

    @Autowired
    LegacyRolesPermissionsDao legacyRolesPermissionsDao;

    private MeetingBO meeting;
    private CustomerBO center;
    private CustomerBO client;
    private CustomerBO group;
    private OfficeBO office;
    private OfficeBO branchOffice;
    private OfficeBO createdBranchOffice;
    private PersonnelBO personnel;
    private Name name;
    private final OfficePersistence officePersistence = new OfficePersistence();

    private CustomerService customerService = ApplicationContextProvider.getBean(CustomerService.class);
    private CustomerDao customerDao = ApplicationContextProvider.getBean(CustomerDao.class);

    @Before
    public void setUp() throws Exception {
        initializeStatisticsService();
    }

    @After
    public void tearDown() throws Exception {
        office = null;
        branchOffice = null;
        name = null;
        client = null;
        group = null;
        center = null;
        personnel = null;
        createdBranchOffice = null;
        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testActiveLoanOfficersInBranch() throws Exception {
        List<PersonnelDto> personnels = legacyPersonnelDao.getActiveLoanOfficersInBranch(
                PersonnelConstants.LOAN_OFFICER, Short.valueOf("3"), Short.valueOf("3"),
                PersonnelConstants.LOAN_OFFICER);
        Assert.assertEquals(1, personnels.size());
    }

    @Test
    public void testNonLoanOfficerInBranch() throws Exception {
        List<PersonnelDto> personnels = legacyPersonnelDao.getActiveLoanOfficersInBranch(
                PersonnelConstants.LOAN_OFFICER, Short.valueOf("3"), OFFICE_WITH_BRANCH_MANAGER,
                PersonnelConstants.NON_LOAN_OFFICER);
        Assert.assertEquals(1, personnels.size());
    }

    @Test
    public void testIsUserExistSucess() throws Exception {
        Assert.assertTrue(legacyPersonnelDao.isUserExist("mifos"));
    }

    @Test
    public void testIsUserExistFailure() throws Exception {
        Assert.assertFalse(legacyPersonnelDao.isUserExist("XXX"));
    }

    @Test
    public void testIsUserExistWithGovernmentIdSucess() throws Exception {
        Assert.assertTrue(legacyPersonnelDao.isUserExistWithGovernmentId("123"));
    }

    @Test
    public void testIsUserExistWithGovernmentIdFailure() throws Exception {
        Assert.assertFalse(legacyPersonnelDao.isUserExistWithGovernmentId("XXX"));
    }

    @Test
    public void testIsUserExistWithDobAndDisplayNameSucess() throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Assert.assertTrue(legacyPersonnelDao.isUserExist("mifos", dateFormat.parse("1979-12-12")));
    }

    @Test
    public void testIsUserExistWithDobAndDisplayNameFailure() throws Exception {
        Assert.assertFalse(legacyPersonnelDao.isUserExist("mifos", new GregorianCalendar(1989, 12, 12, 0, 0, 0)
                .getTime()));
    }

    @Test
    public void testActiveCustomerUnderLO() throws Exception {
        createCustomers(CustomerStatus.GROUP_ACTIVE, CustomerStatus.CLIENT_ACTIVE);
        Assert.assertTrue(legacyPersonnelDao.getActiveChildrenForLoanOfficer(Short.valueOf("1"), Short.valueOf("3")));
    }

    @Test
    public void testGetAllCustomerUnderLO() throws Exception {
        createCustomers(CustomerStatus.GROUP_CLOSED, CustomerStatus.CLIENT_CANCELLED);
        Assert.assertTrue(legacyPersonnelDao.getAllChildrenForLoanOfficer(Short.valueOf("1"), Short.valueOf("3")));
        StaticHibernateUtil.flushSession();

        CustomerStatusFlag customerStatusFlag = null;
        CustomerNoteEntity customerNote = new CustomerNoteEntity("Made Inactive", new java.util.Date(), center.getPersonnel(), center);

        customerService.updateCenterStatus((CenterBO) center, CustomerStatus.CENTER_INACTIVE, customerStatusFlag, customerNote);
        center = customerDao.findCenterBySystemId(center.getGlobalCustNum());

        Assert.assertTrue(legacyPersonnelDao.getAllChildrenForLoanOfficer(Short.valueOf("1"), Short.valueOf("3")));
    }

    @Test
    public void testGetAllPersonnelNotes() throws Exception {
        office = TestObjectFactory.getOffice(TestObjectFactory.HEAD_OFFICE);
        branchOffice = TestObjectFactory.getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        createdBranchOffice = TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, office, "Office_BRanch1", "OFB");
        StaticHibernateUtil.flushSession();
        createdBranchOffice = (OfficeBO) StaticHibernateUtil.getSessionTL().get(OfficeBO.class,
                createdBranchOffice.getOfficeId());
        createPersonnel(branchOffice, PersonnelLevel.LOAN_OFFICER);
        Assert.assertEquals(branchOffice.getOfficeId(), personnel.getOffice().getOfficeId());
        createInitialObjects(branchOffice.getOfficeId(), personnel.getPersonnelId());

        PersonnelNotesEntity personnelNotes = new PersonnelNotesEntity("Personnel notes created",
                legacyPersonnelDao.getPersonnel(PersonnelConstants.SYSTEM_USER), personnel);
        personnel.addNotes(PersonnelConstants.SYSTEM_USER, personnelNotes);
        StaticHibernateUtil.flushSession();
        StaticHibernateUtil.flushSession();
        client = (ClientBO) StaticHibernateUtil.getSessionTL().get(ClientBO.class, client.getCustomerId());
        group = (GroupBO) StaticHibernateUtil.getSessionTL().get(GroupBO.class, group.getCustomerId());
        center = (CenterBO) StaticHibernateUtil.getSessionTL().get(CenterBO.class, center.getCustomerId());
        personnel = (PersonnelBO) StaticHibernateUtil.getSessionTL().get(PersonnelBO.class, personnel.getPersonnelId());
        createdBranchOffice = (OfficeBO) StaticHibernateUtil.getSessionTL().get(OfficeBO.class,
                createdBranchOffice.getOfficeId());
        Assert.assertEquals(1, legacyPersonnelDao.getAllPersonnelNotes(personnel.getPersonnelId()).getSize());
    }

    @Test
    public void testSuccessfullGetPersonnel() throws Exception {
        branchOffice = TestObjectFactory.getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        personnel = createPersonnel(branchOffice, PersonnelLevel.LOAN_OFFICER);
        String oldUserName = personnel.getUserName();
        personnel = legacyPersonnelDao.getPersonnelByUserName(personnel.getUserName());
        Assert.assertEquals(oldUserName, personnel.getUserName());
    }

    @Test
    public void testGetPersonnelByGlobalPersonnelNum() throws Exception {
        Assert.assertNotNull(legacyPersonnelDao.getPersonnelByGlobalPersonnelNum("1"));
    }

    @Test
    public void testGetPersonnelRoleCount() throws Exception {
        Integer count = legacyPersonnelDao.getPersonnelRoleCount((short) 2);
        Assert.assertEquals(0, (int) count);
        count = legacyPersonnelDao.getPersonnelRoleCount((short) 1);
        Assert.assertEquals(3, (int) count);
    }

    @Test
    public void testSearch() throws Exception {
        branchOffice = TestObjectFactory.getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        personnel = createPersonnel(branchOffice, PersonnelLevel.LOAN_OFFICER);
        QueryResult queryResult = legacyPersonnelDao.search(personnel.getUserName(), Short.valueOf("1"));
        Assert.assertNotNull(queryResult);
        Assert.assertEquals(1, queryResult.getSize());
        Assert.assertEquals(1, queryResult.get(0, 10).size());
    }

    @Test
    public void testSearchFirstNameAndLastName() throws Exception {
        branchOffice = TestObjectFactory.getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        personnel = createPersonnelWithName(branchOffice, PersonnelLevel.LOAN_OFFICER, new Name("Rajender", null,
                "singh", "saini"));
        QueryResult queryResult = legacyPersonnelDao.search(personnel.getPersonnelDetails().getName().getFirstName()
                + " " + personnel.getPersonnelDetails().getName().getLastName(), Short.valueOf("1"));
        Assert.assertNotNull(queryResult);
        Assert.assertEquals(1, queryResult.getSize());
        Assert.assertEquals(1, queryResult.get(0, 10).size());
    }

    @Test
    public void testGetActiveLoanOfficersUnderOffice() throws Exception {
        branchOffice = TestObjectFactory.getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE);
        personnel = createPersonnel(branchOffice, PersonnelLevel.LOAN_OFFICER);
        List<PersonnelBO> loanOfficers = legacyPersonnelDao.getActiveLoanOfficersUnderOffice(branchOffice
                .getOfficeId());
        Assert.assertNotNull(loanOfficers);
        Assert.assertEquals(2, loanOfficers.size());
        Assert.assertEquals("loan officer", loanOfficers.get(0).getDisplayName());
        Assert.assertEquals("XYZ", loanOfficers.get(1).getDisplayName());
    }

    @Test
    public void testGetAllPersonnel() throws Exception {
        List<PersonnelBO> personnel = legacyPersonnelDao.getAllPersonnel();
        Assert.assertNotNull(personnel);
        Assert.assertEquals(3, personnel.size());
    }

    private void createInitialObjects(final Short officeId, final Short personnelId) {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());

        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting, officeId, personnelId);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
    }

    private PersonnelBO createPersonnel(final OfficeBO office, final PersonnelLevel personnelLevel) throws Exception {
        name = new Name("XYZ", null, null, null);
        return create(personnelLevel, name, PersonnelConstants.SYSTEM_USER, office);
    }

    private PersonnelBO createPersonnelWithName(final OfficeBO office, final PersonnelLevel personnelLevel,
                                                final Name personnelName) throws Exception {
        return create(personnelLevel, personnelName, PersonnelConstants.SYSTEM_USER, office);
    }

    private PersonnelBO create(final PersonnelLevel personnelLevel, final Name name, final Short createdBy,
                               final OfficeBO office) throws Exception {
        List<CustomFieldDto> customFieldDto = new ArrayList<CustomFieldDto>();
        customFieldDto.add(new CustomFieldDto(Short.valueOf("9"), "123456", CustomFieldType.NUMERIC.getValue()));
        Address address = new Address("abcd", "abcd", "abcd", "abcd", "abcd", "abcd", "abcd", "abcd");
        Date date = new Date();
        personnel = new PersonnelBO(personnelLevel, office, Integer.valueOf("1"), Short.valueOf("1"), "ABCD", "XYZ",
                "xyz@yahoo.com", null, customFieldDto, name, "111111", date, Integer.valueOf("1"), Integer
                        .valueOf("1"), date, date, address, createdBy, new Date(), new HashSet());
        IntegrationTestObjectMother.createPersonnel(personnel);
        return IntegrationTestObjectMother.findPersonnelById(personnel.getPersonnelId());
    }

    private void createCustomers(final CustomerStatus groupStatus, final CustomerStatus clientStatus) {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createWeeklyFeeCenter("Center", meeting);
        group = TestObjectFactory.createWeeklyFeeGroupUnderCenter("group", groupStatus, center);
        client = TestObjectFactory.createClient("client", clientStatus, group, new Date());
    }

    @Test
    public void testGetActiveBranchManagerUnderOffice() throws Exception {
        List<PersonnelBO> activeBranchManagersUnderOffice = legacyPersonnelDao
                .getActiveBranchManagersUnderOffice(OFFICE_WITH_BRANCH_MANAGER, legacyRolesPermissionsDao
                        .getRole(Short.valueOf("1")));
        Assert.assertNotNull(activeBranchManagersUnderOffice);
        Assert.assertEquals(2, activeBranchManagersUnderOffice.size());
    }

}
