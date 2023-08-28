package org.toxsoft.uskat.s5.server.sequences;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Информация об удалении значений данного
 *
 * @author mvk
 */
public interface IS5SequenceRemoveInfo
    extends Comparable<ITimeInterval> {

  /**
   * Информация об отсутствии удаления
   */
  IS5SequenceRemoveInfo NULL = new InternalNullSequenceRemoveInfo();

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
   * Возвращает интервал в котором находятся удаляемые значения последовательности
   *
   * @return {@link ITimeInterval} интервал значений последовательности
   */
  ITimeInterval interval();
}

/**
 * Реализация несуществующей фрагментации {@link IS5SequenceRemoveInfo#NULL}.
 */
class InternalNullSequenceRemoveInfo
    implements IS5SequenceRemoveInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5SequenceRemoveInfo#NULL}.
   *
   * @return Object объект {@link IS5SequenceRemoveInfo#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    // return IS5SequenceRemoveInfo.NULL;
    return null;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5SequenceFragmentInfo
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
    return IS5SequenceRemoveInfo.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
