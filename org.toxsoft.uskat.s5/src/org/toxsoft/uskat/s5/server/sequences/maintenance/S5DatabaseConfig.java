package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

/**
 * Описание конфигурации подсистемы базы данных сервера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public final class S5DatabaseConfig {

  /**
   * Префикс идентфикаторов подсистемы
   */
  public static final String SYBSYSTEM_ID_PREFIX = S5BackendCoreConfig.SYBSYSTEM_ID_PREFIX + ".db";

  /**
   * Параметр {@link S5BackendSequenceSupportSingleton#configuration()}: тип используемой СУБД
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  public static final IDataDef DATABASE_ENGINE = create( SYBSYSTEM_ID_PREFIX + ".engine", EAtomicType.VALOBJ, //
      TSID_NAME, STR_N_DATABASE_ENGINE, //
      TSID_DESCRIPTION, STR_D_DATABASE_ENGINE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( ES5DatabaseEngine.MARIADB ) );

  /**
   * Параметр {@link S5BackendSequenceSupportSingleton#configuration()}: схема базы данных сервера в СУБД
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  public static final IDataDef DATABASE_SCHEMA = create( SYBSYSTEM_ID_PREFIX + ".schema", EAtomicType.STRING, //
      TSID_NAME, STR_N_DATABASE_SCHEMA, //
      TSID_DESCRIPTION, STR_D_DATABASE_SCHEMA, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * Параметр {@link S5BackendSequenceSupportSingleton#configuration()}: гарантированное время (сутки) хранения данных.
   * <p>
   * Определяет время хранения значений исторических данных, событий и истории команд. По факту система может хранить
   * данные более долгий период (определяется реализацией), но не меньший.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  public static final IDataDef DATABASE_DEPTH = create( SYBSYSTEM_ID_PREFIX + ".depth", EAtomicType.INTEGER, //
      TSID_NAME, STR_N_DATABASE_DEPTH, //
      TSID_DESCRIPTION, STR_D_DATABASE_DEPTH, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 3 * 365 ) ); // По умолчанию: 3 года

  /**
   * Все параметры подсистемы.
   */
  public static final IStridablesList<IDataDef> ALL_DATABASE_OPDEFS = new StridablesList<>( //
      DATABASE_ENGINE, //
      DATABASE_SCHEMA, //
      DATABASE_DEPTH //
  );
}
