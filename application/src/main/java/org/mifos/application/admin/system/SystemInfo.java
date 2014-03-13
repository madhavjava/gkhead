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

package org.mifos.application.admin.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.servicefacade.ApplicationContextProvider;
import org.mifos.core.MifosResourceUtil;
import org.mifos.core.MifosRuntimeException;
import org.mifos.framework.persistence.DatabaseMigrator;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.StandardTestingService;

/**
 * JDBC URL parsing code in this class is <a
 * href="https://groups.google.com/group/comp.lang.java.programmer/browse_thread/thread/b7090860d6834bae">only known to
 * work with MySQL JDBC URLs</a>. Once Mifos supports other database backends, here are some ideas:
 * <ul>
 * <li>fallback to simply printing out the full JDBC URL rather than trying to parse port, host, etc.</li>
 * <li>implement or reuse parsers for JDBC URLs specific to other databases</li>
 * </ul>
 */
public class SystemInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private final DatabaseMetaData databaseMetaData;
    private Locale locale;

    private String applicationServerInfo;
    private String javaVendor;
    private String javaVersion;
    private VersionInfo versionInfo;
    private String osName;
    private String osArch;
    private String osVersion;
    private String osUser;
    private String customReportsDir;

    private String infoSource;
    private URI infoURL;
    private String databaseUser;

    // Note: make sure to close the connection that got the metadata!
    public SystemInfo(DatabaseMetaData databaseMetaData, ServletContext context, Locale locale, boolean getInfoSource) {

        if (getInfoSource) {
            try {
                this.infoSource = Arrays.toString(new StandardTestingService().getAllSettingsFilenames());
            } catch (IOException e) {
                this.infoSource = ApplicationContextProvider.getBean(MessageLookup.class).lookup("admin.unableToDetermineConfigurationSource");
            }
        }

        this.databaseMetaData = databaseMetaData;
        this.locale = locale;

        try {
            URI mysqlOnly = new URI(databaseMetaData.getURL());
            this.infoURL = new URI(mysqlOnly.getSchemeSpecificPart());

            setDatabaseUser(databaseMetaData.getUserName());
            setApplicationServerInfo(context.getServerInfo());
            setJavaVendor(System.getProperty("java.vendor"));
            setJavaVersion(System.getProperty("java.version"));
            setBuildInformation(new VersionInfo());
            setOsName(System.getProperty("os.name"));
            setOsArch(System.getProperty("os.arch"));
            setOsVersion(System.getProperty("os.version"));
            setOsUser(System.getProperty("user.name"));
        } catch (URISyntaxException e) {
            throw new MifosRuntimeException(e);
        } catch (SQLException e) {
            throw new MifosRuntimeException(e);
        }
    }

    public String getApplicationVersion() {
        return getApplicationVersion(new DatabaseMigrator().getAppliedUpgrades(), getReleaseUpgrades(),
                getReleaseSchemaName());
    }

    public String getApplicationVersion(List<Integer> appliedUpgrades, List<Integer> releaseUpgrades,
            String releaseSchemaName) {
        String version = "Developer Schema";

        if (appliedUpgrades.equals(releaseUpgrades)) {
            version = releaseSchemaName;
        }

        return version;
    }

    private String getReleaseSchemaName() {
        Reader reader;
        BufferedReader bufferedReader;
        try {
            reader = MifosResourceUtil.getSQLFileAsReader("release-upgrades.txt");
            bufferedReader = new BufferedReader(reader);

            while (true) {
                String line = bufferedReader.readLine();
                if (line.startsWith("release")) {
                    line = line.substring(line.indexOf(':') + 1);
                    return line;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public String getDatabaseVendor() {
        try {
            return databaseMetaData.getDatabaseProductName();
        } catch (SQLException e) {
            return "unknown";
        }
    }

    public String getDatabaseVersion() {
        try {
            return databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            return "unknown";
        }
    }

    public String getDriverName() {
        try {
            return databaseMetaData.getDriverName();
        } catch (SQLException e) {
            return "unknown";
        }
    }

    public String getDriverVersion() {
        try {
            return databaseMetaData.getDriverVersion();
        } catch (SQLException e) {
            return "unknown";
        }
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getApplicationServerInfo() {
        return applicationServerInfo;
    }

    public void setApplicationServerInfo(String applicationServerInfo) {
        this.applicationServerInfo = applicationServerInfo;
    }

    public String getCommitIdentifier() {
        return versionInfo.getCommitIdentifier();
    }

    public String getBuildNumber() {
        return versionInfo.getBuildNumber();
    }

    public String getBuildDate() {
        return versionInfo.getBuildDate();
    }

    public String getReleaseName() {
        return versionInfo.getReleaseName();
    }

    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public void setBuildInformation(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void setCustomReportsDir(String dir) {
        customReportsDir = dir;
    }

    public String getCustomReportsDir() {
        return customReportsDir;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getInfoSource() {
        return infoSource;
    }

    public void setInfoSource(String infoSource) {
        this.infoSource = infoSource;
    }

    public String getDatabaseServer() {
        return infoURL.getHost();
    }

    public String getDatabasePort() {
        if (infoURL.getPort() < 0) {
            return "unknown";
        }
        return "" + infoURL.getPort();
    }

    public String getDatabaseName() {
        String path = infoURL.getPath();
        if (path != null) {
            path = path.replaceFirst("/", "");
        }
        return path;
    }

    public String getDatabaseUser() {
        return this.databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getInfoURL() {
        return infoURL.toString();
    }

    public void setInfoURL(URI infoURL) {
        this.infoURL = infoURL;
    }

    public DateTime getDateTime() {
        return new DateTimeService().getCurrentDateTime();
    }

    public String getDateTimeString() {
        DateTimeFormatter formatter = DateTimeFormat.shortDateTime().withOffsetParsed().withLocale(locale);
        return formatter.print(getDateTime().getMillis());
    }

    public String getDateTimeStringIso8601() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        return formatter.print(getDateTime().getMillis());
    }

    public String getOsUser() {
        return osUser;
    }

    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }



    /**
     * @deprecated this method employs the NSDU upgrade mechanism to figure out the release upgrades.
     * Since NSDU is currently de-commissioned, use the getReleaseUpgrades()
     * @return the list of release upgrade IDs applied in the current database
     */
    @Deprecated
	private List<Integer> getReleaseUpgrades() {
        Reader reader = null;
        BufferedReader bufferedReader = null;
        Integer upgradeId;
        List<Integer> releaseUpgrades = new ArrayList<Integer>();
        try {
            reader = MifosResourceUtil.getSQLFileAsReader("release-upgrades.txt");
            bufferedReader = new BufferedReader(reader);

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (!line.startsWith("release")) {
                    upgradeId = Integer.parseInt(line);
                    releaseUpgrades.add(upgradeId);
                }


            }

        } catch (IOException e) {
            // log.error("An error occurred whilst reading the upgrades.txt file");

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return releaseUpgrades;
    }

}
