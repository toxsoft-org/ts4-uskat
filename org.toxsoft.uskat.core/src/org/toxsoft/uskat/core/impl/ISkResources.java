package org.toxsoft.uskat.core.impl;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * Common messages.
   */
  String FMT_ERR_NO_SUCH_CLASS = Messages.getString( "FMT_ERR_NO_SUCH_CLASS" ); //$NON-NLS-1$
  String FMT_ERR_NO_SUCH_OBJ   = Messages.getString( "FMT_ERR_NO_SUCH_OBJ" );   //$NON-NLS-1$

  /**
   * {@link AbstractSkService}
   */
  String FMT_INFO_SERVICE_INIT         = Messages.getString( "FMT_INFO_SERVICE_INIT" );         //$NON-NLS-1$
  String FMT_INFO_SERVICE_CLOSE        = Messages.getString( "FMT_INFO_SERVICE_CLOSE" );        //$NON-NLS-1$
  String FMT_WARN_INV_SERVICE_GT_MSG   = Messages.getString( "FMT_WARN_INV_SERVICE_GT_MSG" );   //$NON-NLS-1$
  String FMT_WARN_UNKNOWN_MSG          = Messages.getString( "FMT_WARN_UNKNOWN_MSG" );          //$NON-NLS-1$
  String FMT_ERR_CLAIM_VIOLATION       = Messages.getString( "FMT_ERR_CLAIM_VIOLATION" );       //$NON-NLS-1$
  String FMT_ERR_INVALID_THREAD_ACCESS = Messages.getString( "FMT_ERR_INVALID_THREAD_ACCESS" ); //$NON-NLS-1$

  /**
   * {@link CoreL10n}
   */
  String FMT_WARN_L10N_NO_ROOT_DIR      = Messages.getString( "FMT_WARN_L10N_NO_ROOT_DIR" );      //$NON-NLS-1$
  String FMT_WARN_L10N_NO_LOCALE_DIR    = Messages.getString( "FMT_WARN_L10N_NO_LOCALE_DIR" );    //$NON-NLS-1$
  String FMT_WARN_L10N_BAD_FILE         = Messages.getString( "FMT_WARN_L10N_BAD_FILE" );         //$NON-NLS-1$
  String FMT_ERR_L10N_LOADING_FILE      = Messages.getString( "FMT_ERR_L10N_LOADING_FILE" );      //$NON-NLS-1$
  String FMT_WARN_L10N_INV_DATA_TYPE_ID = Messages.getString( "FMT_WARN_L10N_INV_DATA_TYPE_ID" ); //$NON-NLS-1$
  String FMT_WARN_L10N_INV_SDC_GWID_STR = Messages.getString( "FMT_WARN_L10N_INV_SDC_GWID_STR" ); //$NON-NLS-1$
  String FMT_WARN_L10N_INV_OBJ_SKID_STR = Messages.getString( "FMT_WARN_L10N_INV_OBJ_SKID_STR" ); //$NON-NLS-1$
  String FMT_LAST_READ_ITEM             = Messages.getString( "FMT_LAST_READ_ITEM" );             //$NON-NLS-1$
  String MSG_NO_ITEMS_READ_YET          = Messages.getString( "MSG_NO_ITEMS_READ_YET" );          //$NON-NLS-1$

  /**
   * {@link ISkCoreConfigConstants}.
   */
  String STR_OP_L10N_FILES_DIR          = Messages.getString( "STR_OP_L10N_FILES_DIR" );          //$NON-NLS-1$
  String STR_OP_L10N_FILES_DIR_D        = Messages.getString( "STR_OP_L10N_FILES_DIR_D" );        //$NON-NLS-1$
  String STR_OP_LOCALE                  = Messages.getString( "STR_OP_LOCALE" );                  //$NON-NLS-1$
  String STR_OP_LOCALE_D                = Messages.getString( "STR_OP_LOCALE_D" );                //$NON-NLS-1$
  String STR_OP_DEF_CORE_LOG_SEVERITY   = Messages.getString( "STR_OP_DEF_CORE_LOG_SEVERITY" );   //$NON-NLS-1$
  String STR_OP_DEF_CORE_LOG_SEVERITY_D = Messages.getString( "STR_OP_DEF_CORE_LOG_SEVERITY_D" ); //$NON-NLS-1$
  String STR_REF_THREAD_EXECUTOR        = Messages.getString( "STR_REF_THREAD_EXECUTOR" );        //$NON-NLS-1$
  String STR_REF_THREAD_EXECUTOR_D      = Messages.getString( "STR_REF_THREAD_EXECUTOR_D" );      //$NON-NLS-1$
  String STR_REF_BACKEND_PROVIDER       = Messages.getString( "STR_REF_BACKEND_PROVIDER" );       //$NON-NLS-1$
  String STR_REF_BACKEND_PROVIDER_D     = Messages.getString( "STR_REF_BACKEND_PROVIDER_D" );     //$NON-NLS-1$

  /**
   * {@link SkConnection}.
   */
  String MSG_ERR_CONN_NOT_ACTIVE = Messages.getString( "MSG_ERR_CONN_NOT_ACTIVE" ); //$NON-NLS-1$
  String MSG_ERR_CONN_IS_OPEN    = Messages.getString( "MSG_ERR_CONN_IS_OPEN" );    //$NON-NLS-1$

  /**
   * {@link SkCoreApi}
   */
  String MSG_ERR_CONN_NOT_OPEN              = Messages.getString( "MSG_ERR_CONN_NOT_OPEN" );              //$NON-NLS-1$
  String FMT_ERR_CANT_CREATE_SERVICE        = Messages.getString( "FMT_ERR_CANT_CREATE_SERVICE" );        //$NON-NLS-1$
  String FMT_ERR_DUP_SERVICE_ID             = Messages.getString( "FMT_ERR_DUP_SERVICE_ID" );             //$NON-NLS-1$
  String LOG_WARN_UNHANDLED_BACKEND_MESSAGE = Messages.getString( "LOG_WARN_UNHANDLED_BACKEND_MESSAGE" ); //$NON-NLS-1$

  /**
   * {@link SkCoreServClobs}
   */
  String FMT_ERR_NON_CLOB_GWID        = Messages.getString( "FMT_ERR_NON_CLOB_GWID" );        //$NON-NLS-1$
  String FMT_ERR_CLOB_CLASS_NOT_EXIST = Messages.getString( "FMT_ERR_CLOB_CLASS_NOT_EXIST" ); //$NON-NLS-1$
  String FMT_ERR_CLOB_NOT_EXIST       = Messages.getString( "FMT_ERR_CLOB_NOT_EXIST" );       //$NON-NLS-1$
  String FMT_ERR_CLOB_TOO_LONG        = Messages.getString( "FMT_ERR_CLOB_TOO_LONG" );        //$NON-NLS-1$
  String FMT_ERR_CLOB_TO_BACKEND      = Messages.getString( "FMT_ERR_CLOB_TO_BACKEND" );      //$NON-NLS-1$
  String FMT_ERR_NO_OBJ_OF_CLOB       = Messages.getString( "FMT_ERR_NO_OBJ_OF_CLOB" );       //$NON-NLS-1$

  /**
   * {@link SkCoreServCommands}
   */
  String FMT_ERR_CMD_CLASS_NOT_EXIST      = Messages.getString( "FMT_ERR_CMD_CLASS_NOT_EXIST" );      //$NON-NLS-1$
  String FMT_ERR_CMD_NOT_EXIST            = Messages.getString( "FMT_ERR_CMD_NOT_EXIST" );            //$NON-NLS-1$
  String FMT_ERR_CMD_AUTHOR_NOT_EXIST     = Messages.getString( "FMT_ERR_CMD_AUTHOR_NOT_EXIST" );     //$NON-NLS-1$
  String FMT_ERR_UNHANDLED_CMD            = Messages.getString( "FMT_ERR_UNHANDLED_CMD" );            //$NON-NLS-1$
  String FMT_ERR_UNEXPECTED_EXECUTION     = Messages.getString( "FMT_ERR_UNEXPECTED_EXECUTION" );     //$NON-NLS-1$
  String FMT_LOG_WARN_NO_STATE_CHANGE_CMD = Messages.getString( "FMT_LOG_WARN_NO_STATE_CHANGE_CMD" ); //$NON-NLS-1$

  /**
   * {@link SkCoreServLinks}
   */
  String FMT_ERR_NO_SUCH_LINK1           = Messages.getString( "FMT_ERR_NO_SUCH_LINK1" );           //$NON-NLS-1$
  String FMT_ERR_NO_LINK_LEFT_OBJ        = Messages.getString( "FMT_ERR_NO_LINK_LEFT_OBJ" );        //$NON-NLS-1$
  String FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS = Messages.getString( "FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS" ); //$NON-NLS-1$
  String FMT_ERR_NO_LINK_RIGHT_OBJ_STRID = Messages.getString( "FMT_ERR_NO_LINK_RIGHT_OBJ_STRID" ); //$NON-NLS-1$
  String FMT_ERR_RIGHT_OBJ_INV_CLASS     = Messages.getString( "FMT_ERR_RIGHT_OBJ_INV_CLASS" );     //$NON-NLS-1$

  /**
   * {@link SkCoreServObject}
   */
  String FMT_ERR_INV_ATTR_TYPE      = Messages.getString( "FMT_ERR_INV_ATTR_TYPE" );      //$NON-NLS-1$
  String FMT_ERR_NULL_ATTR_VAL      = Messages.getString( "FMT_ERR_NULL_ATTR_VAL" );      //$NON-NLS-1$
  String FMT_ERR_NO_ATTR_VAL        = Messages.getString( "FMT_ERR_NO_ATTR_VAL" );        //$NON-NLS-1$
  String FMT_ERR_NO_RIVET_CLASS     = Messages.getString( "FMT_ERR_NO_RIVET_CLASS" );     //$NON-NLS-1$
  String FMT_ERR_NO_RIVET           = Messages.getString( "FMT_ERR_NO_RIVET" );           //$NON-NLS-1$
  String FMT_ERR_INV_RIVET_COUNT    = Messages.getString( "FMT_ERR_INV_RIVET_COUNT" );    //$NON-NLS-1$
  String FMT_ERR_INV_RIVET_OBJ_CLS  = Messages.getString( "FMT_ERR_INV_RIVET_OBJ_CLS" );  //$NON-NLS-1$
  String FMT_ERR_OBJ_ALREADY_EXISTS = Messages.getString( "FMT_ERR_OBJ_ALREADY_EXISTS" ); //$NON-NLS-1$
  String FMT_ERR_CANT_CHANGE_SKID   = Messages.getString( "FMT_ERR_CANT_CHANGE_SKID" );   //$NON-NLS-1$
  String FMT_ERR_CANT_REMOVE_NO_OBJ = Messages.getString( "FMT_ERR_CANT_REMOVE_NO_OBJ" ); //$NON-NLS-1$

  /**
   * {@link SkCoreServRtdata}
   */
  String FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE         = Messages.getString( "FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE" );         //$NON-NLS-1$
  String FMT_ERR_CDW_CHANNEL_HAS_NO_KEY           = Messages.getString( "FMT_ERR_CDW_CHANNEL_HAS_NO_KEY" );           //$NON-NLS-1$
  String FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL = Messages.getString( "FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL" ); //$NON-NLS-1$

  /**
   * {@link SkCoreServSysdescr}
   */
  String FMT_WARN_ORPHAN_CLASS             = Messages.getString( "FMT_WARN_ORPHAN_CLASS" );             //$NON-NLS-1$
  String FMT_WARN_UNWANTED_CLASS_ID        = Messages.getString( "FMT_WARN_UNWANTED_CLASS_ID" );        //$NON-NLS-1$
  String FMT_WARN_EMPTY_CLASS_NAME         = Messages.getString( "FMT_WARN_EMPTY_CLASS_NAME" );         //$NON-NLS-1$
  String FMT_ERR_CLASS_HAS_PROP_ID         = Messages.getString( "FMT_ERR_CLASS_HAS_PROP_ID" );         //$NON-NLS-1$
  String FMT_ERR_CLASS_ALREADY_EXISTS      = Messages.getString( "FMT_ERR_CLASS_ALREADY_EXISTS" );      //$NON-NLS-1$
  String FMT_ERR_NO_PARENT_CLASS           = Messages.getString( "FMT_ERR_NO_PARENT_CLASS" );           //$NON-NLS-1$
  String FMT_ERR_DUP_PROP_IN_SUPER         = Messages.getString( "FMT_ERR_DUP_PROP_IN_SUPER" );         //$NON-NLS-1$
  String FMT_ERR_DUP_PROP_IN_SUB           = Messages.getString( "FMT_ERR_DUP_PROP_IN_SUB" );           //$NON-NLS-1$
  String MSG_ERR_CANT_CHANGE_ROOT_CLASS    = Messages.getString( "MSG_ERR_CANT_CHANGE_ROOT_CLASS" );    //$NON-NLS-1$
  String FMT_ERR_CANT_CHANGE_CLASS_ID      = Messages.getString( "FMT_ERR_CANT_CHANGE_CLASS_ID" );      //$NON-NLS-1$
  String FMT_ERR_CANT_CHANGE_PARENT        = Messages.getString( "FMT_ERR_CANT_CHANGE_PARENT" );        //$NON-NLS-1$
  String FMT_ERR_CANT_REMOVE_ABSENT_CLASS  = Messages.getString( "FMT_ERR_CANT_REMOVE_ABSENT_CLASS" );  //$NON-NLS-1$
  String FMT_ERR_CANT_REMOVE_CHILDED_CLASS = Messages.getString( "FMT_ERR_CANT_REMOVE_CHILDED_CLASS" ); //$NON-NLS-1$
  String MSG_ERR_CANT_REMOVE_ROOT_CLASS    = Messages.getString( "MSG_ERR_CANT_REMOVE_ROOT_CLASS" );    //$NON-NLS-1$
  String FMT_ERR_INV_CLASS_LOAD_IGNORED    = Messages.getString( "FMT_ERR_INV_CLASS_LOAD_IGNORED" );    //$NON-NLS-1$

  /**
   * {@link SkCoreServUsers}
   */
  String MSG_ERR_PSWD_IS_BLANK          = Messages.getString( "MSG_ERR_PSWD_IS_BLANK" );          //$NON-NLS-1$
  String FMT_ERR_NOT_USER_DPU           = Messages.getString( "FMT_ERR_NOT_USER_DPU" );           //$NON-NLS-1$
  String FMT_ERR_DUP_USER               = Messages.getString( "FMT_ERR_DUP_USER" );               //$NON-NLS-1$
  String MSG_ERR_NO_ROLES               = Messages.getString( "MSG_ERR_NO_ROLES" );               //$NON-NLS-1$
  String FMT_ERR_INV_ROLES              = Messages.getString( "FMT_ERR_INV_ROLES" );              //$NON-NLS-1$
  String FMT_ERR_NOT_ROLE_DPU           = Messages.getString( "FMT_ERR_NOT_ROLE_DPU" );           //$NON-NLS-1$
  String FMT_ERR_DUP_ROLE               = Messages.getString( "FMT_ERR_DUP_ROLE" );               //$NON-NLS-1$
  String MSG_ERR_CANT_DISABLE_ROOT_USER = Messages.getString( "MSG_ERR_CANT_DISABLE_ROOT_USER" ); //$NON-NLS-1$
  String MSG_WARN_DISABLING_GUEST_USER  = Messages.getString( "MSG_WARN_DISABLING_GUEST_USER" );  //$NON-NLS-1$
  String MSG_ERR_CANT_DISABLE_ROOT_ROLE = Messages.getString( "MSG_ERR_CANT_DISABLE_ROOT_ROLE" ); //$NON-NLS-1$
  String MSG_WARN_DISABLING_GUEST_ROLE  = Messages.getString( "MSG_WARN_DISABLING_GUEST_ROLE" );  //$NON-NLS-1$
  String FMT_WARN_CANT_DEL_NO_USER      = Messages.getString( "FMT_WARN_CANT_DEL_NO_USER" );      //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_ROOT_USER     = Messages.getString( "MSG_ERR_CANT_DEL_ROOT_USER" );     //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_GUEST_USER    = Messages.getString( "MSG_ERR_CANT_DEL_GUEST_USER" );    //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_CURRENT_USER  = Messages.getString( "MSG_ERR_CANT_DEL_CURRENT_USER" );  //$NON-NLS-1$
  String FMT_WARN_CANT_DEL_NO_ROLE      = Messages.getString( "FMT_WARN_CANT_DEL_NO_ROLE" );      //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_ROOT_ROLE     = Messages.getString( "MSG_ERR_CANT_DEL_ROOT_ROLE" );     //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_GUEST_ROLE    = Messages.getString( "MSG_ERR_CANT_DEL_GUEST_ROLE" );    //$NON-NLS-1$
  String MSG_ERR_CANT_DEL_CURRENT_ROLE  = Messages.getString( "MSG_ERR_CANT_DEL_CURRENT_ROLE" );  //$NON-NLS-1$

  /**
   * {@link SkCoreUtils}
   */
  String STR_ROOT_CLASS      = Messages.getString( "STR_ROOT_CLASS" );      //$NON-NLS-1$
  String STR_ROOT_CLASS_D    = Messages.getString( "STR_ROOT_CLASS_D" );    //$NON-NLS-1$
  String STR_ATTR_SKID       = Messages.getString( "STR_ATTR_SKID" );       //$NON-NLS-1$
  String STR_ATTR_SKID_D     = Messages.getString( "STR_ATTR_SKID_D" );     //$NON-NLS-1$
  String STR_ATTR_CLASS_ID   = Messages.getString( "STR_ATTR_CLASS_ID" );   //$NON-NLS-1$
  String STR_ATTR_CLASS_ID_D = Messages.getString( "STR_ATTR_CLASS_ID_D" ); //$NON-NLS-1$
  String STR_ATTR_STRID      = Messages.getString( "STR_ATTR_STRID" );      //$NON-NLS-1$
  String STR_ATTR_STRID_D    = Messages.getString( "STR_ATTR_STRID_D" );    //$NON-NLS-1$

  /**
   * {@link SkAsynchronousQuery}
   */
  String FMT_ERR_QUERY_INVALID_STATE = Messages.getString( "FMT_ERR_QUERY_INVALID_STATE" ); //$NON-NLS-1$
  String FMT_ERR_QUERY_TIMEOUT       = Messages.getString( "FMT_ERR_QUERY_TIMEOUT" );       //$NON-NLS-1$

  /**
   * {@link SkatletBase}
   */
  String FMT_INFO_SKATLET_INITIALIZE  = Messages.getString( "FMT_INFO_SKATLET_INITIALIZE" );  //$NON-NLS-1$
  String FMT_INFO_SKATLET_SET_CONTEXT = Messages.getString( "FMT_INFO_SKATLET_SET_CONTEXT" ); //$NON-NLS-1$
  String FMT_INFO_SKATLET_START       = Messages.getString( "FMT_INFO_SKATLET_START" );       //$NON-NLS-1$
  String FMT_INFO_SKATLET_QUERY_STOP  = Messages.getString( "FMT_INFO_SKATLET_QUERY_STOP" );  //$NON-NLS-1$
  String FMT_INFO_SKATLET_DESTROY     = Messages.getString( "FMT_INFO_SKATLET_DESTROY" );     //$NON-NLS-1$
  String FMT_INFO_SKATLET_DOJOB       = Messages.getString( "FMT_INFO_SKATLET_DOJOB" );       //$NON-NLS-1$
  String FMT_ERR_SKATLET_INITIALIZE   = Messages.getString( "FMT_ERR_SKATLET_INITIALIZE" );   //$NON-NLS-1$
  String FMT_WARN_ATTEMPT_OPEN_SHARE  = Messages.getString( "FMT_WARN_ATTEMPT_OPEN_SHARE" );  //$NON-NLS-1$
  String FMT_WARN_ATTEMPT_CLOSE_SHARE = Messages.getString( "FMT_WARN_ATTEMPT_OPEN_SHARE" );  //$NON-NLS-1$
}
