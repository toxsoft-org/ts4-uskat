package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

/**
 * Хранение промежуточных значений курсора
 *
 * @param <T> тип значений курсора
 * @author mvk
 */
class S5CursorHolder<T extends ITemporal<?>> {

  private final IS5SequenceCursor<?> cursor;
  private ITemporal<?>               value;

  S5CursorHolder( IS5SequenceCursor<?> aCursor ) {
    TsNullArgumentRtException.checkNull( aCursor );
    cursor = aCursor;
    next();
  }

  ITemporal<?> value() {
    return value;
  }

  void next() {
    value = null;
    if( cursor.hasNextValue() ) {
      value = cursor.nextValue();
    }
  }

  @SuppressWarnings( "unchecked" )
  static <T extends ITemporal<?>> T nextValueOrNull( ElemArrayList<S5CursorHolder<T>> aCursorHolders ) {
    TsNullArgumentRtException.checkNull( aCursorHolders );
    int foundIndex = -1;
    T foundValue = null;
    for( int index = 0, n = aCursorHolders.size(); index < n; index++ ) {
      S5CursorHolder<T> holder = aCursorHolders.get( index );
      T value = (T)holder.value();
      if( value == null ) {
        continue;
      }
      if( foundValue != null && foundValue.timestamp() < value.timestamp() ) {
        continue;
      }
      foundValue = value;
      foundIndex = index;
    }
    if( foundValue != null ) {
      aCursorHolders.get( foundIndex ).next();
    }
    return foundValue;
  }
}
