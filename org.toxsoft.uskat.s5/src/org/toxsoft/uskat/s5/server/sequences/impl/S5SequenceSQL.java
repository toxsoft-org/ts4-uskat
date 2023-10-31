package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5DataID.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock.*;

import java.math.BigInteger;
import java.sql.*;
import java.util.List;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.S5Partition;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.utils.collections.S5FixedCapacityTimedList;

/**
 * Служебные методы для выполнения SQL-запросов к последовательностям значений данных.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
class S5SequenceSQL {

  /**
   * Журанл
   */
  private static ILogger logger = getLogger( S5SequenceSQL.class );

  /**
   * Оператор объединения SQL запросов
   */
  private static final String UNION_ALL = "union all";

  // ------------------------------------------------------------------------------------
  // Тесты SQL-запросов
  //
  /**
   * Формат запроса размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал закрытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   * <li>7. %d - время начала запроса (startTime).</li>
   * <li>8. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSCE_SIZES = //
      "select " + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" + //
          "(" + FIELD_START_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)" //
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал открытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   * <li>7. %d - время начала запроса (startTime).</li>
   * <li>8. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSOE_SIZES = //
      "select " + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<= %d)or" //
          + "(" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<= %d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">= %d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал закрытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSOE_SIZES = //
      "select " + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал открытй с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSCE_SIZES = //
      "select " + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME;

  /**
   * Загрузка размеров блоков попадающих в указанный интервал
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aFirstPosition int позиция с которой у dbms запрашивается результат
   * @param aMaxResultCount int максимальное количество блоков которое может быть обработано за один вызов. <= 0:
   *          неограничего
   * @return List&lt;Integer&gt последовательность размеров блоков
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> List<Integer> loadBlockSizes( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aInterval );
    // Время начала запроса
    long traceStartTime = System.currentTimeMillis();
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // WORKAROUND: нельзя давать в SQL запросы константы MIN_TIMESTAMP, MIN_TIMESTAMP ???
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    startTime = (startTime == MIN_TIMESTAMP ? startTime + 1 : startTime);
    endTime = (endTime == MAX_TIMESTAMP ? endTime - 1 : endTime);
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Текст SQL-запроса
    String sql = EMPTY_STRING;
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    sql = switch( aInterval.type() ) {
      case CSCE -> format( QFRMT_CSCE_SIZES, tableName, aGwid, st, et );
      case OSOE -> format( QFRMT_OSOE_SIZES, tableName, aGwid, st, et, st, et, st, et );
      case CSOE -> format( QFRMT_CSOE_SIZES, tableName, aGwid, st, et, st, et );
      case OSCE -> format( QFRMT_OSCE_SIZES, tableName, aGwid, st, et, st, et );
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( aFirstPosition );
      if( aMaxResultCount > 0 ) {
        query.setMaxResults( aMaxResultCount );
      }
      // Запрос данных
      List<Integer> entities = query.getResultList();
      if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
        Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        logger.debug( "loadBlockSizes(...): %d (msec)", time );
      }
      return entities;
    }
    catch( RuntimeException e ) {
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_BLOCK_SIZES_READ, aGwid, aInterval, cause( e ) );
    }

  }

  /**
   * Формат запроса времени и размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал закрытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   * <li>7. %d - время начала запроса (startTime).</li>
   * <li>8. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSCE_TIMES_SIZES =
      "select " + FIELD_START_TIME + ", " + FIELD_END_TIME + ", " + FIELD_SIZE + " from %s where"//
          + "(" + FIELD_GWID + "='%s')and"//
          + "(" + FIELD_START_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)" //
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса времени и размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал открытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   * <li>7. %d - время начала запроса (startTime).</li>
   * <li>8. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSOE_TIMES_SIZES =
      "select " + FIELD_START_TIME + "," + FIELD_END_TIME + "," + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "(" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<= %d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса времени и размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал закрытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSOE_TIMES_SIZES =
      "select " + FIELD_START_TIME + "," + FIELD_END_TIME + "," + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса времени и размеров блоков данного полностью находящихся в указанном интервале или на его границах
   * <p>
   * Интервал открытй с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSCE_TIMES_SIZES =
      "select " + FIELD_START_TIME + "," + FIELD_END_TIME + "," + FIELD_SIZE + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "  (" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME;

  /**
   * Загрузка меток времени и размеров блоков попадающих в указанный интервал
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aFirstPosition int позиция с которой у dbms запрашивается результат
   * @param aMaxResultCount int максимальное количество блоков которое может быть обработано за один вызов. <= 0:
   *          неограничего
   * @return List&lt;Object[]&gt последовательность меток времени начала блоков и размеров.<br>
   *         Первый элемент массива: метка времени начала блока. <br>
   *         Второй элемент массива: метка времени завершения блока.<br>
   *         Третий элемент массива: размер блока.
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> List<Object[]> loadBlockStartTimesSizes( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aInterval );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // WORKAROUND: нельзя давать в SQL запросы константы MIN_TIMESTAMP, MIN_TIMESTAMP ???
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    startTime = (startTime == MIN_TIMESTAMP ? startTime + 1 : startTime);
    endTime = (endTime == MAX_TIMESTAMP ? endTime - 1 : endTime);
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Текст SQL-запроса
    String sql = EMPTY_STRING;
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    sql = switch( aInterval.type() ) {
      case CSCE -> format( QFRMT_CSCE_TIMES_SIZES, tableName, aGwid, st, et );
      case OSOE -> format( QFRMT_OSOE_TIMES_SIZES, tableName, aGwid, st, et, st, et, st, et );
      case CSOE -> format( QFRMT_CSOE_TIMES_SIZES, tableName, aGwid, st, et, st, et );
      case OSCE -> format( QFRMT_OSCE_TIMES_SIZES, tableName, aGwid, st, et, st, et );
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( aFirstPosition );
      if( aMaxResultCount > 0 ) {
        query.setMaxResults( aMaxResultCount );
      }
      // Запрос данных
      List<Object[]> entities = query.getResultList();
      return entities;
    }
    catch( RuntimeException e ) {
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_BLOCK_SIZES_READ, aGwid, aInterval, cause( e ) );
    }
  }

  /**
   * Формат запроса блоков данного полностью находящихся в указанном интервале
   * <p>
   * Интервал закрытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSCE_BLOCKS = //
      "select b from %s b where"//
          + "(" + FIELD_GWID + "='%s')and"//
          + "(" + FIELD_START_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)" //
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса блоков данного полностью или частично попадающих в указанный интервал
   * <p>
   * Интервал открытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   * <li>7. %d - время начала запроса (startTime).</li>
   * <li>8. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSOE_BLOCKS = //
      "select b from %s b where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "(" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса блоков данного полностью или частично попадающих в указанный интервал
   * <p>
   * Интервал закрытый с начала, открытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSOE_BLOCKS = //
      "select b from %s b where" //
          + "( " + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Формат запроса блоков данного полностью или частично попадающих в указанный интервал
   * <p>
   * Интервал открытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   * <li>5. %d - время начала запроса (startTime).</li>
   * <li>6. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_OSCE_BLOCKS = //
      "select b from %s b where" //
          + "( " + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Загрузка блоков попадающих в указанный интервал
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aFirstPosition int позиция с которой у dbms запрашивается результат
   * @param aMaxResultCount int максимальное количество блоков которое может быть обработано за один вызов. <= 0:
   *          запрашиваются все данные
   * @return List&lt;{@link IS5SequenceBlock}&lt;V&gt;&gt; последовательность блоков
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> List<IS5SequenceBlock<V>> loadBlocks( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aInterval );
    // Время начала запроса
    long traceStartTime = System.currentTimeMillis();
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // WORKAROUND: нельзя давать в SQL запросы константы MIN_TIMESTAMP, MIN_TIMESTAMP ???
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    startTime = (startTime == MIN_TIMESTAMP ? startTime + 1 : startTime);
    endTime = (endTime == MAX_TIMESTAMP ? endTime - 1 : endTime);
    // Имя класса реализации блока
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
    // Имя таблицы
    String tableName = getLast( blockImplClass );
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    // Текст SQL-запроса
    String sql = EMPTY_STRING;
    sql = switch( aInterval.type() ) {
      case CSCE -> format( QFRMT_CSCE_BLOCKS, tableName, aGwid, st, et );
      case OSOE -> format( QFRMT_OSOE_BLOCKS, tableName, aGwid, st, et, st, et, st, et );
      case CSOE -> format( QFRMT_CSOE_BLOCKS, tableName, aGwid, st, et, st, et );
      case OSCE -> format( QFRMT_OSCE_BLOCKS, tableName, aGwid, st, et, st, et );
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    try {
      // Класс реализации блока
      Class<?> blockClass = Class.forName( blockImplClass );
      // Выполнение запроса
      TypedQuery<?> query = aEntityManager.createQuery( sql, blockClass );
      // Ограничение выборки
      query.setFirstResult( aFirstPosition );
      if( aMaxResultCount > 0 ) {
        query.setMaxResults( aMaxResultCount );
      }
      // Запрос данных
      List<?> entities = query.getResultList();
      if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
        Long time = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        logger.debug( "loadBlocks(...): %d (msec). sql =\n %s", time, sql );
      }
      return (List<IS5SequenceBlock<V>>)entities;
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации блоков
      throw new TsInternalErrorRtException( e, ERR_IMPL_BLOCK_NOT_FOUND, aGwid, tableName, cause( e ) );
    }
    catch( RuntimeException e ) {
      // Неожиданная ошибка чтения блоков
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_BLOCK_ENCLOSED_READ, aGwid, aInterval, cause( e ) );
    }
  }

  /**
   * Формат запроса удаления блоков данного по идентификатору данного полностью попадающий в указанном интервале
   * <p>
   * <li>1. %s - имя таблицы blob, например, S5HistDataAsyncBlob;</li>
   * <li>2. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>3. %s - имя таблицы blob, например, S5HistDataAsyncBlob;</li>
   * <li>4. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>5. %s - имя таблицы blob, например, S5HistDataAsyncBlob;</li>
   * <li>6. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>7. %s - имя таблицы blob, например, S5HistDataAsyncBlob;</li>
   * <li>8. %s - идентификатор данного (gwid);</li>
   * <li>9. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>10. %d - время начала запроса (startTime).</li>
   * <li>11. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>12. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_DELETE_BLOCKS = "delete %s,%s " //
      + "from %s "//
      + "INNER JOIN %s " //
      + "where(%s.gwid = %s.gwid)and(%s.gwid = '%s')and" //
      + "(%s." + FIELD_START_TIME + ">=%d)and(%s." + FIELD_END_TIME + "<=%d)";

  /**
   * Удаляет блоки данного в указанном интервале
   * <p>
   * Блоки удаляется полностью даже если частично попадают в указанный интервал
   * <p>
   * Если блоков нет, то ничего не делает.
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал удаляемых блоков
   * @param <V> тип значения последовательности
   * @return int количество удаленных блоков
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> int removeBlocks( EntityManager aEntityManager, IS5SequenceFactory<V> aFactory,
      Gwid aGwid, ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aInterval );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // WORKAROUND: нельзя давать в SQL запросы константы MIN_TIMESTAMP, MIN_TIMESTAMP ???
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    startTime = (startTime == MIN_TIMESTAMP ? startTime + 1 : startTime);
    endTime = (endTime == MAX_TIMESTAMP ? endTime - 1 : endTime);
    // Имя таблицы реализации блоков
    String blockImplTableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Имя таблицы реализации blob
    String blobImplTableName = getLast( OP_BLOB_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    // Текст SQL-запроса
    String sql = format( QFRMT_DELETE_BLOCKS, //
        blobImplTableName, blockImplTableName, //
        blobImplTableName, blockImplTableName, //
        blobImplTableName, blockImplTableName, //
        blobImplTableName, aGwid, //
        blockImplTableName, st, //
        blockImplTableName, et );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      int retCode = query.executeUpdate();
      return retCode;
    }
    catch( RuntimeException e ) {
      throw new TsIllegalArgumentRtException( e, ERR_REMOVE_BLOCK, aGwid, aInterval, cause( e ) );
    }
  }

  /**
   * Формат запроса последнего блока интервал времени которого находится перед(или в нем) указанным временем
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время запроса (time).</li>
   * <li>4. %d - время запроса (time).</li>
   * <li>5. %d - время запроса (time).</li>
   */
  private static final String QFRMT_BLOCK_BEFORE_TIME = //
      "select b from %s b where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + "<%d)or"//
          + " (" + FIELD_START_TIME + "<=%d)and( " + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME + " desc";

  /**
   * Проводится поиск ближайшего блока у которого есть значение на указанной метке времени или перед ней
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aTimestamp long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return {@link IS5SequenceBlock}&lt;V&gt; найденный блок. null: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> IS5SequenceBlock<V> findBlockBefore( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid, long aTimestamp ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Имя класса реализации блока
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
    // Имя таблицы
    String tableName = getLast( blockImplClass );
    try {
      List<IS5SequenceBlock<V>> blocks;
      Long time = Long.valueOf( aTimestamp );
      if( aTimestamp == MIN_TIMESTAMP ) {
        time = Long.valueOf( MIN_TIMESTAMP + 1 );
      }
      if( aTimestamp == MAX_TIMESTAMP ) {
        time = Long.valueOf( MAX_TIMESTAMP - 1 );
      }
      // Текст SQL-запроса
      String sql = format( QFRMT_BLOCK_BEFORE_TIME, tableName, aGwid, time, time, time );
      // Класс реализации блока
      Class<?> blockClass = Class.forName( blockImplClass );
      // Выполнение запроса
      TypedQuery<?> query = aEntityManager.createQuery( sql, blockClass );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      blocks = (List<IS5SequenceBlock<V>>)query.getResultList();
      if( blocks.size() == 0 ) {
        return null;
      }
      return blocks.get( 0 );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации блоков
      throw new TsInternalErrorRtException( e, ERR_IMPL_BLOCK_NOT_FOUND, aGwid, tableName, cause( e ) );
    }
    catch( OutOfMemoryError e ) {
      // Ошибка дефицита памяти для чтения блоков
      String ts = timestampToString( aTimestamp );
      throw new TsIllegalArgumentRtException( ERR_READ_OUT_OF_MEMORY, aGwid, ts, cause( e ) );
    }
  }

  /**
   * Читает блоки значений (с одинаковой реализацией хранения) указанных данных в указанном интервале времени
   *
   * @param aQuery {@link IS5SequenceReadQuery} запрос чтения хранимых данных
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @return {@link IMap}&lt;Long,{@link IList}&lt;{@link IS5SequenceBlock}&lt;V&gt;&gt; карта найденных блоков.<br>
   *         Ключ: идентификатор данного<br>
   *         Значение: список блоков.
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException блоки/blob значений реализуются разными классами
   */
  @SuppressWarnings( { "unchecked", "resource" } )
  static <V extends ITemporal<?>> IMap<Gwid, IList<IS5SequenceBlock<V>>> readBlocks( IS5SequenceReadQuery aQuery,
      IGwidList aGwids ) {
    TsNullArgumentRtException.checkNulls( aQuery, aGwids );
    // Количество запрашиваемых данных
    int gwidSize = aGwids.size();
    if( gwidSize == 0 ) {
      // Частный случай пустой запрос. Формирование пустого результата
      IMapEdit<Gwid, IListEdit<IS5SequenceBlock<V>>> retValue = new ElemMap<>();
      for( Gwid gwid : aGwids ) {
        retValue.put( gwid, IList.EMPTY );
      }
      return (IMap<Gwid, IList<IS5SequenceBlock<V>>>)(Object)retValue;
    }

    StringBuilder sb = new StringBuilder();
    // Формирование подзапросов для каждого параметра
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      Gwid gwid = aGwids.get( index );
      sb.append( '(' );
      sb.append( prepareReadSQLForOne( aQuery, gwid ) );
      sb.append( ')' );
      if( index + 1 < n ) {
        sb.append( UNION_ALL );
      }
    }
    // Текст запроса
    String sql = sb.toString();
    // Журнал
    String infoStr = format( MSG_INFO_COUNT, Integer.valueOf( gwidSize ) );
    // Интервал запроса
    IQueryInterval interval = aQuery.interval();
    // Вывод текста запроса в журнал
    logger.debug( MSG_READ_BLOCK_SQL, infoStr, interval, sql );
    logger.debug( MSG_READ_BLOCK_SQL_SIZE, Integer.valueOf( sql.length() ) );
    // Фактическое время начала данных
    long factStartTime = TimeUtils.MAX_TIMESTAMP;
    // Фактическое время завершения данных
    long factEndTime = TimeUtils.MIN_TIMESTAMP;
    // Запрос
    long traceQueryStartTime = System.currentTimeMillis();
    long traceStartTime = traceQueryStartTime;
    // Количество прочитанных блоков
    int readBlockCount = 0;
    // Формирование результата
    IMapEdit<Gwid, IListEdit<IS5SequenceBlock<V>>> retValue = new ElemMap<>();
    try {
      Connection connection = aQuery.connection();
      try( Statement statement = connection.createStatement(); ) {
        if( aQuery.isClosed() ) {
          // Выполнение запроса было отменено
          return IMap.EMPTY;
        }
        if( aQuery.maxExecutionTimeout() > 1000 ) {
          // statement.setQueryTimeout( (int)(aQuery.maxExecutionTimeout() / 1000) );
        }
        aQuery.addStatement( statement );
        try {
          try( ResultSet rs = statement.executeQuery( sql ); ) {
            traceStartTime = System.currentTimeMillis();
            // Вывод текста запроса в журнал
            logger.debug( MSG_READ_BLOCK_START, infoStr, interval );
            // statement.setPoolable( false );
            // statement.setMaxRows( infoCount );
            // statement.setFetchSize( infoCount );
            IS5SequenceFactory<V> factory = (IS5SequenceFactory<V>)aQuery.factory();
            while( rs.next() ) {
              // Идентификатор данного значения которого находятся в блоке
              Gwid gwid = Gwid.KEEPER.str2ent( rs.getString( S5DataID.FIELD_GWID ) );
              // Описание типа данного
              IParameterized typeInfo = factory.typeInfo( gwid );
              // Имя класса реализации блока
              String blockImplClass = blockImplClassFromInfo( typeInfo );
              // Формирование блока
              IS5SequenceBlock<V> block = factory.createBlock( blockImplClass, rs );
              // Обработка блока согласно интервалу (чтобы в блоке не было лишних данных)
              block = trimOrNull( factory, interval, block );
              if( block == null ) {
                // Все значения блока вне интервала
                continue;
              }
              IListEdit<IS5SequenceBlock<V>> readBlocks = retValue.findByKey( gwid );
              if( readBlocks == null ) {
                readBlocks = new ElemLinkedList<>();
                retValue.put( gwid, readBlocks );
              }
              if( readBlocks.size() > 0 && readBlocks.get( readBlocks.size() - 1 ).equals( block ) ) {
                // Допустимая ситуация так как мы используем "union all" чтобы ускорить выполнение запроса
                logger.debug( "Повтор чтения блока %s. Игнорирование. sql(%d) = \n%s", block,
                    Integer.valueOf( sql.length() ), sql );
                continue;
              }
              readBlockCount++;
              // Формирование списка считанных блоков
              readBlocks.add( block );
              // Формирование меток времени фактического интервала данных
              if( block.startTime() < factStartTime ) {
                factStartTime = block.startTime();
              }
              if( factEndTime < block.endTime() ) {
                factEndTime = block.endTime();
              }
            }
          }
        }
        finally {
          aQuery.removeStatement( statement );
        }

      }
      long currTime = System.currentTimeMillis();
      ITimeInterval factInterval = ITimeInterval.NULL;
      if( factStartTime != TimeUtils.MAX_TIMESTAMP && factEndTime != TimeUtils.MIN_TIMESTAMP ) {
        factInterval = new TimeInterval( factStartTime, factEndTime );
      }
      Integer c = Integer.valueOf( readBlockCount );
      Long ta = Long.valueOf( currTime - traceQueryStartTime );
      Long tq = Long.valueOf( traceStartTime - traceQueryStartTime );
      Long th = Long.valueOf( currTime - traceStartTime );
      logger.info( MSG_READ_BLOCK_END, infoStr, interval, factInterval, c, ta, tq, th );
      // Результат
      return (IMap<Gwid, IList<IS5SequenceBlock<V>>>)(Object)retValue;
    }
    catch( OutOfMemoryError e ) {
      // Ошибка дефицита памяти для чтения блоков
      logger.error( e, ERR_READ_OUT_OF_MEMORY, infoStr, interval, cause( e ) );
      throw new TsIllegalArgumentRtException( ERR_READ_OUT_OF_MEMORY, infoStr, interval, cause( e ) );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка чтения блоков
      logger.error( e, ERR_READ_UNEXPECTED, infoStr, cause( e ) );
      throw new TsInternalErrorRtException( e, ERR_READ_UNEXPECTED, infoStr, cause( e ) );
    }
  }

  /**
   * Формат запроса времени завершения последнего блока значений данного перед указанным временем (включительно)
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (aFromTime).</li>
   * <li>4. %d - время начала запроса (aFromTime).</li>
   * <li>5. %d - время начала запроса (aFromTime).</li>
   */
  private static final String QFRMT_TIME_BEFORE_TIME = //
      "select " + FIELD_END_TIME + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + "<%d)or"//
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME + " desc";

  /**
   * Проводится поиск ближайшего блока у которого есть значение на указанной метке времени или перед ней и возвращает
   * время завершения этого блока если оно ДО указанной метки-аргумент или саму метку-аргумент если она попадает в
   * найденный блок
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aBeforeTime long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return long время(мсек с начала эпохи) метка времени. Если блок не найден, то {@link TimeUtils#MIN_TIMESTAMP}
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findTimeBefore( EntityManager aEntityManager, IS5SequenceFactory<V> aFactory,
      Gwid aGwid, Long aBeforeTime ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aBeforeTime );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Метка времени
    Long beforeTime = aBeforeTime;
    if( beforeTime.longValue() == MIN_TIMESTAMP ) {
      beforeTime = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( beforeTime.longValue() == MAX_TIMESTAMP ) {
      beforeTime = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Текст SQL-запроса
    String sql = format( QFRMT_TIME_BEFORE_TIME, tableName, aGwid, beforeTime, beforeTime, beforeTime );
    // Вывод текста запроса в журнал
    // logger.debug( MSG_FIND_TIME_BEFORE_SQL, aInfo, timestampToString( beforeTime.longValue() ), sql );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      List<BigInteger> entities = query.getResultList();
      if( entities.size() == 0 ) {
        // Нет данных
        return MIN_TIMESTAMP;
      }
      BigInteger endTime = entities.get( 0 );
      return Math.min( aBeforeTime.longValue(), endTime.longValue() );
    }
    catch( RuntimeException e ) {
      String fromtime = timestampToString( aBeforeTime.longValue() );
      throw new TsInternalErrorRtException( e, ERR_READ_OUT_OF_MEMORY, aGwid, fromtime, cause( e ) );
    }
  }

  /**
   * Формат запроса времени начала первого блока значений данного с указанного времени (включительно)
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (aFromTime).</li>
   * <li>4. %d - время начала запроса (aFromTime).</li>
   * <li>5. %d - время начала запроса (aFromTime).</li>
   */
  private static final String QFRMT_TIME_AFTER_TIME = //
      "select " + FIELD_START_TIME + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">%d)or"//
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME;

  /**
   * Проводится поиск ближайшего блока у которого есть значение на указанной метке времени или после нее и возвращает
   * время начала этого блока если оно ПОСЛЕ указанной метки-аргумент или саму метку-аргумент если она попадает в
   * найденный блок
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aAfterTime long метка времени (мсек с начала эпохи) после которой или на которой проводится поиск блока со
   *          значением
   * @return long время(мсек с начала эпохи) Время НАЧАЛА блока. Если блок не найден, то {@link TimeUtils#MAX_TIMESTAMP}
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findTimeAfter( EntityManager aEntityManager, IS5SequenceFactory<V> aFactory,
      Gwid aGwid, Long aAfterTime ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid, aAfterTime );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Метка времени
    Long afterTime = aAfterTime;
    if( afterTime.longValue() == MIN_TIMESTAMP ) {
      afterTime = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( afterTime.longValue() == MAX_TIMESTAMP ) {
      afterTime = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Текст SQL-запроса
    String sql = format( QFRMT_TIME_AFTER_TIME, tableName, aGwid, afterTime, afterTime, afterTime );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      List<BigInteger> entities = query.getResultList();
      if( entities.size() == 0 ) {
        // Нет данных
        return MAX_TIMESTAMP;
      }
      BigInteger startTime = entities.get( 0 );
      return Math.max( aAfterTime.longValue(), startTime.longValue() );
    }
    catch( RuntimeException e ) {
      String fromtime = timestampToString( aAfterTime.longValue() );
      throw new TsInternalErrorRtException( e, ERR_READ_OUT_OF_MEMORY, aGwid, fromtime, cause( e ) );
    }
  }

  /**
   * Формат запроса интервала первого блока значений хранимого в БД
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   */
  private static final String QFRMT_FIRST_BLOCK_TIME_INTERVAL = //
      "select " + FIELD_START_TIME + ',' + FIELD_END_TIME + " from %s where" //
          + "(" + FIELD_GWID + "='%s')"//
          + "order by " + FIELD_START_TIME;

  /**
   * Проводится первого блока значений и возвращает его интервал времени
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @return {@link ITimeInterval} интервал первого блока значений. {@link ITimeInterval#NULL}: блок не найден (нет
   *         значений)
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> ITimeInterval findFirstBlockTimeInterval( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Текст SQL-запроса
    String sql = format( QFRMT_FIRST_BLOCK_TIME_INTERVAL, tableName, aGwid );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      List<Object> entities = query.getResultList();
      if( entities.size() == 0 ) {
        // Нет данных
        return ITimeInterval.NULL;
      }
      Object[] obj = (Object[])entities.get( 0 );
      BigInteger startTime = (BigInteger)obj[0];
      BigInteger endTime = (BigInteger)obj[1];
      long stl = startTime.longValue();
      long etl = endTime.longValue();
      ITimeInterval retValue = new TimeInterval( stl, etl );
      return retValue;
    }
    catch( RuntimeException e ) {
      throw new TsInternalErrorRtException( e, ERR_READ_OUT_OF_MEMORY2, aGwid, cause( e ) );
    }
  }

  /**
   * Формат запроса времени последнего блока более чем указанный размер
   * <p>
   * Интервал открытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - размер блока.</li>
   */
  private static final String QFRMT_FIRST_FRAGMENTED_TIME = //
      "select " + FIELD_END_TIME + " from %s where" //
          + "(" + FIELD_GWID + "='%s')and"//
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_SIZE + ">=%d)" //
          + " order by " + FIELD_START_TIME + " desc";

  /**
   * Проводит поиск времени начала фрагментации блоков, чтобы можно было бы выполнить процесс их объединения и получения
   * полного блока
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aToTime long (время мсек с начала эпохи) до которого проводить поиск фрагментированных блоков (включительно)
   * @param aMaxSize int размер блока меньше которого блок считается фрагментированным
   * @param aFragmentCountMin int минимальное количество блоков которые требуется для дефрагментации. <= 0: отключено
   * @param aFragmentCountMax int максимальное количество блоков которые требуетя для дефрагментации. <= 0: отключено
   * @param aFragmentTimeout long время (мсек) между блоками больше которого проводится принудительная дефрагментация.
   *          <= 0: отключено.
   * @return {@link IS5SequenceFragmentInfo} информация о фрагментации. {@link IS5SequenceFragmentInfo#NULL} нет
   *         фрагментации
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aFragmentCountMax = 1
   */
  static <V extends ITemporal<?>> IS5SequenceFragmentInfo findFragmentationTime( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid, long aToTime, int aMaxSize, int aFragmentCountMin,
      int aFragmentCountMax, long aFragmentTimeout ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    TsInternalErrorRtException.checkTrue( aFragmentCountMax == 1 );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // WORKAROUND: нельзя давать в SQL запросы константы MIN_TIMESTAMP, MIN_TIMESTAMP ???
    Long t = Long.valueOf( aToTime );
    Long s = Long.valueOf( aMaxSize );
    // Имя таблицы
    String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Выполнение запроса поиск времени первого фрагментированного блока
    String sql = format( QFRMT_FIRST_FRAGMENTED_TIME, tableName, aGwid, t, s );
    try {
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      List<BigInteger> entities = query.getResultList();
      // Время первого блока
      long fragmentStartTime = MAX_TIMESTAMP;
      // Время последнего блока
      long fragmentEndTime = MAX_TIMESTAMP;
      if( entities.size() > 0 ) {
        // Найден НЕфрагментированный блок, поиск от него
        fragmentStartTime = entities.get( 0 ).longValue() + 1;
        fragmentEndTime = fragmentStartTime;
      }
      if( fragmentStartTime == MAX_TIMESTAMP ) {
        // Не найден НЕфрагментированный блок, поиск от начала
        fragmentStartTime = findTimeAfter( aEntityManager, aFactory, aGwid, Long.valueOf( MIN_TIMESTAMP ) );
        fragmentEndTime = fragmentStartTime;
      }
      if( fragmentStartTime == MAX_TIMESTAMP ) {
        // Не найдено время фрагментации
        return IS5SequenceFragmentInfo.NULL;
      }
      if( fragmentStartTime >= aToTime ) {
        // До указанного времени нет фрагментации
        return IS5SequenceFragmentInfo.NULL;
      }
      // Интервал запроса размеров фрагментированных блоков
      IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, fragmentStartTime, aToTime );
      // Запрос меток времени и размеров (метка времени начала блока и его размер)
      List<Object[]> startsTimesSizesList = loadBlockStartTimesSizes( aEntityManager, aFactory, aGwid, interval, 0, 0 );
      // Общее количество найденных фрагментов
      int startsTimesSizesListSize = startsTimesSizesList.size();
      if( startsTimesSizesListSize == 0 ) {
        // Нет блоков для дефрагментации
        return IS5SequenceFragmentInfo.NULL;
      }
      // Общее количество значений фрагментированных блоков
      int allSize = 0;
      // Общее количество блоков годных для объединения
      int allUnionableCount = 0;
      // Общее количество фрагментов (непроверенных) найденных после завершения поиска
      int allUnionableAfterCount = startsTimesSizesListSize;
      // Метка времени завершения предыдущего блока. MIN_TIMESTAMP: неопределено
      long allFirstEndTime = MIN_TIMESTAMP;
      // Признак необходимости выполнить дефрагментацию так как есть "старые" блоки
      boolean tooLateBlocks = false;
      // Двойной размер блоков
      int doubleMaxSize = 2 * aMaxSize;
      for( int index = 0; index < startsTimesSizesListSize; index++ ) {
        Object[] bts = startsTimesSizesList.get( index );
        allUnionableAfterCount = startsTimesSizesListSize - index - 1;
        long startTime = ((BigInteger)bts[0]).longValue();
        long endTime = ((BigInteger)bts[1]).longValue();
        int size = ((Integer)bts[2]).intValue();
        if( size >= aMaxSize ) {
          // Найден полный блок с которым невозможно объединение
          allSize = 0;
          allUnionableCount = 0;
          allFirstEndTime = MIN_TIMESTAMP;
          tooLateBlocks = false;
          continue;
        }
        if( allSize + size >= doubleMaxSize && allUnionableCount < 2 ) {
          // Невозможно объединить два соседних блока
          fragmentStartTime = startTime;
          fragmentEndTime = endTime;
          allSize = size;
          allUnionableCount = 0;
          allFirstEndTime = endTime;
          tooLateBlocks = false;
          continue;
        }
        fragmentEndTime = endTime;
        allSize += size;
        allUnionableCount++;
        if( aFragmentCountMax > 0 && allUnionableCount >= aFragmentCountMax ) {
          // Найдено необходимое количество блоков
          break;
        }
        if( allFirstEndTime == MIN_TIMESTAMP ) {
          allFirstEndTime = endTime;
          continue;
        }
        // Проверка признака: слишком большой интервал между завершением предыдущего блока и началом следующего
        tooLateBlocks = tooLateBlocks || (aFragmentTimeout > 0 && (startTime - allFirstEndTime) > aFragmentTimeout);
      }
      // Признак того, что количество блоков годных для объединения слишком большое
      boolean tooManyBlocks = (aFragmentCountMin > 0 && allUnionableCount > aFragmentCountMin);
      if( !tooLateBlocks && !tooManyBlocks && allSize < aMaxSize ) {
        // Недостаточно значений для объединения
        return new S5SequenceFragmentInfo( tableName, aGwid, fragmentStartTime, fragmentStartTime, -1,
            allUnionableCount + allUnionableAfterCount );
      }
      if( fragmentStartTime >= aToTime ) {
        // Нет интервала для дефрагментации
        return new S5SequenceFragmentInfo( tableName, aGwid, fragmentStartTime, fragmentStartTime, -1,
            allUnionableCount + allUnionableAfterCount );
      }
      // Формирование результата
      return new S5SequenceFragmentInfo( tableName, aGwid, fragmentStartTime, fragmentEndTime, allUnionableCount,
          allUnionableAfterCount );
    }
    catch( RuntimeException e ) {
      String time = timestampToString( aToTime );
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_FIND_FRAGMENTATION, aGwid, time, cause( e ) );
    }
  }

  /**
   * Формат запроса времени завершения последнего блока
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   */
  private static final String QFRMT_LAST_BLOCK_END_TIME = //
      "select " + FIELD_END_TIME + " from %s where"//
          + "(" + FIELD_GWID + "='%s')" //
          + "order by " + FIELD_START_TIME + " desc";

  /**
   * Проводится поиск ближайшего блока у которого есть значение на указанной метке времени или перед ней
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @return long метка времени завершения последнего блока. MIN_TIMESTAMP: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findLastBlockEndTime( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    try {
      // Имя таблицы
      String tableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
      // Текст SQL-запроса
      String sql = format( QFRMT_LAST_BLOCK_END_TIME, tableName, aGwid );
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      List<BigInteger> entities = query.getResultList();
      if( entities.size() == 0 ) {
        // Нет данных
        return MIN_TIMESTAMP;
      }
      BigInteger startTime = entities.get( 0 );
      return startTime.longValue();

    }
    catch( OutOfMemoryError e ) {
      throw new TsIllegalArgumentRtException( ERR_READ_UNEXPECTED, aGwid, cause( e ) );
    }
  }

  /**
   * Формат запроса последнего блока
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   */
  private static final String QFRMT_LAST_BLOCK = //
      "select b from %s b where" //
          + "(" + FIELD_GWID + "='%s')"//
          + "order by " + FIELD_START_TIME + " desc";

  /**
   * Проводится поиск последнего блока
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @return {@link IS5SequenceBlock}&lt;V&gt; найденный блок. null: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> IS5SequenceBlock<V> findLastBlock( EntityManager aEntityManager,
      IS5SequenceFactory<V> aFactory, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Имя класса реализации блока
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
    // Имя таблицы
    String tableName = getLast( blockImplClass );
    try {
      List<IS5SequenceBlock<V>> blocks;
      // Текст SQL-запроса
      String sql = format( QFRMT_LAST_BLOCK, tableName, aGwid );
      // Класс реализации блока
      Class<?> blockClass = Class.forName( blockImplClass );
      // Выполнение запроса
      TypedQuery<?> query = aEntityManager.createQuery( sql, blockClass );
      // Ограничение выборки
      query.setFirstResult( 0 );
      query.setMaxResults( 1 );
      // Запрос данных
      blocks = (List<IS5SequenceBlock<V>>)query.getResultList();
      if( blocks.size() == 0 ) {
        return null;
      }
      return blocks.get( 0 );
    }
    catch( ClassNotFoundException e ) {
      // Не найден класс реализации блоков
      throw new TsInternalErrorRtException( e, ERR_IMPL_BLOCK_NOT_FOUND, aGwid, tableName, cause( e ) );
    }
    catch( OutOfMemoryError e ) {
      throw new TsIllegalArgumentRtException( ERR_READ_UNEXPECTED, aGwid, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Формат NATIVE(!)-запроса объединения результатов поиска последних {@link #QFRMT_NATIVE_BLOCKS_BEFORE_TIME_SUB} или
   * первых {@link #QFRMT_NATIVE_BLOCKS_AFTER_TIME_SUB} блоков
   * <p>
   * <li>1. %s - имя таблицы реализации blob, например, S5HistDataAsyncBooleanBlobEntity;</li>
   * <li>2. %s - sql-подзапроса в формате {@link #QFRMT_NATIVE_BLOCKS_BEFORE_TIME_SUB} или
   * {@link #QFRMT_NATIVE_BLOCKS_AFTER_TIME_SUB}.</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_NATIVE_JOIN_ASYNC_BLOCKS =                                                          //
      "select d.*, b." + FIELD_END_TIME + ", b." + FIELD_DEBUG_START_TIME + ", b." + FIELD_DEBUG_END_TIME + ", b."
          + FIELD_SIZE + " " +                                                                                          //
          "from %s as d inner join (%s) as b on("                                                                       //
          + "(d." + FIELD_GWID + "=b." + FIELD_GWID + ")AND(d." + FIELD_START_TIME + "=b." + FIELD_START_TIME + ")" +   //
          ") order by " + "d." + FIELD_START_TIME;
  private static final String QFRMT_NATIVE_JOIN_SYNC_BLOCKS  =                                                          //
      "select d.*, b." + FIELD_END_TIME + ", b." + FIELD_DEBUG_START_TIME + ", b." + FIELD_DEBUG_END_TIME + ", b."
          + FIELD_SIZE + ", b." + S5SequenceSyncBlock.FIELD_SYNC_DATA_DELTA + " " +                                     //
          "from %s as d inner join (%s) as b on("                                                                       //
          + "(d." + FIELD_GWID + "=b." + FIELD_GWID + ")AND(d." + FIELD_START_TIME + "=b." + FIELD_START_TIME + ")" +   //
          ") order by " + "d." + FIELD_START_TIME;

  /**
   * Формат NATIVE(!)-запроса последнего блока интервал времени которого находится перед(или в нем) указанным временем
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время запроса (time).</li>
   * <li>4. %d - время запроса (time).</li>
   * <li>5. %d - время запроса (time).</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_NATIVE_BLOCKS_BEFORE_TIME_SUB = //
      "select * from %s where" + //
          "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_END_TIME + "<%d)or"//
          + "(" + FIELD_START_TIME + "<= %d)and(" + FIELD_END_TIME + ">=%d))" //
          + "order by " + FIELD_START_TIME + " desc limit 1";

  /**
   * Формат NATIVE(!)-запроса последнего блока интервал времени которого находится перед(или в нем) указанным временем
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время запроса (time).</li>
   * <li>4. %d - время запроса (time).</li>
   * <li>5. %d - время запроса (time).</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_NATIVE_BLOCKS_AFTER_TIME_SUB = //
      "select * from %s where" //
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">%d)or"//
          + "(" + FIELD_START_TIME + "<= %d)and(" + FIELD_END_TIME + ">=%d))"//
          + "order by " + FIELD_START_TIME + " limit 1";

  /**
   * Формат NATIVE(!)-запроса блоков в указанном интервале или на его границах
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время запроса (time).</li>
   * <li>4. %d - время запроса (time).</li>
   * <li>5. %d - время запроса (time).</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_NATIVE_BLOCKS_SUB = //
      "select * from %s where"//
          + "(" + FIELD_GWID + "='%s')and" //
          + "((" + FIELD_START_TIME + ">=%d)and(" + FIELD_START_TIME + "<=%d)or" //
          + "(" + FIELD_END_TIME + ">=%d)and(" + FIELD_END_TIME + "<=%d)or" //
          + "(" + FIELD_START_TIME + "<=%d)and(" + FIELD_END_TIME + ">=%d))order by " + FIELD_START_TIME;

  /**
   * Подготовливает SQL запрос чтения блоков для указанного данного
   *
   * @param aQuery {@link IS5SequenceReadQuery} запрос чтения хранимых данных.
   * @param aGwid {@link Gwid} идентификатор данного.
   * @return String текст SQL запроса.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String prepareReadSQLForOne( IS5SequenceReadQuery aQuery, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aQuery, aGwid );
    // Фабрика формирования значений
    IS5SequenceFactory<?> factory = aQuery.factory();
    // Описание типа данного
    IParameterized typeInfo = factory.typeInfo( aGwid );
    // Имя класса реализации блока
    String blockImplClass = blockImplClassFromInfo( typeInfo );
    // Имя класса реализации blob
    String blobImplClass = blobImplClassFromInfo( typeInfo );
    // Имя таблицы блоков
    String blockTableName = getLast( blockImplClass );
    // Имя таблицы blob
    String blobTableName = getLast( blobImplClass );
    // Интервал запроса
    IQueryInterval interval = aQuery.interval();
    // Тип интервала запроса
    EQueryIntervalType intervalType = interval.type();
    // Время начала запроса
    Long startTime = Long.valueOf( interval.startTime() );
    if( startTime.longValue() == MIN_TIMESTAMP ) {
      startTime = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( startTime.longValue() == MAX_TIMESTAMP ) {
      startTime = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    // Время завершения запроса
    Long endTime = Long.valueOf( interval.endTime() );
    if( endTime.longValue() == MIN_TIMESTAMP ) {
      endTime = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( endTime.longValue() == MAX_TIMESTAMP ) {
      endTime = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    // Загрузка блоков последовательности
    StringBuilder sb = new StringBuilder();
    // Запрос значений до интервала
    if( intervalType == OSCE || intervalType == OSOE ) {
      sb.append( "(" );
      sb.append(
          format( QFRMT_NATIVE_BLOCKS_BEFORE_TIME_SUB, blockTableName, aGwid, startTime, startTime, startTime ) );
      sb.append( ")" + UNION_ALL );
    }
    // Запрос значений внутри интервала включительно
    sb.append( "(" );
    sb.append( format( QFRMT_NATIVE_BLOCKS_SUB, blockTableName, aGwid, startTime, endTime, startTime, endTime,
        startTime, endTime ) );
    sb.append( ")" );
    // Запрос значений после интервала
    if( intervalType == CSOE || intervalType == OSOE ) {
      sb.append( UNION_ALL + "(" );
      sb.append( format( QFRMT_NATIVE_BLOCKS_AFTER_TIME_SUB, blockTableName, aGwid, endTime, endTime, endTime ) );
      sb.append( ")" );
    }
    // Добавление в запрос оператора объединения подзапросов
    return addJoinOpertatorToSQL( blockImplClass, blobTableName, sb.toString() );
  }

  /**
   * Добавляет в текст SQL-запроса оператор объединения подзапросов
   *
   * @param aBlockImplClass String полное имя класса реализации блока значений
   * @param aBlobTable String имя таблицы хранения blob
   * @param aSQL String текст SQL-запроса
   * @return String текст SQL-запроса с добавленным оператором объединения подзапросов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String addJoinOpertatorToSQL( String aBlockImplClass, String aBlobTable, String aSQL ) {
    TsNullArgumentRtException.checkNull( aBlockImplClass, aBlobTable, aSQL );
    // Класс реализации блока
    try {
      Class<?> blockClass = Class.forName( aBlockImplClass );
      // оператор SQL-запроса зависит от реализации хранения блока
      String queryType = (S5SequenceSyncBlock.class.isAssignableFrom( blockClass ) ? //
          QFRMT_NATIVE_JOIN_SYNC_BLOCKS : QFRMT_NATIVE_JOIN_ASYNC_BLOCKS);
      // Текст SQL-запроса
      String sql = format( queryType, aBlobTable, aSQL );
      return sql;
    }
    catch( ClassNotFoundException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  /**
   * Возвращает полное имя класса реализации блока
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание данного.
   * @return String полное имя класса реализации
   * @throws TsNullArgumentRtException аргумент = null
   */
  static String blockImplClassFromInfo( IParameterized aTypeInfo ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( aTypeInfo.params() ).asString();
    return blockImplClass;
  }

  /**
   * Возвращает полное имя класса реализации blob
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание данного.
   * @return String полное имя класса реализации
   * @throws TsNullArgumentRtException аргумент = null
   */
  static String blobImplClassFromInfo( IParameterized aTypeInfo ) {
    String blockImplClass = OP_BLOB_IMPL_CLASS.getValue( aTypeInfo.params() ).asString();
    return blockImplClass;
  }

  /**
   * Запрос создания записи(блока, blob)
   * <p>
   * <li>1. %s - Имя таблицы хранения объекта, например, {@link S5ObjectEntity} ;</li>
   * <li>2. %s - Список формальных параметров через запятую. Например: a,b,c ;</li>
   * <li>3. %s - Список фактических параметров через запятую. Например: :a,:b,:c} ;</li>
   */
  static final String QFRMT_INSERT_ROW = //
      "insert into %s" //
          + "(%s)values" //
          + "(%s)";

  /**
   * Формирует запрос создания записи в таблице базы данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aTable String имя таблицы
   * @param aParams {@link IStringMap}&lt;Object&gt; карта параметров запроса.<br>
   *          Ключ: имя поля таблицы;Значение: значение поля.
   * @return {@link Query} созданный запрос
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Query createInsertQuery( EntityManager aEntityManager, String aTable, IStringMap<Object> aParams ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTable, aParams );
    // Построители формальных и фактических параметров
    StringBuilder formalParams = new StringBuilder();
    StringBuilder factParams = new StringBuilder();
    for( int index = 0, n = aParams.size(); index < n; index++ ) {
      String fieldName = aParams.keys().get( index );
      formalParams.append( fieldName );
      factParams.append( ':' + fieldName );
      if( index + 1 < n ) {
        formalParams.append( ',' );
        factParams.append( ',' );
      }
    }
    // Текст SQL-запроса
    String sql = format( QFRMT_INSERT_ROW, aTable, formalParams.toString(), factParams.toString() );
    Query query = aEntityManager.createNativeQuery( sql );
    for( String fieldName : aParams.keys() ) {
      query.setParameter( fieldName, aParams.getByKey( fieldName ) );
    }
    return query;
  }

  /**
   * Запрос обновления записи (блока, blob)
   * <p>
   * <li>1. %s - Имя таблицы хранения объекта, например, {@link S5ObjectEntity} ;</li>
   * <li>2. %s - Список параметров через запятую. Например: a=:a,b=:b,c=:c, где a,b и c имена полей таблицы;</li>
   */
  static final String QFRMT_UPDATE_ROW = //
      "update %s " //
          + "set %s " //
          + "where " + FIELD_GWID + "=:" + FIELD_GWID + " and " + FIELD_START_TIME + "=:" + FIELD_START_TIME;

  /**
   * Формирует запрос создания записи в таблице базы данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aTable String имя таблицы
   * @param aParams {@link IStringMap}&lt;Object&gt; карта параметров запроса.<br>
   *          Ключ: имя поля таблицы;Значение: значение поля.
   * @param aId {@link S5DataID} первичный ключ редактируемой записи
   * @return {@link Query} созданный запрос
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Query createUpdateQuery( EntityManager aEntityManager, String aTable, IStringMap<Object> aParams,
      S5DataID aId ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTable, aParams, aId );
    // Построители формальных и фактических параметров
    StringBuilder factParams = new StringBuilder();
    for( int index = 0, n = aParams.size(); index < n; index++ ) {
      String fieldName = aParams.keys().get( index );
      factParams.append( fieldName );
      factParams.append( "=:" );
      factParams.append( fieldName );
      if( index + 1 < n ) {
        factParams.append( ',' );
      }
    }
    // Текст SQL-запроса
    String sql = format( QFRMT_UPDATE_ROW, aTable, factParams.toString() );
    Query query = aEntityManager.createNativeQuery( sql );
    for( String fieldName : aParams.keys() ) {
      query.setParameter( fieldName, aParams.getByKey( fieldName ) );
    }
    query.setParameter( FIELD_GWID, aId.gwid() );
    query.setParameter( FIELD_START_TIME, aId.startTime() );

    return query;
  }

  /**
   * Формат NATIVE(!)-запроса всех gwid в указанной таблице
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_GET_GWIDS = //
      "select " + FIELD_GWID + " from %s group by " + FIELD_GWID;

  /**
   * Возвращает список идентификаторов всех данных которые хранятся в базе данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aTables {@link IList}&lt;{@link Pair}&lt;String,String&gt;&gt; список пар таблиц реализации блоков
   *          ({@link Pair#left()}) и их blob {@link Pair#right()}
   * @return {@link IGwidList} список идентификаторов
   * @throws TsNullArgumentRtException аргумент = null
   */
  static IGwidList getAllGwids( EntityManager aEntityManager, IList<IS5SequenceTableNames> aTables ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTables );
    GwidList retValue = new GwidList();
    for( IS5SequenceTableNames tableNames : aTables ) {
      // Имя таблицы
      String tableName = tableNames.blockTableName();
      // Текст SQL-запроса
      String sql = format( QFRMT_GET_GWIDS, tableName );
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      // Запрос данных
      List<Object> entities = query.getResultList();
      for( Object entity : entities ) {
        Gwid gwid = Gwid.KEEPER.str2ent( (String)entity );
        retValue.add( gwid );
      }
    }
    return retValue;
  }

  /**
   * Формат NATIVE(!)-запроса всех gwid в указанной таблице в указанных разделах
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %s - список идентификаторов разделов через ','.</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_GET_GWIDS_BY_PARTITIONS = //
      "select " + FIELD_GWID + " from %s.%s partition(%s)group by " + FIELD_GWID;

  /**
   * Возвращает список идентификаторов всех данных которые хранятся в таблице базы данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String имя схемы базы данных сервера
   * @param aTable String имя таблицы в которой находятся разделы
   * @param aPartitionInfos {@link IList}&lt;{@link S5Partition}&gt; список описаний разделов
   * @return {@link IGwidList} список идентификаторов
   * @throws TsNullArgumentRtException аргумент = null
   */
  static IGwidList getAllPartitionGwids( EntityManager aEntityManager, String aScheme, String aTable,
      IList<S5Partition> aPartitionInfos ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable, aPartitionInfos );
    StringBuilder partitionIds = new StringBuilder();
    for( int index = 0, n = aPartitionInfos.size(); index < n; index++ ) {
      partitionIds.append( aPartitionInfos.get( index ).name() );
      if( index + 1 < n ) {
        partitionIds.append( ',' );
      }
    }
    GwidList retValue = new GwidList();
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_GWIDS_BY_PARTITIONS, aScheme, aTable, partitionIds.toString() );
    // Выполнение запроса
    Query query = aEntityManager.createNativeQuery( sql );
    // Запрос данных
    List<Object> entities = query.getResultList();
    for( Object entity : entities ) {
      Gwid gwid = Gwid.KEEPER.str2ent( (String)entity );
      retValue.add( gwid );
    }
    return retValue;
  }

  /**
   * Запрос получения разделов (партиций) указанной таблицы
   * <p>
   * <li>1. %s - Имя схемы базы данных ;</li>
   * <li>2. %s - Имя таблицы;</li>
   */
  static final String QFRMT_GET_PARTIONS = //
      "select distinct partition_name,partition_description from information_schema.partitions " //
          + "where table_schema='%s' and table_name='%s';";

  /**
   * Возвращает список идентификаторов всех данных которые хранятся в базе данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String имя схемы базы данных сервера
   * @param aTable String имя таблицы в которой находятся разделы
   * @return {@link IList}&lt;{@link S5Partition}&gt; список описаний разделов
   * @throws TsNullArgumentRtException аргумент = null
   */
  static IList<S5Partition> readPartitions( EntityManager aEntityManager, String aScheme, String aTable ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable );
    IListEdit<S5Partition> retValue = new ElemLinkedList<>();
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_PARTIONS, aScheme, aTable );
    // Выполнение запроса
    Query query = aEntityManager.createNativeQuery( sql );
    // Запрос данных
    List<Object> entities = query.getResultList();
    if( entities.size() == 0 ) {
      // Нет данных
      return IList.EMPTY;
    }
    for( Object entity : entities ) {
      Object[] partitionRow = (Object[])entity;
      String partitionName = (String)partitionRow[0];
      String endTimeStr = (String)partitionRow[1];
      if( partitionName == null ) {
        logger.debug( "schemeName = %s, aTableName = %s. entity = %s. partitionName = null", aScheme, aTable, entity );
        continue;
      }
      if( endTimeStr == null ) {
        logger.debug( "schemeName = %s, aTableName = %s. partitionName = %s. entity = %s. endTimeStr = null", aScheme,
            aTable, partitionName, entity );
        continue;
      }
      long endTime = ("MAXVALUE".equals( endTimeStr ) ? TimeUtils.MAX_TIMESTAMP : Long.parseLong( endTimeStr ));
      retValue.add( new S5Partition( partitionName, endTime ) );
    }
    return retValue;
  }

  /**
   * Запрос на создание разделов (партиций) указанной таблицы
   * <p>
   * <li>1. %s - Имя схемы базы данных ;</li>
   * <li>2. %s - Имя таблицы;</li>
   * <li>3. %s - Имя раздела;</li>
   * <li>3. %s - метка времени завершения интервала времени значений раздела;</li>
   */
  static final String QFRMT_CREATE_PARTION = //
      "alter table %s.%s partition by range columns(" + FIELD_END_TIME + ") (" //
          + "partition %s values less than(%d)" //
          + ");";

  /**
   * Запрос на добавление раздела (партиции) в указанную таблицу
   * <p>
   * <li>1. %s - Имя схемы базы данных ;</li>
   * <li>2. %s - Имя таблицы;</li>
   * <li>3. %s - Имя раздела;</li>
   * <li>3. %s - метка времени завершения интервала времени значений раздела;</li>
   */
  static final String QFRMT_ADD_PARTION = //
      "alter table %s.%s add partition (" //
          + "partition %s values less than(%d)" //
          + ");";

  /**
   * Добавляет указанный раздел в указанную таблицу
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String имя схемы базы данных сервера
   * @param aTable String имя таблицы в которой находятся разделы
   * @param aInfo {@link S5Partition} описание раздела
   * @param aCreating boolean <true> добавить раздел в таблицу в которой не было разделов; <b>false</b> добавить раздел
   *          в таблицу с уже существующими разделами.
   * @return int количество удаленных блоков
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static int addPartition( EntityManager aEntityManager, String aScheme, String aTable, S5Partition aInfo,
      boolean aCreating ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable, aInfo );
    // Текст SQL-запроса
    String sql = format( aCreating ? QFRMT_CREATE_PARTION : QFRMT_ADD_PARTION, //
        aScheme, aTable, aInfo.name(), Long.valueOf( aInfo.interval().endTime() ) );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      int retCode = query.executeUpdate();
      return retCode;
    }
    catch( RuntimeException e ) {
      // Ошибка добавления разделов
      throw new TsIllegalArgumentRtException( e, ERR_ADD_PARTITION2, aScheme, aTable, aInfo, cause( e ) );
    }
  }

  /**
   * Формат NATIVE(!)-запроса количества строк в указанной таблице в указанных разделах
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBooleanEnity;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %s - список идентификаторов разделов через ','.</li>
   * <p>
   * Примечание: сделано без форматирования, так как используется в циклах формирования конечного sql-запроса
   */
  private static final String QFRMT_GET_COUNT_FOR_PARTITION = //
      "select count(*) from %s.%s partition(%s);";

  /**
   * Возвращает количество строк в разделе таблицы базе данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String имя схемы базы данных сервера
   * @param aTable String имя таблицы в которой находятся разделы
   * @param aPartition String имя раздела
   * @return int количество строк в разделе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static int getPartitionRowCount( EntityManager aEntityManager, String aScheme, String aTable, String aPartition ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable, aPartition );
    // Текст SQL-запроса
    String sql = format( QFRMT_GET_COUNT_FOR_PARTITION, aScheme, aTable, aPartition );
    // Выполнение запроса
    Query query = aEntityManager.createNativeQuery( sql );
    // Запрос данных
    List<BigInteger> entities = query.getResultList();
    if( entities.size() == 0 ) {
      // Нет данных
      return 0;
    }
    BigInteger retValue = entities.get( 0 );
    return retValue.intValue();
  }

  /**
   * Запрос на удаление раздела (партиции) из указанной таблицы
   * <p>
   * <li>1. %s - Имя схемы базы данных ;</li>
   * <li>2. %s - Имя таблицы;</li>
   * <li>3. %s - Имя раздела;</li>
   * <li>3. %s - метка времени завершения интервала времени значений раздела;</li>
   */
  static final String QFRMT_DROP_PARTION = //
      "alter table %s.%s drop partition %s;";

  /**
   * Удаляет указанный раздел из указанной таблицы
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String имя схемы базы данных сервера
   * @param aTable String имя таблицы в которой находятся разделы
   * @param aPartition String имя раздела
   * @return int количество удаленных блоков
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static int dropPartition( EntityManager aEntityManager, String aScheme, String aTable, String aPartition ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable, aPartition );
    int rowCount = getPartitionRowCount( aEntityManager, aScheme, aTable, aPartition );
    // Текст SQL-запроса
    String sql = format( QFRMT_DROP_PARTION, aScheme, aTable, aPartition );
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      query.executeUpdate();
      return rowCount;
    }
    catch( RuntimeException e ) {
      // Ошибка удаления раздела
      throw new TsIllegalArgumentRtException( e, ERR_DROP_PARTITION2, aScheme, aTable, aPartition, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Удаляет из блока значения которые не попадают в интервал
   *
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательности
   * @param aInterval {@link IQueryInterval} интервал запроса последовательности
   * @param aBlock {@link IS5SequenceBlock} блок
   * @return {@link IS5SequenceBlock} блок результат. null: все значения блока не попадают в интервал
   * @param <V> тип значений блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static <V extends ITemporal<?>> IS5SequenceBlock<V> trimOrNull( IS5SequenceFactory<V> aFactory,
      IQueryInterval aInterval, IS5SequenceBlock<V> aBlock ) {
    TsNullArgumentRtException.checkNulls( aFactory, aInterval, aBlock );
    IParameterized typeInfo = aFactory.typeInfo( aBlock.gwid() );
    long ist = S5SequenceBlock.alignByDDT( typeInfo, aInterval.startTime() );
    long iet = S5SequenceBlock.alignByDDT( typeInfo, aInterval.endTime() );
    if( ist <= aBlock.startTime() && aBlock.endTime() <= iet ) {
      // Все значения блока попадают в интервал
      return aBlock;
    }
    // Признак открытого начала
    boolean isStartOpen = (aInterval.type() == OSCE || aInterval.type() == OSOE);
    // Признак открытого завершения
    boolean isEndOpen = (aInterval.type() == CSOE || aInterval.type() == OSOE);

    // Индекс первого значения в интервале, включительно
    int firstIndex = (ist < aBlock.startTime() ? 0 : //
        aBlock.firstByTime( aBlock.endTime() < ist ? aBlock.endTime() : ist ));
    // Сдвиг индекса в прошлое в соответствии типом интервала
    while( firstIndex > 0 ) { //
      long currTime = aBlock.timestamp( firstIndex );
      long nextTime = aBlock.timestamp( firstIndex - 1 );
      if( isStartOpen && currTime >= ist || //
          !isStartOpen && nextTime == ist ) {
        firstIndex--;
        isStartOpen = (currTime > ist ? isStartOpen : false);
        continue;
      }
      break;
    }
    // Индекс последнего значения в интервале, включительно
    int lastIndex = (aBlock.endTime() < iet ? aBlock.size() - 1 : //
        aBlock.lastByTime( iet < aBlock.startTime() ? aBlock.startTime() : iet ));
    // Сдвиг индекса в будущее в соответствии типом интервала
    while( lastIndex + 1 < aBlock.size() ) {
      long currTime = aBlock.timestamp( lastIndex );
      long nextTime = aBlock.timestamp( lastIndex + 1 );
      if( isEndOpen && currTime <= iet || //
          !isEndOpen && nextTime == iet ) {
        lastIndex++;
        isEndOpen = (currTime < iet ? isEndOpen : false);
        continue;
      }
      break;
    }
    // Восстановление признаков
    isStartOpen = (aInterval.type() == OSCE || aInterval.type() == OSOE);
    isEndOpen = (aInterval.type() == CSOE || aInterval.type() == OSOE);
    // Список значений выбранных в указанном интервале. aAllowDuplicates = true
    ITimedListEdit<V> values = new S5FixedCapacityTimedList<>( lastIndex - firstIndex + 1, true );
    for( int index = firstIndex; index <= lastIndex; index++ ) {
      V value = aBlock.getValue( index );
      long timestamp = value.timestamp();
      if( !isStartOpen && timestamp < ist ) {
        continue;
      }
      if( !isEndOpen && iet < timestamp ) {
        continue;
      }
      values.add( value );
    }
    if( values.size() == 0 ) {
      return null;
    }
    // Создание блока с значениями которые находятся в блоке
    return aFactory.createBlock( aBlock.gwid(), values );
  }
}
