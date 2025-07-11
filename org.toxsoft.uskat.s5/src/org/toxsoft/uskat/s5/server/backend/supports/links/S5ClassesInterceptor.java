package org.toxsoft.uskat.s5.server.backend.supports.links;

import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksReflectUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinksSQL.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import java.lang.reflect.*;
import java.util.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.transactions.*;

/**
 * Интерсептор системного описания используемый {@link S5BackendLinksSingleton}
 * <p>
 * Решаемые задачи:
 * <ul>
 * <li>Отслеживание изменения класса объекта. Добавление и/или удаление связей между объектами;</li>
 * </ul>
 *
 * @author mvk
 */
class S5ClassesInterceptor
    implements IS5ClassesInterceptor {

  private final EntityManager                  entityManager;
  private final IS5TransactionManagerSingleton txManager;
  private final IS5BackendObjectsSingleton     objectsBackend;
  private final IS5BackendLinksSingleton       linksBackend;

  /**
   * Конструктор
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aObjectsBackend {@link IS5BackendLinksSingleton} backend управления объектами
   * @param aLinksBackend {@link IS5BackendLinksSingleton} backend управления связями между объектами системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5ClassesInterceptor( EntityManager aEntityManager, IS5TransactionManagerSingleton aTransactionManager,
      IS5BackendObjectsSingleton aObjectsBackend, IS5BackendLinksSingleton aLinksBackend ) {
    entityManager = TsNullArgumentRtException.checkNull( aEntityManager );
    txManager = TsNullArgumentRtException.checkNull( aTransactionManager );
    objectsBackend = TsNullArgumentRtException.checkNull( aObjectsBackend );
    linksBackend = TsNullArgumentRtException.checkNull( aLinksBackend );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ClassesInterceptor
  //
  @Override
  public void beforeCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void beforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // Подготовка транзакции к перемещению реализации (объектов и/или их связей) если необходимо
    beforeChangeImplIfNeed( txManager, entityManager, aPrevClassInfo, aNewClassInfo );
  }

  @Override
  public void afterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // Завершение перемещения реализации (объектов и/или их связей) если необходимо
    afterChangeImplIfNeed( txManager, entityManager, aNewClassInfo );

    // Идентификатор изменяемого класса
    String classId = aNewClassInfo.id();
    // Список удаленных связей
    IStridablesListEdit<IDtoLinkInfo> removedLinks = new StridablesList<>();
    // Список добавленных связей
    IStridablesListEdit<IDtoLinkInfo> addedLinks = new StridablesList<>();
    // 2024-10-29 mvk ---+++
    // Анализ для формирования списка добавленных и удаленных связей.
    // loadSysdescrChangedProps( aPrevClassInfo.linkInfos(), aNewClassInfo.linkInfos(), removedLinks, addedLinks );
    // Внимание! Анализ только по идентификаторам связей.
    loadSysdescrChangedPropIds( aPrevClassInfo.linkInfos(), aNewClassInfo.linkInfos(), removedLinks, addedLinks );

    // TODO: отработать изменения ограничений по связи CollConstraint

    // Проверка есть ли добавленные или удаленные связи. Если нет, то проверка не требуется
    if( removedLinks.size() == 0 ) {
      return;
    }
    // Список объектов изменяющихся классов
    IList<IDtoObject> objs = S5TransactionUtils.txUpdatedClassObjs( txManager, objectsBackend, classId, aDescendants );
    if( objs.size() == 0 ) {
      // Нет объектов
      return;
    }
    // Список удаляемых ПРЯМЫХ связей удаляемых объектов.
    IListEdit<IDtoLinkFwd> removedFwdLinks = new ElemArrayList<>( removedLinks.size() * objs.size() );
    // Удаление реализаций прямых связей из таблиц которые больше неопределены в классе
    for( IDtoLinkInfo link : removedLinks ) {
      String linkId = link.id();
      List<IDtoLinkFwd> objLinks = linksBackend.getLinks( classId, linkId );
      for( IDtoLinkFwd objLink : objLinks ) {
        // При редактировании класса запрещается менять описание установленных связей между объектами (удалять их)
        TsIllegalArgumentRtException.checkTrue( objLink.rightSkids().size() > 0, ERR_ATTEMPT_REMOVE_LINKS_BY_EDIT_CLASS,
            classId, link, rightObjsToString( objLink ) );
        removedFwdLinks.add( objLink );
      }
    }
    if( removedFwdLinks.size() > 0 ) {
      // Запись в базу данных. false: запретить перехват
      linksBackend.writeLinksFwd( removedFwdLinks, false );
    }
  }

  @Override
  public void beforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5LinkFwdEntity -> S5ClassEntity
  }

  @Override
  public void afterDeleteClass( IDtoClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5LinkFwdEntity -> S5ClassEntity
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Если необходимо, то подготавливает транзакцию к перемещению реализации связей объектов из одной таблицы базы данных
   * в другую
   *
   * @param aTxManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aPrevClassInfo {@link IDtoClassInfo} описание класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание класса (новая редакция)
   * @return boolean <b>true</b> выполнен процесс перемещения реализации;<b>false</b> перемещение не требуется
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean beforeChangeImplIfNeed( IS5TransactionManagerSingleton aTxManager,
      EntityManager aEntityManager, IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo ) {
    TsNullArgumentRtException.checkNulls( aTxManager, aEntityManager, aPrevClassInfo, aNewClassInfo );
    // Класс реализации хранения связей
    String prevObjectImplClassName = OP_OBJECT_IMPL_CLASS.getValue( aPrevClassInfo.params() ).asString();
    String newObjectImplClassName = OP_OBJECT_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    String prevLinkFwdImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( aPrevClassInfo.params() ).asString();
    String newLinkFwdImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    String prevLinkRevImplClassName = OP_REV_LINK_IMPL_CLASS.getValue( aPrevClassInfo.params() ).asString();
    String newLinkRevImplClassName = OP_REV_LINK_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    // Признак изменения класса реализации связи (перемещаться могут только связи измененного класса без наследников
    // - у наследников собственное определение)
    boolean needMoving = ( //
    !prevObjectImplClassName.equals( newObjectImplClassName ) || //
        !prevLinkFwdImplClassName.equals( newLinkFwdImplClassName ) || //
        !prevLinkRevImplClassName.equals( newLinkRevImplClassName ));
    if( needMoving ) {
      String classId = aPrevClassInfo.id();
      Class<S5LinkFwdEntity> prevFwdClass = getLinkFwdImplClass( prevLinkFwdImplClassName );
      Class<S5LinkRevEntity> prevRevClass = getLinkRevImplClass( prevLinkRevImplClassName );
      List<IDtoLinkFwd> fwdLinks =
          getFwdLinksByClassId( aEntityManager, prevFwdClass.getName(), classId, TsLibUtils.EMPTY_STRING );
      List<IDtoLinkRev> revLinks = getRevLinksByClassId( aEntityManager, prevRevClass.getName(), classId );
      // Текущая транзакция
      IS5Transaction tx = aTxManager.getTransaction();
      // Размещение ресурсов в транзакции
      TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_FWD_LINKS_BY_CHANGE_IMPL, fwdLinks ) );
      TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_REV_LINKS_BY_CHANGE_IMPL, revLinks ) );
      // Удаление связей из старых таблиц
      if( fwdLinks.size() > 0 ) {
        deleteLinksByClassId( aEntityManager, prevFwdClass.getName(), classId );
      }
      if( revLinks.size() > 0 ) {
        deleteLinksByClassId( aEntityManager, prevRevClass.getName(), classId );
      }
    }
    return needMoving;
  }

  /**
   * Если необходимо, завершает перемещение реализации связей объектов из одной таблицы базы данных в другую
   *
   * @param aTxManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aNewClassInfo {@link IDtoClassInfo} описание класса (новая редакция)
   * @return boolean <b>true</b> выполнен процесс перемещения реализации;<b>false</b> перемещение не требуется
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean afterChangeImplIfNeed( IS5TransactionManagerSingleton aTxManager, EntityManager aEntityManager,
      IDtoClassInfo aNewClassInfo ) {
    TsNullArgumentRtException.checkNulls( aTxManager, aEntityManager, aNewClassInfo );
    // Текущая транзакция
    IS5Transaction tx = aTxManager.getTransaction();
    // Поиск ресурсов в транзакции
    List<IDtoLinkFwd> fwdLinks = tx.findResource( TX_UPDATED_FWD_LINKS_BY_CHANGE_IMPL );
    List<IDtoLinkRev> revLinks = tx.findResource( TX_UPDATED_REV_LINKS_BY_CHANGE_IMPL );
    if( fwdLinks == null || revLinks == null ) {
      return false;
    }
    // Классы реализации хранения связей
    String newLinkFwdImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    String newLinkRevImplClassName = OP_REV_LINK_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    Class<S5LinkFwdEntity> newFwdClass = getLinkFwdImplClass( newLinkFwdImplClassName );
    Class<S5LinkRevEntity> newRevClass = getLinkRevImplClass( newLinkRevImplClassName );
    // Конструктор копирования для ПРЯМОЙ связи новой реализации
    Constructor<S5LinkFwdEntity> fwdConstructor = getConstructorLinkFwdBySource( newFwdClass );
    for( IDtoLinkFwd link : fwdLinks ) {
      // 2020-07-21 mvk
      // aEntityManager.persist( createLinkFwdEntity( fwdConstructor, link ) );
      aEntityManager.merge( createLinkFwdEntity( fwdConstructor, link ) );
    }
    // Конструктор копирования для ОБРАТНЫОЙ связи новой реализации
    Constructor<S5LinkRevEntity> revConstructor = getConstructorLinkRevBySource( newRevClass );
    for( IDtoLinkRev link : revLinks ) {
      // 2020-07-21 mvk
      // aEntityManager.persist( createLinkRevEntity( revConstructor, link ) );
      aEntityManager.merge( createLinkRevEntity( revConstructor, link ) );
    }
    return true;
  }

  /**
   * Возвращает текстовое представление правых объектов связи
   *
   * @param aLink {@link IDtoLinkFwd} прямая связь объекта
   * @return String текстовое представление списка правых объектов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String rightObjsToString( IDtoLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    StringBuilder sb = new StringBuilder();
    int index = 0;
    int n = aLink.rightSkids().size();
    for( Skid obj : aLink.rightSkids() ) {
      index++;
      sb.append( obj );
      if( index > 2 ) {
        if( n > 3 ) {
          sb.append( "..." ); //$NON-NLS-1$
        }
        break;
      }
      if( index < n ) {
        sb.append( "," ); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }

}
