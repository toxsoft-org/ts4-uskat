package org.toxsoft.uskat.s5.common;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;

import org.toxsoft.core.tslib.utils.logs.ILogger;

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
  /**
   * Формат вывода элемента стека
   */
  String MSG_STACK_ITEM = "   %s\n";

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
  ILogger commonResourceLogger = getLogger( IS5CommonResources.class );

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

  /**
   * Возвращает стек вызовов текущего потока в текстовом представлении
   *
   * @return String текстовое представление стека
   */
  static String currentThreadStackToString() {
    // Вывод стека запроса завершения
    StringBuilder sb = new StringBuilder();
    // Стек текущего потока
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    // index = 2 чтобы избежать вывод вызова Thread.getStackTrace() и currentThreadStackToString()
    for( int index = 2, n = stack.length; index < n; index++ ) {
      sb.append( format( MSG_STACK_ITEM, stack[index] ) );
    }
    return sb.toString();
  }
}
