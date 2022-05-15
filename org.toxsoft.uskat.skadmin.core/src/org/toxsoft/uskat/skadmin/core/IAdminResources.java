package org.toxsoft.uskat.skadmin.core;

/**
 * Константы, локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminResources {

  String E_CN_N_PLUGIN_PATHS    = "Плагины";
  String E_CN_D_PLUGIN_PATHS    = "Каталоги в которых могут находится плагины для s5admin";
  String E_CN_N_APPLICATION_DIR = "Каталог приложения";
  String E_CN_D_APPLICATION_DIR = "Каталог относительного которого конечное приложение формирует свою файловую систему";
  String E_CN_N_CONNECTION      = "s5connection";
  String E_CN_D_CONNECTION      = "Соединение с s5-сервером";

  String E_CN_N_SERVER_API = "API сервера";
  String E_CN_D_SERVER_API = "Клиентская реализация API сервера s5";

  String E_CN_N_MESSAGE_SERVICE    = "Служба сообщений";
  String E_CN_D_MESSAGE_SERVICE    = "Клиентская реализация службы сообщений";
  String E_CN_N_CLASS_SERVICE      = "Служба классов";
  String E_CN_D_CLASS_SERVICE      = "Клиентская реализация службы управления классами";
  String E_CN_N_OBJECT_SERVICE     = "Служба объектов";
  String E_CN_D_OBJECT_SERVICE     = "Клиентская реализация службы управления объектами";
  String E_CN_N_LINK_SERVICE       = "Служба связей";
  String E_CN_D_LINK_SERVICE       = "Клиентская реализация службы управления связями между объектами";
  String E_CN_N_REFBOOK_SERVICE    = "Служба справочников";
  String E_CN_D_REFBOOK_SERVICE    = "Клиентская реализация службы управления справочниками";
  String E_CN_N_USER_SERVICE       = "Служба пользователей";
  String E_CN_D_USER_SERVICE       = "Клиентская реализация службы управления пользователями";
  String E_CN_N_USER_PREFS_SERVICE = "Служба настроек";
  String E_CN_D_USER_PREFS_SERVICE = "Клиентская реализация службы управления настройками пользователей";
  String E_CN_N_PORTABLE_SERVICE   = "Служба портирования";
  String E_CN_D_PORTABLE_SERVICE   = "Клиентская реализация службы портирования классов и объектов";
  String E_CN_N_CURRDATA_SERVICE   = "Служба текущих данных";
  String E_CN_D_CURRDATA_SERVICE   = "Клиентская реализация службы текущих данных";
  String E_CN_N_HISTDATA_SERVICE   = "Служба исторических данных";
  String E_CN_D_HISTDATA_SERVICE   = "Клиентская реализация службы исторических данных";
  String E_CN_N_COMMAND_SERVICE    = "Служба команд";
  String E_CN_D_COMMAND_SERVICE    = "Клиентская реализация службы команд";
  String E_CN_N_EVENT_SERVICE      = "Служба событий";
  String E_CN_D_EVENT_SERVICE      = "Клиентская реализация службы событий";

  String E_CN_N_CLASS_EDITOR        = "Редактор класса";
  String E_CN_D_CLASS_EDITOR        = "Редактор класса объектов";
  String E_CN_N_OBJECT_EDITOR       = "Редактор объекта";
  String E_CN_D_OBJECT_EDITOR       = "Редактор объекта";
  String E_CN_N_USER_EDITOR         = "Редактор пользователя";
  String E_CN_D_USER_EDITOR         = "Редактор пользователя";
  String E_CN_N_REFBOOK_EDITOR      = "Редактор справочника";
  String E_CN_D_REFBOOK_EDITOR      = "Редактор справочника";
  String E_CN_N_REFBOOK_ITEM_EDITOR = "Редактор элемента справочника";
  String E_CN_D_REFBOOK_ITEM_EDITOR = "Редактор элемента справочника";
  String E_CN_N_AV_TREE_EDITOR      = "Редактор дерева значений";
  String E_CN_D_AV_TREE_EDITOR      = "Редактор дерева значений";

  String E_CN_N_BS_OBJ  = "Объект";
  String E_CN_D_BS_OBJ  = "Бизнес объект системы";
  String E_CN_N_BS_OBJS = "Объекты";
  String E_CN_D_BS_OBJS = "Список бизнес объектов системы";

  String E_CN_N_SK_CONNECTION     = "skConnection";
  String E_CN_D_SK_CONNECTION     = "Подключение к серверу skat.s5";
  String E_CN_N_PAS_CLIENT    = "pasClient";
  String E_CN_D_PAS_CLIENT    = "Клиент сервера PAS (Public Access Server)";
  String E_CN_N_CORE_API          = "API";
  String E_CN_D_CORE_API          = "API сервера skat.s5";
  String E_CN_N_HOST              = "Адрес";
  String E_CN_D_HOST              = "Адрес подключенного сервера";
  String E_CN_N_SK_CLASS_SERVICE  = "Служба классов";
  String E_CN_D_SK_CLASS_SERVICE  = "Cлужба управления классами сервера skat.s5";
  String E_CN_N_SK_OBJECT_SERVICE = "Служба объектов";
  String E_CN_D_SK_OBJECT_SERVICE = "Cлужба управления объектами сервера skat.s5";
  String E_CN_N_SK_LINK_SERVICE   = "Служба связей";
  String E_CN_D_SK_LINK_SERVICE   = "Cлужба управления связями сервера skat.s5";

  String E_CN_N_SK_VALUE = "Значение";
  String E_CN_D_SK_VALUE = "Атомарное значение данного (IAtomicValue)";

  String MSG_ERR_WRONG_CONSTRUCTOR = "Для создания значений команды требуется использовать другой конструктор";
  String MSG_ERR_WRONG_VALUE_TYPE  = "Несовместимые типы значения. AdminValueType = %s, IAtomicValue.type = %s";
  String MSG_ERR_VALUE_IS_SINGLE   = "Значение является единичным значением IAtomicValue";
  String MSG_ERR_VALUE_IS_ARRAY    = "Значение является массивом значений IAtomicValue";
}
