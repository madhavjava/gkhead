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
package org.mifos.reports.pentaho.util;

public enum PentahoOutputType {

    PDF(0, "PDF", "application/pdf", ".pdf"),
    XLS(1, "XLS", "application/vnd.ms-excel", ".xls"),
    RTF(2, "RTF", "application/rtf", ".rtf"),
    HTML(3, "HTML", "text/html", ".html"),
    XML(4, "XML", "text/xml", ".xml"),
    CSV(5, "CSV", "text/csv", ".csv");

    private final Integer id;
    private final String displayName;
    private final String contentType;
    private final String fileExtension;

    private PentahoOutputType(Integer id, String displayName, String contentType, String fileExtension) {
        this.id = id;
        this.displayName = displayName;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public Integer getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static PentahoOutputType findById(Integer id) {
        PentahoOutputType result = null;
        for (PentahoOutputType val : PentahoOutputType.values()) {
            if (id.equals(val.getId())) {
                result = val;
                break;
            }
        }
        return result;
    }
}
