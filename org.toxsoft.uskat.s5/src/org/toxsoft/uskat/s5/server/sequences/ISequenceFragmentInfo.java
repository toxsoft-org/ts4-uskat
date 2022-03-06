package org.toxsoft.uskat.s5.server.sequences;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Информация о фрагментации данного
 *
 * @author mvk
 */
public interface ISequenceFragmentInfo
    extends Comparable<ITimeInterval> {

  /**
   * Информация об отсутствии фрагментации
   */
  ISequenceFragmentInfo NULL = new InternalNullSequenceFragmentInfo();

  /**
   * Возвращает имя таблицы хранения данного
   *
   * @return String имя таблицы в базе данных
   */
  String tableName();

  /**
   * Возвращает идентификатор данного
   *
   * @return {@link Gwid} идентификатор данного
   */
  Gwid gwid();

  /**
   * Возвращает интервал в котором находятся фрагменты последовательности
   *
   * @return {@link ITimeInterval} интервал фрагментов последовательности
   */
  ITimeInterval interval();

  /**
   * Возвращает количество фрагментов в интервале
   *
   * @return количество фрагментов. < 0: неопределенно
   */
  int fragmentCount();

  /**
   * Возвращает количество фрагментов найденных ПОСЛЕ интервала {@link #interval()}
   *
   * @return количество фрагментов. < 0: неопределенно
   */
  int fragmentAfterCount();
}

/**
 * Реализация несуществующей фрагментации {@link ISequenceFragmentInfo#NULL}.
 */
class InternalNullSequenceFragmentInfo
    implements ISequenceFragmentInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link ISequenceFragmentInfo#NULL}.
   *
   * @return Object объект {@link ISequenceFragmentInfo#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    // return ISequenceFragmentInfo.NULL;
    return null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов ISequenceFragmentInfo
  //
  @Override
  public String tableName() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Gwid gwid() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public ITimeInterval interval() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentCount() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public int fragmentAfterCount() {
    throw new TsNullObjectErrorRtException();
  }

  // ------------------------------------------------------------------------------------
  // Реализация Comparable
  //
  @Override
  public int compareTo( ITimeInterval aO ) {
    return 0;
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
    return ISequenceFragmentInfo.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
