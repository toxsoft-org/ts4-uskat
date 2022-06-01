package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * Superclass for all <b>core</b> service implementations.
 * <p>
 * This class shows backend as {@link #ba()} and checks for correct {@link #serviceId()}.
 * <p>
 * This class is <code>not</code> public class. Therefore core services may be implemented only in this package.
 *
 * @author hazard157
 */
abstract class AbstractSkCoreService
    extends AbstractSkService {

  private final ISkBackend backend;

  protected AbstractSkCoreService( String aId, IDevCoreApi aCoreApi ) {
    super( aId, aCoreApi );
    TsIllegalArgumentRtException.checkFalse( aId.startsWith( SK_CORE_SERVICE_ID_PREFIX ) );
    backend = coreApi().backend();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public ISkBackend ba() {
    return backend;
  }

}
