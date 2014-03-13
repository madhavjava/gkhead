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

package org.mifos.test.acceptance.questionnaire;

import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.UiTestCaseBase;
import org.mifos.test.acceptance.framework.questionnaire.AttachQuestionGroupParameters;
import org.mifos.test.acceptance.framework.testhelpers.QuestionGroupTestHelper;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:ui-test-context.xml"})
@SuppressWarnings("PMD.CyclomaticComplexity")
@Test(singleThreaded = true, groups = {"client", "acceptance", "ui", "no_db_unit"})
public class QuestionGroupCenterTest extends UiTestCaseBase {

    private QuestionGroupTestHelper questionGroupTestHelper;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        questionGroupTestHelper = new QuestionGroupTestHelper(selenium);
    }

    @AfterMethod
    public void logOut() {
        (new MifosPage(selenium)).logout();
    }

    /**
     * Attaching a Question Group to View Center and capturing responses
     * http://mifosforge.jira.com/browse/MIFOSTEST-661
     * @throws Exception
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void verifyAttachingQuestionGroupToCenter() throws Exception {
        AttachQuestionGroupParameters attachParams = new AttachQuestionGroupParameters();
        attachParams.setTarget("Default Center");
        attachParams.setQuestionGroupName("ViewCenterQG");
        attachParams.addTextResponse("Date", "09/02/2011");
        attachParams.addTextResponse("question 3", "25/02/2011");
        attachParams.addCheckResponse("question 4", "yes");
        attachParams.addTextResponse("DateQuestion", "19/02/2011");
        attachParams.addTextResponse("Number", "60");
        attachParams.addTextResponse("question 1", "tekst tekst");
        attachParams.addTextResponse("Text", "ale alo olu");
        AttachQuestionGroupParameters attachErrorParams = new AttachQuestionGroupParameters();
        attachErrorParams.setTarget("Default Center");
        attachErrorParams.setQuestionGroupName("ViewCenterQG");
        attachErrorParams.addTextResponse("question 3", "25/02/2011");
        attachErrorParams.addTextResponse("Number", "sdfsdf");
        attachErrorParams.addCheckResponse("question 4", "yes");
        attachErrorParams.addError("Please specify Date.");
        attachErrorParams.addError("Please specify a number for Number.");
        attachErrorParams.addError("Please specify Text.");

        questionGroupTestHelper.verifyErrorsWhileAttachingQuestionGroupToCenter(attachErrorParams);
        questionGroupTestHelper.attachQuestionGroupToCenter(attachParams);

        attachParams.addTextResponse("Number", "20");
        attachParams.addTextResponse("question 3", "21/02/2011");
        questionGroupTestHelper.editQuestionGroupResponsesInCenter(attachParams);
    }

}
