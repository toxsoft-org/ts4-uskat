package org.toxsoft.uskat.backend.sqlite;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * {@link ISkBackendProvider} provider implementation to create {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class SkBackendSqliteProvider
    implements ISkBackendProvider {

  /**
   * Constructor.
   */
  public SkBackendSqliteProvider() {
    // nop
  }

  @Override
  public ISkBackendMetaInfo getMetaInfo() {
    return SkBackendSqliteMetaInfo.INSTANCE;
  }

  @Override
  public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    return new SkBackendSqlite( aFrontend, aArgs );
  }

}
