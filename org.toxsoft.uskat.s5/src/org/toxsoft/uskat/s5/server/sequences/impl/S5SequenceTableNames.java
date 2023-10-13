package org.toxsoft.uskat.s5.server.sequences.impl;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceTableNames;

/**
 * Реалиация {@link IS5SequenceTableNames}.
 *
 * @author mvk
 */
public final class S5SequenceTableNames
    implements IS5SequenceTableNames {

  private final String blockTableName;
  private final String blobTableName;

  /**
   * Конструктор
   *
   * @param aBlockTableName String имя таблиц хранения блоков
   * @param aBlobTableName String имя таблицы хранения blob
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SequenceTableNames( String aBlockTableName, String aBlobTableName ) {
    TsNullArgumentRtException.checkNulls( aBlockTableName, aBlobTableName );
    blockTableName = aBlockTableName;
    blobTableName = aBlobTableName;
  }

  // ------------------------------------------------------------------------------------
  // IS5SequenceTableNames
  //
  @Override
  public String blockTableName() {
    return blockTableName;
  }

  @Override
  public String blobTableName() {
    return blobTableName;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return String.format( "%s { %s, %s }", getClass().getSimpleName(), blockTableName, blobTableName ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + blockTableName.hashCode();
    result = TsLibUtils.PRIME * result + blobTableName.hashCode();
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
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    IS5SequenceTableNames other = (IS5SequenceTableNames)aObject;
    if( !blockTableName.equals( other.blockTableName() ) ) {
      return false;
    }
    if( !blobTableName.equals( other.blobTableName() ) ) {
      return false;
    }
    return true;
  }

}
