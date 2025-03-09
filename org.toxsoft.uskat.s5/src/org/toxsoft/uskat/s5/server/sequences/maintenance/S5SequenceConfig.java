package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;

/**
 * Описание конфигурации подсистемы обработки последовательностей значений.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5SequenceConfig {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5DatabaseConfig.SYBSYSTEM_ID_PREFIX + ".sequences";

  /**
   * Интервал обработки статистики (мсек).
   */
  public static final IDataDef SEQUENCES_STATISTICS_TIMEOUT =
      create( SYBSYSTEM_ID_PREFIX + ".statisticsTimeout", INTEGER, //$NON-NLS-1$
          TSID_NAME, N_STATISTICS_TIMEOUT, //
          TSID_DESCRIPTION, D_STATISTICS_TIMEOUT, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_IS_MANDATORY, AV_FALSE, //
          TSID_DEFAULT_VALUE, AvUtils.avInt( 1000 ) );

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_SEQUENCES_OPDEFS = new StridablesList<>( //
      SEQUENCES_STATISTICS_TIMEOUT //
  );
}
