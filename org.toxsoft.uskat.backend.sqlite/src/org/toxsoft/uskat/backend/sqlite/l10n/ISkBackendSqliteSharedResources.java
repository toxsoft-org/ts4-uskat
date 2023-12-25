package org.toxsoft.uskat.backend.sqlite.l10n;

import org.toxsoft.uskat.backend.sqlite.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkBackendSqliteSharedResources {

  /**
   * {@link ISkBackensSqliteConstants}
   */
  String STR_OP_DB_FILE_NAME   = Messages.getString( "STR_OP_DB_FILE_NAME" );   //$NON-NLS-1$
  String STR_OP_DB_FILE_NAME_D = Messages.getString( "STR_OP_DB_FILE_NAME_D" ); //$NON-NLS-1$

  /**
   * SkBackendSqliteMetaInfo
   */
  String STR_BACKEND_SQLITE   = Messages.getString( "STR_BACKEND_SQLITE" );   //$NON-NLS-1$
  String STR_BACKEND_SQLITE_D = Messages.getString( "STR_BACKEND_SQLITE_D" ); //$NON-NLS-1$

  /**
   * {@link SkSqlRtException}
   */
  String FMT_ERR_SQL_EXCEPTION     = Messages.getString( "FMT_ERR_SQL_EXCEPTION" );     //$NON-NLS-1$
  String MSG_ERR_GENERAL_SQL_ERROR = Messages.getString( "MSG_ERR_GENERAL_SQL_ERROR" ); //$NON-NLS-1$

}
