package org.toxsoft.uskat.s5.client.remote.connection;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullObjectErrorRtException;

/**
 * Слушатель изменения состояния подключения к серверу.
 *
 * @author goga
 * @author mvk
 */
public interface IS5ConnectionListener {

  /**
   * Несуществующий слушатель соединения
   */
  IS5ConnectionListener NULL = new InternalNullConnectionListener();

  /**
   * Вызывается перед активизацией соединения.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onBeforeActivate( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после активизации соединения.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onAfterActivate( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается перед деактивизацией соединения.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onBeforeDeactivate( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после деактивизации соединения.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onAfterDeactivate( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается перед попыткой образования связи с сервером.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onBeforeConnect( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после обнаружения сервера.
   * <p>
   * Событие сообщает о том, что обнаружен сервер и с ним установлен канал {@link PasChannel}
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onAfterDiscover( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после образования связи с сервером.
   * <p>
   * При обработке события клиенты могут использовать функции сервера в полном объеме.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onAfterConnect( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается перед завершением связи работы с сервером.
   * <p>
   * При обработке события клиенты могут использовать функции сервера в полном объеме.
   * <p>
   * Следует учитывать, что событие {@link #onBeforeDisconnect(IS5Connection)} не формируется при обрыве связи со
   * стороны сервера.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onBeforeDisconnect( IS5Connection aSource ) {
    // nop
  }

  /**
   * Вызывается после завершения сеанса работы с сервером.
   * <p>
   * После события {@link #onAfterDisconnect(IS5Connection)} соединение остается в активном состоянии до появления
   * события {@link #onAfterDeactivate(IS5Connection)}.
   *
   * @param aSource {@link IS5Connection} - соединение - источник сообщения
   */
  default void onAfterDisconnect( IS5Connection aSource ) {
    // nop
  }

}

/**
 * Реализация несуществующего описания соединения {@link IS5ConnectionListener#NULL}.
 */
class InternalNullConnectionListener
    implements IS5ConnectionListener, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5ConnectionListener#NULL}.
   *
   * @return Object объект {@link IS5ConnectionListener#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5ConnectionListener.NULL;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов IS5ConnectionListener
  //
  @Override
  public void onBeforeActivate( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onAfterActivate( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onBeforeDeactivate( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onAfterDeactivate( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onAfterConnect( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onBeforeDisconnect( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public void onAfterDisconnect( IS5Connection aSource ) {
    throw new TsNullObjectErrorRtException();
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов Object
  //
  @Override
  public int hashCode() {
    return TsLibUtils.INITIAL_HASH_CODE;
  }

  @Override
  public boolean equals( Object obj ) {
    return obj == this;
  }

  @Override
  public String toString() {
    return IS5ConnectionListener.class.getSimpleName() + ".NULL"; //$NON-NLS-1$
  }
}
