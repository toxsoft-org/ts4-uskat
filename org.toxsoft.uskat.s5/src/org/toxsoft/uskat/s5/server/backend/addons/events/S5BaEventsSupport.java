package org.toxsoft.uskat.s5.server.backend.addons.events;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassHierarchyExplorer;
import org.toxsoft.uskat.core.impl.SkEventList;
import org.toxsoft.uskat.s5.legacy.SkGwidUtils;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Вспомогательный класс обработки событий {@link SkEvent}.
 *
 * @author mvk
 */
public final class S5BaEventsSupport
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Список идентификаторов прослушиваемых событий.
   * <p>
   * Список возможных {@link Gwid} идентифификаторов приведен в комментарии к методу
   * {@link ISkEventService#registerHandler(IGwidList, ISkEventHandler)}</code>.
   */
  private final GwidList gwids = new GwidList();

  /**
   * Блокировка доступа к {@link #gwids()}. (lazy)
   */
  private transient S5Lockable lock;

  /**
   * Журнал подсистемы. (lazy)
   */
  private transient ILogger logger;

  /**
   * Конструктор.
   */
  public S5BaEventsSupport() {
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает суммарный список всех {@link Gwid}-идентификаторов событий на которые зарегистрированы обработчики
   * событий
   *
   * @return {@link IGwidList} список идентификаторов событий
   */
  public IGwidList gwids() {
    lockRead( lock() );
    try {
      return new GwidList( gwids );
    }
    finally {
      unlockRead( lock() );
    }
  }

  /**
   * Указывет бекенду, о каких событиях следует извещать этот фронтенд.
   * <p>
   * Аргумент заменяет существующий до этого список подписки в бекенде. Пустой список означает, что фронтенд отказываеся
   * от подписки.
   * <p>
   * Описание содержимого и использования списка-аргумента приведено в комментарии к методу
   * {@link ISkEventService#registerHandler(IGwidList, ISkEventHandler)}</code>.
   *
   * @param aNeededGwids {@link IGwidList} - список GWID-ов интересующих событий
   */
  public void setNeededEventGwids( IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aNeededGwids );
    lockWrite( lock() );
    try {
      gwids.clear();
      for( Gwid gwid : aNeededGwids ) {
        if( gwids.hasElem( gwid ) ) {
          continue;
        }
        switch( gwid.kind() ) {
          case GW_CLASS:
          case GW_EVENT:
            gwids.add( gwid );
            // Подписка на события с идентификатором GWID
            logger().debug( "Subscription on event with GWID: %s", gwid ); //$NON-NLS-1$
            break;
          case GW_ATTR:
          case GW_CMD:
          case GW_CMD_ARG:
          case GW_EVENT_PARAM:
          case GW_LINK:
          case GW_RTDATA:
          case GW_CLOB:
          case GW_RIVET:
            // Попытка подписки на события с игнорируемый типом идентификатора GWID
            logger().warning( "Attempt to subscribe on ignored type of GWID: %s", gwid ); //$NON-NLS-1$
            break;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
    }
    finally {
      unlockWrite( lock() );
    }
  }

  /**
   * Фильтрует указанные события в соответствии с {@link #gwids}.
   *
   * @param aHierarchy {@link ISkClassHierarchyExplorer} поставщик информации об иерархии классов
   * @param aEvents {@link ITimedList} список событий для фильтрации
   * @return SkEventList список событий прошедших фильтрацию
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkEventList filter( ISkClassHierarchyExplorer aHierarchy, ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNulls( aHierarchy, aEvents );
    // Список отправляемых событий
    SkEventList events = new SkEventList();
    // Блокирование доступа на чтение карты обработчиков
    lockRead( lock() );
    try {
      // Формирование карты событий
      for( SkEvent event : aEvents ) {
        for( Gwid gwid : gwids ) {
          if( !SkGwidUtils.acceptableEvent( aHierarchy, gwid, event ) ) {
            continue;
          }
          // Событие принимается обработчиком
          events.add( event );
        }
      }
    }
    finally {
      // Разблокирование доступа на чтение карты обработчиков
      unlockRead( lock() );
    }
    return events;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает блокировку класса
   *
   * @return {@link S5Lockable} блокировка класса
   */
  private synchronized S5Lockable lock() {
    if( lock == null ) {
      lock = new S5Lockable();
    }
    return lock;
  }

  /**
   * Возвращает журнал подсистемы
   *
   * @return {@link ILogger} журнал подсистемы
   */
  private synchronized ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }

}
