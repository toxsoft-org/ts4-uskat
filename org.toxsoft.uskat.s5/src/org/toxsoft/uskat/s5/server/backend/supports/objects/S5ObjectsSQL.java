package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectID.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectReflectUtils.*;

import java.lang.reflect.Constructor;
import java.sql.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Служебные константы и методы для выполнения SQL-запросов
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
class S5ObjectsSQL {

  /**
   * Журнал работы
   */
  private static final ILogger logger = getLogger( S5ObjectsSQL.class );

  // ------------------------------------------------------------------------------------
  // Запросы
  //
  /**
   * Запрос объектов по некоторому условию выбора
   * <p>
   * <li>1. %s - Имя таблицы хранения объекта, например, {@link S5ObjectEntity} ;</li>
   * <li>2. %s - Условие выбора.</li>
   */
  static final String QFRMT_GET_OBJECTS = "(SELECT * FROM %s WHERE%s)";

  /**
   * Условие выбора класса
   * <p>
   * <li>1. %s - Идентификато класса объекта (classId).</li>
   */
  static final String CFRMT_GET_BY_CLASSID = "(" + FIELD_CLASSID + "='%s')";

  /**
   * Загрузка объектов по классам
   *
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aClassIds {@link ISkidList} список идентификаторов классов запрашиваемых объектов
   * @return IList&lt;{@link IDpuObject}&lt;V&gt;&gt; данные объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IList<IDpuObject> loadByClasses( Connection aConnection, ISkSysdescrReader aSysdescrReader,
      IStringList aClassIds ) {
    TsNullArgumentRtException.checkNulls( aConnection, aSysdescrReader, aClassIds );
    // Карта имен классов реализации объектов по идентификаторам классов
    IStringMapEdit<String> objectImplClassNames = new StringMap<>();
    // Карта построителей условий по таблицам. Ключ: имя таблицы, значение построитель строки условия
    IStringMapEdit<StringBuilder> whereBuildersByTables = new StringMap<>();
    for( int index = 0, n = aClassIds.size(); index < n; index++ ) {
      // Идентификатор класса
      String classId = aClassIds.get( index );
      // Класс реализации хранения значений объекта
      String objectImplClassName = objectImplClassNames.findByKey( classId );
      if( objectImplClassName == null ) {
        // Реализация объектов еще неопределена
        // Описание класса объекта
        ISkClassInfo classInfo = aSysdescrReader.getClassInfo( classId );
        // Класс реализации хранения значений объекта
        objectImplClassName = DDEF_OBJECT_IMPL_CLASS.getValue( classInfo.params() ).asString();
        // Сохраняем имя класса реализации объектов в карте
        objectImplClassNames.put( classId, objectImplClassName );
      }
      // Имя таблицы реализации хранения
      String tableName = getLast( objectImplClassName );
      // Построитель условия выбора на таблицы
      StringBuilder whereBuilder = whereBuildersByTables.findByKey( tableName );
      if( whereBuilder == null ) {
        whereBuilder = new StringBuilder();
        whereBuildersByTables.put( tableName, whereBuilder );
      }
      if( whereBuilder.length() > 0 ) {
        whereBuilder.append( "OR" );
      }
      whereBuilder.append( format( CFRMT_GET_BY_CLASSID, classId ) );
    }
    // Текст SQL-запроса
    String sql = getGetObjectsSQL( whereBuildersByTables );
    // Сформирован SQL-запрос
    logger.info( MSG_READ_OBJ_BY_CLASSID_SQL_START, sql );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Выполнение запроса
    IList<IDpuObject> retValue = executeQuery( aConnection, objectImplClassNames, sql );
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_OBJ_BY_CLASSID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return retValue;
  }

  /**
   * Запрос объекта по skid-идентификатору
   * <p>
   * <li>2. %s - Идентификато класса объекта (classId);</li>
   * <li>3. %s - Строковый идентификатор объекта (strid).</li>
   */
  static final String CFRMT_GET_BY_SKID = "((" + FIELD_CLASSID + "='%s')AND(" + FIELD_STRID + "='%s'))";

