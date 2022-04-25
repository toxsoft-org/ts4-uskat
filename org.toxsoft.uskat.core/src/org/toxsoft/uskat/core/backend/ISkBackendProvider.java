package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * The provider (in fact, the builder) of the {@link ISkBackend} instance.
 *
 * @author hazard157
 */
public interface ISkBackendProvider {

  /**
   * Cretaes, initializes and returns the instance of the backend.
   * <p>
   * <code>aArgs</code> is the same argument that is passed to the {@link ISkConnection#open(ITsContextRo)}.
   * <p>
   * Provider may throw additional exceptions if backend initialization fails.
   *
   * @param aFrontend {@link ISkFrontendRear} - the frontend
   * @param aArgs {@link ITsContextRo} - creation arguments
   * @return {@link ISkBackend} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkBackend createBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs );

}
