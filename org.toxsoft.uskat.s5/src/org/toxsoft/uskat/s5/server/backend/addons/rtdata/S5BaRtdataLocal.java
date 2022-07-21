package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.S5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.S5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Local {@link IBaRtdata} implementation.
 *
 * @author mvk
 */
class S5BaRtdataLocal
    extends S5AbstractBackendAddonLocal
    implements IBaRtdata {

  /**
   * Поддержка сервера обработки запросов к текущим данным
   */
  private final IS5BackendCurrDataSingleton currDataSupport;

  /**
   * Поддержка сервера обработки запросов к хранимым данным
   */
  private final IS5BackendHistDataSingleton histDataSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaRtdata}
   */
  private final S5BaRtdataData baData = new S5BaRtdataData();

  /**
   * Карта значений подготовленных для отправки в бекенд
   */
  private final IMapEdit<Gwid, IAtomicValue> currDataToBackend = new ElemMap<>();

  /**
   * Блокировка доступа к {@link #currDataToBackend}
   */
  private final S5Lockable currDataToBackendLock = new S5Lockable();

  /**
   * Таймаут (мсек) передачи текущих данных в бекенд
   */
  private long currDataToBackendTimeout = 1000;

  /**
   * Время последней передачи данных в бекенд
   */
  private volatile long lastCurrDataToBackendTime = System.currentTimeMillis();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaRtdataLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
    currDataSupport = aOwner.backendSingleton().get( S5BackendCurrDataSingleton.BACKEND_CURRDATA_ID,
        IS5BackendCurrDataSingleton.class );
    histDataSupport = aOwner.backendSingleton().get( S5BackendHistDataSingleton.BACKEND_HISTDATA_ID,
        IS5BackendHistDataSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaRtdata.ADDON_ID, baData );
    // Установка таймаута
    currDataToBackendTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
  }

  @Override
  public void doJob() {
    long currTime = System.currentTimeMillis();
    if( currTime - lastCurrDataToBackendTime > currDataToBackendTimeout ) {
      lockWrite( currDataToBackendLock );
      try {
        // Отправка данных в бекенд
        currDataSupport.writeValues( currDataToBackend );
        currDataToBackend.clear();
        lastCurrDataToBackendTime = currTime;
      }
      finally {
        unlockWrite( currDataToBackendLock );
      }
    }
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaRtdata
  //
  @Override
  public void configureCurrDataReader( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    currDataSupport.configureCurrDataReader( frontend(), aRtdGwids );
  }

  @Override
  public void configureCurrDataWriter( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    currDataSupport.configureCurrDataWriter( frontend(), aRtdGwids );
  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    lockWrite( currDataToBackendLock );
    try {
      currDataToBackend.put( aGwid, aValue );
    }
    finally {
      unlockWrite( currDataToBackendLock );
    }
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    histDataSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return histDataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
