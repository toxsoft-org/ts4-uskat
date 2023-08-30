package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
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

  private final String  tableName;
  private final Gwid    gwid;
  private ITimeInterval removeInterval     = ITimeInterval.NULL;
  private ITimeInterval firstBlockInterval = ITimeInterval.NULL;

  /**
   * Конструктор
   *
   * @param aTableName String имя таблицы хранения данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  public S5SequenceRemoveInfo( String aTableName, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aTableName, aGwid );
    tableName = aTableName;
    gwid = aGwid;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Устанавливает интервал в котором находятся удаляемые значения
   *
   * @param aRemoveInterval {@link ITimeInterval} интервал удаляемых значений, {@link ITimeInterval#NULL}: пустой
   *          интервал.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setRemoveInterval( ITimeInterval aRemoveInterval ) {
    TsNullArgumentRtException.checkNull( aRemoveInterval );
    removeInterval = aRemoveInterval;
  }

  /**
   * Устанавливает интервал первого блока значений хранимого в БД.
   *
   * @param aInterval {@link ITimeInterval} интервал. {@link ITimeInterval#NULL}: нет блока (нет значений).
   */
  public void setFirstBlockInterval( ITimeInterval aInterval ) {
    firstBlockInterval = aInterval;
  }

  // ------------------------------------------------------------------------------------
  // /Реализация IS5SequenceRemoveInfo
  //
  @Override
  public String tableName() {
    return tableName;
  }

  @Override
  public Gwid gwid() {
    return gwid;
  }

  @Override
  public ITimeInterval removeInterval() {
    return removeInterval;
  }

  @Override
  public ITimeInterval firstBlockInterval() {
    return firstBlockInterval;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Object
  //
  @Override
  public String toString() {
    return tableName + ':' + gwid.toString() + '[' + removeInterval + ',' + firstBlockInterval + ']';
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + tableName.hashCode();
    result = TsLibUtils.PRIME * result + gwid.hashCode();
    result = TsLibUtils.PRIME * result + removeInterval.hashCode();
    result = TsLibUtils.PRIME * result + firstBlockInterval.hashCode();
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
    if( !gwid.equals( other.gwid() ) ) {
      return false;
    }
    if( !removeInterval.equals( other.removeInterval() ) ) {
      return false;
    }
    if( !firstBlockInterval.equals( other.firstBlockInterval() ) ) {
      return false;
    }
    return true;
  }
}
