package org.toxsoft.uskat.s5.server.sequences.writer;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;

/**
 * Писатель значений последовательностей данных {@link IS5Sequence}
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceWriter<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends ICooperativeMultiTaskable, ICloseable {

  /**
   * Установить новую конфигурацию подсистемы хранения (данные/команды/события).
   *
   * @param aConfiguration {@link IOptionSet} конфигурация подсистемы хранения.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setConfiguration( IOptionSet aConfiguration );

  /**
   * Запись в dbms значений последовательности данных
   *
   * @param aEntityManager {@link EntityManager} мененджер постоянства который МОЖЕТ использовать писатель для записи
   * @param aSequences {@link IList}&lt;{@link IS5Sequence}&gt; список последовательностей значений
   * @return {@link IS5SequenceWriteStat} статистика выполнения записи
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество описаний не равно количеству последовательностей
   * @throws TsIllegalArgumentRtException какое либо данное не существует
   * @throws TsIllegalStateRtException метод должен быть вызван из открытой транзакции
   * @throws TsIllegalStateRtException попытка конкурентного изменения данных (в нескольких транзакциях)
   */
  IS5SequenceWriteStat write( EntityManager aEntityManager, IList<S> aSequences );

  /**
   * Дефрагментация блоков последовательностей
   * <p>
   * Ничего не делает, если писателю не требуется регламент
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для дефрагментации блоков (смотри {@link S5SequenceUnionConfig}).
   * @return {@link IS5SequenceUnionStat} статистика процесса объединения
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceUnionStat union( String aAuthor, IOptionSet aArgs );

  /**
   * Выполнение операций над разделами таблиц
   * <p>
   * Ничего не делает, если писателю не требуется выполнять операции над разделами
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для операций над разделами (смотри {@link S5SequencePartitionConfig}).
   * @return {@link IS5SequencePartitionStat} статистика процесса выполнения операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequencePartitionStat partition( String aAuthor, IOptionSet aArgs );

  /**
   * Выполняет проверку/исправление блоков последовательностей
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для проверки блоков (смотри {@link S5SequenceValidationConfig}).
   * @return {@link IS5SequenceValidationStat} статистика процесса проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceValidationStat validation( String aAuthor, IOptionSet aArgs );
}
