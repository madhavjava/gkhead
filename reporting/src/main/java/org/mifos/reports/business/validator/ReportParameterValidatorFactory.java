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

package org.mifos.reports.business.validator;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.framework.util.helpers.FilePaths;
import org.mifos.reports.business.ReportParameterForm;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReportParameterValidatorFactory {
    private static Collection<ReportParameterValidator> validators;

    @SuppressWarnings("unchecked")
    public ReportParameterValidator<ReportParameterForm> getValidator(final String reportFilePath) {
        initValidators();
        return (ReportParameterValidator<ReportParameterForm>) CollectionUtils.find(validators, new Predicate() {
            @Override
            public boolean evaluate(Object validator) {
                return ((ReportParameterValidator<ReportParameterForm>) validator)
                        .isApplicableToReportFilePath(reportFilePath);
            }
        });
    }

    private void initValidators() {
        if (validators == null) {
            validators = ApplicationContextProvider.getApplicationContext().getBeansOfType(ReportParameterValidator.class).values();
        }
    }
}
