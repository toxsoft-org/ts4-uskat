package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда s5admin: Чтение значения текущего данного
 *
 * @author mvk
 */
public class AdminCmdRead
    extends AbstractAdminCmd {

  /**
   * Соединение выполняемой команды
   */
  private static ISkConnection connection = null;

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Карта каналов чтения текущих данных открытых в фоновом режиме
   */
  private static final IMapEdit<ISkConnection, IMapEdit<Gwid, ISkReadCurrDataChannel>> cdChannels = new ElemMap<>();

  /**
   * Карта каналов готовых для чтения текущих данных
   */
  private static final IMapEdit<ISkConnection, IListEdit<Gwid>> cdGwids = new ElemMap<>();

  /**
   * Слушатель изменений значений currdata
   */
  private static final ISkCurrDataChangeListener cdListener = aRtdMap -> {
    String time = TimeUtils.timestampToString( System.currentTimeMillis() );
    for( Gwid gwid : aRtdMap.keys() ) {
      IAtomicValue value = aRtdMap.getByKey( gwid );
      print( '\n' + MSG_CMD_READ_VALUE, time, gwid, value );
    }
    // Ожидание значений созданных каналов
    synchronized (cdChannels) {
      if( connection == null ) {
        return;
      }
      IListEdit<Gwid> channels = cdGwids.findByKey( connection );
      if( channels == null ) {
        channels = new ElemArrayList<>( false );
        cdGwids.put( connection, channels );
      }
      channels.addAll( aRtdMap.keys() );
      // Оповещение об изменении в таблице открытых каналов
      cdChannels.notifyAll();
    }

  };

  /**
   * Слушатель изменений состояния соединений
   */
  private static final ISkConnectionListener connectionListener = ( aSource, aOldState ) -> {
    cdChannels.removeByKey( aSource );
    cdGwids.removeByKey( aSource );
  };

  /**
   * Конструктор
   */
  public AdminCmdRead() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // Идентификатор данного
    addArg( ARG_DATAID );
    // Требование закрыть указанные каналы чтения или все если не указан класс объектов
    addArg( ARG_READ_CLOSE );
    // Метка времени начала чтения хранимых данных
    addArg( ARG_READ_START_TIME );
    // Тип интервала чтения данных
    addArg( ARG_READ_TYPE );
    // Метка времени завершения чтения хранимых данных
    addArg( ARG_READ_END_TIME );
    // Максимальное время ожидания данных
    addArg( ARG_READ_TIMEOUT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_READ_ID;
  }

  @Override
  public String alias() {
    return CMD_READ_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_READ_NAME;
  }

  @Override
  public String description() {
    return CMD_READ_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return CTX_SK_ATOMIC_VALUE.type();
  }

  @Override
  public String resultDescription() {
    return CTX_SK_ATOMIC_VALUE.description();
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    params.add( CTX_SK_ATOMIC_VALUE );
    return params;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    connection = (ISkConnection)argSingleRef( CTX_SK_CONNECTION );
    connection.addConnectionListener( connectionListener );
    callback = aCallback;
    ISkCoreApi coreApi = connection.coreApi();
    String classId = argSingleValue( ARG_CLASSID ).asString();
    String objStrid = argSingleValue( ARG_STRID ).asString();
    String dataId = argSingleValue( ARG_DATAID ).asString();
    if( objStrid.equals( EMPTY_STRING ) ) {
      objStrid = STR_MULTI_ID;
    }
    if( dataId.equals( EMPTY_STRING ) ) {
      dataId = STR_MULTI_ID;
    }
    boolean close = argSingleValue( ARG_READ_CLOSE ).asBool();
    IAtomicValue readStartTime = argSingleValue( ARG_READ_START_TIME );
    IAtomicValue readEndTime = argSingleValue( ARG_READ_END_TIME );
    IAtomicValue readType = argSingleValue( ARG_READ_TYPE );
    IAtomicValue readTimeout = argSingleValue( ARG_READ_TIMEOUT );
    if( !readTimeout.isAssigned() ) {
      readTimeout = avInt( 10000 );
    }
    // Служба текущих данных
    ISkRtdataService currdata = coreApi.rtdService();
    // Регистрация(перегистрация слушателя) текущих данных
    currdata.eventer().addListener( cdListener );
    // Каналы уже открытые на текущем соединении
    IMapEdit<Gwid, ISkReadCurrDataChannel> channels = cdChannels.findByKey( connection );
    if( channels == null ) {
      channels = new ElemMap<>();
      cdChannels.put( connection, channels );
      cdGwids.put( connection, new ElemArrayList<>( false ) );
    }
    try {
      if( classId.equals( EMPTY_STRING ) ) {
        if( !close ) {
          // Нет команды
          IPlexyValue pxValue = pvSingleRef( IAtomicValue.NULL );
          setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
          resultOk( pxValue );
          return;
        }
        // Закрываются все каналы
        long startTime = System.currentTimeMillis();
        // Время в текстовом виде
        String time = TimeUtils.timestampToString( startTime );
        // Значение текущего данного прочитанное из последнего канала
        IAtomicValue value = IAtomicValue.NULL;
        // Чтение каналов
        addResultInfo( "\n" + MSG_CMD_READ, time, Integer.valueOf( channels.size() ) ); //$NON-NLS-1$
        for( ISkReadCurrDataChannel channel : channels ) {
          value = channel.getValue();
          addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, channel.gwid(), value ); //$NON-NLS-1$
          channel.close();
        }
        cdChannels.removeByKey( connection );
        cdGwids.removeByKey( connection );
        currdata.eventer().removeListener( cdListener );
        IPlexyValue pxValue = pvSingleRef( value );
        setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
        resultOk( pxValue );
        return;
      }
      // Получение идентификаторов текущих данных. currdata = true, histdata = false
      IList<Gwid> gwids = getDataGwids( coreApi, classId, objStrid, dataId, true, false );

      if( readStartTime.isAssigned() ) {
        if( !readEndTime.isAssigned() ) {
          readEndTime = avTimestamp( System.currentTimeMillis() );
        }
        if( !readType.isAssigned() ) {
          readType = avStr( EQueryIntervalType.CSCE.id() );
        }
        // Чтение хранимых данных
        IOptionSetEdit options = new OptionSet();
        // 2022-09-19 mvk ---
        // options.setValue( ISkRtdataHardConstants.OP_SK_HDQUERY_CHECK_VALID_GWIDS, avBool( false ) );
        options.setValue( ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME, readTimeout );

        // Служба запросов данных
        ISkHistoryQueryService histdata = coreApi.hqService();

        ISkQueryRawHistory query = histdata.createHistoricQuery( options );
        // try {
        query.genericChangeEventer().addListener( new AdminHistDataQueryChangeListener( query, callback ) );
        query.prepare( new GwidList( gwids ) );
        // synchronized (query) {
        EQueryIntervalType rt = EQueryIntervalType.findById( readType.asString() );
        long st = readStartTime.asLong();
        long et = readEndTime.asLong();
        query.exec( new QueryInterval( rt, st, et ) );
        // query.wait( readTimeout.asLong() );
        // }
        // }
        // finally {
        // query.close();
        // }
      }
      // Время начала чтения значений
      long startTime = System.currentTimeMillis();
      // Время в текстовом виде
      String time = TimeUtils.timestampToString( startTime );
      // Значение текущего данного прочитанное из последнего канала
      IAtomicValue value = IAtomicValue.NULL;
      // Создание или чтение каналов
      print( "\n" + (close ? MSG_CMD_READ : MSG_CMD_READ_CREATE), time, Integer.valueOf( gwids.size() ) ); //$NON-NLS-1$
      // Список идентфикаторов создаваемых каналов
      GwidList newChannelGwids = new GwidList();
      for( Gwid gwid : gwids ) {
        ISkReadCurrDataChannel channel = channels.findByKey( gwid );
        // Признак необходимости создать новый канал
        boolean needNewChannel = (channel == null || !channel.isOk());
        if( needNewChannel ) {
          newChannelGwids.add( gwid );
        }
      }
      IMap<Gwid, ISkReadCurrDataChannel> newChannels = null;
      // Создание новых каналов (добавление в карту cdChannels проводится в onCurrData(...)
      newChannels = currdata.createReadCurrDataChannels( newChannelGwids );
      // Добавление вновь добавленных каналов в кэша
      channels.putAll( newChannels );
      // Готовые каналы соединения
      IListEdit<Gwid> readyChannelIds = cdGwids.getByKey( connection );
      // Вывод уже полученных значений
      for( Gwid gwid : gwids ) {
        ISkReadCurrDataChannel channel = channels.getByKey( gwid );
        if( channel == null ) {
          addResultInfo( "\n" + MSG_CMD_NOT_FOUND, time, gwid ); //$NON-NLS-1$
          continue;
        }
        if( !channel.isOk() ) {
          addResultInfo( "\n" + MSG_CMD_NOT_READY, time, gwid ); //$NON-NLS-1$
          continue;
        }
        // Вывод текущего значения канала
        value = channel.getValue();
        addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, gwid, value ); //$NON-NLS-1$
      }

      if( close ) {
        // Завершение работы каналов
        for( Gwid gwid : gwids ) {
          ISkReadCurrDataChannel channel = channels.removeByKey( gwid );
          if( channel != null ) {
            channel.close();
          }
          readyChannelIds.remove( gwid );
        }
        if( channels.size() == 0 ) {
          currdata.eventer().removeListener( cdListener );
        }
      }
      addResultInfo( "\n\n" + MSG_CMD_TIME, Long.valueOf( System.currentTimeMillis() - startTime ) ); //$NON-NLS-1$

      IPlexyValue pxValue = pvSingleRef( value );
      setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
      resultOk( pxValue );
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
      IAtomicValue atomicValue = avStr( STR_MULTI_ID );
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
      IAtomicValue atomicValue = avStr( STR_MULTI_ID );
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
    if( aArgId.equals( ARG_READ_TYPE.id() ) ) {
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue atomicValue = avStr( STR_MULTI_ID );
      IPlexyValue plexyValue = pvSingleValue( atomicValue );
      values.add( plexyValue );
      for( EQueryIntervalType type : EQueryIntervalType.values() ) {
        atomicValue = avStr( type.id() );
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
