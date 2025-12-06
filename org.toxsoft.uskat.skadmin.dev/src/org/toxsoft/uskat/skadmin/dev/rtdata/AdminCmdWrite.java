package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда s5admin: Сохраняет в системе значение текущего данного
 *
 * @author mvk
 */
public class AdminCmdWrite
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdWrite() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // Идентификатор данного
    addArg( ARG_DATAID );
    // Новое значение для данного
    addArg( ARG_WRITE_VALUE );
    // Требование сохранить как текущее значение
    addArg( ARG_WRITE_CURRDATA );
    // Требование сохранить как хранимое значение
    addArg( ARG_WRITE_HISTDATA );
    // Метка времени для сохранения значений хранимых данных
    addArg( ARG_WRITE_TIMESTAMP );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_WRITE_ID;
  }

  @Override
  public String alias() {
    return CMD_WRITE_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_WRITE_NAME;
  }

  @Override
  public String description() {
    return CMD_WRITE_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    String classId = argSingleValue( ARG_CLASSID ).asString();
    String stridArg = argSingleValue( ARG_STRID ).asString();
    String dataIdArg = argSingleValue( ARG_DATAID ).asString();
    if( stridArg.equals( EMPTY_STRING ) ) {
      stridArg = STR_MULTI_ID;
    }
    String strid = stridArg;
    if( dataIdArg.equals( EMPTY_STRING ) ) {
      dataIdArg = STR_MULTI_ID;
    }
    String dataId = dataIdArg;
    if( classId.equals( EMPTY_STRING ) ) {
      // Нет команды
      resultOk();
      return;
    }
    // IAtomicValue value = AtomicValueKeeper.KEEPER.fromStr( argSingleValue( ARG_WRITE_VALUE ).asString() );
    IAtomicValue value = argSingleValue( ARG_WRITE_VALUE );
    IAtomicValue writeCurrdata = argSingleValue( ARG_WRITE_CURRDATA );
    IAtomicValue writeHistdata = argSingleValue( ARG_WRITE_HISTDATA );
    IAtomicValue writeTimestamp = argSingleValue( ARG_WRITE_TIMESTAMP );
    ISkRtdataService currdata = coreApi.rtdService();
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( coreApi );

    // Время начала записи
    long startTime = System.currentTimeMillis();
    // Время в текстовом виде
    String time = TimeUtils.timestampToString( startTime );

    try {
      // Запись текущих данных
      if( !writeCurrdata.isAssigned() || writeCurrdata.asBool() ) {
        // Получение идентификаторов текущих данных. currdata = true, histdata = false
        IGwidList gwids = getDataGwids( coreApi, classId, strid, dataId, true, false );
        // Запрос к серверу
        threadExecutor.syncExec( () -> {
          // Создание каналов
          IMap<Gwid, ISkWriteCurrDataChannel> channels = currdata.createWriteCurrDataChannels( gwids );
          try {
            addResultInfo( '\n' + MSG_CMD_WRITE_CURRDATA, Integer.valueOf( gwids.size() ) );
            for( Gwid gwid : gwids ) {
              channels.getByKey( gwid ).setValue( value );
              addResultInfo( '\n' + MSG_CMD_WRITE_VALUE, time, gwid, value );
            }
            addResultInfo( "\n" ); //$NON-NLS-1$
          }
          finally {
            for( ISkWriteCurrDataChannel channel : channels.values() ) {
              channel.close();
            }
          }
        } );
      }

      // Запись хранимых данных
      if( !writeHistdata.isAssigned() || writeHistdata.asBool() ) {
        // Получение идентификаторов хранимых данных. currdata = false, histdata = true
        IGwidList gwids = getDataGwids( coreApi, classId, strid, dataId, false, true );
        // Запрос к серверу
        threadExecutor.syncExec( () -> {
          // Создание каналов
          IMap<Gwid, ISkWriteHistDataChannel> channels = currdata.createWriteHistDataChannels( gwids );
          long timestamp = (writeTimestamp.isAssigned() ? writeTimestamp.asLong() : startTime);
          ITimeInterval interval = new TimeInterval( timestamp, timestamp );
          ITimedList<ITemporalAtomicValue> values = new TimedList<>( new TemporalAtomicValue( timestamp, value ) );
          try {
            addResultInfo( '\n' + MSG_CMD_WRITE_HISTDATA, Integer.valueOf( gwids.size() ) );
            for( Gwid gwid : gwids ) {
              channels.getByKey( gwid ).writeValues( interval, values );
              addResultInfo( '\n' + MSG_CMD_WRITE_VALUE, TimeUtils.timestampToString( timestamp ), gwid, value );
            }
            addResultInfo( "\n" ); //$NON-NLS-1$
          }
          finally {
            for( ISkWriteHistDataChannel channel : channels.values() ) {
              channel.close();
            }
          }
        } );
      }
      long delta = (System.currentTimeMillis() - startTime);
      addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
      resultOk();
    }
    catch( Throwable e ) {
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    IListEdit<IPlexyValue> retValues = new ElemArrayList<>();
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( pxCoreApi.singleRef() );
    // Определение допускаемых значений
    threadExecutor.syncExec( () -> {
      ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
      ISkSysdescr sysdescr = coreApi.sysdescr();
      ISkObjectService objService = coreApi.objService();
      if( aArgId.equals( ARG_CLASSID.id() ) ) {
        // Список всех классов
        IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
        // Подготовка списка возможных значений
        for( int index = 0, n = classInfos.size(); index < n; index++ ) {
          IAtomicValue atomicValue = avStr( classInfos.get( index ).id() );
          IPlexyValue plexyValue = pvSingleValue( atomicValue );
          retValues.add( plexyValue );
        }
      }
      if( (aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() )) ) {
        // Идентификатор класса
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        // Список всех объектов с учетом наследников
        ISkidList objList = objService.listSkids( classId, true );
        // Значение '*'
        IAtomicValue atomicValue = avStr( STR_MULTI_ID );
        IPlexyValue plexyValue = pvSingleValue( atomicValue );
        retValues.add( plexyValue );
        for( int index = 0, n = objList.size(); index < n; index++ ) {
          atomicValue = avStr( objList.get( index ).strid() );
          plexyValue = pvSingleValue( atomicValue );
          retValues.add( plexyValue );
        }
      }
      if( aArgId.equals( ARG_DATAID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
        if( classInfo == null ) {
          return;
        }
        IStridablesList<IDtoRtdataInfo> rtdInfos = classInfo.rtdata().list();
        // Значение '*'
        IAtomicValue atomicValue = avStr( STR_MULTI_ID );
        IPlexyValue plexyValue = pvSingleValue( atomicValue );
        retValues.add( plexyValue );
        for( IDtoRtdataInfo rtdInfo : rtdInfos ) {
          if( !rtdInfo.isCurr() ) {
            continue;
          }
          atomicValue = avStr( rtdInfo.id() );
          plexyValue = pvSingleValue( atomicValue );
          retValues.add( plexyValue );
        }
      }
    } );
    return retValues;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
