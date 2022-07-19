package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Реализация синглетона {@link IS5BackendHistDataSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendHistDataSingleton
    // TODO:
    // extends S5BackendSequenceSupportSingleton<IS5HistDataSequence, ITemporalAtomicValue>
    extends S5BackendSupportSingleton
    implements IS5BackendHistDataSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_HISTDATA_ID = "S5BackendHistDataSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Конструктор.
   */
  public S5BackendHistDataSingleton() {
    super( BACKEND_HISTDATA_ID, STR_D_BACKEND_HISTDATA );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendHistDataSingleton
  //
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    // TODO:
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    // TODO:
    return null;
  }

}
