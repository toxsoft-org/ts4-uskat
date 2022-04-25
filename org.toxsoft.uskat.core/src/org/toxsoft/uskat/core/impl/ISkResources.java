package org.toxsoft.uskat.core.impl;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link AbstractSkService}
   */
  String FMT_INFO_SERVICE_INIT  = "Service %s: init() called";
  String FMT_INFO_SERVICE_CLOSE = "Service %s: close() called";

  /**
   * {@link CoreL10n}
   */
  String FMT_WARN_L10N_NO_ROOT_DIR      = "Локализация uskat не работает, недоступна директория %s";
  String FMT_WARN_L10N_NO_LOCALE_DIR    = "Локализация uskat не работает, нет подпапки локали '%s' или '%s'";
  String FMT_WARN_L10N_BAD_FILE         = "Нет доступа к файлу локализации %s";
  String FMT_ERR_L10N_LOADING_FILE      = "Ошибка чтения из файла локализации %s";
  String FMT_WARN_L10N_INV_DATA_TYPE_ID = "Недопустимый l10n идентификатор типа данного (%s)";
  String FMT_WARN_L10N_INV_SDC_GWID_STR = "Недопустимый l10n Gwid в описании системы (%s)";
  String FMT_WARN_L10N_INV_OBJ_SKID_STR = "Недопустимый l10n Skid объекта (%s)";
  String FMT_LAST_READ_ITEM             = "Last item read was %s (%s)";
  String MSG_NO_ITEMS_READ_YET          = "No items was read from file";

  /**
   * {@link ISkCoreConfigConstants}.
   */
  String STR_N_OP_L10N_FILES_DIR    = "L10n directory";
  String STR_D_OP_L10N_FILES_DIR    = "Localization files root directory";
  String STR_N_OP_LOCALE            = "Locale";
  String STR_D_OP_LOCALE            = "Locale for core entitties localization";
  String STR_N_REF_BACKEND_PROVIDER = "Backend provider";
  String STR_D_REF_BACKEND_PROVIDER = "Refernce to the of the USkat API backend instance creator";

  /**
   * {@link SkConnection}.
   */
  String MSG_ERR_CONN_NOT_ACTIVE = "Connection is not active";
  String MSG_ERR_CONN_IS_OPEN    = "Connection is already open";

  /**
   * {@link SkCoreApi}
   */
  String MSG_ERR_CONN_NOT_OPEN              = "Connection is closed";
  String LOG_WARN_UNHANDLED_BACKEND_MESSAGE = "Unhandled message from backend, topicID= %s";

  /**
   * {@link SkObjectService}
   */
  String FMT_ERR_NO_SUCH_OBJ = "Нет объекта с идентификатором %s";

  /**
   * {@link SkSysdescr}
   */
  String STR_N_ROOT_CLASS    = "Корневой класс";
  String STR_D_ROOT_CLASS    = "Корневой класс иерархии классов USkat";
  String STR_N_ATTR_SKID     = "Skid";
  String STR_D_ATTR_SKID     = "Тип данных, содержащий Skid идентификатор";
  String STR_N_ATTR_CLASS_ID = "ClassId";
  String STR_D_ATTR_CLASS_ID = "Идентификатор класса объекта";
  String STR_N_ATTR_STRID    = "Strid";
  String STR_D_ATTR_STRID    = "Строковый идентификатор объекта (в уникальный в рамках класса)";

}
