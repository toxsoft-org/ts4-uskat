package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat backend API.
 * <p>
 * Notes on implementation:
 * <ul>
 * <li>All modification methods may throw additional exceptions. Backed API does not require backend to enforce any
 * validation when writing data to it. However particular backend may check data to be written and not allow to violate
 * the storage integrity;</li>
 * <li>Communication may be initiatied both by the frontend or the backend. The frontend simply calls methods of the
 * backend. The only way to cimmunicate to frontend at the backends initiative is to send message via
 * {@link ISkFrontendRear#onBackendMessage(GtMessage)}.</li>
 * <li>Execution threads. Some backend implementations may have internal execution threads and may want to send message
 * to the frontend from the internal thread. Such backends inform their counterparts with the
 * {@link ISkBackendHardConstant#OPDEF_SKBI_NEEDS_THREAD_SEPARATOR} option set ti <code>true</code> in
 * {@link ISkBackendInfo#params()}. Because core API impementation (class {@link SkCoreApi} and servcies) is
 * single-threaded, the threads must be separated by supplying {@link SkBackendThreadSeparator} instance in the
 * {@link ISkCoreConfigConstants#REFDEF_BACKEND_THREAD_SEPARATOR} when opening th connection via
 * {@link ISkConnection#open(ITsContextRo)}.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkBackend
    extends ICloseable {

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
   * Returns information about backend instance.
   *
   * @return {@link ISkBackendInfo} - the backend info
   */
  ISkBackendInfo getBackendInfo();

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
   * @return {@link IBaClasses} - objects storage
   */
  IBaObjects baObjects();

  /**
   * Returns backend addon for links storage.
   *
   * @return {@link IBaClasses} - links storage
   */
  IBaLinks baLinks();

  /**
   * Returns backend addon for events messaging.
   *
   * @return {@link IBaEvents} - events messaging
   */
  IBaEvents baEvents();

  /**
   * Returns backend addon for classes storage.
   *
   * @return {@link IBaClasses} - class storage
   */
  IBaClobs baClobs();

  // ------------------------------------------------------------------------------------
  // Optional addons
  //

  /**
   * Returns the means to create USkat extension services provided by the backend.
   * <p>
   * If backend provides no extension services than method returns {@link ISkExtServicesProvider#NULL}.
   *
   * @return {@link ISkExtServicesProvider} - extension services provider
   */
  ISkExtServicesProvider getExtServicesProvider();

  /**
   * Returns bakend addon by it's interface.
   *
   * @param <T> - expected interface/class of the addon
   * @param aAddonId String the ID of the addon
   * @param aExpectedType {@link Class}&lt;T&gt; - expected interface of the addons
   * @return &lt;T&gt; - found addon or <code>null</code> if no such optional addon exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException found addon is not of expected Java type
   */
  <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType );

}
