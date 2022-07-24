package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.BaMsgRtdataCurrData;
import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

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
class S5BaRtdataSession
    extends S5AbstractBackendAddonSession
    implements IS5BaRtdataSession {

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
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaRtdataData baData = new S5BaRtdataData();
    frontend().frontendData().setBackendAddonData( IBaCommands.ADDON_ID, baData );
    // Регистрация слушателя событий от фронтенда
    frontend().gtMessageEventer().addListener( aMessage -> {
      // Получение значений текущих данных от фронтенда для записи в бекенда
      if( aMessage.messageId().equals( BaMsgRtdataCurrData.MSG_ID ) ) {
        IMap<Gwid, IAtomicValue> values = BaMsgRtdataCurrData.INSTANCE.getNewValues( aMessage );
        // Запись новых значений текущих данных
        currDataSupport.writeValues( values );
        // Обработка статистики приема пакета текущих данных
        statisticCounter().onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED_CURRDATA, AV_1 );
        return;
      }
      // TODO: for histdata
    } );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaRtdataSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataReader( IGwidList aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    currDataSupport.configureCurrDataReader( frontend(), aRtdGwids );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
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
  // GtMessage). Смотри метод doAfterInit(...). Реализация сделана только для целостности поддержки API
  @Deprecated
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    IMap<Gwid, IAtomicValue> values = new ElemMap<>();
    currDataSupport.writeValues( values );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    histDataSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return histDataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
