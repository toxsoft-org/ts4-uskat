package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.keeper.std.StringListKeeper;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;

/**
 * Описание параметров(опций) для команды выполнения операций над разделами
 *
 * @author mvk
 */
public interface IS5SequencePartitionOptions {

  /**
   * Интервал удаления разделов. Если интервал не указан, то процесс автоматически определяет требуемый интервал
   */
  IDataDef REMOVE_INTERVAL = create( "s5.sequence.partition.remove.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_INTERVAL, //
      TSID_DESCRIPTION, D_REMOVE_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список имен таблиц разделы которых будут обработаны. Если список не указан, то все таблицы
   */
  IDataDef TABLES = create( "s5.sequence.partition.tables", VALOBJ, //$NON-NLS-1$
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
  IDataDef AUTO_THREADS_COUNT = create( "s5.sequence.partition.auto.threads_count", INTEGER, //$NON-NLS-1$
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
  IDataDef AUTO_LOOKUP_COUNT = create( "s5.sequence.partition.auto.lookup_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_LOOKUP_COUNT, //
      TSID_DESCRIPTION, D_REMOVE_LOOKUP_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      // 2023-11-18 mvk
      // TSID_DEFAULT_VALUE, avInt( 100 ) );
      TSID_DEFAULT_VALUE, AV_N1 );
}
