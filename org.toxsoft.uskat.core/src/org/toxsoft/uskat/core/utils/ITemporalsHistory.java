package org.toxsoft.uskat.core.utils;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Provides access to the history of entities identified by the GWID.
 *
 * @author hazard157
 * @param <T> - {@link ITemporal} entity type
 */
public interface ITemporalsHistory<T extends ITemporal<T>> {

  /**
   * Returns the history of the queried entities.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwids {@link IGwidList} - requested entity GWIDs, each may be multi GWID {@link Gwid#isMulti()} =
   *          <code>true</code>.
   * @return {@link ITimedList}&lt;T&gt; - list of the queried entities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ITimedList<T> query( IQueryInterval aInterval, IGwidList aGwids );

  /**
   * Returns the queried entities.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwid {@link Gwid} - requested entity GWID, may be multi GWID {@link Gwid#isMulti()} = <code>true</code>.
   * @return {@link ITimedList}&lt;T&gt; - list of the queried entities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ITimedList<T> query( IQueryInterval aInterval, Gwid aGwid ) {
    return query( aInterval, new GwidList( aGwid ) );
  }

}
