package org.toxsoft.uskat.core;

import org.toxsoft.core.tslib.gw.gwid.*;

public interface ICoreListener {

  /**
   * Вызвается при изменении в классах или объектах.
   */
  void onGreenWorldChanged( IGwidList aAdded, IGwidList aRemoved, IGwidList aChanged );

}
