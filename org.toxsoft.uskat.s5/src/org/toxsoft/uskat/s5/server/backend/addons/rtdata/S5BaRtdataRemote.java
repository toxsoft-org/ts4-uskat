package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.backend.addons.rtdata.IS5Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;

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
   * Максимальный размер буфера хранимых данных
   */
  private final int histBufferSize;

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
    baData.currdataTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
    baData.histdataTimeout = IS5ConnectionParams.OP_HISTDATA_TIMEOUT.getValue( aOwner.openArgs().params() ).asLong();
    histBufferSize = IS5ConnectionParams.OP_HISTDATA_BUFFER_SIZE.getValue( aOwner.openArgs().params() ).asInt();
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    if( aMessage.messageId().equals( S5BaBeforeConnectMessages.MSG_ID ) ) {
      owner().sessionInitData().setBackendAddonData( IBaRtdata.ADDON_ID, baData );
    }
  }

  @Override
  public void doJob() {
    long currTime = System.currentTimeMillis();
    GtMessage currDataMessage = null;
    GtMessage histDataMessage = null;
    synchronized (baData) {
      if( baData.currdataToBackend.size() > 0 && //
          owner().isActive() && //
          (baData.currdataTimeout <= 0 || currTime - baData.lastCurrdataToBackendTime > baData.currdataTimeout) ) {
        // Отправка значений текущих данных от фронтенда в бекенд
        currDataMessage = BaMsgRtdataCurrData.INSTANCE.makeMessage( baData.currdataToBackend );

        // TODO: 2023-11-19 mvkd
        // if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        // Gwid testGwid = Gwid.of( "AnalogInput[TP1]$rtdata(rtdPhysicalValue)" );
        // IAtomicValue testValue = baData.currdataToBackend.findByKey( testGwid );
        // if( testValue != null ) {
        // logger().debug( "send currdata: %s = %s", testGwid, testValue );
        // }
        // }

        baData.currdataToBackend.clear();
        baData.lastCurrdataToBackendTime = currTime;
      }
      if( baData.histdataToBackend.size() > 0 && //
          owner().isActive() && //
          (baData.histdataTimeout <= 0 || currTime - baData.lastHistdataToBackendTime > baData.histdataTimeout) ) {
        // Отправка значений хранимых данных от фронтенда в бекенд
        histDataMessage = BaMsgRtdataHistData.INSTANCE.makeMessage( baData.histdataToBackend );
        // Журналирование
        if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
          logger().debug( MSG_HD_SENDING, hdToLog( baData.histdataToBackend ) );
          logger().debug( MSG_HD_SENDED, hdToLog( BaMsgRtdataHistData.INSTANCE.getNewValues( histDataMessage ) ) );
        }
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
  public IMap<Gwid, IAtomicValue> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove == null ) {
      baData.currdataGwidsToFrontend.clear();
    }
    if( aToRemove != null ) {
      for( Gwid gwid : aToRemove ) {
        baData.currdataGwidsToFrontend.remove( gwid );
      }
    }
    baData.currdataGwidsToFrontend.addAll( aToAdd );
    IMap<Gwid, IAtomicValue> retValue = session().configureCurrDataReader( aToRemove, aToAdd );
    return retValue;
  }

  @Override
  public void configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove == null ) {
      baData.currdataGwidsToBackend.clear();
    }
    if( aToRemove != null ) {
      for( Gwid gwid : aToRemove ) {
        baData.currdataGwidsToBackend.remove( gwid );
      }
    }
    baData.currdataGwidsToBackend.addAll( aToAdd );
    session().configureCurrDataWriter( aToRemove, aToAdd );
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
    // Выполнение doJob для обработки немедленной отправки (baData.currdataTimeout <= 0)
    doJob();
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    BaMsgRtdataHistData.checkIntervals( aGwid, aInterval, aValues );
    synchronized (baData) {
      if( baData.histdataToBackend.size() == 0 ) {
        baData.lastHistdataToBackendTime = System.currentTimeMillis();
      }
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> prevValues = baData.histdataToBackend.findByKey( aGwid );
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> newValues = new Pair<>( aInterval, aValues );
      if( prevValues != null ) {
        // Объединение значений по одному данному
        ITimeInterval prevInterval = prevValues.left();
        ITimeInterval newInterval = TimeUtils.union( prevInterval, aInterval );
        TimedList<ITemporalAtomicValue> values = new TimedList<>( prevValues.right() );
        values.addAll( newValues.right() );
        BaMsgRtdataHistData.checkIntervals( aGwid, newInterval, values );
        // Ограничение размера буфера значений параметра
        if( values.size() - histBufferSize >= 0 ) {
          values.removeRangeByIndex( 0, values.size() - histBufferSize );
        }
        BaMsgRtdataHistData.checkIntervals( aGwid, newInterval, values );
        newValues = new Pair<>( newInterval, values );
      }
      baData.histdataToBackend.put( aGwid, newValues );
    }
    // Выполнение doJob для обработки немедленной отправки (baData.histdataTimeout <= 0)
    doJob();
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return session().queryObjRtdata( aInterval, aGwid );
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  @SuppressWarnings( { "nls", "boxing" } )
  static String hdToLog( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aHistData ) {
    StringBuilder sb = new StringBuilder();
    for( Gwid gwid : aHistData.keys() ) {
      Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>> p = aHistData.getByKey( gwid );
      sb.append( format( "gwid = %s, ", gwid ) );
      sb.append( format( "interval = %s, ", p.left() ) );
      if( p.right().size() > 0 ) {
        sb.append( format( "first = %s, ", p.right().first() ) );
        sb.append( format( "last = %s, ", p.right().first() ) );
        sb.append( format( "size = %d, ", p.right().size() ) );
      }
      sb.append( "\n" );
    }
    return sb.toString();
  }

}
