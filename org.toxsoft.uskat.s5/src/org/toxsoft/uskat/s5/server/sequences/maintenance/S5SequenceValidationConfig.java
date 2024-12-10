package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Описание конфигурации подсистемы проверки блоков последовательности.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5SequenceValidationConfig
    extends S5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5SequenceConfig.SYBSYSTEM_ID_PREFIX + ".validation";

  /**
   * Автоматическое восстановление целостности при обнаружении ошибок
   */
  public static final IDataDef VALIDATION_AUTO_REPAIR =
      register( SYBSYSTEM_ID_PREFIX + ".auto_repair", BOOLEAN, TSID_NAME, N_VALID_AUTO_REPAIR, //
          TSID_DESCRIPTION, D_VALID_AUTO_REPAIR, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Интервал проверки
   */
  public static final IDataDef VALIDATION_INTERVAL =
      register( SYBSYSTEM_ID_PREFIX + ".interval", VALOBJ, TSID_NAME, N_VALID_INTERVAL, //
          TSID_DESCRIPTION, D_VALID_INTERVAL, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Требование восстановить базу только если это НЕ приводит к потери данных
   */
  public static final IDataDef VALIDATION_REPAIR =
      register( SYBSYSTEM_ID_PREFIX + ".repair", BOOLEAN, TSID_NAME, N_VALID_REPAIR, //
          TSID_DESCRIPTION, D_VALID_REPAIR, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Требование восстановить базу даже если это приводит к частичной потери данных
   */
  public static final IDataDef VALIDATION_FORCE_REPAIR =
      register( SYBSYSTEM_ID_PREFIX + ".force_repair", BOOLEAN, TSID_NAME, N_VALID_FORCE_REPAIR, //
          TSID_DESCRIPTION, D_VALID_FORCE_REPAIR, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_FALSE );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо проверить. Если список пустой, то все данные
   */
  public static final IDataDef VALIDATION_GWIDS =
      register( SYBSYSTEM_ID_PREFIX + ".ids", VALOBJ, TSID_NAME, N_VALID_GWIDS, //
          TSID_DESCRIPTION, D_VALID_GWIDS, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, avValobj( IGwidList.EMPTY ) );

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_VALIDATION_OPDEFS = new StridablesList<>( //
      VALIDATION_AUTO_REPAIR, //
      VALIDATION_INTERVAL, //
      VALIDATION_REPAIR, //
      VALIDATION_FORCE_REPAIR, //
      VALIDATION_GWIDS //
  );
}
