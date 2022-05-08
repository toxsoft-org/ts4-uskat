package org.toxsoft.uskat.core.api.evserv;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * USkat event collecion.
 *
 * @author hazard157
 */
public interface ISkEventsList {

  /**
   * Returns the list of events ordered byt timestamp.
   *
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - events by time
   */
  ITimedList<SkEvent> events();

  // TODO misc caching methods to work with list

  /**
   * Returns uniqueq concrete GWIDs of events in the list.
   *
   * @return {@link IGwidList} - events concrete GWIDs
   */
  IGwidList eventGwids();

}
