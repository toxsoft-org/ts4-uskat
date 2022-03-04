package org.toxsoft.uskat.s5.utils.threads.impl;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Вспомогательные методы для работы
 *
 * @author mvk
 */
public class S5ThreadUtils {

  /**
   * Возвращает потокобезопасный список из исходного списка
   *
   * @param <T> тип значений списка
   * @param aSource {@link IList} исходный список
   * @param aSourceLock {@link S5Lockable} блокировка доступа к исходному списку
   * @return {@link IList} потокобезопасный список
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static <T> IList<T> threadSafeList( IList<T> aSource, S5Lockable aSourceLock ) {
    TsNullArgumentRtException.checkNulls( aSource, aSourceLock );
    lockRead( aSourceLock );
    try {
      IListEdit<T> retValue = new ElemArrayList<>( aSource.size() );
      retValue.addAll( aSource );
      return retValue;
    }
    finally {
      unlockRead( aSourceLock );
    }
  }

  /**
   * Возвращает потокобезопасную карту из исходной карты
   *
   * @param <K> тип ключа карты
   * @param <V> тип значения карты
   * @param aSource {@link IList} исходная карта
   * @param aSourceLock {@link S5Lockable} блокировка доступа к исходной карте
   * @return {@link IMap} потокобезопасная карта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static <K, V> IMap<K, V> threadSafeMap( IMap<K, V> aSource, S5Lockable aSourceLock ) {
    TsNullArgumentRtException.checkNulls( aSource, aSourceLock );
    lockRead( aSourceLock );
    try {
      IMapEdit<K, V> retValue = new ElemMap<>();
      retValue.putAll( aSource );
      return retValue;
    }
    finally {
      unlockRead( aSourceLock );
    }
  }

  /**
   * Возвращает потокобезопасную карту из исходной карты cо строковыми ключами
   *
   * @param <V> тип значения карты
   * @param aSource {@link IList} исходная карта
   * @param aSourceLock {@link S5Lockable} блокировка доступа к исходной карте
   * @return {@link IStringMap} потокобезопасная карта
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static <V> IStringMap<V> threadSafeMap( IStringMap<V> aSource, S5Lockable aSourceLock ) {
    TsNullArgumentRtException.checkNulls( aSource, aSourceLock );
    lockRead( aSourceLock );
    try {
      IStringMapEdit<V> retValue = new StringMap<>( aSource.size() );
      retValue.putAll( aSource );
      return retValue;
    }
    finally {
      unlockRead( aSourceLock );
    }
  }
}
