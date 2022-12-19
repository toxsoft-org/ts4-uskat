package org.toxsoft.uskat.s5.server.backend.supports.gwiddb;

/**
 * Локализуемые ресурсы реализации службы.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_GWIDDB = Messages.getString( "IS5Resources.STR_D_BACKEND_GWIDDB" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  // String MSG_READ_OBJ_BY_SKID_SQL_START = "loadBySkids: сформирован SQL-запрос:\n'%s'";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SERIALIZE_UNEXPECTED   = Messages.getString( "IS5Resources.ERR_SERIALIZE_UNEXPECTED" );   //$NON-NLS-1$
  String ERR_DESERIALIZE_UNEXPECTED = Messages.getString( "IS5Resources.ERR_DESERIALIZE_UNEXPECTED" ); //$NON-NLS-1$
  String ERR_ZIP_UNEXPECTED         = Messages.getString( "IS5Resources.ERR_ZIP_UNEXPECTED" );         //$NON-NLS-1$
  String ERR_UNZIP_UNEXPECTED       = Messages.getString( "IS5Resources.ERR_UNZIP_UNEXPECTED" );       //$NON-NLS-1$
  String ERR_LOB_NOT_FOUND          = Messages.getString( "IS5Resources.ERR_LOB_NOT_FOUND" );          //$NON-NLS-1$

  String ERR_READ_JDBC_UNEXPECTED = Messages.getString( "IS5Resources.ERR_READ_JDBC_UNEXPECTED" ); //$NON-NLS-1$
}
