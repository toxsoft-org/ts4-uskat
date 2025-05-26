package org.toxsoft.uskat.ws.conn.mws.main;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.cfg.*;

/**
 * Helper class to implement different task of server connections.
 * <p>
 * Usage: simply create an instance of {@link HandlerHelper}, call all necessary methods and forget about it - instance
 * does not allocates any resources.
 *
 * @author hazard157
 */
public interface IHandlerHelper {

  /**
   * returns the connection this handler is operating on.
   *
   * @return {@link ISkConnection} - managed connection, never is <code>null</code>
   */
  ISkConnection skConn();

  /**
   * Returns configuration of the last successful connection.
   * <p>
   * If value can not be returned for any reason, displays a warning dialog and returns <code>null</code>.
   *
   * @return {@link IConnectionConfig} - last connection configuration or <code>null</code>
   */
  IConnectionConfig findLastConfig();

  /**
   * Selects configuration of the {@link IConnectionConfigService} found in the context.
   * <p>
   * Method assumes that selection is done for immediate connection to the server so appropriate message is displayed to
   * the user.
   *
   * @param aInitalCfgId String - initially selected configuration ID or <code>null</code>
   * @return {@link IConnectionConfig} - selected configuration or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IConnectionConfig selectCfgToConnect( String aInitalCfgId );

  /**
   * Edits configuration of the {@link IConnectionConfigService} found in the context.
   */
  void editCfgs();

  /**
   * Prepares connection arguments for the specified configuration.
   * <p>
   * Displays all necessary dialogs like error/warning messages and asks login information (login/password).
   * <p>
   * If for any reason (like bad configuration or user cancel) the arguments can not be prepared, returns
   * <code>null</code>.
   *
   * @param aCfg {@link IConnectionConfig} - the configuration of the server to connect to
   * @return {@link ITsContext} - arguments for {@link ISkConnection#open(ITsContextRo)} or <code>null</code>
   */
  ITsContext prepareConnArgs( IConnectionConfig aCfg );

  /**
   * Opens the connection with progress dialog.
   * <p>
   * On error displays dialog and returns <code>false</code>.
   *
   * @param aConnArgs {@link ITsContext} - the connection arguments
   * @return boolean - <code>true</code> if connection was opened
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException connection is already open
   */
  boolean openConnection( ITsContext aConnArgs );

  /**
   * Displays dialog with information about server or warning if no connection.
   *
   * @throws TsIllegalStateRtException connection is closed
   */
  void showServerInfo();

  /**
   * Closes current connection.
   * <p>
   * Displays all necessary dialogs like warning if connection is not opened.
   */
  void closeCOnnection();

}
