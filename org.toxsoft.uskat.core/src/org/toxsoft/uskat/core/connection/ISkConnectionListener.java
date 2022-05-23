package org.toxsoft.uskat.core.connection;

/**
 * Listener to the {@link ISkConnection#state()} change.
 *
 * @author hazard157
 */
public interface ISkConnectionListener {

  /**
   * Called when {@link ISkConnection#state()} is changed.
   *
   * @param aSource {@link ISkConnection} - event source
   * @param aOldState {@link ESkConnState} - previous state
   */
  void onSkConnectionStateChanged( ISkConnection aSource, ESkConnState aOldState );

  /**
   * Called before {@link ISkConnection#state()} is changed.
   *
   * @param aSource {@link ISkConnection} - event source
   * @param aNewState {@link ESkConnState} - next expected state
   */
  default void beforeSkConnectionStateChange( ISkConnection aSource, ESkConnState aNewState ) {
    // nop
  }

}
