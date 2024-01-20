package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.S5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.S5BackendHistDataSingleton;

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
    // Регистрация слушателя событий от фронтенда
    frontend().frontendEventer().addListener( aMessage -> {
      // Получение значений текущих данных от фронтенда для записи в бекенда
      if( aMessage.messageId().equals( BaMsgRtdataCurrData.MSG_ID ) ) {
        IMap<Gwid, IAtomicValue> values = BaMsgRtdataCurrData.INSTANCE.getNewValues( aMessage );
        // Запись новых значений текущих данных
        currDataSupport.writeValues( frontend(), values );
        // Обработка статистики приема пакета текущих данных
        statisticCounter().onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED_CURRDATA, AV_1 );
        return;
      }
      if( aMessage.messageId().equals( BaMsgRtdataHistData.MSG_ID ) ) {
        IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> values =
            BaMsgRtdataHistData.INSTANCE.getNewValues( aMessage );
        // Запись новых значений хранимых данных проводится асинхронно, чтобы не замедлять потоки текущих данных
        histDataSupport.asyncWriteValues( values );
        // Обработка статистики приема пакета текущих данных
        statisticCounter().onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED_HISTDATA, AV_1 );
        return;
      }
    } );
    // Установка таймаутов
    baData.currdataTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
    baData.histdataTimeout = IS5ConnectionParams.OP_HISTDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();

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
    GtMessage currDataMessage = null;
    GtMessage histDataMessage = null;
    synchronized (baData) {
      if( baData.currdataToBackend.size() > 0
          && currTime - baData.lastCurrdataToBackendTime > baData.currdataTimeout ) {
        // Отправка данных от фронтенда в бекенд
        currDataMessage = BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currdataToBackend );
        baData.currdataToBackend.clear();
        baData.lastCurrdataToBackendTime = currTime;
      }
      if( baData.histdataToBackend.size() > 0
          && currTime - baData.lastHistdataToBackendTime > baData.histdataTimeout ) {
        // Отправка значений хранимых данных от фронтенда в бекенд
        histDataMessage = BaMsgRtdataHistData.INSTANCE.makeMessage( baData.histdataToBackend );
        baData.histdataToBackend.clear();
        baData.lastHistdataToBackendTime = currTime;
      }
    }
    if( currDataMessage != null ) {
      owner().onFrontendMessage( currDataMessage );
    }
    if( histDataMessage != null ) {
      owner().onFrontendMessage( histDataMessage );
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
    synchronized (baData) {
      if( baData.currdataToBackend.size() == 0 ) {
        baData.lastCurrdataToBackendTime = System.currentTimeMillis();
      }
      baData.currdataToBackend.put( aGwid, aValue );
    }
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    synchronized (baData) {
      if( baData.histdataToBackend.size() == 0 ) {
        baData.lastHistdataToBackendTime = System.currentTimeMillis();
      }
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> prevValues = baData.histdataToBackend.findByKey( aGwid );
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> newValues = new Pair<>( aInterval, aValues );
      if( prevValues != null ) {
        // Объединение значений по одному данному
        long startTime = Math.min( prevValues.left().startTime(), newValues.left().startTime() );
        long endTime = Math.max( prevValues.left().endTime(), newValues.left().endTime() );
        TimedList<ITemporalAtomicValue> values = new TimedList<>( prevValues.right() );
        values.addAll( newValues.right() );
        newValues = new Pair<>( new TimeInterval( startTime, endTime ), values );
      }
      baData.histdataToBackend.put( aGwid, newValues );
    }
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return histDataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
