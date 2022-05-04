package org.toxsoft.uskat.s5.server.frontend;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.impl.S5EventSupport;

import ru.uskat.common.dpu.rt.events.SkEvent;

/**
 * Данные frontend
 *
 * @author mvk
 */
public interface IS5FrontendData {

  /**
   * Возвращает настройки событий получаемых клиентом
   *
   * @return {@link S5EventSupport} вспомогательный класс обработки событий {@link SkEvent}
   */
  S5EventSupport events();

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
   * @throws TsIllegalArgumentRtException данные не существуют
   * @throws ClassCastException данные расширение есть, но они не запрошенного типа
   */
  <T extends IS5FrontendAddonData> T getAddonData( String aAddonId, Class<T> aAddonDataType );

  /**
   * Устанавливает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aData {@link IS5FrontendAddonData} данные фронтенда расширения бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные должны поддерживать сериализацию
   * @throws TsItemAlreadyExistsRtException данные расширения уже установлены
   */
  void setAddonData( String aAddonId, IS5FrontendAddonData aData );
}
