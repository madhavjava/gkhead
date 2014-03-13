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

package org.mifos.framework.util;

import java.io.File;

/* package local helper, not public */ 
class ConfigurationLocatorHelper {

    public File getFile(String directory) {
        return new File (directory);
    }

    public String getHomeProperty(String homePropertyName) {
      return System.getProperty(homePropertyName);
    }

    public String getEnvironmentProperty(String environmentPropertyName) {
        String environmentProperty = System.getenv(environmentPropertyName);
        if (environmentProperty == null) {
            environmentProperty = evaluateForMifosConf(environmentPropertyName);
        }
        return environmentProperty;
    }

    private String evaluateForMifosConf(String environmentPropertyName) {
        String property = null;
        if ("MIFOS_CONF".equals(environmentPropertyName)) {
            property = getHomeProperty("mifos.conf");
            if (property == null) {
                property = getHomeProperty("user.home") + "/.mifos";
            }
        }
        return property;
    }
}