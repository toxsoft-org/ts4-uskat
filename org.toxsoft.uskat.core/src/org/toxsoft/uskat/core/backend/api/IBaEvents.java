package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.events.*;

/**
 * Backend addon for events send/receive and storage.
 * <p>
 * This is the madatory addon however some or whole features may not be supported by addon. The flags
 * <code>OPDEF_SKBI_BA_EVENTS_XXX</code> informs CoreAPI and user about supported features.
 *
 * @author hazard157
 */
public interface IBaEvents {

  /**
   * Sends events to all consumers including the remote ones.
   *
   * @param aEvents {@link ITimedList} - the list of the events to send
   */
  void fireEvents( ITimedList<SkEvent> aEvents );

  /**
   * Informs backend about events the frontend is interested in.
   * <p>
   * Argument replaces previous list of needed GWIDs in the backend. An empty list means that frontend does not
   * subscribes on any event. However some system events will delivered to the frontend event if there is no
   * subscription at all.
   * <p>
   * The list can contain only GWIDs of events (i.e., of type {@link EGwidKind#GW_EVENT}. The multi-GWIDs are allowed,
   * in particular (from more general to more specific):
   * <p>
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
   * All other GWIDs are ignored. Duplicate GWIDs and GWIDs in list if they are covered by more general GWIDs are
   * ignored.
   *
   * @param aNeededGwids {@link IGwidList} - GWIDs of needed events
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void setNeededEventGwids( IGwidList aNeededGwids );

  /**
   * Queries the history of events.
   * <p>
   * В списке интересующих событий допускаются только GWID-ы событий. Все элементы, у которых {@link Gwid#kind()} не
   * равно {@link EGwidKind#GW_EVENT} молча игнорируются.
   * <p>
   * В списке могут быть как конкретные (с идентификатором объекта) {@link Gwid}-ы, так и абстрактные. Абстрактный
   * {@link Gwid} означает запрос указанного события от всех объектов. Кроме того, в запросе могут присутствовать
   * мулти-GWID-ы, у которых {@link Gwid#isMulti()} = <code>true</code>, что означает "все собятия запрошенного
   * объекта". Получается, что абстрактный мули-GWID запрашивает все события от всех объектов класса
   * {@link Gwid#classId()}.
   *
   * @param aInterval {@link IQueryInterval} - the time interval to be queried
   * @param aNeededGwids {@link IGwidList} - list of event GWIDs to be queried
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of requested events
   */
  ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids );

}
