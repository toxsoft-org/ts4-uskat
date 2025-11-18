package org.toxsoft.uskat.backend.s5.gui.utils;

import static org.toxsoft.uskat.backend.s5.gui.utils.ISkResources.*;

import java.lang.reflect.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.glib.query.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;

/**
 * Вспомогательные методы для работы с s5-бекендом в GUI
 *
 * @author mvk
 */
public class S5BackendGuiUtils {

  /**
   * Milliseconds to wait after failed attempt to connection before next try.
   */
  private static final long ERR_CONNECTION_WAIT_MSECS = 300;

  /**
   * Establishes connection to the server with progress dialog.
   * <p>
   * Progress dialog has single "Cancel" button.
   *
   * @param aShell {@link Shell} - parent shell for the progress dialog window
   * @param aConnection {@link ISkConnection} - connection to be opened
   * @param aArgs {@link ITsContextRo} - the connection arguments for {@link ISkConnection#open(ITsContextRo)}
   * @param aCaption String - dialog window caption text
   * @param aTimeout long - Connection wait time (milliseconds), < 0: infinite.
   * @return boolean - success flag, <code>true</code> connection was established
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException connection is already open
   */
  public static boolean showConnProgressDialog( Shell aShell, ISkConnection aConnection, ITsContextRo aArgs,
      String aCaption, long aTimeout ) {
    TsNullArgumentRtException.checkNulls( aShell, aConnection, aArgs, aCaption );
    TsIllegalArgumentRtException.checkTrue( aConnection.state().isOpen() );
    ITsThreadExecutor threadExecutor = ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.getRef( aArgs );

    // Создание диалога прогресса выполнения запроса
    ConnectionProgressDialog progressDialog = new ConnectionProgressDialog( aShell, aCaption, aTimeout );
    progressDialog.setCancelHandler( () -> {
      // Пользователь отменил операцию
      progressDialog.cancel();
    } );
    try {
      progressDialog.run( true, true, aMonitor -> {
        while( aConnection.state() != ESkConnState.ACTIVE ) {
          if( progressDialog.isNeedCancel() ) {
            // Пользователь отменил операцию
            return;
          }
          threadExecutor.syncExec( () -> {
            try {
              aConnection.open( aArgs );
            }
            catch( S5ConnectionException e ) {
              LoggerUtils.errorLogger().error( ERR_NO_CONNECT, e.getLocalizedMessage() );
            }
            try {
              Thread.sleep( ERR_CONNECTION_WAIT_MSECS );
            }
            catch( @SuppressWarnings( "unused" ) InterruptedException e ) {
              // nop
            }
            catch( Exception e ) {
              LoggerUtils.errorLogger().error( e );
            }
          } );
        }
        aMonitor.done();
        LoggerUtils.defaultLogger().info( "Connection opened" ); //$NON-NLS-1$
        return;
      } );

    }
    catch( InvocationTargetException | InterruptedException ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    return aConnection.state().isOpen();
  }

  /**
   * Диалог подключения
   */
  private final static class ConnectionProgressDialog
      extends SkProgressDialog {

    boolean needCancel = false;

    ConnectionProgressDialog( Shell aShell, String aDialogName, long aTimeout ) {
      super( aShell, aDialogName, aTimeout );
    }

    boolean isNeedCancel() {
      return needCancel;
    }

    void cancel() {
      needCancel = true;
    }
  }
}
