package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.EQueryIntervalType;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.concurrent.S5SynchronizedConnection;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.connection.ISkConnectionListener;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

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
  private static S5SynchronizedConnection connection = null;

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Карта каналов чтения текущих данных открытых в фоновом режиме
   */
  private static final IMapEdit<ISkConnection, IMapEdit<Gwid, ISkReadCurrDataChannel>> backgroundChannels =
      new ElemMap<>();

  /**
   * Карта каналов готовых для чтения текущих данных открытых в фоновом режиме
   */
  private static final IMapEdit<ISkConnection, IListEdit<Gwid>> backgroundReadyChannels = new ElemMap<>();

  /**
   * Слушатель изменений значений currdata
   */
  private static final ISkCurrDataChangeListener currdataListener = aRtdMap -> {
    String time = TimeUtils.timestampToString( System.currentTimeMillis() );
    for( Gwid gwid : aRtdMap.keys() ) {
      IAtomicValue value = aRtdMap.getByKey( gwid );
      print( '\n' + MSG_CMD_READ_VALUE, time, gwid, value );
    }
    // Ожидание значений созданных каналов
    synchronized (backgroundChannels) {
      if( connection == null ) {
        return;
      }
      IListEdit<Gwid> channels = backgroundReadyChannels.findByKey( connection );
      if( channels == null ) {
        channels = new ElemArrayList<>( false );
        backgroundReadyChannels.put( connection, channels );
      }
      channels.addAll( aRtdMap.keys() );
      // Оповещение об изменении в таблице открытых каналов
      backgroundChannels.notifyAll();
    }

  };

  /**
   * Слушатель изменений состояния соединений
   */
  private static final ISkConnectionListener connectionListener = ( aSource, aOldState ) -> {
    backgroundChannels.removeByKey( aSource );
    backgroundReadyChannels.removeByKey( aSource );
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
    connection = (S5SynchronizedConnection)argSingleRef( CTX_SK_CONNECTION );
    try {
      connection.addConnectionListener( connectionListener );
      callback = aCallback;
      ISkCoreApi coreApi = connection.coreApi();
      String classId = argSingleValue( ARG_CLASSID ).asString();
      String objStrid = argSingleValue( ARG_STRID ).asString();
      String dataId = argSingleValue( ARG_DATAID ).asString();
      if( objStrid.equals( EMPTY_STRING ) ) {
        objStrid = MULTI;
      }
      if( dataId.equals( EMPTY_STRING ) ) {
        dataId = MULTI;
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
      currdata.eventer().addListener( currdataListener );
      // Каналы уже открытые на текущем соединении
      IMapEdit<Gwid, ISkReadCurrDataChannel> channels = backgroundChannels.findByKey( connection );
      if( channels == null ) {
        channels = new ElemMap<>();
        backgroundChannels.put( connection, channels );
        backgroundReadyChannels.put( connection, new ElemArrayList<>( false ) );
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
          backgroundChannels.removeByKey( connection );
          backgroundReadyChannels.removeByKey( connection );
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
          try {
            query.genericChangeEventer().addListener( new AdminHistDataQueryChangeListener( query, callback ) );
            query.prepare( new GwidList( gwids ) );
            synchronized (query) {
              EQueryIntervalType rt = EQueryIntervalType.findById( readType.asString() );
              long st = readStartTime.asLong();
              long et = readEndTime.asLong();
              query.exec( new QueryInterval( rt, st, et ) );
              query.wait( readTimeout.asLong() );
            }
          }
          finally {
            query.close();
          }
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
        lockWrite( connection.getLock() );
        try {
          // Создание новых каналов (добавление в карту backgroundChannels проводится в onCurrData(...)
          newChannels = currdata.createReadCurrDataChannels( newChannelGwids );
        }
        finally {
          unlockWrite( connection.getLock() );
        }
        // Ожидание значений
        if( !waitValues( newChannels, readTimeout.asLong() ) ) {
          // Завершение работы созданных каналов
          for( ISkReadCurrDataChannel channel : newChannels ) {
            channel.close();
          }
          // Завершение ожидания значений по таймауту
          addResultError( ERR_CMD_READ_TIMEOUT );
          resultFail();
          return;
        }
        // Добавление вновь добавленных каналов в кэша
        channels.putAll( newChannels );
        // Готовые каналы соединения
        IListEdit<Gwid> readyChannelIds = backgroundReadyChannels.getByKey( connection );
        // Вывод текущих значений
        for( Gwid gwid : gwids ) {
          ISkReadCurrDataChannel channel = channels.getByKey( gwid );
          // Вывод текущего значения канала
          value = channel.getValue();
          addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, gwid, value ); //$NON-NLS-1$
          if( close ) {
            channel.close();
            channels.removeByKey( gwid );
            readyChannelIds.remove( gwid );
          }
        }
        if( close ) {
          // Вызов с пустым списком "заставит" фронтенд отказаться от подписки на данные в бекенде
          currdata.createReadCurrDataChannels( IGwidList.EMPTY );
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
    finally {
      connection = null;
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
    if( aArgId.equals( ARG_READ_TYPE.id() ) ) {
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue atomicValue = avStr( MULTI );
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
   * Ожидание значений каналов
   *
   * @param aNewChannels {@link IMap}&lt; {@link Gwid}, {@link ISkReadCurrDataChannel}&gt; карта новых открытых каналов
   * @param aTimeout long время (мсек) ожидания данных
   * @return boolean <b>true</b> значение получено;<b>false</b> значение не получено
   * @throws InterruptedException прерывание ожидания
   */
  private static boolean waitValues( IMap<Gwid, ISkReadCurrDataChannel> aNewChannels, long aTimeout )
      throws InterruptedException {
    TsNullArgumentRtException.checkNull( aNewChannels );
    synchronized (backgroundChannels) {
      IList<Gwid> readyChannelIds = backgroundReadyChannels.getByKey( connection );
      IMapEdit<Gwid, ISkReadCurrDataChannel> newChannels = backgroundChannels.getByKey( connection );
      if( newChannels == null ) {
        newChannels = new ElemMap<>();
        backgroundChannels.put( connection, newChannels );
      }
      for( Gwid gwid : aNewChannels.keys() ) {
        if( !readyChannelIds.hasElem( gwid ) ) {
          // Канал еще не получил значение, ожидание
          backgroundChannels.wait( aTimeout );
          // Проверка получения значения
          if( !readyChannelIds.hasElem( gwid ) ) {
            return false;
          }
          newChannels.put( gwid, aNewChannels.getByKey( gwid ) );
        }
      }
    }
    return true;
  }

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
