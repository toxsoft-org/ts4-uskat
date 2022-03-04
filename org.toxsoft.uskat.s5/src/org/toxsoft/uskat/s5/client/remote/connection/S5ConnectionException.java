package org.toxsoft.uskat.s5.client.remote.connection;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.utils.errors.TsRuntimeException;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;

/**
 * Ошибка при установлении соединения с сервером.
 * <p>
 * Кроме сообщения об ошибке содержит в себе детальную информацию о несостоявщемся подключении (адрес сервера, имя
 * пользователя и т.п.).
 *
 * @author goga
 */
public class S5ConnectionException
    extends TsRuntimeException {

  private static final long serialVersionUID = 157157L;

  private final IOptionSet configuration;

  /**
   * Создает исключение с форматированным сообщением.
   *
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5ConnectionException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
    configuration = new OptionSet();
  }

  /**
   * Создает (трансилирующее) исключение с форматированным сообщением.
   *
   * @param aCause Throwable - ошибка, вызвавшее данное исключние
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5ConnectionException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
    configuration = new OptionSet();
  }

  /**
   * Создает (трансилирующее) исключение с форматированным сообщением.
   *
   * @param aCause Throwable - ошибка, вызвавшее данное исключние
   * @param aConfiguation {@link IOptionSet} - конфигурация подключения к серверу
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5ConnectionException( Throwable aCause, IOptionSet aConfiguation, String aMessageFormat,
      Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
    configuration = aConfiguation;
  }

  /**
   * Создает исключение с форматированным сообщением.
   *
   * @param aConfiguation {@link IOptionSet} - конфигурация подключения к серверу
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5ConnectionException( IOptionSet aConfiguation, String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
    configuration = aConfiguation;
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * Возвращает конфигурацию подключения к серверу
   *
   * @return {@link IOptionSet} конфигурация подключения к серверу
   */
  public IOptionSet configuration() {
    return configuration;
  }

  /**
   * Возвращает имя пользователя, который пытался подключитсья к серверу.
   *
   * @return String - имя пользователя, который пытался подключитьcя к серверу
   */
  public String loginName() {
    return IS5ConnectionParams.OP_USERNAME.getValue( configuration ).asString();
  }

}
