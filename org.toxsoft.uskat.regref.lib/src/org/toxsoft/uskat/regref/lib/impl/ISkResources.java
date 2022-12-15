package org.toxsoft.uskat.regref.lib.impl;

/**
 * Localizable resources.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  String FMT_ERR_CLASS_IS_REGREF_OWNED     = "Класс %s управлентся службой НСИ";
  String FMT_ERR_SECTION_ID_ALREADY_ESISTS = "Раздел с идентификатором %s уже существует";
  String FMT_ERR_INV_SECTION_ID            = "Раздел с идентификатором %s уже существует";
  String FMT_ERR_EMPTY_NAME                = "Нужно задать название раздела";
  String FMT_ERR_SECTION_ID_NOT_ESISTS     = "Раздел с идентификатором %s не существует";
  String STR_N_LINK_RRI_COMPANION_MASTER   = "Мастер";
  String STR_D_LINK_RRI_COMPANION_MASTER   = "Мастер-объект для которого сформировано НСИ";
  String STR_N_ATTR_RRI_SECTION_PARAMS     = "Параметры";
  String STR_D_ATTR_RRI_SECTION_PARAMS     = "Набор параметров раздела НСИ и их значений";
  String STR_N_TYPE_OPRION_SET             = "Опции";
  String STR_D_TYPE_OPRION_SET             = "Тип данных, хранящийнабор опции (идентифицированных атомарных значений";

  String STR_N_EVENT_RRI_EDIT     = "Правка НСИ";
  String STR_D_EVENT_RRI_EDIT     = "Событие редактирования значения параметра НСИ";
  String STR_N_EVPRM_REASON       = "Причина";
  String STR_D_EVPRM_REASON       = "Причина изменения параметров НСИ";
  String STR_N_EVPRM_AUTHOR_LOGIN = "Автор";
  String STR_D_EVPRM_AUTHOR_LOGIN = "Автор (точнее, логин, имя входа) изменения параметров НСИ";
  String STR_N_EVPRM_SECTION_ID   = "Раздел";
  String STR_D_EVPRM_SECTION_ID   = "Идентификатор раздела НСИ";
  String STR_N_EVPRM_PARAM_ID     = "Параметр";
  String STR_D_EVPRM_PARAM_ID     = "Идентификатор параметра НСИ";
  String STR_N_EVPRM_IS_LINK      = "Связь?";
  String STR_D_EVPRM_IS_LINK      = "Признак, что параметр НСИ связь, а не атрибут объекта";
  String STR_N_EVPRM_OLD_VAL_ATTR = "Предыдущее";
  String STR_D_EVPRM_OLD_VAL_ATTR = "Предыдущее значение параметра НСИ";
  String STR_N_EVPRM_OLD_VAL_LINK = "Предыдущее";
  String STR_D_EVPRM_OLD_VAL_LINK = "Предыдущее значение параметра НСИ";
  String STR_N_EVPRM_NEW_VAL_ATTR = "Новое";
  String STR_D_EVPRM_NEW_VAL_ATTR = "Новое значение параметра НСИ";
  String STR_N_EVPRM_NEW_VAL_LINK = "Новое";
  String STR_D_EVPRM_NEW_VAL_LINK = "Новое значение параметра НСИ";

}
