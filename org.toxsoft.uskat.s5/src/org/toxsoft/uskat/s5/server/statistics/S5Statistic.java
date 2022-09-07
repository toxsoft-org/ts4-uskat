package org.toxsoft.uskat.s5.server.statistics;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.std.StringKeeper;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListBasicEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.s5.server.statistics.handlers.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Реализация статистической информации о работе сессии пользователя с возможностью обновления
 *
 * @author mvk
 */
public class S5Statistic
    implements IS5StatisticCounter, IS5Statistic, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5Statistic"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5Statistic> KEEPER =
      new AbstractEntityKeeper<>( S5Statistic.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5Statistic aEntity ) {
          // info
          S5StatisticParamInfo.KEEPER.writeColl( aSw, aEntity.infos, true );
          aSw.writeSeparatorChar();
          // keys
          // aIndented = false
          StringKeeper.KEEPER.writeColl( aSw, aEntity.handlers.keys(), false );
          aSw.writeSeparatorChar();
          // values
          IListEdit<IAtomicValue> values = new ElemArrayList<>( aEntity.handlers.keys().size() );
          for( String key : aEntity.handlers.keys() ) {
            values.add( aEntity.handlers.getByKey( key ).value() );
          }
          // aIndented = false
          AtomicValueKeeper.KEEPER.writeColl( aSw, values, false );
        }

        @Override
        protected S5Statistic doRead( IStrioReader aSr ) {
          // info
          IStridablesList<S5StatisticParamInfo> infos =
              new StridablesList<>( S5StatisticParamInfo.KEEPER.readColl( aSr ) );
          aSr.ensureSeparatorChar();
          // keys
          IStringList keys = new StringArrayList( StringKeeper.KEEPER.readColl( aSr ) );
          aSr.ensureSeparatorChar();
          // values
          IList<IAtomicValue> values = AtomicValueKeeper.KEEPER.readColl( aSr );
          IStringMapEdit<IAtomicValue> initValues = new StringMap<>();
          for( int index = 0, n = keys.size(); index < n; index++ ) {
            initValues.put( keys.get( index ), values.get( index ) );
          }
          S5Statistic retValue = new S5Statistic( infos, initValues );
          return retValue;
        }
      };

  private final IStridablesList<S5StatisticParamInfo> infos;
  private final IStringMap<S5StatisticHandler>        handlers;
  private long                                        updateTime;
  private transient S5Lockable                        lock;

  /**
   * Конструктор
   *
   * @param aInfos {@link IStridablesList} список описаний параметров статистики
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5Statistic( IStridablesList<S5StatisticParamInfo> aInfos ) {
    this( aInfos, IStringMap.EMPTY );
  }

  /**
   * Конструктор
   *
   * @param aInfos {@link IStridablesList} список описаний параметров статистики
   * @param aInitValues &lt;{@link IStringMap}&lt;{@link IAtomicValue}&gt; карта значений для инициализации. Пустая
   *          карта: все значения: {@link IAtomicValue#NULL}.
   *          <p>
   *          Ключ: значение формируемое {@link #paramKey(String, IS5StatisticInterval)};<br>
   *          Значение: значение параметра при создании обработчика.<br>
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5Statistic( IStridablesList<S5StatisticParamInfo> aInfos, IStringMap<IAtomicValue> aInitValues ) {
    TsNullArgumentRtException.checkNulls( aInfos, aInitValues );
    infos = new StridablesList<>( aInfos );
    handlers = createHandlers( aInfos, aInitValues );
    updateTime = System.currentTimeMillis();
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Сохранить накопленные данные статистики в указанный редактор и сбросить счетчик в начальное состояние
   *
   * @param aEditor {@link IS5StatisticCounter} редактор в который перемещаются накопленные данные
   * @return boolean <b>true</b> данные были обработаны; <b>false</b> данные не изменились
   * @throws TsNullArgumentRtException аргумент = null
   */
  public boolean write( IS5StatisticCounter aEditor ) {
    TsNullArgumentRtException.checkNull( aEditor );
    lockWrite( lock() );
    try {
      boolean retValue = false;
      for( S5StatisticParamInfo info : infos ) {
        for( IS5StatisticInterval interval : info.intervals() ) {
          String paramKey = paramKey( info.id(), interval );
          S5StatisticHandler handler = handlers.getByKey( paramKey );
          IAtomicValue value = handler.value();
          retValue |= aEditor.onEvent( interval, info.id(), value );
        }
      }
      return retValue;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5StatisticCounter
  //
  @Override
  public boolean onEvent( IStridable aParam, IAtomicValue aValue ) {
    return onEvent( aParam.id(), aValue );
  }

  @Override
  public boolean onEvent( String aParam, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aParam, aValue );
    S5StatisticParamInfo info = infos.getByKey( aParam );
    lockWrite( lock() );
    try {
      boolean retValue = false;
      for( IS5StatisticInterval interval : info.intervals() ) {
        String paramKey = paramKey( info.id(), interval );
        S5StatisticHandler handler = handlers.getByKey( paramKey );
        retValue |= handler.onValue( aValue );
      }
      return retValue;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  @Override
  public boolean onEvent( IS5StatisticInterval aInterval, String aParam, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aInterval, aParam, aValue );
    lockWrite( lock() );
    try {
      String paramKey = paramKey( aParam, aInterval );
      S5StatisticHandler handler = handlers.getByKey( paramKey );
      boolean retValue = handler.onValue( aValue );
      return retValue;
    }
    finally {
      unlockWrite( lock() );
    }
  }

  @Override
  public boolean update() {
    IListEdit<S5StatisticHandler> updatedHandlers = new ElemLinkedList<>();
    lockWrite( lock() );
    try {
      for( S5StatisticHandler handler : handlers ) {
        if( handler.update() != IAtomicValue.NULL ) {
          updatedHandlers.add( handler );
        }
      }
    }
    finally {
      unlockWrite( lock() );
    }
    if( updatedHandlers.size() == 0 ) {
      return false;
    }
    for( S5StatisticHandler handler : updatedHandlers ) {
      doOnStatValue( handler.id(), handler.interval(), handler.value() );
    }
    updateTime = System.currentTimeMillis();
    return true;
  }

  @Override
  public long updateTime() {
    return updateTime;
  }

  @Override
  public void reset() {
    lockWrite( lock() );
    try {
      for( S5StatisticHandler handler : handlers ) {
        handler.reset();
      }
      updateTime = System.currentTimeMillis();
    }
    finally {
      unlockWrite( lock() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5Statistic
  //
  @Override
  public IStridablesList<IS5StatisticInterval> intervals() {
    lockRead( lock() );
    try {
      IStridablesListBasicEdit<IS5StatisticInterval> retValue = new StridablesList<>();
      for( S5StatisticParamInfo info : infos ) {
        for( IS5StatisticInterval interval : info.intervals() ) {
          retValue.add( interval );
        }
      }
      return retValue;
    }
    finally {
      unlockRead( lock() );
    }
  }

  @Override
  public IOptionSet params( IS5StatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    lockRead( lock() );
    try {
      IOptionSetEdit retValue = new OptionSet();
      for( S5StatisticParamInfo info : infos ) {
        String paramKey = paramKey( info.id(), aInterval );
        S5StatisticHandler handler = handlers.findByKey( paramKey );
        if( handler == null ) {
          // По запрашиваемому интервалу нет значений
          continue;
        }
        handler.update();
        // Нельзя взять результат update() так как он возращает значения только в конце интервала
        retValue.setValue( info.id(), handler.value() );
      }
      return retValue;
    }
    finally {
      unlockRead( lock() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные методы для реализации наследниками
  //
  /**
   * Вызывается после формирования нового значения статистики
   *
   * @param aId String идентификатор параметра
   * @param aInterval {@link IS5StatisticInterval} интервал за который было сформировано значение
   * @param aValue {@link IAtomicValue} сформированное значение
   */
  protected void doOnStatValue( String aId, IS5StatisticInterval aInterval, IAtomicValue aValue ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Создает обработчики статистических параметров по интервалам
   *
   * @param aParamDefs {@link IStridablesList}&lt;{@link S5StatisticParamInfo}&gt; список описаний параметров
   * @param aInitValues &lt;{@link IStringMap}&lt;{@link IAtomicValue}&gt; карта значений для инициализации. Пустая
   *          карта: все значения: {@link IAtomicValue#NULL}.
   *          <p>
   *          Ключ: значение формируемое {@link #paramKey(String, IS5StatisticInterval)};<br>
   *          Значение: значение параметра при создании обработчика.<br>
   * @return &lt;{@link IStringMap}&lt;&lt;{@link S5StatisticHandler}&gt обработчики параметров.
   *         <p>
   *         Ключ: значение формируемое {@link #paramKey(String, IS5StatisticInterval)};<br>
   *         Значение: обработчик параметра.<br>
   */
  private static IStringMap<S5StatisticHandler> createHandlers( IStridablesList<S5StatisticParamInfo> aParamDefs,
      IStringMap<IAtomicValue> aInitValues ) {
    TsNullArgumentRtException.checkNulls( aParamDefs );
    IStringMapEdit<S5StatisticHandler> retValue = new StringMap<>();
    for( int index = 0, n = aParamDefs.size(); index < n; index++ ) {
      S5StatisticParamInfo info = aParamDefs.get( index );
      for( IS5StatisticInterval interval : info.intervals() ) {
        // Ключ доступа к параметр
        String paramKey = paramKey( info.id(), interval );
        // Значение по умолчанию
        IAtomicValue initValue = (aInitValues.size() > 0 ? aInitValues.getByKey( paramKey ) : IAtomicValue.NULL);
        // Если нет значения инициализации, то возвращаем значение по умолчанию
        if( !initValue.isAssigned() ) {
          initValue = info.params().getValue( TSID_DEFAULT_VALUE );
        }
        S5StatisticHandler handler = null;
        handler = switch( info.func() ) {
          case SUMMA -> new S5StatisticSummator( info.id(), info.atomicType(), interval, initValue );
          case FIRST -> new S5StatisticFirstKeeper( info.id(), info.atomicType(), interval, initValue );
          case LAST -> new S5StatisticLastKeeper( info.id(), info.atomicType(), interval, initValue );
          case AVERAGE -> new S5StatisticAveragator( info.id(), info.atomicType(), interval, initValue );
          case COUNT -> new S5StatisticCounter( info.id(), info.atomicType(), interval, initValue );
          case MAX -> new S5StatisticMaximumSeeker( info.id(), info.atomicType(), interval, initValue );
          case MIN -> new S5StatisticMinimumSeeker( info.id(), info.atomicType(), interval, initValue );
          default -> throw new TsNotAllEnumsUsedRtException();
        };
        handler.reset();
        retValue.put( paramKey, handler );
      }
    }
    return retValue;
  }

  /**
   * Возвращает ключ для доступа к обработчику параметра в {@link #handlers}
   *
   * @param aId String идентификатор параметра
   * @param aInterval {@link IS5StatisticInterval} интервал статистической обработки
   * @return String ключ доступа к обработчику параметра в {@link #handlers}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String paramKey( String aId, IS5StatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aId, aInterval );
    return aId + '#' + aInterval.id();
  }

  /**
   * Возвращает блокировку к данным статистики
   *
   * @return {@link S5Lockable} блокировка данных
   */
  private synchronized S5Lockable lock() {
    if( lock == null ) {
      lock = new S5Lockable();
    }
    return lock;
  }
}
