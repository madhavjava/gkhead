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

package org.mifos.framework.hibernate.helper;

import org.mifos.framework.business.AbstractBusinessObject;
import org.mifos.framework.components.audit.util.helpers.AuditInterceptor;

/**
 * Implementation of {@link HibernateTransactionHelper} for {@link StaticHibernateUtil}.
 */
public class HibernateTransactionHelperForStaticHibernateUtil implements HibernateTransactionHelper {

    @Override
    public void beginAuditLoggingFor(AbstractBusinessObject domainEntity) {
        StaticHibernateUtil.getSessionTL();
        ((AuditInterceptor) StaticHibernateUtil.getInterceptor()).createInitialValueMap(domainEntity);
    }

    @Override
    public void startTransaction() {
        StaticHibernateUtil.startTransaction();
    }

    @Override
    public void commitTransaction() {
        StaticHibernateUtil.commitTransaction();
    }

    @Override
    public void rollbackTransaction() {
        StaticHibernateUtil.rollbackTransaction();
    }

    @Override
    public void closeSession() {
        StaticHibernateUtil.closeSession();
    }

    @Override
    public void flushAndClearSession() {
        StaticHibernateUtil.flushAndClearSession();
    }

    @Override
    public void flushSession() {
        StaticHibernateUtil.flushSession();
    }

}