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

package org.mifos.customers.office.business;

import java.util.ArrayList;
import java.util.List;

import org.mifos.customers.office.util.helpers.OfficeLevel;
import org.mifos.customers.office.util.helpers.OperationMode;
import org.mifos.dto.domain.CustomFieldDto;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.util.helpers.IntegrationTestObjectMother;
import org.mifos.framework.util.helpers.TestObjectFactory;

/**
 * use builder and {@link IntegrationTestObjectMother#createOffice(OfficeBO)} to set up state for integration tests
 */
@Deprecated
public class OfficeTemplateImpl implements OfficeTemplate {
    private Address address;
    private String officeName;
    private String shortName;
    private List<CustomFieldDto> customFieldDtos;
    private Short parentOfficeId;
    private OfficeLevel officeLevel;

    private OfficeTemplateImpl(OfficeLevel officeLevel) {
        this.officeLevel = officeLevel;
        address = new Address("Address Line1", null, null, "Seattle", "WA", "USA", "98117", "206-555-1212");
        this.officeName = "TestOfficeName";
        this.shortName = "TON";
        this.parentOfficeId = TestObjectFactory.HEAD_OFFICE;

        this.customFieldDtos = new ArrayList<CustomFieldDto>();
        CustomFieldDto customFieldDto = new CustomFieldDto();
        customFieldDto.setFieldId(Short.valueOf("1"));
        customFieldDto.setFieldValue("CustomField1Value");
        customFieldDtos.add(customFieldDto);
    }

    @Override
	public OfficeLevel getOfficeLevel() {
        return this.officeLevel;
    }

    @Override
	public String getOfficeName() {
        return officeName;
    }

    @Override
	public Short getParentOfficeId() {
        return parentOfficeId;
    }

    public void setParentOfficeId(Short id) {
        this.parentOfficeId = id;
    }

    @Override
	public String getShortName() {
        return shortName;
    }

    @Override
	public Address getOfficeAddress() {
        return address;
    }

    @Override
	public List<CustomFieldDto> getCustomFieldViews() {
        return customFieldDtos;
    }

    @Override
	public OperationMode getOperationMode() {
        return OperationMode.REMOTE_SERVER;
    }

    /**
     * Use this in transactions that you don't plan on committing to the
     * database. If you commit more than one of these to the database you'll run
     * into uniqueness constraints. Plan on always rolling back the transaction.
     *
     * @param officeLevel
     * @return officeTemplateImpl
     */
    public static OfficeTemplateImpl createNonUniqueOfficeTemplate(OfficeLevel officeLevel) {
        return new OfficeTemplateImpl(officeLevel);
    }
}
