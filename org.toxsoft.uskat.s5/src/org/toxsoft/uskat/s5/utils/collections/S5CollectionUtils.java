package org.toxsoft.uskat.s5.utils.collections;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Вспомогательные методы для работы с коллекциями
 *
 * @author mvk
 */
public class S5CollectionUtils {

  /**
   * Возвращает текстовое представление массива элементов
   *
   * @param aItems Object[] массив элементов
   * @return String текстовое представление массива
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String itemsToString( Object aItems[] ) {
    TsNullArgumentRtException.checkNull( aItems );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aItems.length; index < n; index++ ) {
      retValue.append( aItems[index] );
      if( index + 1 < n ) {
        retValue.append( CHAR_ITEM_SEPARATOR );
        retValue.append( CHAR_SPACE );
      }
    }
    return retValue.toString();
  }

  /**
   * Возвращает текстовое представление списка элементов
   *
   * @param aItems {@link IList} список элементов
   * @return String текстовое представление списка
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String itemsToString( IList<?> aItems ) {
    TsNullArgumentRtException.checkNulls( aItems );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      retValue.append( aItems.get( index ) );
      if( index + 1 < n ) {
        retValue.append( CHAR_ITEM_SEPARATOR );
        retValue.append( CHAR_SPACE );
      }
    }
    return retValue.toString();
  }

  /**
   * Возвращает текстовое представление списка элементов
   *
   * @param aItems {@link IStridablesList} список элементов
   * @return String текстовое представление массива
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static String itemsToString( IStridablesList<?> aItems ) {
    TsNullArgumentRtException.checkNulls( aItems );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      retValue.append( aItems.get( index ).id() );
      if( index + 1 < n ) {
        retValue.append( CHAR_ITEM_SEPARATOR );
        retValue.append( CHAR_SPACE );
      }
    }
    return retValue.toString();
  }
}
