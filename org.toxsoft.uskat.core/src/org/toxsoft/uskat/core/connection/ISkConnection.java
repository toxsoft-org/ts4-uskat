package org.toxsoft.uskat.core.connection;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * A connection to the MicroSKAT server.
 *
 * @author hazard157
 */
public interface ISkConnection
    extends ICloseable {

  /**
   * Determines the current (at the moment of method call) connection state.
   *
   * @return {@link ESkConnState} - the connection state
   */
  ESkConnState state();

  /**
   * Opens the connection.
   * <p>
   * Available arguments depends on connection implementation.
   *
   * @param aArgs {@link ITsContextRo} - the connection arguments (options and references)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException connection is already open
   * @throws TsItemNotFoundRtException mandatory argument is missed
   */
  void open( ITsContextRo aArgs );

  /**
   * Closes the connection.
   * <p>
   * Has no effect on closed connection.
   * <p>
   * Never throws an exception1.
   */
  @Override
  void close();

  /**
   * Returns the API of the USkat core.
   * <p>
   * Every time connection opens reference to the new instance of {@link ISkCoreApi} is returned.
   *
   * @return {@link ISkCoreApi} - the core API
   * @throws TsIllegalStateRtException connection is closed
   */
  ISkCoreApi coreApi();

  /**
   * Returns the information about the backend.
   *
   * @return {@link ISkBackendInfo} - information about the backend
   * @throws TsIllegalStateRtException connection is closed
   */
  ISkBackendInfo backendInfo();

  /**
   * Adds the connection state change listener.
   *
   * @param aListener {@link ISkConnectionListener} - the listener
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void addConnectionListener( ISkConnectionListener aListener );

  /**
   * Removes the connection state change listener.
   *
   * @param aListener {@link ISkConnectionListener} - the listener
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeConnectionListener( ISkConnectionListener aListener );

  /**
   * Returns the scope of the connection - arbitrary references and options bound to this connection.
   * <p>
   * User may bind anything with connection, USkat does not uses content of this context.
   *
   * @return {@link ITsContext} - editable set of custom references and options
   */
  ITsContext scope();

}
