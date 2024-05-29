package org.toxsoft.uskat.s5.server.statistics;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Статистика параметров сохраняемая в системе
 *
 * @author mvk
 */
public class S5StatisticWriter
    implements IS5StatisticCounter {

  /**
   * Синхронизатор API
   */
  private final ITsThreadExecutor synchronizer;

  /**
   * Статистика
   */
  private final S5Statistic stat;

  /**
   * Карта каналов записи значений данных статистики
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: пара каналов записи
   */
  private final IStringMapEdit<Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel>> writeChannels =
      new StringMap<>();

  /**
   * Блокировки доступа к картам каналов записи {@link #writeChannels}
   */
  private final S5Lockable channelsLock = new S5Lockable();

  /**
   * Конструктор
   *
   * @param aConnection {@link ISkConnection} соединение используемое для сохранения статистики
   * @param aStatisticObjId {@link Skid} идентификатор объекта в котором сохраняются данные
   * @param aInfos {@link IStridablesList} список описаний параметров статистики
   * @throws TsNullArgumentRtException аргумент= null
   */
  @SuppressWarnings( "serial" )
  public S5StatisticWriter( ISkConnection aConnection, Skid aStatisticObjId,
      IStridablesList<S5StatisticParamInfo> aInfos ) {
    TsNullArgumentRtException.checkNulls( aConnection, aStatisticObjId );
    synchronizer = (ITsThreadExecutor)aConnection.coreApi().services().getByKey( SkThreadExecutorService.SERVICE_ID );
    stat = new S5Statistic( aInfos ) {

      @Override
      protected void doOnStatValue( String aId, IS5StatisticInterval aInterval, IAtomicValue aValue ) {
        writeValue( aId, aInterval, aValue );
      }

    };
    // Проверка определения данных статистики в объекте
    createStatIfNeed( aConnection, aStatisticObjId, aInfos );
    // Создание каналов записи данных
    writeChannels.putAll( createChannels( aConnection, aStatisticObjId, aInfos ) );
  }

  // ------------------------------------------------------------------------------------
  // IS5StatisticCounter
  //
  @Override
  public boolean isClosed() {
    return stat.isClosed();
  }

  @Override
  public boolean onEvent( IStridable aParam, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aParam, aValue );
    return onEvent( aParam.id(), aValue );
  }

  @Override
  public boolean onEvent( String aParam, IAtomicValue aValue ) {
    if( !stat.onEvent( aParam, aValue ) ) {
      return false;
    }
    writeValue( aParam, EStatisticInterval.ALL, aValue );
    return true;
  }

  @Override
  public boolean onEvent( IS5StatisticInterval aInterval, String aParam, IAtomicValue aValue ) {
    if( !stat.onEvent( aInterval, aParam, aValue ) ) {
      return false;
    }
    writeValue( aParam, aInterval, aValue );
    return true;
  }

  @Override
  public boolean update() {
    return stat.update();
  }

  @Override
  public long updateTime() {
    return stat.updateTime();
  }

  @Override
  public void reset() {
    stat.reset();
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    lockWrite( channelsLock );
    try {
      stat.close();
      for( Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel> channels : writeChannels ) {
        channels.left().close();
        channels.right().close();
      }
      writeChannels.clear();
    }
    finally {
      unlockWrite( channelsLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Сохранение значения статистического параметра в системе
   *
   * @param aParam String имя параметра
   * @param aInterval {@link IS5StatisticInterval} интервал статистики
   * @param aValue {@link IAtomicValue} значение параметра
   */
  private void writeValue( String aParam, IS5StatisticInterval aInterval, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aInterval, aParam, aValue );
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Канал записи значений данного
    Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel> channels = findChannels( aInterval, aParam );
    if( channels == null ) {
      // Не найдены каналы для данного. Например, когда интервал ALL не используется в статистике
      return;
    }
    ITimeInterval interval = new TimeInterval( currTime, currTime );
    ITimedList<ITemporalAtomicValue> values = new TimedList<>( new TemporalAtomicValue( currTime, aValue ) );
    // Запись значений в соединение
    synchronizer.syncExec( () -> {
      // Запись текущих данных
      channels.left().setValue( aValue );
      // Запись хранимых данных
      channels.right().writeValues( interval, values );
    } );
  }

  /**
   * Возвращает пару каналов для записи текущих и хранимых значений укзанного данного
   *
   * @param aInterval {@link IS5StatisticInterval} интервал статистики
   * @param aParam String имя параметра
   * @return {@link Pair}&lt; {@link ISkWriteCurrDataChannel}, {@link ISkWriteHistDataChannel}&gt; пара каналов записи
   *         данных. null: для указанного параметра не созданы каналы
   * @throws TsNullArgumentRtException аргумент = null
   */
  private Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel> findChannels( IS5StatisticInterval aInterval,
      String aParam ) {
    TsNullArgumentRtException.checkNulls( aInterval, aParam );
    // Имя параметра для интервала
    String param = getDataId( aInterval, aParam );
    // Попытка получить уже ранее открытый канал
    lockRead( channelsLock );
    try {
      Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel> retValue = writeChannels.findByKey( param );
      return retValue;
    }
    finally {
      unlockRead( channelsLock );
    }
  }

  /**
   * Проверяет, если необходимо добавляет в класс указанного объекта, указанные параметры статистики
   *
   * @param aConnection {@link ISkConnection} соединение используемое для сохранения статистики
   * @param aStatisticObjId {@link Skid} идентификатор объекта в котором сохраняются данные
   * @param aInfos {@link IStridablesList} список описаний параметров статистики
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createStatIfNeed( ISkConnection aConnection, Skid aStatisticObjId,
      IStridablesList<S5StatisticParamInfo> aInfos ) {
    TsNullArgumentRtException.checkNulls( aConnection, aStatisticObjId, aInfos );
    ISkSysdescr cim = aConnection.coreApi().sysdescr();
    ISkClassInfo classInfo = cim.getClassInfo( aStatisticObjId.classId() );
    ISkClassInfo parentClassInfo = cim.getClassInfo( classInfo.parentId() );
    IStridablesList<IDtoRtdataInfo> rtInfos = classInfo.rtdata().list();
    // Список описаний данных которые необходимо добавить в класс
    IListEdit<IDtoRtdataInfo> needDataInfos = new ElemLinkedList<>();
    for( S5StatisticParamInfo info : aInfos ) {
      for( IS5StatisticInterval interval : info.intervals() ) {
        String dataId = getDataId( interval, info.id() );
        if( !rtInfos.hasKey( dataId ) ) {
          // Описание базового данного
          IDtoRtdataInfo dataInfo = DtoRtdataInfo.create1( //
              dataId, new DataType( info.atomicType() ), true, true, true, interval.milli(), //
              OptionSetUtils.createOpSet( //
                  DDEF_NAME, info.nmName() + '(' + interval.nmName() + ')', //
                  DDEF_DESCRIPTION, info.description() + '(' + info.description() + ')' ) //
          );
          needDataInfos.add( dataInfo );
        }
      }
    }
    if( needDataInfos.size() > 0 ) {
      // Добавление новых данных в класс
      DtoClassInfo dtoClassInfo = dtoClassInfo( parentClassInfo, classInfo );
      dtoClassInfo.rtdataInfos().addAll( needDataInfos );
      cim.defineClass( dtoClassInfo );
    }
  }

  /**
   * Проверяет, если необходимо добавляет в класс указанного объекта, указанные параметры статистики
   *
   * @param aConnection {@link ISkConnection} соединение используемое для сохранения статистики
   * @param aStatisticObjId {@link Skid} идентификатор объекта в котором сохраняются данные
   * @param aInfos {@link IStridablesList} список описаний параметров статистики
   * @return {@link IStringMap}&lt;Pai возвращение aInfos
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IStringMap<Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel>> createChannels(
      ISkConnection aConnection, Skid aStatisticObjId, IStridablesList<S5StatisticParamInfo> aInfos ) {
    TsNullArgumentRtException.checkNulls( aConnection, aStatisticObjId, aInfos );
    // Список идентификаторов создаваемых каналов
    GwidList gwids = new GwidList();
    for( S5StatisticParamInfo info : aInfos ) {
      for( IS5StatisticInterval interval : info.intervals() ) {
        String dataId = getDataId( interval, info.id() );
        gwids.add( Gwid.createRtdata( aStatisticObjId.classId(), aStatisticObjId.strid(), dataId ) );
      }
    }
    if( gwids.size() == 0 ) {
      // Нет каналов
      return IStringMap.EMPTY;
    }
    ISkRtdataService rtService = aConnection.coreApi().rtdService();
    IMap<Gwid, ISkWriteCurrDataChannel> cdChannels = rtService.createWriteCurrDataChannels( gwids );
    IMap<Gwid, ISkWriteHistDataChannel> hdChannels = rtService.createWriteHistDataChannels( gwids );
    IStringMapEdit<Pair<ISkWriteCurrDataChannel, ISkWriteHistDataChannel>> retValue = new StringMap<>();
    for( Gwid gwid : cdChannels.keys() ) {
      ISkWriteCurrDataChannel cdChannel = cdChannels.getByKey( gwid );
      ISkWriteHistDataChannel hdChannel = hdChannels.getByKey( gwid );
      retValue.put( gwid.propId(), new Pair<>( cdChannel, hdChannel ) );
    }
    return retValue;
  }

  /**
   * Возвращает описание класса в формате DTO
   *
   * @param aParentClassInfo {@link ISkClassInfo} описание родительского класса
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @return {@link DtoClassInfo} описание класса в формате DTO
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static DtoClassInfo dtoClassInfo( ISkClassInfo aParentClassInfo, ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aParentClassInfo, aClassInfo );
    DtoClassInfo retValue = new DtoClassInfo( aClassInfo.id(), aClassInfo.parentId(), IOptionSet.NULL );
    for( IDtoAttrInfo info : aClassInfo.attrs().list() ) {
      if( !aParentClassInfo.attrs().list().hasKey( info.id() ) ) {
        retValue.attrInfos().add( info );
      }
    }
    for( IDtoRivetInfo info : aClassInfo.rivets().list() ) {
      if( !aParentClassInfo.rivets().list().hasKey( info.id() ) ) {
        retValue.rivetInfos().add( info );
      }
    }
    for( IDtoLinkInfo info : aClassInfo.links().list() ) {
      if( !aParentClassInfo.links().list().hasKey( info.id() ) ) {
        retValue.linkInfos().add( info );
      }
    }
    for( IDtoRtdataInfo info : aClassInfo.rtdata().list() ) {
      if( !aParentClassInfo.rtdata().list().hasKey( info.id() ) ) {
        retValue.rtdataInfos().add( info );
      }
    }
    for( IDtoCmdInfo info : aClassInfo.cmds().list() ) {
      if( !aParentClassInfo.cmds().list().hasKey( info.id() ) ) {
        retValue.cmdInfos().add( info );
      }
    }
    for( IDtoEventInfo info : aClassInfo.events().list() ) {
      if( !aParentClassInfo.events().list().hasKey( info.id() ) ) {
        retValue.eventInfos().add( info );
      }
    }
    for( IDtoClobInfo info : aClassInfo.clobs().list() ) {
      if( !aParentClassInfo.clobs().list().hasKey( info.id() ) ) {
        retValue.clobInfos().add( info );
      }
    }
    return retValue;
  }

  /**
   * Возвращает идентификатора данного (текущего, хранимого) в классе для параметра с указанным интервалом
   *
   * @param aInterval {@link IS5StatisticInterval} интервал статистики
   * @param aParam String имя параметра
   * @return String идентификатор данного
   */
  @SuppressWarnings( "nls" )
  private static String getDataId( IS5StatisticInterval aInterval, String aParam ) {
    return (aParam + "." + aInterval.id());
  }
}
