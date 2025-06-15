package org.toxsoft.uskat.core.impl;

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
  String FMT_ERR_NO_SUCH_CLASS = Messages.getString( "FMT_ERR_NO_SUCH_CLASS" );
  String FMT_ERR_NO_SUCH_OBJ   = Messages.getString( "FMT_ERR_NO_SUCH_OBJ" );

  /**
   * {@link AbstractSkService}
   */
  String FMT_INFO_SERVICE_INIT         = Messages.getString( "FMT_INFO_SERVICE_INIT" );
  String FMT_INFO_SERVICE_CLOSE        = Messages.getString( "FMT_INFO_SERVICE_CLOSE" );
  String FMT_WARN_INV_SERVICE_GT_MSG   = Messages.getString( "FMT_WARN_INV_SERVICE_GT_MSG" );
  String FMT_WARN_UNKNOWN_MSG          = Messages.getString( "FMT_WARN_UNKNOWN_MSG" );
  String FMT_ERR_CLAIM_VIOLATION       = Messages.getString( "FMT_ERR_CLAIM_VIOLATION" );
  String FMT_ERR_INVALID_THREAD_ACCESS = Messages.getString( "FMT_ERR_INVALID_THREAD_ACCESS" );

  /**
   * {@link ISkCoreConfigConstants}.
   */
  String STR_OP_L10N_FILES_DIR          = Messages.getString( "STR_OP_L10N_FILES_DIR" );
  String STR_OP_L10N_FILES_DIR_D        = Messages.getString( "STR_OP_L10N_FILES_DIR_D" );
  String STR_OP_LOCALE                  = Messages.getString( "STR_OP_LOCALE" );
  String STR_OP_LOCALE_D                = Messages.getString( "STR_OP_LOCALE_D" );
  String STR_OP_DEF_CORE_LOG_SEVERITY   = Messages.getString( "STR_OP_DEF_CORE_LOG_SEVERITY" );
  String STR_OP_DEF_CORE_LOG_SEVERITY_D = Messages.getString( "STR_OP_DEF_CORE_LOG_SEVERITY_D" );
  String STR_REF_THREAD_EXECUTOR        = Messages.getString( "STR_REF_THREAD_EXECUTOR" );
  String STR_REF_THREAD_EXECUTOR_D      = Messages.getString( "STR_REF_THREAD_EXECUTOR_D" );
  String STR_REF_BACKEND_PROVIDER       = Messages.getString( "STR_REF_BACKEND_PROVIDER" );
  String STR_REF_BACKEND_PROVIDER_D     = Messages.getString( "STR_REF_BACKEND_PROVIDER_D" );

  /**
   * {@link SkConnection}.
   */
  String MSG_ERR_CONN_NOT_ACTIVE = Messages.getString( "MSG_ERR_CONN_NOT_ACTIVE" );
  String MSG_ERR_CONN_IS_OPEN    = Messages.getString( "MSG_ERR_CONN_IS_OPEN" );

  /**
   * {@link SkCoreApi}
   */
  String MSG_ERR_CONN_NOT_OPEN              = Messages.getString( "MSG_ERR_CONN_NOT_OPEN" );
  String FMT_ERR_CANT_CREATE_SERVICE        = Messages.getString( "FMT_ERR_CANT_CREATE_SERVICE" );
  String FMT_ERR_DUP_SERVICE_ID             = Messages.getString( "FMT_ERR_DUP_SERVICE_ID" );
  String LOG_WARN_UNHANDLED_BACKEND_MESSAGE = Messages.getString( "LOG_WARN_UNHANDLED_BACKEND_MESSAGE" );

  /**
   * {@link SkCoreServClobs}
   */
  String FMT_ERR_NON_CLOB_GWID        = Messages.getString( "FMT_ERR_NON_CLOB_GWID" );
  String FMT_ERR_CLOB_CLASS_NOT_EXIST = Messages.getString( "FMT_ERR_CLOB_CLASS_NOT_EXIST" );
  String FMT_ERR_CLOB_NOT_EXIST       = Messages.getString( "FMT_ERR_CLOB_NOT_EXIST" );
  String FMT_ERR_CLOB_TOO_LONG        = Messages.getString( "FMT_ERR_CLOB_TOO_LONG" );
  String FMT_ERR_CLOB_TO_BACKEND      = Messages.getString( "FMT_ERR_CLOB_TO_BACKEND" );
  String FMT_ERR_NO_OBJ_OF_CLOB       = Messages.getString( "FMT_ERR_NO_OBJ_OF_CLOB" );

  /**
   * {@link SkCoreServCommands}
   */
  String FMT_ERR_CMD_CLASS_NOT_EXIST      = Messages.getString( "FMT_ERR_CMD_CLASS_NOT_EXIST" );
  String FMT_ERR_CMD_NOT_EXIST            = Messages.getString( "FMT_ERR_CMD_NOT_EXIST" );
  String FMT_ERR_CMD_AUTHOR_NOT_EXIST     = Messages.getString( "FMT_ERR_CMD_AUTHOR_NOT_EXIST" );
  String FMT_ERR_UNHANDLED_CMD            = Messages.getString( "FMT_ERR_UNHANDLED_CMD" );
  String FMT_ERR_UNEXPECTED_EXECUTION     = Messages.getString( "FMT_ERR_UNEXPECTED_EXECUTION" );
  String FMT_LOG_WARN_NO_STATE_CHANGE_CMD = Messages.getString( "FMT_LOG_WARN_NO_STATE_CHANGE_CMD" );

  /**
   * {@link SkCoreServLinks}
   */
  String FMT_ERR_NO_SUCH_LINK1           = Messages.getString( "FMT_ERR_NO_SUCH_LINK1" );
  String FMT_ERR_NO_LINK_LEFT_OBJ        = Messages.getString( "FMT_ERR_NO_LINK_LEFT_OBJ" );
  String FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS = Messages.getString( "FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS" );
  String FMT_ERR_NO_LINK_RIGHT_OBJ_STRID = Messages.getString( "FMT_ERR_NO_LINK_RIGHT_OBJ_STRID" );
  String FMT_ERR_RIGHT_OBJ_INV_CLASS     = Messages.getString( "FMT_ERR_RIGHT_OBJ_INV_CLASS" );

  /**
   * {@link SkCoreServObject}
   */
  String FMT_ERR_INV_ATTR_TYPE      = Messages.getString( "FMT_ERR_INV_ATTR_TYPE" );
  String FMT_ERR_NULL_ATTR_VAL      = Messages.getString( "FMT_ERR_NULL_ATTR_VAL" );
  String FMT_ERR_NO_ATTR_VAL        = Messages.getString( "FMT_ERR_NO_ATTR_VAL" );
  String FMT_ERR_NO_RIVET_CLASS     = Messages.getString( "FMT_ERR_NO_RIVET_CLASS" );
  String FMT_ERR_NO_RIVET           = Messages.getString( "FMT_ERR_NO_RIVET" );
  String FMT_ERR_INV_RIVET_COUNT    = Messages.getString( "FMT_ERR_INV_RIVET_COUNT" );
  String FMT_ERR_INV_RIVET_OBJ_CLS  = Messages.getString( "FMT_ERR_INV_RIVET_OBJ_CLS" );
  String FMT_ERR_OBJ_ALREADY_EXISTS = Messages.getString( "FMT_ERR_OBJ_ALREADY_EXISTS" );
  String FMT_ERR_CANT_CHANGE_SKID   = Messages.getString( "FMT_ERR_CANT_CHANGE_SKID" );
  String FMT_ERR_CANT_REMOVE_NO_OBJ = Messages.getString( "FMT_ERR_CANT_REMOVE_NO_OBJ" );

  /**
   * {@link SkCoreServRtdata}
   */
  String FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE         = Messages.getString( "FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE" );
  String FMT_ERR_CDW_CHANNEL_HAS_NO_KEY           = Messages.getString( "FMT_ERR_CDW_CHANNEL_HAS_NO_KEY" );
  String FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL = Messages.getString( "FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL" );

  /**
   * {@link SkCoreServSysdescr}
   */
  String FMT_WARN_ORPHAN_CLASS             = Messages.getString( "FMT_WARN_ORPHAN_CLASS" );
  String FMT_WARN_UNWANTED_CLASS_ID        = Messages.getString( "FMT_WARN_UNWANTED_CLASS_ID" );
  String FMT_WARN_EMPTY_CLASS_NAME         = Messages.getString( "FMT_WARN_EMPTY_CLASS_NAME" );
  String FMT_ERR_CLASS_HAS_PROP_ID         = Messages.getString( "FMT_ERR_CLASS_HAS_PROP_ID" );
  String FMT_ERR_CLASS_ALREADY_EXISTS      = Messages.getString( "FMT_ERR_CLASS_ALREADY_EXISTS" );
  String FMT_ERR_NO_PARENT_CLASS           = Messages.getString( "FMT_ERR_NO_PARENT_CLASS" );
  String FMT_ERR_DUP_PROP_IN_SUPER         = Messages.getString( "FMT_ERR_DUP_PROP_IN_SUPER" );
  String FMT_ERR_DUP_PROP_IN_SUB           = Messages.getString( "FMT_ERR_DUP_PROP_IN_SUB" );
  String MSG_ERR_CANT_CHANGE_ROOT_CLASS    = Messages.getString( "MSG_ERR_CANT_CHANGE_ROOT_CLASS" );
  String FMT_ERR_CANT_CHANGE_CLASS_ID      = Messages.getString( "FMT_ERR_CANT_CHANGE_CLASS_ID" );
  String FMT_ERR_CANT_CHANGE_PARENT        = Messages.getString( "FMT_ERR_CANT_CHANGE_PARENT" );
  String FMT_ERR_CANT_REMOVE_ABSENT_CLASS  = Messages.getString( "FMT_ERR_CANT_REMOVE_ABSENT_CLASS" );
  String FMT_ERR_CANT_REMOVE_CHILDED_CLASS = Messages.getString( "FMT_ERR_CANT_REMOVE_CHILDED_CLASS" );
  String MSG_ERR_CANT_REMOVE_ROOT_CLASS    = Messages.getString( "MSG_ERR_CANT_REMOVE_ROOT_CLASS" );
  String FMT_ERR_INV_CLASS_LOAD_IGNORED    = Messages.getString( "FMT_ERR_INV_CLASS_LOAD_IGNORED" );

  /**
   * {@link SkCoreServUsers}
   */
  String MSG_ERR_PSWD_IS_BLANK          = Messages.getString( "MSG_ERR_PSWD_IS_BLANK" );
  String FMT_ERR_NOT_USER_DPU           = Messages.getString( "FMT_ERR_NOT_USER_DPU" );
  String FMT_ERR_DUP_USER               = Messages.getString( "FMT_ERR_DUP_USER" );
  String MSG_ERR_NO_ROLES               = Messages.getString( "MSG_ERR_NO_ROLES" );
  String FMT_ERR_INV_ROLES              = Messages.getString( "FMT_ERR_INV_ROLES" );
  String FMT_ERR_NOT_ROLE_DPU           = Messages.getString( "FMT_ERR_NOT_ROLE_DPU" );
  String FMT_ERR_DUP_ROLE               = Messages.getString( "FMT_ERR_DUP_ROLE" );
  String MSG_ERR_CANT_DISABLE_ROOT_USER = Messages.getString( "MSG_ERR_CANT_DISABLE_ROOT_USER" );
  String MSG_WARN_DISABLING_GUEST_USER  = Messages.getString( "MSG_WARN_DISABLING_GUEST_USER" );
  String MSG_ERR_CANT_DISABLE_ROOT_ROLE = Messages.getString( "MSG_ERR_CANT_DISABLE_ROOT_ROLE" );
  String MSG_WARN_DISABLING_GUEST_ROLE  = Messages.getString( "MSG_WARN_DISABLING_GUEST_ROLE" );
  String FMT_WARN_CANT_DEL_NO_USER      = Messages.getString( "FMT_WARN_CANT_DEL_NO_USER" );
  String MSG_ERR_CANT_DEL_ROOT_USER     = Messages.getString( "MSG_ERR_CANT_DEL_ROOT_USER" );
  String MSG_ERR_CANT_DEL_GUEST_USER    = Messages.getString( "MSG_ERR_CANT_DEL_GUEST_USER" );
  String MSG_ERR_CANT_DEL_CURRENT_USER  = Messages.getString( "MSG_ERR_CANT_DEL_CURRENT_USER" );
  String FMT_WARN_CANT_DEL_NO_ROLE      = Messages.getString( "FMT_WARN_CANT_DEL_NO_ROLE" );
  String MSG_ERR_CANT_DEL_ROOT_ROLE     = Messages.getString( "MSG_ERR_CANT_DEL_ROOT_ROLE" );
  String MSG_ERR_CANT_DEL_GUEST_ROLE    = Messages.getString( "MSG_ERR_CANT_DEL_GUEST_ROLE" );
  String MSG_ERR_CANT_DEL_CURRENT_ROLE  = Messages.getString( "MSG_ERR_CANT_DEL_CURRENT_ROLE" );

  /**
   * {@link SkCoreUtils}
   */
  String STR_ROOT_CLASS      = Messages.getString( "STR_ROOT_CLASS" );
  String STR_ROOT_CLASS_D    = Messages.getString( "STR_ROOT_CLASS_D" );
  String STR_ATTR_SKID       = Messages.getString( "STR_ATTR_SKID" );
  String STR_ATTR_SKID_D     = Messages.getString( "STR_ATTR_SKID_D" );
  String STR_ATTR_CLASS_ID   = Messages.getString( "STR_ATTR_CLASS_ID" );
  String STR_ATTR_CLASS_ID_D = Messages.getString( "STR_ATTR_CLASS_ID_D" );
  String STR_ATTR_STRID      = Messages.getString( "STR_ATTR_STRID" );
  String STR_ATTR_STRID_D    = Messages.getString( "STR_ATTR_STRID_D" );

  /**
   * {@link SkAsynchronousQuery}
   */
  String FMT_ERR_QUERY_INVALID_STATE = Messages.getString( "FMT_ERR_QUERY_INVALID_STATE" );
  String FMT_ERR_QUERY_TIMEOUT       = Messages.getString( "FMT_ERR_QUERY_TIMEOUT" );

  /**
   * {@link SkatletBase}
   */
  String FMT_INFO_SKATLET_INITIALIZE  = Messages.getString( "FMT_INFO_SKATLET_INITIALIZE" );
  String FMT_INFO_SKATLET_SET_CONTEXT = Messages.getString( "FMT_INFO_SKATLET_SET_CONTEXT" );
  String FMT_INFO_SKATLET_START       = Messages.getString( "FMT_INFO_SKATLET_START" );
  String FMT_INFO_SKATLET_QUERY_STOP  = Messages.getString( "FMT_INFO_SKATLET_QUERY_STOP" );
  String FMT_INFO_SKATLET_DESTROY     = Messages.getString( "FMT_INFO_SKATLET_DESTROY" );
  String FMT_INFO_SKATLET_DOJOB       = Messages.getString( "FMT_INFO_SKATLET_DOJOB" );
  String FMT_ERR_SKATLET_INITIALIZE   = Messages.getString( "FMT_ERR_SKATLET_INITIALIZE" );
  String FMT_WARN_ATTEMPT_OPEN_SHARE  = Messages.getString( "FMT_WARN_ATTEMPT_OPEN_SHARE" );
  String FMT_WARN_ATTEMPT_CLOSE_SHARE = Messages.getString( "FMT_WARN_ATTEMPT_OPEN_SHARE" );

  /**
   * {@link AbstractSkRivetEditor}
   */
  String METHOD_CREATING_RIVETS = "creatingRivets";
  String METHOD_UPDATING_RIVETS = "updatingRivets";
  String METHOD_REMOVING_RIVETS = "removingRivets";

  String ERR_CANT_REMOVE_HAS_RIVET_REVS        =
      "%s: prohibition of deleting an object that is in the rivets of other objects: \n%s";
  String ERR_RIVERT_REVS_ALREADY_EXIST         =
      "%s(...): rightObj %s, rivetId = %s, leftObj =%s: rivet rev already exist.";
  String ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND    = "%s(...): right obj %s is not found";
  String ERR_RIVET_REVS_LEFT_OBJ_NOT_FOUND     =
      "%s(...): rightObj %s, rivetId = %s, leftObj =%s: leftObj is not found";
  String ERR_RIVET_REVS_EDITOR_CLASS_NOT_FOUND = "openRivetRevsEditor(...): %s: rivetClassId %s is not found";
  String ERR_RIVET_REVS_EDITOR_RIVET_NOT_FOUND = "openRivetRevsEditor(...): %s: rivetId %s is not found";
}
