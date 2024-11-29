package org.toxsoft.uskat.s5.server.sequences.maintenance;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Описание конфигурации подсистемы обработки последовательностей значений.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5SequenceConfig
    extends S5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5DatabaseConfig.SYBSYSTEM_ID_PREFIX + ".sequences";

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_SEQUENCES_OPDEFS = new StridablesList<>( //
  );
}
