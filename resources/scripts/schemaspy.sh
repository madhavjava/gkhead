#!/bin/bash
set -o errexit

DATABASE_VERSION=`echo 'select UNIX_TIMESTAMP(DATEEXECUTED) from DATABASECHANGELOG where ORDEREXECUTED = (select MIN(ORDEREXECUTED) from DATABASECHANGELOG)' | mysql --skip-column-names -u hudson -phudson hudson_mifos_gazelle_trunk`
tempDir=`mktemp -d`
outputDir=/var/www/schema/head/$DATABASE_VERSION

if [ -d $outputDir ]
then
    echo "$outputDir exists--looks like the schema has not changed. Exiting gracefully."
    rm -rf $tempDir
    exit 0
fi

echo "Generating schema documentation for database version $DATABASE_VERSION"

java -jar $HOME/arc/schemaSpy_5.0.0.jar -t mysql -host localhost -u hudson -p hudson -db hudson_mifos_gazelle_trunk -dp $HOME/arc/mysql-connector-java-5.1.12-bin.jar -hq -o $tempDir

# this mv increases the probability that $outputDir will not exist unless schemaSpy succeeded
# FIXME: this won't work unless schemaSpy is modified to return exit codes that make sense... currently it appears to always return "success"
mv $tempDir $outputDir

# without this permission http://ci.mifos.org/schema/head/ will not list new directory
# (When Hudson runs this script, umask might not be 0022)
chmod 755 $outputDir

cd $outputDir/..
rm latest
ln -s $DATABASE_VERSION latest
