package org.toxsoft.uskat.core.gui.conn;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;
import org.toxsoft.uskat.core.impl.*;

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
 * <li>connection supplier does <b>not</b> manages connection state. Opening or closing connections is out of scope of
 * this interface;</li>
 * <li>all connections are stored with associated key of type {@link IdChain};</li>
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
   * The ID of {@link #defConn()} is a key to get default connection from {@link #allConns()} map.
   */
  IdChain DEF_CONN_ID = IdChain.NULL;

  /**
   * Returns the default connection if defined.
   * <p>
   * This method always returns the same reference to the default connection. Default connection get also be retrieved
   * by the key {@link #DEF_CONN_ID} from the {@link #allConns()} map.
   *
   * @return {@link ISkConnection} - default connection, never is <code>null</code>
   */
  ISkConnection defConn();

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
   * Return all connections instances.
   * <p>
   * Returned map contains at least one entry with the key {@link IdChain#NULL}.
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
   * Returns list of the registered Sk-service creators.
   * <p>
   * Every connection in {@link #allConns()}, including default one {@link #defConn()} will have all registered services
   * added to the core API.
   *
   * @return {@link IList}&lt;{@link ISkServiceCreator}&gt; - registered creators list
   */
  IList<ISkServiceCreator<? extends AbstractSkService>> listRegisteredSkServiceCreators();

  /**
   * Registers Sk-service creator.
   * <p>
   * Attempt to register already registered creator is ignored.
   * <p>
   * The registered service will be added to the open connections also.
   *
   * @param aCreator {@link ISkServiceCreator} - the creator to register
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void registerSkServiceCreator( ISkServiceCreator<? extends AbstractSkService> aCreator );

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

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkConnectionSupplierValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkConnectionSupplierValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkConnectionSupplierListener}&gt; - the eventer
   */
  ITsEventer<ISkConnectionSupplierListener> eventer();

}
