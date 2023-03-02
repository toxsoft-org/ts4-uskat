package org.toxsoft.uskat.base.gui.utils;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.*;
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
public class SkQueryProgressDialogUtils {

  /**
   * Выполняет запрос в прогресс-диалоге с возможностью отмены выполения.
   *
   * @param aShell {@link Shell} родительское окно
   * @param aDialogName String - имя диалога ожидания.
   * @param aQuery {@link ISkAsynchronousQuery} запрос для выполнения.
   * @param aInterval {@link IQueryInterval} интервал времени запроса
   * @param aTimeout long таймаут (мсек) максимальное время выполнения запроса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void execQueryByProgressDialog( Shell aShell, String aDialogName, ISkAsynchronousQuery aQuery,
      IQueryInterval aInterval, long aTimeout ) {
    TsNullArgumentRtException.checkNulls( aShell, aDialogName, aQuery, aInterval );

    final ProgressMonitorDialog dialog = new ProgressMonitorDialog( aShell ) {

      @Override
      protected Control createDialogArea( Composite aParent ) {

        Control c = super.createDialogArea( aParent );
        c.getShell().setText( aDialogName );
        return c;
      }

      @Override
      protected void cancelPressed() {
        aQuery.cancel();
      }

      @Override
      public int open() {
        Thread thread = new Thread( () -> {
          aShell.getDisplay().asyncExec( () -> {
            if( aTimeout >= 0 ) {
              progressIndicator.beginTask( 1000 );
            }
          } );
          if( aTimeout >= 0 ) {
            long sleep = aTimeout / 1000;
            for( int index = 0; index < 1000; index++ ) {
              aShell.getDisplay().asyncExec( () -> {
                if( progressIndicator.isDisposed() ) {
                  return;
                }
                progressIndicator.worked( 1 );
              } );
              try {
                Thread.sleep( sleep );
              }
              catch( InterruptedException e ) {
                LoggerUtils.errorLogger().error( e );
              }
            }
          }
        } );
        thread.start();
        return super.open();
      }
    };

    try {
      dialog.run( true, true, aMonitor -> {
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
        synchronized (aQuery) {
          aQuery.exec( aInterval );
          aQuery.wait();
        }
      } );
    }
    catch( InvocationTargetException | InterruptedException e ) {
      LoggerUtils.errorLogger().error( e );
    }
  }

}
