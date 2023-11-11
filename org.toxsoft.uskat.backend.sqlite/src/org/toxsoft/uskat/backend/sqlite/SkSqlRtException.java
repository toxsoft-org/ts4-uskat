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
   * <p>
   * Message string is created using {@link String#format(String, Object...)}.
   *
   * @param aCause Throwable - cause, may be <code>null</code>
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   */
  public SkSqlRtException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }

  /**
   * Constructor.
   * <p>
   * Message string is created using {@link String#format(String, Object...)}.
   *
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   */
  public SkSqlRtException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
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
