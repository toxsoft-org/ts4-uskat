package org.toxsoft.uskat.s5.utils.collections;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Карта атомарных значений
 *
 * @author mvk
 */
public interface IAtomicValueMap
    extends IStringMap<IAtomicValue> {

  /**
   * Пустая карта
   */
  IAtomicValueMap NULL = new InternalNullAtomicValueMap();
}

/**
 * Always empty uneditable (immutable) atomic values map.
 */
class InternalNullAtomicValueMap
    implements IAtomicValueMap, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IAtomicValueMap#EMPTY}.
   *
   * @return Object объект {@link IAtomicValueMap#EMPTY}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IAtomicValueMap.NULL;
  }

  // ------------------------------------------------------------------------------------
  // ITsCountableCollection
  //

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  // ------------------------------------------------------------------------------------
  // IAtomicValueMap
  //
  @Override
  public IStringList keys() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public boolean hasKey( String aKey ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IAtomicValue findByKey( String aKey ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IList<IAtomicValue> values() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public boolean hasElem( IAtomicValue aElem ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IAtomicValue[] toArray( IAtomicValue[] aSrcArray ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Object[] toArray() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public Iterator<IAtomicValue> iterator() {
    throw new TsNullObjectErrorRtException();
  }
}
