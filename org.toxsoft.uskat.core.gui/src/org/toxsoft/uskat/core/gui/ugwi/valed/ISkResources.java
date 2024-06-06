package org.toxsoft.uskat.core.gui.ugwi.valed;

/**
 * Localizable resources.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * {@link PanelUgwiSelector}
   */
  String STR_SINGLE_UGWI_KIND       = "Тип: ";
  String STR_LIST_UGWI_KINDS        = "Типы: ";
  String DLG_CAPTION_STR            = "Ugwi selector";
  String DLG_TITLE_STR              = "Select Ugwi and press Ok";
  String PANEL_ERR_MSG_NO_UGWI_KIND = "No Ugwi kind to select";

  /**
   * {@link ValedUgwiSelectorTextAndButton}
   */
  String VALED_ERR_MSG_NO_UGWI_KIND =
      "No Ugwi kind to select from. Set any one of options:\n - OPDEF_SINGLE_UGWI_KIND_ID\n - OPDEF_UGWI_KIND_IDS_LIST";

  /**
   * {@link ValedUgwiSelectorFactory}
   */
  String STR_N_UGWI_KIND_IDS_LIST = "list of Ugwi kind ids";
  String STR_D_UGWI_KIND_IDS_LIST = "List of Ugwi kind ids applayble in context";

}
