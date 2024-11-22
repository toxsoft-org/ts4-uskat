package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_SYSDESCR = Messages.getString( "IS5Resources.STR_D_BACKEND_SYSDESCR" ); //$NON-NLS-1$
  String STR_N_GW_ROOT_CLASS    = Messages.getString( "IS5Resources.STR_N_GW_ROOT_CLASS" );    //$NON-NLS-1$
  String STR_D_GW_ROOT_CLASS    = Messages.getString( "IS5Resources.STR_D_GW_ROOT_CLASS" );    //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CREATE_ROOT_CLASS_START     = Messages.getString( "IS5Resources.MSG_CREATE_ROOT_CLASS_START" );     //$NON-NLS-1$
  String MSG_CREATE_ROOT_CLASS_FINISH    = Messages.getString( "IS5Resources.MSG_CREATE_ROOT_CLASS_FINISH" );    //$NON-NLS-1$
  String MSG_CREATE_DATE_TYPE_START      = Messages.getString( "IS5Resources.MSG_CREATE_DATE_TYPE_START" );      //$NON-NLS-1$
  String MSG_CREATE_DATE_TYPE_FINISH     = Messages.getString( "IS5Resources.MSG_CREATE_DATE_TYPE_FINISH" );     //$NON-NLS-1$
  String MSG_CREATE_USER_CLASS_START     = Messages.getString( "IS5Resources.MSG_CREATE_USER_CLASS_START" );     //$NON-NLS-1$
  String MSG_CREATE_USER_CLASS_FINISH    = Messages.getString( "IS5Resources.MSG_CREATE_USER_CLASS_FINISH" );    //$NON-NLS-1$
  String MSG_CREATE_SESSION_CLASS_START  = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CLASS_START" );  //$NON-NLS-1$
  String MSG_CREATE_SESSION_CLASS_FINISH = Messages.getString( "IS5Resources.MSG_CREATE_SESSION_CLASS_FINISH" ); //$NON-NLS-1$
  String MSG_CREATED_CLASS               = "created class %s";                                                   //$NON-NLS-1$
  String MSG_UPDATED_CLASS               = "updated class %s";                                                   //$NON-NLS-1$
  String MSG_REMOVED_CLASS               = "removed class %s";                                                   //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_HAS_TYPE_DEPENDENT_CLASSES = Messages.getString( "IS5Resources.MSG_ERR_HAS_TYPE_DEPENDENT_CLASSES" ); //$NON-NLS-1$
  String MSG_ERR_HAS_DESCENDANTS            = Messages.getString( "IS5Resources.MSG_ERR_HAS_DESCENDANTS" );            //$NON-NLS-1$
  String MSG_ERR_CLASS_NOT_FOUND            = Messages.getString( "IS5Resources.MSG_ERR_CLASS_NOT_FOUND" );            //$NON-NLS-1$
  String MSG_ERR_PARENT_NOT_FOUND           = Messages.getString( "IS5Resources.MSG_ERR_PARENT_NOT_FOUND" );           //$NON-NLS-1$
  String MSG_ERR_DELETE_HAS_LINKED_CLS      = Messages.getString( "IS5Resources.MSG_ERR_DELETE_HAS_LINKED_CLS" );      //$NON-NLS-1$
  String MSG_ERR_DELETE_HAS_LINKED_OJBS     = Messages.getString( "IS5Resources.MSG_ERR_DELETE_HAS_LINKED_OJBS" );     //$NON-NLS-1$
  String MSG_ERR_DELETE_ROOT                = Messages.getString( "IS5Resources.MSG_ERR_DELETE_ROOT" );                //$NON-NLS-1$
  String MSG_ERR_USER_CONSTRAINTS           = Messages.getString( "IS5Resources.MSG_ERR_USER_CONSTRAINTS" );           //$NON-NLS-1$
  String MSG_ERR_ATTR_ALREADY_EXIST         = Messages.getString( "IS5Resources.MSG_ERR_ATTR_ALREADY_EXIST" );         //$NON-NLS-1$
  String MSG_ERR_LINK_ALREADY_EXIST         = Messages.getString( "IS5Resources.MSG_ERR_LINK_ALREADY_EXIST" );         //$NON-NLS-1$
  String MSG_ERR_DATA_ALREADY_EXIST         = Messages.getString( "IS5Resources.MSG_ERR_DATA_ALREADY_EXIST" );         //$NON-NLS-1$
  String MSG_ERR_CMD_ALREADY_EXIST          = Messages.getString( "IS5Resources.MSG_ERR_CMD_ALREADY_EXIST" );          //$NON-NLS-1$
  String MSG_ERR_EVENT_ALREADY_EXIST        = Messages.getString( "IS5Resources.MSG_ERR_EVENT_ALREADY_EXIST" );        //$NON-NLS-1$
  String MSG_ERR_RIVET_ALREADY_EXIST        = Messages.getString( "IS5Resources.MSG_ERR_RIVET_ALREADY_EXIST" );        //$NON-NLS-1$
  String MSG_ERR_CLOB_ALREADY_EXIST         = Messages.getString( "IS5Resources.MSG_ERR_CLOB_ALREADY_EXIST" );         //$NON-NLS-1$

  String ERR_MSG_TYPE_NOT_FOUND  = Messages.getString( "IS5Resources.ERR_MSG_TYPE_NOT_FOUND" );  //$NON-NLS-1$
  String ERR_MSG_CLASS_NOT_FOUND = Messages.getString( "IS5Resources.ERR_MSG_CLASS_NOT_FOUND" ); //$NON-NLS-1$
}
