package org.toxsoft.uskat.s5.utils.collections;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;

/**
 * Реализация {@link IAtomicValueMap}
 *
 * @author mvk
 */
public class AtomicValueMap
    extends StringMap<IAtomicValue>
    implements IAtomicValueMap {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор, создающий карту с емкостью хеш-таблицы по умолчанию.
   */
  public AtomicValueMap() {
    super();
  }

  /**
   * Конструктор копирования.
   *
   * @param aSrc {@link IAtomicValueMap} - карта с добавляемыми элементами
   */
  public AtomicValueMap( IAtomicValueMap aSrc ) {
    super();
    putAll( aSrc );
  }

}
