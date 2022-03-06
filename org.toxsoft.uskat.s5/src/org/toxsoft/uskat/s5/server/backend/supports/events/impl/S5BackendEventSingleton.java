package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import static org.toxsoft.uskat.core.impl.S5EventsUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.realtime.S5RealtimeUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.events.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;
import static ru.uskat.core.impl.SkGwidUtils.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.addons.realtime.S5RealtimeFrontendData;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.IS5EventSequence;
import org.toxsoft.uskat.s5.server.backend.supports.events.sequences.IS5EventSequenceEdit;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.collections.S5FixedCapacityTimedList;

import ru.uskat.backend.messages.SkMessageWhenEvents;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.common.dpu.rt.events.SkEventList;
import ru.uskat.core.api.sysdescr.ISkClassInfo;

/**
 * Реализация синглетона {@link IS5BackendEventSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LOBS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendEventSingleton
    extends S5BackendSequenceSupportSingleton<IS5EventSequence, SkEvent>
    implements IS5BackendEventSingleton, IS5TransactionListener {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_EVENTS_ID = "S5BackendEventSingleton"; //$NON-NLS-1$

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
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
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
        Gwid gwid = Gwid.create( needGwid.classId(), needGwid.strid(), null, null );
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
      IList<IDpuObject> objs = objectsBackend().readObjects( classIds );
      for( IDpuObject obj : objs ) {
        Gwid gwid = Gwid.create( obj.classId(), obj.strid(), null, null );
        if( !gwids.hasElem( gwid ) ) {
          gwids.add( gwid );
        }
      }
    }

    // Чтение событий
    long traceReadStartTime = System.currentTimeMillis();
    IList<IS5EventSequence> sequences = readSequences( gwids, aInterval, ACCESS_TIMEOUT_DEFAULT );
    long traceReadEndTime = System.currentTimeMillis();

    // Фильтрация событий и формирование сводного(по объектам) результата запроса
    IListEdit<SkEvent> events = new ElemLinkedList<>();
    for( IS5EventSequence sequence : sequences ) {
      for( ISequenceBlock<SkEvent> block : sequence.blocks() ) {
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
    // Отправка сообщений об изменении связей только после успешного завершения транзакции
    for( IS5FrontendRear fireRaiser : aEvents.keys() ) {
      try {
        // Список событий для передачи frontend
        ITimedList<SkEvent> events = aEvents.getByKey( fireRaiser );
        // Формирование последовательностей событий по объектам
        IList<IS5EventSequence> sequences = createEventSequences( factory(), events );
        // Cохранение событий в базе данных
        writeSequences( sequences );
        // Передача событий frontend-ам
        for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
          if( frontend.equals( fireRaiser ) ) {
            // События не передаются frontend-у которые он отправил
            continue;
          }
          S5RealtimeFrontendData frontendData = getRealtimeFrontendData( frontend );
          if( frontendData == null ) {
            // Фронтенд не поддерживает реальное время
            continue;
          }
          // Фильтрация интересуемых событий
          SkEventList frontendEvents = frontendData.events.filter( sysdescrBackend(), events );
          if( frontendEvents.size() == 0 ) {
            // Нечего отправлять
            continue;
          }
          try {
            SkMessageWhenEvents.send( frontend, frontendEvents );
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
  protected ISequenceFactory<SkEvent> doCreateFactory() {
    return new S5EventSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSequenceSupportSingleton
  //
  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    if( aPrevClassInfo.eventInfos().size() > 0 && aNewClassInfo.eventInfos().size() == 0 ) {
      // Удаление идентификаторов данных объектов у которых больше нет событий
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      // Список объектов изменившихся классов
      IList<IDpuObject> objs = S5TransactionUtils.txUpdatedClassObjs( transactionManager(), objectsBackend(),
          aNewClassInfo.id(), aDescendants );
      for( IDpuObject obj : objs ) {
        String classId = obj.classId();
        ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
        if( classInfo.eventInfos().size() == 0 ) {
          gwidsEditor.removeByKey( Gwid.create( classId, obj.strid(), null, null ) );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doBeforeDeleteClass( IDpuSdClassInfo aClassInfo ) {
    String classId = aClassInfo.id();
    // Удаление идентификаторов данных объектов удаленного класса
    if( aClassInfo.eventInfos().size() > 0 ) {
      // Удаление идентификаторов данных объектов удаленного класса
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      for( Gwid gwid : new ElemArrayList<>( gwidsEditor.keys() ) ) {
        if( gwid.classId().equals( classId ) ) {
          gwidsEditor.removeByKey( gwid );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // Удаление идентификаторов данных удаленных объектов
    IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      if( classInfo.eventInfos().size() == 0 ) {
        // У класса объекта нет событий
        continue;
      }
      IList<IDpuObject> objs = aRemovedObjs.getByKey( classInfo );
      for( IDpuObject obj : objs ) {
        gwidsEditor.removeByKey( Gwid.create( obj.classId(), obj.strid(), null, null ) );
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
      Gwid objId = Gwid.create( skid.classId(), skid.strid(), null, null );
      IQueryInterval interval = new QueryInterval( CSCE, events.first().timestamp(), events.last().timestamp() );
      IS5EventSequenceEdit sequence = new S5EventSequence( aFactory, objId, interval, IList.EMPTY );
      sequence.set( events );
      retValue.add( sequence );
    }
    return (IList<IS5EventSequence>)(Object)retValue;
  }
}