package org.toxsoft.uskat.s5.server.backend.addons;

import javax.naming.Context;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.sessions.pas.IS5MessageProcessor;

/**
 * Построитель расширения бекенда предоставляемого сервером
 *
 * @author mvk
 */
public interface IS5BackendAddonCreator
    extends IStridable {

  /**
   * Возвращает построителя службы предоставляемой расширением бекенда
   *
   * @return {@link ISkServiceCreator} построитель службы расширения бекенда.
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkServiceCreator<? extends AbstractSkService> serviceCreator();

  /**
   * Создает доступ к управлению сессией расширения бекенда
   *
   * @param aContext {@link Context} контекст имен сервера
   * @return {@link IS5BackendAddonSessionControl} доступ к управлению сессией. null: расширение не имеет удаленного
   *         доступа
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5BackendAddonSessionControl createSessionControl( Context aContext );

  /**
   * Создает локальный доступ к расширению бекенда
   *
   * @param aOwner {@link IS5BackendLocal} локальный доступ к серверу
   * @return {@link IS5BackendAddonLocal} локальный доступ к расширению бекенда. null: расширение не поддерживает
   *         локальный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5BackendAddonLocal createLocal( IS5BackendLocal aOwner );

  /**
   * Создает удаленный доступ к расширению бекенда
   *
   * @param aOwner {@link IS5BackendRemote} удаленный доступ к серверу
   * @return {@link IS5BackendAddonRemote} удаленный доступ к расширению бекенда. null: расширение не поддерживает
   *         удаленный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5BackendAddonRemote createRemote( IS5BackendRemote aOwner );

  /**
   * Возвращает процессор сообщений бекенда.
   * <p>
   * Процессоры сообщений могут(!) использоваться для "накопления" данных перед их фактической отправкой фронтенду.
   * Например, передача значений текущих данных может осуществляться не чаще одного раза в секунду (параметр настройки).
   *
   * @return {@link IS5MessageProcessor} процессор сообщений {@link IS5MessageProcessor#NULL}: нет процессора
   */
  IS5MessageProcessor messageProcessor();

  /**
   * Возвращает список синглетонов поддержки {@link IS5BackendSupportSingleton} необходимых для работы расширения
   * бекенда
   *
   * @return {@link IStringList} список идентификаторов {@link IS5BackendSupportSingleton#id()} синглетонов
   */
  IStringList supportSingletonIds();

}
