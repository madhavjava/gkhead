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

package org.mifos.accounts.financial.business;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.mifos.framework.business.AbstractEntity;

/**
 * Unused and candidate for removal.
 */
@NamedQueries(
 {
  @NamedQuery(
    name="GETALLCOA",
    query="from COAIDMapperEntity"
  )
 }
)
@Entity
@Table(name = "coa_idmapper")
public class COAIDMapperEntity  extends AbstractEntity {

    @Id
    @GeneratedValue
    private Short constantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coa_id", unique = true, insertable = false, updatable = false)
    private COABO coa;

    protected COAIDMapperEntity() {
    }

    public COAIDMapperEntity(Short constantId, COABO coa) {
        this.constantId = constantId;
        this.coa = coa;
    }

    public Short getConstantId() {
        return constantId;
    }

    public COABO getCoa() {
        return coa;
    }

    protected void setConstantId(Short constantId) {
        this.constantId = constantId;
    }

    protected void setCoa(COABO coa) {
        this.coa = coa;
    }

}
