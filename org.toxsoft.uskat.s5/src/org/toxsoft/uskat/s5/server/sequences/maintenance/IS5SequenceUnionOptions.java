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
 * Описание параметров(опций) для команды дефрагментации блоков последовательности
 *
 * @author mvk
 */
public interface IS5SequenceUnionOptions {

  /**
   * Интервал дефрагментации. Если интервал не указан, то процесс автоматически определяет требуемый интервал
   */
  IDataDef UNION_INTERVAL = create( "s5.sequence.union.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_INTERVAL, //
      TSID_DESCRIPTION, D_UNION_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо дефрагментировать. Если список не указан, то все
   * данные
   */
  IDataDef UNION_GWIDS = create( "s5.sequence.union.ids", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_GWIDS, //
      TSID_DESCRIPTION, D_UNION_GWIDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new GwidList() ) );

  /**
   * Отступ (мсек) времени от текущего времени до которого проводится автоматическое дефрагментация блоков
   */
  IDataDef UNION_AUTO_OFFSET = create( "s5.sequence.union.auto.offset", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_UNION_OFFSET, //
      TSID_DESCRIPTION, D_UNION_OFFSET, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_0 );

  /**
   * Максимальное время (мсек) между фрагментированными блоками больше которого производится принудительная
   * дефрагментация (даже если значений недостаточно для формирования полного блока). <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_TIMEOUT = create( "s5.sequence.union.auto.fragment_timeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_UNION_OFFSET, //
      TSID_DESCRIPTION, D_UNION_OFFSET, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( -1 ) );

  /**
   * Минимальное количество фрагментированных блоков больше которого производится принудительная дефрагментация (даже
   * если значений недостаточно для формирования полного блока). <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_COUNT_MIN = create( "s5.sequence.union.auto.fragment_min", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FRAGMENT_COUNT_MIN, //
      TSID_DESCRIPTION, D_FRAGMENT_COUNT_MIN, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5000 ) );

  /**
   * Максимальное количество фрагментированных блоков которое может быть дефрагментировано за один раз. <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_COUNT_MAX = create( "s5.sequence.union.auto.fragment_max", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FRAGMENT_COUNT_MAX, //
      TSID_DESCRIPTION, D_FRAGMENT_COUNT_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5000 ) );

  /**
   * Максимальное количество потоков дефрагментации запускаемых в автоматическом режиме
   */
  IDataDef UNION_AUTO_THREADS_COUNT = create( "s5.sequence.union.auto.threads_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_THREADS_COUNT, //
      TSID_DESCRIPTION, D_THREADS_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_1 );

  /**
   * Максимальное количество проверяемых данных на предмет необходимости дефрагментации за один проход. <= 0: без
   * ограничения
   */
  IDataDef UNION_AUTO_LOOKUP_COUNT = create( "s5.sequence.union.auto.lookup_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_LOOKUP_COUNT, //
      TSID_DESCRIPTION, D_LOOKUP_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 100 ) );
}
