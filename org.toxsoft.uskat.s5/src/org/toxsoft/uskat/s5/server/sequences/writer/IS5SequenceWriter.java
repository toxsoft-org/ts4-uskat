package org.toxsoft.uskat.s5.server.sequences.writer;

import javax.persistence.EntityManager;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;

/**
 * Писатель значений последовательностей данных {@link IS5Sequence}
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceWriter<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends ICloseable {

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
   * @param aArgs {@link IOptionSet} аргументы для дефрагментации блоков (смотри {@link IS5SequenceUnionOptions}).
   * @return {@link IS5SequenceUnionStat} статистика процесса объединения
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceUnionStat union( IOptionSet aArgs );

  /**
   * Удаление блоков последовательностей
   * <p>
   * Ничего не делает, если писателю не требуется регламент
   *
   * @param aArgs {@link IOptionSet} аргументы для удаления блоков (смотри {@link IS5SequenceRemoveOptions}).
   * @return {@link IS5SequenceRemoveStat} статистика процесса удаления
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceRemoveStat remove( IOptionSet aArgs );

  /**
   * Выполняет проверку/исправление блоков последовательностей
   *
   * @param aArgs {@link IOptionSet} аргументы для проверки блоков (смотри {@link IS5SequenceValidationOptions}).
   * @return {@link IS5SequenceValidationStat} статистика процесса проверки
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceValidationStat validation( IOptionSet aArgs );

}
