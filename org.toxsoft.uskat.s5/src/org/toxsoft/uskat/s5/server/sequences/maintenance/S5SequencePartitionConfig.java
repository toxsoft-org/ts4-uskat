package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.s5.utils.schedules.*;

/**
 * Описание конфигурации подсистемы управления разделами базы данных (контроль глубины хранения).
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public class S5SequencePartitionConfig {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5DatabaseConfig.SYBSYSTEM_ID_PREFIX + ".partitions";

  /**
   * Календари проведения удаления значений
   */
  public static final IDataDef PARTITION_CALENDARS = create( SYBSYSTEM_ID_PREFIX + ".calendars", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_PARTITION_CALENDARS, //
      TSID_DESCRIPTION, D_PARTITION_CALENDARS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_IS_MANDATORY, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ScheduleExpressionList( //
          S5ScheduleUtils.createSchedule( "*", "*", "*", "*/60" ) ) )//
  );

  /**
   * Интервал удаления разделов. Если интервал {@link ITimeInterval#NULL}, то процесс автоматически определяет требуемый
   * интервал
   */
  public static final IDataDef PARTITION_REMOVE_INTERVAL = create( SYBSYSTEM_ID_PREFIX + ".remove.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_INTERVAL, //
      TSID_DESCRIPTION, D_REMOVE_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_IS_MANDATORY, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список имен таблиц разделы которых будут обработаны. Если список не указан, то все таблицы
   */
  public static final IDataDef PARTITION_TABLES = create( SYBSYSTEM_ID_PREFIX + ".tables", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_FROM_TABLES, //
      TSID_DESCRIPTION, D_REMOVE_FROM_TABLES, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_KEEPER_ID, avStr( StringListKeeper.KEEPER_ID ), //
      TSID_IS_MANDATORY, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY, StringListKeeper.KEEPER, StringListKeeper.KEEPER_ID ) //
  );

  /**
   * Максимальное количество потоков обработки операций над разделами запускаемых в автоматическом режиме. <= 0: без
   * ограничения
   */
  public static final IDataDef PARTITION_AUTO_THREADS_COUNT =
      create( SYBSYSTEM_ID_PREFIX + ".auto.threads_count", INTEGER, //$NON-NLS-1$
          TSID_NAME, N_REMOVE_THREADS_COUNT, //
          TSID_DESCRIPTION, D_REMOVE_THREADS_COUNT, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_N1 );

  /**
   * Максимальное количество проверяемых таблиц на предмет необходимости запуска операции над разделами за один проход.
   * <= 0: без ограничения
   */
  public static final IDataDef PARTITION_AUTO_LOOKUP_COUNT =
      create( SYBSYSTEM_ID_PREFIX + ".auto.lookup_count", INTEGER, //$NON-NLS-1$
          TSID_NAME, N_REMOVE_LOOKUP_COUNT, //
          TSID_DESCRIPTION, D_REMOVE_LOOKUP_COUNT, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_N1 );

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_PARTITION_OPDEFS = new StridablesList<>( //
      PARTITION_CALENDARS, //
      PARTITION_REMOVE_INTERVAL, //
      PARTITION_TABLES, //
      PARTITION_AUTO_THREADS_COUNT, //
      PARTITION_AUTO_LOOKUP_COUNT //
  );
}
