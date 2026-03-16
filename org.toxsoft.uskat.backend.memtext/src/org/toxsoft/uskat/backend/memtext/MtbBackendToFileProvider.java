package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;

/**
 * @author hazard157
 */
public class MtbBackendToFileProvider
    implements ISkBackendProvider {

  /**
   * Constructor.
   */
  public MtbBackendToFileProvider() {
    // nop
  }

  @Override
  public ISkBackendMetaInfo getMetaInfo() {
    return MtbBackendToFileMetaInfo.INSTANCE;
  }

  @Override
  public ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    return new MtbBackendToFile( aFrontend, aArgs );
  }

}
