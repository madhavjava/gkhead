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

package org.mifos.application.master.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.persistence.SqlUpgrade;
import org.mifos.framework.persistence.Upgrade;

public abstract class LanguageUpgrade extends Upgrade {

    private static final int LANGUAGE_ENTITY_NUMBER = 74;
    private int upgradeTimeStamp;


    public LanguageUpgrade(int upgradeTimestamp) {
        super();
        this.upgradeTimeStamp = upgradeTimestamp;
    }

    /**
     * This method adds language name, code pairs to the map to be added to the
     * database
     *
     * @param languageNameAndCodesToAdd
     *            - add data to this map
     */
    public abstract void addData(List<String[]> languageNameAndCodesToAdd);

    @Override
    public void upgrade(Connection connection)
            throws IOException, SQLException {
        addCountryCodes(connection, upgradeTimeStamp);
        addLanguageDescriptionLookupValues(connection);
        addLocales(connection, upgradeTimeStamp);
    }

    private void addLanguageDescriptionLookupValues(Connection connection) throws SQLException {
        List<String[]> languageData = getLanguageData();
        for (String[] languageEntry : languageData) {
            String languageName = languageEntry[0];
            String languageCode = languageEntry[1];
            insertLanguageAndLookupValue(connection, getLargestLanguageId(connection) + 1, languageName, languageCode);
        }
    }

    private List<String[]> getLanguageData() {
        List<String[]> languageNameAndCodesToAdd = new ArrayList<String[]>();
        addData(languageNameAndCodesToAdd);
        return languageNameAndCodesToAdd;
    }

    private void addCountryCodes(Connection connection,
            int upgradeTimestamp) throws IOException, SQLException {
        upgradePart(connection, "upgrade_to_" + upgradeTimestamp + "_part_1.sql");
    }

    private void addLocales(Connection connection,
            int upgradeTimestamp) throws IOException, SQLException {
        upgradePart(connection, "upgrade_to_" + upgradeTimestamp + "_part_3.sql");
    }

    private void upgradePart(Connection connection,
            String sqlUpgradeScriptFilename) throws IOException, SQLException {
        SqlUpgrade upgradePart = new SqlUpgrade(sqlUpgradeScriptFilename);
        upgradePart.runScript(connection);
    }

    private void insertLanguageAndLookupValue(Connection connection, int languageId, String languageName,
            String languageCode) throws SQLException {
        int lookupId = insertLookupValue(connection, LANGUAGE_ENTITY_NUMBER, "Language-" + languageName);
        String insertLanguageSql = "insert into language(lang_id,lang_name,lang_short_name,lookup_id) values("
                + languageId + ", '" + languageName + "','" + languageCode + "'," + lookupId + ")";
        execute(connection, insertLanguageSql);
    }

    private int getLargestLanguageId(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("select max(lang_id) from language");
        if (!results.next()) {
            throw new SystemException(SystemException.DEFAULT_KEY, "Did not find an existing lang_id in language table");
        }
        int largestLangId = results.getInt(1);
        results.close();
        statement.close();
        return largestLangId;
    }

}
