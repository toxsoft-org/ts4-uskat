package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.IS5Connection;

/**
 * Реализация клиентского стаба удаленного доступа к расширению backend предоставляемое s5-сервером
 *
 * @author mvk
 * @param <T> тип интерфейса расширения backend
 */
public class S5BackendAddonRemote<T>
    extends Stridable
    implements ICooperativeMultiTaskable, Serializable {

  private static final long serialVersionUID = 157157L;

  private final String    addonId;
  private final Class<T>  addonInterface;
  private S5RemoteBackend owner;
  private final ILogger   logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения backend
   * @param aName String имя расширения backend
   * @param aAddonInterface Class&lt;T&gt; тип интерфейса расширения backend
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected S5BackendAddonRemote( String aAddonId, String aName, Class<T> aAddonInterface ) {
    super( aAddonId, aName, TsLibUtils.EMPTY_STRING );
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonInterface );
    addonId = StridUtils.checkValidIdPath( aAddonId );
    addonInterface = aAddonInterface;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Провести инициализацию расширения backend
   * <p>
   * В момент вызова {@link #init(S5RemoteBackend)} соединение создано, но еще не установлено
   *
   * @param aOwner {@link S5RemoteBackend} backend ядра
   */
  final void init( S5RemoteBackend aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    owner = aOwner;
  }

  /**
   * Вызывается после обнаружения сервера.
   * <p>
   * Событие сообщает о том, что обнаружен сервер и с ним установлен канал {@link PasChannel}
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  final void onAfterDiscover( IS5Connection aSource ) {
    doOnAfterDiscover( aSource );
  }

  /**
   * Вызывается после образования связи с сервером.
   * <p>
   * При обработке события клиенты могут использовать функции сервера в полном объеме.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  final void onAfterConnect( IS5Connection aSource ) {
    doOnAfterConnect( aSource );
  }

  /**
   * Вызывается после завершения сеанса работы с сервером.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  final void onAfterDisconnect( IS5Connection aSource ) {
    doOnAfterDisconnect( aSource );
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает бекенд, контейнер расширений
   *
   * @return {@link S5RemoteBackend} бекенд
   */
  protected final S5RemoteBackend owner() {
    return owner;
  }

  /**
   * Возвращает текущее состояние backend
   *
   * @return boolean <b>true</b> удаленный backend доступен для обращения;<b>false</b> backend недоступен
   */
  protected final boolean isActive() {
    TsIllegalStateRtException.checkNull( owner, ERR_BACKEND_NOT_INIT );
    return owner.isActive();
  }

  /**
   * Возвращает удаленный доступ к расширению backend
   *
   * @return T удаленный доступ к расширению backend
   * @throws TsIllegalStateRtException нет соединения с сервером
   * @throws TsItemNotFoundRtException не найдено расширение backend
   */
  protected final T remote() {
    TsIllegalStateRtException.checkNull( owner, ERR_BACKEND_NOT_INIT );
    T retValue = findRemote();
    if( retValue == null ) {
      // Не найден addons
      if( !owner.isActive() ) {
        // Нет связи с сервером
        throw new TsIllegalStateRtException( ERR_NO_CONNECTION );
      }
      // Не найдено расширение backend
      throw new TsItemNotFoundRtException( ERR_BACKEND_ADDON_NOT_FOUND, addonId );
    }
    return retValue;
  }

  /**
   * Возвращает удаленный доступ к расширению backend
   *
   * @return T удаленный доступ к расширению backend
   */
  protected final T findRemote() {
    if( owner == null ) {
      // Расширение backend не инициализировано
      logger().debug( ERR_BACKEND_NOT_INIT, id() );
      return null;
    }
    if( !owner.isActive() ) {
      // Нет связи с сервером
      return null;
    }
    return owner.getBackendAddonRemote( addonId, addonInterface );
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Вызывается после обнаружения сервера.
   * <p>
   * Событие сообщает о том, что обнаружен сервер и с ним установлен канал {@link PasChannel}
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  protected void doOnAfterDiscover( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после образования связи с сервером.
   * <p>
   * При обработке события клиенты могут использовать функции сервера в полном объеме.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  protected void doOnAfterConnect( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после завершения сеанса работы с сервером.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  protected void doOnAfterDisconnect( IS5Connection aSource ) {
    // nop
  }
}
