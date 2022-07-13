package org.toxsoft.uskat.s5.server.frontend;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Данные frontend
 *
 * @author mvk
 */
public interface IS5FrontendData {

  /**
   * Возвращает список идентификаторов инициализируемых расширений бекендов
   *
   * @return {@link IStringList} список идентификаторов расширений
   */
  IStringList addonIds();

  /**
   * Возвращает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aAddonDataType Java-тип данных расширения
   * @return T данные расширения. null: данные не существуют
   * @param <T> тип данных расширения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws ClassCastException данные расширение есть, но они не запрошенного типа
   */
  <T extends IS5BackendAddonData> T findBackendAddonData( String aAddonId, Class<T> aAddonDataType );

  /**
   * Устанавливает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aData {@link IS5BackendAddonData} данные фронтенда расширения бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные должны поддерживать сериализацию
   * @throws TsItemAlreadyExistsRtException данные расширения уже установлены
   */
  void setBackendAddonData( String aAddonId, IS5BackendAddonData aData );
}
