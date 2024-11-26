package org.toxsoft.uskat.s5.server.backend.supports.core;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.core.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5RegisteredConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Параметры подсистемы
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5BackendCoreConstants
    extends IS5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  String SYBSYSTEM_ID_PREFIX = "uskat.backend.s5.server";

  /**
   * Минимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}.
   */
  IDataDef OPDEF_START_TIME_MIN = register( SYBSYSTEM_ID_PREFIX + ".startTimeMin", INTEGER, //
      TSID_NAME, STR_START_TIME_MIN, //
      TSID_DESCRIPTION, STR_START_TIME_MIN_D, //
      TSID_DEFAULT_VALUE, avInt( 10 ), //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Максимальное время (секунды) в котором сервер находится в режиме {@link ES5ServerMode#STARTING}.
   * <p>
   * Сервер может быть досрочно переведен в режим {@link ES5ServerMode#WORKING} если после запуска сервера прошло больше
   * времени чем {@link #OPDEF_START_TIME_MIN} и текущий уровень загрузки меньше {@link #OPDEF_BOOSTED_AVERAGE}.
   * <p>
   * Если с момента запуска прошло больше времени чем {@link #OPDEF_START_TIME_MAX}, то сервер переводится в режим
   * {@link ES5ServerMode#BOOSTED} или {@link ES5ServerMode#OVERLOADED} в завимости от текущего уровня загрузки.
   */
  IDataDef OPDEF_START_TIME_MAX = register( SYBSYSTEM_ID_PREFIX + ".startTimeMax", INTEGER, //
      TSID_NAME, STR_START_TIME_MAX, //
      TSID_DESCRIPTION, STR_START_TIME_MAX_D, //
      TSID_DEFAULT_VALUE, avInt( 600 ), //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Уровень загрузки при котором s5-сервер может быть автоматически переключен в усиленный(форсаж) режим (
   * {@link ES5ServerMode#BOOSTED}).
   */
  IDataDef OPDEF_BOOSTED_AVERAGE = register( SYBSYSTEM_ID_PREFIX + ".boosted.average", FLOATING, //
      TSID_NAME, STR_BOOSTED_AVERAGE, //
      TSID_DESCRIPTION, STR_BOOSTED_AVERAGE_D, //
      TSID_DEFAULT_VALUE, avFloat( 0.7f ), //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Уровень загрузки при котором s5-сервер может быть автоматически переключен в режим перегрузки
   * ({@link ES5ServerMode#OVERLOADED}).
   */
  IDataDef OPDEF_OVERLOADED_AVERAGE = register( SYBSYSTEM_ID_PREFIX + ".overloaded.average", FLOATING, //
      TSID_NAME, STR_OVERLOADED_AVERAGE, //
      TSID_DESCRIPTION, STR_OVERLOADED_AVERAGE_D, //
      TSID_DEFAULT_VALUE, avFloat( 3.0f ), //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Все параметры подсистемы.
   */
  IStridablesList<IDataDef> ALL_CORE_OPDEFS = new StridablesList<>( //
      OPDEF_START_TIME_MIN, //
      OPDEF_START_TIME_MAX, //
      OPDEF_BOOSTED_AVERAGE, //
      OPDEF_OVERLOADED_AVERAGE //
  );

}
