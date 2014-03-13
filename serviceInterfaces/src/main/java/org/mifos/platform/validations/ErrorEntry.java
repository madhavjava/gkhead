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

package org.mifos.platform.validations;

import java.io.Serializable;
import java.util.List;

import org.mifos.platform.util.CollectionUtils;

@SuppressWarnings("PMD")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"SE_NO_SERIALVERSIONID", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification="should disable at filter level and also for pmd - not important for us")
public class ErrorEntry implements Serializable {
    
    private final String fieldName;
    private final String errorCode;
    private final String defaultMessage;
    private List<String> args;

    public ErrorEntry(String errorCode, String fieldName) {
        this(errorCode, fieldName, null);
    }

    public ErrorEntry(String errorCode, String fieldName, String defaultMessage) {
        this.errorCode = errorCode;
        this.fieldName = fieldName;
        this.defaultMessage = defaultMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isSameField(String fieldName) {
        return this.fieldName.equals(fieldName);
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public boolean hasErrorArgs() {
        return CollectionUtils.isNotEmpty(args);
    }

    public List<String> getArgs() {
        return args;
    }
}