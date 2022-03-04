package org.toxsoft.uskat.s5.server.interceptors;

import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.coll.primtypes.IIntListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntArrayList;
import org.toxsoft.core.tslib.coll.synch.SynchronizedListEdit;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Механизм поддержки
 *
 * @author mvk
 * @param <T> тип интерспетора(перехватчика)
 */
public class S5InterceptorSupport<T extends IS5Interceptor>
    implements IS5Interceptable<T> {

  /**
   * Блокировка доступа к классу
   */
  private final S5Lockable lock = new S5Lockable();

  /**
   * Интерсепторы операций проводимых над сущностями системы. false: повторы запрещены
   * <p>
   * Первыми в списке идут интерсеторы с высшим приоритетом, последними с низшим
   */
  private final IListEdit<T> interceptors =
      new SynchronizedListEdit<>( new ElemArrayList<>( false ), nativeLock( lock ) );

  /**
   * Список приоритетов интерсепторов. Индекс приоритета = индексу интерспетора {@link #interceptors}.
   * <p>
   * Чем меньше значение, тем выше приоритет.
   */
  private final IIntListEdit priorities = new IntArrayList();

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает список интерспеторов
   * <p>
   * Первыми в списке идут интерсеторы с высшим приоритетом, последними с низшим
   *
   * @return {@link IList}&lt;{@link IS5Interceptor}&gt; список интерсепторов в порядке убывания приоритета
   */
  public final IList<T> interceptors() {
    lockRead( lock );
    try {
      return new ElemArrayList<>( interceptors );
    }
    finally {
      unlockRead( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5Interceptable
  //
  @Override
  public final void add( T aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    lockWrite( lock );
    try {
      // Удаление (если уже есть)
      remove( aInterceptor );
      // Добавление (возможно с новым приоритетом)
      int index = getIndexByPriority( priorities, aPriority );
      interceptors.insert( getIndexByPriority( priorities, aPriority ), aInterceptor );
      priorities.insert( index, aPriority );
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public final void remove( T aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    lockWrite( lock );
    try {
      int index = interceptors.indexOf( aInterceptor );
      if( index < 0 ) {
        return;
      }
      interceptors.removeByIndex( index );
      priorities.removeByIndex( index );
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Возвращает индекс по которому должен быть размещен интерспетор с указанным приоритетом
   *
   * @param aPriorities {@link IIntList} список приоритетов интерсепторов
   * @param aPriority int приоритет
   * @return int индекс для интерсептора
   */
  private static int getIndexByPriority( IIntListEdit aPriorities, int aPriority ) {
    TsNullArgumentRtException.checkNull( aPriorities );
    for( int index = 0, n = aPriorities.size(); index < n; index++ ) {
      if( aPriorities.getValue( index ) >= aPriority ) {
        return index;
      }
    }
    return aPriorities.size();
  }
}
