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

package org.mifos.platform.questionnaire.service.validators;

import org.mifos.platform.validations.ValidationException;
import org.mifos.platform.questionnaire.service.dtos.QuestionDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupDto;
import org.mifos.platform.questionnaire.service.dtos.SectionDto;

public interface QuestionGroupDtoValidator {
    void validate(QuestionGroupDto questionGroupDto, ValidationException validationException);
    void validate(SectionDto sectionDto, ValidationException validationException);
    void validate(QuestionDto questionDto, ValidationException validationException);
}
