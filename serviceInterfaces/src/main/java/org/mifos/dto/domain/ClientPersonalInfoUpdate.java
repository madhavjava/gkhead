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

package org.mifos.dto.domain;

import java.io.InputStream;
import java.util.List;

import org.mifos.dto.screen.ClientNameDetailDto;
import org.mifos.dto.screen.ClientPersonalDetailDto;

public class ClientPersonalInfoUpdate {

    private final Integer customerId;
    private final Integer originalClientVersionNumber;
    private final List<CustomFieldDto> clientCustomFields;
    private final AddressDto address;
    private final ClientPersonalDetailDto clientDetail;
    private final ClientNameDetailDto clientNameDetails;
    private final ClientNameDetailDto spouseFather;
    private final InputStream picture;
    private final String governmentId;
    private final String clientDisplayName;
    private final String dateOfBirth;

    @SuppressWarnings("PMD")
    public ClientPersonalInfoUpdate(Integer customerId, Integer originalClientVersionNumber, List<CustomFieldDto> clientCustomFields, AddressDto address,
            ClientPersonalDetailDto clientDetail, ClientNameDetailDto clientNameDetails, ClientNameDetailDto spouseFather,
            InputStream picture, String governmentId, String clientDisplayName, String dateOfBirth) {
        this.customerId = customerId;
        this.originalClientVersionNumber = originalClientVersionNumber;
        this.clientCustomFields = clientCustomFields;
        this.address = address;
        this.clientDetail = clientDetail;
        this.clientNameDetails = clientNameDetails;
        this.spouseFather = spouseFather;
        this.picture = picture;
        this.governmentId = governmentId;
        this.clientDisplayName = clientDisplayName;
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getCustomerId() {
        return this.customerId;
    }

    public Integer getOriginalClientVersionNumber() {
        return this.originalClientVersionNumber;
    }

    public List<CustomFieldDto> getClientCustomFields() {
        return this.clientCustomFields;
    }

    public AddressDto getAddress() {
        return this.address;
    }

    public ClientPersonalDetailDto getClientDetail() {
        return this.clientDetail;
    }

    public ClientNameDetailDto getClientNameDetails() {
        return this.clientNameDetails;
    }

    public ClientNameDetailDto getSpouseFather() {
        return this.spouseFather;
    }

    public InputStream getPicture() {
        return this.picture;
    }

    public String getGovernmentId() {
        return this.governmentId;
    }

    public String getClientDisplayName() {
        return this.clientDisplayName;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }
}