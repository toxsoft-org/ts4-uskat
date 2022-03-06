package org.toxsoft.uskat.s5.server.sessions;

import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;

/**
 * Внутренняя ошибка API-сессии пользователя.
 * <p>
 * Ошибка появляется при вызове бизнес-методов API сессии, когда методы поднимают незадекларированные в API исключения
 *
 * @author mvk
 */
public class S5SessionApiRtException
    extends TsInternalErrorRtException {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает (трансилирующее) исключение, основанное на вызвавшем исключении.
   *
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5SessionApiRtException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
  }

  /**
   * Создает (трансилирующее) исключение, основанное на вызвавшем исключении.
   *
   * @param aCause {@link Throwable} ошибка, вызвавшее данное исключние
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5SessionApiRtException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }

}
