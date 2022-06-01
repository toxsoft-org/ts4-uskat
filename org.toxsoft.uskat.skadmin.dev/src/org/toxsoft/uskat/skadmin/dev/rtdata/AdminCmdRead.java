package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.AdminCurrdataUtils.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
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
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.rtdata.*;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.core.connection.ISkConnectionListener;

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
  private static final IMapEdit<ISkConnection, IMapEdit<Gwid, ISkReadCurrDataChannel>> backgroundChannels =
      new ElemMap<>();

  /**
   * Слушатель изменений значений currdata
   */
  private static final ISkCurrDataChangeListener currdataListener = aRtdMap -> {
    String time = TimeUtils.timestampToString( System.currentTimeMillis() );
    if( connection == null ) {
      for( Gwid gwid1 : aRtdMap.keys() ) {
        ISkReadCurrDataChannel channel1 = aRtdMap.getByKey( gwid1 );
        print( '\n' + MSG_CMD_READ_VALUE, time, gwid1, channel1.getValue() );
      }
      return;
    }
    // Ожидание значений созданных каналов
    synchronized (backgroundChannels) {
      IMapEdit<Gwid, ISkReadCurrDataChannel> channels = backgroundChannels.findByKey( connection );
      for( Gwid gwid2 : aRtdMap.keys() ) {
        ISkReadCurrDataChannel channel2 = aRtdMap.getByKey( gwid2 );
        if( channels == null ) {
          channels = new ElemMap<>();
          backgroundChannels.put( connection, channels );
        }
        channels.put( gwid2, channel2 );
      }
      // Оповещение об изменении в таблице открытых каналов
      backgroundChannels.notifyAll();
    }

  };

  /**
   * Слушатель изменений состояния соединений
   */
  private static final ISkConnectionListener connectionListener =
      ( aSource, aOldState ) -> backgroundChannels.removeByKey( aSource );

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
    connection = argSingleRef( CTX_SK_CONNECTION );
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
      ISkRtDataService currdata = coreApi.rtDataService();
      // Регистрация(перегистрация слушателя) текущих данных
      currdata.eventer().addListener( currdataListener );
      // Каналы уже открытые на текущем соединении
      IMapEdit<Gwid, ISkReadCurrDataChannel> channels = backgroundChannels.findByKey( connection );
      if( channels == null ) {
        channels = new ElemMap<>();
        backgroundChannels.put( connection, channels );
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
          options.setValue( ISkRtdataHardConstants.OP_SK_HDQUERY_CHECK_VALID_GWIDS, avBool( false ) );
          options.setValue( ISkRtdataHardConstants.OP_SK_HDQUERY_MAX_EXECUTION_TIME, readTimeout );
          ISkHistDataQuery query = currdata.createQuery( options );
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
        connection.mainLock().writeLock().lock();
        try {
          // Создание новых каналов (добавление в карту backgroundChannels проводится в onCurrData(...)
          newChannels = currdata.createReadCurrDataChannels( newChannelGwids );
        }
        finally {
          connection.mainLock().writeLock().unlock();
        }
        // Ожидание значений
        if( !waitValues( gwids, readTimeout.asLong() ) ) {
          // Завершение работы созданных каналов
          for( ISkReadCurrDataChannel channel : newChannels ) {
            channel.close();
          }
          // Завершение ожидания значений по таймауту
          addResultError( ERR_CMD_READ_TIMEOUT );
          resultFail();
          return;
        }
        for( Gwid gwid : gwids ) {
          ISkReadCurrDataChannel channel = channels.findByKey( gwid );
          if( channel == null ) {
            continue;
          }
          // Вывод текущего значения канала
          value = channel.getValue();
          addResultInfo( "\n" + MSG_CMD_READ_VALUE, time, gwid, value ); //$NON-NLS-1$
          if( close ) {
            channel.close();
            channels.removeByKey( gwid );
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
    if( aArgId.equals( ARG_READ_TYPE.id() ) ) {
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue dataValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( EQueryIntervalType type : EQueryIntervalType.values() ) {
        dataValue = AvUtils.avStr( type.id() );
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
  /**
   * Ожидание значений каналов
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов каналов
   * @param aTimeout long время (мсек) ожидания данных
   * @return boolean <b>true</b> значение получено;<b>false</b> значение не получено
   * @throws InterruptedException прерывание ожидания
   */
  private static boolean waitValues( IList<Gwid> aGwids, long aTimeout )
      throws InterruptedException {
    TsNullArgumentRtException.checkNull( aGwids );
    synchronized (backgroundChannels) {
      for( Gwid gwid : aGwids ) {
        IMap<Gwid, ISkReadCurrDataChannel> channels = backgroundChannels.getByKey( connection );
        if( !channels.hasKey( gwid ) ) {
          // Канал еще не получил значение, ожидание
          backgroundChannels.wait( aTimeout );
          // Проверка получения значения
          if( !channels.hasKey( gwid ) ) {
            return false;
          }
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
