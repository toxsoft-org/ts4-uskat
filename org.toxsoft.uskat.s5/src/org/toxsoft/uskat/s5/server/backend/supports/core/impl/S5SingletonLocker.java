package org.toxsoft.uskat.s5.server.backend.supports.core.impl;

/**
 * Вспомогательный механизм управления синглтонами сервера.
 *
 * @author mvk
 */
public class S5SingletonLocker {

  private static boolean isDoJobEnable = false;

  // ------------------------------------------------------------------------------------
  // public API
  //
  /**
   * Возвращает признак разрешения выполнения doJob-потоков
   *
   * @return boolean <b>true</b> doJob разрешены; <b>false</b> doJob запрещены.
   */
  public static boolean isDoJobEnable() {
    return isDoJobEnable;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //
  /**
   * Установить разрешение выполнения doJob-потоков.
   *
   * @param aEnable boolean <b>true</b> doJob разрешены; <b>false</b> doJob запрещены.
   */
  static void setDoJobEnable( boolean aEnable ) {
    isDoJobEnable = aEnable;
  }

}
