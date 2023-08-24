package org.toxsoft.uskat.core.gui.glib.query;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;

/**
 * GUI-поддержка выполнения запросов {@link ISkAsynchronousQuery} в
 *
 * @author mvk
 */
public class SkQueryUtils {

  /**
   * Выполняет запрос с монитором процесса выполнения.
   *
   * @param aDisplay {@link Display} дисплей
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @param aMonitor {@link IProgressMonitor} монитор выполнения
   * @param aCancelProducer {@link ISkQueryCancelProducer} механизм отмены запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void execQueryWithProgress( Display aDisplay, ISkAsynchronousQuery aQuery, IQueryInterval aInterval,
      IProgressMonitor aMonitor, ISkQueryCancelProducer aCancelProducer ) {
    TsNullArgumentRtException.checkNulls( aDisplay, aQuery, aInterval, aMonitor, aCancelProducer );
    TsIllegalStateRtException.checkFalse( Thread.currentThread() == aDisplay.getThread() );
    // Установка обработчика отмены выполнения запроса
    ISkQueryCancelHandler prevCancelHandler = aCancelProducer.setCancelHandler( () -> aQuery.cancel() );
    try {
      // Установка слушателя выполнения запроса
      aQuery.genericChangeEventer().addListener( aSource -> {
        if( !aQuery.equals( aSource ) ) {
          return;
        }
        aMonitor.setTaskName( aQuery.stateMessage() );
        LoggerUtils.defaultLogger().info( "query message: %s", aQuery.stateMessage() ); //$NON-NLS-1$
      } );
      aQuery.exec( aInterval );
      while( aQuery.state() != ESkQueryState.READY && aQuery.state() != ESkQueryState.FAILED ) {
        aDisplay.readAndDispatch();
      }
    }
    finally {
      // Восстановление обработчика отмены операции
      aCancelProducer.setCancelHandler( prevCancelHandler );
    }
  }

}
