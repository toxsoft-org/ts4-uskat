package org.toxsoft.uskat.s5.server.transactions;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Локальный интерфейс синглтона обнаружения транзакций сервера s5
 *
 * @author mvk
 */
@Local
public interface IS5TransactionDetectorSingleton {

  /**
   * Константа определяющая, что у метода нет параметров
   */
  Object[] NO_METHOD_PARAMS = new Objects[0];

  /**
   * Обработка события: перехват вызова метода
   *
   * @param aOwner Object - собственник (компонент) транзакции
   * @param aMethod {@link Method} - метод собственника открывший транзакцию
   * @param aParams Object[] - параметры вызова
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void onCallBusinessMethod( Object aOwner, Method aMethod, Object[] aParams );
}
