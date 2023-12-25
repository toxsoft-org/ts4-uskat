package org.toxsoft.uskat.backend.sqlite;

import static org.toxsoft.uskat.backend.sqlite.ISkBackensSqliteConstants.*;
import static org.toxsoft.uskat.backend.sqlite.l10n.ISkBackendSqliteSharedResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link ISkBackendMetaInfo} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
class SkBackendSqliteMetaInfo
    extends SkBackendMetaInfo {

  /**
   * The instance singleton.
   */
  static final ISkBackendMetaInfo INSTANCE = new SkBackendSqliteMetaInfo();

  private SkBackendSqliteMetaInfo() {
    super( BACKEND_ID, STR_BACKEND_SQLITE, STR_BACKEND_SQLITE_D, ESkAuthentificationType.NONE );
    argOps().add( OPDEF_DB_FILE_NAME );
  }

  @Override
  protected ValidationResult doCheckArguments( ITsContextRo aArgs ) {
    // TODO error if file OPDEF_DB_FILE_NAME is not accessible for writing
    // TODO warn if file OPDEF_DB_FILE_NAME does not exists
    return ValidationResult.SUCCESS;
  }

}
