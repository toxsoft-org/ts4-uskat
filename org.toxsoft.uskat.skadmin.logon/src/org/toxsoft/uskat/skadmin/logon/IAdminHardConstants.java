package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.s5.utils.collections.S5CollectionUtils.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Константы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardConstants {

  /**
   * ИД-путь команд которые находятся в плагине
   */
  String CMD_PATH = ""; //$NON-NLS-1$

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = CMD_PATH + "";

  // ------------------------------------------------------------------------------------
  // Общие
  //
  /**
   * Аргумент: Требование строгого форматирования. Строки обрезаются если они не умещаются в столбцы выводимых таблиц
   */
  IAdminCmdArgDef ARG_FORMAT = new AdminCmdArgDef( "format", createType( BOOLEAN, "true" ), STR_ARG_FORMAT );

  /**
   * Аргумент: Требование отвечать на вопросы команды ответом 'y' (продолжать)
   */
  IAdminCmdArgDef ARG_YES = new AdminCmdArgDef( "yes", "y", createType( BOOLEAN, "false" ), STR_ARG_YES );

  // ------------------------------------------------------------------------------------
  // AdminCmdConnect
  //
  String CMD_CONNECT_ID    = CMD_PATH_PREFIX + "connect";
  String CMD_CONNECT_ALIAS = EMPTY_STRING;
  String CMD_CONNECT_NAME  = EMPTY_STRING;
  String CMD_CONNECT_DESCR = STR_CMD_CONNECT;

  /**
   * Аргумент {@link AdminCmdConnect}: Имя пользователя
   */
  IAdminCmdArgDef ARG_CONNECT_USER = new AdminCmdArgDef( "user", createType( STRING, "root" ), STR_ARG_CONNECT_USER );

  /**
   * Аргумент {@link AdminCmdConnect}: Пароль пользователя
   */
  IAdminCmdArgDef ARG_CONNECT_PASSWORD =
      new AdminCmdArgDef( "password", createType( STRING, "1" ), STR_ARG_CONNECT_PASSWORD );

  /**
   * Аргумент {@link AdminCmdConnect}: Список сетевых имен или IP-адресов узлов кластера сервера
   */
  IAdminCmdArgDef ARG_CONNECT_HOST = new AdminCmdArgDef( "host", PT_LIST_STRING, STR_ARG_CONNECT_HOST );

  /**
   * Аргумент {@link AdminCmdConnect}: Список портов узлов (в том же порядке что и host) кластера сервера
   */
  IAdminCmdArgDef ARG_CONNECT_PORT = new AdminCmdArgDef( "port", PT_LIST_INTEGER, STR_ARG_CONNECT_PORT );

  /**
   * Аргумент {@link AdminCmdConnect}: Таймаут(мсек) ожидания соединения с сервером по истечении которого принимается
   * решение, что соединение невозможно
   */
  IAdminCmdArgDef ARG_CONNECT_CONNECT_TIMEOUT =
      new AdminCmdArgDef( "connectTimeout", createType( INTEGER, "5000" ), STR_ARG_CONNECT_CONNECT_TIMEOUT );

  /**
   * Аргумент {@link AdminCmdConnect}: Таймаут(мсек) ожидания ответов сервера по истечении которого принимается решение,
   * что произошел обрыв связи с сервером
   */
  IAdminCmdArgDef ARG_CONNECT_FAILURE_TIMEOUT =
      new AdminCmdArgDef( "failureTimeout", createType( INTEGER, "3000" ), STR_ARG_CONNECT_FAILURE_TIMEOUT );

  /**
   * Аргумент {@link AdminCmdConnect}: Таймаут (мсек) передачи текущих данных от сервера клиенту. Значение <=0 -
   * отправлять немедленно
   */
  IAdminCmdArgDef ARG_CONNECT_CURRDATA_TIMEOUT =
      new AdminCmdArgDef( "currdataTimeout", createType( INTEGER, "300" ), STR_ARG_CONNECT_CURRDATA_TIMEOUT );

  /**
   * Аргумент {@link AdminCmdConnect}: Таймаут (мсек) передачи текущих данных от сервера клиенту. Значение <=0 -
   * отправлять немедленно
   */
  IAdminCmdArgDef ARG_CONNECT_HISTDATA_TIMEOUT =
      new AdminCmdArgDef( "histdataTimeout", createType( INTEGER, "60000" ), STR_ARG_CONNECT_HISTDATA_TIMEOUT );

  /**
   * Аргумент {@link AdminCmdConnect}: Имя класса инициализатора клиентского API
   */
  IAdminCmdArgDef ARG_CONNECT_INITIALIZER =
      new AdminCmdArgDef( "initializer", DT_STRING_NULLABLE, STR_ARG_CONNECT_INITIALIZER );

  /**
   * Аргумент {@link AdminCmdConnect}: Подключение расширения API: 'Реальное время'
   */
  IAdminCmdArgDef ARG_CONNECT_REALTIME =
      new AdminCmdArgDef( "realtime", createType( BOOLEAN, "true" ), STR_ARG_CONNECT_REALTIME );

  /**
   * Аргумент {@link AdminCmdConnect}: Подключение расширения API: 'Пакетные операции'
   */
  IAdminCmdArgDef ARG_CONNECT_BATCH =
      new AdminCmdArgDef( "batch", createType( BOOLEAN, "true" ), STR_ARG_CONNECT_BATCH );

  // ------------------------------------------------------------------------------------
  // AdminCmdDisconnect
  //
  String CMD_DISCONNECT_ID    = CMD_PATH_PREFIX + "disconnect";
  String CMD_DISCONNECT_ALIAS = EMPTY_STRING;
  String CMD_DISCONNECT_NAME  = EMPTY_STRING;
  String CMD_DISCONNECT_DESCR = STR_CMD_DISCONNECT;

  /**
   * Аргумент {@link AdminCmdDisconnect}: Идентификатор сессии или его часть(поиск совпадения)
   */
  IAdminCmdArgDef ARG_DISCONNECT_SESSION =
      new AdminCmdArgDef( "session", PT_SINGLE_STRING, STR_ARG_DISCONNECT_SESSION );

  // ------------------------------------------------------------------------------------
  // AdminCmdGetConnection
  //
  String CMD_GET_CONNECTION_ID    = CMD_PATH_PREFIX + "getConnection";
  String CMD_GET_CONNECTION_ALIAS = EMPTY_STRING;
  String CMD_GET_CONNECTION_NAME  = EMPTY_STRING;
  String CMD_GET_CONNECTION_DESCR = STR_CMD_GET_CONNECTION;

  /**
   * Аргумент {@link AdminCmdGetConnection}: Индекс в списке открытых соединений
   */
  IAdminCmdArgDef ARG_GET_CONNECTION_INDEX =
      new AdminCmdArgDef( "index", DT_INTEGER_NULLABLE, STR_ARG_GET_CONNECTION_INDEX );

  // ------------------------------------------------------------------------------------
  // AdminCmdInfo
  //
  String CMD_INFO_ID    = CMD_PATH_PREFIX + "info";
  String CMD_INFO_ALIAS = EMPTY_STRING;
  String CMD_INFO_NAME  = EMPTY_STRING;
  String CMD_INFO_DESCR = STR_CMD_INFO;

  /**
   * Аргумент {@link AdminCmdInfo}: Интервал статистики
   */
  IAdminCmdArgDef ARG_INFO_INTERVAL = new AdminCmdArgDef( "interval", createType( EAtomicType.STRING, avStr( "min" ) ),
      STR_ARG_INFO_INTERVAL + itemsToString( EStatisticInterval.asStridablesList() ) );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации о платформе
   */
  IAdminCmdArgDef ARG_INFO_PLATFORM =
      new AdminCmdArgDef( "platform", "p", createType( BOOLEAN, avBool( false ) ), STR_ARG_INFO_PLATFORM );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода указанных параметров из {@link ISkBackend#getBackendInfo()}. * -
   * все параметры.
   */
  IAdminCmdArgDef ARG_BACKEND_INFO_PARAMS =
      new AdminCmdArgDef( "backendInfoParams", "", createType( STRING, avStr( "" ) ), STR_ARG_BACKEND_INFO_PARAMS );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации о транзакциях
   */
  IAdminCmdArgDef ARG_INFO_TRANSACTIONS =
      new AdminCmdArgDef( "transactions", "t", createType( BOOLEAN, avBool( false ) ), STR_ARG_INFO_TRANSACTIONS );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации об открытых сессиях
   */
  IAdminCmdArgDef ARG_INFO_OPEN_SESSIONS =
      new AdminCmdArgDef( "openSessions", "o", createType( BOOLEAN, avBool( true ) ), STR_ARG_INFO_OPEN_SESSIONS );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации о закрытых сессиях
   */
  IAdminCmdArgDef ARG_INFO_CLOSE_SESSIONS =
      new AdminCmdArgDef( "closeSessions", "c", createType( BOOLEAN, avBool( false ) ), STR_ARG_INFO_CLOSE_SESSIONS );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации о топологии кластеров доступных клиенту
   */
  IAdminCmdArgDef ARG_INFO_TOPOLOGY =
      new AdminCmdArgDef( "topology", createType( BOOLEAN, avBool( false ) ), STR_ARG_INFO_TOPOLOGY );

  /**
   * Аргумент {@link AdminCmdInfo}: Требование вывода информации о приеме/передаче хранимых данных клиентам
   */
  IAdminCmdArgDef ARG_INFO_HISTDATA =
      new AdminCmdArgDef( "histdata", createType( BOOLEAN, avBool( false ) ), STR_ARG_INFO_HISTDATA );

  /**
   * Аргумент {@link AdminCmdInfo}: Имя текстового файла (в каталоге data), в формате \"имя_клиента, ip-адрес\" для
   * контроля связи с клиентом
   */
  IAdminCmdArgDef ARG_INFO_CHECKFILE =
      new AdminCmdArgDef( "checkfile", createType( STRING, avStr( "clients.txt" ) ), STR_ARG_INFO_CHECKFILE );

}
