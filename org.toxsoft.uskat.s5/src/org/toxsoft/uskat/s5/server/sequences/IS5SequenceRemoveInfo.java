package org.toxsoft.uskat.s5.server.sequences;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequencePartitionInfo;

/**
 * Информация об удалении значений данного
 *
 * @author mvk
 */
public interface IS5SequenceRemoveInfo {

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
   * Список идентификаторов данных в удляемых таблицах
   *
   * @return {@link IGwidList} список идентификаторов данных
   */
  IGwidList gwids();

  /**
   * Возвращает список описаний удаляемых разделов
   *
   * @return {@link IList}&lt;{@link S5SequencePartitionInfo}&gt; список описаний разделов
   */
  IList<S5SequencePartitionInfo> partitionInfos();
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
  public IGwidList gwids() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IList<S5SequencePartitionInfo> partitionInfos() {
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
    return IS5SequenceRemoveInfo.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
