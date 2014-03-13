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

package org.mifos.application.master.business;

import org.mifos.framework.business.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * The entityType field should be a CamelCase name containing no whitespace (since it is used as part of a properties
 * file key value) The no whitespace requirement is enforced by the unit test
 * ApplicationConfigurationPersistenceIntegrationTest.testGetLookupEntities()
 */
@NamedQueries( {
  @NamedQuery(
    name="entities",
    query="from LookUpEntity "
  ),
  @NamedQuery(
    name="masterdata.mifosEntityValue",
    query="select new org.mifos.application.master.business.BusinessActivityEntity(value.lookUpId ,value.lookUpName, value.lookUpName) "+
          "from LookUpEntity entity, LookUpValueEntity value "+
          "where entity.entityId = value.lookUpEntity.entityId and entity.entityType=:entityType "
  ),
  @NamedQuery(
    name = "masterdata.entityvalue",
    query = "select new org.mifos.application.master.business.CustomValueDto(entity.entityId ,label.localeId,label.labelName) "
         + "from LookUpEntity entity, LookUpLabelEntity label "
         + "where entity.entityId = label.lookUpEntity.entityId and entity.entityType=:entityType"
  ),
  @NamedQuery(
          name = "findLookupEntityByEntityType",
          query = "from LookUpEntity entity where entity.entityType=:entityType"
  ),
  @NamedQuery(
    name = "masterdata.entitylookupvalue",
    query = "select new org.mifos.application.master.business.CustomValueListElementDto(lookup.lookUpId,lookup.lookUpName, lookup.lookUpName) "
                + "from LookUpValueEntity lookup, LookUpEntity entity "
                + "where entity.entityType=:entityType and lookup.lookUpEntity.entityId =entity.entityId"
  )

})
@Entity
@Table(name = "lookup_entity")
public class LookUpEntity extends AbstractEntity {

    public static final Short DEFAULT_LOCALE_ID = 1;

    public static final int ETHNICITY = 19;
    public static final int ACCOUNT_ACTION = 69;
    public static final int ACCOUNT_STATE_FLAG = 70;
    public static final int ACTIVITY = 87;
    public static final int REPAYMENT_RULE = 91;
    public static final int INTEREST_TYPES = 37;
    public static final int FINANCIAL_ACTION = 76;
    public static final int BUSINESS_ACTIVITY = 21;
    public static final int CITIZENSHIP = 18;

    @Id
    @GeneratedValue
    @Column(name="entity_id")
    private Short entityId;

    @Column(name="entity_name")
    private String entityType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "entity_id", updatable = false)
    private Set<LookUpLabelEntity> lookUpLabels;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "entity_id", updatable = false)
    private Set<LookUpValueEntity> lookUpValues;

    public LookUpEntity() {
        super();
    }

    public Short getEntityId() {
        return entityId;
    }

    public void setEntityId(Short entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Set<LookUpLabelEntity> getLookUpLabels() {
        return lookUpLabels;
    }

    public void setLookUpLabels(Set<LookUpLabelEntity> lookUpLabels) {
        this.lookUpLabels = lookUpLabels;
    }

    public Set<LookUpValueEntity> getLookUpValues() {
        return lookUpValues;
    }

    public void setLookUpValues(Set<LookUpValueEntity> lookUpValues) {
        this.lookUpValues = lookUpValues;
    }

    public String findLabel() {
        return findLabelForLocale(DEFAULT_LOCALE_ID);
    }

    public String findLabelKey() {
        return findLabelKeyForLocale(DEFAULT_LOCALE_ID);
    }

    private String findLabelForLocale(Short localeId) {
        for (LookUpLabelEntity lookUpLabel : lookUpLabels) {
            if (lookUpLabel.getLocaleId().equals(localeId)) {
                return lookUpLabel.getLabelText();
            }
        }
//        throw new RuntimeException("Label not found for locale with id: \"" + localeId + "\"");
        return "";
    }

    private String findLabelKeyForLocale(Short localeId) {
        for (LookUpLabelEntity lookUpLabel : lookUpLabels) {
            if (lookUpLabel.getLocaleId().equals(localeId)) {
                return lookUpLabel.getLabelKey();
            }
        }
        return "";
    }
}
