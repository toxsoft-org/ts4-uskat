package org.toxsoft.uskat.sysext.realtime.addon;

import static org.toxsoft.uskat.s5.server.backend.supports.events.impl.S5BackendEventSingleton.*;
import static org.toxsoft.uskat.sysext.realtime.addon.IS5Resources.*;
import static org.toxsoft.uskat.sysext.realtime.supports.commands.impl.S5BackendCommandSingleton.*;
import static org.toxsoft.uskat.sysext.realtime.supports.currdata.S5BackendCurrDataSingleton.*;
import static org.toxsoft.uskat.sysext.realtime.supports.histdata.S5BackendHistDataSingleton.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.s5.client.local.S5LocalBackend;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.utils.datasets.S5DatasetSupport;
import org.toxsoft.uskat.sysext.realtime.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.commands.impl.S5CommandIdGenerator;
import org.toxsoft.uskat.sysext.realtime.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.IS5BackendHistDataSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.IS5HistDataSequenceWriter;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.S5HistDataWriteSupport;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.common.dpu.rt.events.DpuWriteHistData;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.connection.ISkConnection;

/**
 * Реализация локального доступа к расширению backend {@link ISkBackendAddonRealtime}.
 *
 * @author mvk
 */
public final class S5RealtimeLocal
    extends S5BackendAddonLocal
    implements ISkBackendAddonRealtime, IS5HistDataSequenceWriter {

  private static final long serialVersionUID = 157157L;

  private IS5BackendCurrDataSingleton  currdataBackend;
  private IS5BackendHistDataSingleton  histdataBackend;
  private IS5BackendCommandSingleton   commandsBackend;
  private IS5BackendEventSingleton     eventsBackend;
  private S5RealtimeFrontendData       frontendData = new S5RealtimeFrontendData();
  private final S5HistDataWriteSupport histdataWriteSupport;

  /**
   * Конструктор
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения {@link ISkConnection#open(ITsContextRo)}
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5RealtimeLocal( ITsContextRo aArgs ) {
    super( SK_BACKEND_ADDON_ID, STR_D_BACKEND_REALTIME );
    TsNullArgumentRtException.checkNull( aArgs );
    histdataWriteSupport = new S5HistDataWriteSupport( aArgs );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  public void doAfterInit( S5LocalBackend aOwner, IS5BackendCoreSingleton aBackend ) {
    aOwner.frontendData().setAddonData( SK_BACKEND_ADDON_ID, frontendData );
    currdataBackend = aBackend.get( BACKEND_CURRDATA_ID, IS5BackendCurrDataSingleton.class );
    histdataBackend = aBackend.get( BACKEND_HISTDATA_ID, IS5BackendHistDataSingleton.class );
    commandsBackend = aBackend.get( BACKEND_COMMANDS_ID, IS5BackendCommandSingleton.class );
    eventsBackend = aBackend.get( BACKEND_EVENTS_ID, IS5BackendEventSingleton.class );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendRealtime
  //
  @Override
  public IIntMap<Gwid> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    // Текущий набор чтения текущих данных сессии
    S5DatasetSupport currdata = frontendData.readCurrdata;
    if( aToRemove != null && aToRemove.size() == 0 && aToAdd.size() == 0 ) {
      // Клиент сделал пустой по смыслу запрос
      return currdata.dataset();
    }
    // Реконфигурация набора
    IIntMap<Gwid> retValue = currdataBackend.configureCurrDataReader( frontend(), aToRemove, aToAdd );
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      StringBuilder sb = new StringBuilder();
      for( int index : retValue.keys() ) {
        sb.append( String.format( "   %d = %s\n", Integer.valueOf( index ), retValue.getByKey( index ) ) ); //$NON-NLS-1$
      }
      logger().debug( MSG_CD_READ_TABLE, sb.toString() );
    }
    return retValue;
  }

  @Override
  public IIntMap<Gwid> configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    // Текущий набор записи текущих данных сессии
    S5DatasetSupport currdata = frontendData.writeCurrdata;
    if( aToRemove != null && aToRemove.size() == 0 && aToAdd.size() == 0 ) {
      // Клиент сделал пустой по смыслу запрос
      return currdata.dataset();
    }
    IIntMap<Gwid> retValue = currdataBackend.configureCurrDataWriter( frontend(), aToRemove, aToAdd );
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      StringBuilder sb = new StringBuilder();
      for( int index : retValue.keys() ) {
        sb.append( String.format( "   %d = %s\n", Integer.valueOf( index ), retValue.getByKey( index ) ) ); //$NON-NLS-1$
      }
      logger().debug( MSG_CD_WRITE_TABLE, sb.toString() );
    }
    return retValue;
  }

  @Override
  public void writeCurrData( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в лог сохраняемых данных
      // Текущий набор записи текущих данных сессии
      S5DatasetSupport currdata = frontendData.writeCurrdata;
      logger().debug( S5RealtimeUtils.toStr( MSG_TRANSMIT_VALUES, currdata.dataset(), aValues ) );
    }
    currdataBackend.writeCurrData( aValues );
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    histdataWriteSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @Override
  public String execHistData( IQueryInterval aQueryInterval, IGwidList aGwids, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryInterval, aGwids, aParams );
    // Формирование идентификатора
    String queryId = histdataBackend.uuidGenerator().nextId();
    // Запуск выполнения запроса
    histdataBackend.execHistData( frontend(), queryId, aQueryInterval, aGwids, aParams );
    return queryId;
  }

  @Override
  public void cancelHistDataResult( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    histdataBackend.cancelHistDataResult( aQueryId );
  }

  @Override
  public void fireEvents( ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    eventsBackend.fireEvents( frontend(), aEvents );
  }

  @Override
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    return eventsBackend.queryEvents( aInterval, aNeededGwids );
  }

  @Override
  public void setExcutableCommandGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    commandsBackend.setExecutableCommandGwids( frontend(), aNeededGwids );
  }

  @Override
  public IDpuCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Идентификатор новой команды
    String cmdId = S5CommandIdGenerator.INSTANCE.nextId();
    // Формирование команды
    IDpuCommand cmd = new DpuCommand( currTime, cmdId, aCmdGwid, aAuthorSkid, aArgs );
    // Передача команды на исполнение
    commandsBackend.sendCommand( frontend(), cmd );
    // Возвращение результата
    return cmd;
  }

  @Override
  public void changeCommandState( DpuCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    commandsBackend.changeCommandState( frontend(), aStateChangeInfo );
  }

  @Override
  public ITimedList<IDpuCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    return commandsBackend.queryCommands( aInterval, aNeededGwids );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // Попытка передачи накопленных исторических данных. aForce = false;
    histdataWriteSupport.doJob( this, false );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение IS5HistDataSequenceWriter
  //
  @Override
  public void write( DpuWriteHistData aHistData ) {
    TsNullArgumentRtException.checkNull( aHistData );
    histdataBackend.writeHistData( aHistData );
  }
}
