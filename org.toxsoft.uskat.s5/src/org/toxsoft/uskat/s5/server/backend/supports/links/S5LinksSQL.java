package org.toxsoft.uskat.s5.server.backend.supports.links;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkFwdEntity.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkID.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.s5.server.entities.S5DefaultLinkFwdEntity;
import org.toxsoft.uskat.s5.server.entities.S5DefaultLinkRevEntity;

/**
 * Служебные константы и методы для выполнения SQL-запросов
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
class S5LinksSQL {

  /**
   * Журнал работы
   */
  private static final ILogger logger = getLogger( S5LinksSQL.class );

  // ------------------------------------------------------------------------------------
  // Запросы
  //
  /**
   * Формат запроса всех связей объектов указанного класса
   * <p>
   * <li>1. %s - Класс реализации описания, например,{@link S5DefaultLinkFwdEntity}/{@link S5DefaultLinkRevEntity};</li>
   * <li>2. %s - Идентификатор класса левого/правого объекта связи.</li>
   */
  private static final String QFRMT_GET_LINKS_BY_CLASSID =
      "SELECT link FROM %s link WHERE (link." + FIELD_ID + "." + FIELD_CLASSID + "='%s' )";

  /**
   * Формат запроса связей объектов указанного класса
   * <p>
   * <li>1. %s - Класс реализации описания, например,{@link S5DefaultLinkFwdEntity}/{@link S5DefaultLinkRevEntity};</li>
   * <li>2. %s - Идентификатор класса левого/правого объекта связи;</li>
   * <li>3. %s - Идентификатор связи.</li>
   */
  private static final String QFRMT_GET_LINKS_BY_CLASSID_LINKID = "SELECT link FROM %s link WHERE" + //
      "(link." + FIELD_ID + "." + FIELD_CLASSID + "='%s')AND(link." + FIELD_ID + "." + FIELD_LINKID + "='%s')";

  /**
   * Формат запроса связей указанного объекта
   * <p>
   * <li>1. %s - Класс реализации описания, например,{@link S5DefaultLinkFwdEntity}/{@link S5DefaultLinkRevEntity};</li>
   * <li>2. %s - Идентификатор класса левого/правого объекта связи;</li>
   * <li>3. %s - Идентификатор левого/правого объекта связи.</li>
   */
  private static final String QFRMT_GET_LINKS_BY_OBJID = "SELECT link FROM %s link WHERE" + //
      "(link." + FIELD_ID + "." + FIELD_CLASSID + "='%s')AND(link." + FIELD_ID + "." + FIELD_STRID + "='%s')";

  /**
   * Возвращает все ПРЯМЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aLinkFwdImplClassName String полное имя класса реализации прямой связи, наследник {@link S5LinkFwdEntity}
   * @param aLefObjClassId String идентификатор класса левого объекта связи
   * @param aLefObjClassLinkId String идентификатор запрашиваемых связей. Пустая строка: все связи
   * @return {@link IList}&lt;{@link S5LinkFwdEntity}&gt; список прямых связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static List<IDtoLinkFwd> getFwdLinksByClassId( EntityManager aEntityManager, String aLinkFwdImplClassName,
      String aLefObjClassId, String aLefObjClassLinkId ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aLefObjClassId, aLefObjClassLinkId );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Текст SQL-запроса
    String tableName = getLast( aLinkFwdImplClassName );
    // Текст SQL-запроса
    String sql = aLefObjClassLinkId.length() == 0 ? //
        format( QFRMT_GET_LINKS_BY_CLASSID, tableName, aLefObjClassId ) : //
        format( QFRMT_GET_LINKS_BY_CLASSID_LINKID, tableName, aLefObjClassId, aLefObjClassLinkId );
    // Выполнение запроса
    Query query = aEntityManager.createQuery( sql );
    List<Object> retValue = query.getResultList();
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_FWD_LINKS_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return (List<IDtoLinkFwd>)(Object)retValue;
  }

  /**
   * Возвращает все ПРЯМЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aLinkFwdImplClassName String полное имя класса реализации прямой связи, наследник {@link S5LinkFwdEntity}
   * @param aLeftSkid {@link Skid} идентификатор левого объекта связи
   * @return {@link IList}&lt;{@link S5LinkFwdEntity}&gt; список прямых связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static List<IDtoLinkFwd> getFwdLinksByObjectId( EntityManager aEntityManager, String aLinkFwdImplClassName,
      Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aLeftSkid );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Текст SQL-запроса
    String tableName = getLast( aLinkFwdImplClassName );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_LINKS_BY_OBJID, tableName, aLeftSkid.classId(), aLeftSkid.strid() );
    // Выполнение запроса
    Query query = aEntityManager.createQuery( sql );
    List<Object> retValue = query.getResultList();
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_FWD_LINKS_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return (List<IDtoLinkFwd>)(Object)retValue;
  }

  /**
   * Возвращает все ОБРАТНЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aLinkRevImplClassName String полное имя класса реализации обратной связи, наследник {@link S5LinkRevEntity}
   * @param aRightObjClassId String идентификатор класса правого объекта связи
   * @return {@link IList}&lt;{@link S5LinkRevEntity}&gt; список обратных связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static List<IDtoLinkRev> getRevLinksByClassId( EntityManager aEntityManager, String aLinkRevImplClassName,
      String aRightObjClassId ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aRightObjClassId );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Текст SQL-запроса
    String tableName = getLast( aLinkRevImplClassName );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_LINKS_BY_CLASSID, tableName, aRightObjClassId );
    // Выполнение запроса
    Query query = aEntityManager.createQuery( sql );
    List<S5LinkRevEntity> retValue = query.getResultList();
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_REV_LINKS_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return (List<IDtoLinkRev>)(Object)retValue;
  }

  /**
   * Возвращает все ОБРАТНЫЕ связи указанного объекта (без учета наследников)
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aLinkRevImplClassName String полное имя класса реализации обратной связи, наследник {@link S5LinkRevEntity}
   * @param aRightSkid String идентификатор класса правого объекта связи
   * @return {@link IList}&lt;{@link S5LinkRevEntity}&gt; список обратных связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static List<IDtoLinkRev> getRevLinksByObjId( EntityManager aEntityManager, String aLinkRevImplClassName,
      Skid aRightSkid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aRightSkid );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Текст SQL-запроса
    String tableName = getLast( aLinkRevImplClassName );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_LINKS_BY_OBJID, tableName, aRightSkid.classId(), aRightSkid.strid() );
    // Выполнение запроса
    Query query = aEntityManager.createQuery( sql );
    List<S5LinkRevEntity> retValue = query.getResultList();
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_REV_LINKS_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return (List<IDtoLinkRev>)(Object)retValue;
  }

  /**
   * Удаляет ПРЯМЫЕ/ОБРАТНЫЕ связи левого/правого объектов указанного класса из базы данных
   *
   * @param aEntityManager {@link AbstractSkObjectManager} менеджер постоянства
   * @param aLinkImplClassName String полное имя класса реализации связи, наследника {@link S5LinkFwdEntity} или
   *          {@link S5LinkRevEntity}
   * @param aClassId String идентификатор класса левого объекта связи
   * @return int количество удаленных связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static int deleteLinksByClassId( EntityManager aEntityManager, String aLinkImplClassName, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aLinkImplClassName, aClassId );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Текст SQL-запроса
    String tableName = getLast( aLinkImplClassName );
    // Формирование запроса
    String sql = format( QFRMT_GET_LINKS_BY_CLASSID, tableName, aClassId );
    // Выполнение запроса
    Query query = aEntityManager.createQuery( sql );
    List<Object> retValue = query.getResultList();
    for( Object obj : retValue ) {
      aEntityManager.remove( obj );
    }
    // Журнал
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_DELETE_LINKS_BY_CLASSID_SQL_FINISH, tableName, Integer.valueOf( retValue.size() ), time );
    return retValue.size();
  }
}
