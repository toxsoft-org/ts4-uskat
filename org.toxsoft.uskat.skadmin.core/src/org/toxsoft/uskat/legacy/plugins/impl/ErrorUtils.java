package org.toxsoft.uskat.legacy.plugins.impl;

import static org.toxsoft.uskat.legacy.plugins.impl.ISkResources.*;

import java.util.Collection;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Набор методов для работы с исключениями ТоксСофт.
 *
 * @author goga
 */
public final class ErrorUtils {

  /**
   * Проверяет аргумент-массив на допустимость и выбрасывает исключение.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aArrayArg E[] - проверяемый массив
   * @param aMinCount int - минимально допустимое кол-во элементов в массиве
   * @return E[] - всегда возвращает аргумент aArrayArg
   * @throws TsNullArgumentRtException aArrayArg = null
   * @throws TsNullArgumentRtException длина массива меньше aMinCount
   * @throws TsNullArgumentRtException любой элемент массива aArrayArg = null
   */
  public static <E> E[] checkArrayArg( E[] aArrayArg, int aMinCount ) {
    if( aArrayArg == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aArrayArg.length < aMinCount ) {
      throw new TsIllegalArgumentRtException();
    }
    for( int i = 0, n = aArrayArg.length; i < n; i++ ) {
      if( aArrayArg[i] == null ) {
        throw new TsNullArgumentRtException();
      }
    }
    return aArrayArg;
  }

  /**
   * Проверяет аргумент-массив и его элементы на равенство null.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aArrayArg E[] - проверяемый массив
   * @return E[] - всегда возвращает аргумент aArrayArg
   * @throws TsNullArgumentRtException aArrayArg = null
   * @throws TsNullArgumentRtException любой элемент массива aArrayArg = null
   */
  public static <E> E[] checkArrayArg( E[] aArrayArg ) {
    return checkArrayArg( aArrayArg, 0 );
  }

  /**
   * Проверяет, что список-коллекция не null, ни все его элементы не-null.
   *
   * @param <E> - необязательная типизация по переданной ссылке
   * @param aColl Collection&lt;E&gt; - проверямая коллекция
   * @return Collection&lt;E&gt; - всегда возвращает аргумент aColl
   * @throws TsNullArgumentRtException aColl = null
   * @throws TsNullArgumentRtException любой элемент коллекции aColl = null
   */
  public static <E> Collection<E> checkCollectionArg( Collection<E> aColl ) {
    TsNullArgumentRtException.checkNull( aColl );
    for( E e : aColl ) {
      TsNullArgumentRtException.checkNull( e );
    }
    return aColl;
  }

  /**
   * Форматирует сообщение об ошибке из исключения в многострочный текст.
   * <p>
   * Особым образом обрабатывает исключения TsException/TsRuntiomeException.
   *
   * @param aEx {@link Exception} - исключение с сообщением об ошибке
   * @return String - многострочный текст с указанием имени класса, вывзвавшего ошибку
   */
  public static String formatErrMsg( Exception aEx ) {
    StringBuilder sb = new StringBuilder();
    if( !(aEx instanceof TsException) ) {
      sb.append( aEx.getClass().getName() );
      sb.append( ": " ); //$NON-NLS-1$
    }
    sb.append( aEx.getMessage() );
    // add causes messages
    for( Throwable th = aEx.getCause(); th != null; th = th.getCause() ) {
      sb.append( MSG_FMT_EX_CAUSE_CLASS );
      sb.append( th.getClass().getName() );
      sb.append( MSG_FMT_EX_MESSAGE );
      sb.append( th.getMessage() );
    }
    return sb.toString();
  }

  /**
   * Запрет на создавани экземпляры класса.
   */
  public ErrorUtils() {
    // nop
  }
}
