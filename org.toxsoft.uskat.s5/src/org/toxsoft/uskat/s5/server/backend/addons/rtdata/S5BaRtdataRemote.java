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
import org.toxsoft.uskat.core.backend.api.BaMsgRtdataCurrData;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Remote {@link IBaRtdata} implementation.
 *
 * @author mvk
 */
class S5BaRtdataRemote
    extends S5AbstractBackendAddonRemote<IS5BaRtdataSession>
    implements IBaRtdata {

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
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaRtdataRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA, IS5BaRtdataSession.class );
    // Установка таймаута
    currDataToBackendTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void doJob() {
    long currTime = System.currentTimeMillis();
    if( currTime - lastCurrDataToBackendTime > currDataToBackendTimeout ) {
      lockWrite( currDataToBackendLock );
      try {
        // Отправка данных от фронтенда в бекенд
        owner().onFrontendMessage( BaMsgRtdataCurrData.INSTANCE.makeMessage( currDataToBackend ) );
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
    session().configureCurrDataReader( aRtdGwids );
  }

  @Override
  public void configureCurrDataWriter( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    session().configureCurrDataWriter( aRtdGwids );
  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    session().writeCurrData( aGwid, aValue );
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    session().writeHistData( aGwid, aInterval, aValues );
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return session().queryObjRtdata( aInterval, aGwid );
  }
}
