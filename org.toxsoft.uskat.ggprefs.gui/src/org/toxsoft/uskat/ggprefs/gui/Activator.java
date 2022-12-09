package org.toxsoft.uskat.ggprefs.gui;

import org.toxsoft.core.tsgui.mws.bases.MwsActivator;

/**
 * Активатор плагина.
 *
 * @author goga
 */
public class Activator
    extends MwsActivator {

  /**
   * Идентификатор плагина.
   */
  public static final String PLUGIN_ID = "org.toxsoft.uskat.ggprefs.gui"; //$NON-NLS-1$

  private static Activator instance = null;

  /**
   * Пустой конструктор.
   */
  public Activator() {
    super( PLUGIN_ID );
    checkInstance( instance );
    instance = this;
  }

  /**
   * Возвращает ссылку на единственный экземпляр этого активатора.
   *
   * @return {@link Activator} - ссылка на единственный экземпляр
   */
  public static Activator getInstance() {
    return instance;
  }

}
