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

package org.mifos.reports.admindocuments.struts.action;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.reports.admindocuments.business.AdminDocumentBO;
import org.mifos.reports.admindocuments.persistence.LegacyAdminDocumentDao;
import org.mifos.reports.admindocuments.struts.actionforms.BirtAdminDocumentUploadActionForm;
import org.mifos.reports.business.MockFormFile;
import org.mifos.reports.business.ReportsBO;
import org.mifos.reports.business.ReportsJasperMap;
import org.mifos.reports.persistence.ReportsPersistence;
import org.mifos.security.rolesandpermission.business.ActivityEntity;
import org.mifos.security.rolesandpermission.persistence.LegacyRolesPermissionsDao;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class BirtAdminDocumentUploadActionStrutsTest extends MifosMockStrutsTestCase {

    @Autowired
    private LegacyAdminDocumentDao legacyAdminDocumentDao;

    @Autowired
    LegacyRolesPermissionsDao legacyRolesPermissionsDao;

    @Override
    protected void setStrutsConfig() throws IOException {
        super.setStrutsConfig();
        setConfigFile("/WEB-INF/struts-config.xml,/WEB-INF/admindocument-struts-config.xml");
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetBirtAdminDocumentUploadPage() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "getBirtAdminDocumentUploadPage");
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testShouldSubmitSucessWhenUploadNewAdminDocument() throws Exception {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");

        BirtAdminDocumentUploadActionForm form = new BirtAdminDocumentUploadActionForm();
        form
                .setAdminiDocumentTitle("testShouldSubmitSucessWhenUploadNewAdminDocumentWithAVeryLongNameThatExceedsOneHundredCharactersInLength");
        form.setIsActive("1");
        form.setFile(new MockFormFile("testFileName1.rptdesign"));
        setActionForm(form);

        addRequestParameter("method", "upload");
        actionPerform();

        AdminDocumentBO adminDocument = (AdminDocumentBO) request.getAttribute(Constants.BUSINESS_KEY);
        Assert.assertNotNull(adminDocument);
        ReportsPersistence rp = new ReportsPersistence();
        ReportsJasperMap jasper = rp.getPersistentObject(ReportsJasperMap.class, adminDocument
                .getAdmindocId());
        Assert.assertNotNull(jasper);

        verifyNoActionErrors();
        verifyForward("create_success");

        removeReport(adminDocument.getAdmindocId());

    }

    private void removeReport(Short reportId) throws PersistenceException {
        legacyAdminDocumentDao.getSession().clear();
        ReportsBO report = legacyAdminDocumentDao.getPersistentObject(ReportsBO.class, reportId);
        ActivityEntity activityEntity = legacyRolesPermissionsDao.getPersistentObject(ActivityEntity.class,
                report.getActivityId());
        legacyAdminDocumentDao.delete(report);

        LookUpValueEntity anLookUp = activityEntity.getActivityNameLookupValues();

        legacyRolesPermissionsDao.delete(activityEntity);
        legacyRolesPermissionsDao.delete(anLookUp);

        StaticHibernateUtil.flushSession();
    }

    @Test
    public void testLoadProductInstance() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "loadProductInstance");
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testGetViewBirtAdminDocumentPage() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "loadProductInstance");
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testUpload() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "upload");
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testEdit() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "edit");
        addRequestParameter("admindocId", "1");
        actionPerform();
        AdminDocumentBO adminDocument = (AdminDocumentBO) request.getAttribute(Constants.BUSINESS_KEY);
       Assert.assertEquals("1", adminDocument.getAdmindocId().toString());
        verifyNoActionErrors();
        verifyForward(ActionForwards.edit_success.toString());

    }

    @Test
    public void testEditThenUpload() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "editThenUpload");
        actionPerform();
        verifyNoActionErrors();
    }

    @Test
    public void testDownloadAdminDocument() {
        setRequestPathInfo("/birtAdminDocumentUploadAction.do");
        addRequestParameter("method", "downloadAdminDocument");
        addRequestParameter("admindocId", "1");
        actionPerform();
        verifyNoActionErrors();
    }

}
