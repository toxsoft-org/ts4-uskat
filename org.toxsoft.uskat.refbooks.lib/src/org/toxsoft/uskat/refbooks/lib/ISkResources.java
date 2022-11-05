package org.toxsoft.uskat.refbooks.lib;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  // TODO L10N

  /**
   * {@link ISkRefbookServiceHardConstants}
   */
  String STR_N_ITEM_CLASS_ID       = "Класс элемента";
  String STR_D_ITEM_CLASS_ID       = "Идентификатор класса элемента справочника";
  String STR_N_REFBOOK_ITEM_CHANGE = "RB item";
  String STR_D_REFBOOK_ITEM_CHANGE = "Refbook item changed";
  String STR_N_CHANGE_AUTHOR       = "Author";
  String STR_D_CHANGE_AUTHOR       = "The user (if applicable) who changed the refbook";
  String STR_N_CRUD_OP             = "CRUD operation";
  String STR_D_CRUD_OP             = "What happened with item: CREATE, CHANGE, REMOVE";
  String STR_N_ITEM_SKID           = "Item SKID";
  String STR_D_ITEM_SKID           = "SKID of the affected item";
  String STR_N_OLD_ATTRS           = "Old attrs";
  String STR_D_OLD_ATTRS           = "Values of refbook item attributes before change, or nothing if item just created";
  String STR_N_NEW_ATTRS           = "New attrs";
  String STR_D_NEW_ATTRS           = "Values of refbook item attributes after change, or nothing if item just removed";
  String STR_N_OLD_LINKS           = "Old links";
  String STR_D_OLD_LINKS           = "Values of refbook item links before change, or nothing if item just created";
  String STR_N_NEW_LINKS           = "New links";
  String STR_D_NEW_LINKS           = "Values of refbook item links after change, or nothing if item just removed";
  String STR_N_OLD_RIVETS          = "Old rivets";
  String STR_D_OLD_RIVETS          = "Values of refbook item rivets before change, or nothing if item just created";
  String STR_N_NEW_RIVETS          = "New rivets";
  String STR_D_NEW_RIVETS          = "Values of refbook item rivets after change, or nothing if item just removed";

}
