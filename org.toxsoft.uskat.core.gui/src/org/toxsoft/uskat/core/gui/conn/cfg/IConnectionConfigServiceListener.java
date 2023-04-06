package org.toxsoft.uskat.core.gui.conn.cfg;

/**
 * The service {@link IConnectionConfigService} listener.
 *
 * @author hazard157
 */
public interface IConnectionConfigServiceListener {

  /**
   * Called when the list {@link IConnectionConfigService#listConfigs()} changes.
   *
   * @param aSource {@link IConnectionConfigService} - the event source
   */
  void onConfigsListChanged( IConnectionConfigService aSource );

  /**
   * Called when the list {@link IConnectionConfigService#listProviders()} changes.
   *
   * @param aSource {@link IConnectionConfigService} - the event source
   */
  void onProvidersListChanged( IConnectionConfigService aSource );

}
