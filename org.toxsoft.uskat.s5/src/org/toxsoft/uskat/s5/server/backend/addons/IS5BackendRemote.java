package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitData;

/**
 * Удаленный доступ к серверу
 *
 * @author mvk
 */
public interface IS5BackendRemote
    extends IS5Backend {

  /**
   * Возвращает данные для инициализации сессии на сервере
   *
   * @return {@link S5SessionInitData} данные инициализации сессии
   */
  S5SessionInitData sessionInitData();

  /**
   * Возвращает результат подключения к серверу
   *
   * @return {@link IS5SessionInitResult} результат инициализации сессии
   */
  IS5SessionInitResult sessionInitResult();

  /**
   * Возвращает сессию расширения бекенда на сервере
   *
   * @param aAddonId String - идентификатор (ИД-путь) расширения
   * @param aAddonSessionClass - класс сессии расширения бекенда
   * @return SESSION удаленный доступ к расширению
   * @param <SESSION> тип возвращаемого доступа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException не найден удаленный доступ
   * @throws TsIllegalArgumentRtException ошибка приведения типа доступа к указанному интерфейсу
   */
  <SESSION extends IS5BackendAddonSession> SESSION getBaSession( String aAddonId, Class<SESSION> aAddonSessionClass );

}
