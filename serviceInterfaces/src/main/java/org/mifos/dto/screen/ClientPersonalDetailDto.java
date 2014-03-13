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

package org.mifos.dto.screen;

import java.io.Serializable;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID"}, justification="should disable at filter level and also for pmd - not important for us")
public class ClientPersonalDetailDto implements Serializable {

    private Integer ethnicity;
    private Integer citizenship;
    private Integer handicapped;
    private Integer businessActivities;
    private Integer maritalStatus;
    private Integer educationLevel;
    private Short numChildren;
    private Short gender;
    private Short povertyStatus;

    public static final int MARRIED = 66;

    public ClientPersonalDetailDto() {
        super();
    }

    public ClientPersonalDetailDto(Integer ethnicity, Integer citizenship, Integer handicapped, Integer businessActivities,
            Integer maritalStatus, Integer educationLevel, Short numChildren, Short gender, Short povertyStatus) {
        this.ethnicity = ethnicity;
        this.citizenship = citizenship;
        this.handicapped = handicapped;
        this.businessActivities = businessActivities;
        this.maritalStatus = maritalStatus;
        this.educationLevel = educationLevel;
        this.numChildren = numChildren;
        this.gender = gender;
        this.povertyStatus = povertyStatus;
    }

    public Integer getBusinessActivities() {
        return businessActivities;
    }

    public void setBusinessActivities(Integer businessActivities) {
        this.businessActivities = businessActivities;
    }

    public Integer getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Integer citizenship) {
        this.citizenship = citizenship;
    }

    public Integer getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(Integer educationLevel) {
        this.educationLevel = educationLevel;
    }

    public Integer getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(Integer ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Short getGender() {
        return gender;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public Integer getHandicapped() {
        return handicapped;
    }

    public void setHandicapped(Integer handicapped) {
        this.handicapped = handicapped;
    }

    public Integer getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Integer maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Short getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(Short numChildren) {
        this.numChildren = numChildren;
    }

    public Short getPovertyStatus() {
        return povertyStatus;
    }

    public void setPovertyStatus(Short povertyStatus) {
        this.povertyStatus = povertyStatus;
    }

}
