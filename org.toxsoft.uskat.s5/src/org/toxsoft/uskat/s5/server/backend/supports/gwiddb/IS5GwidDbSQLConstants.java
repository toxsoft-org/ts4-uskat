package org.toxsoft.uskat.s5.server.backend.supports.gwiddb;

import org.toxsoft.core.tslib.bricks.strid.more.IdPair;

/**
 * Константы для выполнения SQL-запросов {@link IS5BackendGwidDbSingleton}
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5GwidDbSQLConstants {

  // ------------------------------------------------------------------------------------
  // Параметры запросов
  //
  String QPARAM_ID = "id";

  // ------------------------------------------------------------------------------------
  // Тесты SQL-запросов
  //
  /**
   * Формат запроса получения всех идентификаторов секций существующих в базе данных
   */
  String QFRMT_GET_SECTIONS = "SELECT DISTINCT " + S5GwidDbID.FIELD_SECTION + " FROM S5GwidDbEntity";

  /**
   * Формат запроса получения всех ключей секции значений
   * <p>
   * <li>1. %s - Строковое представление ({@link IdPair#KEEPER}) секции;</li>
   */
  String QFRMT_GET_KEYS =
      "SELECT " + S5GwidDbID.FIELD_GWID + " FROM S5GwidDbEntity WHERE " + S5GwidDbID.FIELD_SECTION + "='%s'";

  /**
   * Формат запроса получения идентификаторов lob-данных
   * <p>
   */
  String QFRMT_GET_SYS_IDS = "SELECT gdbEntity.id FROM S5GwidDbEntity gdbEntity ";

  /**
   * Формат запроса получения lob-данного
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_GET_VALUE = "SELECT gdbEntity FROM S5GwidDbEntity gdbEntity WHERE gdbEntity.id = '%s'";

  /**
   * Формат запроса проверки существования lob-данного
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_HAS_VALUE = "SELECT gdbEntity.id FROM S5GwidDbEntity gdbEntity WHERE gdbEntity.id = '%s'";

  /**
   * Формат удаления lob-данного по идентификатору
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_REMOVE_VALUE = "DELETE FROM S5GwidDbEntity gdbEntity WHERE gdbEntity.id = '%s'";

}
