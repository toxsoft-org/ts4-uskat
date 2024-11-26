package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.IS5RegisteredConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Описание параметров(опций) для команды проверки блоков последовательности
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5SequenceValidationOptions
    extends IS5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  String SYBSYSTEM_ID_PREFIX = IS5SequenceHardConstants.SYBSYSTEM_ID_PREFIX + ".validation";

  /**
   * Автоматическое восстановление целостности при обнаружении ошибок
   */
  IDataDef VALIDATION_AUTO_REPAIR =
      register( SYBSYSTEM_ID_PREFIX + ".auto_repair", BOOLEAN, TSID_NAME, N_VALID_AUTO_REPAIR, //
          TSID_DESCRIPTION, D_VALID_AUTO_REPAIR, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Интервал проверки
   */
  IDataDef VALIDATION_INTERVAL = register( SYBSYSTEM_ID_PREFIX + ".interval", VALOBJ, TSID_NAME, N_VALID_INTERVAL, //
      TSID_DESCRIPTION, D_VALID_INTERVAL, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ITimeInterval.NULL ) );

  /**
   * Требование восстановить базу только если это НЕ приводит к потери данных
   */
  IDataDef VALIDATION_REPAIR = register( SYBSYSTEM_ID_PREFIX + ".repair", BOOLEAN, TSID_NAME, N_VALID_REPAIR, //
      TSID_DESCRIPTION, D_VALID_REPAIR, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_TRUE );

  /**
   * Требование восстановить базу даже если это приводит к частичной потери данных
   */
  IDataDef VALIDATION_FORCE_REPAIR =
      register( SYBSYSTEM_ID_PREFIX + ".force_repair", BOOLEAN, TSID_NAME, N_VALID_FORCE_REPAIR, //
          TSID_DESCRIPTION, D_VALID_FORCE_REPAIR, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, AV_FALSE );

  /**
   * Список идентификаторов данных ({@link Gwid}) которые необходимо проверить. Если список не указан, то все данные
   */
  IDataDef VALIDATION_GWIDS = register( SYBSYSTEM_ID_PREFIX + ".ids", VALOBJ, TSID_NAME, N_VALID_GWIDS, //
      TSID_DESCRIPTION, D_VALID_GWIDS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( IGwidList.EMPTY ) );

  /**
   * Все параметры подсистемы.
   */
  IStridablesList<IDataDef> ALL_VALIDATION_OPDEFS = new StridablesList<>( //
      VALIDATION_AUTO_REPAIR, //
      VALIDATION_INTERVAL, //
      VALIDATION_REPAIR, //
      VALIDATION_FORCE_REPAIR, //
      VALIDATION_GWIDS //
  );
}
