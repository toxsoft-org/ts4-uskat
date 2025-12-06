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
import org.toxsoft.core.tslib.bricks.threadexec.*;
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
import org.toxsoft.uskat.core.impl.*;
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
    ISkRtdataService currdata = coreApi.rtdService();

    IAtomicValue classId = argSingleValue( ARG_CLASSID );
    String strid = argSingleValue( ARG_STRID ).asString();
    String dataId = argSingleValue( ARG_DATAID ).asString();
    if( strid.equals( EMPTY_STRING ) ) {
      strid = STR_MULTI_ID;
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
    // Каналы уже открытые на текущем соединении
    final IMapEdit<Gwid, ISkReadCurrDataChannel> channels = getConnectionChannels();
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( coreApi );
    // Регистрация(перегистрация слушателя) текущих данных
    threadExecutor.syncExec( () -> currdata.eventer().addListener( cdListener ) );
    try {
      // Если нет класса объектов и close = true, то выводятся значения всех открытых каналов и после они закрываются
      if( !classId.isAssigned() ) {
        if( !close ) {
          // Нет команды
          IPlexyValue pxValue = pvSingleRef( IAtomicValue.NULL );
          setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
          resultOk( pxValue );
          return;
        }
        long startTime = System.currentTimeMillis();
        // Время в текстовом виде
        String time = TimeUtils.timestampToString( startTime );
        // Чтение каналов
        addResultInfo( "\n" + MSG_CMD_READ, time, Integer.valueOf( channels.size() ) ); //$NON-NLS-1$
        // Чтение и вывод текущих значений
        threadExecutor.syncExec( () -> {
          // Значение текущего данного прочитанное из последнего канала
          IAtomicValue value = IAtomicValue.NULL;
          for( ISkReadCurrDataChannel channel : channels ) {
            value = channel.getValue();
            addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, channel.gwid(), value ); //$NON-NLS-1$
            channel.close();
          }
          cdChannels.removeByKey( connection );
          cdGwids.removeByKey( connection );
          currdata.eventer().removeListener( cdListener );
        } );
        IPlexyValue pxValue = pvSingleRef( IAtomicValue.NULL );
        setContextParamValue( CTX_SK_ATOMIC_VALUE, pxValue );
        resultOk( pxValue );
      }
      // Получение идентификаторов текущих данных. currdata = true, histdata = false
      IList<Gwid> gwids = getDataGwids( coreApi, classId.asString(), strid, dataId, true, false );
      // Чтение хранимых данных
      if( readStartTime.isAssigned() ) {
        // Чтение хранимых данных
        IOptionSetEdit options = new OptionSet();
        // 2022-09-19 mvk ---
        // options.setValue( ISkRtdataHardConstants.OP_SK_HDQUERY_CHECK_VALID_GWIDS, avBool( false ) );
        options.setValue( ISkHistoryQueryServiceConstants.OP_SK_MAX_EXECUTION_TIME, readTimeout );

        // Служба запросов данных
        ISkHistoryQueryService histdata = coreApi.hqService();
        // Запрос хранимых данных у сервера
        threadExecutor.syncExec( () -> {
          ISkQueryRawHistory query = histdata.createHistoricQuery( options );
          // try {
          query.genericChangeEventer().addListener( new AdminHistDataQueryChangeListener( query, callback ) );
          query.prepare( new GwidList( gwids ) );
          // synchronized (query) {
          IAtomicValue ret = readEndTime;
          if( !ret.isAssigned() ) {
            ret = avTimestamp( System.currentTimeMillis() );
          }
          IAtomicValue rt = readType;
          if( !rt.isAssigned() ) {
            rt = avStr( EQueryIntervalType.CSCE.id() );
          }
          EQueryIntervalType type = EQueryIntervalType.findById( rt.asString() );
          long st = readStartTime.asLong();
          long et = readEndTime.asLong();
          query.exec( new QueryInterval( type, st, et ) );
        } );
        // TODO:
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
      // Запрос к серверу на создание текущих данных и вывод их значений
      threadExecutor.syncExec( () -> {
        // Создание новых каналов (добавление в карту cdChannels проводится в onCurrData(...)
        channels.putAll( currdata.createReadCurrDataChannels( newChannelGwids ) );
        // Готовые каналы соединения
        IListEdit<Gwid> readyChannelIds = cdGwids.getByKey( connection );
        // Вывод уже полученных значений
        for( Gwid gwid : gwids ) {
          ISkReadCurrDataChannel channel = channels.findByKey( gwid );
          if( channel == null ) {
            addResultInfo( "\n" + MSG_CMD_NOT_FOUND, time, gwid ); //$NON-NLS-1$
            continue;
          }
          if( !channel.isOk() ) {
            addResultInfo( "\n" + MSG_CMD_NOT_READY, time, gwid ); //$NON-NLS-1$
            continue;
          }
          // Вывод текущего значения канала
          addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, gwid, channel.getValue() ); //$NON-NLS-1$
        }
        // Завершение работы каналов по требованию
        if( close ) {
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
      } );
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
      if( aArgId.equals( ARG_READ_TYPE.id() ) ) {
        // Значение '*'
        IAtomicValue atomicValue = avStr( STR_MULTI_ID );
        IPlexyValue plexyValue = pvSingleValue( atomicValue );
        retValues.add( plexyValue );
        for( EQueryIntervalType type : EQueryIntervalType.values() ) {
          atomicValue = avStr( type.id() );
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
  private static IMapEdit<Gwid, ISkReadCurrDataChannel> getConnectionChannels() {
    IMapEdit<Gwid, ISkReadCurrDataChannel> channels = cdChannels.findByKey( connection );
    if( channels == null ) {
      channels = new ElemMap<>();
      cdChannels.put( connection, channels );
      cdGwids.put( connection, new ElemArrayList<>( false ) );
    }
    return channels;
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
