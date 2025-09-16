package org.toxsoft.uskat.skadmin.logon;

/**
 * Локализуемые ресурсы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  // ------------------------------------------------------------------------------------
  // Общие
  //
  String STR_ARG_FORMAT =
      "Требование строгого форматирования. Строки обрезаются если они не умещаются в столбцы выводимых таблиц";
  String STR_ARG_YES    = "Требование отвечать на вопросы команды ответом 'y' (продолжать)";

  // ------------------------------------------------------------------------------------
  // AdminCmdConnect
  //
  String STR_CMD_CONNECT                  = "Установка соединения с сервером skat(s5)";
  String STR_ARG_CONNECT_USER             = "Имя пользователя";
  String STR_ARG_CONNECT_PASSWORD         = "Пароль пользователя";
  String STR_ARG_CONNECT_HOST             = "Список сетевых имен или IP-адресов узлов кластера сервера";
  String STR_ARG_CONNECT_PORT             = "Список портов узлов (в том же порядке что и host) кластера сервера";
  String STR_ARG_CONNECT_CONNECT_TIMEOUT  =
      "Таймаут(мсек) ожидания соединения с сервером по истечении которого принимается решение, что соединение невозможно";
  String STR_ARG_CONNECT_FAILURE_TIMEOUT  =
      "Таймаут(мсек) ожидания ответов сервера по истечении которого принимается решение, что произошел обрыв связи с сервером";
  String STR_ARG_CONNECT_CURRDATA_TIMEOUT =
      "Таймаут (мсек) передачи текущих данных от  сервера клиенту. Значение <=0 - отправлять немедленно";
  String STR_ARG_CONNECT_HISTDATA_TIMEOUT =
      "Таймаут (мсек) передачи хранимых данных от  сервера клиенту. Значение <=0 - отправлять немедленно";
  String STR_ARG_CONNECT_SERVER_ID        = "Идентификатор сервера";
  String STR_ARG_CONNECT_MODULE           = "Имя модуля сервера";
  String STR_ARG_CONNECT_IFACE            = "Полное имя интерфейса API(точка входа) подключения к сервера";
  String STR_ARG_CONNECT_BEAN             = "Имя (без доменной части) бина API(точка входа) подключения к сервера";
  String STR_ARG_CONNECT_INITIALIZER      = "Имя класса инициализатора клиентского API";
  String STR_ARG_CONNECT_REALTIME         = "Подключение расширения API: 'Реальное время'";
  String STR_ARG_CONNECT_BATCH            = "Подключение расширения API: 'Пакетные операции'";

  String STR_CMD_DISCONNECT         = "Разрыв соединения с клиентом";
  String STR_ARG_DISCONNECT_SESSION = "Идентификатор сессии или его часть(поиск совпадения)";

  String MSG_CONNECT              = "\n Сессия               : %s.";
  String MSG_CONNECT_SERVER_ID    = "\n ID                   : %s";
  String MSG_CONNECT_SERVER_NAME  = "\n Имя                  : %s";
  String MSG_CONNECT_SERVER_DESCR = "\n Описание             : %s";
  String MSG_CONNECT_VERSION      = "Модуль,версия,дата    : %s.\n";
  String MSG_CONNECT_DEPENDS      = "Зависимости:                \n";
  String MSG_CONNECT_BACKEND      = "\n-Backend------------------------------";
  String MSG_CONNECT_MODULE       = " %-30s[%-50s] %s\n";

  String ERR_CONNECT_NO_CONNECTION    = "Ошибка подключения к серверу skat-s5. Причина: '%s'. \n";
  String ERR_CONNECT_WRONG_HOST       = "Неопределен список сетевых имен или ip-адресов сервера (host)";
  String ERR_CONNECT_WRONG_NODE_PORTS =
      "Количество портов узлов кластера должно быть равным количеству узлов (host) или 0";

  // ------------------------------------------------------------------------------------
  // AdminCmdDisconnect
  //
  String ERR_DISCONNECT_SESSION_NOT_FOUND = "Сессия %s не найдена";

  // ------------------------------------------------------------------------------------
  // AdminCmdGetConnection
  //
  String STR_CMD_GET_CONNECTION       = "Возвращает ранее открытое соединение с сервером по указанному индексу";
  String STR_ARG_GET_CONNECTION_INDEX =
      "Индекс в списке открытых соединений. Не указан: вывод списка открытых соединений";

  String MSG_GET_CONNECTION_EXT_SERVICE = "Дополнительная служба сервера";
  String MSG_GET_CONNECTION_CONNECTIONS = "\nОткрытые соединения (%d) :";
  String MSG_GET_CONNECTION_CONNECTION  = "\n  %s [%d] %s";

  String ERR_GET_CONNECTION_NO_OPENS    = "\nНет открытых соединений";
  String ERR_GET_CONNECTION_WRONG_INDEX = "\nНедопустимый индекс соединения: %d. Должен быть 0 до %d включительно";

  // ------------------------------------------------------------------------------------
  // AdminCmdInfo
  //
  String STR_CMD_INFO                = "Возвращает информацию о текущем состоянии сервера";
  String STR_ARG_INFO_INTERVAL       = "Интервал запрашиваемой статитстики. Допустимые значения: ";
  String STR_ARG_INFO_PLATFORM       = "Требование вывода информации о платформе (память, загруженность)";
  String STR_ARG_BACKEND_INFO_PARAMS =
      "Требование вывода указанных параметров из ({@link ISkBackend#getBackendInfo()}). * - все параметры";
  String STR_ARG_INFO_TRANSACTIONS   = "Требование вывода информации о транзакциях";
  String STR_ARG_INFO_OPEN_SESSIONS  = "Требование вывода информации об открытых сессиях";
  String STR_ARG_INFO_CLOSE_SESSIONS = "Требование вывода информации об закрытых сессиях";
  String STR_ARG_INFO_TOPOLOGY       = "Требование вывода информации о топологии кластеров доступных клиенту";
  String STR_ARG_INFO_HISTDATA       = "Требование вывода информации о приеме/передаче хранимых данных клиентам";
  String STR_ARG_INFO_CHECKFILE      =
      "Имя текстового файла (в каталоге data), в формате \"имя_клиента, ip-адрес\" для контроля связи с клиентом";

  String MSG_INFO_CONNECT               = "Сессия                : %s.";
  String MSG_INFO_ID                    = "\nИдентификатор         : %s.\n";
  String MSG_INFO_NAME                  = "Имя                   : %s.\n";
  String MSG_INFO_DESCR                 = "Описание              : %s.\n";
  String MSG_INFO_ZONE                  = "Зона                  : %s.\n";
  String MSG_INFO_START                 = "Время старта          : %s.\n";
  String MSG_INFO_CURRENT               = "Время текущее         : %s.\n";
  String MSG_INFO_SN_ACTIVE             = "Открытых сессий       : %d.\n";
  String MSG_INFO_TX_ACTIVE             = "Активных транзакций   : %d.\n";
  String MSG_INFO_TX_COMMITED           = "Выполненных транзакций: %d.\n";
  String MSG_INFO_TX_ROLLBACKED         = "Отмененных транзакций : %d.\n";
  String MSG_INFO_TX_OPENED_LIST        = "Список открытых транзакций: \n";
  String MSG_INFO_TX_COMMITED_LIST      = "Список последних завершенных транзакций: \n";
  String MSG_INFO_TX_ROLLBACKED_LIST    = "Список последних отмененных транзакций: \n";
  String MSG_INFO_TX_LONGTIME_LIST      = "Список наиболее длительных транзакций: \n";
  String MSG_INFO_OPERATION_SYSTEM_INFO = "Информация по ОС: \n%s";
  String MSG_INFO_HEAP_USAGE_INFO       = "Использование heap памяти: \n%s";
  String MSG_INFO_NON_HEAP_USAGE_INFO   = "Использование non-heap памяти: \n%s";
  String MSG_INFO_CREATE_CHECKFILE      = "Создан файл проверки сессий клиентов: %s\n";
  String MSG_INFO_BACKEND_INFO          = "ISkBackendInfo: \n";
  String MSG_INFO_LINE                  =
      "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_INFO_TX_LEGEND             =
      "| Сессия         | Класс                         | Метод               | Аргументы                     | Статус    | Начало             | Длительность   | Идентификатор                        |  Комментарий \n";
  String MSG_INFO_TX                    =
      "| %-15.15s| %-30.30s| %-20.20s| %-30.30s| %-10.10s| %s| %-15d| %-36.36s | %s\n";
  String MSG_INFO_TX_FREE               = "| %-15s| %-30s| %-20s| %-30s| %-10s| %s| %-15d| %-34s | %s\n";
  String MSG_INFO_SESSIONS_LINE         =
      "-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
  String MSG_INFO_LINE_INTERVAL         =
      "-------------------------------------------------------------------------------------------------------------------------------------- %-34.34s----------------------------\n";
  String MSG_INFO_OPENED_LIST           = "Открытые сессии: \n";
  String MSG_INFO_CLOSED_LIST           = "Закрытые сессии: \n";
  String MSG_INFO_SESSIONS_OPEN         =
      "| WID | Открытие            | login               | IP            | Порт  | Версия                   | Отправлено     | Получено        | Ошибок          | Комментарий\n";
  String MSG_INFO_SESSIONS_CLOSED       =
      "| WID | Открытие            | Закрытие            | login               | IP            | Порт  | Версия                   | Отправлено     | Получено        | Ошибок          | Комментарий\n";
  String MSG_INFO_SESSION_OPEN          =
      "| %-4.4s| %19s | %-20.20s| %-13s | %-5.5s | %-24.24s | %-15d| %-15.15s | %-15d | %s\n";
  String MSG_INFO_SESSION_CLOSED        =
      "| %-4.4s| %19s | %19s | %-20.20s| %-13s | %-5.5s | %-24.24s | %-15d| %-15.15s | %-15d | %s\n";
  String MSG_INFO_SESSIONS_OPEN_FREE    = "| %-10s| %19s | %-20s| %-13s | %-5s | %s | %-15d| %-15s | %-15d | %s\n";
  String MSG_INFO_SESSIONS_CLOSED_FREE  =
      "| %-4s| %19s | %19s | %-20s| %-13s | %-5s | %s | %-15d| %-30s | %-15d | %s\n";

  String MSG_INFO_TOPOLOGIES_OPEN      =
      "| WID | Открытие            | login               | IP            | Порт  | Топология кластеров доступных клиенту                                                       | Комментарий\n";
  String MSG_INFO_TOPOLOGIES_CLOSED    =
      "| WID | Открытие            | Закрытие            | login               | IP            | Порт  | Топология кластеров доступных клиенту                                                       | Комментарий\n";
  String MSG_INFO_TOPOLOGY_OPEN        = "| %-4.4s| %19s | %-20.20s| %-13s | %-5.5s | %-91.91s | %s\n";
  String MSG_INFO_TOPOLOGY_CLOSED      = "| %-4.4s| %19s | %19s | %-20.20s| %-13s | %-5.5s | %-91.91s | %s\n";
  String MSG_INFO_TOPOLOGY_OPEN_FREE   = "| %-10s| %19s | %-20s| %-13s | %-5s | %s | %s\n";
  String MSG_INFO_TOPOLOGY_CLOSED_FREE = "| %-4s| %19s | %19s | %-20s| %-13s | %-5s | %s | %s\n";

  String MSG_INFO_NOT_FOUND          =
      "|     |                     | %-20.20s| %-13s | %-5.5s | %-24.24s |                |                 |                 |                 | Нет сессии клиента\n";
  String MSG_INFO_TOPOLOGY_NOT_FOUND =
      "|     |                     | %-20.20s| %-13s | %-5.5s | %-7.7s                                                                                     | Нет сессии клиента\n";
  String MSG_INFO_RESUME             =
      "|      всего: %-5d box:    %-5d     rcp: %-5d     rap: %-5d     admin: %-5d                     | %-15d| %-15d |\n";

  String ERR_INFO_UNKNOW_CLIENT = "Неизвестный тип клиента";

}
