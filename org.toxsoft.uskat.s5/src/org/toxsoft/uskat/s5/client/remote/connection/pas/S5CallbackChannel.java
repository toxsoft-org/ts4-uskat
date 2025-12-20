package org.toxsoft.uskat.s5.client.remote.connection.pas;

import java.net.*;

import org.toxsoft.core.pas.client.*;
import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Канал приема обратных вызовов сервера
 *
 * @author mvk
 */
final class S5CallbackChannel
    extends PasClientChannel {

  private final S5CallbackClient receiver;
  private boolean                needRegularFailureTimeout;

  /**
   * Фабрика каналов
   */
  @SuppressWarnings( "hiding" )
  static final IPasClientChannelCreator<S5CallbackChannel> CREATOR = S5CallbackChannel::new;

  /**
   * Конструктор.
   *
   * @param aContext {@link ITsContextRo} - контекст выполнения, общий для всех каналов и сервера
   * @param aSocket {@link Socket} сокет соединения
   * @param aHandlerHolder {@link PasHandlerHolder} хранитель обработчиков канала
   * @param aLogger {@link ILogger} журнал работы класса канала
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException ошибка создания читателя канала
   * @throws TsIllegalArgumentRtException ошибка создания писателя канала
   */
  S5CallbackChannel( ITsContextRo aContext, Socket aSocket, PasHandlerHolder<? extends PasClientChannel> aHandlerHolder,
      ILogger aLogger ) {
    super( aContext, aSocket, aHandlerHolder, aLogger );
    receiver = aContext.get( S5CallbackClient.class );
    needRegularFailureTimeout = true;
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов PasChannel
  //
  @Override
  protected boolean doInit() {
    if( super.doInit() ) {
      // Требование базового класса завершить работу канала
      return true;
    }
    // Оповещение об открытии канала
    receiver.onOpenChannel( this );
    // false: Требуем продолжить работу канала
    return false;
  }

  @Override
  protected void doClose() {
    receiver.onCloseChannel( this );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает признак того, что не установлен таймаут отказа работоспособности
   *
   * @return <b>true</b> таймаут не установлен;<b>false</b> таймаут установлен
   */
  boolean needRegularFailureTimeout() {
    return needRegularFailureTimeout;
  }

  /**
   * Установка таймаута отказа работоспособности канала
   *
   * @param aTimeout int мсек таймаута.
   * @throws TsIllegalArgumentRtException недопустимый таймаут
   */
  void setRegularFailureTimeout( int aTimeout ) {
    TsIllegalArgumentRtException.checkTrue( aTimeout <= 0 );
    needRegularFailureTimeout = false;
    super.setFailureTimeout( aTimeout );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
