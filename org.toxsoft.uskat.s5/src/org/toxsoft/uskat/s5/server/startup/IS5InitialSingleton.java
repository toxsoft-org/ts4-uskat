package org.toxsoft.uskat.s5.server.startup;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Локальный интерфейс синглтона, стартующего первым на сервере.
 * <p>
 * Этот интерфейс нужен только для реализации служб сервера. Для клиентов он не нужен.
 *
 * @author goga
 */
@Local
public interface IS5InitialSingleton {

  /**
   * Загружает из БД конфигурацию службы.
   * <p>
   * Если для данной службы в БД нет конфигурации, возвращает null.
   *
   * @param aServiceId String - идентификатор (ИД-путь) службы
   * @return {@link IOptionSet} - загруженная конфигуарция или null
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  IOptionSet loadServiceConfig( String aServiceId );

  /**
   * Сохраняет в БД конфигурацию службы.
   * <p>
   * Существующая конфигурация обновляется, несуществующая - создается.
   *
   * @param aServiceId String - идентификатор(ИД-путь) службы
   * @param aServiceConfig {@link IOptionSet} - конфигурация службы
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор не ИД-путь
   */
  void saveServiceConfig( String aServiceId, IOptionSet aServiceConfig );

}
