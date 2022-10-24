package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.coll.primtypes.impl.LongArrayList;

/**
 * Список long чисел с возможностью указания начальной мощности
 *
 * @author mvk
 */
public class S5LongArrayList
    extends LongArrayList {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   *
   * @param aInitialCapacity int начальная мощность списка
   */
  public S5LongArrayList( int aInitialCapacity ) {
    ensureCapacity( aInitialCapacity );
  }
}
