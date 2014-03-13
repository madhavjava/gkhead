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

package org.mifos.ui.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This controller serves localized message bundle used by Datatables jQuery
 * plugin.
 * 
 * @see http://www.datatables.net/examples/advanced_init/language_file.html
 * @see http://www.datatables.net/usage/i18n (oLanguage.sUrl)
 */
@Controller
@RequestMapping(value = "/jqueryDatatableMessages")
public class JqueryDatatableMessageController {

    @RequestMapping(method = RequestMethod.GET)
    public void messages() {
        // empty
    }
}
