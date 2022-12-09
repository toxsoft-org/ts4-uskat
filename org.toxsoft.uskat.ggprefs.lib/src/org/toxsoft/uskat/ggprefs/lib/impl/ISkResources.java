package org.toxsoft.uskat.ggprefs.lib.impl;

/**
 * Localizable resources.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link GuiGwPrefsSection}
   */
  String FMT_ERR_NO_SUCH_OBJ = "Объект %s не существует";
  String FMT_ERR_INV_OP_TYPE = "Опция %s: значение имеет тип %s вместо ожидаемого %s";

  /**
   * {@link IServiceInternalConstants}
   */
  String STR_N_AINF_SECTTION_DEF_PARAMS = "Параметры";
  String STR_D_AINF_SECTTION_DEF_PARAMS = "Параметры описание раздела настроек GUI";

  /**
   * {@link SkGuiGwPrefsService}
   */
  String FMT_ERR_DUP_SECTION_ID = "Раздел с идентификатором %s уже существует";

}
