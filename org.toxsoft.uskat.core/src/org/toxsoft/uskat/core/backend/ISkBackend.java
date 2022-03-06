package org.toxsoft.uskat.core.backend;

import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * USkat backend API.
 * <p>
 * Notes on implementation:
 * <ul>
 * <li>All modification methods may throw additional exceptions. Backed API does not require backend to enforce any
 * validation when writing data to it. However particular backend may check data to be written and not allow to violate
 * the storage integrity;</li>
 * <li>TODO frontend rear messaging - ???.</li>
 * <li>TODO working with threads- ???.</li>
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
   * @param <T> - expected interface of the addon
   * @param aAddonInterface {@link Class}&lt;T&gt; - expected interface of the addons
   * @return &lt;T&gt; - found addon or <code>null</code> if no such optional addon exists
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T> T findBackendAddon( Class<T> aAddonInterface );

}
