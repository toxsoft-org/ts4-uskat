package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;

/**
 * Описание параметров(опций) для команды проверки блоков последовательности
 *
 * @author mvk
 */
public interface IS5SequenceValidationOptions {

  /**
   * Автоматическое восстановление целостности при обнаружении ошибок
   */
  IDataDef AUTO_REPAIR = create( "s5.sequence.validation.auto_repair", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, N_VALID_AUTO_REPAIR, //
      TSID_DESCRIPTION, D_VALID_AUTO_REPAIR, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Интервал проверки
   */
  IDataDef INTERVAL = create( "s5.sequence.validation.interval", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_VALID_INTERVAL, //
      TSID_DESCRIPTION, D_VALID_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Требование восстановить базу только если это НЕ приводит к потери данных
   */
  IDataDef REPAIR = create( "s5.sequence.validation.repair", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, N_VALID_REPAIR, //
      TSID_DESCRIPTION, D_VALID_REPAIR, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Требование восстановить базу даже если это приводит к частичной потери данных
   */
  IDataDef FORCE_REPAIR = create( "s5.sequence.validation.force_repair", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, N_VALID_FORCE_REPAIR, //
      TSID_DESCRIPTION, D_VALID_FORCE_REPAIR, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_FALSE );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо проверить. Если список не указан, то все данные
   */
  IDataDef GWIDS = create( "s5.sequence.validation.ids", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_VALID_GWIDS, //
      TSID_DESCRIPTION, D_VALID_GWIDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( IGwidList.EMPTY ) );
}
