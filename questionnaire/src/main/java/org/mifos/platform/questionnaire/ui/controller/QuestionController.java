/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */
package org.mifos.platform.questionnaire.ui.controller;

import org.apache.commons.lang.StringUtils;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.platform.questionnaire.QuestionnaireConstants;
import org.mifos.platform.questionnaire.service.QuestionDetail;
import org.mifos.platform.questionnaire.service.QuestionnaireServiceFacade;
import org.mifos.platform.questionnaire.ui.model.Question;
import org.mifos.platform.questionnaire.ui.model.QuestionForm;
import org.springframework.binding.message.MessageContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;

@Controller
@SuppressWarnings("PMD")
public class QuestionController extends QuestionnaireController {

    @SuppressWarnings({"UnusedDeclaration"})
    public QuestionController() {
        super();
    }

    public QuestionController(QuestionnaireServiceFacade questionnaireServiceFacade) {
        super(questionnaireServiceFacade);
    }

    @RequestMapping("/viewQuestions.ftl")
    public String getAllQuestions(ModelMap model) {
        model.addAttribute("questions", questionnaireServiceFacade.getAllQuestions());
        return "viewQuestions";
    }

    public String addQuestion(QuestionForm questionForm, RequestContext requestContext, boolean createMode) {
        MessageContext context = requestContext.getMessageContext();
        boolean result = validateQuestion(questionForm, context, createMode);
        if (result) {
            questionForm.addCurrentQuestion();
        }
        return result? "success": "failure";
    }

    public String addSmartChoiceTag(QuestionForm questionForm, RequestContext requestContext, int choiceIndex) {
        MessageContext context = requestContext.getMessageContext();
        boolean result = validateSmartChoice(questionForm, context, choiceIndex);
        if (result) {
            questionForm.getCurrentQuestion().addSmartChoiceTag(choiceIndex);
        }
        return result? "success": "failure";
    }

    private boolean validateSmartChoice(QuestionForm questionForm, MessageContext context, int choiceIndex) {
        boolean result = true;
        Question question = questionForm.getCurrentQuestion();

        if (context.hasErrorMessages()) {
            result = false;
        }

        else if (question.isSmartChoiceDuplicated(choiceIndex)) {
            constructErrorMessage(
                    context, "questionnaire.error.question.tags.duplicate",
                    "currentQuestion.answerChoices", "The tag with the same name already exists.");
            result = false;
        }

        else if (question.isTagsLimitReached(choiceIndex)) {
            constructErrorMessage(
                    context, "questionnaire.error.question.tags.limit",
                    "currentQuestion.answerChoices", "You cannot add more than five tags.");
            result = false;
        }

        return result;
    }

    private boolean validateQuestion(QuestionForm questionForm, MessageContext context, boolean createMode) {
        questionForm.validateConstraints(context);
        boolean result = true;
        String text = StringUtils.trim(questionForm.getCurrentQuestion().getText());

        if (context.hasErrorMessages()) {
            result = false;
        }

        else if (checkDuplicateTitleForCreateOperation(questionForm, createMode)) {
            constructErrorMessage(
                    context, "questionnaire.error.question.duplicate",
                    "currentQuestion.text", "The text specified already exists.");
            result = false;
        }

        else if (checkDuplicateTextForEditOperation(questionForm, createMode, text)) {
            constructErrorMessage(
                    context, "questionnaire.error.question.duplicate",
                    "currentQuestion.text", "The text specified already exists.");
            result = false;
        }

        else if(questionForm.answerChoicesAreInvalid()) {
            constructErrorMessage(
                    context, "questionnaire.error.question.choices",
                    "currentQuestion.choice", "Please specify at least 2 choices.");
            result = false;
        }

        else if (questionForm.numericBoundsAreInvalid()) {
            constructErrorMessage(
                    context, QuestionnaireConstants.INVALID_NUMERIC_BOUNDS,
                    "currentQuestion.numericMin", "Please ensure maximum value is greater than minimum value.");
            result = false;
        }

        return result;
    }

    private boolean checkDuplicateTextForEditOperation(QuestionForm questionForm, boolean createMode, String text) {
        return !createMode && questionForm.textHasChanged() && questionnaireServiceFacade.isDuplicateQuestion(text);
    }

    private boolean checkDuplicateTitleForCreateOperation(QuestionForm questionForm, boolean createMode) {
        return createMode && isDuplicateQuestion(questionForm);
    }

    public void removeQuestion(QuestionForm questionForm, String questionTitle) {
        questionForm.removeQuestion(questionTitle);
    }

    public String createQuestions(QuestionForm questionForm, RequestContext requestContext) {
        String result = "success";
        try {
            questionnaireServiceFacade.createQuestions(getQuestionDetails(questionForm));
        } catch (SystemException e) {
            constructAndLogSystemError(requestContext.getMessageContext(), e);
            result = "failure";
        }
        return result;
    }

    private List<QuestionDetail> getQuestionDetails(QuestionForm questionForm) {
        List<QuestionDetail> questionDetails = new ArrayList<QuestionDetail>();
        for (Question question : questionForm.getQuestions()) {
            questionDetails.add(question.getQuestionDetail());
        }
        return questionDetails;
    }

    private boolean isDuplicateQuestion(QuestionForm questionForm) {
        String text = StringUtils.trim(questionForm.getCurrentQuestion().getText());
        return questionForm.isDuplicateTitle(text) || questionnaireServiceFacade.isDuplicateQuestion(text);
    }

}
