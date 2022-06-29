package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5LinksInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksReflectUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksSQL.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.impl.dto.DtoLinkFwd;
import org.toxsoft.uskat.core.impl.dto.DtoLinkRev;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

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
  // Реализация интерфейса ISkBackendLinksManagement
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDtoLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );

    // Пред-интерсепция
    IDtoLinkFwd retValue = callBeforeFindLink( interceptors, aClassId, aLinkId, aLeftSkid );

    if( retValue == null ) {
      ISkClassInfo classInfo = sysdescrReader.findClassInfo( aLeftSkid.classId() );
      if( classInfo != null && classInfo.links().list().hasKey( aLinkId ) ) {
        Class<S5LinkFwdEntity> linkImplClass = getLinkFwdImplClass( classInfo );
        retValue = entityManager.find( linkImplClass, new S5LinkID( aLeftSkid, aClassId, aLinkId ) );
        if( retValue == null ) {
          // Проверяем если объект
          if( objectsBackend.findObject( aLeftSkid ) != null ) {
            // Связь с объектами не найдена (пустая)
            retValue = new DtoLinkFwd( Gwid.createLink( aClassId, aLinkId ), aLeftSkid, ISkidList.EMPTY );
          }
        }
      }
    }

    // Пост-интерсепция
    retValue = callAfterFindLink( interceptors, aClassId, aLinkId, aLeftSkid, retValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDtoLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );

    // Пред-интерсепция
    IDtoLinkFwd retValue = callBeforeReadLink( interceptors, aClassId, aLinkId, aLeftSkid );

    if( retValue == null ) {
      // Описание класса левого объекта связи
      ISkClassInfo classInfo = sysdescrReader.getClassInfo( aLeftSkid.classId() );
      if( !classInfo.links().list().hasKey( aLinkId ) ) {
        // У объекта нет указанной связи
        throw new TsItemNotFoundRtException( MSG_ERR_OBJECT_DONT_HAVE_LINK, aLeftSkid, aLinkId );
      }
      Class<S5LinkFwdEntity> linkImplClass = getLinkFwdImplClass( classInfo );
      retValue = entityManager.find( linkImplClass, new S5LinkID( aLeftSkid, aClassId, aLinkId ) );
      if( retValue == null ) {
        // Проверяем если объект
        if( objectsBackend.findObject( aLeftSkid ) == null ) {
          // Нет объекта
          throw new TsItemNotFoundRtException( MSG_ERR_OBJECT_NOT_FOUND, aLeftSkid );
        }
        // Связь с объектами не найдена (пустая)
        retValue = new DtoLinkFwd( Gwid.createLink( aClassId, aLinkId ), aLeftSkid, ISkidList.EMPTY );
      }
    }

    // Пост-интерсепция
    retValue = callAfterReadLink( interceptors, aClassId, aLinkId, aLeftSkid, retValue );

    if( retValue == null ) {
      // У объекта нет указанной связи
      throw new TsItemNotFoundRtException( MSG_ERR_OBJECT_DONT_HAVE_LINK, aLeftSkid, aLinkId );
    }

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDtoLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid, aLeftClassIds );

    // Пред-интерсепция
    IDtoLinkRev retValue = callBeforeReadReverseLink( interceptors, aClassId, aLinkId, aRightSkid, aLeftClassIds );

    if( retValue == null ) {
      // Описание класса в котором определена связь
      ISkClassInfo linkClassInfo = sysdescrReader.getClassInfo( aClassId );
      if( !linkClassInfo.links().list().hasKey( aLinkId ) ) {
        // У класса нет указанной связи
        throw new TsItemNotFoundRtException( MSG_ERR_CLASS_DONT_HAVE_LINK, aClassId, aLinkId );
      }
      // Описание класса правого объекта связи
      ISkClassInfo classInfo = sysdescrReader.getClassInfo( aRightSkid.classId() );
      Class<S5LinkRevEntity> linkImplClass = getLinkRevImplClass( classInfo );
      retValue = entityManager.find( linkImplClass, new S5LinkID( aRightSkid, aClassId, aLinkId ) );
      if( retValue == null ) {
        // Проверяем есть ли объект
        if( objectsBackend.findObject( aRightSkid ) == null ) {
          // Объект не существует
          throw new TsItemNotFoundRtException( MSG_ERR_OBJECT_NOT_FOUND, aRightSkid );
        }
        // Связи на объект не найдены (пустая)
        return new DtoLinkRev( Gwid.createLink( aClassId, aLinkId ), aRightSkid, ISkidList.EMPTY );
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
    retValue = callAfterReadReverseLink( interceptors, aClassId, aLinkId, aRightSkid, aLeftClassIds, retValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeLink( IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNulls( aLink );
    // Запись связей. true: разрешить перехват
    writeLinks( new ElemArrayList<>( aLink ), true );
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

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @SuppressWarnings( "unchecked" )
  public void writeLinks( IList<IDtoLinkFwd> aLinks, boolean aInterceptionEnabled ) {
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
            throw new TsInternalErrorRtException( e, MSG_ERR_WRITE_REMOVE_LINK, linkToStr( prevLink ), cause( e ) );
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
            throw new TsInternalErrorRtException( e, MSG_ERR_WRITE_EXIST_LINK, linkToStr( newLink ), cause( e ) );
          }
          removeCount += removedRightLinks.size();
          updateCount++;
          createCount += createdRightLinks.size();
        }
        if( prevCount == 0 && newCount > 0 ) {
          // Создание новых связей
          try {
            createdLinks( linkWriterSupport, newLink );
          }
          catch( Throwable e ) {
            // Неожиданная ошибка записи связи(создание)
            throw new TsInternalErrorRtException( e, MSG_ERR_WRITE_NEW_LINK, linkToStr( newLink ), cause( e ) );
          }
          createCount += 1 + newCount;
        }
      }
    }
    long entityManagerTimestamp1 = System.currentTimeMillis();

    // Синхронизация с базой данных
    entityManager.flush();
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
    Integer rc = Integer.valueOf( removeCount );
    Integer uc = Integer.valueOf( updateCount );
    Integer cc = Integer.valueOf( createCount );
    Long at = Long.valueOf( eventTimestamp - currTime );
    Long lt = Long.valueOf( loadTimestamp - currTime );
    Long it1 = Long.valueOf( interceptorTimestamp1 - loadTimestamp );
    Long et1 = Long.valueOf( entityManagerTimestamp1 - interceptorTimestamp1 );
    Long et2 = Long.valueOf( entityManagerTimestamp2 - entityManagerTimestamp1 );
    Long it2 = Long.valueOf( interceptorTimestamp2 - entityManagerTimestamp2 );
    Long et = Long.valueOf( eventTimestamp - interceptorTimestamp2 );
    logger().info( MSG_WRITE_LINKS, rc, uc, cc, at, lt, it1, et1, et2, it2, et );
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
    aLogger.debug( "removeLinks(...): remove entity: aLinkFwd=%s", aLinkFwd ); //$NON-NLS-1$
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
      createLinkRev( aSupport, leftSkid, linkClassId, linkId, rightSkid );
    }
    // 2020-07-23 mvk
    // aSupport.entityManager.merge( aNewLink );
    ((S5LinkFwdEntity)aSupport.entityManager.merge( aNewLink )).setRightSkids( aNewLink.rightSkids() );
    aLogger.debug( "updateLinks(...): merge entity: aNewLink=%s", aNewLink ); //$NON-NLS-1$
  }

  /**
   * Создает в базе данных связи объекта
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLinkFwd {@link IDtoLinkFwd} добавляемая ПРЯМАЯ связь объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createdLinks( S5LinkWriterSupport aSupport, IDtoLinkFwd aLinkFwd ) {
    TsNullArgumentRtException.checkNulls( aSupport, aLinkFwd );
    Skid leftSkid = aLinkFwd.leftSkid();
    String linkClassId = aLinkFwd.classId();
    String linkId = aLinkFwd.linkId();
    for( Skid rightSkid : aLinkFwd.rightSkids() ) {
      createLinkRev( aSupport, leftSkid, linkClassId, linkId, rightSkid );
    }
    aSupport.entityManager.persist( aLinkFwd );
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
      aLogger.debug( "removeLinkRev(...): entity already removed: linkID=%s", linkID ); //$NON-NLS-1$
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
      aLogger.debug( "removeLinkRev(...): merge entity: revLink=%s, leftSkids=%s", revLink, leftSkids ); //$NON-NLS-1$
      return;
    }
    // 2021-01-27 mvk
    aSupport.entityManager.remove( revLink );
    aLogger.debug( "removeLinkRev(...): remove entity: revLink=%s, leftSkids=IStridableList.EMPTY", revLink ); //$NON-NLS-1$
  }

  /**
   * Создание обратной связи
   *
   * @param aSupport {@link S5LinkWriterSupport} вспомогательный механизм записи
   * @param aLeftSkid {@link Skid} идентификатор левого объекта связи
   * @param aLinkClassId String идентификатор класса левого объекта связи в котором определена связь
   * @param aLinkId String идентификатор связи
   * @param aRightSkid {@link Skid} идентификатор правого объекта связи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void createLinkRev( S5LinkWriterSupport aSupport, Skid aLeftSkid, String aLinkClassId, String aLinkId,
      Skid aRightSkid ) {
    TsNullArgumentRtException.checkNulls( aLeftSkid, aLinkClassId, aLinkId, aRightSkid );
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
