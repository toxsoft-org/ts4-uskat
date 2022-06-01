package org.toxsoft.uskat.skadmin.dev.pas;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardResources {

  // ------------------------------------------------------------------------------------
  // AdminCmdPasRequest,AdminCmdPasNotify
  //
  String STR_ARG_METHOD = "Имя метода";
  String STR_ARG_PARAMS = "Параметры метода";

  // ------------------------------------------------------------------------------------
  // AdminCmdPasConnect
  //
  String STR_CMD_PAS_CONNECT = "Подключение к PAS-серверу (Public Access Server).";

  String STR_ARG_HOST            = "Имя хоста или IP адрес PAS-сервера. По умолчанию: localhost";
  String STR_ARG_PORT            = "Порт PAS-сервера. По умолчанию: 2194";
  String STR_ARG_CREATE_TIMEOUT  = "Таймаут (мсек) подключения к PAS. По умолчанию: 10000";
  String STR_ARG_FAILURE_TIMEOUT = "Таймаут (мсек) обрыва связи с PAS. По умолчанию: 3000";
  String STR_ARG_WRITE_TIMEOUT   = "Таймаут (мсек) записи в PAS. По умолчанию: 100";

  // ------------------------------------------------------------------------------------
  // AdminCmdPasRequest
  //
  String STR_CMD_PAS_REQUEST       = "Выполнение запросов к PAS-серверу (Public Access Server).";
  String STR_ARG_REQUEST_FILENAME  = "Имя файла в котором сохраняется результат. По умолчанию: не сохранять в файле";
  String STR_ARG_REQUEST_TIMEOUT   = "Таймаут (мсек) выполнения метода. По умолчанию: 10000";
  String STR_ARG_REQUEST_IO_LOGGER = "Требование выводить на экран принятые данные. По умолчанию: true";

  // ------------------------------------------------------------------------------------
  // AdminCmdPasNotify
  //
  String STR_CMD_PAS_NOTIFY = "Передача уведомления PAS-серверу (Public Access Server).";

  // ------------------------------------------------------------------------------------
  // AdminCmdPasClose
  //
  String STR_CMD_PAS_CLOSE = "Завершение работы с PAS-сервером (Public Access Server).";

  // ------------------------------------------------------------------------------------
  // Сообщения
  //

  String MSG_INFO_LINE       =
      "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_COMMA           = ",";
  String MSG_YES             = "Да";
  String MSG_NO              = "Нет";
  String MSG_CMD_PAS_CONNECT = "Выполнено подключение к PAS %s";
  String MSG_CMD_TIME        = "Время выполнения команды: %d (мсек).";
  String MSG_RECEVIED_RESULT = "Channel %s received '%s' result:\n%s";

  // ------------------------------------------------------------------------------------
  // Ошибки
  //
  String ERR_NOT_CONNECTION_BY_TIMEOUT = "Ошибка подключения к PAS %s по таймауту (%d)";
  String ERR_NOT_CONNECTION            = "Нет связи с PAS %s";
  String ERR_RECEVIED_ERROR            = "Channel %s received '%s' error:\n%s";
}
