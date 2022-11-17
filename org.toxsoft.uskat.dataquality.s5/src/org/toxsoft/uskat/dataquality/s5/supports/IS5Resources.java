package org.toxsoft.uskat.dataquality.s5.supports;

/**
 * Локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_DATA_QUALITY = "Поддержка расширения бекенда для службы качества данных ISkDataQualitySerivce";

  String STR_D_TICKETS = "Список зарегистрированных тикетов";
  String STR_N_TICKETS = "Список тикетов";

  String STR_D_NOT_CONNECTED = "Нет связи с поставщиком данного";
  String STR_N_NOT_CONNECTED = "Нет связи";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_REGISTER_TICKET          = "Регистрация нового тикета %s";
  String MSG_UNREGISTER_TICKET        = "ДЕРегистрация тикета %s";
  String MSG_SET_CONNECTED_VENDOR     =
      "Регистрация поставщика ресурсов(NoConnection). Количество ресурсов = %d. sessionID = %s:\n%s";
  String MSG_SET_MARK                 =
      "Установка метки %s (value = %s). Количество ресурсов = %d. sessionID = %s:\n%s";
  String MSG_SET_MARK_RESOURCE        = "Ресурс %s. Установка метки %s (value = %s). sessionID = %s";
  String MSG_REMOVE_MARK              = "Удаление метки %s.  Количество ресурсов = %d.  sessionID = %s";
  String MSG_NO_RESOURCE_SESSION      = "Сессия не регистрировала ресурсы. sessionID = %s";
  String MSG_REMOVE_MARK_RESOURCE     = "Ресурс %s. Удаление метки %s. sessionID = %s";
  String MSG_SUPPLY_BY_OTHER_SESSIONS = "Ресурс %s. Значение метки %s предоставляется другими сессиями (size = %d)";
  String MSG_GWID                     = "   %s\n";
  String MSG_ADD_SESSION_RESOURCES    = "addConnectedResources(...): aSessionID = %s, aResources = %d, needEvent = %b";
  String MSG_REMOVE_SESSION_RESOURCES =
      "removeConnectedResources(...): aSessionID = %s, aResources = %d, needEvent = %b";
  String MSG_SET_SESSION_RESOURCES    =
      "setConnectedResources(...): aSessionID = %s, prevResources = %d, newResources = %d, needEvent = %b";
  String MSG_AFTER_CLOSE_SESSION      = "afterCloseSession(...): aSessionID = %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_CLASS_NOT_FOUND                  = "Gwid-идентификатор представляет несуществующий класс. gwid = %s";
  String ERR_OBJECT_NOT_FOUND                 = "Gwid-идентификатор представляет несуществующий объект. gwid = %s";
  String ERR_DATA_NOT_FOUND                   = "Gwid-идентификатор представляет несуществующее данное. gwid = %s";
  String ERR_ABSTRACT_GWID                    =
      "Запрещено использовать абстрактные Gwid-идентификаторы данных (без указания объекта(ов, *). gwid = %s";
  String ERR_NO_DATA_GWID                     =
      "Gwid-идентификатор должен представлять данное объекта (EGwidKind.GW_RTDATA). gwid = %s";
  String ERR_MULTI_RESOURCE_NOT_ALLOWED       =
      "Ресурс указанный через групповой идентификатор(Gwid.isMulti() == true)  не допускается. aGwid = %s";
  String ERR_EDIT_BUILTIN_TICKET              = "Попытка редактирования встроенного тикета %s";
  String ERR_TICKET_NOT_FOUND                 = "Тикет %s не зарегистрирован";
  String ERR_MARK_BUILTIN_TICKET              = "Попытка установки метки встроенного тикета %s";
  String ERR_WRONG_TICKET_MARK                =
      "Недопустимое значение метки %s (с типом %s) для  тикета %s (с типом %s)";
  String ERR_IGNORE_TICKET_FOR_UNDEF_RESOURCE =
      "Игнорирование попытки установить тикет %s [value = %s] для ресурса с которым нет связи %s";
  String ERR_RESOURCE_ALREADY_UNREG           = "Ресурс %s уже дерегистрирован";
}
