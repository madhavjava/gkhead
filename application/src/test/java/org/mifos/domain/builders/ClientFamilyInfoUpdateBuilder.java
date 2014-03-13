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

package org.mifos.domain.builders;

import java.util.ArrayList;
import java.util.List;

import org.mifos.dto.domain.ClientFamilyInfoUpdate;
import org.mifos.dto.screen.ClientFamilyDetailDto;
import org.mifos.dto.screen.ClientNameDetailDto;

public class ClientFamilyInfoUpdateBuilder {

    private Integer customerId = Integer.valueOf(-1);
    private Integer oldVersionNum = Integer.valueOf(1);
    private List<Integer> familyPrimaryKey = new ArrayList<Integer>();
    private List<ClientNameDetailDto> familyNames = new ArrayList<ClientNameDetailDto>();
    private List<ClientFamilyDetailDto> familyDetails = new ArrayList<ClientFamilyDetailDto>();

    public ClientFamilyInfoUpdate build() {
        return new ClientFamilyInfoUpdate(customerId, oldVersionNum, familyPrimaryKey, familyNames, familyDetails);
    }

}
