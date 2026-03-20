package org.toxsoft.uskat.s5.common;

import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.logger.*;

/**
 * Общие локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5CommonResources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  /**
   * Неизвестная причина ошибки
   */
  String ERR_MSG_UNKNOWN_CAUSE = "???";

  /**
   * Журнал работы
   */
  ILogger commonResourceLogger = LoggerWrapper.getLogger( IS5CommonResources.class );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Возвращает причину ошибки
   *
   * @param aError {@link Throwable} ошибка
   * @return String причина ошибки
   */
  static String cause( Throwable aError ) {
    if( aError instanceof OutOfMemoryError ) {
      commonResourceLogger.error( aError );
    }
    if( aError != null && aError.getCause() instanceof OutOfMemoryError ) {
      commonResourceLogger.error( aError );
    }
    if( aError == null ) {
      return ERR_MSG_UNKNOWN_CAUSE;
    }
    String err = aError.getClass().getSimpleName();
    return (aError.getLocalizedMessage() != null ? err + ": " + aError.getLocalizedMessage() : err);
  }
}
