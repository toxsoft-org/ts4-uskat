package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.rtdata.*;
import ru.uskat.core.api.sysdescr.*;

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

  @SuppressWarnings( "deprecation" )
  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    String classId = argSingleValue( ARG_CLASSID ).asString();
    String objStrid = argSingleValue( ARG_STRID ).asString();
    String dataId = argSingleValue( ARG_DATAID ).asString();
    if( objStrid.equals( EMPTY_STRING ) ) {
      objStrid = MULTI;
    }
    if( dataId.equals( EMPTY_STRING ) ) {
      dataId = MULTI;
    }
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
    ISkRtDataService currdata = coreApi.rtDataService();

    // Время начала записи
    long startTime = System.currentTimeMillis();
    // Время в текстовом виде
    String time = TimeUtils.timestampToString( startTime );

    try {
      if( !writeCurrdata.isAssigned() || writeCurrdata.asBool() ) {
        // Получение идентификаторов текущих данных. currdata = true, histdata = false
        IGwidList gwids = getDataGwids( coreApi, classId, objStrid, dataId, true, false );
        // Создание каналов
        IMap<Gwid, ISkWriteCurrDataChannel> channels = currdata.createWriteCurrDataChannels( gwids );
        try {
          addResultInfo( '\n' + MSG_CMD_WRITE_CURRDATA, Integer.valueOf( gwids.size() ) );
          for( Gwid gwid : gwids ) {
            channels.getByKey( gwid ).setValue( value );
            addResultInfo( '\n' + MSG_CMD_WRITE_VALUE, time, gwid, value );
          }
          addResultInfo( "\n" ); //$NON-NLS-1$
          // Непосредственная запись данных на сервер
          currdata.writeCurrValues();
        }
        finally {
          for( ISkWriteCurrDataChannel channel : channels.values() ) {
            channel.close();
          }
        }
      }
      if( !writeHistdata.isAssigned() || writeHistdata.asBool() ) {
        // Получение идентификаторов хранимых данных. currdata = false, histdata = true
        IGwidList gwids = getDataGwids( coreApi, classId, objStrid, dataId, false, true );
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
    ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
    ISkSysdescr sysdescr = coreApi.sysdescr();
    ISkClassInfoManager classManager = sysdescr.classInfoManager();
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = classManager.listClasses();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
      for( int index = 0, n = classInfos.size(); index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( (aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() )) ) {
      // Идентификатор класса
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      // Список всех объектов с учетом наследников
      ISkidList objList = objService.listSkids( classId, true );
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( objList.size() );
      // Значение '*'
      IAtomicValue dataValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( int index = 0, n = objList.size(); index < n; index++ ) {
        dataValue = AvUtils.avStr( objList.get( index ).strid() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( aArgId.equals( ARG_DATAID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      ISkClassInfo classInfo = classManager.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<ISkRtdataInfo> dataInfos = classInfo.rtdInfos();
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue dataValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( ISkRtdataInfo dataInfo : dataInfos ) {
        if( !dataInfo.isCurr() ) {
          continue;
        }
        dataValue = AvUtils.avStr( dataInfo.id() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
