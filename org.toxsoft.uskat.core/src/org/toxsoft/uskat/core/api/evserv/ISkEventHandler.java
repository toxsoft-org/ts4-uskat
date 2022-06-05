package org.toxsoft.uskat.core.api.evserv;

/**
 * Handler of the events {@link SkEvent}.
 *
 * @author hazard157
 */
public interface ISkEventHandler {

  /**
   * Called by the event service when events of interest arrive.
   *
   * @param aEvents {@link ISkEventList} - non-empty ist of events
   */
  void onEvents( ISkEventList aEvents );

}
