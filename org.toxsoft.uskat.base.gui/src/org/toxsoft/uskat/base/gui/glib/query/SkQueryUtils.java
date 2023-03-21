package org.toxsoft.uskat.base.gui.glib.query;

import org.eclipse.core.runtime.IProgressMonitor;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
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
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @param aMonitor {@link IProgressMonitor} монитор выполнения
   * @param aCancelProducer {@link ISkQueryCancelProducer} механизм отмены запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void execQueryWithProgress( ISkAsynchronousQuery aQuery, IQueryInterval aInterval,
      IProgressMonitor aMonitor, ISkQueryCancelProducer aCancelProducer ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInterval, aMonitor, aCancelProducer );
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
        if( aQuery.state() == ESkQueryState.READY || aQuery.state() == ESkQueryState.FAILED ) {
          synchronized (aQuery) {
            aQuery.notifyAll();
          }
        }
      } );
      try {
        synchronized (aQuery) {
          aQuery.exec( aInterval );
          aQuery.wait();
        }
      }
      catch( InterruptedException e ) {
        LoggerUtils.errorLogger().error( e );
      }
    }
    finally {
      // Восстановление обработчика отмены операции
      aCancelProducer.setCancelHandler( prevCancelHandler );
    }
  }

}
