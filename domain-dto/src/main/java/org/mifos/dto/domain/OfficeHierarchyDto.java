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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EQ_COMPARETO_USE_OBJECT_EQUALS", justification="")
public class OfficeHierarchyDto implements Comparable<OfficeHierarchyDto> {

    private final Short officeId;
    private final String officeName;
    private final String searchId;
    private final boolean active;
    private final List<OfficeHierarchyDto> children;

    public OfficeHierarchyDto(Short officeId, String officeName, String searchId, boolean active,
            List<OfficeHierarchyDto> children) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.searchId = searchId;
        this.active = active;
        this.children = children;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public String getData() {
        return this.officeName;
    }

    public List<OfficeHierarchyDto> getChildren() {
        return this.children;
    }

    public Short getOfficeId() {
        return this.officeId;
    }

    public String getId() {
        return this.officeId.toString();
    }

    public Properties getAttr() {
        Properties properties = new Properties();
        properties.put("id", getId());
        return properties;
    }

    public String getSearchId() {
        return this.searchId;
    }

    public boolean isActive() {
        return this.active;
    }

    @Override
    public int compareTo(OfficeHierarchyDto other) {
        return officeName.compareTo(other.getOfficeName());
    }

    public String toJSONString() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        LinkedList<OfficeHierarchyDto> officeHierarchyList = new LinkedList<OfficeHierarchyDto>();
        officeHierarchyList.add(this);
        return jsonMapper.writeValueAsString(officeHierarchyList.toArray());
    }
}
