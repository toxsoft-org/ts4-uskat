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
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;
import org.toxsoft.uskat.s5.server.sequences.*;
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
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
      ISequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
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
      ISequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aFirstPosition int позиция с которой у dbms запрашивается результат
   * @param aMaxResultCount int максимальное количество блоков которое может быть обработано за один вызов. <= 0:
   *          запрашиваются все данные
   * @return List&lt;{@link ISequenceBlock}&lt;V&gt;&gt; последовательность блоков
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> List<ISequenceBlock<V>> loadBlocks( EntityManager aEntityManager,
      ISequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval, int aFirstPosition, int aMaxResultCount ) {
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
      return (List<ISequenceBlock<V>>)entities;
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
   * Формат запроса удаления блоков данного по идентификатору данного в указанном интервале или на его границах
   * <p>
   * Интервал закрытый с начала, закрытый по завершению
   * <p>
   * <li>1. %s - имя таблицы блока, например, S5HistDataAsyncBlock;</li>
   * <li>2. %s - идентификатор данного (gwid);</li>
   * <li>3. %d - время начала запроса (startTime).</li>
   * <li>4. %d - время завершения запроса (endTime).</li>
   */
  private static final String QFRMT_CSCE_DELETE = "delete from %s where" //
      + "(" + FIELD_GWID + "='%s')and" //
      + "(%d<=" + FIELD_START_TIME + ")and(" + FIELD_END_TIME + "<=%d)";

  /**
   * Формат запроса удаления блоков данного по идентификатору данного в указанном интервале или на его границах
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
  private static final String QFRMT_OSOE_DELETE = "delete from %s where" //
      + "(" + FIELD_GWID + "='%s')and" //
      + "((%d<=" + FIELD_START_TIME + ")and(" + FIELD_START_TIME + "<=%d)or"//
      + "(%d<=" + FIELD_END_TIME + ")and(" + FIELD_END_TIME + "<=%d)or" //
      + "(%d<=" + FIELD_START_TIME + ")and(" + FIELD_END_TIME + "<=%d))";

  /**
   * Формат запроса удаления блоков данного по идентификатору данного в указанном интервале или на его границах
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
  private static final String QFRMT_CSOE_DELETE = "delete from %s where"//
      + "(" + FIELD_GWID + "='%s')and" //
      + "((%d<=" + FIELD_END_TIME + ")and(" + FIELD_END_TIME + "<=%d)or" //
      + "(%d<= " + FIELD_START_TIME + ")and(" + FIELD_END_TIME + "<=%d))";

  /**
   * Формат запроса удаления блоков данного по идентификатору данного в указанном интервале или на его границах
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
  private static final String QFRMT_OSCE_DELETE = "delete from %s where" //
      + "(" + FIELD_GWID + "='%s')and" //
      + "((%d<=" + FIELD_START_TIME + ")and(" + FIELD_START_TIME + "<=%d)or"//
      + "(%d<=" + FIELD_START_TIME + ")and(" + FIELD_END_TIME + "<=%d))";

  /**
   * Удаляет блоки данного в указанном интервале
   * <p>
   * Блоки удаляется полностью даже если частично попадают в указанный интервал
   * <p>
   * Если блоков нет, то ничего не делает.
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал удаляемых блоков
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> void removeBlocks( EntityManager aEntityManager, ISequenceFactory<V> aFactory,
      Gwid aGwid, IQueryInterval aInterval ) {
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
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    // Текст SQL-запроса
    String sql = EMPTY_STRING;
    sql = switch( aInterval.type() ) {
      case CSCE -> format( QFRMT_CSCE_DELETE, tableName, aGwid, st, et );
      case OSOE -> format( QFRMT_OSOE_DELETE, tableName, aGwid, st, et, st, et, st, et );
      case CSOE -> format( QFRMT_CSOE_DELETE, tableName, aGwid, st, et, st, et );
      case OSCE -> format( QFRMT_OSCE_DELETE, tableName, aGwid, st, et, st, et );
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    try {
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql );
      query.executeUpdate();
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aTimestamp long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return {@link ISequenceBlock}&lt;V&gt; найденный блок. null: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> ISequenceBlock<V> findBlockBefore( EntityManager aEntityManager,
      ISequenceFactory<V> aFactory, Gwid aGwid, long aTimestamp ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Имя класса реализации блока
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
    // Имя таблицы
    String tableName = getLast( blockImplClass );
    try {
      List<ISequenceBlock<V>> blocks;
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
      blocks = (List<ISequenceBlock<V>>)query.getResultList();
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
   * @return {@link IMap}&lt;Long,{@link IList}&lt;{@link ISequenceBlock}&lt;V&gt;&gt; карта найденных блоков.<br>
   *         Ключ: идентификатор данного<br>
   *         Значение: список блоков.
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException блоки/blob значений реализуются разными классами
   */
  @SuppressWarnings( { "unchecked", "resource" } )
  static <V extends ITemporal<?>> IMap<Gwid, IList<ISequenceBlock<V>>> readBlocks( IS5SequenceReadQuery aQuery,
      IGwidList aGwids ) {
    // 2020-10-30 mvkd
    // GwidList aGwids = new GwidList();
    // for( int index = 0, n = aGwids1.size(); index < n; index++ ) {
    // if( index > 0 ) {
    // break;
    // }
    // Gwid gwid = aGwids1.get( index );
    // if( gwid.toString().indexOf( "startTime" ) >= 0 ) {
    // System.err.println( "S5SequenceSQL.readBlock. find startTime" );
    // }
    // aGwids.add( gwid );
    // }
    TsNullArgumentRtException.checkNulls( aQuery, aGwids );
    // Количество запрашиваемых данных
    int count = aGwids.size();
    if( count == 0 ) {
      // Частный случай пустой запрос. Формирование пустого результата
      IMapEdit<Gwid, IListEdit<ISequenceBlock<V>>> retValue = new ElemMap<>();
      for( Gwid gwid : aGwids ) {
        retValue.put( gwid, IList.EMPTY );
      }
      return (IMap<Gwid, IList<ISequenceBlock<V>>>)(Object)retValue;
    }
    // Фабрика формирования значений
    ISequenceFactory<V> factory = (ISequenceFactory<V>)aQuery.factory();
    // Список описаний типов данных
    IListEdit<IParameterized> typeInfos = new ElemArrayList<>( aGwids.size() );
    for( Gwid gwid : aGwids ) {
      // TODO: 2020-12-07 mvkd
      // if( gwid.toString().equals( "tm.TrackChain[argTrackChain176a]$rtdata(routeNumber)" ) ) {
      typeInfos.add( factory.typeInfo( gwid ) );
      // }
    }
    // TODO: 2020-12-07 mvkd
    // if( typeInfos.size() == 0 ) {
    // return IMap.EMPTY;
    // }
    // Имя класса реализации блока
    String blockImplClass = blockImplClassFromInfos( typeInfos );
    // Имя класса реализации blob
    String blobImplClass = blobImplClassFromInfos( typeInfos );
    // Журнал
    String infoStr = format( MSG_INFO, blockImplClass, Integer.valueOf( count ) );
    // Интервал запроса
    IQueryInterval interval = aQuery.interval();
    EQueryIntervalType intervalType = interval.type();

    // TODO:
    // 2020-10-30 mvkd
    // if( intervalType != CSCE ) {
    // logger.error(
    // "S5SequenceSQL.readBlocks(): обработка запроса по типу %s находится в отладке. Запрос выполняется для CSCE",
    // intervalType );
    // intervalType = CSCE;
    // }
    // 2020-10-31 mvk
    // Подготовка запроса
    String sql = "select * from(";
    if( intervalType == OSCE || intervalType == OSOE ) {
      // Запрос данных перед интервалом
      sql += "(" + prepareReadBeforeSQL( blockImplClass, blobImplClass, aGwids, interval.startTime() ) + ")union";
    }
    // Запрос данных на интервале
    sql += "(" + prepareReadSQL( blockImplClass, blobImplClass, aGwids, interval ) + ")";
    if( intervalType == CSOE || intervalType == OSOE ) {
      // Запрос данных после интервала
      sql += "union(" + prepareReadAfterSQL( blockImplClass, blobImplClass, aGwids, interval.endTime() ) + ")";
    }
    // 2020-10-31 mvk
    sql += ") as v order by v." + FIELD_START_TIME;
    // 2019-09-19 mvk mariadb 10.4.8, ошибка "Duplicate column name 'id'" - не понятно как это раньше работало! )))
    // sql += "order by startTime";
    // if( intervalType != CSCE ) {
    // sql += "order by " + FIELD_START_TIME;
    // }
    // Вывод текста запроса в журнал
    logger.debug( MSG_READ_BLOCK_SQL, infoStr, interval, sql );
    // Фактическое время начала данных
    long factStartTime = TimeUtils.MAX_TIMESTAMP;
    // Фактическое время завершения данных
    long factEndTime = TimeUtils.MIN_TIMESTAMP;
    // Возвращаемый результат
    IListEdit<ISequenceBlock<V>> readedBlocks = new ElemArrayList<>( count );
    // Запрос
    long traceQueryStartTime = System.currentTimeMillis();
    long traceStartTime = traceQueryStartTime;
    try {
      Connection connection = aQuery.connection();
      try( Statement statement = connection.createStatement(); ) {
        aQuery.addStatement( statement );
        try {
          try( ResultSet rs = statement.executeQuery( sql ); ) {
            traceStartTime = System.currentTimeMillis();
            // Вывод текста запроса в журнал
            logger.debug( MSG_READ_BLOCK_START, infoStr, interval );
            // statement.setPoolable( false );
            // statement.setMaxRows( infoCount );
            // statement.setFetchSize( infoCount );
            if( aQuery.maxExecutionTimeout() > 1000 ) {
              statement.setQueryTimeout( (int)(aQuery.maxExecutionTimeout() / 1000) );
            }
            for( boolean hasData = rs.first(); hasData; hasData = rs.next() ) {
              ISequenceBlock<V> block = factory.createBlock( blockImplClass, rs );
              if( readedBlocks.size() > 0 && readedBlocks.get( readedBlocks.size() - 1 ).equals( block ) ) {
                // throw new TsInternalErrorRtException();
                logger.warning( "Повтор чтения блока %s. Игнорирование. sql(%d) = \n%s", block,
                    Integer.valueOf( sql.length() ), sql );
                continue;
              }
              // Обработка блока согласно интервалу (чтобы в блоке не было лишних данных)
              block = trim( factory, interval, block );
              if( block == ISequenceBlock.NULL ) {
                // Все значения блока вне интервала
                continue;
              }
              // Формирование списка считанных блоков
              readedBlocks.add( block );
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
      // Формирование результата
      IMapEdit<Gwid, IListEdit<ISequenceBlock<V>>> retValue = new ElemMap<>();
      for( Gwid gwid : aGwids ) {
        retValue.put( gwid, new ElemArrayList<>( readedBlocks.size() ) );
      }
      for( ISequenceBlock<V> block : readedBlocks ) {
        retValue.getByKey( block.gwid() ).add( block );
      }
      long currTime = System.currentTimeMillis();
      ITimeInterval factInterval = ITimeInterval.NULL;
      if( factStartTime != TimeUtils.MAX_TIMESTAMP && factEndTime != TimeUtils.MIN_TIMESTAMP ) {
        factInterval = new TimeInterval( factStartTime, factEndTime );
      }
      Integer c = Integer.valueOf( readedBlocks.size() );
      Long ta = Long.valueOf( currTime - traceQueryStartTime );
      Long tq = Long.valueOf( traceStartTime - traceQueryStartTime );
      Long th = Long.valueOf( currTime - traceStartTime );
      logger.info( MSG_READ_BLOCK_END, infoStr, interval, factInterval, c, ta, tq, th );
      // Результат
      return (IMap<Gwid, IList<ISequenceBlock<V>>>)(Object)retValue;
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aBeforeTime long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return long время(мсек с начала эпохи) метка времени. Если блок не найден, то {@link TimeUtils#MIN_TIMESTAMP}
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findTimeBefore( EntityManager aEntityManager, ISequenceFactory<V> aFactory,
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aAfterTime long метка времени (мсек с начала эпохи) после которой или на которой проводится поиск блока со
   *          значением
   * @return long время(мсек с начала эпохи) Время НАЧАЛА блока. Если блок не найден, то {@link TimeUtils#MAX_TIMESTAMP}
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findTimeAfter( EntityManager aEntityManager, ISequenceFactory<V> aFactory,
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aToTime long (время мсек с начала эпохи) до которого проводить поиск фрагментированных блоков (включительно)
   * @param aMaxSize int размер блока меньше которого блок считается фрагментированным
   * @param aFragmentCountMin int минимальное количество блоков которые требуется для дефрагментации. <= 0: отключено
   * @param aFragmentCountMax int максимальное количество блоков которые требуетя для дефрагментации. <= 0: отключено
   * @param aFragmentTimeout long время (мсек) между блоками больше которого проводится принудительная дефрагментация.
   *          <= 0: отключено.
   * @return {@link ISequenceFragmentInfo} информация о фрагментации. {@link ISequenceFragmentInfo#NULL} нет
   *         фрагментации
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aFragmentCountMax = 1
   */
  static <V extends ITemporal<?>> ISequenceFragmentInfo findFragmentationTime( EntityManager aEntityManager,
      ISequenceFactory<V> aFactory, Gwid aGwid, long aToTime, int aMaxSize, int aFragmentCountMin,
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
        return ISequenceFragmentInfo.NULL;
      }
      if( fragmentStartTime >= aToTime ) {
        // До указанного времени нет фрагментации
        return ISequenceFragmentInfo.NULL;
      }
      // Интервал запроса размеров фрагментированных блоков
      IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, fragmentStartTime, aToTime );
      // Запрос меток времени и размеров (метка времени начала блока и его размер)
      List<Object[]> startsTimesSizesList = loadBlockStartTimesSizes( aEntityManager, aFactory, aGwid, interval, 0, 0 );
      // Общее количество найденных фрагментов
      int startsTimesSizesListSize = startsTimesSizesList.size();
      if( startsTimesSizesListSize == 0 ) {
        // Нет блоков для дефрагментации
        return ISequenceFragmentInfo.NULL;
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @return long метка времени завершения последнего блока. MIN_TIMESTAMP: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static <V extends ITemporal<?>> long findLastBlockEndTime( EntityManager aEntityManager, ISequenceFactory<V> aFactory,
      Gwid aGwid ) {
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
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aGwid {@link Gwid} идентификатор данного
   * @return {@link ISequenceBlock}&lt;V&gt; найденный блок. null: блок не найден
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  static <V extends ITemporal<?>> ISequenceBlock<V> findLastBlock( EntityManager aEntityManager,
      ISequenceFactory<V> aFactory, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aFactory, aGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aGwid );
    // Имя класса реализации блока
    String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
    // Имя таблицы
    String tableName = getLast( blockImplClass );
    try {
      List<ISequenceBlock<V>> blocks;
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
      blocks = (List<ISequenceBlock<V>>)query.getResultList();
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
  // TODO:
  // 2020-10-31 mvkd
  // private static final String QFRMT_NATIVE_JOIN_BLOCKS = "select * from %s as b1 inner join (%s) as b2 on(" //
  // + "(b1." + FIELD_GWID + "=b2." + FIELD_GWID + ")AND(b1." + FIELD_START_TIME + "=b2." + FIELD_START_TIME + ")" + //
  // ") order by " + "b1." + FIELD_START_TIME;
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
  // private static final String QFRMT_NATIVE_JOIN_BLOCKS =
  // "select * from %s as b1 inner join (%s) as b2 on b1." + FIELD_GWID + "=b2." + FIELD_GWID + " and b1."
  // + FIELD_START_TIME + "=b2." + FIELD_START_TIME + " order by startTime";

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
   * Подготовливает SQL запрос на поиск ближайших блоков для каждого указанного данного у которого есть значение на
   * указанной метке времени или перед ней
   *
   * @param aBlockImplClass String полное имя класса реализации блока значений
   * @param aBlobImplClass String полное имя класса реализации blob значений
   * @param aGwids {@link IGwidList} список идентификаторов данных
   * @param aTimestamp long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return String текст SQL запроса.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String prepareReadBeforeSQL( String aBlockImplClass, String aBlobImplClass, IGwidList aGwids,
      long aTimestamp ) {
    TsNullArgumentRtException.checkNulls( aBlobImplClass, aBlobImplClass, aGwids );
    String blockTableName = getLast( aBlockImplClass );
    String blobTableName = getLast( aBlobImplClass );
    // Загрузка блоков последовательности
    Long time = Long.valueOf( aTimestamp );
    if( aTimestamp == MIN_TIMESTAMP ) {
      time = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( aTimestamp == MAX_TIMESTAMP ) {
      time = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      Gwid gwid = aGwids.get( index );
      String subSql = format( QFRMT_NATIVE_BLOCKS_BEFORE_TIME_SUB, blockTableName, gwid, time, time, time );
      sb.append( "(" );
      sb.append( subSql );
      sb.append( ")" );
      if( index + 1 < n ) {
        sb.append( "union all" );
      }
    }
    // Добавление в запрос оператора объединения подзапросов
    return addJoinOpertatorToSQL( aBlockImplClass, blobTableName, sb.toString() );
  }

  /**
   * Подготовливает SQL запрос на поиск ближайших блоков для каждого указанного данного у которого есть значение на
   * указанной метке времени или после нее
   *
   * @param aBlockImplClass String полное имя класса реализации блока значений
   * @param aBlobImplClass String полное имя класса реализации blob значений
   * @param aGwids {@link IGwidList} список идентификаторов данных
   * @param aTimestamp long метка времени (мсек с начала эпохи) перед которой или на которой проводится поиск блока со
   *          значением
   * @return String текст SQL запроса.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String prepareReadAfterSQL( String aBlockImplClass, String aBlobImplClass, IGwidList aGwids,
      long aTimestamp ) {
    TsNullArgumentRtException.checkNulls( aBlobImplClass, aBlobImplClass, aGwids );
    String blockTableName = getLast( aBlockImplClass );
    String blobTableName = getLast( aBlobImplClass );
    // Загрузка блоков последовательности
    Long time = Long.valueOf( aTimestamp );
    if( aTimestamp == MIN_TIMESTAMP ) {
      time = Long.valueOf( MIN_TIMESTAMP + 1 );
    }
    if( aTimestamp == MAX_TIMESTAMP ) {
      time = Long.valueOf( MAX_TIMESTAMP - 1 );
    }
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      Gwid gwid = aGwids.get( index );
      String subSql = format( QFRMT_NATIVE_BLOCKS_AFTER_TIME_SUB, blockTableName, gwid, time, time, time );
      sb.append( "(" );
      sb.append( subSql );
      sb.append( ")" );
      if( index + 1 < n ) {
        sb.append( "union all" );
      }
    }
    // Добавление в запрос оператора объединения подзапросов
    return addJoinOpertatorToSQL( aBlockImplClass, blobTableName, sb.toString() );
  }

  /**
   * Подготовливает SQL запрос на получение блоков в указанном интервале
   *
   * @param aBlockImplClass String полное имя класса реализации блока значений
   * @param aBlobImplClass String полное имя класса реализации blob значений
   * @param aGwids {@link IGwidList} список идентификаторов данных
   * @param aInterval {@link ITimeInterval} интервал запроса
   * @return String текст SQL запроса.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String prepareReadSQL( String aBlockImplClass, String aBlobImplClass, IGwidList aGwids,
      ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aBlobImplClass, aBlobImplClass, aGwids, aInterval );
    String blockTableName = getLast( aBlockImplClass );
    String blobTableName = getLast( aBlobImplClass );
    // Загрузка блоков последовательности
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    startTime = (startTime == MIN_TIMESTAMP ? startTime + 1 : startTime);
    endTime = (endTime == MAX_TIMESTAMP ? endTime - 1 : endTime);
    Long st = Long.valueOf( startTime );
    Long et = Long.valueOf( endTime );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      Gwid gwid = aGwids.get( index );
      String subSql = format( QFRMT_NATIVE_BLOCKS_SUB, blockTableName, gwid, st, et, st, et, st, et );
      sb.append( "(" );
      sb.append( subSql );
      sb.append( ")" );
      if( index + 1 < n ) {
        sb.append( "union all" );
      }
    }
    // Добавление в запрос оператора объединения подзапросов
    return addJoinOpertatorToSQL( aBlockImplClass, blobTableName, sb.toString() );
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
   * @param aTypeInfos {@link IList}&lt;{@link IParameterized}&gt; список параметризованных описаний данных.
   * @return String полное имя класса реализации
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException пустой список описаний данных
   * @throws TsIllegalStateRtException блоки значений реализуются более чем одним классом
   */
  private static String blockImplClassFromInfos( IList<IParameterized> aTypeInfos ) {
    TsNullArgumentRtException.checkNull( aTypeInfos );
    TsIllegalArgumentRtException.checkFalse( aTypeInfos.size() > 0 );
    String retValue = null;
    for( IParameterized typeInfo : aTypeInfos ) {
      String blockImplClass = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
      if( retValue == null ) {
        retValue = blockImplClass;
        continue;
      }
      TsIllegalArgumentRtException.checkFalse( retValue.equals( blockImplClass ) );
    }
    return retValue;
  }

  /**
   * Возвращает полное имя класса реализации blob
   *
   * @param aTypeInfos {@link IList}&lt;{@link IParameterized}&gt; список параметризованных описаний данных.
   * @return String полное имя класса реализации
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException пустой список описаний данных
   * @throws TsIllegalStateRtException blob значений реализуются более чем одним классом
   */
  private static String blobImplClassFromInfos( IList<IParameterized> aTypeInfos ) {
    TsNullArgumentRtException.checkNull( aTypeInfos );
    TsIllegalArgumentRtException.checkFalse( aTypeInfos.size() > 0 );
    String retValue = null;
    for( IParameterized typeInfo : aTypeInfos ) {
      String blockImplClass = OP_BLOB_IMPL_CLASS.getValue( typeInfo.params() ).asString();
      if( retValue == null ) {
        retValue = blockImplClass;
        continue;
      }
      TsIllegalArgumentRtException.checkFalse( retValue.equals( blockImplClass ) );
    }
    return retValue;
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
   * @param aTableName String имя таблицы
   * @param aParams {@link IStringMap}&lt;Object&gt; карта параметров запроса.<br>
   *          Ключ: имя поля таблицы;Значение: значение поля.
   * @return {@link Query} созданный запрос
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Query createInsertQuery( EntityManager aEntityManager, String aTableName, IStringMap<Object> aParams ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTableName, aParams );
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
    String sql = format( QFRMT_INSERT_ROW, aTableName, formalParams.toString(), factParams.toString() );
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
   * @param aTableName String имя таблицы
   * @param aParams {@link IStringMap}&lt;Object&gt; карта параметров запроса.<br>
   *          Ключ: имя поля таблицы;Значение: значение поля.
   * @param aId {@link S5DataID} первичный ключ редактируемой записи
   * @return {@link Query} созданный запрос
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static Query createUpdateQuery( EntityManager aEntityManager, String aTableName, IStringMap<Object> aParams,
      S5DataID aId ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTableName, aParams, aId );
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
    String sql = format( QFRMT_UPDATE_ROW, aTableName, factParams.toString() );
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
   * Возвращает список идентификаторов всех данных которые храняться в базе данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aTableNames {@link IList}&lt;{@link Pair}&lt;String,String&gt;&gt; список пар таблиц реализации блоков
   *          ({@link Pair#left()}) и их blob {@link Pair#right()}
   * @return {@link IGwidList} список идентификаторов
   * @throws TsNullArgumentRtException аргумент = null
   */
  static IGwidList getAllGwids( EntityManager aEntityManager, IList<Pair<String, String>> aTableNames ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aTableNames );
    GwidList retValue = new GwidList();
    for( Pair<String, String> pair : aTableNames ) {
      // Имя таблицы
      String tableName = pair.left();
      // Текст SQL-запроса
      String sql = format( QFRMT_GET_GWIDS, tableName );
      // Выполнение запроса
      Query query = aEntityManager.createNativeQuery( sql, String.class );
      // Запрос данных
      List<String> entities = query.getResultList();
      for( String entity : entities ) {
        retValue.add( Gwid.KEEPER.str2ent( entity ) );
      }
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Удаляет из блока значения которые не попадают в интервал
   *
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательности
   * @param aInterval {@link IQueryInterval} интервал запроса последовательности
   * @param aBlock {@link ISequenceBlock} блок
   * @return {@link ISequenceBlock} блок результат. {@link ISequenceBlock#NULL}: все значения блока не попадают в
   *         интервал
   * @param <V> тип значений блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static <V extends ITemporal<?>> ISequenceBlock<V> trim( ISequenceFactory<V> aFactory,
      IQueryInterval aInterval, ISequenceBlock<V> aBlock ) {
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
    boolean isEndOpen = (aInterval.type() == OSCE || aInterval.type() == OSOE);

    // Индекс первого значения в интервале, включительно
    // int firstIndex = (ist < aBlock.startTime() ? 0 : aBlock.firstByTime( ist ));
    int firstIndex = (ist < aBlock.startTime() ? 0 : //
        aBlock.firstByTime( aBlock.endTime() < ist ? aBlock.endTime() : ist ));
    // Сдвиг индекса в прошлое в соответствии типом интервала
    while( firstIndex > 0 && //
        (isStartOpen && aBlock.timestamp( firstIndex ) >= ist
            || !isStartOpen && aBlock.timestamp( firstIndex ) == ist) ) {
      firstIndex--;
    }
    // Индекс последнего значения в интервале, включительно
    // int lastIndex = (aBlock.endTime() < iet ? aBlock.size() - 1 : aBlock.lastByTime( iet ));
    int lastIndex = (aBlock.endTime() < iet ? aBlock.size() - 1 : //
        aBlock.lastByTime( iet < aBlock.startTime() ? aBlock.startTime() : iet ));
    // Сдвиг индекса в будущее в соответствии типом интервала
    while( lastIndex + 1 < aBlock.size() && //
        (isEndOpen && aBlock.timestamp( lastIndex ) >= iet || !isEndOpen && aBlock.timestamp( lastIndex ) == iet) ) {
      lastIndex++;
    }

    // Список значений выбранных в указанном интервале. aAllowDuplicates = true
    ITimedListEdit<V> values = new S5FixedCapacityTimedList<>( lastIndex - firstIndex + 1, true );
    for( int index = firstIndex; index <= lastIndex; index++ ) {
      values.add( aBlock.getValue( index ) );
    }
    // Создание блока с значениями которые находятся в блоке
    return aFactory.createBlock( aBlock.gwid(), values );
  }
}
