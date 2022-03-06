package org.toxsoft.uskat.s5.common.error;

import javax.ejb.ApplicationException;

import org.toxsoft.core.tslib.utils.TsLibUtils;

/**
 * {@link RuntimeException}-исключение без отката текущей транзакции
 *
 * @author mvk
 */
@ApplicationException( rollback = false )
public class S5NonRollbackTransactionException
    extends S5RuntimeException {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает трансилирующее исключение с форматированным сообщением.
   * <p>
   * Форматная строка aMessageFormat используется для создания сообщения методом
   * {@link String#format(String, Object...)}.
   *
   * @param aCause Throwable - ошибка, вызвавшее данное исключние
   */
  public S5NonRollbackTransactionException( Throwable aCause ) {
    super( aCause, TsLibUtils.EMPTY_STRING );
  }

  /**
   * Создает трансилирующее исключение с форматированным сообщением.
   * <p>
   * Форматная строка aMessageFormat используется для создания сообщения методом
   * {@link String#format(String, Object...)}.
   *
   * @param aCause Throwable - ошибка, вызвавшее данное исключние
   * @param aMessageFormat String - форматная строка
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5NonRollbackTransactionException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }

  /**
   * Создает исключение с форматированным сообщением.
   * <p>
   * Форматная строка aMessageFormat используется для создания сообщения методом
   * {@link String#format(String, Object...)}.
   *
   * @param aMessageFormat String - форматная строка
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5NonRollbackTransactionException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
  }
}
