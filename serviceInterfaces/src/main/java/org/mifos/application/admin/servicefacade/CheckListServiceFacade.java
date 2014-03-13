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

package org.mifos.application.admin.servicefacade;

import java.util.List;

import org.mifos.dto.domain.CheckListMasterDto;
import org.mifos.dto.screen.AccountCheckBoxItemDto;
import org.mifos.dto.screen.CheckListStatesView;
import org.mifos.dto.screen.CustomerCheckBoxItemDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CheckListServiceFacade {

    List<CustomerCheckBoxItemDto> retreiveAllCustomerCheckLists();

    List<AccountCheckBoxItemDto> retreiveAllAccountCheckLists();

    @PreAuthorize("isFullyAuthenticated() and hasAnyRole('ROLE_DEFINE_CHECKLIST', 'ROLE_MODIFY_CHECKLIST')")
    List<CheckListMasterDto> retrieveChecklistMasterData();

    @PreAuthorize("isFullyAuthenticated()")
    List<CheckListStatesView> retrieveAllCustomerStates(Short levelId);

    @PreAuthorize("isFullyAuthenticated()")
    List<CheckListStatesView> retrieveAllAccountStates(Short prdTypeId);

    @PreAuthorize("isFullyAuthenticated()")
    void validateIsValidCheckListState(Short masterTypeId, Short stateId, boolean isCustomer);

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_DEFINE_CHECKLIST')")
    void createCustomerChecklist(Short levelId, Short stateId, String checklistName, List<String> checklistDetails);

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_DEFINE_CHECKLIST')")
    void createAccountChecklist(Short productId, Short stateId, String checklistName, List<String> checklistDetails);

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_MODIFY_CHECKLIST')")
    void updateCustomerChecklist(Short checklistId, Short levelId, Short stateId, Short checklistStatus,
            String checklistName, List<String> details);

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_MODIFY_CHECKLIST')")
    void updateAccountChecklist(Short checklistId, Short productId, Short stateId, Short checklistStatus,
            String checklistName, List<String> details);
}
