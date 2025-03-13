package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

/**
 * Реализация сессии расширения бекенда {@link IS5BaRtdataSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class S5BaRtdataSession
    extends S5AbstractBackendAddonSession
    implements IS5BaRtdataSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера запросов к текущим данным
   */
  @EJB
  private IS5BackendCurrDataSingleton currDataSupport;

  /**
   * Поддержка сервера запросов к хранимым данным
   */
  @EJB
  private IS5BackendHistDataSingleton histDataSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaRtdataSession() {
    super( ISkBackendHardConstant.BAINF_RTDATA );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaRtdataSession> doGetSessionView() {
    return IS5BaRtdataSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // 2023-10-20 mvk ---+++
    // S5BaRtdataData baData = new S5BaRtdataData();
    // frontend().frontendData().setBackendAddonData( IBaRtdata.ADDON_ID, baData );
    S5BaRtdataData baData = aInitData.findBackendAddonData( IBaRtdata.ADDON_ID, S5BaRtdataData.class );
    if( baData == null ) {
      // Клиент не поддерживает реальное время (работу с текущими данными)
      baData = new S5BaRtdataData();
    }
    // Установка в данных сессии конфигурации реального времени
    frontend().frontendData().setBackendAddonData( IBaRtdata.ADDON_ID, baData );

    // TODO: 2023-11-19 mvkd
    // Gwid testGwid = Gwid.of( "AnalogInput[TP1]$rtdata(rtdPhysicalValue)" );
    // IAtomicValue testValue = baData.currdataToBackend.findByKey( testGwid );
    // boolean readPresent = baData.currdataGwidsToFrontend.hasElem( testGwid );
    // boolean writePresent = baData.currdataGwidsToBackend.hasElem( testGwid );
    // LoggerUtils.defaultLogger().info(
    // "S5BaRtDataSession.doAfterInit(...): testGwid = %s: readPresent = %s, writePresent = %s, value = %s", testGwid,
    // Boolean.valueOf( readPresent ), Boolean.valueOf( writePresent ), testValue );

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
    // 2023-03-12 mvk+++
    // Передача текущих значений данных на которые подписан клиент
    if( baData.currdataGwidsToFrontend.size() > 0 ) {
      IMap<Gwid, IAtomicValue> retValue = currDataSupport.readValues( baData.currdataGwidsToFrontend );
      // Немедленная передача текущих значений фронтенду
      GtMessage message = BaMsgRtdataCurrData.INSTANCE.makeMessage( retValue );
      // Немедленная передача текущих значений фронтенду
      frontend().onBackendMessage( message );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaRtdataSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IMap<Gwid, IAtomicValue> configureCurrDataReader( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    IMap<Gwid, IAtomicValue> retValue = currDataSupport.configureCurrDataReader( frontend(), aRtdGwids );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataWriter( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    currDataSupport.configureCurrDataWriter( frontend(), aRtdGwids );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
  }

  // Клиент не должен использовать этот метод - текущие данные поступают через IGtMessageListener.onGenericTopicMessage(
  // GtMessage). Смотри метод doAfterInit(...). Реализация сделана только для полной поддержки API
  @Deprecated
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    IMap<Gwid, IAtomicValue> values = new ElemMap<>();
    currDataSupport.writeValues( frontend(), values );
  }

  // Клиент не должен использовать этот метод - текущие данные поступают через IGtMessageListener.onGenericTopicMessage(
  // GtMessage). Смотри метод doAfterInit(...). Реализация сделана только для полной поддержки API
  @Deprecated
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    IMapEdit<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> values = new ElemMap<>();
    values.put( aGwid, new Pair<>( aInterval, aValues ) );
    histDataSupport.syncWriteValues( values );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return histDataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
