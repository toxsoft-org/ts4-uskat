package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.api.BackendAddonBase;
import org.toxsoft.uskat.core.backend.api.IBackendAddon;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Base implementation of local {@link IBackendAddon} for s5 backend.
 *
 * @author mvk
 */
public abstract class S5AbstractBackendAddonLocal
    extends BackendAddonBase<IS5BackendLocal>
    implements IS5BackendAddonLocal {

  /**
   * Счетчик статистической информации сессии
   */
  private IS5StatisticCounter statisticCounter;

  /**
   * Журнал
   */
  private ILogger logger = LoggerWrapper.getLogger( getClass() );

  /**
   * Constructor for subclasses.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @param aInfo {@link IStridable} - the addon info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException section name is not an IDpath
   */
  protected S5AbstractBackendAddonLocal( IS5BackendLocal aOwner, IStridable aInfo ) {
    super( aOwner, aInfo );
    statisticCounter = aOwner.backendSingleton().sessionManager().findStatisticCounter( aOwner.sessionID() );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonLocal
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
  // API для наследников
  //
  /**
   * Возвращает идентификатор сессии
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession#skid()}
   */
  protected final Skid sessionID() {
    return owner().sessionID();
  }

  /**
   * Возвращает модуль формирования статистики
   *
   * @return {@link IS5StatisticCounter} статистика
   */
  protected final IS5StatisticCounter statisticCounter() {
    return statisticCounter;
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

}
