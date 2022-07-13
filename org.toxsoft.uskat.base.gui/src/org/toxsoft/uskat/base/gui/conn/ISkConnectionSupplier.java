package org.toxsoft.uskat.base.gui.conn;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Manages {@link ISkConnection} instances in the MWS application.
 * <p>
 * Reference to this interface is placed in application level context.
 *
 * @author hazard157
 */
public interface ISkConnectionSupplier {

  /**
   * Returns the default connection if defined.
   *
   * @return {@link ISkConnection} - default connection or <code>null</code>
   */
  ISkConnection defConn();

  /**
   * Set the connection as {@link #defConn()}.
   * <p>
   * {@link IdChain#NULL} argument resets {@link #defConn()} to <code>null</code>.
   *
   * @param aKey {@link IdChain} - the key or <code>null</code> to reset
   * @return {@link ISkConnection} - connection that became the default one or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no connection with the specified key
   */
  ISkConnection setDefaultConnection( IdChain aKey );

  /**
   * Returns the key of the default connection.
   *
   * @return {@link IdChain} - the key of the default connection or {@link IdChain#NULL}
   */
  IdChain getDefaultConnectionKey();

  /**
   * Creates the connection instance with the specified key.
   *
   * @param aKey {@link IdChain} - connection key
   * @return {@link ISkConnection} - new instance of connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is {@link IdChain#NULL}
   * @throws TsItemAlreadyExistsRtException connection with this key already exists
   */
  ISkConnection createConnection( IdChain aKey );

  /**
   * Return sll connections instances.
   *
   * @return {@link IMap}&lt;{@link IdChain},{@link ISkConnection}&lt; - map "connection key" - "connection instance"
   */
  IMap<IdChain, ISkConnection> allConns();

  /**
   * Removes closed connection if it exists.
   * <p>
   * Removing default connection resets {@link #defConn()} to <code>null</code>.
   *
   * @param aKey {@link IdChain} - the key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException connection is open
   */
  void removeConnection( IdChain aKey );

  /**
   * Returns connection by ID.
   *
   * @param aId {@link IdChain} - the connection ID
   * @return {@link ISkConnection} - found connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such connection
   */
  default ISkConnection getConn( IdChain aId ) {
    return allConns().getByKey( aId );
  }

}
