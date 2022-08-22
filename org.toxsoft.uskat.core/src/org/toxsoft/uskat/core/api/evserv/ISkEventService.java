package org.toxsoft.uskat.core.api.evserv;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Core service: object-generated events management.
 *
 * @author hazard157
 */
public interface ISkEventService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Events"; //$NON-NLS-1$

  /**
   * Generates an event.
   *
   * @param aEvent {@link SkEvent} - генерируемое событие
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such object
   * @throws TsItemNotFoundRtException no such event is defined
   * @throws TsIllegalArgumentRtException invalid event parameter
   */
  void fireEvent( SkEvent aEvent );

  /**
   * Registers a handler, that is, subscribes to events of interest.
   * <p>
   * If such a handler is already registered, then method adds new identifiers from the aNeededGwids argument to the
   * list of GWIDs of interest.
   * <p>
   * Only GWIDs of kind {@link EGwidKind#GW_EVENT} are considered, all other kinds are ignored. Multi-GWIDs are
   * allowed.<br>
   * In particular, the following rules apply (from more general to more specific):
   * <ul>
   * <li>multi-object with multi event ID like <code>classid[*]$event(*)</code> - subscribes to all events of all events
   * of all objects of the specified class and it's subclasses. Particulary GWID SkObject[*]$event(*) means subsciption
   * to all events;</li>
   * <li>concrete GWID with multi event ID like <code>classid[obj_strid]$event(*)</code> - all events of the specified
   * object;</li>
   * <li>multi-object with the specified event ID like <code>classid[*]$event(good_event)</code> - subscribes to the
   * specified event of all objects of specified class and it's subclasses;</li>
   * <li>concrete GWID with spcified event ID like classid[obj_strid]$event(good_event) - single event of the single
   * object.</li>
   * </ul>
   * <p>
   * Abstract GWID are considered as GWIDs with multi-objects.
   * <p>
   * Empty list of GWIDs does nothing. Duplictae GWIDs and more specific GWIDs covered with more general ones are
   * removed from the internal list of GWIDs of interest.
   *
   * @param aNeededGwids {@link IGwidList} - list of GWIDs of interesting events
   * @param aEventHandler {@link ISkEventHandler} - the event handler
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void registerHandler( IGwidList aNeededGwids, ISkEventHandler aEventHandler );

  /**
   * Unregisters the event handler.
   * <p>
   * Method cancels all subsriptions of handler.
   * <p>
   * If no such handler is registered, it does nothing.
   *
   * @param aEventHandler {@link ISkEventHandler} - the event handler
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void unregisterHandler( ISkEventHandler aEventHandler );

  /**
   * Returns the objects event history for specified time interval.
   * <p>
   * Method accepts concrete GWID of kind {@link EGwidKind#GW_EVENT}. Multi objects are <b>not</b> allowed, however
   * multi-events {@link Gwid#isPropMulti()} = <code>true</code> are allowed.
   * <p>
   * Note: do not ask for long time interval, this method is synchronous and hence may freeze for a long time.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwid {@link Gwid} - concrete GWID of event(s)
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of the reuried events
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid GWID
   * @throws TsItemNotFoundRtException no such event exists in sysdescr
   */
  ITimedList<SkEvent> queryObjEvents( IQueryInterval aInterval, Gwid aGwid );

}
