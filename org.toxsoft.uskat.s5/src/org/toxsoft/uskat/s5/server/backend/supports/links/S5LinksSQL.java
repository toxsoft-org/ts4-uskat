package org.toxsoft.uskat.s5.server.backend.supports.links;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkFwdEntity.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5LinkID.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.entities.*;
import org.toxsoft.uskat.s5.server.logger.*;

import jakarta.persistence.*;

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
  private static final ILogger logger = LoggerWrapper.getLogger( S5LinksSQL.class );

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
   * Формат запроса связей по абстрактному {@link Gwid}-идентификатору.
   * <p>
   * <li>1. %s - Класс реализации описания, например,{@link S5DefaultLinkFwdEntity}/{@link S5DefaultLinkRevEntity};</li>
   * <li>2. %s - Идентификатор класса определящего связь;</li>
   * <li>3. %s - Идентификатор связи.</li>
   */
  private static final String QFRMT_GET_LINKS_BY_GWID2 = "(SELECT * FROM %s WHERE" + //
      "(" + FIELD_LINK_CLASSID + "='%s')AND(" + FIELD_LINKID + "='%s'))";

  /**
   * Возвращает все ПРЯМЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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
    Query query = aEntityManager.createQuery( sql, S5LinkFwdEntity.class );
    List<Object> retValue = query.getResultList();
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_FWD_LINKS_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return (List<IDtoLinkFwd>)(Object)retValue;
  }

  /**
   * Возвращает все ПРЯМЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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
    // Имя класса реализации
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
   * Возвращает все ПРЯМЫЕ связи по описанию абстрактной связи.
   *
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aLinkGwids {@link IGwidList} список описаний абстрактных связей
   * @return {@link IList}&lt;{@link S5LinkFwdEntity}&gt; список прямых связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IList<IDtoLinkFwd> getFwdLinksByLinkGwids( Connection aConnection, ISkSysdescrReader aSysdescrReader,
      IGwidList aLinkGwids ) {
    TsNullArgumentRtException.checkNulls( aConnection, aSysdescrReader, aLinkGwids );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();

    // Карта имен классов реализации прямых связей по идентификаторам классов
    IStringMapEdit<String> linkImplByClassIds = new StringMap<>();
    // Карта имен классов реализации прямых связей по идентификаторам связей
    IMapEdit<Gwid, IStringList> linkImplByGwids = new ElemMap<>();
    for( Gwid linkGwid : aLinkGwids ) {
      ISkClassInfo classInfo = aSysdescrReader.findClassInfo( linkGwid.classId() );
      IStridablesList<ISkClassInfo> sc = classInfo.listSubclasses( false, true ); // aOnlyChilds, aIncludeSelf
      IStringListEdit linkGwidImpls = new StringArrayList( sc.size() );
      for( ISkClassInfo c : sc ) {
        String classId = c.id();
        String linkImplClassName = OP_FWD_LINK_IMPL_CLASS.getValue( c.params() ).asString();
        linkImplByClassIds.put( classId, linkImplClassName );
        linkGwidImpls.add( linkImplClassName );
      }
      if( linkGwidImpls.size() > 0 ) {
        linkImplByGwids.put( linkGwid, linkGwidImpls );
      }
    }
    // Построитель SQL-запроса
    StringBuilder sqlBuilder = new StringBuilder();
    for( int index = 0, n = linkImplByGwids.keys().size(); index < n; index++ ) {
      Gwid linkGwid = linkImplByGwids.keys().get( index );
      IStringList linkImpls = linkImplByGwids.getByKey( linkGwid );
      for( int index2 = 0, n2 = linkImpls.size(); index2 < n2; index2++ ) {
        // Класс реализации хранения значений объекта
        String linkImplClassName = linkImpls.get( index2 );
        // Имя таблицы реализации хранения
        String tableName = getLast( linkImplClassName );
        // Текст SQL-подзапроса
        String subSql = format( QFRMT_GET_LINKS_BY_GWID2, tableName, linkGwid.classId(), linkGwid.propId() );
        // Формирование SQL-запроса
        sqlBuilder.append( subSql );
        if( index2 + 1 < n2 ) {
          sqlBuilder.append( "union" );
        }
      }
      if( index + 1 < n ) {
        sqlBuilder.append( "union" );
      }
    }
    // Текст SQL-полного запроса
    String sql = sqlBuilder.toString();
    // Сформирован SQL-запрос
    logger.info( MSG_READ_FWD_LINKS_BY_GWID_SQL, sql );
    // Выполнение запроса
    IList<IDtoLinkFwd> retValue = executeLinkFwdQuery( aConnection, linkImplByClassIds, sql );
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_FWD_LINKS_BY_GWID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return retValue;
  }

  /**
   * Возвращает все ОБРАТНЫЕ связи объектов указанного класса (без учета наследников)
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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
   * @param aEntityManager {@link EntityManager} менеджер постоянства
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

  /**
   * Выполнение запроса на получение связей.
   *
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aLinkImplClassNames {@link IStringMap}&lt;String&gt; карта имен классов реализации связей. <br>
   *          Ключ: имя класса объекта;<br>
   *          Значение: полное имя java-класса реализации связи
   * @param aSQL String текст SQL-запроса
   * @return {@link IList}&lt;{@link IDtoLinkFwd}&gt; список загруженных связей
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IList<IDtoLinkFwd> executeLinkFwdQuery( Connection aConnection, IStringMap<String> aLinkImplClassNames,
      String aSQL ) {
    TsNullArgumentRtException.checkNulls( aConnection, aLinkImplClassNames, aSQL );
    // Результат выполнения запроса
    IListEdit<IDtoLinkFwd> retValue = new ElemLinkedList<>();
    // Карта конструкторов объектов. Ключ: идентификатор класса; Значение: конструктор
    IStringMapEdit<Constructor<S5LinkFwdEntity>> objectContructors = new StringMap<>();
    try {
      // Выполнение запроса
      try( Statement statement =
          aConnection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
          ResultSet rs = statement.executeQuery( aSQL ); ) {
        for( boolean hasData = rs.first(); hasData; hasData = rs.next() ) {
          // Идентификатор класса объекта
          String classId = rs.getString( FIELD_CLASSID );
          // Конструктор c
          Constructor<S5LinkFwdEntity> linkConstructor = objectContructors.findByKey( classId );
          if( linkConstructor == null ) {
            // Конструктор еще неопределен
            String linkImplClassName = aLinkImplClassNames.getByKey( classId );
            linkConstructor = S5LinkFwdReflectUtils.getConstructorByResultSet( linkImplClassName );
            objectContructors.put( classId, linkConstructor );
          }
          retValue.add( linkConstructor.newInstance( rs ) );
        }
      }
    }
    catch( Throwable e ) {
      // Неожиданная ошибка выполнения запроса на получение объектов
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
    return retValue;
  }
}
