package org.toxsoft.uskat.core.gui.glib.query;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Диалог прогресса выполнения операции
 *
 * @author mvk
 */
public class SkProgressDialog
    extends ProgressMonitorDialog
    implements ISkQueryCancelProducer {

  private String                dialogName;
  private long                  timeout;
  private ISkQueryCancelHandler cancelHandler;

  /**
   * Конструктор
   *
   * @param aShell {@link Shell} родительское окно
   * @param aDialogName String заголовок диалога
   * @param aTimeout long таймаут ожидания (мсек). < 0: бесконечно
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkProgressDialog( Shell aShell, String aDialogName, long aTimeout ) {
    super( TsNullArgumentRtException.checkNull( aShell ) );
    dialogName = TsNullArgumentRtException.checkNull( aDialogName );
    timeout = aTimeout;
  }

  // ------------------------------------------------------------------------------------
  // ISkQueryCancelProducer
  //
  @Override
  public ISkQueryCancelHandler setCancelHandler( ISkQueryCancelHandler aCancelHandler ) {
    ISkQueryCancelHandler prevHandler = cancelHandler;
    cancelHandler = aCancelHandler;
    return prevHandler;
  }

  // ------------------------------------------------------------------------------------
  // ProgressMonitorDialog
  //
  @Override
  protected Control createDialogArea( Composite aParent ) {
    Control c = super.createDialogArea( aParent );
    c.getShell().setText( dialogName );
    return c;
  }

  @Override
  public int open() {
    Thread thread = new Thread( () -> {
      getParentShell().getDisplay().asyncExec( () -> {
        if( progressIndicator.isDisposed() ) {
          return;
        }
        if( timeout >= 0 ) {
          progressIndicator.beginTask( 1000 );
        }
        else {
          progressIndicator.beginAnimatedTask();
        }
      } );
      if( timeout >= 0 ) {
        long sleep = timeout / 1000;
        for( int index = 0; index < 1000; index++ ) {
          getParentShell().getDisplay().asyncExec( () -> {
            if( progressIndicator.isDisposed() ) {
              return;
            }
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

  @Override
  protected void cancelPressed() {
    if( cancelHandler != null ) {
      cancelHandler.cancel();
    }
  }

  // ------------------------------------------------------------------------------------
  // protected
  //
  protected long timeout() {
    return timeout;
  }
}
