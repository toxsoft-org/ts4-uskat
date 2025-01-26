package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.skatlets.IS5Resources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.*;

/**
 * Параметры конфигурации подсистемы
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5BackendSkatletsConfig {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = ISkHardConstants.USKAT_FULL_ID + ".skatlets";

  /**
   * Порядок загрузки скатлетов. Скатлеты не указанные в списке загружаются последними.
   */
  public static final IDataDef SKATLETS_LOAD_ORDERSKATLETS_LOAD_ORDER =
      create( SYBSYSTEM_ID_PREFIX + ".loadOrder", VALOBJ, //
          TSID_NAME, STR_LOAD_ORDER, //
          TSID_DESCRIPTION, STR_LOAD_ORDER_D, //
          TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ), //
          TSID_IS_MANDATORY, AV_FALSE //
      );

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_SKATLETS_OPDEFS = new StridablesList<>( //
      SKATLETS_LOAD_ORDERSKATLETS_LOAD_ORDER //
  );

}
