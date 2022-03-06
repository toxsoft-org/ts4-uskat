package org.toxsoft.uskat.s5.server.backend.supports.objects;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_OBJECTS = Messages.getString( "IS5Resources.STR_D_BACKEND_OBJECTS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_OBJ_BY_SKID_SQL_START  = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_SKID_SQL_START" );  //$NON-NLS-1$
  String MSG_READ_OBJ_BY_SKID_SQL_FINISH = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_SKID_SQL_FINISH" ); //$NON-NLS-1$

  String MSG_READ_OBJ_BY_CLASSID_SQL_START  = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_CLASSID_SQL_START" );  //$NON-NLS-1$
  String MSG_READ_OBJ_BY_CLASSID_SQL_FINISH = Messages.getString( "IS5Resources.MSG_READ_OBJ_BY_CLASSID_SQL_FINISH" ); //$NON-NLS-1$
  String MSG_WRITE_OBJECTES                 = Messages.getString( "IS5Resources.MSG_WRITE_OBJECTES" );                 //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_OBJECT_IMPL_NOT_FOUND         = Messages.getString( "IS5Resources.ERR_OBJECT_IMPL_NOT_FOUND" );         //$NON-NLS-1$
  String ERR_OBJECT_CONSTRUCTOR_NOT_FOUND1 = Messages.getString( "IS5Resources.ERR_OBJECT_CONSTRUCTOR_NOT_FOUND1" ); //$NON-NLS-1$
  String ERR_OBJECT_CONSTRUCTOR_NOT_FOUND2 = Messages.getString( "IS5Resources.ERR_OBJECT_CONSTRUCTOR_NOT_FOUND2" ); //$NON-NLS-1$
  String ERR_SQL_EXECUTE_UNEXPECTED        = Messages.getString( "IS5Resources.ERR_SQL_EXECUTE_UNEXPECTED" );        //$NON-NLS-1$
  String ERR_READ_JDBC_UNEXPECTED          = Messages.getString( "IS5Resources.ERR_READ_JDBC_UNEXPECTED" );          //$NON-NLS-1$
  String ERR_READ_UNEXPECTED               = Messages.getString( "IS5Resources.ERR_READ_UNEXPECTED" );               //$NON-NLS-1$

  String ERR_CREATE_OBJ_UNEXPECTED = Messages.getString( "IS5Resources.ERR_CREATE_OBJ_UNEXPECTED" ); //$NON-NLS-1$

  String ERR_NO_DEFAULT_VALUE            = Messages.getString( "IS5Resources.ERR_NO_DEFAULT_VALUE" );            //$NON-NLS-1$
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_CLASS_WITH_OBJS" ); //$NON-NLS-1$
  String ERR_ATTR_NOT_HAVE_DEFAULT_VALUE = Messages.getString( "IS5Resources.ERR_ATTR_NOT_HAVE_DEFAULT_VALUE" ); //$NON-NLS-1$
  String ERR_CANT_CHANGE_IMPL_AND_ATTRS  = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_IMPL_AND_ATTRS" );  //$NON-NLS-1$
  String ERR_CHANGE_OBJECT_IMPL          = Messages.getString( "IS5Resources.ERR_CHANGE_OBJECT_IMPL" );          //$NON-NLS-1$
}
