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

package org.mifos.framework.components.audit.util.helpers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.master.persistence.LegacyMasterDao;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.config.Localization;
import org.mifos.customers.office.business.OfficeLevelEntity;
import org.mifos.customers.office.business.OfficeStatusEntity;
import org.mifos.customers.personnel.business.PersonnelLevelEntity;
import org.mifos.customers.personnel.business.PersonnelStatusEntity;
import org.mifos.dto.domain.ValueListElement;
import org.mifos.framework.components.audit.persistence.LegacyAuditLookupValuesDao;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.util.helpers.FilePaths;

@SuppressWarnings("unchecked")
public class AuditConfiguration {

    private static final Map<String, String> entityToClassPath = new HashMap<String, String>();

    public static final Map<Object, Map> entityMap = new HashMap<Object, Map>();
    private Map<String, Map> propertyMap;
    private Map<String, String> valueMap;

    public static final Map<Object, Map> entityMapForColumn = new HashMap<Object, Map>();
    private Map<String, String> columnPropertyMap;

    public static final Map<Object, EntitiesToLog> entitiesToLog = new HashMap<Object, EntitiesToLog>();

    private PropertyResourceBundle columnNames;
    static private Locale locale;

    private Set<Short> locales;

    private LegacyMasterDao legacyMasterDao = ApplicationContextProvider.getBean(LegacyMasterDao.class);

    public static AuditConfiguration auditConfigurtion = new AuditConfiguration();

    private AuditConfiguration() {
        locales = Localization.getInstance().getLocaleIdSet();
        locale = Localization.getInstance().getConfiguredLocale();
        columnNames = (PropertyResourceBundle) PropertyResourceBundle.getBundle(
                FilePaths.COLUMN_MAPPING_BUNDLE_PROPERTYFILE, locale, 
                this.getClass().getClassLoader(), new UTF8Control());

    }

    public static void init(Locale currentLocale) throws SystemException {
        auditConfigurtion.createEntityValueMap();
        locale = currentLocale;
    }

    private void createEntityValueMap() throws SystemException {
        ColumnPropertyMapping columnPropertyMapping = XMLParser.getInstance().parser();
        EntityType[] entityTypes = columnPropertyMapping.getEntityTypes();
        for (EntityType entityType : entityTypes) {
            entityToClassPath.put(entityType.getClassPath(), entityType.getName());
            entityMapForColumn.put(entityType.getName(), createColumnNames(entityType));
            entityMap.put(entityType.getName(), createPropertyNames(entityType.getPropertyNames()));
            entitiesToLog.put(entityType.getName(), entityType.getEntitiesToLog());
        }
    }

    public static String getEntityToClassPath(String classPath) {
        return entityToClassPath.get(classPath);
    }

    public static boolean isObjectToBeLogged(String entityType, String name, String parentName) {
        Boolean flag = false;
        EntitiesToLog objectToBeLogged = entitiesToLog.get(entityType);
        if (objectToBeLogged == null || objectToBeLogged.getEntities() == null) {
            return flag;
        }
        Entity[] entities = objectToBeLogged.getEntities();
        for (Entity entity : entities) {
            if (entity.getName().equalsIgnoreCase(name)) {
                if (parentName == null) {
                    flag = entity.getParentName() == null;
                    return flag;
                } else if (entity.getParentName() != null) {
                    flag = entity.getParentName().equalsIgnoreCase(parentName);
                    return flag;
                }
            }
        }
        return flag;
    }

