package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat backend API.
 * <p>
 * Notes on implementation:
 * <ul>
 * <li>All modification methods may throw additional exceptions. Backed API does not require backend to enforce any
 * validation when writing data to it. However particular backend may check data to be written and not allow to violate
 * the storage integrity;</li>
 * <li>Communication may be initiated both by the frontend or the backend. The frontend simply calls methods of the
 * backend. The only way to communicate to frontend at the backends initiative is to send message via
 * {@link ISkFrontendRear#onBackendMessage(GtMessage)}.</li>
 * <li>Execution threads. Some backend implementations may have internal execution threads and may want to send message
 * to the frontend from the internal thread. Such backends inform their counterpart with the
 * {@link ISkBackendHardConstant#OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND} option set to <code>true</code> in
 * {@link ISkBackendInfo#params()}. Because core API implementation (class {@link SkCoreApi} and it's reap part
 * {@link ISkFrontendRear}) is single-threaded by default, the threads must be separated by supplying ensuring that
 * {@link ISkFrontendRear} method calls are thread-safe.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkBackend
    extends ICloseable, IInitializable {

  /**
   * Determines if backend is active.
   * <p>
   * Existence of backend instance (until it is closed) means that connection to USkat is open. However open connection
   * may be in inactive state, for example due to network failure. There is no API to change backend activity state -
   * backend tries to stay in active state or restore after temporary failures.
   *
   * @return boolean - backend activity state
   */
  boolean isActive();

  /**
   * Called once immediately after constructor.
   * <p>
   * This is second step of the two-step initialization of a backend (constructor and then {@link #initialize()}).
   */
  @Override
  void initialize();

  /**
   * Called once when backend finishes working.
   */
  @Override
  void close();

  /**
   * Returns information about backend instance.
   *
   * @return {@link ISkBackendInfo} - the backend info
   */
  ISkBackendInfo getBackendInfo();

  /**
   * Returns the owner frontend.
   *
   * @return {@link ISkFrontendRear} - the frontend
   */
  ISkFrontendRear frontend();

  /**
   * Returns the connection opening arguments used to create backend.
   *
   * @return {@link ITsContextRo} - connection opening arguments
   */
  ITsContextRo openArgs();

  // ------------------------------------------------------------------------------------
  // Mandatory addons
  //

  /**
   * Returns backend addon for classes storage.
   *
   * @return {@link IBaClasses} - classes storage
   */
  IBaClasses baClasses();

  /**
   * Returns backend addon for objects storage.
   *
   * @return {@link IBaObjects} - objects storage
   */
  IBaObjects baObjects();

  /**
   * Returns backend addon for links storage.
   *
   * @return {@link IBaLinks} - links storage
   */
  IBaLinks baLinks();

  /**
   * Returns backend addon for events messaging.
   *
   * @return {@link IBaEvents} - events messaging
   */
  IBaEvents baEvents();

  /**
   * Returns backend addon for CLOBs storage.
   *
   * @return {@link IBaClobs} - CLOBs storage
   */
  IBaClobs baClobs();

  /**
   * Returns backend addon to work with real-time data.
   *
   * @return {@link IBaRtdata} - real-time data
   */
  IBaRtdata baRtdata();

  /**
   * Returns backend addon to work with the commands.
   *
   * @return {@link IBaCommands} - commands
   */
  IBaCommands baCommands();

  /**
   * Returns backend addon for history queries.
   *
   * @return {@link IBaQueries} - history queries
   */
  IBaQueries baQueries();

  /**
   * Returns backend addon for GWID-String key-value database.
   *
   * @return {@link IBaGwidDb} - history queries
   */
  IBaGwidDb baGwidDb();

  // ------------------------------------------------------------------------------------
  // Optional addons
  //

  /**
   * Returns the list to create USkat services provided by the backend.
   *
   * @return {@link IList}&lt;{@link ISkServiceCreator}&gt; - services creators list
   */
  IList<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators();

  /**
   * Finds backend addon.
   *
   * @param <T> - expected interface/class of the addon
   * @param aAddonId String - the ID of the addon
   * @param aExpectedType {@link Class}&lt;T&gt; - expected interface of the addons
   * @return &lt;T&gt; - found addon or <code>null</code> if no such optional addon exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException addon was found but not of expected type
   */
  <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType );

  // ------------------------------------------------------------------------------------
  // Messages
  //

  /**
   * Instructs the backend to send the specified message to all CoreAPI instances.
   * <p>
   * Main purpose of this method is to allow Sk-service implementations to send messages to other instances of the same
   * service. For example, the refbooks service may inform all other instances about changes in refbooks.
   * <p>
   * Local backends (that is backends without servers) simply call {@link ISkFrontendRear#onBackendMessage(GtMessage)}.
   * Server-based backends distribute the message to all active (working instances of) backends via the server to call
   * {@link ISkFrontendRear#onBackendMessage(GtMessage)} method of the frontend.
   *
   * @param aMessage {@link GtMessage} - message to be send to all connected frontends
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void sendBackendMessage( GtMessage aMessage );

}
