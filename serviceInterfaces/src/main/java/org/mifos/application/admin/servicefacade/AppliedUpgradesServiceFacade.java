package org.mifos.application.admin.servicefacade;

import org.mifos.db.upgrade.ChangeSetInfo;
import org.mifos.db.upgrade.UnRunChangeSetInfo;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface AppliedUpgradesServiceFacade {
    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_VIEW_SYSTEM_INFO')")
    List<ChangeSetInfo> getAppliedUpgrades();

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_VIEW_SYSTEM_INFO')")
    List<UnRunChangeSetInfo> getUnRunChangeSets();
}
