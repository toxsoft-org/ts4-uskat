package org.toxsoft.uskat.ggprefs.lib;

import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Слушатель изменений одной опции конкретного объекта.
 *
 * @author goga
 */
public interface IGuiGwPrefsOptionListener {

  /**
   * Вызывается при измненении опции в конкретной связке, связянной с объектом.
   * <p>
   * Внимание: метод вызывается только при изменении существующей опции.
   *
   * @param aSource {@link IGuiGwPrefsSection} - раздел, истоник сообщения
   * @param aObjSkid {@link Skid} - SKID объекта, чьи GUI настройки изменились
   * @param aOptionId String - идентификатор опции, чье значение изменилось
   */
  void onGuiGwPrefsBundleChanged( IGuiGwPrefsSection aSource, Skid aObjSkid, String aOptionId );

}
