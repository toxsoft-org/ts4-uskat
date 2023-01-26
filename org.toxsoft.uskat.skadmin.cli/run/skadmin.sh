# Настройка окружения запуска
ADMIN_CLASSPATH=\
../../../ts4-targets/ts4-target-extlibs/lib/*:\
../../../ts4-targets/ts4-target-core/lib/*:\
../../../ts4-targets/ts4-target-uskat/lib/*:\
../../../ts4-targets/ts4-target-sitrol/lib/*:\
../dist/*

ADMIN_PLUGINPATH=\
../../../ts4-targets/ts4-target-uskat/main:\
../../../ts4-targets/ts4-target-sitrol/lib:\
../../../ts4-targets/ts4-target-sitrol/main

ADMIN_USER=root
ADMIN_PASSWORD=1
ADMIN_HOST="127.0.0.1"
ADMIN_PORT="8080"
ADMIN_CONNECT_TIMEOUT=3000
ADMIN_FAILURE_TIMEOUT=500000
ADMIN_CURRDATA_TIMEOUT=-1
ADMIN_INITIALIZER=""


# Параметры jvm
_CLASS_PATH="-cp $ADMIN_CLASSPATH"
_PLUGIN_PATHS="-Dorg.toxsoft.uskat.skadmin.plugin.paths=$ADMIN_PLUGINPATH"
_MAIN_CLASS="org.toxsoft.uskat.skadmin.cli.Main"
_XMS_MEMORY="-Xms256m"
_XMX_MEMORY="-Xmx512m"
_CHARSET="-Dfile.encoding=UTF8"
_LOGGER="-Dlog4j.configuration=file:log4j.xml"
_REMOTE_DEBUG="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"


java $_PLUGIN_PATHS $_CHARSET $_LOGGER $_XMS_MEMORY $_XMX_MEMORY $_CLASS_PATH $_MAIN_CLASS connect -user $ADMIN_USER -password $ADMIN_PASSWORD -host $ADMIN_HOST -port $ADMIN_PORT -connectTimeout



