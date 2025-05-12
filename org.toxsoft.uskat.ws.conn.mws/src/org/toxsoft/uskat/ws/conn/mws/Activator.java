package org.toxsoft.uskat.ws.conn.mws;

import org.toxsoft.core.tsgui.mws.bases.*;

/**
 * The simple plugin activator template.
 * <p>
 * <b>Important</b>: after copy-pasting this file from the templates project it is mandatory to replace value of the
 * {@link #PLUGIN_ID} constant with the ID of the destination project.
 *
 * @author hazard157
 */
public class Activator
    extends MwsActivator {

  /**
   * The plugin ID (for Java static imports).
   */
  public static final String PLUGIN_ID = "org.toxsoft.uskat.ws.conn.mws"; //$NON-NLS-1$

  private static Activator instance = null;

  /**
   * Constructor.
   */
  public Activator() {
    super( PLUGIN_ID );
    checkInstance( instance );
    instance = this;
  }

  @Override
  protected void doStart() {
    // TODO 0. prepare settings from default values
    // TODO 1. override settings from config file
    // TODO 2. override settings from command line arguments
    // TODO 4. process (aply) settings either here or in processor/addon
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