  /**
   * Загрузка объектов по skid-идентификаторам
   * <p>
   * Если объект не существует, то он молча игнорируется
   *
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aSkids {@link ISkidList} список идентификаторов запрашиваемых объектов
   * @return IList&lt;{@link IDpuObject}&lt;V&gt;&gt; данные объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IList<IDpuObject> loadBySkids( Connection aConnection, ISkSysdescrReader aSysdescrReader, ISkidList aSkids ) {
    TsNullArgumentRtException.checkNulls( aConnection, aSysdescrReader, aSkids );
    // Карта имен классов реализации объектов по идентификаторам классов
    IStringMapEdit<String> objectImplClassNames = new StringMap<>();
    // Карта построителей условий по таблицам. Ключ: имя таблицы, значение построитель строки условия
    IStringMapEdit<StringBuilder> conditionBuildersByTables = new StringMap<>();
    for( int index = 0, n = aSkids.size(); index < n; index++ ) {
      // Идентификатор объекта
      Skid skid = aSkids.get( index );
      // Идентификатор класса
      String classId = skid.classId();
      // Класс реализации хранения значений объекта
      String objectImplClassName = objectImplClassNames.findByKey( classId );
      if( objectImplClassName == null ) {
        // Реализация объектов еще неопределена
        // Описание класса объекта
        ISkClassInfo classInfo = aSysdescrReader.findClassInfo( classId );
        if( classInfo == null ) {
          // Класс объекта не существует, контракт допускает пропуск таких объектов
          continue;
        }
        // Класс реализации хранения значений объекта
        objectImplClassName = DDEF_OBJECT_IMPL_CLASS.getValue( classInfo.params() ).asString();
        // Сохраняем имя класса реализации объектов в карте
        objectImplClassNames.put( classId, objectImplClassName );
      }
      // Имя таблицы реализации хранения
      String tableName = getLast( objectImplClassName );
      // Построитель условия выбора на таблицы
      StringBuilder tableConditionBuilder = conditionBuildersByTables.findByKey( tableName );
      if( tableConditionBuilder == null ) {
        tableConditionBuilder = new StringBuilder();
        conditionBuildersByTables.put( tableName, tableConditionBuilder );
      }
      if( tableConditionBuilder.length() > 0 ) {
        tableConditionBuilder.append( "OR" );
      }
      tableConditionBuilder.append( format( CFRMT_GET_BY_SKID, classId, skid.strid() ) );
    }
    if( conditionBuildersByTables.size() == 0 ) {
      // Не было найдено ни одного объекта, контракт допускает пустой результат
      return IList.EMPTY;
    }
    // Текст SQL-запроса
    String sql = getGetObjectsSQL( conditionBuildersByTables );
    // Сформирован SQL-запрос
    logger.info( MSG_READ_OBJ_BY_SKID_SQL_START, sql );
    // Время начала выполнения запроса
    long traceStartTime = System.currentTimeMillis();
    // Выполнение запроса
    IList<IDpuObject> retValue = executeQuery( aConnection, objectImplClassNames, sql );
    // Получен результат запроса
    Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    logger.info( MSG_READ_OBJ_BY_SKID_SQL_FINISH, Integer.valueOf( retValue.size() ), time );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Формирует текст SQL-запроса получения объектов из разных таблиц с разными условиями
   *
   * @param aWhereBuildersByTables {@link IStringMap}&lt;{@link StringBuilder}&gt; карта условий по таблицам. <br>
   *          Ключ: имя таблицы;<br>
   *          Значение: построитель текста условия.
   * @return String текст SQL-запроса
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static String getGetObjectsSQL( IStringMap<StringBuilder> aWhereBuildersByTables ) {
    TsNullArgumentRtException.checkNull( aWhereBuildersByTables );
    // Построитель SQL-запроса
    StringBuilder sqlBuilder = new StringBuilder();
    for( int index = 0, n = aWhereBuildersByTables.size(); index < n; index++ ) {
      // Таблица
      String tableName = aWhereBuildersByTables.keys().get( index );
      // Условие на таблице
      String whereCondition = aWhereBuildersByTables.values().get( index ).toString();
      // Текст SQL-подзапроса
      String subSql = format( QFRMT_GET_OBJECTS, tableName, whereCondition );
      // Формирование SQL-запроса
      sqlBuilder.append( subSql );
      if( index + 1 < n ) {
        sqlBuilder.append( "union" );
      }
    }
    return sqlBuilder.toString();
  }

  /**
   * Выполнение запроса на получение объектов
   *
   * @param aConnection {@link Connection} соединение с базой данных
   * @param aObjectImplClassNames {@link IStringMap}&lt;String&gt; карта имен классов реализации объектов. <br>
   *          Ключ: имя класса объекта;<br>
   *          Значение: полное имя java-класса реализации объекта
   * @param aSQL String текст SQL-запроса
   * @return {@link IList}&lt;{@link IDpuObject}&gt; список загруженных объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IList<IDpuObject> executeQuery( Connection aConnection, IStringMap<String> aObjectImplClassNames,
      String aSQL ) {
    TsNullArgumentRtException.checkNulls( aConnection, aObjectImplClassNames, aSQL );
    // Результат выполнения запроса
    IListEdit<IDpuObject> retValue = new ElemLinkedList<>();
    // Карта конструкторов объектов. Ключ: идентификатор класса; Значение: конструктор
    IStringMapEdit<Constructor<S5ObjectEntity>> objectContructors = new StringMap<>();
    try {
      // Выполнение запроса
      try( Statement statement = aConnection.createStatement(); ResultSet rs = statement.executeQuery( aSQL ); ) {
        for( boolean hasData = rs.first(); hasData; hasData = rs.next() ) {
          // Идентификатор класса объекта
          String classId = rs.getString( FIELD_CLASSID );
          // Конструктор объекта
          Constructor<S5ObjectEntity> objectConstructor = objectContructors.findByKey( classId );
          if( objectConstructor == null ) {
            // Конструктор еще неопределен
            String objectImplClassName = aObjectImplClassNames.getByKey( classId );
            objectConstructor = getConstructorByResultSet( objectImplClassName );
            objectContructors.put( classId, objectConstructor );
          }
          retValue.add( objectConstructor.newInstance( rs ) );
        }
      }
    }
    catch( Throwable e ) {
      // Неожиданная ошибка выполнения запроса на получение объектов
      throw new TsInternalErrorRtException( e, ERR_READ_JDBC_UNEXPECTED, cause( e ) );
    }
    return retValue;
  }

  /**
   * Запрос объектов по некоторому условию выбора
   * <p>
   * <li>1. %s - Имя таблицы хранения объекта, например, {@link S5ObjectEntity} ;</li>
   */
  private static final String QFRMT_INSERT_OBJECT = //
      "insert into %s" //
          + "(" + FIELD_CLASSID + "," + FIELD_STRID + "," + FIELD_ATTRS_STRING + ")values" //
          + "(:" + FIELD_CLASSID + ",:" + FIELD_STRID + ",:" + FIELD_ATTRS_STRING + ")";

