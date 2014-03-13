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

import java.util.Date;

import org.mifos.framework.business.AbstractEntity;

public class PersonnelUsedPasswordEntity extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private Date dateChanged;
	
    private PersonnelBO personnel;

    private byte[] usedPassword;
    
	public PersonnelUsedPasswordEntity(int id, PersonnelBO personnel, byte[] usedPassword, Date dateChanged) {
		super();
		this.id = id;
		this.personnel = personnel;
		this.usedPassword = usedPassword;
		this.dateChanged = dateChanged;
	}
	
	public PersonnelUsedPasswordEntity() {
	}

	public byte[] getUsedPassword() {
		return usedPassword;
	}

	public void setUsedPassword(byte[] usedPassword) {
		this.usedPassword = usedPassword;
	}

	public PersonnelBO getPersonnel() {
		return personnel;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setPersonnel(PersonnelBO personnel) {
		this.personnel = personnel;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PersonnelUsedPasswordEntity) {
			return ((PersonnelUsedPasswordEntity) other).getId().equals(id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : super.hashCode();
	}
}
