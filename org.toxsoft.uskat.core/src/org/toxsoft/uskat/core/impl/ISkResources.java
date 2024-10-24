package org.toxsoft.uskat.core.impl;

import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * Common messages.
   */
  String FMT_ERR_NO_SUCH_CLASS = Messages.getString( "FMT_ERR_NO_SUCH_CLASS" ); //$NON-NLS-1$
  String FMT_ERR_NO_SUCH_OBJ   = "Object '%s' does not exists";

  /**
   * {@link AbstractSkService}
   */
  String FMT_INFO_SERVICE_INIT         = "Service %s: init() called";
  String FMT_INFO_SERVICE_CLOSE        = "Service %s: close() called";
  String FMT_WARN_INV_SERVICE_GT_MSG   = "Service %s: received invalid message with topic ID '%s'";
  String FMT_WARN_UNKNOWN_MSG          = "Service %s: received message with unknown message ID '%s'";
  String FMT_ERR_CLAIM_VIOLATION       = "Entities of class ID %s are claimed by service %s";
  String FMT_ERR_INVALID_THREAD_ACCESS = "Invalid thread access (TsIllegalStateRtException)";

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
  String STR_N_OP_L10N_FILES_DIR        = "L10n directory";
  String STR_D_OP_L10N_FILES_DIR        = "Localization files root directory";
  String STR_N_OP_LOCALE                = "Locale";
  String STR_D_OP_LOCALE                = "Locale for core entitties localization";
  String STR_N_OP_DEF_CORE_LOG_SEVERITY = "Log severity";
  String STR_D_OP_DEF_CORE_LOG_SEVERITY = "Default log messages lowest severity to be logged";
  String STR_N_REF_THREAD_EXECUTOR      = "Thread executor";
  String STR_D_REF_THREAD_EXECUTOR      = "Еxecutor of API calls in one thread used by the connection";
  String STR_N_REF_BACKEND_PROVIDER     = "Backend provider";
  String STR_D_REF_BACKEND_PROVIDER     = "Refernce to the of the USkat API backend instance creator";

  /**
   * {@link SkConnection}.
   */
  String MSG_ERR_CONN_NOT_ACTIVE = "Connection is not active";
  String MSG_ERR_CONN_IS_OPEN    = "Connection is already open";

  /**
   * {@link SkCoreApi}
   */
  String MSG_ERR_CONN_NOT_OPEN              = "Connection is closed";
  String FMT_ERR_CANT_CREATE_SERVICE        = "Creator '%s' can not create service instance";
  String FMT_ERR_DUP_SERVICE_ID             = "Creator '%s' tries to create service of existing ID '%s'";
  String LOG_WARN_UNHANDLED_BACKEND_MESSAGE = "Unhandled message from backend, topicID= %s, messageId = %s, args = %s";

  /**
   * {@link SkCoreServClobs}
   */
  String FMT_ERR_NON_CLOB_GWID        = "Concrete GWID of CLOB was expected instead of '%s'";
  String FMT_ERR_CLOB_CLASS_NOT_EXIST = "CLOB class %s does not exist";
  String FMT_ERR_CLOB_NOT_EXIST       = "CLOB %s does not exist in class %s";
  String FMT_ERR_CLOB_TOO_LONG        = "Clob length %d exceeds platforn restirction %d";
  String FMT_ERR_CLOB_TO_BACKEND      = "Error writing CLOB (GWID='%s') to to the backend";
  String FMT_ERR_NO_OBJ_OF_CLOB       = "Object '%s' for CLOB '%s' doesa not exists";

  /**
   * {@link SkCoreServCommands}
   */
  String FMT_ERR_CMD_CLASS_NOT_EXIST      = "Command class %s does not exist";
  String FMT_ERR_CMD_NOT_EXIST            = "Command %s does not exist in class %s";
  String FMT_ERR_CMD_AUTHOR_NOT_EXIST     = "Author %s does not exist";
  String FMT_ERR_UNHANDLED_CMD            = "No executor found for command: %s";                   //$NON-NLS-1$
  String FMT_ERR_UNEXPECTED_EXECUTION     = "Unexpected error while executing command: %s";        //$NON-NLS-1$
  String FMT_LOG_WARN_NO_STATE_CHANGE_CMD = "No executing command found for state change info %s"; //$NON-NLS-1$

  /**
   * {@link SkCoreServLinks}
   */
  String FMT_ERR_NO_SUCH_LINK1           = "Link '%s' does not exists in class '%s'";
  String FMT_ERR_NO_LINK_LEFT_OBJ        = "Left object %s of link %s does not exists";
  String FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS = "Class of right object %s of link %s does not exists";
  String FMT_ERR_NO_LINK_RIGHT_OBJ_STRID = "Right object %s of link %s does not exists";
  String FMT_ERR_RIGHT_OBJ_INV_CLASS     = "Right object %s is not allowed in link %s";

  /**
   * {@link SkCoreServObject}
   */
  String FMT_ERR_INV_ATTR_TYPE      = "Object '%s', attribute '%s': value type '%s' is not assignable to the type '%s'";
  String FMT_ERR_NO_ATTR_VAL        = "Object '%s': value of the attribute '%s' must be specified";
  String FMT_ERR_NO_RIVET_CLASS     = "Object '%s', rivet '%s': right class '%s' does not exists";
  String FMT_ERR_NO_RIVET           = "Object '%s': riveted objects of the rivet '%s' must be specified";
  String FMT_ERR_INV_RIVET_COUNT    = "Object '%s', rivet '%s': number of riveted objects is %d instead of %d";
  String FMT_ERR_INV_RIVET_OBJ_CLS  = "Object '%s', rivet '%s': right object '%s' is not of class '%s'";
  String FMT_ERR_OBJ_ALREADY_EXISTS = "Object with SKID '%s' уже существует";
  String FMT_ERR_CANT_CHANGE_SKID   = "Object '%s': can't change SKID";
  String FMT_ERR_CANT_REMOVE_NO_OBJ = "Object '%s'^ does not exists, can not remove it";

  /**
   * {@link SkCoreServRtdata}
   */
  String FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE         = "RtData channel %s: got %s value instead of %s";
  String FMT_ERR_CDW_CHANNEL_HAS_NO_KEY           = "CurrDataWrite channel %s has no key(-1)";
  String FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL = "History data write interval %s is less then values interval %s";

  /**
   * {@link SkCoreServSysdescr}
   */
  String FMT_WARN_ORPHAN_CLASS             = "Ignoring an orphan class loaded from backend (class ID: '%s')";
  String FMT_WARN_UNWANTED_CLASS_ID        = "Class ID has unwanted value '%s'";
  String FMT_WARN_EMPTY_CLASS_NAME         = "Желательно задать имя класса с идентификатором %s";
  String FMT_ERR_CLASS_HAS_PROP_ID         = "Свойство с идентификатором %s уже существует в классе";
  String FMT_ERR_CLASS_ALREADY_EXISTS      = "Класс с идентификатором %s уже существует";
  String FMT_ERR_NO_PARENT_CLASS           = "Не существует родительского класса с идентификатором '%s'";
  String FMT_ERR_DUP_PROP_IN_SUPER         = "%s '%s' is already declared in the ancestor class '%s'";
  String FMT_ERR_DUP_PROP_IN_SUB           = "%s '%s' is already declared in the descendant classes '%s'";
  String MSG_ERR_CANT_CHANGE_ROOT_CLASS    = "Нельзя редактировать корневой класс";
  String FMT_ERR_CANT_CHANGE_CLASS_ID      = "Нельзя изменить идентификатор существующего класса %s";
  String FMT_ERR_CANT_CHANGE_PARENT        = "Нельзя изменить родителя существующего класса %s";
  String FMT_ERR_CANT_REMOVE_ABSENT_CLASS  = "Нельзя удалить не существующий класс %s";
  String FMT_ERR_CANT_REMOVE_CHILDED_CLASS = "Нельзя удалить класс %s - у него есть наследники";
  String MSG_ERR_CANT_REMOVE_ROOT_CLASS    = "Нельзя удалить корневой класс";
  String FMT_ERR_INV_CLASS_LOAD_IGNORED    = "Class '%s' ignored: %s '%s' is already declared in class '%s'";

  /**
   * {@link SkCoreServUsers}
   */
  String MSG_ERR_PSWD_IS_BLANK          = "Не допускается пустой пароль";
  String FMT_ERR_NOT_USER_DPU           = "Внутрненняя ошибка: DPU пользователя имеет класс '%s' вместо '%s'";
  String FMT_ERR_DUP_USER               = "Пользователь с логином %s уже существет";
  String MSG_ERR_NO_ROLES               = "Пользователю должна быть назначена хотя бы одна роль";
  String FMT_ERR_INV_ROLES              = "Попытка назначить несуществующую роль %s";
  String FMT_ERR_NOT_ROLE_DPU           = "Внутрненняя ошибка: DPU роли имеет класс '%s' вместо '%s'";
  String FMT_ERR_DUP_ROLE               = "Роль с идентификатором %s уже существет";
  String MSG_ERR_CANT_DISABLE_ROOT_USER = "Запрет отключения учетной записи суперпользователя";
  String MSG_WARN_DISABLING_GUEST_USER  = "Нежелательно отклуючать учетную запись гостя";
  String MSG_ERR_CANT_DISABLE_ROOT_ROLE = "Запрет отключения роли суперпользователя";
  String MSG_WARN_DISABLING_GUEST_ROLE  = "Нежелательно отклуючать гостевую роль";

  String FMT_WARN_CANT_DEL_NO_USER   = "Нельзя удалить несуществующего пользователя с логином '%s'";
  String MSG_ERR_CANT_DEL_ROOT_USER  = "Нельзя удалить учетную запись суперпользователя";
  String MSG_ERR_CANT_DEL_GUEST_USER = "Нельзя удалить гостевую учетную запись";
  String FMT_WARN_CANT_DEL_NO_ROLE   = "Нельзя удалить несуществующую роль с идентификатором '%s'";
  String MSG_ERR_CANT_DEL_ROOT_ROLE  = "Нельзя удалить роль суперпользователя";
  String MSG_ERR_CANT_DEL_GUEST_ROLE = "Нельзя удалить гостевую роль";

  // ------------------------------------------------------------------------------------
  // USkat entities are defined only in English, l10n done via USkat localization service
  String STR_ROOT_ROLE    = "Superuser";        //$NON-NLS-1$
  String STR_ROOT_ROLE_D  = "Superuser role";   //$NON-NLS-1$
  String STR_GUEST_ROLE   = "Guest";            //$NON-NLS-1$
  String STR_GUEST_ROLE_D = "Guest role";       //$NON-NLS-1$
  String STR_ROOT_USER    = "Root";             //$NON-NLS-1$
  String STR_ROOT_USER_D  = "Root - superuser"; //$NON-NLS-1$
  String STR_GUEST_USER   = "Guest";            //$NON-NLS-1$
  String STR_GUEST_USER_D = "Guest user";       //$NON-NLS-1$

  /**
   * {@link SkCoreUtils}
   */
  String STR_ROOT_CLASS      = Messages.getString( "STR_ROOT_CLASS" );                          //$NON-NLS-1$
  String STR_ROOT_CLASS_D    = Messages.getString( "STR_ROOT_CLASS_D" );                        //$NON-NLS-1$
  String STR_N_ATTR_SKID     = "Skid";
  String STR_D_ATTR_SKID     = "Тип данных, содержащий Skid идентификатор";
  String STR_N_ATTR_CLASS_ID = "ClassId";
  String STR_D_ATTR_CLASS_ID = "Идентификатор класса объекта";
  String STR_N_ATTR_STRID    = "Strid";
  String STR_D_ATTR_STRID    = "Строковый идентификатор объекта (в уникальный в рамках класса)";

  /**
   * {@link SkAsynchronousQuery}
   */
  String FMT_ERR_QUERY_INVALID_STATE = "%s. query invalid state: %s";
  String FMT_ERR_QUERY_TIMEOUT       = "Cancel query by timeout error. Try change -"
      + ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME.id() + " value to up (%d))";

  /**
   * {@link SkatletBase}
   */
  String FMT_INFO_SKATLET_INIT       = "%s doInit(). connection = %s.";
  String FMT_INFO_SKATLET_START      = "%s: start().";
  String FMT_INFO_SKATLET_QUERY_STOP = "%s: queryStop().";
  String FMT_INFO_SKATLET_DESTROY    = "%s: destroy().";
  String FMT_INFO_SKATLET_DOJOB      = "%s: doJob().";
}
