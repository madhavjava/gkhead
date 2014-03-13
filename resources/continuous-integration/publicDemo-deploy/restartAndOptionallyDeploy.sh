#!/bin/bash
set -ex

# JOB_NAME environment variable must be set. We count on Hudson for this.

controlScript=$WORKSPACE/resources/continuous-integration/deploy/tomcat/control.sh
lastStableWAR=$WORKSPACE/application/target/mifos-webapp.war
lastStableWAR=$WORKSPACE/../../$WAR_JOB/workspace/application/target/mifos-webapp.war
deployRoot=$HOME/deploys/mifos-$JOB_NAME-deploy
targetWARlocation=$deployRoot/tomcat6/webapps/mifos.war

if [ "$FETCH_NEW_WAR" == "true" ]
then
    $controlScript stop
    rm -f $deployRoot/tomcat6/logs/*
    rm -rf $deployRoot/tomcat6/webapps/mifos
    rm -rf $deployRoot/tomcat6/work
    cp $lastStableWAR $targetWARlocation
    $controlScript start
else
    $controlScript restart
fi

# unlock mifos user account and reset password; this account sometimes gets
# locked out. db connection settings here are duplicated in
# mifos_conf/local.properties
cat $WORKSPACE/db/src/main/resources/sql/init_mifos_password.sql | \
    mysql -u hudson -phudson hudson_mifos_public_demo
echo 'update personnel set locked=0, no_of_tries=0 where personnel_id=1' | \
    mysql -u hudson -phudson hudson_mifos_public_demo

can_hit_test_server=1
while [ $can_hit_test_server -ne 0 ]
do
    set +e # or a failure would stop the script prematurely
    curl --fail http://demo.mifos.org/mifos/
    can_hit_test_server=$?
    sleep 1
done
