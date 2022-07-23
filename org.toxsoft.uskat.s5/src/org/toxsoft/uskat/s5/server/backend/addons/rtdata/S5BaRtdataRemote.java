package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.BaMsgRtdataCurrData;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaRtdata} implementation.
 *
 * @author mvk
 */
class S5BaRtdataRemote
    extends S5AbstractBackendAddonRemote<IS5BaRtdataSession>
    implements IBaRtdata {

  /**
   * Данные конфигурации фронтенда для {@link IBaRtdata}
   */
  private final S5BaRtdataData baData = new S5BaRtdataData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaRtdataRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA, IS5BaRtdataSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaRtdata.ADDON_ID, baData );
    // Установка таймаутов
    baData.currDataToSendTimeout =
        IS5ConnectionParams.OP_CURRDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
    baData.histDataToSendTimeout =
        IS5ConnectionParams.OP_HISTDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
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
    synchronized (baData) {
      if( currTime - baData.lastCurrDataToSendTime > baData.currDataToSendTimeout ) {
        // Отправка данных от фронтенда в бекенд
        owner().onFrontendMessage( BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currDataToSend ) );
        baData.currDataToSend.clear();
        baData.lastCurrDataToSendTime = currTime;
      }
      if( currTime - baData.lastHistDataToSendTime > baData.histDataToSendTimeout ) {
        // TODO:
        baData.lastHistDataToSendTime = currTime;
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
    synchronized (baData) {
      baData.currDataToSend.put( aGwid, aValue );
    }
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
