#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Настройка окружения запуска
ADMIN_CLASSPATH=\
../../../ts4-targets/ts4-target-extlibs/lib/*:\
../../../ts4-targets/ts4-target-core/lib/*:\
../../../ts4-targets/ts4-target-uskat/lib/*:\
../../../ts4-targets/ts4-target-sitrol/lib/*:\
../dist/*

ADMIN_PLUGINPATH=\
../../../ts4-targets/ts4-target-uskat/main/plugins

ADMIN_USER=root
ADMIN_PASSWORD=root
ADMIN_HOST="127.0.0.1"
ADMIN_PORT="8080"
ADMIN_CONNECT_TIMEOUT=3000
ADMIN_FAILURE_TIMEOUT=10000
ADMIN_CURRDATA_TIMEOUT=300
ADMIN_DOJOB_TIMEOUT=10
ADMIN_MEMORY=512M
ADMIN_CHARSET="UTF8"

# раскоментировать если нужна удаленная отладка
_REMOTE_DEBUG="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"


${JAVA_HOME}/bin/java \
  -Xms$ADMIN_MEMORY \
  -Xmx$ADMIN_MEMORY \
  -cp $ADMIN_CLASSPATH \
  -Dorg.toxsoft.uskat.s5.client.doJobTimeout=$ADMIN_DOJOB_TIMEOUT \
  -Dorg.toxsoft.uskat.skadmin.plugin.paths=$ADMIN_PLUGINPATH \
  -Dfile.encoding=$ADMIN_CHARSET \
  -Dlog4j.configuration=file:log4j.xml \
  $_REMOTE_DEBUG \
  org.toxsoft.uskat.skadmin.cli.Main \
  connect \
  -user     $ADMIN_USER     \
  -password $ADMIN_PASSWORD \
  -host     $ADMIN_HOST     \
  -port     $ADMIN_PORT     \
  -connectTimeout  $ADMIN_CONNECT_TIMEOUT  \
  -failureTimeout  $ADMIN_FAILURE_TIMEOUT  \
  -currdataTimeout $ADMIN_CURRDATA_TIMEOUT


