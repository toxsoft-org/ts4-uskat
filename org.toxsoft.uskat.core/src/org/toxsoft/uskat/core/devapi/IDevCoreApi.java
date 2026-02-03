package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;
import org.toxsoft.uskat.core.devapi.transactions.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Core API extensions for USkat service developers.
 *
 * @author hazard157
 */
public interface IDevCoreApi
    extends ISkCoreApi {

  /**
   * Returns the GWID-Steing key-value database for the developers.
   *
   * @return {@link ISkGwidDbService} - GWID-Steing key-value database
   */
  ISkGwidDbService gwidDbService();

  /**
   * Returns the batch operations service.
   *
   * @return {@link ISkTransactionService} - the batch service
   */
  ISkTransactionService transactionService();

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

  /**
   * Returns the core entities localization support.
   *
   * @return {@link ICoreL10n} - the core localizer
   */
  ICoreL10n l10n();

  /**
   * Returns arguments used in {@link ISkConnection#open(ITsContextRo)}.
   *
   * @return {@link ITsContextRo} - connection opening arguments
   */
  ITsContextRo openArgs();

  /**
   * Returns information how the Sk-object of asked class is implemented in corAPI.
   * <p>
   * All classes not explicitly claimed by any service is considered to be "owned" by {@link ISkSysdescr#SERVICE_ID}.
   * <p>
   * For non-existing class and classes not claimed by any service returns "owner" service
   * {@link ISkSysdescr#SERVICE_ID} and pure {@link SkObject} creator.
   *
   * @param aClassId String - class ID
   * @return {@link SkClassImplementationInfo} - Sk-class implementation information
   */
  SkClassImplementationInfo getSkClassImplementationInfo( String aClassId );

  /**
   * Returns the Sk-connection - owner and creator of this instance.
   *
   * @return {@link ISkConnection} - the parent connection
   */
  ISkConnection skConn();

  /**
   * Returns the means to execute specific Core code in the user-specified thread.
   * <p>
   * There are some tasks in the USkat Core implementation needing to be called from the user-controlled thread like
   * main thread in GUI applications. Also the {@link ITsThreadExecutor} used to separate backend threads from the
   * user-controlled thread.
   *
   * @return {@link ITsThreadExecutor} - the user-specified thread access
   */
  ITsThreadExecutor executor();

  /**
   * Returns the progress callback of a lengthy operation.
   *
   * @return {@link ILongOpProgressCallback} the progress callback
   */
  ILongOpProgressCallback progressCallback();

  /**
   * @deprecated Use {@link #getSkClassImplementationInfo(String)} instead
   */
  @Deprecated
  @SuppressWarnings( "javadoc" )
  default String determineClassClaimingServiceId( String aClassId ) {
    return getSkClassImplementationInfo( aClassId ).claimingServiceId();
  }

}
