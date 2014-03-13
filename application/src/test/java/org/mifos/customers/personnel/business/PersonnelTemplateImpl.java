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

package org.mifos.customers.personnel.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifos.application.master.business.CustomFieldType;
import org.mifos.customers.personnel.util.helpers.PersonnelLevel;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.business.util.Name;

public class PersonnelTemplateImpl implements PersonnelTemplate {
    private PersonnelLevel personnelLevel;
    private Short officeId;
    private Integer titleId;
    private Short preferredLocale;
    private String password;
    private String userName;
    private String emailId;
    private List<Short> roleIds;
    private List<CustomFieldDto> customFields;
    private Name name;
    private String governmentIdNumber;
    private Date dateOfBirth;
    private Integer maritalStatusId;
    private Integer genderId;
    private Date dateOfJoiningMFI;
    private Date dateOfJoiningBranch;
    private Address address;

    private PersonnelTemplateImpl(Short officeId) {
        this.personnelLevel = PersonnelLevel.LOAN_OFFICER;
        this.officeId = officeId;
        this.titleId = new Integer(1);
        this.preferredLocale = new Short((short) 1);
        this.userName = "TestUserName";
        this.password = "password";
        this.emailId = "foo@mifos.org";
        this.roleIds = new ArrayList<Short>();
        this.roleIds.add(new Short((short) 1));
        this.customFields = new ArrayList<CustomFieldDto>();
        customFields.add(new CustomFieldDto(Short.valueOf("9"), "123456", CustomFieldType.NUMERIC.getValue()));
        this.name = new Name("TestFirstName", null, null, null);
        this.governmentIdNumber = "111111";
        this.dateOfBirth = new Date();
        this.maritalStatusId = new Integer(1);
        this.genderId = new Integer(1);
        this.dateOfJoiningMFI = new Date();
        this.dateOfJoiningBranch = new Date();
        this.address = new Address();
    }

    @Override
	public PersonnelLevel getPersonnelLevel() {
        return this.personnelLevel;
    }

    @Override
	public Short getOfficeId() {
        return this.officeId;
    }

    @Override
	public Integer getTitleId() {
        return this.titleId;
    }

    @Override
	public Short getPreferredLocale() {
        return this.preferredLocale;
    }

    @Override
	public String getPassword() {
        return this.password;
    }

    @Override
	public String getUserName() {
        return this.userName;
    }

    @Override
	public String getEmailId() {
        return this.emailId;
    }

    @Override
	public List<Short> getRoleIds() {
        return this.roleIds;
    }

    @Override
	public List<CustomFieldDto> getCustomFields() {
        return this.customFields;
    }

    @Override
	public Name getName() {
        return this.name;
    }

    @Override
	public String getGovernmentIdNumber() {
        return this.governmentIdNumber;
    }

    @Override
	public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    @Override
	public Integer getMaritalStatusId() {
        return this.maritalStatusId;
    }

    @Override
	public Integer getGenderId() {
        return this.genderId;
    }

    @Override
	public Date getDateOfJoiningMFI() {
        return this.dateOfJoiningMFI;
    }

    @Override
	public Date getDateOfJoiningBranch() {
        return this.dateOfJoiningBranch;
    }

    @Override
	public Address getAddress() {
        return this.address;
    }

    /**
     * Use this in transactions that you don't plan on committing to the
     * database. If you commit more than one of these to the database you'll run
     * into uniqueness constraints. Plan on always rolling back the transaction.
     *
     * @param officeId
     * @return personnelTemplateImpl
     */
    public static PersonnelTemplateImpl createNonUniquePersonnelTemplate(Short officeId) {
        return new PersonnelTemplateImpl(officeId);
    }
}
