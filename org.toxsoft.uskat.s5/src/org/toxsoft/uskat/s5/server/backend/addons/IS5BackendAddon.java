package org.toxsoft.uskat.s5.server.backend.addons;

import javax.naming.Context;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;

import ru.uskat.core.api.ISkService;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Расширение бекенда предоставляемое сервером
 *
 * @author mvk
 */
public interface IS5BackendAddon
    extends IStridable {

  /**
   * Создает и возвращает службы поддерживаемые расширением
   *
   * @param aCoreApi {@link IDevCoreApi} - API ядра
   * @return {@link IStringMap}&lt;{@link AbstractSkService}&gt; карта служб.
   *         <p>
   *         Ключ: идентификатор службы {@link ISkService#serviceId()};<br>
   *         Значение: реализация созданной службы.
   * @throws TsNullArgumentRtException аргумент = null
   */
  IStringMap<AbstractSkService> createServices( IDevCoreApi aCoreApi );

  /**
   * Создает сессию для удаленного доступа к расширению бекенда
   *
   * @param aContext {@link Context} контекст имен сервера
   * @return {@link IS5BackendAddonSession} сессия для удаленного доступа к расширению бекенда. null: расширение не
   *         имеет удаленного доступа
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5BackendAddonSession createSession( Context aContext );

  /**
   * Создает локального клиента расширения бекенда
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения
   * @return {@link S5BackendAddonLocal} локальный клиент расширения. null: расширение не поддерживает локальный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5BackendAddonLocal createLocalClient( ITsContextRo aArgs );

  /**
   * Создает удаленного клиента расширения бекенда
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения
   * @return {@link S5BackendAddonRemote} удаленный клиент расширения. null: расширение не поддерживает удаленный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5BackendAddonRemote<?> createRemoteClient( ITsContextRo aArgs );

  /**
   * Возвращает список синглетонов поддержки {@link IS5BackendSupportSingleton} необходимых для работы расширения
   * бекенда
   *
   * @return {@link IStringList} список идентификаторов {@link IS5BackendSupportSingleton#id()} синглетонов
   */
  IStringList supportSingletonIds();
}
