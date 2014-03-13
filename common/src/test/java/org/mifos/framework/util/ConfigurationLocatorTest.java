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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationLocatorTest {

    private static final String EXPECTED_PATH = "/Users/caitie/.mifos";

    // class under test
    private ConfigurationLocator configurationLocator;

    @Mock
    private File file;

    @Mock
    private ConfigurationLocatorHelper configurationLocatorHelper;

    @Before
    public void setup() {
        configurationLocator = new ConfigurationLocator();
        configurationLocator.setConfigurationLocatorHelper(configurationLocatorHelper);
    }

    @Test
    public void testGetResource() throws IOException {

        // exercise test
        Resource resource = configurationLocator.getResource("mock.mifosChartOfAccounts.xml");

        // verification
        Assert.assertNotNull(resource);
        Assert.assertTrue(resource.exists());
    }

    public void testGetFileHandleFailure() throws IOException {
        // exercise test
        configurationLocator.getResource("x.xml");
    }

    @Test
    public void testGetConfigurationDirectory() {

        // stubbing
        when(file.exists()).thenReturn(true);

        when(configurationLocatorHelper.getFile(EXPECTED_PATH)).thenReturn(file);
        when(configurationLocatorHelper.getHomeProperty("user.home")).thenReturn("/Users/caitie");

        // exercise test
        String configurationDirectory = configurationLocator.getConfigurationDirectory();

        // verification
        Assert.assertNotNull(configurationDirectory);
        Assert.assertEquals(EXPECTED_PATH, configurationDirectory);
    }

    @Test
    public void testResolvePath() {
        when(configurationLocatorHelper.getEnvironmentProperty("MIFOS_CONF")).thenReturn("/users/admin/.mifos");
        when(configurationLocatorHelper.getHomeProperty("question.group.folder")).thenReturn("question/groups/xmls");
        String path = configurationLocator.resolvePath("$MIFOS_CONF/${question.group.folder}/ppi/PPIIndia.xml");
        Assert.assertEquals("/users/admin/.mifos/question/groups/xmls/ppi/PPIIndia.xml", path);
        verify(configurationLocatorHelper, times(1)).getEnvironmentProperty("MIFOS_CONF");
        verify(configurationLocatorHelper, times(1)).getHomeProperty("question.group.folder");
        Assert.assertEquals("/users/admin/.mifos/question/groups/xmls/ppi/PPIIndia.xml", configurationLocator.resolvePath("/users/admin/.mifos/question/groups/xmls/ppi/PPIIndia.xml"));
    }
}
