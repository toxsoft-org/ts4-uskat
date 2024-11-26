package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5RegisteredConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.utils.*;
import org.toxsoft.uskat.s5.utils.schedules.*;

/**
 * Описание параметров(опций) для команды дефрагментации блоков последовательности
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5SequenceUnionOptions
    extends IS5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  String SYBSYSTEM_ID_PREFIX = IS5SequenceHardConstants.SYBSYSTEM_ID_PREFIX + ".union";

  /**
   * Календари проведения дефрагментации
   */
  IDataDef UNION_CALENDARS = register( SYBSYSTEM_ID_PREFIX + ".calendars", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_CALENDARS, //
      TSID_DESCRIPTION, D_UNION_CALENDARS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ScheduleExpressionList( //
          // TODO: 2023-11-10 mvkd перегрузка хранимыми данными gubaha-stend
          // S5ScheduleUtils.createSchedule( "*", "*", "*", "*/5" ) ) )//
          S5ScheduleUtils.createSchedule( "*", "*", "*", "*" ) ) )//
  );

  /**
   * Интервал дефрагментации. Если интервал не указан, то процесс автоматически определяет требуемый интервал
   */
  IDataDef UNION_INTERVAL = register( SYBSYSTEM_ID_PREFIX + ".interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_INTERVAL, //
      TSID_DESCRIPTION, D_UNION_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо дефрагментировать. Если список не указан, то все
   * данные
   */
  IDataDef UNION_GWIDS = register( SYBSYSTEM_ID_PREFIX + ".ids", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_GWIDS, //
      TSID_DESCRIPTION, D_UNION_GWIDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new GwidList() ) );

  /**
   * Отступ (мсек) времени от текущего времени до которого проводится автоматическое дефрагментация блоков
   */
  IDataDef UNION_AUTO_OFFSET = register( SYBSYSTEM_ID_PREFIX + ".auto.offset", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_UNION_OFFSET, //
      TSID_DESCRIPTION, D_UNION_OFFSET, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.avInt( 10 * 60 * 1000 ) );

  /**
   * Максимальное время (мсек) между фрагментированными блоками больше которого производится принудительная
   * дефрагментация (даже если значений недостаточно для формирования полного блока). <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_TIMEOUT = register( SYBSYSTEM_ID_PREFIX + ".auto.fragment_timeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_UNION_OFFSET, //
      TSID_DESCRIPTION, D_UNION_OFFSET, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( -1 ) );

  /**
   * Минимальное количество фрагментированных блоков больше которого производится принудительная дефрагментация (даже
   * если значений недостаточно для формирования полного блока). <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_COUNT_MIN = register( SYBSYSTEM_ID_PREFIX + ".auto.fragment_min", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FRAGMENT_COUNT_MIN, //
      TSID_DESCRIPTION, D_FRAGMENT_COUNT_MIN, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5000 ) );

  /**
   * Максимальное количество фрагментированных блоков которое может быть дефрагментировано за один раз. <= 0: Отключено
   */
  IDataDef UNION_AUTO_FRAGMENT_COUNT_MAX = register( SYBSYSTEM_ID_PREFIX + ".auto.fragment_max", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FRAGMENT_COUNT_MAX, //
      TSID_DESCRIPTION, D_FRAGMENT_COUNT_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5000 ) );

  /**
   * Максимальное количество потоков дефрагментации запускаемых в автоматическом режиме
   */
  IDataDef UNION_AUTO_THREADS_COUNT = register( SYBSYSTEM_ID_PREFIX + ".auto.threads_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_THREADS_COUNT, //
      TSID_DESCRIPTION, D_THREADS_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_1 );

  /**
   * Максимальное количество проверяемых данных на предмет необходимости дефрагментации за один проход. <= 0: без
   * ограничения
   */
  IDataDef UNION_AUTO_LOOKUP_COUNT = register( SYBSYSTEM_ID_PREFIX + ".auto.lookup_count", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_LOOKUP_COUNT, //
      TSID_DESCRIPTION, D_LOOKUP_COUNT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 100 ) );

  /**
   * Все параметры подсистемы.
   */
  IStridablesList<IDataDef> ALL_UNION_OPDEFS = new StridablesList<>( //
      UNION_CALENDARS, //
      UNION_INTERVAL, //
      UNION_GWIDS, //
      UNION_AUTO_OFFSET, //
      UNION_AUTO_FRAGMENT_TIMEOUT, //
      UNION_AUTO_FRAGMENT_COUNT_MIN, //
      UNION_AUTO_FRAGMENT_COUNT_MAX, //
      UNION_AUTO_FRAGMENT_COUNT_MAX, //
      UNION_AUTO_THREADS_COUNT, //
      UNION_AUTO_LOOKUP_COUNT //
  );
}
