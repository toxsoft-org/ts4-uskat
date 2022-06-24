package org.toxsoft.uskat.s5.server.sessions.init;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;

/**
 * Результат инициализации сессии
 *
 * @author mvk
 */
public interface IS5SessionInitResult {

  /**
   * Возвращает карту сессий расширений бекенда s5-сервера
   *
   * @return {@link IStringMap}&lt;{@link IS5BackendAddonSession}&gt; карта доступа. <br>
   *         Ключ: идентификатор расширения {@link IS5BackendAddonSession#id()};<br>
   *         Значение: сессия расширения бекенда.
   */
  IStringMap<IS5BackendAddonSession> baSessions();

  /**
   * Возвращает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aAddonDataType Java-тип данных расширения
   * @return {@link IS5SessionAddonInitResult} данные расширения
   * @param <T> тип данных расширения. null: данные не найдены
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные не существуют
   * @throws ClassCastException данные расширение есть, но они не запрошенного типа
   */
  <T extends IS5SessionAddonInitResult> T getBaData( String aAddonId, Class<T> aAddonDataType );
}
