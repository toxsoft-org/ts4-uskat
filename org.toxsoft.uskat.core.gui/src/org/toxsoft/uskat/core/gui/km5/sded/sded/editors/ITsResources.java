package org.toxsoft.uskat.core.gui.km5.sded.sded.editors;

import org.toxsoft.core.tsgui.valed.controls.basic.*;

/**
 * Localized resources
 *
 * @author dima
 */
public interface ITsResources {

  /**
   * {@link ValedDoubleSpinner}
   */
  String STR_N_DOUBLE_SPINNER_FLOATING_DIGITS = "После запятой";
  String STR_D_DOUBLE_SPINNER_FLOATING_DIGITS = "Количество знаков после запятой";
  String STR_N_DOUBLE_SPINNER_STEP            = "Шаг";
  String STR_D_DOUBLE_SPINNER_STEP            = "Шаг изменения значения стрелкой";
  String STR_N_DOUBLE_SPINNER_PAGE_STEP       = "Скачок";
  String STR_D_DOUBLE_SPINNER_PAGE_STEP       = "Шаг изменения значения клавишами PageUp/PageDown";
  String FMT_ERR_INV_FLOATING_TEXT            = "Неверный формат представления вещественного числа";
  String MSG_ERR_INV_GWID_FORMAT              = "Неверный формат представления Gwid";               //$NON-NLS-1$
  String MSG_ERR_INV_SKID_FORMAT              = "Неверный формат представления Skid";               //$NON-NLS-1$

  /**
   * {@link PanelSkidListSelector}
   */
  String DLG_T_SKID_LIST_SEL         = "Выбор объектов";
  String STR_MSG_SKID_LIST_SELECTION = "Выберите нужные объекты и нажмите Ok";
  String MSG_ERR_NO_OBJ_SELECTED     = "Не выбран ни один объект";
}
