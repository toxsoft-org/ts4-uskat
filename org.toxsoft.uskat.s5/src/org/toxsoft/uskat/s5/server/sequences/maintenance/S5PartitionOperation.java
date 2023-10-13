package org.toxsoft.uskat.s5.server.sequences.maintenance;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Информация об операциях выполняемых над разделами (partition) таблицы.
 *
 * @author mvk
 */
public final class S5PartitionOperation
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  private final String                 tableName;
  private final IListEdit<S5Partition> createPartitions = new ElemLinkedList<>();
  private final IListEdit<S5Partition> addPartitions    = new ElemLinkedList<>();
  private final IListEdit<S5Partition> removePartitions = new ElemLinkedList<>();
  private final GwidList               removeGwids      = new GwidList();

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5PartitionOperation( String aTableName ) {
    TsNullArgumentRtException.checkNulls( aTableName );
    tableName = aTableName;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает имя таблицы хранения данного
   *
   * @return String имя таблицы в базе данных
   */
  public String tableName() {
    return tableName;
  }

  /**
   * Список идентификаторов данных в удляемых таблицах
   *
   * @return {@link IGwidList} список идентификаторов данных
   */
  public IGwidList gwids() {
    return removeGwids;
  }

  /**
   * Возвращает список разделов добавляемых в таблицу БЕЗ разделов
   *
   * @return {@link IList}&lt;{@link S5Partition}&gt; список добавляемых разделов
   */
  public IListEdit<S5Partition> createPartitions() {
    return createPartitions;
  }

  /**
   * Возвращает список разделов добавляемых в таблицу с уже имеющей разделы
   *
   * @return {@link IList}&lt;{@link S5Partition}&gt; список добавляемых разделов
   */
  public IListEdit<S5Partition> addPartitions() {
    return addPartitions;
  }

  /**
   * Возвращает список удаляемых разделов
   *
   * @return {@link IList}&lt;{@link S5Partition}&gt; список удаляемых разделов
   */
  public IListEdit<S5Partition> removePartitions() {
    return removePartitions;
  }

  /**
   * Возвращает список данных хранимых в удаленных разделах
   *
   * @return {@link GwidList} список данных
   */
  public GwidList removeGwids() {
    return removeGwids;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return tableName + ':' + removePartitions;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + tableName.hashCode();
    result = TsLibUtils.PRIME * result + removeGwids.hashCode();
    result = TsLibUtils.PRIME * result + removePartitions.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( aObject.getClass() != S5PartitionOperation.class ) {
      return false;
    }
    S5PartitionOperation other = (S5PartitionOperation)aObject;
    if( !tableName.equals( other.tableName() ) ) {
      return false;
    }
    if( !removeGwids.equals( other.gwids() ) ) {
      return false;
    }
    if( !removePartitions.equals( other.removePartitions() ) ) {
      return false;
    }
    return true;
  }
}
