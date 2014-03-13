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

import org.junit.Test;
import org.mifos.application.master.business.LookUpEntity;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.persistence.LegacyMasterDao;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.reports.struts.actionforms.ReportsCategoryActionForm;
import org.mifos.reports.util.helpers.ReportsConstants;
import org.mifos.security.rolesandpermission.business.ActivityEntity;
import org.mifos.security.rolesandpermission.persistence.LegacyRolesPermissionsDao;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportsCategoryActionStrutsTest extends MifosMockStrutsTestCase {

    @Autowired
    LegacyMasterDao legacyMasterDao;

    @Autowired
    LegacyRolesPermissionsDao legacyRolesPermissionsDao;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/reports-struts-config.xml");
    }

    @Test
    public void testShouldForwardToDefineNewCategoryPage() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "loadDefineNewCategoryPage");
        actionPerform();
        verifyForward("load_success");
        verifyForwardPath("/pages/application/reports/jsp/defineNewReportsCategory.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldPreviewSuccessIfCategoryNameIsNotNull() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("categoryName", "NotNull");
        actionPerform();
        verifyForward("preview_success");
        verifyForwardPath("/pages/application/reports/jsp/defineNewReportsCategoryPreview.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldPreviewFailureIfReportCategoryNameIsNull() {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "preview");
        actionPerform();

        verifyForwardPath("/reportsCategoryAction.do?method=validate");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAME };
        verifyActionErrors(errors);
    }

    @Test
    public void testCategoryNameShouldEqualsToInputValueWhenDoPreivewAction() throws Exception {
        String categoryName = "hahaCategoryName";
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("categoryName", categoryName);
        actionPerform();
       Assert.assertEquals(categoryName, ((ReportsCategoryActionForm) getActionForm()).getCategoryName());
    }

    @Test
    public void testShouldSubmitSuccess() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "addNewCategory");
        addRequestParameter("categoryName", "Haha");
        actionPerform();
        verifyForward("create_success");
        verifyForwardPath("/reportsCategoryAction.do?method=viewReportsCategory");
        verifyNoActionErrors();
        shouldForwardToDefineNewCategoryPageIfCategoryNameExisted();
    }

    private void shouldForwardToDefineNewCategoryPageIfCategoryNameExisted() {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "preview");
        addRequestParameter("categoryName", "Haha");
        actionPerform();
        addRequestParameter("method", "preview");
        addRequestParameter("categoryName", "Haha");
        actionPerform();

        verifyForwardPath("/pages/application/reports/jsp/defineNewReportsCategory.jsp");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAMEALREADYEXIST };
        verifyActionErrors(errors);

    }

    @Test
    public void testCategoryNameShouldBeNullBeforeDefineNewCategory() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");

        ReportsCategoryActionForm form = new ReportsCategoryActionForm();
        String categoryName = "Not Null";
        form.setCategoryName(categoryName);
        setActionForm(form);

        addRequestParameter("method", "loadDefineNewCategoryPage");

        actionPerform();

        Assert.assertNull(((ReportsCategoryActionForm) getActionForm()).getCategoryName());
    }

    @Test
    public void testShouldForwardToViewReportsCategoryPage() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "viewReportsCategory");
        actionPerform();
        verifyForwardPath("/pages/application/reports/jsp/viewReportsCategory.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldForwardToEditReportsCategoryPages() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "edit");
        addRequestParameter("categoryId", "1");
        actionPerform();
        verifyNoActionErrors();
        verifyForwardPath("/pages/application/reports/jsp/editReportsCategory.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldHaveReportCategoriesBOWhenViewReportsCategoryPage() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "viewReportsCategory");
        actionPerform();
        Assert.assertNotNull(getSession().getAttribute(ReportsConstants.LISTOFREPORTCATEGORIES));
    }

    @Test
    public void testCategoryNameShouldEqualsToInputValueWhenDoEditPreivewAction() throws Exception {
        String categoryName = "hahaCategoryName";
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "editPreview");
        addRequestParameter("categoryName", categoryName);
        addRequestParameter("categoryId", "1");
        actionPerform();
       Assert.assertEquals(categoryName, ((ReportsCategoryActionForm) getActionForm()).getCategoryName());
    }

    @Test
    public void testShouldSubmitSuccessAfterEdit() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");

        addRequestParameter("method", "editThenSubmit");
        ReportsCategoryActionForm form = new ReportsCategoryActionForm();
        String categoryName = "neverExist";
        form.setCategoryName(categoryName);
        form.setCategoryId((short) 1);
        setActionForm(form);
        actionPerform();
        verifyForward("create_success");
        verifyForwardPath("/reportsCategoryAction.do?method=viewReportsCategory");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldForwardToConfirmDeleteReportsCategoryPage() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "confirmDeleteReportsCategory");
        addRequestParameter("categoryId", "1");
        actionPerform();
        verifyForward("confirm_delete");
        verifyForwardPath("/pages/application/reports/jsp/confirmDeleteCategory.jsp");
    }

    @Test
    public void testShouldForwardToConfirmDeleteReportsCategoryPageWhenDeleteReportCategoryHasReports()
            throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "deleteReportsCategory");
        addRequestParameter("categoryId", "1");
        actionPerform();
        verifyForward("confirm_delete");
        verifyForwardPath("/pages/application/reports/jsp/confirmDeleteCategory.jsp");
    }

    @Test
    public void testShouldEditPreviewSuccessIfCategoryNameIsNotNull() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "editPreview");
        addRequestParameter("categoryName", "NotNull");
        addRequestParameter("categoryId", "1");
        actionPerform();
        verifyForward("editpreview_success");
        verifyForwardPath("/pages/application/reports/jsp/edit_preview_reports_category.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldEditPreviewFailureIfReportCategoryNameIsNull() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "editPreview");
        actionPerform();

        verifyForwardPath("/reportsCategoryAction.do?method=validate");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAME };
        verifyActionErrors(errors);
    }

    @Test
    public void testShouldEditPreviewFailureIfReportCategoryNotEdit() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        ReportsCategoryActionForm form = new ReportsCategoryActionForm();
        String categoryName = "categoryName3";
        form.setCategoryName(categoryName);
        form.setCategoryId((short) 1);
        setActionForm(form);
        addRequestParameter("method", "editThenSubmit");
        actionPerform();

        addRequestParameter("method", "editPreview");
        addRequestParameter("categoryName", "categoryName3");
        addRequestParameter("categoryId", "1");
        actionPerform();

        verifyForwardPath("/pages/application/reports/jsp/editReportsCategory.jsp");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAMENOTEDIT };
        verifyActionErrors(errors);
    }

    @Test
    public void testShouldEditPreviewFailureIfReportCategoryAlreadyExist() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "addNewCategory");
        addRequestParameter("categoryName", "jijiwaiwai");
        actionPerform();
        addRequestParameter("method", "editPreview");
        addRequestParameter("categoryName", "jijiwaiwai");
        addRequestParameter("categoryId", "1");
        actionPerform();

        verifyForwardPath("/pages/application/reports/jsp/editReportsCategory.jsp");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAMEALREADYEXIST };
        verifyActionErrors(errors);
    }

    @Test
    public void testShouldPreviewSuccessAfterEdit() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");

        ReportsCategoryActionForm form = new ReportsCategoryActionForm();
        String categoryName = "Not Null";
        form.setCategoryName(categoryName);
        form.setCategoryId((short) 1);
        setActionForm(form);

        addRequestParameter("method", "editPreview");

        actionPerform();
        verifyForwardPath("/pages/application/reports/jsp/edit_preview_reports_category.jsp");
        verifyNoActionErrors();
    }

    @Test
    public void testShouldEditFailureIfReportCategoryAlreadyExist() throws Exception {
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "addNewCategory");
        addRequestParameter("categoryName", "wuwulala");
        actionPerform();

        ReportsCategoryActionForm form = new ReportsCategoryActionForm();
        String categoryName = "wuwulala";
        form.setCategoryName(categoryName);
        form.setCategoryId((short) 1);
        setActionForm(form);
        addRequestParameter("method", "editThenSubmit");
        actionPerform();

        verifyForwardPath("/pages/application/reports/jsp/editReportsCategory.jsp");
        String[] errors = { ReportsConstants.ERROR_CATEGORYNAMEALREADYEXIST };
        verifyActionErrors(errors);
    }

    @Test
    public void testShouldCreateFailureWhenActivityIdOutOfRange() throws Exception {
        ActivityEntity activity = insertActivityForTest(Short.MIN_VALUE);
        setRequestPathInfo("/reportsCategoryAction.do");
        addRequestParameter("method", "addNewCategory");
        addRequestParameter("categoryName", "willFailureForRangeNewCategory");
        actionPerform();

        verifyForward("preview_failure");
        String[] errors = { ReportsConstants.ERROR_NOMOREDYNAMICACTIVITYID };
        verifyActionErrors(errors);

        deleteActivityForTest(activity);
    }

    public ActivityEntity insertActivityForTest(short activityId) throws PersistenceException {
        LookUpValueEntity anLookUp = new LookUpValueEntity();
        LookUpEntity lookUpEntity = legacyMasterDao.getPersistentObject(LookUpEntity.class, Short
                .valueOf((short) LookUpEntity.ACTIVITY));
        anLookUp.setLookUpEntity(lookUpEntity);
        ActivityEntity parent = legacyMasterDao.getPersistentObject(ActivityEntity.class, (short) 13);
        ActivityEntity activityEntity = new ActivityEntity(activityId, parent, anLookUp);
        legacyRolesPermissionsDao.createOrUpdate(anLookUp);
        legacyRolesPermissionsDao.createOrUpdate(activityEntity);
        return activityEntity;
    }

    private void deleteActivityForTest(ActivityEntity activityEntity) throws PersistenceException {
        LookUpValueEntity anLookUp = activityEntity.getActivityNameLookupValues();
        legacyRolesPermissionsDao.delete(activityEntity);
        legacyRolesPermissionsDao.delete(anLookUp);
    }

}
