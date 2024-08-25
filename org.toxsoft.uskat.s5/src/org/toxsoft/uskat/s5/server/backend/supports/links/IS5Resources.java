package org.toxsoft.uskat.s5.server.backend.supports.links;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_LINKS = Messages.getString( "IS5Resources.STR_D_BACKEND_LINKS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_FWD_LINKS_BY_CLASSID_SQL_FINISH          =
      Messages.getString( "IS5Resources.MSG_READ_FWD_LINKS_BY_CLASSID_SQL_FINISH" );                               //$NON-NLS-1$
  String MSG_READ_REV_LINKS_BY_CLASSID_SQL_FINISH          =
      Messages.getString( "IS5Resources.MSG_READ_REV_LINKS_BY_CLASSID_SQL_FINISH" );                               //$NON-NLS-1$
  String MSG_DELETE_LINKS_BY_CLASSID_SQL_FINISH            =
      Messages.getString( "IS5Resources.MSG_DELETE_LINKS_BY_CLASSID_SQL_FINISH" );                                 //$NON-NLS-1$
  String MSG_DELETE_LINKS_BY_CLASSID_AND_LINKID_SQL_FINISH =
      Messages.getString( "IS5Resources.MSG_DELETE_LINKS_BY_CLASSID_AND_LINKID_SQL_FINISH" );                      //$NON-NLS-1$
  String MSG_WRITE_LINKS                                   = Messages.getString( "IS5Resources.MSG_WRITE_LINKS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND1 = Messages.getString( "IS5Resources.ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND1" ); //$NON-NLS-1$
  String ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND2 = Messages.getString( "IS5Resources.ERR_LINK_FWD_CONSTRUCTOR_NOT_FOUND2" ); //$NON-NLS-1$
  String ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND1 = Messages.getString( "IS5Resources.ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND1" ); //$NON-NLS-1$
  String ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND2 = Messages.getString( "IS5Resources.ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND2" ); //$NON-NLS-1$
  String ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND3 = Messages.getString( "IS5Resources.ERR_LINK_REV_CONSTRUCTOR_NOT_FOUND3" ); //$NON-NLS-1$
  String ERR_READ_JDBC_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_READ_JDBC_UNEXPECTED" );            //$NON-NLS-1$
  String ERR_LINK_IMPL_NOT_FOUND             = Messages.getString( "IS5Resources.ERR_LINK_IMPL_NOT_FOUND" );             //$NON-NLS-1$
  String ERR_SQL_EXECUTE_UNEXPECTED          = Messages.getString( "IS5Resources.ERR_SQL_EXECUTE_UNEXPECTED" );          //$NON-NLS-1$
  String ERR_READ_UNEXPECTED                 = Messages.getString( "IS5Resources.ERR_READ_UNEXPECTED" );                 //$NON-NLS-1$
  String ERR_CLASS_DONT_HAVE_LINK            = Messages.getString( "IS5Resources.ERR_CLASS_DONT_HAVE_LINK" );            //$NON-NLS-1$
  String ERR_OBJECT_DONT_HAVE_LINK           = Messages.getString( "IS5Resources.ERR_OBJECT_DONT_HAVE_LINK" );           //$NON-NLS-1$
  String ERR_OBJECT_NOT_FOUND                = Messages.getString( "IS5Resources.ERR_OBJECT_NOT_FOUND" );                //$NON-NLS-1$

  String ERR_CREATE_LINK_UNEXPECTED = Messages.getString( "IS5Resources.ERR_CREATE_LINK_UNEXPECTED" ); //$NON-NLS-1$
  String ERR_WRITE_EXIST_LINK       = Messages.getString( "IS5Resources.ERR_WRITE_EXIST_LINK" );       //$NON-NLS-1$
  String ERR_WRITE_NEW_LINK         = Messages.getString( "IS5Resources.ERR_WRITE_NEW_LINK" );         //$NON-NLS-1$
  String ERR_WRITE_REMOVE_LINK      = Messages.getString( "IS5Resources.ERR_WRITE_REMOVE_LINK" );      //$NON-NLS-1$
  String ERR_NOT_FOUND_LINK_OBJ     = Messages.getString( "IS5Resources.ERR_NOT_FOUND_LINK_OBJ" );     //$NON-NLS-1$

  String ERR_ATTEMPT_REMOVE_LINKS_BY_EDIT_CLASS =
      "attempt to remove references between objects by editing the class %s. link = %s, rightObjs = %s: "; //$NON-NLS-1$
}
