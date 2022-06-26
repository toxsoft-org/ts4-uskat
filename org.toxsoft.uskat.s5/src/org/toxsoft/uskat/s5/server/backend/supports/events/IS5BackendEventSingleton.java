package org.toxsoft.uskat.s5.server.backend.supports.events;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.IS5EventSequence;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5BackendSequenceSupportSingleton;

/**
 * Локальный интерфейс синглетона событий предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendEventSingleton
    extends IS5BackendSequenceSupportSingleton<IS5EventSequence, SkEvent> {

  /**
   * Фронтенд сгенерировал события, которые бекенд должен раздать всем, кроме этого фронтенда.
   * <p>
   * Предплоагается, что уж собственных подписчиков фронтед известит сам.
   *
   * @param aFrontend {@link IS5FrontendRear} frontend передающий события на обработку
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; - упорядоченный по времени список событий
   */
  void fireEvents( IS5FrontendRear aFrontend, ITimedList<SkEvent> aEvents );

  /**
   * Фронтенд сгенерировал события, которые бекенд должен асинхронно раздать всем, кроме этого фронтенда.
   * <p>
   * Предплоагается, что уж собственных подписчиков фронтед известит сам.
   *
   * @param aFrontend {@link IS5FrontendRear} frontend передающий события на обработку
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; - упорядоченный по времени список событий
   */
  void fireAsyncEvents( IS5FrontendRear aFrontend, ITimedList<SkEvent> aEvents );

  /**
   * Запрашивает события за заданный период времени.
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
   * @param aInterval {@link IQueryInterval} - запрошенный интервал времени
   * @param aNeededGwids {@link IGwidList} - список GWID-ов интересующих событий
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - список запрошенных событий
   */
  ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids );

  /**
   * Реализация передачи сообщений в систему
   *
   * @param aEvents {@link IMap}&lt;{@link IS5FrontendRear},{@link ITimedList}&lt;{@link SkEvent}&gt;&gt; карта
   *          отправляемых событий. <br>
   *          Ключ: фроненд формирующий события; <br>
   *          Значение: список формируемых событий.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void writeEventsImpl( IMap<IS5FrontendRear, ITimedList<SkEvent>> aEvents );
}
