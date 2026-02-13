package org.toxsoft.uskat.s5.server.backend.impl;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;

/**
 * Ошибка доступа к системе: "Доступ к системе запрещен. Неверное имя пользователя или пароль".
 * <p>
 * Ошибка появляется при подключении пользователя к системе {@link S5BackendSession#init(IS5SessionInitData)}
 *
 * @author mvk
 */
public class S5AccessDeniedException
    extends TsIllegalArgumentRtException {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор
   *
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5AccessDeniedException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
  }

  /**
   * Constructor for wrapper exception.
   * <p>
   * Message string is created using {@link String#format(String, Object...)}.
   *
   * @param aCause Throwable - cause, mey be <code>null</code>
   * @param aMessageFormat String - message format string
   * @param aMsgArgs Object[] - optional arguments for message string
   */
  public S5AccessDeniedException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }
}
