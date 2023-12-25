package org.toxsoft.uskat.backend.sqlite;

import static org.toxsoft.uskat.backend.sqlite.l10n.ISkBackendSqliteSharedResources.*;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * SQL execution error.
 *
 * @author hazard157
 */
public class SkSqlRtException
    extends TsRuntimeException {

  private static final long serialVersionUID = -788795883701927395L;

  /**
   * Constructor for wrapper exception.
   *
   * @param aCause Throwable - cause, may be <code>null</code>
   * @param aSql String - SQL query causing exception
   */
  public SkSqlRtException( Throwable aCause, String aSql ) {
    super( aCause, aSql );
  }

  /**
   * Constructor.
   * <p>
   *
   * @param aSql String - SQL query causing exception
   */
  public SkSqlRtException( String aSql ) {
    super( aSql );
  }

  /**
   * Constructor for wrapper exception with preset message.
   *
   * @param aCause Throwable - cause, may be <code>null</code>
   */
  public SkSqlRtException( Throwable aCause ) {
    super( aCause, FMT_ERR_SQL_EXCEPTION, aCause.getLocalizedMessage() );
  }

  /**
   * Constructor with preset message.
   */
  public SkSqlRtException() {
    super( MSG_ERR_GENERAL_SQL_ERROR );
  }

}
