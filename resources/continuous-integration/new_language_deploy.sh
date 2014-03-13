#!/bin/bash
set -o errexit

# Create new deployment root for a Mifos Hudson build job. Uses a war built off
# the trunk and deploys into an instance configured for a different language.

# 1. create location in scm (resources/continuous-integration in "head" repo)
# 2. run this as user "hudson" on birch

deployNickname=$1
if [ -z "$deployNickname" ]
then
    echo "ERROR: Must provide a deployment nickname like 'head-master' to proceed"
    echo "Usage: $0 NICKNAME"
    echo "(Example: $0 chinese)"
    exit 1
fi

deployRoot=$HOME/deploys/mifos-head-master-$deployNickname-deploy
if [ -e $deployRoot ]
then
    echo ERROR: $deployRoot already exists.
    exit 1
fi

ciResources=$HOME/hudson-home/jobs/head-master/workspace/resources/continuous-integration

set -x
mkdir $deployRoot
cd $deployRoot
tar -xzf $HOME/arc/apache-tomcat-6.*.tar.gz
ln -s apache-tomcat-* tomcat6
ln -s $ciResources/$deployNickname-deploy/mifos_conf
cd tomcat6/conf
ln -fs $ciResources/$deployNickname-deploy/tomcat/server.xml
ln -fs $ciResources/$deployNickname-deploy/tomcat/context.xml
cd ../lib
ln -s $ciResources/$deployNickname-deploy/tomcat/c3p0.properties
