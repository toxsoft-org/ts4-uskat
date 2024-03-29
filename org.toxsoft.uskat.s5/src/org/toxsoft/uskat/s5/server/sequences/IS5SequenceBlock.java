package org.toxsoft.uskat.s5.server.sequences;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;

/**
 * Блок значений последовательности {@link IS5Sequence}.
 *
 * @author mvk
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceBlock<V extends ITemporal<?>>
    extends IS5SequenceBlockReader {

  /**
   * "Нулевой" блок, всместо использования null.
   * <p>
   * Все методы {@link IS5SequenceBlock} выбрасывают исключение {@link TsIllegalStateRtException}
   */
  IS5SequenceBlock<?> NULL = new InternalNullDataValue();

  /**
   * Возвращает НЕабстрактный {@link Gwid}-идентификатор данного значения которого хранятся в блоке
   * <ul>
   * <li>Для данных реального времени - {@link EGwidKind#GW_RTDATA};</li>
   * <li>Для событий и истории команд - {@link EGwidKind#GW_CLASS}.</li>
   * </ul>
   *
   * @return {@link Gwid} идентификатор
   */
  Gwid gwid();

  /**
   * Возвращает признак того, что значения в блоке являются синхронными.
   * <p>
   * По своей сути для данных реального времени тоже самое, что и {@link IDtoRtdataInfo#isSync()}. Для истории событий и
   * команд данные всегда асинхронные.
   *
   * @return boolean <b>true</b> данное является синхронным; <b>false</b> данное является асинхронным.
   */
  boolean isSync();

  /**
   * Возвращает количество значений(элементов) в блоке
   *
   * @return int количество элементов
   */
  int size();

  /**
   * Возвращает метку времени значения указанного по индексу значения
   *
   * @param aIndex int индекс значения в массиве значений блока
   * @return long метка времени значения (мсек с начала эпохи)
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  long timestamp( int aIndex );

  /**
   * Возвращает значение блока в виде {@link ITemporal} по индексу
   *
   * @param aIndex int индекс значения блока
   * @return V значение
   * @throws TsIllegalArgumentRtException неверный индекс значения
   */
  V getValue( int aIndex );

  /**
   * Создает курсор получения значений последовательности
   *
   * @return {@link IS5SequenceCursor} курсор
   */
  IS5SequenceCursor<V> createCursor();

  /**
   * Возвращает время начала данных в блоке
   * <p>
   * {@link ITimeInterval} не используется в блоке по причине производительности.
   *
   * @return long время (мсек с начала эпохи) начала данных в блоке. Включительно
   */
  long startTime();

  /**
   * Возвращает время завершения данных в блоке
   * <p>
   * {@link ITimeInterval} не используется в блоке по причине производительности.
   *
   * @return long время (мсек с начала эпохи) завершения данных в блоке. Включительно
   */
  long endTime();

  /**
   * Возвращает индекс первого значения в блоке с указанной или ближайшей к ней метке времени
   *
   * @param aTimestamp long время (мсек с начала эпохи) начала поиска значений (включительно) в блоке.
   * @return int индекс первого найденого значения. -1: значения не найдены (пустой блок)
   * @throws TsIllegalArgumentRtException метка не попадает диапазон времени блока
   * @throws TsIllegalArgumentRtException для блоков сихронных значений метка времени должна быть выравнена по интервалу
   */
  int firstByTime( long aTimestamp );

  /**
   * Возвращает индекс последнего значения в блоке с указанной или ближайшей к ней метке времени
   *
   * @param aTimestamp long время (мсек с начала эпохи) начала поиска значений (включительно) в блоке.
   * @return int индекс первого найденого значения. -1: значения не найдены (пустой блок)
   * @throws TsIllegalArgumentRtException метка не попадает диапазон времени блока
   * @throws TsIllegalArgumentRtException для блоков сихронных значений метка времени должна быть выравнена по интервалу
   */
  int lastByTime( long aTimestamp );
}

/**
 * Несуществующее значение
 *
 * @author mvk
 */
interface IInternalNullValue
    extends ITemporal<IInternalNullValue> {
  // nop
}

/**
 * Реализация несуществующего блока
 */
class InternalNullDataValue
    implements IS5SequenceBlock<IInternalNullValue>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5SequenceBlock#NULL}.
   *
   * @return Object объект {@link IS5SequenceBlock#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5SequenceBlock.NULL;
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
    return IS5SequenceBlock.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5SequenceBlock
  //
  @Override
  public Gwid gwid() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public boolean isSync() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int size() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long timestamp( int aIndex ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IInternalNullValue getValue( int aIndex ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IS5SequenceCursor<IInternalNullValue> createCursor() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long startTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public long endTime() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int firstByTime( long aTimestamp ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int lastByTime( long aTimestamp ) {
    throw new TsNullObjectErrorRtException();
  }
}
