package org.toxsoft.uskat.core.api;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Allows client code to perform actions during {@link ISkCoreApi} lifecycle.
 *
 * @author hazard157
 */
public interface ISkCoreExternalHandler {

  /**
   * Implementation must perform actions immediately after Core API is initialized.
   * <p>
   * Method is called from {@link ISkConnection#open(ITsContextRo)} before method returns.
   *
   * @param aCoreApi {@link ISkCoreApi} - the processed Core API
   */
  void processSkCoreInitialization( ISkCoreApi aCoreApi );

  /**
   * Implementation may optionally process when remote Sk-backend activity state changes.
   * <p>
   * One called immediately after {@link #processSkCoreInitialization(ISkCoreApi)} from
   * {@link ISkConnection#open(ITsContextRo)} before method returns. Also called every time when backend activity state
   * actually changes.
   * <p>
   * Note: some serverless (non-remote) backends never calls this method because they are always active.
   *
   * @param aCoreApi {@link ISkCoreApi} - the processed Core API
   * @param aActive boolean - backend activity state
   */
  default void processSkBackendActiveStateChange( ISkCoreApi aCoreApi, boolean aActive ) {
    // nop
  }

  /**
   * Implementation may optionally perform actions before Core API normal shutdown.
   * <p>
   * Called from {@link ISkConnection#close()} before internal shutdown process starts.
   *
   * @param aCoreApi {@link ISkCoreApi} - the processed Core API
   */
  default void processSkCoreShutdown( ISkCoreApi aCoreApi ) {
    // nop
  }

}
