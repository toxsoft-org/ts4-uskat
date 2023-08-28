package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;

/**
 * Описание параметров(опций) для команды удаления блоков последовательности
 *
 * @author mvk
 */
public interface IS5SequenceRemoveOptions {

  /**
   * Интервал удаления. Если интервал не указан, то процесс автоматически определяет требуемый интервал
   */
  IDataDef REMOVE_INTERVAL = create( "s5.sequence.remove.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_INTERVAL, //
      TSID_DESCRIPTION, D_REMOVE_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо удалять. Если список не указан, то все данные
   */
  IDataDef REMOVE_GWIDS = create( "s5.sequence.remove.ids", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_GWIDS, //
      TSID_DESCRIPTION, D_REMOVE_GWIDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new GwidList() ) );

  /**
   * Максимальное количество потоков удаления данных запускаемых в автоматическом режиме
   */
  IDataDef REMOVE_AUTO_THREADS_COUNT = create( "s5.sequence.remove.auto.threads_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_THREADS_COUNT, //
      TSID_DESCRIPTION, D_REMOVE_THREADS_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_1 );

  /**
   * Максимальное количество проверяемых данных на предмет необходимости удаления за один проход. <= 0: без ограничения
   */
  IDataDef REMOVE_AUTO_LOOKUP_COUNT = create( "s5.sequence.remove.auto.lookup_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_LOOKUP_COUNT, //
      TSID_DESCRIPTION, D_REMOVE_LOOKUP_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 100 ) );
}
