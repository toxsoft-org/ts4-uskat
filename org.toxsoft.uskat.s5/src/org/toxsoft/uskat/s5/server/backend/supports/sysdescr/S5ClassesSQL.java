package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

/**
 * Служебные константы и методы для выполнения SQL-запросов
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
class S5ClassesSQL {

  // ------------------------------------------------------------------------------------
  // Индексы таблиц
  //
  /**
   * Имя внешнего ключа: parentId -> S5ClassEntity
   */
  static final String FK_PARENTID_TO_CLASS = "S5ClassEntity_parentId_to_S5ClassEntity_fk"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Запросы
  //
  /**
   * Имя запроса описаний классов
   */
  static final String QUERY_NAME_GET_TYPES = "S5TypeEntity.getTypes";

  /**
   * Запрос описаний классов
   */
  static final String QUERY_GET_TYPES = "SELECT tp FROM S5TypeEntity tp";

  /**
   * Имя запроса описаний классов
   */
  static final String QUERY_NAME_GET_CLASSES = "S5TypeEntity.getClasses";

  /**
   * Запрос описаний классов
   */
  static final String QUERY_GET_CLASSES = "SELECT cls FROM S5ClassEntity cls";
}
