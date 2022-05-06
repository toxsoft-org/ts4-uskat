package org.toxsoft.uskat.sysext.realtime.addon;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.sysext.realtime.addon.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;
import org.toxsoft.uskat.sysext.realtime.pas.S5RealtimeCallbackWriteCurrData;
import org.toxsoft.uskat.sysext.realtime.pas.S5RealtimeCallbackWriteHistData;
import org.toxsoft.uskat.sysext.realtime.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.commands.impl.S5CommandIdGenerator;
import org.toxsoft.uskat.sysext.realtime.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.IS5BackendHistDataSingleton;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.incub.FixedCapacityIntMap;

/**
 * Сессия реализации расширения backend {@link ISkBackendAddonRealtime}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class S5RealtimeSession
    extends S5BackendAddonSession
    implements IS5RealtimeRemote, IS5RealtimeSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Менеджер сессий
   */
  @EJB
  private IS5SessionManager sessionManager;

  /**
   * backend управления текущими данными системы
   */
  @EJB
  private IS5BackendCurrDataSingleton currdataBackend;

  /**
   * backend управления текущими данными системы
   */
  @EJB
  private IS5BackendHistDataSingleton histdataBackend;

  /**
   * backend событий
   */
  @EJB
  private IS5BackendEventSingleton eventsBackend;

  /**
   * backend команд
   */
  @EJB
  private IS5BackendCommandSingleton commandsBackend;

  /**
   * Пустой конструктор.
   */
  public S5RealtimeSession() {
    super( SK_BACKEND_ADDON_ID, STR_D_BACKEND_REALTIME );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BackendAddonSession> doGetLocalView() {
    return IS5RealtimeSession.class;
  }

  @Override
  protected Class<? extends IS5BackendAddonRemote> doGetRemoteView() {
    return IS5RealtimeRemote.class;
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  public void doAfterInit( S5SessionCallbackWriter aFrontend, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // Данные для инициализации расширения бекенда "реальное время"
    S5RealtimeInitData initData = aInitData.findAddonData( SK_BACKEND_ADDON_ID, S5RealtimeInitData.class );
    if( initData == null ) {
      return;
    }
    // Результаты инициализации расширения бекенда "реальное время"
    S5RealtimeInitResult initResult = new S5RealtimeInitResult();
    aInitResult.setAddonData( SK_BACKEND_ADDON_ID, initResult );

    // Сессия связанная с писателем обратных вызовов
    S5RemoteSession session = aFrontend.session();
    // Данные фронтенда
    S5RealtimeFrontendData frontendData = new S5RealtimeFrontendData();
    session.frontendData().setAddonData( SK_BACKEND_ADDON_ID, frontendData );

    // Регистрация обработчиков приема текущих и хранимых данных
    aFrontend.registerNotificationHandler( S5RealtimeCallbackWriteCurrData.WRITE_CURRDATA_METHOD,
        new S5RealtimeCallbackWriteCurrData( currdataBackend, sessionManager, sessionID() ) );
    aFrontend.registerNotificationHandler( S5RealtimeCallbackWriteHistData.WRITE_HISTDATA_METHOD,
        new S5RealtimeCallbackWriteHistData( histdataBackend, sessionManager, sessionID() ) );

    // Регистрация набора чтения текущих данных
    IIntMap<Gwid> readDataset = currdataBackend.configureCurrDataReader( aFrontend, null, initData.readCurrdataGwids );
    initResult.readCurrdataDataset.setAll( readDataset );
    // Регистрация набора чтения хранимых данных
    IIntMap<Gwid> writeDataset =
        currdataBackend.configureCurrDataWriter( aFrontend, null, initData.writeCurrdataGwids );
    initResult.writeCurrdataDataset.setAll( writeDataset );

    // Регистрация исполнителей команд
    commandsBackend.setExecutableCommandGwids( aFrontend, initData.commandsGwids );

    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( format( "doAfterInit(...): sessionID = %s, resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( format( "\n   === readCurrdata (%d) === ", //$NON-NLS-1$
          Integer.valueOf( frontendData.readCurrdata.dataset().values().size() ) ) );
      for( Gwid gwid : frontendData.readCurrdata.dataset().values() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      sb.append( format( "\n   === writeCurrdata (%d) === ", //$NON-NLS-1$
          Integer.valueOf( frontendData.writeCurrdata.dataset().values().size() ) ) );
      for( Gwid gwid : frontendData.writeCurrdata.dataset().values() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      IGwidList eventIds = session.frontendData().events().gwids();
      sb.append( format( "\n\n   === events (%d) === ", Integer.valueOf( eventIds.size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : eventIds ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      sb.append( format( "\n\n   === commands (%d) === ", Integer.valueOf( frontendData.commands.gwids().size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : frontendData.commands.gwids() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
  }

  @Override
  protected void doBeforeClose() {
    S5SessionCallbackWriter callbackWriter = sessionManager.findCallbackWriter( sessionID() );
    if( callbackWriter == null ) {
      logger().error( "doBeforeClose(...): callbackWriter = null" ); //$NON-NLS-1$
      return;
    }
    commandsBackend.setExecutableCommandGwids( callbackWriter, IGwidList.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendAddonRealtime
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IIntMap<Gwid> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    // Писатель обратных вызовов сессии
    S5SessionCallbackWriter frontend = sessionManager.getCallbackWriter( sessionID() );
    // Реконфигурация набора
    IIntMap<Gwid> dataset = currdataBackend.configureCurrDataReader( frontend, aToRemove, aToAdd );
    // Сохранение измененной сессии в кэше
    sessionManager.updateRemoteSession( frontend.session() );
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    FixedCapacityIntMap<Gwid> retValue = new FixedCapacityIntMap<>( dataset.size() );
    for( int index : dataset.keys() ) {
      retValue.put( index, dataset.getByKey( index ) );
    }
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      S5RealtimeFrontendData frontendData =
          frontend.frontendData().getAddonData( SK_BACKEND_ADDON_ID, S5RealtimeFrontendData.class );
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( format( "configureCurrDataReader(...): sessionID = %s, changed resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( format( "\n   === readCurrdata (%d) === ", //$NON-NLS-1$
          Integer.valueOf( frontendData.readCurrdata.dataset().values().size() ) ) );
      for( Gwid gwid : frontendData.readCurrdata.dataset().values() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IIntMap<Gwid> configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    // Писатель обратных вызовов сессии
    S5SessionCallbackWriter frontend = sessionManager.getCallbackWriter( sessionID() );
    // Реконфигурация набора
    IIntMap<Gwid> dataset = currdataBackend.configureCurrDataWriter( frontend, aToRemove, aToAdd );
    // Сохранение измененной сессии в кэше
    sessionManager.updateRemoteSession( frontend.session() );
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    FixedCapacityIntMap<Gwid> retValue = new FixedCapacityIntMap<>( dataset.size() );
    for( int index : dataset.keys() ) {
      retValue.put( index, dataset.getByKey( index ) );
    }
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      S5RealtimeFrontendData frontendData =
          frontend.frontendData().getAddonData( SK_BACKEND_ADDON_ID, S5RealtimeFrontendData.class );
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( format( "configureCurrDataWriter(...): sessionID = %s, changed resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( format( "\n   === writeCurrdata (%d) === ", //$NON-NLS-1$
          Integer.valueOf( frontendData.writeCurrdata.dataset().values().size() ) ) );
      for( Gwid gwid : frontendData.writeCurrdata.dataset().values() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
    return retValue;
  }

  // TODO: 2020-06-04 mvk - асинхронный прием текущих данных может вызывать дефицит потоков прием из-за
  // чего текущие данные просто перестают приниматься (usecase tm2, local server, gateways).
  // @Deprecated 2020-07-31 данные передаются через PAS
  // @Asynchronous
  @Deprecated
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeCurrData( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aValues );
    // // TODO: 2020-06-23 mvk ловушка гонки потоков
    // // Писатель обратных вызовов сессии
    // S5SessionCallbackWriter callbackWriter = sessionManager.callbackWriter( wildflySessionID() );
    // // Сессия связанная с писателем обратных вызовов
    // S5RemoteSession session = callbackWriter.session();
    // // Данные фронтенда
    // S5RealtimeFrontendData frontendData = session.frontendData().getAddonData( SK_BACKEND_ADDON_ID,
    // S5RealtimeFrontendData.class );
    // // Текущий набор записи текущих данных сессии
    // S5DatasetSupport currdata = frontendData.writeCurrdata;
    // // Карта индексов данных поддержки
    // IMap<Integer, Gwid> parentDataset = currdataBackend.datasetIndexes();
    // for( int index : aValues.ids() ) {
    // Integer indexKey = Integer.valueOf( index );
    // Gwid parentGwid = parentDataset.findByKey( indexKey );
    // Gwid ownGwid = currdata.configuration().findByKey( indexKey );
    // if( parentGwid.equals( ownGwid ) == false ) {
    // // Ошибка индексов в наборах текущих данных
    // logger().error( ERR_CURRDATA_WRONG_INDEXES, indexKey, parentGwid, ownGwid, currdata.configuration().size() );
    // }
    // }

    currdataBackend.writeCurrData( aValues );
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    // Хранимые данные должны передаваться через pas (S5RealtimeCallbackWriteHistData)
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public String execHistData( IQueryInterval aQueryInterval, IGwidList aGwids, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryInterval, aGwids, aParams );
    // Формирование идентификатора
    String queryId = histdataBackend.uuidGenerator().nextId();
    // Писатель обратных вызовов сессии
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID() );
    // Запуск выполнения запроса
    histdataBackend.execHistData( callbackWriter, queryId, aQueryInterval, aGwids, aParams );
    return queryId;
  }

  @Override
  public void cancelHistDataResult( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    histdataBackend.cancelHistDataResult( aQueryId );
  }

  @Asynchronous
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void fireEvents( ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID() );
    eventsBackend.fireEvents( callbackWriter, aEvents );
  }

  @Override
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    return eventsBackend.queryEvents( aInterval, aNeededGwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void setExcutableCommandGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    // Писатель обратных вызовов сессии
    S5SessionCallbackWriter frontend = sessionManager.getCallbackWriter( sessionID() );
    // Сессия связанная с писателем обратных вызовов
    S5RemoteSession session = frontend.session();
    // Регистрация исполнителя команд в backend команд (ожидается изменение данных сессии)
    commandsBackend.setExecutableCommandGwids( frontend, aNeededGwids );
    // Сохранение измененной сессии в кэше
    sessionManager.updateRemoteSession( session );
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      S5RealtimeFrontendData frontendData =
          frontend.frontendData().getAddonData( SK_BACKEND_ADDON_ID, S5RealtimeFrontendData.class );
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( format( "setExcutableCommandGwids(...): sessionID = %s, changed resources:", sessionID() ) ); //$NON-NLS-1$
      sb.append( format( "\n   === commands (%d) === ", Integer.valueOf( frontendData.commands.gwids().size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : frontendData.commands.gwids() ) {
        sb.append( format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IDpuCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Передатчик обратных вызовов (frontend)
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID() );
    // Идентификатор новой команды
    String cmdId = S5CommandIdGenerator.INSTANCE.nextId();
    // Формирование команды
    IDpuCommand cmd = new DpuCommand( currTime, cmdId, aCmdGwid, aAuthorSkid, aArgs );

    // TODO: 2020-02-18 mvk изменения данных реального времени теперь проводится в singleton.sendCommand
    // требуется проверка - нужно ли делать updateSesion???
    // // Сессия связанная с писателем обратных вызовов
    // S5RemoteSession session = callbackWriter.session();
    // // Обновление сессии, так как изменения состоянии может быть получено немедленно
    // sessionManager.updateSession( session );

    // Передача команды
    commandsBackend.sendCommand( callbackWriter, cmd );
    // Возвращение выполняемой команды
    return cmd;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void changeCommandState( DpuCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID() );
    commandsBackend.changeCommandState( callbackWriter, aStateChangeInfo );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<IDpuCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    return commandsBackend.queryCommands( aInterval, aNeededGwids );
  }
}