  /**
   * Сохранить новый объект в базе данных
   * <p>
   * TODO: mvkd эксперименты с native-SQL для insert и update
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aObject {@link IDpuObject} объект
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void createObject( EntityManager aEntityManager, IDpuObject aObject ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aObject );
    // Текст SQL-запроса
    String sql = format( QFRMT_INSERT_OBJECT, "S5DefaultObjectEntity" );

    Query query = aEntityManager.createNativeQuery( sql );
    query.setParameter( FIELD_CLASSID, aObject.classId() );
    query.setParameter( FIELD_STRID, aObject.strid() );
    query.setParameter( FIELD_ATTRS_STRING, OptionSetKeeper.KEEPER.ent2str( aObject.attrs() ) );
    query.executeUpdate();
  }

  /**
   * Запрос объектов по некоторому условию выбора
   * <p>
   * <li>1. %s - Имя таблицы хранения объекта, например, {@link S5ObjectEntity} ;</li>
   */
  private static final String QFRMT_UPDATE_OBJECT = //
      "update %s set " //
          + FIELD_ATTRS_STRING + "=:" + FIELD_ATTRS_STRING + " " //
          + "where " + FIELD_CLASSID + "=:" + FIELD_CLASSID + " and " + FIELD_STRID + "=:" + FIELD_STRID;

  /**
   * Сохранить новый объект в базе данных
   * <p>
   * TODO: mvkd эксперименты с native-SQL для insert и update
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aObject {@link IDpuObject} объект
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void updateObject( EntityManager aEntityManager, IDpuObject aObject ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aObject );
    // Текст SQL-запроса
    String sql = format( QFRMT_UPDATE_OBJECT, "S5DefaultObjectEntity" );

    Query query = aEntityManager.createNativeQuery( sql );
    query.setParameter( FIELD_CLASSID, aObject.classId() );
    query.setParameter( FIELD_STRID, aObject.strid() );
    query.setParameter( FIELD_ATTRS_STRING, OptionSetKeeper.KEEPER.ent2str( aObject.attrs() ) );
    int i = query.executeUpdate();
    System.out.println( i );
  }
}
