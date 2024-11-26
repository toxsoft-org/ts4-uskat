package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5RegisteredConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.utils.*;
import org.toxsoft.uskat.s5.utils.schedules.*;

/**
 * Описание параметров(опций) для команды выполнения операций над разделами
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5SequencePartitionOptions
    extends IS5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  String SYBSYSTEM_ID_PREFIX = IS5SequenceHardConstants.SYBSYSTEM_ID_PREFIX + ".partition";

  /**
   * Календари проведения удаления значений
   */
  IDataDef PARTITION_CALENDARS = register( SYBSYSTEM_ID_PREFIX + ".calendars", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_PARTITION_CALENDARS, //
      TSID_DESCRIPTION, D_PARTITION_CALENDARS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ScheduleExpressionList( //
          S5ScheduleUtils.createSchedule( "*", "*", "*", "*/60" ) ) )//
  );

  /**
   * Интервал удаления разделов. Если интервал не указан, то процесс автоматически определяет требуемый интервал
   */
  IDataDef PARTITION_REMOVE_INTERVAL = register( SYBSYSTEM_ID_PREFIX + ".remove.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_INTERVAL, //
      TSID_DESCRIPTION, D_REMOVE_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список имен таблиц разделы которых будут обработаны. Если список не указан, то все таблицы
   */
  IDataDef PARTITION_TABLES = register( SYBSYSTEM_ID_PREFIX + ".tables", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_FROM_TABLES, //
      TSID_DESCRIPTION, D_REMOVE_FROM_TABLES, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_KEEPER_ID, avStr( StringListKeeper.KEEPER_ID ), //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY, StringListKeeper.KEEPER, StringListKeeper.KEEPER_ID ) //
  );

  /**
   * Максимальное количество потоков обработки операций над разделами запускаемых в автоматическом режиме. <= 0: без
   * ограничения
   */
  IDataDef PARTITION_AUTO_THREADS_COUNT = register( SYBSYSTEM_ID_PREFIX + ".auto.threads_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_THREADS_COUNT, //
      TSID_DESCRIPTION, D_REMOVE_THREADS_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      // 2023-11-18 mvk
      // TSID_DEFAULT_VALUE, AV_1 );
      TSID_DEFAULT_VALUE, AV_N1 );

  /**
   * Максимальное количество проверяемых таблиц на предмет необходимости запуска операции над разделами за один проход.
   * <= 0: без ограничения
   */
  IDataDef PARTITION_AUTO_LOOKUP_COUNT = register( SYBSYSTEM_ID_PREFIX + ".auto.lookup_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_LOOKUP_COUNT, //
      TSID_DESCRIPTION, D_REMOVE_LOOKUP_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      // 2023-11-18 mvk
      // TSID_DEFAULT_VALUE, avInt( 100 ) );
      TSID_DEFAULT_VALUE, AV_N1 );

  /**
   * Все параметры подсистемы.
   */
  IStridablesList<IDataDef> ALL_PARTITION_OPDEFS = new StridablesList<>( //
      PARTITION_CALENDARS, //
      PARTITION_REMOVE_INTERVAL, //
      PARTITION_TABLES, //
      PARTITION_AUTO_THREADS_COUNT, //
      PARTITION_AUTO_LOOKUP_COUNT //
  );
}
