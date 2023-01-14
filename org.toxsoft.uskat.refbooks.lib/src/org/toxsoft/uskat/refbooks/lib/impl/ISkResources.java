package org.toxsoft.uskat.refbooks.lib.impl;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link SkRefbookService}
   */
  String FMT_ERR_CLASS_IS_REFBOOK_OWNED     = "Класс %s управляется службой справочников";
  String FMT_ERR_OBJ_CLASS_IS_REFBOOK_OWNED = "Объекты класса %s управляются службой справочников";
  String STR_N_ATTR_ITEM_CLASS_ID           = "Класс элемента";
  String STR_D_ATTR_ITEM_CLASS_ID           = "Идентификатор описательного класса элементов справочника";
  String FMT_ERR_REFBOOK_ALREADY_EXISTS     = "Справочник с идентификатором %s уже существует";
  String FMT_WARN_RB_NAME_ALREADY_EXISTS    = "Справочник с таким же именем '%s' уже существует";
  String FMT_ERR_REFBOOK_NOT_EXISTS         = "Справочник с идентификатором %s не существует";
  String FMT_ERR_ITEM_ALREADY_EXISTS        = "Элемент с идентификатором %s уже существует";
  String FMT_WARN_ITEM_NAME_ALREADY_EXISTS  = "Элемент с таким же именем '%s' уже существует";
  String FMT_ERR_ITEM_NOT_EXISTS            = "Элемент с идентификатором %s не существует";
  String FMT_ERR_NO_SUCH_LINK               = "Попытка задать несуществующую связь '%s'";
  String FMT_ERR_INV_LINK                   = "Неверная связь '%s': %s";

}
