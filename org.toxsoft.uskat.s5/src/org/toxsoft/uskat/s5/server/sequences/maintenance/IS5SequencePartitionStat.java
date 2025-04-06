package org.toxsoft.uskat.s5.server.sequences.maintenance;

import java.io.*;

import org.toxsoft.core.tslib.coll.*;

/**
 * Статистика выполнения процесса выполнения операций над разделами таблиц хранимых данных
 *
 * @author mvk
 */
public interface IS5SequencePartitionStat {

  /**
   * Пустая (нет связи с сервером) информация о сессии пользователя
   */
  IS5SequencePartitionStat NONE = new NonePartitionStat();

  /**
   * Возвращает список описаний операций над разделами таблиц
   *
   * @return {@link IList}&lt;{@link S5PartitionOperation}&gt; список описаний операций над разделами таблиц
   */
  IList<S5PartitionOperation> operations();

  /**
   * Возвращает общее количество проанализированных последовательностей при поиске значений для удаления
   *
   * @return int количество последовательностей
   */
  int lookupCount();

  /**
   * Возвращает количество добавленных разделов таблиц
   *
   * @return int количество разделов
   */
  int addedCount();

  /**
   * Возвращает количество разделов таблиц удаленных из dbms
   *
   * @return int количество разделов
   */
  int removedPartitionCount();

  /**
   * Возвращает количество блоков удаленных из dbms
   *
   * @return int количество блоков
   */
  int removedBlockCount();

  /**
   * Возвращает общее количество ошибок возникших при дефрагментации
   *
   * @return общее количество ошибок дефрагментации блоков
   */
  int errorCount();

  /**
   * Возвращает текущий размер очереди на удаление данных
   *
   * @return int количество данных в очереди
   */
  int queueSize();
}

class NonePartitionStat
    implements IS5SequencePartitionStat {

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5SequencePartitionStat#NONE}.
   *
   * @return Object объект {@link IS5SequencePartitionStat#NONE}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5SequencePartitionStat.NONE;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequencePartitionStat
  //
  @Override
  public IList<S5PartitionOperation> operations() {
    return IList.EMPTY;
  }

  @Override
  public int lookupCount() {
    return 0;
  }

  @Override
  public int addedCount() {
    return 0;
  }

  @Override
  public int removedPartitionCount() {
    return 0;
  }

  @Override
  public int removedBlockCount() {
    return 0;
  }

  @Override
  public int errorCount() {
    return 0;
  }

  @Override
  public int queueSize() {
    return 0;
  }
}
