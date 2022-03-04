package org.toxsoft.uskat.s5.utils.indexes.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.utils.indexes.IIndexEdit;

/**
 * Абстрактная реализация бинарного индекса
 *
 * @author mvk
 * @param <K> тип ключа
 * @param <V> тип значения
 */
abstract class S5AbstractIndex<K extends Comparable<K>, V>
    implements IIndexEdit<K, V>, Serializable {

  private static final long serialVersionUID = 157157L;

  protected final boolean unique;
  protected int           watermark;

  /**
   * Конструктор
   *
   * @param aUnique <b>true</b> индекс с уникальными ключами;<b>false</b> индекс с НЕуникальными ключами
   * @param aWatermark int количество элементов в индексе.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5AbstractIndex( boolean aUnique, int aWatermark ) {
    unique = aUnique;
    watermark = aWatermark;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IIndex
  //
  @Override
  public boolean unique() {
    return unique;
  }

  @Override
  public int watermark() {
    return watermark;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IIndexEdit
  //
  @Override
  public void clear() {
    watermark = 0;
  }

  // @Override
  @Override
  public void add( IList<K> aKeys, IList<V> aValues ) {
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.size(), aValues.size() );
    for( int index = 0, n = aKeys.size(); index < n; index++ ) {
      add( aKeys.get( index ), aValues.get( index ) );
    }
  }

  @Override
  public void add( K[] aKeys, V[] aValues ) {
    TsNullArgumentRtException.checkNulls( aKeys, aValues );
    checkSize( aKeys.length, aValues.length );
    for( int index = 0, n = aKeys.length; index < n; index++ ) {
      add( aKeys[index], aValues[index] );
    }
  }

  @Override
  public void setWatermark( int aWatermark ) {
    TsIllegalArgumentRtException.checkTrue( aWatermark < 0 || aWatermark > capacity() );
    watermark = aWatermark;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  @SuppressWarnings( "nls" )
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = capacity(); index < n; index++ ) {
      sb.append( "index = " );
      sb.append( index );
      sb.append( ", key = " );
      sb.append( key( index ) );
      sb.append( ", value = " );
      sb.append( value( index ) );
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Проверяет размерность массивов ключей и их значений
   *
   * @param aKeysSize int размерность ключей
   * @param aValuesSize int размерность значений
   */
  protected static void checkSize( int aKeysSize, int aValuesSize ) {
    if( aKeysSize != aValuesSize ) {
      Long keysL = Long.valueOf( aKeysSize );
      Long valuesL = Long.valueOf( aValuesSize );
      throw new TsIllegalArgumentRtException( MSG_ERR_INDEX_VALUES, keysL, valuesL );
    }
  }

}
