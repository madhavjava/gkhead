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

import org.joda.time.DateTime;

public class ClientMfiInfoUpdate {

    private final Integer clientId;
    private final Integer orginalClientVersionNumber;
    private final Short personnelId;
    private final String externalId;
    private final boolean trained;
    private final DateTime trainedDate;

    public ClientMfiInfoUpdate(Integer clientId, Integer orginalClientVersionNumber, Short personnelId, String externalId, boolean trained, DateTime trainedDate) {
        this.clientId = clientId;
        this.orginalClientVersionNumber = orginalClientVersionNumber;
        this.personnelId = personnelId;
        this.externalId = externalId;
        this.trained = trained;
        this.trainedDate = trainedDate;
    }

    public Integer getClientId() {
        return this.clientId;
    }

    public Integer getOrginalClientVersionNumber() {
        return this.orginalClientVersionNumber;
    }

    public Short getPersonnelId() {
        return this.personnelId;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean isTrained() {
        return this.trained;
    }

    public DateTime getTrainedDate() {
        return this.trainedDate;
    }
}