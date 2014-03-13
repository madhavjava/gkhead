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

package org.mifos.reports.struts.action;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.security.util.ActivityContext;
import org.mifos.security.util.SecurityConstants;
import org.mifos.security.util.UserContext;

public class ReportsActionStrutsTest extends MifosMockStrutsTestCase {


    private UserContext userContext;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/reports-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {
        userContext = TestUtils.makeUser();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        ActivityContext ac = new ActivityContext((short) 0, userContext.getBranchId().shortValue(), userContext.getId()
                .shortValue());
        request.getSession(false).setAttribute("ActivityContext", ac);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testVerifyForwardOfNonExistentReportThrowsSecurityError() {
        addRequestParameter("viewPath", "report_designer");
        setRequestPathInfo("/reportsAction.do");
        addRequestParameter("method", "getReportPage");
        actionPerform();
        ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
        ActionMessage retrievedMessage = (ActionMessage) (errors).get(SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED)
                .next();
        ActionMessage expectedErrorMessage = new ActionMessage(SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED);
       Assert.assertEquals(expectedErrorMessage.toString(), retrievedMessage.toString());
    }

    @Test
    public void testSkipConvertFormObjectToBusinessObjectReturnsTrueForAnyMethod() throws Exception {
       Assert.assertTrue(new ReportsAction().skipActionFormToBusinessObjectConversion(null));
    }
}
