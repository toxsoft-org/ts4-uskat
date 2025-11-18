package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5LinksInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksReflectUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksSQL.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;
import javax.persistence.*;
import javax.sql.*;

import org.hibernate.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

/**
 * Реализация {@link IS5BackendLinksSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_OBJECTS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendLinksSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendLinksSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_LINKS_ID = "S5BackendLinksSingleton"; //$NON-NLS-1$

  /**
   * Менеджер постоянства
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * База данных
   */
  @Resource
  private DataSource dataSource;

  /**
   * Ядро сервера
   */
  @EJB
  private IS5BackendCoreSingleton backendCore;

  /**
   * backend управления классами системы
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления объектами системы
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * backend событий
   */
  @EJB
  private IS5BackendEventSingleton eventsBackend;

  /**
   * Читатель системного описания
   */
  private ISkSysdescrReader sysdescrReader;

  /**
   * Интерспетор операций проводимых над классами
   */
  private S5ClassesInterceptor classesInterceptor;

  /**
   * Интерспетор операций проводимых над объектами
   */
  private S5ObjectsInterceptor objectsInterceptor;

  /**
   * Поддержка интерсепторов операций проводимых над связями между объектами
   */
  private final S5InterceptorSupport<IS5LinksInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendLinksSingleton() {
    super( BACKEND_LINKS_ID, STR_D_BACKEND_LINKS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    sysdescrReader = sysdescrBackend.getReader();
    IS5BackendLinksSingleton business = sessionContext().getBusinessObject( IS5BackendLinksSingleton.class );
    classesInterceptor = new S5ClassesInterceptor( entityManager, transactionManager(), objectsBackend, business );
    objectsInterceptor = new S5ObjectsInterceptor( business );
    // TODO: mvkd отключено для проведения первичной отладки
    sysdescrBackend.addClassInterceptor( classesInterceptor, 0 );
    objectsBackend.addObjectsInterceptor( objectsInterceptor, 0 );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendLinksSingleton
  //
  @Override
  public void addLinksInterceptor( IS5LinksInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeLinksInterceptor( IS5LinksInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IBaLinks
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDtoLinkFwd findLinkFwd( Gwid aLinkGwid, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aLinkGwid, aLeftSkid );

    // Пред-интерсепция
    IDtoLinkFwd retValue = callBeforeFindLink( interceptors, aLinkGwid, aLeftSkid );

    if( retValue == null ) {
      String classId = aLinkGwid.classId();
      String linkId = aLinkGwid.propId();
      ISkClassInfo classInfo = sysdescrReader.findClassInfo( aLeftSkid.classId() );
      if( classInfo != null && classInfo.links().list().hasKey( linkId ) ) {
        Class<S5LinkFwdEntity> linkImplClass = getLinkFwdImplClass( classInfo );
        retValue = entityManager.find( linkImplClass, new S5LinkID( aLeftSkid, classId, linkId ) );
        if( retValue == null ) {
          // Проверяем если объект
          if( objectsBackend.findObject( aLeftSkid ) != null ) {
            // Связь с объектами не найдена (пустая)
            retValue = new DtoLinkFwd( Gwid.createLink( classId, linkId ), aLeftSkid, ISkidList.EMPTY );
          }
        }
      }
    }

    // Пост-интерсепция
    retValue = callAfterFindLink( interceptors, aLinkGwid, aLeftSkid, retValue );

    return retValue;
  }

  @Override
  public IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aLeftSkid );

    // Пред-интерсепция
    IList<IDtoLinkFwd> retValue = callBeforeGetAllLinksFwd( interceptors, aLeftSkid );
    if( retValue == null ) {
      ISkClassInfo classInfo = sysdescrReader.findClassInfo( aLeftSkid.classId() );
      String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( classInfo.params() ).asString();
      retValue = new ElemArrayList<>( getFwdLinksByObjectId( entityManager, linkImplClassName, aLeftSkid ) );
    }

    // Пост-интерсепция
    retValue = callAfterGetAllLinksFwd( interceptors, aLeftSkid, retValue );

    return retValue;
  }

  @Override
  public IDtoLinkRev findLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aLinkGwid, aRightSkid, aLeftClassIds );
    // Пред-интерсепция
    IDtoLinkRev retValue = callBeforeFindLinkRev( interceptors, aLinkGwid, aRightSkid, aLeftClassIds );

    if( retValue == null ) {
      String classId = aLinkGwid.classId();
      String linkId = aLinkGwid.propId();
      // Описание класса в котором определена связь
      ISkClassInfo linkClassInfo = sysdescrReader.getClassInfo( classId );
      if( !linkClassInfo.links().list().hasKey( linkId ) ) {
        // У класса нет указанной связи
        throw new TsItemNotFoundRtException( ERR_CLASS_DONT_HAVE_LINK, classId, linkId );
      }
      // Описание класса правого объекта связи
      ISkClassInfo classInfo = sysdescrReader.getClassInfo( aRightSkid.classId() );
      Class<S5LinkRevEntity> linkImplClass = getLinkRevImplClass( classInfo );
      retValue = entityManager.find( linkImplClass, new S5LinkID( aRightSkid, classId, linkId ) );
      if( retValue == null ) {
        // Проверяем есть ли объект
        if( objectsBackend.findObject( aRightSkid ) == null ) {
          // Объект не существует
          throw new TsItemNotFoundRtException( ERR_OBJECT_NOT_FOUND, aRightSkid );
        }
        // Связи на объект не найдены (пустая)
        return new DtoLinkRev( Gwid.createLink( classId, linkId ), aRightSkid, ISkidList.EMPTY );
      }
      if( aLeftClassIds.size() > 0 ) {
        // Фильтрация результата в соответствии с параметрами поиска
        SkidList skidList = new SkidList();
        for( Skid skid : retValue.leftSkids() ) {
          if( aLeftClassIds.hasElem( skid.classId() ) ) {
            skidList.add( skid );
          }
        }
        ((S5LinkRevEntity)retValue).setLeftSkids( skidList );
      }
    }

    // Пост-интерсепция
    retValue = callAfterFindLinkRev( interceptors, aLinkGwid, aRightSkid, aLeftClassIds, retValue );

    return retValue;
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    TsNullArgumentRtException.checkNull( aRightSkid );

    // Пред-интерсепция
    IMap<Gwid, IDtoLinkRev> retValue = callBeforeGetAllLinksRev( interceptors, aRightSkid );
    if( retValue == null ) {
      IMapEdit<Gwid, IDtoLinkRev> linksMap = new ElemMap<>();
      ISkClassInfo classInfo = sysdescrReader.getClassInfo( aRightSkid.classId() );
      String revLinkImplClassName = OP_REV_LINK_IMPL_CLASS.getValue( classInfo.params() ).asString();
      List<IDtoLinkRev> links = getRevLinksByObjId( entityManager, revLinkImplClassName, aRightSkid );
      for( IDtoLinkRev link : links ) {
        linksMap.put( link.gwid(), link );
      }
      retValue = (linksMap.size() > 0 ? linksMap : null);
    }

    // Пост-интерсепция
    retValue = callAfterGetAllLinksRev( interceptors, aRightSkid, retValue );

    return retValue;

  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    // aInterceptionEnabled = true
    writeLinksFwd( aLinks, true );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @SuppressWarnings( "unchecked" )
  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks, boolean aInterceptionEnabled ) {
    TsNullArgumentRtException.checkNull( aLinks );
    // Время начала выполнения запроса
    long currTime = System.currentTimeMillis();
    // Механизм поддержки записи связей
    S5LinkWriterSupport linkWriterSupport = new S5LinkWriterSupport( entityManager, sysdescrReader );
    // Загрузка карты обновляемых ПРЯМЫХ связей
    loadUpdatedLinks( linkWriterSupport, aLinks );
    // Карта обновляемых ПРЯМЫХ связей объектов по классам левого объекта связи
    IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>> updatedLinks =
        (IMap<ISkClassInfo, IList<Pair<IDtoLinkFwd, IDtoLinkFwd>>>)(Object)linkWriterSupport.updatedLinks;
    long loadTimestamp = System.currentTimeMillis();

    if( aInterceptionEnabled ) {
      // Пред-интерсепция
      callBeforeWriteLinks( interceptors, updatedLinks );
    }
    long interceptorTimestamp1 = System.currentTimeMillis();

    // Список идентификаторов изменившихся конкретных связей
    GwidList changedConrecteGwids = new GwidList();
    int removeCount = 0;
    int updateCount = 0;
    int createCount = 0;
    for( ISkClassInfo classInfo : updatedLinks.keys() ) {
      IList<Pair<IDtoLinkFwd, IDtoLinkFwd>> links = updatedLinks.getByKey( classInfo );
      for( Pair<IDtoLinkFwd, IDtoLinkFwd> link : links ) {
        IDtoLinkFwd prevLink = link.left();
        IDtoLinkFwd newLink = link.right();
        int prevCount = (prevLink != S5LinkFwdEntity.NULL ? prevLink.rightSkids().size() : 0);
        int newCount = newLink.rightSkids().size();
        if( prevCount > 0 && newCount == 0 ) {
          // Удаление существующих связей
          try {
            removeLinks( linkWriterSupport, prevLink, logger() );
            removeCount += 1 + prevCount;
          }
          catch( Throwable e ) {
            // Неожиданная ошибка записи связи(удаление)
            throw new TsInternalErrorRtException( e, ERR_WRITE_REMOVE_LINK, linkToStr( prevLink ), cause( e ) );
          }
        }
        if( prevCount > 0 && newCount > 0 ) {
          // Обновление существующих связей
          SkidList removedRightLinks = new SkidList();
          SkidList createdRightLinks = new SkidList();
          try {
            loadSkidsChanges( prevLink.rightSkids(), newLink.rightSkids(), removedRightLinks, createdRightLinks );
            updateLinks( linkWriterSupport, newLink, removedRightLinks, createdRightLinks, logger() );
          }
          catch( Throwable e ) {
            // Неожиданная ошибка записи связи(создание)
            throw new TsInternalErrorRtException( e, ERR_WRITE_EXIST_LINK, linkToStr( newLink ), cause( e ) );
          }
          removeCount += removedRightLinks.size();
          updateCount++;
          createCount += createdRightLinks.size();
        }
        if( prevCount == 0 && newCount > 0 ) {
          // Создание новых связей
          try {
            createdLinks( linkWriterSupport, newLink, logger() );
          }
          catch( Throwable e ) {
            if( e.getCause() instanceof TransientPropertyValueException ) {
              // Не найден один или более объектов связи
              TransientPropertyValueException tpe = (TransientPropertyValueException)e.getCause();
              throw new TsItemNotFoundRtException( ERR_NOT_FOUND_LINK_OBJ, linkToStr( newLink ),
                  tpe.getPropertyOwnerEntityName(), tpe.getPropertyName(), tpe.getTransientEntityName() );
            }
            // Неожиданная ошибка записи связи(создание)
            throw new TsInternalErrorRtException( e, ERR_WRITE_NEW_LINK, linkToStr( newLink ), cause( e ) );
          }
          createCount += 1 + newCount;
        }
        // Формирование списка идентификаторов изменившихся конкретных связей
        changedConrecteGwids
            .add( Gwid.createLink( newLink.leftSkid().classId(), newLink.leftSkid().strid(), newLink.linkId() ) );
      }
    }
    if( changedConrecteGwids.size() > 0 ) {
      // Формирование сообщения для фронтенда
      fireWhenLinksChanged( backendCore, changedConrecteGwids );
    }
    long entityManagerTimestamp1 = System.currentTimeMillis();

    // Счетчики для журнала
    Integer rc = Integer.valueOf( removeCount );
    Integer uc = Integer.valueOf( updateCount );
    Integer cc = Integer.valueOf( createCount );

    // Синхронизация с базой данных
    logger().debug( "writeLinksFwd(...): flush before. rc = %d, uc = %d, cc = %d", rc, uc, cc ); //$NON-NLS-1$
    entityManager.flush();
    logger().debug( "writeLinksFwd(...): flush after. rc = %d, uc = %d, cc = %d, time = %d msec", rc, uc, cc, //$NON-NLS-1$
        Long.valueOf( System.currentTimeMillis() - entityManagerTimestamp1 ) );
    long entityManagerTimestamp2 = System.currentTimeMillis();

    if( aInterceptionEnabled ) {
      // Пост-интерсепция
      callAfterWriteLinksInterceptors( interceptors, updatedLinks );
    }
    long interceptorTimestamp2 = System.currentTimeMillis();

    if( aInterceptionEnabled ) {
      // Формирование события
      // TODO: сформировать событие
      // events.add( createEvent( currTime, removedByClasses ) );
      // eventsBackend.writeEvents( events );
    }
    long eventTimestamp = System.currentTimeMillis();

    // Запись в журнал
    Long at = Long.valueOf( eventTimestamp - currTime );
    Long lt = Long.valueOf( loadTimestamp - currTime );
    Long it1 = Long.valueOf( interceptorTimestamp1 - loadTimestamp );
    Long et1 = Long.valueOf( entityManagerTimestamp1 - interceptorTimestamp1 );
    Long et2 = Long.valueOf( entityManagerTimestamp2 - entityManagerTimestamp1 );
    Long it2 = Long.valueOf( interceptorTimestamp2 - entityManagerTimestamp2 );
    Long et = Long.valueOf( eventTimestamp - interceptorTimestamp2 );

    logger().info( MSG_WRITE_LINKS, rc, uc, cc, at, lt, it1, et1, et2, it2, et );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public List<IDtoLinkFwd> getLinks( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    ISkClassInfo classInfo = sysdescrReader.getClassInfo( aClassId );
    // Класс реализации хранения значений объекта
    String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( classInfo.params() ).asString();
    return getFwdLinksByClassId( entityManager, linkImplClassName, aClassId, TsLibUtils.EMPTY_STRING );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public List<IDtoLinkFwd> getLinks( String aClassId, String aLinkId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId );
    ISkClassInfo classInfo = sysdescrReader.getClassInfo( aClassId );
    // Класс реализации хранения значений объекта
    String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( classInfo.params() ).asString();
    return getFwdLinksByClassId( entityManager, linkImplClassName, aClassId, aLinkId );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Формирует список обновляемых ПРЯМЫХ связей
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLinks {@link IList}&lt;{@link IDtoLinkFwd}&gt; список связей объектов записываемых в базу данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void loadUpdatedLinks( S5LinkWriterSupport aSupport, IList<IDtoLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNulls( aSupport, aLinks );
    // Карта конструкторов объектов. Ключ: идентификатор класса левого объекта связи; Значение: конструктор
    IStringMapEdit<Constructor<S5LinkFwdEntity>> linkFwdContructors = new StringMap<>();
    for( IDtoLinkFwd link : aLinks ) {
      String classId = link.leftSkid().classId();
      ISkClassInfo classInfo = getClassInfo( aSupport.sysdescrReader, aSupport.classesByIds, classId );
      // Класс реализации прямой связи
      Class<S5LinkFwdEntity> linkFwdImplClass = getLinkFwdImplClass( classInfo, aSupport.implLinkFwdByIds );
      // Создание новой связи объекта
      IDtoLinkFwd newLink = link;
      // Если связь объекта не может быть маппирована на базу данных, то создаем копию связи объекта
      if( newLink.getClass() != linkFwdImplClass ) {
        // Конструктор связи
        Constructor<S5LinkFwdEntity> linkFwdConstructor = linkFwdContructors.findByKey( classId );
        if( linkFwdConstructor == null ) {
          // Конструктор еще неопределен
          linkFwdConstructor = getConstructorLinkFwdBySource( linkFwdImplClass );
          linkFwdContructors.put( classId, linkFwdConstructor );
        }
        // Создание копии
        newLink = createLinkFwdEntity( linkFwdConstructor, link );
      }
      // Первичный ключ
      S5LinkID linkID = new S5LinkID( link.leftSkid(), link.classId(), link.linkId() );
      // Поиск существующей связи объекта
      IDtoLinkFwd prevLink = aSupport.entityManager.find( linkFwdImplClass, linkID );
      if( prevLink == null ) {
        // Связь не существует
        prevLink = S5LinkFwdEntity.NULL;
      }
      IListEdit<Pair<IDtoLinkFwd, IDtoLinkFwd>> links = aSupport.updatedLinks.findByKey( classInfo );
      if( links == null ) {
        links = new ElemArrayList<>( aLinks.size() );
        aSupport.updatedLinks.put( classInfo, links );
      }
      links.add( new Pair<>( prevLink, newLink ) );
    }
  }

  /**
   * Удаляет из базы данных связи объекта
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLinkFwd {@link IDtoLinkFwd} удаляемая ПРЯМАЯ связь объекта
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void removeLinks( S5LinkWriterSupport aSupport, IDtoLinkFwd aLinkFwd, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSupport, aLinkFwd, aLogger );
    String linkClassId = aLinkFwd.classId();
    Skid leftSkid = aLinkFwd.leftSkid();
    String linkId = aLinkFwd.linkId();
    for( Skid rightSkid : aLinkFwd.rightSkids() ) {
      removeLinkRev( aSupport, linkClassId, leftSkid, linkId, rightSkid, aLogger );
    }
    aSupport.entityManager.remove( aLinkFwd );
    aLogger.debug( "removeLinks(...): remove entity aLinkFwd=%s", aLinkFwd ); //$NON-NLS-1$
  }

  /**
   * Обновляет в базе данных связь объекта
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aNewLink {@link IDtoLinkFwd} новое состояние прямой связи объекта
   * @param aRemovedRightLinks {@link ISkidList} список удаленных правых связей
   * @param aCreatedRightLinks {@link ISkidList} список созданных правых связей
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void updateLinks( S5LinkWriterSupport aSupport, IDtoLinkFwd aNewLink, SkidList aRemovedRightLinks,
      SkidList aCreatedRightLinks, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSupport, aNewLink, aRemovedRightLinks, aCreatedRightLinks, aLogger );
    String linkClassId = aNewLink.classId();
    Skid leftSkid = aNewLink.leftSkid();
    String linkId = aNewLink.linkId();
    for( Skid rightSkid : aRemovedRightLinks ) {
      removeLinkRev( aSupport, linkClassId, leftSkid, linkId, rightSkid, aLogger );
    }
    for( Skid rightSkid : aCreatedRightLinks ) {
      createLinkRev( aSupport, leftSkid, linkClassId, linkId, rightSkid, aLogger );
    }
    // 2020-07-23 mvk
    // aSupport.entityManager.merge( aNewLink );
    ((S5LinkFwdEntity)aSupport.entityManager.merge( aNewLink )).setRightSkids( aNewLink.rightSkids() );
    aLogger.debug( "updateLinks(...): merge entity aNewLink=%s", aNewLink ); //$NON-NLS-1$
  }

  /**
   * Создает в базе данных связи объекта
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLinkFwd {@link IDtoLinkFwd} добавляемая ПРЯМАЯ связь объекта
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createdLinks( S5LinkWriterSupport aSupport, IDtoLinkFwd aLinkFwd, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSupport, aLinkFwd, aLogger );
    Skid leftSkid = aLinkFwd.leftSkid();
    String linkClassId = aLinkFwd.classId();
    String linkId = aLinkFwd.linkId();
    for( Skid rightSkid : aLinkFwd.rightSkids() ) {
      createLinkRev( aSupport, leftSkid, linkClassId, linkId, rightSkid, aLogger );
    }
    aSupport.entityManager.persist( aLinkFwd );
    aLogger.debug( "createdLinks(...): persist entity aLinkFwd=%s", aLinkFwd ); //$NON-NLS-1$
  }

  /**
   * Удаление обратной связи
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLinkClassId String идентификатор класса в котором определена связь
   * @param aLeftSkid {@link Skid} идентификатор левого объекта связи
   * @param aLinkId String идентификатор связи
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void removeLinkRev( S5LinkWriterSupport aSupport, String aLinkClassId, Skid aLeftSkid, String aLinkId,
      Skid aRightSkid, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aSupport, aLinkClassId, aLeftSkid, aLinkId, aRightSkid, aLogger );
    String classId = aRightSkid.classId();
    // Описание класса правого объекта
    ISkClassInfo classInfo = getClassInfo( aSupport.sysdescrReader, aSupport.classesByIds, classId );
    // Класс реализации обратной связи
    Class<S5LinkRevEntity> linkRevImplClass = getLinkRevImplClass( classInfo, aSupport.implLinkRevByIds );
    // Первичный ключ связи
    S5LinkID linkID = new S5LinkID( aRightSkid, aLinkClassId, aLinkId );
    // Обратная связь объекта
    S5LinkRevEntity revLink = aSupport.entityManager.find( linkRevImplClass, linkID );
    // 2021-01-30 mvk
    if( revLink == null ) {
      // Связь уже удалена
      aLogger.debug( "removeLinkRev(...): entity already removed. linkID=%s", linkID ); //$NON-NLS-1$
      return;
    }
    // Список объектов связи
    SkidList leftSkids = new SkidList( revLink.leftSkids() );
    leftSkids.remove( aLeftSkid );
    if( leftSkids.size() > 0 ) {
      // 2020-07-23 mvk
      // revLink.setLeftSkids( leftSkids );
      // aSupport.entityManager.merge( revLink );
      aSupport.entityManager.merge( revLink ).setLeftSkids( leftSkids );
      aLogger.debug( "removeLinkRev(...): merge entity revLink=%s, leftSkids=%s", revLink, leftSkids ); //$NON-NLS-1$
      return;
    }
    // 2021-01-27 mvk
    aSupport.entityManager.remove( revLink );
    aLogger.debug( "removeLinkRev(...): remove entity revLink=%s, leftSkids=IStridableList.EMPTY", revLink ); //$NON-NLS-1$
  }

  /**
   * Создание обратной связи
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLeftSkid {@link Skid} идентификатор левого объекта связи
   * @param aLinkClassId String идентификатор класса левого объекта связи в котором определена связь
   * @param aLinkId String идентификатор связи
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createLinkRev( S5LinkWriterSupport aSupport, Skid aLeftSkid, String aLinkClassId, String aLinkId,
      Skid aRightSkid, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aLeftSkid, aLinkClassId, aLinkId, aRightSkid, aLogger );
    String classId = aRightSkid.classId();
    // Описание класса правого объекта
    ISkClassInfo classInfo = getClassInfo( aSupport.sysdescrReader, aSupport.classesByIds, classId );
    // Класс реализации обратной связи
    Class<S5LinkRevEntity> linkRevImplClass = getLinkRevImplClass( classInfo, aSupport.implLinkRevByIds );
    // Первичный ключ связи
    S5LinkID linkID = new S5LinkID( aRightSkid, aLinkClassId, aLinkId );
    // Обратная связь объекта
    S5LinkRevEntity revLink = aSupport.entityManager.find( linkRevImplClass, linkID );
    if( revLink == null ) {
      // Конструктор обратной связи
      Constructor<S5LinkRevEntity> linkRevConstructor = aSupport.linkRevContructors.findByKey( classId );
      if( linkRevConstructor == null ) {
        // Конструктор еще неопределен
        linkRevConstructor = getConstructorLinkRevByParams( linkRevImplClass );
        aSupport.linkRevContructors.put( classId, linkRevConstructor );
      }
      revLink = createLinkRevEntity( linkRevConstructor, aRightSkid, aLinkClassId, aLinkId, new SkidList( aLeftSkid ) );
      aSupport.entityManager.persist( revLink );
      aLogger.debug( "createLinkRev(...): persist entity revLink=%s", revLink ); //$NON-NLS-1$
      return;
    }
    // Список объектов связи
    SkidList leftSkids = new SkidList( revLink.leftSkids() );
    if( !leftSkids.hasElem( aLeftSkid ) ) {
      leftSkids.add( aLeftSkid );
    }
    // 2020-07-23 mvk
    // revLink.setLeftSkids( leftSkids );
    // aSupport.entityManager.merge( revLink );
    aSupport.entityManager.merge( revLink ).setLeftSkids( leftSkids );
    aLogger.debug( "createLinkRev(...): merge entity revLink=%s, leftSkids=%s", revLink, leftSkids ); //$NON-NLS-1$
  }

  /**
   * Возвращает описание класса
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; карта описаний классов по их
   *          идентификаторам. <br>
   *          Ключ: идентификатор класса левого объекта связи;<br>
   *          Значение: описание класса.
   * @param aClassId String идентификатор класса
   * @return {@link ISkClassInfo} описание класса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static ISkClassInfo getClassInfo( ISkSysdescrReader aSysdescrReader,
      IStringMapEdit<ISkClassInfo> aClassesByIds, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aClassesByIds, aClassId );
    ISkClassInfo classInfo = aClassesByIds.findByKey( aClassId );
    if( classInfo == null ) {
      classInfo = aSysdescrReader.getClassInfo( aClassId );
      aClassesByIds.put( aClassId, classInfo );
    }
    return classInfo;
  }

  /**
   * Формирование события: произошло изменение связей объектов системы
   *
   * @param aEventer {@link IS5BackendEventer} передатчик сообщений бекенда
   * @param aChangedConcreteGwids {@link IGwidList} список изменных связей объектов.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenLinksChanged( IS5BackendEventer aEventer, IGwidList aChangedConcreteGwids ) {
    TsNullArgumentRtException.checkNulls( aEventer, aChangedConcreteGwids );
    GtMessage message = IBaLinksMessages.makeMessage( aChangedConcreteGwids );
    aEventer.fireBackendMessage( message );
  }

  /**
   * Возвращает текстовое представление связи
   *
   * @param aLink {@link IDtoLinkFwd} связь
   * @return String тествое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String linkToStr( IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    StringBuilder sb = new StringBuilder();
    sb.append( aLink.leftSkid() );
    sb.append( '=' );
    sb.append( '(' );
    sb.append( aLink.linkId() );
    sb.append( ')' );
    sb.append( '=' );
    sb.append( '>' );
    sb.append( '[' );
    for( int index = 0, n = aLink.rightSkids().size(); index < n; index++ ) {
      sb.append( aLink.rightSkids().get( index ) );
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    sb.append( ']' );
    return sb.toString();
  }

}
