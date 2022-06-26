package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.addons.IS5Resources.*;

import javax.naming.Context;
import javax.naming.NamingException;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Абстрактная реализация {@link IS5BackendAddonCreator}
 *
 * @author mvk
 */
public abstract class S5AbstractBackendAddonCreator
    extends Stridable
    implements IS5BackendAddonCreator {

  /**
   * Конструктор
   *
   * @param aInfo {@link IStridable} информация об расширении
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5AbstractBackendAddonCreator( IStridable aInfo ) {
    super( aInfo );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonCreator
  //
  @Override
  public final ISkServiceCreator<? extends AbstractSkService> serviceCreator() {
    return doGetServiceCreator();
  }

  @Override
  public final IS5BackendAddonSessionControl createSessionControl( Context aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> sessionClasses =
        doGetSessionClasses();
    if( sessionClasses == null ) {
      return null;
    }
    String beanIface = sessionClasses.left().getName();
    String beanName = sessionClasses.right().getSimpleName();
    String jndi = String.format( BACKEND_ADDON_JNDI, beanName, beanIface );
    try {
      return (IS5BackendAddonSessionControl)aContext.lookup( jndi );
    }
    catch( NamingException e ) {
      // Ошибка поиска сессии
      throw new TsItemNotFoundRtException( e, ERR_BA_SESSION_NOT_FOUND, beanName, beanIface, cause( e ) );
    }
  }

  @Override
  public final IS5BackendAddonLocal createLocal( IS5BackendLocal aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    return doCreateLocal( aOwner );
  }

  @Override
  public final IS5BackendAddonRemote createRemote( IS5BackendRemote aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    return doCreateRemote( aOwner );
  }

  @Override
  public final IStringList supportSingletonIds() {
    return doSupportSingletonIds();
  }

  // ------------------------------------------------------------------------------------
  // Абстрактные и шаблонные методы
  //
  /**
   * Возвращает построителя службы предоставляемой расширением бекенда
   *
   * @return {@link ISkServiceCreator} построитель службы расширения бекенда.
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected abstract ISkServiceCreator<? extends AbstractSkService> doGetServiceCreator();

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
  protected Pair<Class<? extends IS5BackendAddonSession>, Class<? extends IS5BackendAddonSession>> doGetSessionClasses() {
    return null;
  }

  /**
   * Создает локальный доступ к расширению бекенда
   *
   * @param aOwner {@link IS5BackendLocal} локальный доступ к серверу
   * @return {@link IS5BackendAddonLocal} локальный доступ к расширению. null: расширение не поддерживает локальный
   *         доступ
   */
  protected IS5BackendAddonLocal doCreateLocal( IS5BackendLocal aOwner ) {
    return null;
  }

  /**
   * Создает удаленный доступ к расширению бекенда
   *
   * @param aOwner {@link IS5BackendLocal} удаленный доступ к серверу
   * @return {@link IS5BackendAddonLocal} удаленный доступ к расширения бекенда. null: расширение не поддерживает
   *         удаленный доступ
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected IS5BackendAddonRemote doCreateRemote( IS5BackendRemote aOwner ) {
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
