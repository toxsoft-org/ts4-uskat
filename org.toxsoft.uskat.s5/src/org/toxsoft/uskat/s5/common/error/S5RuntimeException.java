package org.toxsoft.uskat.s5.common.error;

import static org.toxsoft.uskat.s5.common.error.IS5Resources.*;

import org.toxsoft.core.tslib.utils.errors.TsRuntimeException;

/**
 * Базовая не проверяемая ошибка в коде сервера S5.
 *
 * @author hazard157
 */
public class S5RuntimeException
    extends TsRuntimeException {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает (трансилирующее) исключение, основанное на вызвавшем исключении.
   *
   * @param aCause {@link Throwable} ошибка, вызвавшее данное исключние
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5RuntimeException( Throwable aCause, String aMessageFormat, Object... aMsgArgs ) {
    super( aCause, aMessageFormat, aMsgArgs );
  }

  /**
   * Создает исключение с заданным текстом сообщения.
   *
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   */
  public S5RuntimeException( String aMessageFormat, Object... aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
  }

  /**
   * Создает трансилирующее исключение с предопределенным текстом сообщения.
   *
   * @param aCause {@link Throwable} ошибка, вызвавшее данное исключние
   */
  public S5RuntimeException( Throwable aCause ) {
    super( MSG_S5_RUNTIME_EXEPTION, aCause );
  }

  /**
   * Создает исключение с предопределенным текстом сообщения.
   */
  public S5RuntimeException() {
    super( MSG_S5_RUNTIME_EXEPTION );
  }

  /**
   * Проверяет выражение, и если оно не верно, выбрасывает исключение.
   *
   * @param aExpression boolean - проверяемое выражение
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   * @throws S5RuntimeException - если aExpression == false
   */
  public static void checkFalse( boolean aExpression, String aMessageFormat, Object... aMsgArgs )
      throws S5RuntimeException {
    if( !aExpression ) {
      throw new S5RuntimeException( aMessageFormat, aMsgArgs );
    }
  }

  /**
   * Проверяет выражение, и если оно не верно, выбрасывает исключение с заданным текстом сообщения.
   *
   * @param aExpression boolean - проверяемое выражение
   * @throws S5RuntimeException - если aExpression == false
   */
  public static void checkFalse( boolean aExpression )
      throws S5RuntimeException {
    if( !aExpression ) {
      throw new S5RuntimeException( MSG_S5_RUNTIME_EXEPTION );
    }
  }

  /**
   * Проверяет выражение, и если оно верно, выбрасывает исключение.
   *
   * @param aExpression boolean - проверяемое выражение
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   * @throws S5RuntimeException - если aExpression == true
   */
  public static void checkTrue( boolean aExpression, String aMessageFormat, Object... aMsgArgs )
      throws S5RuntimeException {
    if( aExpression ) {
      throw new S5RuntimeException( aMessageFormat, aMsgArgs );
    }
  }

  /**
   * Проверяет выражение, и если оно верно, выбрасывает исключение с заданным тектсом сообщения.
   *
   * @param aExpression boolean - проверяемое выражение
   * @throws S5RuntimeException - если aExpression == true
   */
  public static void checkTrue( boolean aExpression )
      throws S5RuntimeException {
    if( aExpression ) {
      throw new S5RuntimeException( MSG_S5_RUNTIME_EXEPTION );
    }
  }

  /**
   * Проверяет ссылку, и если она не нулевая, выбрасывает исключение.<br>
   * Для удобаства использования, возвращает переданную ссылку.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aReference Object - проверяемая ссылка
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   * @return E - переданная ссылка
   * @throws S5RuntimeException - проверяемая ссылка не равна null
   */
  public static <E> E checkNoNull( E aReference, String aMessageFormat, Object... aMsgArgs )
      throws S5RuntimeException {
    if( aReference != null ) {
      throw new S5RuntimeException( aMessageFormat, aMsgArgs );
    }
    return aReference;
  }

  /**
   * Проверяет ссылку, и если она не нулевая, выбрасывает исключение с заданным текстом сообщения.<br>
   * Для удобаства использования, возвращает переданную ссылку.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aReference Object - проверяемая ссылка
   * @return E - переданная ссылка
   * @throws S5RuntimeException - проверяемая ссылка не равна null
   */
  public static <E> E checkNoNull( E aReference )
      throws S5RuntimeException {
    if( aReference != null ) {
      throw new S5RuntimeException( MSG_S5_RUNTIME_EXEPTION );
    }
    return aReference;
  }

  /**
   * Проверяет ссылку, и если она нулевая, выбрасывает исключение.<br>
   * Для удобаства использования, возвращает переданную ссылку.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aReference Object - проверяемая ссылка
   * @param aMessageFormat String - форматирующее сообщение о сути исключения
   * @param aMsgArgs Object[] - аргументы форматированного сообщения
   * @return E - переданная ссылка
   * @throws S5RuntimeException - проверяемая ссылка равна null
   */
  public static <E> E checkNull( E aReference, String aMessageFormat, Object... aMsgArgs )
      throws S5RuntimeException {
    if( aReference == null ) {
      throw new S5RuntimeException( aMessageFormat, aMsgArgs );
    }
    return aReference;
  }

  /**
   * Проверяет ссылку, и если она нулевая, выбрасывает исключение с заданным текстом сообщения.<br>
   * Для удобаства использования, возвращает переданную ссылку.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aReference Object - проверяемая ссылка
   * @return E - переданная ссылка
   * @throws S5RuntimeException - проверяемая ссылка равна null
   */
  public static <E> E checkNull( E aReference )
      throws S5RuntimeException {
    if( aReference == null ) {
      throw new S5RuntimeException( MSG_S5_RUNTIME_EXEPTION );
    }
    return aReference;
  }
}
