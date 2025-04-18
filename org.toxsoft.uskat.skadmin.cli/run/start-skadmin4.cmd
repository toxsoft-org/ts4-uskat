@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21

:: Настройка окружения запуска
set ADMIN_CLASSPATH=^
../../../ts4-targets/ts4-target-extlibs/lib/*;^
../../../ts4-targets/ts4-target-core/lib/*;^
../../../ts4-targets/ts4-target-uskat/lib/*;^
../../../ts4-targets/ts4-target-skf-dq/lib/*;^
../../../ts4-targets/ts4-target-skf-bridge/lib/*;^
../../../ts4-targets/ts4-target-sitrol/lib/*

set ADMIN_PLUGINPATH=^
../../../ts4-targets/ts4-target-uskat/main/plugins:^
../../../ts4-targets/ts4-target-skf-dq/main/plugins
../../../ts4-targets/ts4-target-skf-bridge/main/plugins

set ADMIN_USER=root
set ADMIN_PASSWORD=root
set ADMIN_HOST=localhost
set ADMIN_PORT=8080
set ADMIN_CONNECT_TIMEOUT=3000
set ADMIN_FAILURE_TIMEOUT=10000
set ADMIN_CURRDATA_TIMEOUT=-1
set ADMIN_DOJOB_TIMEOUT=10
set ADMIN_MEMORY=2048M
set ADMIN_CHARSET=CP866

:: раскоментировать если нужна удаленная отладка
set _REMOTE_DEBUG=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8004

"%JAVA_HOME%"\bin\java 							^
  -Xms%ADMIN_MEMORY% 							^
  -Xmx%ADMIN_MEMORY% 							^
  -cp %ADMIN_CLASSPATH% 						^
  -Dorg.toxsoft.uskat.s5.client.doJobTimeout=%ADMIN_DOJOB_TIMEOUT% 	^
  -Dorg.toxsoft.uskat.skadmin.plugin.paths=%ADMIN_PLUGINPATH% 		^
  -Dfile.encoding=%ADMIN_CHARSET% 					^
  -Dlog4j.configuration=file:log4j.xml 					^
  %_REMOTE_DEBUG% 							^
  org.toxsoft.uskat.skadmin.cli.Main 					^
  connect 								^
  -user     %ADMIN_USER%     						^
  -password %ADMIN_PASSWORD% 						^
  -host     %ADMIN_HOST%     						^
  -port     %ADMIN_PORT%     						^
  -connectTimeout  %ADMIN_CONNECT_TIMEOUT%  				^
  -failureTimeout  %ADMIN_FAILURE_TIMEOUT%  				^
  -currdataTimeout %ADMIN_CURRDATA_TIMEOUT%


