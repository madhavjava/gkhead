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

package org.mifos.security;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.mifos.application.master.business.LookUpEntity;
import org.mifos.config.Localization;
import org.mifos.framework.persistence.Upgrade;
import org.mifos.security.rolesandpermission.util.helpers.RolesAndPermissionConstants;

public class AddActivity extends Upgrade {

    private final short newActivityId;
    private final String activityName;
    private final String activityNameKey;
    private final Short parentActivity;
    protected static final String wrongLookupValueKeyFormat = "The key format must be Permissions-...";
    protected static final String keyFormat = "Permissions-";

    /**
     * Define an activity and one name for it. If you want to give it names in
     * multiple locales, this upgrade can not yet do that.
     *
     * @param newActivityId
     *            ID for the activity we are creating
     * @param parentActivity
     *            existing ID for the parent
     * @param locale
     *            Locale in which we want to define a name
     * @param activityName
     *            Name to give the activity, in that locale.
     */
    /*
     * This constructor is used for version 174 and lower. And it must not be
     * used afterward
     */
    public AddActivity(short newActivityId, Short parentActivity, String activityName) {

        this.newActivityId = newActivityId;
        this.parentActivity = parentActivity;
        this.activityName = activityName;
        this.activityNameKey = " ";
    }

    /**
     * Define an activity and the key to lookup its name.
     */
    /*
     * This constructor must be used after version 174. The activityNameKey must
     * in the format Permissions-...
     */
    public AddActivity(String activityNameKey, short newActivityId, Short parentActivity) {
        if (!validateLookupValueKey(keyFormat, activityNameKey)) {
            throw new RuntimeException(wrongLookupValueKeyFormat);
        }
        this.newActivityId = newActivityId;
        this.parentActivity = parentActivity;
        this.activityNameKey = activityNameKey;
        this.activityName = null;
    }

    public AddActivity(String activityNameKey, short newActivityId, Short parentActivity, String activityName) {
        super();
        if (!validateLookupValueKey(keyFormat, activityNameKey)) {
            throw new RuntimeException(wrongLookupValueKeyFormat);
        }
        this.newActivityId = newActivityId;
        this.parentActivity = parentActivity;
        this.activityNameKey = activityNameKey;
        this.activityName = activityName;
    }

    @Override
    public void upgrade(Connection connection)
            throws IOException, SQLException {
        int lookupEntity = LookUpEntity.ACTIVITY;

        int lookupId = insertLookupValue(connection, lookupEntity, activityNameKey);
        insertMessage(connection, lookupId, Localization.ENGLISH_LOCALE_ID, activityName);
        try {
            addActivityEntity(connection, lookupId);
        } catch (SQLException e) {
            deleteFromLookupValueLocale(connection, (short) lookupId);
            deleteFromLookupValue(connection, (short) lookupId);
            throw e;
        }
        try {
            allowActivity(connection, newActivityId, RolesAndPermissionConstants.ADMIN_ROLE);
        } catch (SQLException e) {
            deleteFromActivity(connection);
            deleteFromLookupValueLocale(connection, (short) lookupId);
            deleteFromLookupValue(connection, (short) lookupId);
            throw e;
        }
    }

    private void allowActivity(Connection connection, short activityId, int roleId) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("insert into roles_activity(activity_id, role_id) values(?, ?)");
        statement.setShort(1, activityId);
        statement.setInt(2, roleId);
        statement.executeUpdate();
        statement.close();
    }

    private void addActivityEntity(Connection connection, int lookupId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("insert into activity(" + "activity_id,parent_id,"
                + "activity_name_lookup_id,DESCRIPTION_lookup_id) " + "values(?,?,?,?)");
        statement.setShort(1, newActivityId);
        statement.setObject(2, parentActivity, Types.SMALLINT);
        statement.setInt(3, lookupId);
        statement.setInt(4, lookupId);
        statement.executeUpdate();
        statement.close();
    }

    private static short findLookupId(Connection connection, short activityId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select activity_name_lookup_id "
                + "from activity where activity_id = ?");
        statement.setShort(1, activityId);
        ResultSet results = statement.executeQuery();
        if (results.next()) {
            short lookupId = results.getShort("activity_name_lookup_id");
            statement.close();
            return lookupId;
        }
        statement.close();
        throw new RuntimeException("unable to downgrade: no activity with id " + activityId);
    }

    private void deleteFromActivity(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("delete from activity where activity_id = ?");
        statement.setInt(1, newActivityId);
        statement.executeUpdate();
        statement.close();
    }

    public static void changeActivityMessage(Connection connection, short activity, short locale, String newMessage)
            throws SQLException {
        int lookupId = findLookupId(connection, activity);
        updateMessage(connection, lookupId, locale, newMessage);
    }

    public static void reparentActivity(Connection connection, short activityId, Short newParent) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("update activity set parent_id = ? where activity_id = ?");
        statement.setObject(1, newParent, Types.SMALLINT);
        statement.setShort(2, activityId);
        statement.executeUpdate();
        statement.close();
    }

}
