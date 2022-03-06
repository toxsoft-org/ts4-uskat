package org.toxsoft.uskat.s5.server.sequences.writer;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;

/**
 * Статистика ввода/вывода блоков {@link ISequenceBlock} в dbms
 *
 * @author mvk
 */
public interface IS5DbmsStatistics {

  /**
   * Неопределенная статистика.
   */
  IS5DbmsStatistics NULL = new InternalNullStatistics();

  /**
   * Возвращает общее количество загруженных блоков последовательности
   *
   * @return int количество блоков
   */
  int loadedCount();

  /**
   * Возвращает общее время загрузки блоков последовательности
   *
   * @return int время (мсек)
   */
  int loadedTime();

  /**
   * Возвращает общее количество добавленных блоков в dbms
   *
   * @return int количество блоков
   */
  int insertedCount();

  /**
   * Возвращает общее время добавления блоков в dbms
   *
   * @return int время (мсек)
   */
  int insertedTime();

  /**
   * Возвращает общее количество обновленных блоков в dbms
   *
   * @return int количество блоков
   */
  int mergedCount();

  /**
   * Возвращает общее время обновления блоков в dbms
   *
   * @return int время (мсек)
   */
  int mergedTime();

  /**
   * Возвращает общее количество удаленных блоков в dbms
   *
   * @return int количество блоков
   */
  int removedCount();

  /**
   * Возвращает общее время удаления блоков в dbms
   *
   * @return int время (мсек)
   */
  int removedTime();

  /**
   * Количество ошибок записи хранимых данных
   *
   * @return int количество ошибок
   */
  int writeErrorCount();

  /**
   * Количество проведенных операций дефрагментаций базы данных
   *
   * @return int количество проведенных операций дефрагментации
   */
  int fragmentCount();

  /**
   * Количество обработанных данных при обработке фрагментации
   *
   * @return int количество обработанных данных
   */
  int fragmentLookuped();

  /**
   * Количество дефрагментированных данных
   *
   * @return int количество обработанных фрагментированных данных
   */
  int fragmentThreaded();

  /**
   * Количество обработанных фрагментированных блоков данных
   *
   * @return int количество обработанных фрагментированных блоков данных
   */
  int fragmentBlocks();

  /**
   * Количество ошибок дефрагментации
   *
   * @return int количество ошибок дефрагментации
   */
  int fragmentErrorCount();
}

/**
 * Неопределенная статистика.
 */
class InternalNullStatistics
    implements IS5DbmsStatistics, Serializable {

  private static final long serialVersionUID = 157157L;

  // ------------------------------------------------------------------------------------
  // Реализация IS5DbmsStatistics
  //
  @Override
  public int loadedCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int loadedTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int insertedCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int insertedTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int mergedCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int mergedTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int removedCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int removedTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int writeErrorCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentLookuped() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentThreaded() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentBlocks() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentErrorCount() {
    throw new TsNullObjectErrorRtException();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return IS5DbmsStatistics.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5DbmsStatistics#NULL}.
   *
   * @return Object объект {@link IS5DbmsStatistics#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5DbmsStatistics.NULL;
  }
}
