package org.toxsoft.uskat.s5.server.sequences;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.uskat.s5.server.backend.supports.IS5BackendAddonConfig;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.utils.schedules.S5ScheduleExpressionList;
import org.toxsoft.uskat.s5.utils.schedules.S5ScheduleUtils;

/**
 * Конфигурация расширения backend работающего с {@link IS5Sequence}
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5SequenceAddonConfig
    extends IS5BackendAddonConfig, IS5SequenceUnionOptions, IS5SequenceRemoveOptions, IS5SequenceValidationOptions {

  /**
   * Максимальное значение load average (загрузка системы) при котором возможно проведение записи значений
   * последовательностей
   */
  IDataDef WRITE_LOAD_AVERAGE_MAX = create( "s5.sequence.write.load_average_max", FLOATING, //$NON-NLS-1$
      TSID_NAME, N_WRITE_LOAD_AVERAGE_MAX, //
      TSID_DESCRIPTION, D_WRITE_LOAD_AVERAGE_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avFloat( 3.0 ) );

  /**
   * Максимальное значение load average (загрузка системы) при котором возможно проведение дефрагментации значений
   * последовательностей
   */
  IDataDef UNION_LOAD_AVERAGE_MAX = create( "s5.sequence.union.load_average_max", FLOATING, //$NON-NLS-1$
      TSID_NAME, N_UNION_LOAD_AVERAGE_MAX, //
      TSID_DESCRIPTION, D_UNION_LOAD_AVERAGE_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avFloat( 0.7 ) );

  /**
   * Календари проведения дефрагментации
   */
  IDataDef UNION_CALENDARS = create( "s5.sequence.union.calendars", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_UNION_CALENDARS, //
      TSID_DESCRIPTION, D_UNION_CALENDARS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ScheduleExpressionList( //
          S5ScheduleUtils.createSchedule( "*", "*", "*", "*/5" ) ) )//
  );

  /**
   * Максимальное значение load average (загрузка системы) при котором возможно проведение удаление значений
   * последовательностей
   */
  IDataDef REMOVE_LOAD_AVERAGE_MAX = create( "s5.sequence.remove.load_average_max", FLOATING, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_LOAD_AVERAGE_MAX, //
      TSID_DESCRIPTION, D_REMOVE_LOAD_AVERAGE_MAX, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avFloat( 0.7 ) );

  /**
   * Календари проведения удаления значений
   */
  IDataDef REMOVE_CALENDARS = create( "s5.sequence.remove.calendars", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_REMOVE_CALENDARS, //
      TSID_DESCRIPTION, D_REMOVE_CALENDARS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ScheduleExpressionList( //
          S5ScheduleUtils.createSchedule( "*", "*", "*", "*/60" ) ) )//
  );

}
