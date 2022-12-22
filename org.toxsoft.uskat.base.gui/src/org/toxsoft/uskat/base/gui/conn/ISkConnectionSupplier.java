package org.toxsoft.uskat.base.gui.conn;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Manages {@link ISkConnection} instances in the MWS application.
 * <p>
 * Connection supplier is the single point to create {@link ISkConnection} instances. Reference to the
 * {@link ISkConnectionSupplier} implementation is placed in windows level context by this library quant. So using this
 * library assumes that no one creates {@link ISkConnection} instances directly - only by using {@link #defConn()} or
 * {@link #createConnection(IdChain, ITsGuiContext)}.
 * <p>
 * Connection supplier is based on the following concepts:
 * <ul>
 * <li>there is at least on Sk-connection instance in the application. Inititlly one connection instance is created,put
 * in the map {@link #allConns()} under key {@link IdChain#NULL} and declared as default connection
 * {@link #defConn()};</li>
 * <li>default connection always exists, so {@link #defConn()} always returns with non-<code>null</code> value while
 * {@link #getConn(IdChain)} may throw an item not found exception;</li>
 * <li>user may create additional connections only by {@link #createConnection(IdChain, ITsGuiContext)} method;</li>
 * <li>coneection supplier does <b>not</b> manages connection state. Opening or closing connections is out of scope of
 * this interface;</li>
 * <li>all connections are stored with accosiated key of type {@link IdChain};</li>
 * <li>zzz.</li>
 * </ul>
 * <p>
 * Note: managed connection have {@link KM5Support} enabled so all of them have {@link IM5Domain} in
 * {@link ISkConnection#scope()}.
 *
 * @author hazard157
 */
public interface ISkConnectionSupplier
    extends ICloseable {

  /**
   * Returns the default connection if defined.
   * <p>
   * Inititlly default connection is created and has the key {@link #getDefaultConnectionKey()} = {@link IdChain#NULL}.
   *
   * @return {@link ISkConnection} - default connection, never is <code>null</code>
   */
  ISkConnection defConn();

  /**
   * Returns the key of the default connection.
   * <p>
   * Inintially default connection key is {@link IdChain#NULL}.
   *
   * @return {@link IdChain} - the key of the default connection or {@link IdChain#NULL}
   */
  IdChain getDefaultConnectionKey();

  /**
   * Chooses the connection to be used as default one - {@link #defConn()}.
   * <p>
   * {@link IdChain#NULL} argument resets {@link #defConn()} to the automatically created instance.
   *
   * @param aKey {@link IdChain} - the key or {@link IdChain#NULL} to reset
   * @return {@link ISkConnection} - connection that became the default one or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no connection with the specified key
   */
  ISkConnection setDefaultConnection( IdChain aKey );

  /**
   * Creates the connection instance with the specified key.
   *
   * @param aKey {@link IdChain} - connection key
   * @param aContext {@link ITsGuiContext} - the context for connection and it's M5-domain
   * @return {@link ISkConnection} - new instance of connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException connection with this key already exists
   */
  ISkConnection createConnection( IdChain aKey, ITsGuiContext aContext );

  /**
   * Return sll connections instances.
   * <p>
   * Returned map cntains at least one entry with the key {@link IdChain#NULL}.
   *
   * @return {@link IMap}&lt;{@link IdChain},{@link ISkConnection}&lt; - map "connection key" - "connection instance"
   */
  IMap<IdChain, ISkConnection> allConns();

  /**
   * Removes closed connection if it exists from {@link #allConns()} map.
   * <p>
   * Removing default connection resets {@link #defConn()} to the connection with the key {@link IdChain#NULL}.
   * Obviously entry with the key {@link IdChain#NULL} can not be removed from the map {@link #allConns()}.
   *
   * @param aKey {@link IdChain} - the key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException can not remove connection with key {@link IdChain#NULL}
   * @throws TsIllegalStateRtException connection is open
   */
  void removeConnection( IdChain aKey );

  /**
   * Closes all open connections but does not removes them from {@link #allConns()}.
   */
  @Override
  void close();

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

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkConnectionSupplierListener}&gt; - the eventer
   */
  ITsEventer<ISkConnectionSupplierListener> eventer();

}
