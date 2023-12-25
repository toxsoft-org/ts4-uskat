package org.toxsoft.uskat.s5.server.sequences;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.IS5BackendCoreInterceptor;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5ClassesInterceptor;
import org.toxsoft.uskat.s5.server.sequences.impl.S5Sequence;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReader;

/**
 * Локальный интерфейс поддержки бекенда обрабатывающего последовательности данных {@link IS5Sequence}.
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
@Local
public interface IS5BackendSequenceSupportSingleton<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends IS5BackendSupportSingleton, IS5SequenceReader<S, V>, //
    IS5BackendCoreInterceptor, //
    IS5ClassesInterceptor, //
    IS5ObjectsInterceptor {

  /**
   * Синхронная запись последовательности значений указанных данных
   *
   * @param aSequences {@link IList}&lt;{@link S5Sequence}&gt; последовательность значений данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void writeSequences( IList<S> aSequences );

  /**
   * Асинхронная запись запись последовательности значений указанных данных
   *
   * @param aSequences {@link IList}&lt;{@link S5Sequence}&gt; последовательность значений данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void writeAsyncSequences( IList<S> aSequences );

  // ------------------------------------------------------------------------------------
  // Обслуживание хранилища значений
  //
  /**
   * Запуск задачи дефрагментации блоков последовательности значений данных
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для дефрагментации блоков.
   * @return {@link IS5SequenceUnionStat} статистика процесса объединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество потоков <= 0
   */
  IS5SequenceUnionStat union( String aAuthor, IOptionSet aArgs );

  /**
   * Запуск задачи обработки разделов таблиц значений храненимых данных
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для удаления блоков.
   * @return {@link IS5SequenceUnionStat} статистика процесса удаления
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество потоков <= 0
   */
  IS5SequencePartitionStat partition( String aAuthor, IOptionSet aArgs );

  /**
   * Запуск задачи проверки блоков ВСЕХ данных и восстановления их состояния
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для проверки блоков.
   * @return {@link IS5SequenceValidationStat} статистика процесса проверки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный интервал времени
   * @throws TsIllegalArgumentRtException количество потоков <= 0
   */
  IS5SequenceValidationStat validation( String aAuthor, IOptionSet aArgs );

}
