package org.toxsoft.uskat.s5.server.sessions.init;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;

/**
 * Результат инициализации сессии
 *
 * @author mvk
 */
public interface IS5SessionInitResult {

  /**
   * Возвращает описания всех типов зарегистрированных в системе на момент подключения к серверу
   *
   * @return {@link IStridablesList}&lt;{@link IDpuSdTypeInfo}&gt; описания типов
   */
  IStridablesList<IDpuSdTypeInfo> typeInfos();

  /**
   * Устанавливает описания всех классов зарегистрированных в системе на момент подключения к серверу
   *
   * @return {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; описания классов
   */
  IStridablesList<IDpuSdClassInfo> classInfos();

  /**
   * Возвращает карту удаленного доступа к расширениям backend s5-сервера
   *
   * @return {@link IStringMap}&lt;{@link IS5BackendAddonRemote}&gt; карта доступа. <br>
   *         Ключ: идентификатор службы {@link IS5BackendAddonRemote#id()};<br>
   *         Значение: удаленный доступ к расширению backend.
   */
  IStringMap<IS5BackendAddonRemote> addons();

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
  <T extends IS5SessionAddonInitResult> T getAddonData( String aAddonId, Class<T> aAddonDataType );
}
