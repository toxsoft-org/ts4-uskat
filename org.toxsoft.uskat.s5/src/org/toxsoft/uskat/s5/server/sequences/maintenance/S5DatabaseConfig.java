package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Описание конфигурации подсистемы базы данных сервера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5DatabaseConfig
    extends S5RegisteredConstants {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5BackendCoreConfig.SYBSYSTEM_ID_PREFIX + ".db";

  /**
   * Параметр {@link ISkBackendInfo#params()}: схема базы данных сервера в СУБД
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  public static final IDataDef DATABASE_SCHEMA =
      register( SYBSYSTEM_ID_PREFIX + ".schema", EAtomicType.STRING, TSID_NAME, STR_N_BACKEND_DB_SCHEMA_NAME, //
          TSID_DESCRIPTION, STR_D_BACKEND_DB_SCHEMA_NAME, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * Параметр {@link ISkBackendInfo#params()}: гарантированное время (сутки) хранения данных.
   * <p>
   * Определяет время хранения значений исторических данных, событий и истории команд. По факту система может хранить
   * данные более долгий период (определяется реализацией), но не меньший.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  public static final IDataDef DATABASE_DEPTH =
      register( SYBSYSTEM_ID_PREFIX + ".depth", EAtomicType.INTEGER, TSID_NAME, STR_N_BACKEND_DB_STORAGE_DEPTH, //
          TSID_DESCRIPTION, STR_D_BACKEND_DB_STORAGE_DEPTH, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, avInt( 3 * 365 ) ); // По умолчанию: 3 года

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_DATABASE_OPDEFS = new StridablesList<>( //
      DATABASE_SCHEMA, //
      DATABASE_DEPTH //
  );
}
