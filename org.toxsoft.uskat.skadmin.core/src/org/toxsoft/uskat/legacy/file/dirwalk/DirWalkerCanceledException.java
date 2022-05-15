package org.toxsoft.uskat.legacy.file.dirwalk;

import org.toxsoft.core.tslib.utils.errors.TsException;

/**
 * Исключение, означающее отмену обработчиком обхода директории.
 *
 * @author goga
 */
@SuppressWarnings( "serial" )
public class DirWalkerCanceledException
    extends TsException {

  /**
   * Простой конструктор.
   *
   * @param aReason String причина прекращения обхода
   */
  public DirWalkerCanceledException( String aReason ) {
    super( aReason );
  }

  /**
   * Создаем с исключением, которое заставляет прервать обход.
   *
   * @param aReason String описание причины прекращения обхода
   * @param aCause Throwable исключение - виновник
   */
  public DirWalkerCanceledException( String aReason, Throwable aCause ) {
    super( aReason, aCause );
  }
}
