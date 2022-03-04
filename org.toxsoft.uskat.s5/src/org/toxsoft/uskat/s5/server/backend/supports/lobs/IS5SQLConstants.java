package org.toxsoft.uskat.s5.server.backend.supports.lobs;

/**
 * Константы для выполнения SQL-запросов {@link IS5BackendLobsSingleton}
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5SQLConstants {

  // ------------------------------------------------------------------------------------
  // Параметры запросов
  //
  String QPARAM_ID = "id";

  // ------------------------------------------------------------------------------------
  // Тесты SQL-запросов
  //
  /**
   * Формат запроса получения идентификаторов lob-данных
   * <p>
   */
  String QFRMT_GET_SYS_IDS = "SELECT lobEntity.id FROM S5LobEntity lobEntity ";

  /**
   * Формат запроса получения lob-данного
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_GET_LOB = "SELECT lobEntity FROM S5LobEntity lobEntity WHERE lobEntity.id = '%s'";

  /**
   * Формат запроса проверки существования lob-данного
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_HAS_LOB = "SELECT lobEntity.id FROM S5LobEntity lobEntity WHERE lobEntity.id = '%s'";

  /**
   * Формат удаления lob-данного по идентификатору
   * <p>
   * <li>1. %s - Строковый идентификатор;</li>
   */
  String QFRMT_DELETE_LOB = "DELETE FROM S5LobEntity lobEntity WHERE lobEntity.id = '%s'";

}
