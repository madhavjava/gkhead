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

package org.mifos.platform.questionnaire.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.platform.questionnaire.domain.QuestionEntity;
import org.mifos.platform.questionnaire.domain.QuestionState;
import org.mifos.platform.questionnaire.domain.QuestionnaireService;
import org.mifos.platform.questionnaire.service.QuestionDetail;
import org.mifos.platform.questionnaire.service.QuestionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mifos.platform.questionnaire.service.QuestionType.DATE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-questionnaire-dbContext.xml", "/test-questionnaire-persistenceContext.xml", "/META-INF/spring/QuestionnaireContext.xml"})
@TransactionConfiguration(transactionManager = "platformTransactionManager", defaultRollback = true)
@SuppressWarnings("PMD")
public class QuestionDaoIntegrationTest {

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Test
    @Transactional
    public void testCountOfQuestionsWithTitle() throws SystemException {
        String questionTitle = "Title" + System.currentTimeMillis();
        List<Long> result = questionDao.retrieveCountOfQuestionsWithText(questionTitle);
        assertEquals(Long.valueOf(0), result.get(0));
        defineQuestion(questionTitle, DATE);
        result = questionDao.retrieveCountOfQuestionsWithText(questionTitle);
        assertEquals(Long.valueOf(1), result.get(0));
    }

    @Test
    @Transactional
    public void testRetrieveByState() throws SystemException {
        QuestionDetail questionDetail2 = defineQuestion("Title2" + System.currentTimeMillis(), QuestionType.NUMERIC);
        QuestionDetail questionDetail1 = defineQuestion("Title1" + System.currentTimeMillis(), QuestionType.NUMERIC);
        List<String> expectedTitles = asList(questionDetail1.getText(), questionDetail2.getText());
        List<QuestionEntity> actualQuestions = questionDao.retrieveByState(QuestionState.ACTIVE.getValue());
        assertThat(actualQuestions.size(), is(2));
        assertThat(actualQuestions.get(0).getQuestionText(), is(expectedTitles.get(0)));
        assertThat(actualQuestions.get(1).getQuestionText(), is(expectedTitles.get(1)));
    }

    @Test
    @Transactional
    public void testRetrieveByStateExcluding() throws SystemException {
        QuestionDetail questionDetail3 = defineQuestion("Title3" + System.currentTimeMillis(), QuestionType.NUMERIC);
        QuestionDetail questionDetail2 = defineQuestion("Title2" + System.currentTimeMillis(), QuestionType.NUMERIC);
        QuestionDetail questionDetail1 = defineQuestion("Title1" + System.currentTimeMillis(), QuestionType.NUMERIC);
        List<String> expectedTitles = asList(questionDetail1.getText(), questionDetail2.getText());
        List<QuestionEntity> actualQuestions = questionDao.retrieveByStateExcluding(asList(questionDetail3.getId()), QuestionState.ACTIVE.getValue());
        assertThat(actualQuestions.size(), is(2));
        assertThat(actualQuestions.get(0).getQuestionText(), is(expectedTitles.get(0)));
        assertThat(actualQuestions.get(1).getQuestionText(), is(expectedTitles.get(1)));
    }

    private QuestionDetail defineQuestion(String questionTitle, QuestionType questionType) throws SystemException {
        return questionnaireService.defineQuestion(new QuestionDetail(questionTitle, questionType));
    }
}
