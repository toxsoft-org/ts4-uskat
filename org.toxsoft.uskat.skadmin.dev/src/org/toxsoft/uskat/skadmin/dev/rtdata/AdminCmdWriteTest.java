package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда s5admin: Сохраняет в системе значение текущего данного
 *
 * @author mvk
 */
public class AdminCmdWriteTest
    extends AbstractAdminCmd {

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdWriteTest() {
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
    // Метка времени для сохранения значений хранимых данных
    addArg( ARG_WRITE_TIMEOUT );
    // Количество передач значений
    addArg( ARG_WRITE_COUNT );
    // Прирост значения при каждой передачи
    addArg( ARG_WRITE_INCREMENT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_WRITE_TEST_ID;
  }

  @Override
  public String alias() {
    return CMD_WRITE_TEST_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_WRITE_TEST_NAME;
  }

  @Override
  public String description() {
    return CMD_WRITE_TEST_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @SuppressWarnings( { "nls", "boxing" } )
  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    callback = aCallback;
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
    IAtomicValue writeTimeout = argSingleValue( ARG_WRITE_TIMEOUT );
    IAtomicValue writeCount = argSingleValue( ARG_WRITE_COUNT );
    IAtomicValue writeIncrement = argSingleValue( ARG_WRITE_INCREMENT );
    ISkRtdataService currdata = coreApi.rtdService();

    long timeout = (writeTimeout.isAssigned() ? writeTimeout.asLong() : 0);
    int count = (writeCount.isAssigned() ? writeCount.asInt() : 1);
    int increment = (writeIncrement.isAssigned() ? writeIncrement.asInt() : 1);

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
          print( '\n' + MSG_CMD_WRITE_CURRDATA, Integer.valueOf( gwids.size() ) );
          for( int index = 0; index < count; index++ ) {
            Gwid writedLastGwid = null;
            IAtomicValue writedLastValue = null;
            int writed = 0;
            for( Gwid gwid : gwids ) {
              try {
                channels.getByKey( gwid ).setValue( value );
                writedLastGwid = gwid;
                writedLastValue = value;
                writed++;
              }
              catch( @SuppressWarnings( "unused" ) AvTypeCastRtException e ) {
                continue;
              }
              switch( value.atomicType() ) {
                case FLOATING:
                  value = avFloat( value.asFloat() + increment );
                  break;
                case INTEGER:
                  value = avInt( value.asInt() + increment );
                  break;
                case TIMESTAMP:
                  value = avTimestamp( value.asLong() + increment );
                  break;
                case BOOLEAN:
                case NONE:
                case STRING:
                case VALOBJ:
                  break;
                default:
                  throw new TsNotAllEnumsUsedRtException();
              }
            }
            if( writedLastGwid != null ) {
              String g = writedLastGwid.toString() + (gwids.size() > 2 ? ",...(" + writed + ")" : EMPTY_STRING);
              print( "\n[%d] " + MSG_CMD_WRITE_VALUE, index, time, g, writedLastValue );
            }
            if( timeout > 0 ) {
              Thread.sleep( timeout );
            }
          }
          print( "\n" ); //$NON-NLS-1$
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
          print( '\n' + MSG_CMD_WRITE_HISTDATA, Integer.valueOf( gwids.size() ) );
          for( int index = 0; index < count; index++ ) {
            Gwid writedLastGwid = null;
            int writed = 0;
            for( Gwid gwid : gwids ) {
              try {
                channels.getByKey( gwid ).writeValues( interval, values );
              }
              catch( @SuppressWarnings( "unused" ) AvTypeCastRtException e ) {
                continue;
              }
              switch( value.atomicType() ) {
                case FLOATING:
                  value = avFloat( value.asFloat() + increment );
                  writedLastGwid = gwid;
                  writed++;
                  break;
                case INTEGER:
                  value = avInt( value.asInt() + increment );
                  writedLastGwid = gwid;
                  writed++;
                  break;
                case TIMESTAMP:
                  value = avTimestamp( value.asLong() + increment );
                  writedLastGwid = gwid;
                  writed++;
                  break;
                case BOOLEAN:
                case NONE:
                case STRING:
                case VALOBJ:
                  break;
                default:
                  throw new TsNotAllEnumsUsedRtException();
              }
            }
            if( writedLastGwid != null ) {
              String g = writedLastGwid.toString() + (gwids.size() > 2 ? ",...(" + writed + ")" : EMPTY_STRING);
              print( "\n[%d] " + MSG_CMD_WRITE_VALUE, index, time, g, value );
            }
            if( timeout > 0 ) {
              Thread.sleep( timeout );
            }
          }
          print( "\n" ); //$NON-NLS-1$
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
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
      for( int index = 0, n = classInfos.size(); index < n; index++ ) {
        IAtomicValue atomicValue = avStr( classInfos.get( index ).id() );
        IPlexyValue plexyValue = pvSingleValue( atomicValue );
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
      IAtomicValue atomicValue = avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( atomicValue );
      values.add( plexyValue );
      for( int index = 0, n = objList.size(); index < n; index++ ) {
        atomicValue = avStr( objList.get( index ).strid() );
        plexyValue = pvSingleValue( atomicValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( aArgId.equals( ARG_DATAID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<IDtoRtdataInfo> rtdInfos = classInfo.rtdata().list();
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue atomicValue = avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( atomicValue );
      values.add( plexyValue );
      for( IDtoRtdataInfo rtdInfo : rtdInfos ) {
        if( !rtdInfo.isCurr() ) {
          continue;
        }
        atomicValue = avStr( rtdInfo.id() );
        plexyValue = pvSingleValue( atomicValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }
}
