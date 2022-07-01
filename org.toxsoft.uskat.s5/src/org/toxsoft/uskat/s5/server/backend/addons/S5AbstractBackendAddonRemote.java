package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.uskat.s5.server.backend.addons.IS5Resources.*;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.backend.api.BackendAddonBase;
import org.toxsoft.uskat.core.backend.api.IBackendAddon;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Base implementation of remote {@link IBackendAddon} for s5 backend.
 *
 * @author mvk
 * @param <SESSION> сессия расширения бекенда на сервере
 */
public abstract class S5AbstractBackendAddonRemote<SESSION extends IS5BackendAddonSession>
    extends BackendAddonBase<IS5BackendRemote>
    implements IS5BackendAddonRemote {

  private final Class<SESSION> addonSessionClass;
  private final ILogger        logger = LoggerWrapper.getLogger( getClass() );

  /**
   * Constructor for subclasses.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @param aInfo {@link IStridable} - the addon info
   * @param aAddonSessionClass Class&lt;SESSION&gt; тип интерфейса расширения backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException section name is not an IDpath
   */
  protected S5AbstractBackendAddonRemote( IS5BackendRemote aOwner, IStridable aInfo,
      Class<SESSION> aAddonSessionClass ) {
    super( aOwner, aInfo );
    addonSessionClass = TsNullArgumentRtException.checkNull( aAddonSessionClass );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonRemote
  //
  @Override
  public final IS5FrontendRear frontend() {
    return (IS5FrontendRear)owner().frontend();
  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void doJob() {
    // nop
  }

  @Override
  public abstract void close();

  // ------------------------------------------------------------------------------------
  // public API
  //
  /**
   * Возвращает сессию расширения бекенд на сервере
   *
   * @return SESSION сессия расширения бекенда
   */
  public final SESSION session() {
    SESSION retValue = findRemote();
    if( retValue == null ) {
      // Не найден addons
      if( !owner().isActive() ) {
        // Нет связи с сервером
        throw new TsIllegalStateRtException( ERR_NO_CONNECTION );
      }
      // Не найдено расширение backend
      throw new TsItemNotFoundRtException( ERR_BA_SESSION_NOT_FOUND, id() );
    }
    return retValue;
  }

  /**
   * Возвращает сессию расширения бекенд на сервере
   *
   * @return SESSION сессия расширения бекенда или null если нет связи
   */
  public final SESSION findRemote() {
    if( !owner().isActive() ) {
      // Нет связи с сервером
      return null;
    }
    return owner().getBaSession( id(), addonSessionClass );
  }

  // ------------------------------------------------------------------------------------
  // API for descendans
  //
  /**
   * Возвращает сессию расширения бекенда на сервере
   *
   * @return {@link IS5BackendAddonSession} сессия расширения
   */

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }
}
