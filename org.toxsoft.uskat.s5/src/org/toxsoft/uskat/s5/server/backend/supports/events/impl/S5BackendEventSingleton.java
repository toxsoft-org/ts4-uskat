package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.legacy.SkGwidUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.events.S5BaEventsUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.events.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.server.backend.addons.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.collections.*;
import org.toxsoft.uskat.s5.utils.jobs.*;

/**
 * Реализация синглетона {@link IS5BackendEventSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendEventSingleton
    extends S5BackendSequenceSupportSingleton<IS5EventSequence, SkEvent>
    implements IS5BackendEventSingleton, IS5TransactionListener, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_EVENTS_ID = "S5BackendEventSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Конструктор.
   */
  public S5BackendEventSingleton() {
    super( BACKEND_EVENTS_ID, STR_D_BACKEND_EVENTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendEventSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void fireEvents( IS5FrontendRear aFrontend, ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aEvents );
    IS5Transaction transaction = transactionManager().findTransaction();
    if( transaction == null ) {
      // Нет транзакции, немедленная отправка сообщений
      IMapEdit<IS5FrontendRear, ITimedList<SkEvent>> events = new ElemMap<>();
      events.put( aFrontend, aEvents );
      writeEventsImpl( events );
      return;
    }
    // Текущая транзакция
    IS5Transaction tx = transactionManager().getTransaction();
    // Размещение событий в текущей транзакции
    IMapEdit<IS5FrontendRear, ITimedListEdit<SkEvent>> txEvents = tx.findResource( TX_FIRED_EVENTS );
    if( txEvents == null ) {
      // Событий еще нет в транзакции. Создаем карту
      txEvents = new ElemMap<>();
      tx.putResource( TX_FIRED_EVENTS, txEvents );
      // Регистрируемся на получение событий об изменении состояния транзакции
      tx.addListener( sessionContext().getBusinessObject( IS5TransactionListener.class ) );
    }
    ITimedListEdit<SkEvent> frontendEvents = txEvents.findByKey( aFrontend );
    if( frontendEvents == null ) {
      // frontend еще не передавал события в транзакции. Создаем список событий
      frontendEvents = new TimedList<>();
      txEvents.put( aFrontend, frontendEvents );
    }
    // Размещение события в списке событий
    frontendEvents.addAll( aEvents );
  }

  @Override
  @Asynchronous
  public void fireAsyncEvents( IS5FrontendRear aFrontend, ITimedList<SkEvent> aEvents ) {
    fireEvents( aFrontend, aEvents );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ITimedList<SkEvent> queryEvents( ITimeInterval aInterval, IGwidList aNeededGwids ) {
    TsNullArgumentRtException.checkNulls( aInterval, aNeededGwids );
    long traceStartTime = System.currentTimeMillis();
    // Подготовка списка идентификаторов запрашиваемых объектов. false: без повторов
    GwidList gwids = new GwidList();
    for( Gwid needGwid : aNeededGwids ) {
      if( needGwid.kind() != EGwidKind.GW_EVENT ) {
        // По контракту идентификаторы не события молча игнорируются
        continue;
      }
      if( !needGwid.isAbstract() ) {
        // Определен идентификатор объекта
        Gwid gwid = Gwid.createObj( needGwid.classId(), needGwid.strid() );
        if( !gwids.hasElem( gwid ) ) {
          gwids.add( gwid );
        }
        continue;
      }
      // Все объекты указанного класса. true: включая наследников
      IStringList classIds = sysdescrReader().getClassInfos( needGwid.classId() ).keys();
      if( classIds.size() == 0 ) {
        continue;
      }
      IList<IDtoObject> objs = objectsBackend().readObjects( classIds );
      for( IDtoObject obj : objs ) {
        Gwid gwid = Gwid.createObj( obj.classId(), obj.strid() );
        if( !gwids.hasElem( gwid ) ) {
          gwids.add( gwid );
        }
      }
    }

    // Чтение событий
    long traceReadStartTime = System.currentTimeMillis();
    IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, aInterval.startTime(), aInterval.endTime() );
    IMap<Gwid, IS5EventSequence> sequences = readSequences( gwids, interval, ACCESS_TIMEOUT_DEFAULT );
    long traceReadEndTime = System.currentTimeMillis();

    // Фильтрация событий и формирование сводного(по объектам) результата запроса
    IListEdit<SkEvent> events = new ElemLinkedList<>();
    for( IS5EventSequence sequence : sequences ) {
      for( IS5SequenceBlock<SkEvent> block : sequence.blocks() ) {
        for( int index = 0, n = block.size(); index < n; index++ ) {
          SkEvent event = block.getValue( index );
          for( Gwid gwid : aNeededGwids ) {
            if( acceptableEvent( sysdescrBackend(), gwid, event ) ) {
              events.add( event );
            }
          }
        }
      }
    }
    // Формирование результата. aAllowDuplicates = true
    ITimedListEdit<SkEvent> retValue = new S5FixedCapacityTimedList<>( events.size(), true );
    retValue.addAll( events );

    long traceResultTime = System.currentTimeMillis();
    // Формирование журнала
    Integer gc = Integer.valueOf( gwids.size() );
    Integer rc = Integer.valueOf( retValue.size() );
    Long pt = Long.valueOf( traceReadStartTime - traceStartTime );
    Long rt = Long.valueOf( traceReadEndTime - traceReadStartTime );
    Long ft = Long.valueOf( traceResultTime - traceReadEndTime );
    Long at = Long.valueOf( traceResultTime - traceStartTime );
    // Завершено чтение событий
    logger().info( MSG_READ_EVENTS, gc, aInterval, rc, at, pt, rt, ft );
    return retValue;
  }

  @Override
  @Asynchronous
  public void writeEventsImpl( IMap<IS5FrontendRear, ITimedList<SkEvent>> aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    if( aEvents.size() == 0 ) {
      return;
    }
    // Отправка сообщений об изменении связей только после успешного завершения транзакции
    for( IS5FrontendRear fireRaiser : aEvents.keys() ) {
      try {
        // Список событий для передачи frontend
        ITimedList<SkEvent> events = aEvents.getByKey( fireRaiser );

        if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
          StringBuilder sb = new StringBuilder();
          for( SkEvent event : events ) {
            sb.append( event );
            sb.append( "\n" ); //$NON-NLS-1$
          }
          logger().info( "fireEvents(...): %s", sb.toString() ); //$NON-NLS-1$
        }

        // Формирование последовательностей событий по объектам
        IList<IS5EventSequence> sequences = createEventSequences( factory(), events );
        // Cохранение событий в базе данных
        writeSequences( sequences );
        for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
          // 2024-10-17 mvk --- события могут быть переданы ВСЕМ, определяется только подпиской 
//          if( frontend.equals( fireRaiser ) ) {
//            // События не передаются frontend-у которые он отправил
//            continue;
//          }
          // Фильтрация интересуемых событий
          SkEventList frontendEvents =
              frontend.frontendData().findBackendAddonData( IBaEvents.ADDON_ID, S5BaEventsData.class ).events
                  .filter( sysdescrBackend(), events );
          if( frontendEvents.size() == 0 ) {
            // Нечего отправлять
            continue;
          }
          try {
            frontend.onBackendMessage( IBaEventsMessages.makeMessage( frontendEvents ) );
          }
          catch( Throwable e ) {
            // Ошибка доставки событий
            logger().error( e, MSG_ERR_ON_EVENTS, events2str( frontendEvents ), cause( e ) );
          }
        }
      }
      catch( Throwable e ) {
        logger().error( e );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5TransactionListener
  //
  @Override
  public void changeTransactionStatus( IS5Transaction aTransaction ) {
    ETransactionStatus txStatus = aTransaction.getStatus();
    switch( txStatus ) {
      case PREPARED:
        break;
      case COMMITED:
        IMap<IS5FrontendRear, ITimedList<SkEvent>> txEvents = aTransaction.findResource( TX_FIRED_EVENTS );
        if( txEvents == null ) {
          // В транзакции нет событий
          return;
        }
        // Асинхронная запись событий в систему
        IS5BackendEventSingleton eventSingleton = sessionContext().getBusinessObject( IS5BackendEventSingleton.class );
        eventSingleton.writeEventsImpl( txEvents );
        break;
      case ACTIVE:
      case PREPARING:
      case COMMITTING:
      case ROLLEDBACK:
      case ROLLING_BACK:
      case MARKED_ROLLBACK:
      case NO_TRANSACTION:
      case UNKNOWN:
        break;
      default:
        // TODO: ??? есть какие-то неизвестные состояния транзакции
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация асбтрактных методов S5BackendSequenceSupportSingleton
  //
  @Override
  protected IS5BackendEventSingleton getBusinessObject() {
    return sessionContext().getBusinessObject( IS5BackendEventSingleton.class );
  }

  @Override
  protected IS5SequenceFactory<SkEvent> doCreateFactory() {
    return new S5EventSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSequenceSupportSingleton
  //
  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    if( aPrevClassInfo.eventInfos().size() > 0 && aNewClassInfo.eventInfos().size() == 0 ) {
      // Удаление идентификаторов данных объектов у которых больше нет событий
      S5SequenceFactory factory = ((S5SequenceFactory)factory());
      // Список объектов изменившихся классов
      IList<IDtoObject> objs = S5TransactionUtils.txUpdatedClassObjs( transactionManager(), objectsBackend(),
          aNewClassInfo.id(), aDescendants );
      for( IDtoObject obj : objs ) {
        String classId = obj.classId();
        ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
        if( classInfo.events().list().size() == 0 ) {
          factory.removeTypeInfo( Gwid.createObj( classId, obj.strid() ) );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doBeforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // Удаление идентификаторов данных объектов удаленного класса
    if( aClassInfo.eventInfos().size() > 0 ) {
      // Удаление идентификаторов данных объектов удаленного класса
      ((S5SequenceFactory)factory()).removeTypeInfo( aClassInfo.id() );
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // Удаление идентификаторов данных удаленных объектов
    S5SequenceFactory factory = ((S5SequenceFactory)factory());
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      if( classInfo.events().list().size() == 0 ) {
        // У класса объекта нет событий
        continue;
      }
      IList<IDtoObject> objs = aRemovedObjs.getByKey( classInfo );
      for( IDtoObject obj : objs ) {
        factory.removeTypeInfo( Gwid.createObj( obj.classId(), obj.strid() ) );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

  /**
   * Создает последовательности событий по объектам
   *
   * @param aFactory {@link S5EventSequenceFactory} фабрика последовательностей
   * @param aEvents {@link ITimedList} список событий по всем объектам
   * @return {@link IList}&lt;{@link IS5EventSequence}&gt; последовательность событий по объектам
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  @SuppressWarnings( "unchecked" )
  private static IList<IS5EventSequence> createEventSequences( S5EventSequenceFactory aFactory,
      ITimedList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNulls( aFactory, aEvents );
    // Карта событий по объектам. Ключ: идентификатор объекта. Значение: список событий
    IMapEdit<Skid, TimedList<SkEvent>> eventsByObjs = new ElemMap<>();
    for( SkEvent event : aEvents ) {
      Skid skid = event.eventGwid().skid();
      TimedList<SkEvent> objEvents = eventsByObjs.findByKey( skid );
      if( objEvents == null ) {
        objEvents = new TimedList<>();
        eventsByObjs.put( skid, objEvents );
      }
      objEvents.add( event );
    }
    // Формирование последовательностей
    IListEdit<IS5EventSequenceEdit> retValue = new ElemArrayList<>( eventsByObjs.keys().size() );
    for( Skid skid : eventsByObjs.keys() ) {
      TimedList<SkEvent> events = eventsByObjs.getByKey( skid );
      if( events.size() == 0 ) {
        continue;
      }
      Gwid objId = Gwid.createObj( skid.classId(), skid.strid() );
      IQueryInterval interval =
          new QueryInterval( EQueryIntervalType.CSCE, events.first().timestamp(), events.last().timestamp() );
      IS5EventSequenceEdit sequence = new S5EventSequence( aFactory, objId, interval, IList.EMPTY );
      sequence.set( events );
      retValue.add( sequence );
    }
    return (IList<IS5EventSequence>)(Object)retValue;
  }
}
