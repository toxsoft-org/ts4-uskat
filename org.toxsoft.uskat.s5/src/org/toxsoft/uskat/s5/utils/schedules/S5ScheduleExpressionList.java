package org.toxsoft.uskat.s5.utils.schedules;

import org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Список расписания календарей
 *
 * @author mvk
 */
public class S5ScheduleExpressionList
    extends ElemArrayList<IScheduleExpression> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает пустой список.
   */
  public S5ScheduleExpressionList() {
    super();
  }

  /**
   * Создает список с начальным содержимым набора или массива aElems.
   *
   * @param aElems E... - элементы списка (набор или массив)
   * @throws TsNullArgumentRtException любой элемент = null
   */
  public S5ScheduleExpressionList( IScheduleExpression... aElems ) {
    super( aElems );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if( size() > 1 ) {
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    for( int index = 0, n = size(); index < n; index++ ) {
      sb.append( get( index ).toString() );
      if( index + 1 < n ) {
        sb.append( IStrioHardConstants.CHAR_EOL );
      }
    }
    return sb.toString();
  }

}
