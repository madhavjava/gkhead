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

package org.mifos.framework.components.batchjobs;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.mifos.core.MifosResourceUtil;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.components.batchjobs.exceptions.TaskSystemException;
import org.mifos.framework.util.ConfigurationLocator;

public class MifosSchedulerIntegrationTest extends MifosIntegrationTestCase {

    @Test @Ignore
    public void testRegisterTasks() throws Exception {

        MifosScheduler mifosScheduler = getMifosScheduler("org/mifos/framework/components/batchjobs/schedulerTestTask.xml");
        List<String> taskNames = mifosScheduler.getTaskNames();

        Assert.assertEquals(8, taskNames.size());
        Assert.assertTrue(taskNames.contains("ProductStatusJob"));
        Assert.assertTrue(taskNames.contains("SavingsIntPostingTaskJob"));
        Assert.assertTrue(taskNames.contains("LoanArrearsAndPortfolioAtRiskTaskJob"));
        Assert.assertTrue(taskNames.contains("ApplyCustomerFeeChangesTaskJob"));
        Assert.assertTrue(taskNames.contains("GenerateMeetingsForCustomerAndSavingsTaskJob"));
        Assert.assertTrue(taskNames.contains("LoanArrearsAgingTaskJob"));
        Assert.assertTrue(taskNames.contains("ApplyHolidayChangesTaskJob"));
        Assert.assertTrue(taskNames.contains("BranchReportTaskJob"));
    }

    @Test @Ignore
    public void testCallsConfigurationLocator() throws Exception {

        MifosScheduler mifosScheduler = getMifosScheduler("org/mifos/framework/components/batchjobs/schedulerTestTask2.xml");
        List<String> taskNames = mifosScheduler.getTaskNames();

        Assert.assertEquals(1, taskNames.size());
        Assert.assertTrue(taskNames.contains("MockTaskJob"));
    }

    private MifosScheduler getMifosScheduler(String taskConfigurationPath) throws TaskSystemException, IOException, FileNotFoundException {
        ConfigurationLocator mockConfigurationLocator = createMock(ConfigurationLocator.class);
        expect(mockConfigurationLocator.getResource(SchedulerConstants.CONFIGURATION_FILE_NAME)).andReturn(
                MifosResourceUtil.getClassPathResourceAsResource(taskConfigurationPath));
        expectLastCall().times(2);
        replay(mockConfigurationLocator);

        MifosScheduler mifosScheduler = new MifosScheduler();
        mifosScheduler.setConfigurationLocator(mockConfigurationLocator);
        mifosScheduler.initialize();

        return mifosScheduler;
    }

}
