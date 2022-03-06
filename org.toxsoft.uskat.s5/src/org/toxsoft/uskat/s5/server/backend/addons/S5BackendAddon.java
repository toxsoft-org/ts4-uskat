package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.IS5Resources.*;

import javax.naming.Context;
import javax.naming.NamingException;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.client.remote.S5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;

import ru.uskat.core.api.ISkService;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Абстрактная реализация {@link IS5BackendAddon}
 *
 * @author mvk
 */
public abstract class S5BackendAddon
    extends Stridable
    implements IS5BackendAddon {

  /**
   * Конструктор
   *
   * @param aAddonId String идентификатор расширения бекенда
   * @param aAddonName String имя расширения бекенда
   * @param aAddonDescr String описание расширения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5BackendAddon( String aAddonId, String aAddonName, String aAddonDescr ) {
    super( aAddonId, aAddonDescr, aAddonName );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddon
  //
  @Override
  public final IStringMap<AbstractSkService> createServices( IDevCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    IStringMap<AbstractSkService> retValue = doCreateServices( aCoreApi );
    // TODO: 2020-07-02 mvk ???
    // for( String serviceId : retValue.ids() ) {
    // if( aCoreApi.services().findByKey( serviceId ) == null ) {
    // aCoreApi.addService( new ISkServiceCreator<>() {
    //
    // @Override
    // public AbstractSkService createService( IDevCoreApi aCreatorCoreApi ) {
    // return retValue.get( serviceId );
    // }
    // } );
    // }
    // }
    return retValue;
  }

  @Override
  public final IS5BackendAddonSession createSession( Context aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    Pair<Class<? extends IS5BackendAddonSession>, Class<? extends S5BackendAddonSession>> sessionClasses =
        doGetSessionClasses();
    if( sessionClasses == null ) {
      return null;
    }
    String beanIface = sessionClasses.left().getName();
    String beanName = sessionClasses.right().getSimpleName();
    String jndi = String.format( BACKEND_ADDON_JNDI, beanName, beanIface );
    try {
      return (IS5BackendAddonSession)aContext.lookup( jndi );
    }
    catch( NamingException e ) {
      // Ошибка поиска сессии
      throw new TsItemNotFoundRtException( e, ERR_SESSION_NOT_FOUND, beanName, beanIface, cause( e ) );
    }
  }

  @Override
  public final S5BackendAddonLocal createLocalClient( ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    return doCreateLocalClient( aArgs );
  }

  @Override
  public final S5BackendAddonRemote<?> createRemoteClient( ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    return doCreateRemoteClient( aArgs );
  }

  @Override
  public final IStringList supportSingletonIds() {
    return doSupportSingletonIds();
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные методы
  //
  /**
   * Создает и возвращает службы поддерживаемые расширением
   *
   * @param aCoreApi {@link IDevCoreApi} - API ядра
   * @return {@link IStringMap}&lt;{@link AbstractSkService}&gt; карта служб.
   *         <p>
   *         Ключ: идентификатор службы {@link ISkService#serviceId()};<br>
   *         Значение: реализация созданной службы.
   */
  protected IStringMap<AbstractSkService> doCreateServices( IDevCoreApi aCoreApi ) {
    return IStringMap.EMPTY;
  }

  /**
   * Возвращает классы создания сессии для удаленного доступа к расширению бекенда
   *
   * @return {@link Pair} пара классов сессии. null: нет удаленного доступа к расширению.
   *         <p>
   *         {@link Pair#left()}: класс интерфейса сессии;<br>
   *         {@link Pair#right()}: класс реализации сессии.
   */
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends S5BackendAddonSession>> doGetSessionClasses() {
    return null;
  }

  /**
   * Создает локального клиента расширения бекенда
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения
   * @return {@link S5BackendAddonLocal} локальный клиент расширения. null: расширение не поддерживает локальный доступ
   */
  protected S5BackendAddonLocal doCreateLocalClient( ITsContextRo aArgs ) {
    return null;
  }

  /**
   * Создает удаленного клиента расширения бекенда
   *
   * @param aArgs {@link ITsContextRo} параметры создания соединения
   * @return {@link S5BackendAddonLocal} удаленный клиент расширения. null: расширение не поддерживает удаленный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5BackendAddonRemote<?> doCreateRemoteClient( ITsContextRo aArgs ) {
    return null;
  }

  /**
   * Возвращает список синглетонов поддержки {@link IS5BackendSupportSingleton} необходимых для работы расширения
   * бекенда
   *
   * @return {@link IStringList} список идентификаторов {@link IS5BackendSupportSingleton#id()} синглетонов
   */
  protected IStringList doSupportSingletonIds() {
    return IStringList.EMPTY;
  }
}
