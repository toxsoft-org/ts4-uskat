package org.toxsoft.uskat.s5.server.backend.supports.core;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.core.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Параметры конфигурации подсистемы
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5BackendCoreConfig
    extends S5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = ISkHardConstants.USKAT_FULL_ID + ".backend.s5.server";

  /**
   * Минимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}.
   */
  public static final IDataDef CORE_START_TIME_MIN = register( SYBSYSTEM_ID_PREFIX + ".startTimeMin", INTEGER, //
      TSID_NAME, STR_START_TIME_MIN, //
      TSID_DESCRIPTION, STR_START_TIME_MIN_D, //
      TSID_DEFAULT_VALUE, avInt( 10 ), //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Максимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}.
   * <p>
   * Сервер может быть досрочно переведен в режим {@link ES5ServerMode#WORKING} если после запуска сервера прошло больше
   * времени чем {@link #CORE_START_TIME_MIN} и текущий уровень загрузки меньше {@link #CORE_BOOSTED_AVERAGE}.
   * <p>
   * Если с момента запуска прошло больше времени чем {@link #CORE_START_TIME_MAX}, то сервер переводится в режим
   * {@link ES5ServerMode#BOOSTED} или {@link ES5ServerMode#OVERLOADED} в завимости от текущего уровня загрузки.
   */
  public static final IDataDef CORE_START_TIME_MAX = register( SYBSYSTEM_ID_PREFIX + ".startTimeMax", INTEGER, //
      TSID_NAME, STR_START_TIME_MAX, //
      TSID_DESCRIPTION, STR_START_TIME_MAX_D, //
      TSID_DEFAULT_VALUE, avInt( 600 ), //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Уровень загрузки при котором s5-сервер может быть автоматически переключен в усиленный(форсаж) режим (
   * {@link ES5ServerMode#BOOSTED}).
   */
  public static final IDataDef CORE_BOOSTED_AVERAGE = register( SYBSYSTEM_ID_PREFIX + ".boosted.average", FLOATING, //
      TSID_NAME, STR_BOOSTED_AVERAGE, //
      TSID_DESCRIPTION, STR_BOOSTED_AVERAGE_D, //
      TSID_DEFAULT_VALUE, avFloat( 0.7f ), //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Уровень загрузки при котором s5-сервер может быть автоматически переключен в режим перегрузки
   * ({@link ES5ServerMode#OVERLOADED}).
   */
  public static final IDataDef CORE_OVERLOADED_AVERAGE =
      register( SYBSYSTEM_ID_PREFIX + ".overloaded.average", FLOATING, //
          TSID_NAME, STR_OVERLOADED_AVERAGE, //
          TSID_DESCRIPTION, STR_OVERLOADED_AVERAGE_D, //
          TSID_DEFAULT_VALUE, avFloat( 3.0f ), //
          TSID_IS_MANDATORY, AV_FALSE //
      );

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_CORE_OPDEFS = new StridablesList<>( //
      CORE_START_TIME_MIN, //
      CORE_START_TIME_MAX, //
      CORE_BOOSTED_AVERAGE, //
      CORE_OVERLOADED_AVERAGE //
  );

}
