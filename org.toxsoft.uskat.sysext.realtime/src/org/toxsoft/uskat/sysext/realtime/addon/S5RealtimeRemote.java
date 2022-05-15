package org.toxsoft.uskat.sysext.realtime.addon;

import static org.toxsoft.uskat.s5.utils.collections.S5CollectionUtils.*;
import static org.toxsoft.uskat.sysext.realtime.addon.IS5Resources.*;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.impl.S5CommandSupport;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.client.remote.connection.IS5Connection;
import org.toxsoft.uskat.s5.utils.datasets.S5DatasetDecoder;
import org.toxsoft.uskat.sysext.realtime.pas.S5RealtimeCallbackWriteCurrData;
import org.toxsoft.uskat.sysext.realtime.pas.S5RealtimeCallbackWriteHistData;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.IS5HistDataSequenceWriter;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.S5HistDataWriteSupport;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.common.dpu.rt.events.*;
import ru.uskat.core.connection.ISkConnection;

/**
 * Реализация удаленного доступа к расширению backend {@link ISkBackendAddonRealtime} предоставляемое s5-сервером
 *
 * @author mvk
 */
public final class S5RealtimeRemote
    extends S5BackendAddonRemote<IS5RealtimeRemote>
    implements ISkBackendAddonRealtime, IS5HistDataSequenceWriter {

  private static final long serialVersionUID = 157157L;

  private final S5DatasetDecoder       currdataReaderSupport;
  private final S5DatasetDecoder       currdataWriterSupport;
  private final S5HistDataWriteSupport histdataWriterSupport;
  private final S5CommandSupport       commandSupport;
  private volatile IPasTxChannel       txChannel;

  /**
   * Конструктор
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения {@link ISkConnection#open(ITsContextRo)}
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5RealtimeRemote( ITsContextRo aArgs ) {
    super( ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID, //
        STR_D_BACKEND_ADDON_REALTIME, //
        IS5RealtimeRemote.class );
    currdataReaderSupport = new S5DatasetDecoder();
    currdataWriterSupport = new S5DatasetDecoder();
    histdataWriterSupport = new S5HistDataWriteSupport( aArgs );
    commandSupport = new S5CommandSupport();
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendAddonRealtime
  //
  @Override
  public IIntMap<Gwid> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove != null && aToRemove.size() == 0 && aToAdd.size() == 0 ) {
      // Клиент сделал пустой по смыслу запрос
      return currdataReaderSupport.configuration();
    }
    if( !isActive() && aToRemove == null && aToAdd.size() == 0 ) {
      // Частный случай: попытка очистить подписку уже разорванного соединения
      return currdataReaderSupport.configure( IIntMap.EMPTY );
    }
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    IGwidList toRemove = (aToRemove != null ? new GwidList( aToRemove ) : null);
    IGwidList toAdd = new GwidList( aToAdd );
    IIntMap<Gwid> parentConfiguration = remote().configureCurrDataReader( toRemove, toAdd );
    return currdataReaderSupport.configure( parentConfiguration );
  }

  @Override
  public IIntMap<Gwid> configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove != null && aToRemove.size() == 0 && aToAdd.size() == 0 ) {
      // Клиент сделал пустой по смыслу запрос
      return currdataWriterSupport.configuration();
    }
    if( !isActive() && aToRemove == null && aToAdd.size() == 0 ) {
      // Частный случай: попытка очистить подписку уже разорванного соединения
      return currdataWriterSupport.configure( IIntMap.EMPTY );
    }
    // Некоторые типы реализации IList (например, ElemLinkedList) дают сбой сериализации на больших коллекциях
    IGwidList toRemove = (aToRemove != null ? new GwidList( aToRemove ) : null);
    IGwidList toAdd = new GwidList( aToAdd );
    IIntMap<Gwid> parentConfiguration = remote().configureCurrDataWriter( toRemove, toAdd );
    return currdataWriterSupport.configure( parentConfiguration );
  }

  @Override
  public void writeCurrData( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    if( aValues.size() == 0 ) {
      return;
    }
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в лог сохраняемых данных
      logger().debug( S5RealtimeUtils.toStr( MSG_TRANSMIT_VALUES, currdataWriterSupport.configuration(), aValues ) );
    }
    IIntMap<IAtomicValue> parentValues = currdataWriterSupport.childToParent( aValues );
    SkCurrDataValues values = new SkCurrDataValues();
    values.putAll( parentValues );
    S5RealtimeCallbackWriteCurrData.send( owner().connection().callbackTxChannel(), values );
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    histdataWriterSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @Override
  public String execHistData( IQueryInterval aQueryInterval, IGwidList aGwids, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aQueryInterval, aGwids, aParams );
    return remote().execHistData( aQueryInterval, aGwids, aParams );
  }

  @Override
  public void cancelHistDataResult( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    remote().cancelHistDataResult( aQueryId );
  }

  @Override
  public void fireEvents( ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    remote().fireEvents( aEvents );
  }

  @Override
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    return remote().queryEvents( aInterval, aNeededGwids );
  }

  @Override
  public void setExcutableCommandGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNull( aNeededGwids );
    // Если есть связь с сервером, то в начале пытаемся зарегистировать исполнителя команд на сервере, учитывая
    // возможный конфликт исполнителей для одних и тех же команд между разными соединениями
    IS5RealtimeRemote remote = findRemote();
    if( remote != null ) {
      remote.setExcutableCommandGwids( aNeededGwids );
    }
    // Регистрация команд исполнителя
    IValResList resultList = commandSupport.setExcutableCommandGwids( "local", aNeededGwids ); //$NON-NLS-1$
    // Запись в журнал результата установки списка исполняемых команд
    for( ValidationResult result : resultList.results() ) {
      LoggerWrapper.resultToLog( logger(), result );
    }
    // Вывод журнала
    logger().info( MSG_REGISTER_CMD_GWIDS, itemsToString( aNeededGwids ), Boolean.valueOf( remote != null ) );
  }

  @Override
  public IDpuCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    return remote().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
  }

  @Override
  public void changeCommandState( DpuCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    remote().changeCommandState( aStateChangeInfo );
  }

  @Override
  public ITimedList<IDpuCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    return remote().queryCommands( aInterval, aNeededGwids );
  }

  // ------------------------------------------------------------------------------------
  // IS5HistDataSequenceWriter
  //
  @Override
  public void write( DpuWriteHistData aHistData ) {
    // Фактическая передача значений
    S5RealtimeCallbackWriteHistData.send( txChannel, aHistData );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение методов базового класса
  //
  @Override
  protected void doOnAfterDiscover( IS5Connection aSource ) {
    S5RealtimeInitData initData = aSource.sessionInitData().findAddonData( id(), S5RealtimeInitData.class );
    if( initData == null ) {
      initData = new S5RealtimeInitData();
      aSource.sessionInitData().setAddonData( id(), initData );
    }
    initData.readCurrdataGwids.setAll( currdataReaderSupport.dataset() );
    initData.writeCurrdataGwids.setAll( currdataWriterSupport.dataset() );
    initData.commandsGwids.setAll( commandSupport.gwids() );
  }

  @Override
  protected void doOnAfterConnect( IS5Connection aSource ) {
    txChannel = aSource.callbackTxChannel();
    S5RealtimeInitResult initResult = aSource.sessionInitResult().getAddonData( id(), S5RealtimeInitResult.class );
    currdataReaderSupport.configure( initResult.readCurrdataDataset );
    currdataWriterSupport.configure( initResult.writeCurrdataDataset );
  }

  @Override
  protected void doOnAfterDisconnect( IS5Connection aSource ) {
    txChannel = null;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    if( txChannel != null ) {
      // Попытка передачи накопленных исторических данных. aForce = false;
      histdataWriterSupport.doJob( this, false );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
