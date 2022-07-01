package org.toxsoft.uskat.s5.server.backend.supports.clobs;

/**
 * Локализуемые ресурсы реализации службы.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_LOBS = Messages.getString( "IS5Resources.STR_D_BACKEND_LOBS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  // String MSG_READ_OBJ_BY_SKID_SQL_START = "loadBySkids: сформирован SQL-запрос:\n'%s'";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_SERIALIZE_UNEXPECTED   = Messages.getString( "IS5Resources.MSG_ERR_SERIALIZE_UNEXPECTED" );   //$NON-NLS-1$
  String MSG_ERR_DESERIALIZE_UNEXPECTED = Messages.getString( "IS5Resources.MSG_ERR_DESERIALIZE_UNEXPECTED" ); //$NON-NLS-1$
  String MSG_ERR_ZIP_UNEXPECTED         = Messages.getString( "IS5Resources.MSG_ERR_ZIP_UNEXPECTED" );         //$NON-NLS-1$
  String MSG_ERR_UNZIP_UNEXPECTED       = Messages.getString( "IS5Resources.MSG_ERR_UNZIP_UNEXPECTED" );       //$NON-NLS-1$
  String MSG_ERR_LOB_NOT_FOUND          = Messages.getString( "IS5Resources.MSG_ERR_LOB_NOT_FOUND" );          //$NON-NLS-1$
}
