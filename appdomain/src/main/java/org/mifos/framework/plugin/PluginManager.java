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

package org.mifos.framework.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.mifos.accounts.acceptedpaymenttype.persistence.LegacyAcceptedPaymentTypeDao;
import org.mifos.accounts.api.StandardAccountService;
import org.mifos.accounts.api.TransactionImport;
import org.mifos.accounts.loan.business.service.LoanBusinessService;
import org.mifos.accounts.loan.persistance.LegacyLoanDao;
import org.mifos.accounts.persistence.LegacyAccountDao;
import org.mifos.accounts.savings.persistence.GenericDaoHibernate;
import org.mifos.accounts.savings.persistence.SavingsDao;
import org.mifos.application.admin.servicefacade.MonthClosingServiceFacade;
import org.mifos.application.master.persistence.LegacyMasterDao;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.application.servicefacade.GroupLoanAccountServiceFacade;
import org.mifos.application.servicefacade.SavingsServiceFacade;
import org.mifos.customers.business.service.CustomerSearchServiceImpl;
import org.mifos.customers.persistence.CustomerDaoHibernate;
import org.mifos.customers.personnel.persistence.PersonnelDaoHibernate;
import org.mifos.framework.hibernate.helper.HibernateTransactionHelperForStaticHibernateUtil;
import org.mifos.framework.util.ConfigurationLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    /**
     * Returns specified import plugin or null.
     */
    public TransactionImport getImportPlugin(String importPluginClassname) {
        TransactionImport plugin = null;
        for (TransactionImport ti : loadImportPlugins()) {
            if (ti.getClass().getName().equals(importPluginClassname)) {
                plugin = ti;
            }
        }
        return plugin;
    }

    /**
     * Returns list of import plugins. Note that {@link ServiceLoader} caches
     * loads, so multiple invocations should not incur extra overhead.
     */
    public List<TransactionImport> loadImportPlugins() {
        List<TransactionImport> plugins = new ArrayList<TransactionImport>();
        ClassLoader pluginClassLoader = initializePluginClassLoader();
        ServiceLoader<TransactionImport> loader = ServiceLoader.load(TransactionImport.class, pluginClassLoader);
        for (TransactionImport ti : loader) {
            ti.setAccountService(new StandardAccountService(ApplicationContextProvider.getBean(LegacyAccountDao.class), ApplicationContextProvider.getBean(LegacyLoanDao.class),
                    ApplicationContextProvider.getBean(LegacyAcceptedPaymentTypeDao.class), new PersonnelDaoHibernate(new GenericDaoHibernate()),
                    new CustomerDaoHibernate(new GenericDaoHibernate()), ApplicationContextProvider.getBean(LoanBusinessService.class),
                    new HibernateTransactionHelperForStaticHibernateUtil(), ApplicationContextProvider.getBean(LegacyMasterDao.class),
                    ApplicationContextProvider.getBean(MonthClosingServiceFacade.class), ApplicationContextProvider.getBean(SavingsServiceFacade.class),
                    ApplicationContextProvider.getBean(GroupLoanAccountServiceFacade.class)));
        ti.setCustomerSearchService(new CustomerSearchServiceImpl(new CustomerDaoHibernate(new GenericDaoHibernate())));
            plugins.add(ti);
        }
        return plugins;
    }

    /**
     * Extend classloader by loading jars from ${MIFOS_CONF}/plugins at runtime
     *
     * @return pluginClassLoader
     */
    private ClassLoader initializePluginClassLoader() {
        ConfigurationLocator configurationLocator = new ConfigurationLocator();
        String libDir = configurationLocator.getConfigurationDirectory() + "/plugins";
        File dependencyDirectory = new File(libDir);
        File[] files = dependencyDirectory.listFiles();
        ArrayList<URL> urls = new ArrayList<URL>();
        if (files != null) {
            urls.addAll(getPluginURLs(files));
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
    }

    private ArrayList<URL> getPluginURLs(File[] files) {
        ArrayList<URL> urls = new ArrayList<URL>();
        for (File file : files) {
            if (file.getName().endsWith(".jar")) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn("plugin loading failed",e);
                }
            }
        }
        return urls;
    }

    public List<String> getImportPluginNames() {
        List<String> pluginNames = new ArrayList<String>();
        List<TransactionImport> plugins = loadImportPlugins();
        for (TransactionImport ti : plugins) {
            pluginNames.add(ti.getDisplayName());
        }
        return pluginNames;
    }
}
