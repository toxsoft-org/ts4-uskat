package org.toxsoft.uskat.sysext.alarms.api.filters;

/**
 * Локализуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link ISkAlarmFilterByLevel}
   */
  String STR_N_LVF_COMPARE_OP  = "Оператор";
  String STR_D_LVF_COMPARE_OP  = "Оператор сравнения уровня приоритета проверямой тревоги с целочисленной константой";
  String STR_N_LVF_LEVEL_CONST = "Уровень";
  String STR_D_LVF_LEVEL_CONST = "Целое число, с которым сравнивается уровень приоритета фильтруемой тревоги";

  /**
   * {@link ISkAlarmFilterByAuthorObjId}
   */
  String STR_N_LVF_AUTHOR_OBJ_ID_CONST = "Автор";
  String STR_D_LVF_AUTHOR_OBJ_ID_CONST = "Идентификатор желаемого объекта - автора тревоги";

  /**
   * {@link ISkAlarmFilterByPriority}
   */
  String STR_N_PRF_COMPARE_OP     = "Оператор";
  String STR_D_PRF_COMPARE_OP     = "Оператор сравнения приоритета проверямой тревоги с константой";
  String STR_N_PRF_PRIORITY_CONST = "Приоритет";
  String STR_D_PRF_PRIORITY_CONST = "Константа важности, с которым сравнивается приоритет фильтруемой тревоги";
}
