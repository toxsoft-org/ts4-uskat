package org.toxsoft.uskat.s5.server.interceptors;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Интерфейс, показывающий, что объект способен предоставлять операции над сущностями системы для перехвата
 *
 * @author mvk
 * @param <T> тип интерспетора(перехватчика)
 */
public interface IS5Interceptable<T extends IS5Interceptor> {

  /**
   * Добавляет перехватчика операций над сущностями системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5Interceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void add( T aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над сущностями системы.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5Interceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void remove( T aInterceptor );
}