    public static boolean isObjectPropertiesToBeMerged(String entityType, String name, String parentName) {
        EntitiesToLog objectToBeLogged = entitiesToLog.get(entityType);
        if (objectToBeLogged == null || objectToBeLogged.getEntities() == null) {
            return false;
        }
        Entity[] entities = objectToBeLogged.getEntities();
        for (Entity entity : entities) {
            if (entity.getName().equalsIgnoreCase(name)) {
                if (parentName == null) {
                    if (entity.getParentName() == null && entity.getMergeProperties() != null
                            && entity.getMergeProperties().equalsIgnoreCase("yes")) {
                        return true;
                    }
                } else {
                    if (entity.getParentName().equalsIgnoreCase(parentName) && entity.getMergeProperties() != null
                            && entity.getMergeProperties().equalsIgnoreCase("yes")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getValueOfCorrespondingId(String entityType, String propertyName, Object id, Short localeId) {
        Map<String, Map> propertyMap = entityMap.get(entityType);
        String propName = propertyName + "_" + localeId;
        Map<String, String> valueMap = propertyMap.get(propName);
        String value = null;
        if (id == null || valueMap == null) {
            return "";
        }
        value = valueMap.get(id.toString());
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    public static boolean checkForPropertyName(String entityType, String propertyName, Short localeId) {
        Map<String, Map> propertyMap = entityMap.get(entityType);
        if (propertyMap == null) {
            return false;
        }
        String propName = propertyName + "_" + localeId;
        Map<String, String> valueMap = propertyMap.get(propName);
        if (valueMap == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String getColumnNameForPropertyName(String entityType, String propertyName) {
        String columnName = "";
        Map<String, String> columnPropertyMap = entityMapForColumn.get(entityType);
        if (columnPropertyMap == null) {
            return "";
        }
        columnName = columnPropertyMap.get(propertyName);
        if (columnName == null) {
            return "";
        } else {
            return columnName;
        }
    }

    private Map<String, String> createColumnNames(EntityType entityType) {
        columnPropertyMap = new HashMap<String, String>();
        EntitiesToLog objectToBeLogged = entityType.getEntitiesToLog();
        if (objectToBeLogged != null && objectToBeLogged.getEntities() != null
                && objectToBeLogged.getEntities().length > 0) {
            Entity[] entities = objectToBeLogged.getEntities();
            for (Entity entity : entities) {
                if (entity != null) {
                    if (entity.getMergeProperties() != null && entity.getMergeProperties().equalsIgnoreCase("yes")) {
                        if (entity.getParentName() == null) {
                            columnPropertyMap.put(entity.getName(), getColumnName(entity.getDisplayKey()));
                        } else {
                            columnPropertyMap.put(entity.getParentName().concat(entity.getName()), getColumnName(entity
                                    .getDisplayKey()));

                        }
                    }
                }
            }
        }
        PropertyName[] propertyNames = entityType.getPropertyNames();
        for (PropertyName propertyName2 : propertyNames) {
            String propertyName = null;
            if (propertyName2.getParentName() != null) {
                propertyName = propertyName2.getParentName().concat(propertyName2.getName());
            } else {
                propertyName = propertyName2.getName();
            }
            if (propertyName2.getDoNotLog().equalsIgnoreCase(XMLConstants.YES)) {
                columnPropertyMap.put(propertyName, XMLConstants.DONOTLOGTHISPROPERTY);
            } else {
                columnPropertyMap.put(propertyName, getColumnName(propertyName2.getDisplayKey()));
            }
        }
        return columnPropertyMap;
    }

    private Map<String, Map> createPropertyNames(PropertyName[] propertyNames) throws SystemException {
        propertyMap = new HashMap<String, Map>();
        for (PropertyName propertyName : propertyNames) {
            if (propertyName.getLookUp().equalsIgnoreCase(XMLConstants.YES)
                    && propertyName.getMethodName() == null) {
                for (Short localeId : locales) {
                    if (propertyName.getParentName() != null) {
                        propertyMap.put(propertyName.getParentName().concat(propertyName.getName()).concat(
                                "_" + localeId), createValueMap(propertyName.getEntityName(),
                                localeId));
                    } else {
                        propertyMap.put(propertyName.getName().concat("_" + localeId),
                                createValueMap(propertyName.getEntityName(), localeId));
                    }
                }
            } else if (propertyName.getLookUp().equalsIgnoreCase(XMLConstants.YES)
                    && propertyName.getMethodName() != null) {
                for (Short localeId : locales) {
                    if (propertyName.getParentName() != null) {
                        propertyMap.put(propertyName.getParentName().concat(propertyName.getName()).concat(
                                "_" + localeId), callMethodToCreateValueMap(propertyName
                                .getMethodName(), localeId));
                    } else {
                        propertyMap.put(propertyName.getName().concat("_" + localeId),
                                callMethodToCreateValueMap(propertyName.getMethodName(), localeId));
                    }
                }
            }
        }
        return propertyMap;
    }

    //FIXME I use reflect for invoking methods of Dao (Not type safe)
    private Map<String, String> callMethodToCreateValueMap(String methodName, Short localeId) throws SystemException {
        valueMap = new HashMap<String, String>();
        Method[] methods = LegacyAuditLookupValuesDao.class.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                try {
                    LegacyAuditLookupValuesDao legacyAuditLookupValuesDao = ApplicationContextProvider.getBean(LegacyAuditLookupValuesDao.class);
                    valueMap = (Map<String, String>) method.invoke(legacyAuditLookupValuesDao, new Object[] { localeId });
                } catch (Exception e) {
                    throw new SystemException(e);
                }
            }
        }
        return valueMap;
    }

    private Map<String, String> createValueMap(EntityName entityName, Short localeId) throws SystemException {
        valueMap = new HashMap<String, String>();
        if (entityName.getClassPath() == null) {
            fetchMasterData(entityName.getName(), localeId);
        } else {
            fetchMasterData(entityName.getName(), localeId, entityName.getClassPath().getPath());
        }
        return valueMap;
    }

    private void fetchMasterData(String entityName, Short localeId) throws SystemException {
        List<ValueListElement> businessActivityList = legacyMasterDao
                .findValueListElements(entityName);
        for (ValueListElement businessActivityEntity : businessActivityList) {
            valueMap.put(businessActivityEntity.getId().toString(), businessActivityEntity.getName());
        }
    }

    private void fetchMasterData(String entityName, Short localeId, String classPath) throws SystemException {
        Class clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(classPath);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
        List<MasterDataEntity> masterDataList = legacyMasterDao.findMasterDataEntities(clazz);
        for (MasterDataEntity masterDataEntity : masterDataList) {

            if (masterDataEntity instanceof PersonnelStatusEntity) {
                String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(masterDataEntity.getLookUpValue());
                ((PersonnelStatusEntity) masterDataEntity).setName(name);
            }

            if (masterDataEntity instanceof PersonnelLevelEntity) {
                String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(masterDataEntity.getLookUpValue());
                ((PersonnelLevelEntity) masterDataEntity).setName(name);
            }

            if (masterDataEntity instanceof OfficeLevelEntity) {
                String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(masterDataEntity.getLookUpValue());
                ((OfficeLevelEntity) masterDataEntity).setName(name);
            }

            if (masterDataEntity instanceof OfficeStatusEntity) {
                String name = ApplicationContextProvider.getBean(MessageLookup.class).lookup(masterDataEntity.getLookUpValue());
                ((OfficeStatusEntity) masterDataEntity).setName(name);
            }

            valueMap.put(masterDataEntity.getId().toString(), masterDataEntity.getName());
        }
    }

    private String getColumnName(String DisplayKey) {
        String keys[] = getKeys(DisplayKey);

        String columnName = "";
        for (String key : keys) {
            if (key.contains(".")) {
                columnName = columnName + " " + columnNames.getString(key);
            } else {
                    columnName = columnName + " " + ApplicationContextProvider.getBean(MessageLookup.class).lookupLabel(key);
            }
        }
        return columnName;
    }

    private String[] getKeys(String DisplayKey) {
        String keys[] = null;
        if (DisplayKey.contains(",")) {
            keys = DisplayKey.split(",");
        } else {
            keys = new String[] { DisplayKey };
        }
        return keys;
    }

}
