package org.toxsoft.uskat.s5.server.statistics;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static ru.uskat.common.dpu.impl.IDpuHardConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.common.dpu.IDpuSdRtdataInfo;
import ru.uskat.common.dpu.impl.*;
import ru.uskat.core.api.rtdata.*;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.connection.ISkConnection;

/**
 * Статистика параметров сохраняемая в системе
 *
 * @author mvk
 */
public class S5StatisticWriter
    implements IS5StatisticCounter, ICloseable {

  /**
   * Соединение
   */
  private final ISkConnection connection;

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
    connection = aConnection;
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

  @SuppressWarnings( "deprecation" )
  @Override
  public boolean update() {
    if( stat.update() ) {
      connection.coreApi().rtDataService().writeCurrValues();
      return true;
    }
    return false;
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
    tryLockWrite( channelsLock );
    try {
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
    // Запись текущих данных
    channels.left().setValue( aValue );

    // Запись хранимых данных
    ITimeInterval interval = new TimeInterval( currTime, currTime );
    ITimedList<ITemporalAtomicValue> values = new TimedList<>( new TemporalAtomicValue( currTime, aValue ) );
    channels.right().writeValues( interval, values );
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
    tryLockRead( channelsLock );
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
    ISkClassInfoManager cim = aConnection.coreApi().sysdescr().classInfoManager();
    ISkClassInfo classInfo = cim.getClassInfo( aStatisticObjId.classId() );
    ISkClassInfo parentClassInfo = cim.getClassInfo( classInfo.parentId() );
    IStridablesList<ISkRtdataInfo> rtInfos = classInfo.rtdInfos();
    // Список описаний данных которые необходимо добавить в класс
    IListEdit<IDpuSdRtdataInfo> needDataInfos = new ElemLinkedList<>();
    for( S5StatisticParamInfo info : aInfos ) {
      for( IS5StatisticInterval interval : info.intervals() ) {
        String dataId = getDataId( interval, info.id() );
        if( !rtInfos.hasKey( dataId ) ) {
          // Описание базового данного
          IDpuSdRtdataInfo dataInfo = DpuSdRtdataInfo.create1( //
              dataId, typeId( info.atomicType() ), //
              DDEF_NAME, info.nmName() + '(' + interval.nmName() + ')', //
              DDEF_DESCRIPTION, info.description() + '(' + info.description() + ')', //
              OP_IS_CURR, AV_TRUE, //
              OP_IS_HIST, AV_TRUE, //
              OP_IS_SYNC, AV_TRUE, //
              OP_SYNC_DT, avInt( interval.milli() ) //
          );
          needDataInfos.add( dataInfo );
        }
      }
    }
    if( needDataInfos.size() > 0 ) {
      // Добавление новых данных в класс
      DpuSdClassInfo dpuClassInfo = dpuClassInfo( parentClassInfo, classInfo );
      dpuClassInfo.rtdataInfos().addAll( needDataInfos );
      cim.defineClass( dpuClassInfo );
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
    ISkRtDataService rtService = aConnection.coreApi().rtDataService();
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
   * Возвращает описание класса в формате DPU
   *
   * @param aParentClassInfo {@link ISkClassInfo} описание родительского класса
   * @param aClassInfo {@link ISkClassInfo} описание класса
   * @return {@link DpuSdClassInfo} описание класса в формате DPU
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static DpuSdClassInfo dpuClassInfo( ISkClassInfo aParentClassInfo, ISkClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNulls( aParentClassInfo, aClassInfo );
    DpuSdClassInfo retValue = new DpuSdClassInfo( aClassInfo.id(), aClassInfo.parentId() );
    for( ISkAttrInfo info : aClassInfo.attrInfos() ) {
      if( !aParentClassInfo.attrInfos().hasKey( info.id() ) ) {
        retValue.attrInfos().add( new DpuSdAttrInfo( info.id(), info.params() ) );
      }
    }
    for( ISkLinkInfo info : aClassInfo.linkInfos() ) {
      if( !aParentClassInfo.linkInfos().hasKey( info.id() ) ) {
        retValue.linkInfos().add( new DpuSdLinkInfo( info.id(), info.params() ) );
      }
    }
    for( ISkRtdataInfo info : aClassInfo.rtdInfos() ) {
      if( !aParentClassInfo.rtdInfos().hasKey( info.id() ) ) {
        retValue.rtdataInfos().add( new DpuSdRtdataInfo( info.id(), info.params() ) );
      }
    }
    for( ISkCmdInfo info : aClassInfo.cmdInfos() ) {
      if( !aParentClassInfo.cmdInfos().hasKey( info.id() ) ) {
        retValue.cmdInfos().add( new DpuSdCmdInfo( info.id(), info.params() ) );
      }
    }
    for( ISkEventInfo info : aClassInfo.eventInfos() ) {
      if( !aParentClassInfo.eventInfos().hasKey( info.id() ) ) {
        retValue.eventInfos().add( new DpuSdEventInfo( info.id(), info.params() ) );
      }
    }
    return retValue;
  }

  /**
   * Возвращает идентификатор типа данного по его атомарному типу
   *
   * @param aType {@link EAtomicType} тип данного
   * @return String идентификатор типа
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String typeId( EAtomicType aType ) {
    TsNullArgumentRtException.checkNull( aType );
    return aType.id();
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
