package org.toxsoft.uskat.core.incub;

import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * The asynchronous query.
 *
 * @author hazard157
 * @param <T> - queries temporal value type
 */
public interface ISkHistoryQuery<T extends ITemporalValue<T>>
    extends IGenericChangeEventCapable, ICloseable {

  String queryId();

  EQueryState state();

  IGwidList listGwids();

  void setGwids( IGwidList aList );

  void exec( IQueryInterval aInterval );

  void cancel();

  ITimedList<T> get( Gwid aGwid );

  IMap<Gwid, ITimedList<T>> getAll();

}
