package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.*;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sessions.*;

/**
 * Интерфейс синглетона реестра поддержки бекендов {@link IS5BackendSupportSingleton}
 *
 * @author mvk
 */
@Local
public interface IS5BackendSupportRegistry {

  /**
   * Установить менеджера сессий
   *
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException менеджер сессий уже установлен
   */
  void setSessionManager( IS5SessionManager aSessionManager );

  /**
   * Возвращает список доступных синглетонов поддержки бекенда на текущий момент
   *
   * @return {@link IStringList} список доступных синглетонов поддержки бекенда.
   */
  IStringList listSupportIds();

  /**
   * Добавляет в бекенд синглетон поддержки
   * <p>
   * Метод передает идентификатор синглетона поддержки, чтобы избежать рекурсивного обращения при добавлении синглетона
   * в бекенд
   *
   * @param aSupportId String идентификатор синглетона поддержки ({@link IS5BackendSupportSingleton#id()}).
   * @param aSupportInterface {@link IS5BackendSupportSingleton} сиглетон поддержки
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException синглетон с идентификатором {@link IS5BackendSupportSingleton#id()} уже
   *           зарегистрирован
   */
  void add( String aSupportId, IS5BackendSupportSingleton aSupportInterface );

  /**
   * Удаляет из бекенда синглетон поддержки
   * <p>
   * Если поддержка не зарегистрирована, то ничего не делает
   *
   * @param aSupportId String - идентификатор (ИД-путь) синглетона поддержки {@link IS5BackendSupportSingleton#id()}
   * @throws TsNullArgumentRtException аргумент = null
   */
  void remove( String aSupportId );

  /**
   * Возвращает синглетон поддержки бекенда, если таковой существует.
   *
   * @param aSupportId String - идентификатор (ИД-путь) синглетона поддержки {@link IS5BackendSupportSingleton#id()}
   * @param aSupportInterface - Java-тип интерфейс поддержки бекенда
   * @return &lt;T&gt; - поддержка бекенда или <code>null</code>, если нет такой поддержки не существует
   * @param <T> - тип интерфейса поддержки бекенда
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws ClassCastException поддержка есть, но она не запрошенного класса
   */
  <T extends IS5BackendSupportSingleton> T get( String aSupportId, Class<T> aSupportInterface );
}
