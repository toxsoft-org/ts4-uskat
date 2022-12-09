package org.toxsoft.uskat.ggprefs.lib;

import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Слушатель изменений в разделе настроек.
 *
 * @author goga
 */
public interface IGuiGwPrefsObjectListener {

  /**
   * Вызывается при измненении опции в конкретной связке, связянной с объектом.
   * <p>
   * Внимание: метод вызывается как при изменении <i>значения</i> опции, так и в том случае, когда меняется список
   * <i>описаний</i> опции.
   *
   * @param aSource {@link IGuiGwPrefsSection} - раздел, истоник сообщения
   * @param aObjSkid {@link Skid} - SKID объекта, чьи GUI настройки изменились
   */
  void onGuiGwPrefsBundleChanged( IGuiGwPrefsSection aSource, Skid aObjSkid );

}
