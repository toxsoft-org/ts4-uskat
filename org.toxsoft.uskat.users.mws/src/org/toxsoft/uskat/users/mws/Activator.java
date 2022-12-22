package org.toxsoft.uskat.users.mws;

import org.toxsoft.core.tsgui.mws.bases.*;

/**
 * The plugin activator.
 *
 * @author hazard157
 */
public class Activator
    extends MwsActivator {

  /**
   * The plugin KEEPER_ID (for Java static imports).
   */
  public static final String PLUGIN_ID = "org.toxsoft.uskat.users.mws"; //$NON-NLS-1$

  private static Activator instance = null;

  /**
   * Constructor.
   */
  public Activator() {
    super( PLUGIN_ID );
    checkInstance( instance );
    instance = this;
  }

  /**
   * Returns the reference to the activator singleton.
   *
   * @return {@link Activator} - the activator singleton
   */
  public static Activator getInstance() {
    return instance;
  }

}
