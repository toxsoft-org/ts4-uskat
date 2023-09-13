package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceRemoveInfo;

/**
 * Реализация {@link IS5SequenceRemoveInfo}
 *
 * @author mvk
 */
public class S5SequenceRemoveInfo
    implements IS5SequenceRemoveInfo, Serializable {

  private static final long serialVersionUID = 157157L;

  private final String                         tableName;
  private final IGwidList                      gwids;
  private final IList<S5SequencePartitionInfo> partitionInfos;

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwids {@link IGwidList} список идентификаторов данных удаляемы таблиц
   * @param aPartitionInfos {@link IList}&lt;{@link S5SequencePartitionInfo}&gt; список описаний удалямых разделов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5SequenceRemoveInfo( String aTableName, IGwidList aGwids, IList<S5SequencePartitionInfo> aPartitionInfos ) {
    TsNullArgumentRtException.checkNulls( aTableName, aPartitionInfos );
    tableName = aTableName;
    gwids = aGwids;
    partitionInfos = aPartitionInfos;
  }

  // ------------------------------------------------------------------------------------
  // /Реализация IS5SequenceRemoveInfo
  //
  @Override
  public String tableName() {
    return tableName;
  }

  @Override
  public IGwidList gwids() {
    return gwids;
  }

  @Override
  public IList<S5SequencePartitionInfo> partitionInfos() {
    return partitionInfos;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return tableName + ':' + partitionInfos;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + tableName.hashCode();
    result = TsLibUtils.PRIME * result + gwids.hashCode();
    result = TsLibUtils.PRIME * result + partitionInfos.hashCode();
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
    if( aObject.getClass() != IS5SequenceRemoveInfo.class ) {
      return false;
    }
    IS5SequenceRemoveInfo other = (IS5SequenceRemoveInfo)aObject;
    if( !tableName.equals( other.tableName() ) ) {
      return false;
    }
    if( !gwids.equals( other.gwids() ) ) {
      return false;
    }
    if( !partitionInfos.equals( other.partitionInfos() ) ) {
      return false;
    }
    return true;
  }
}
