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

  private static final long ERR_CONNECTION_TIMEOUT = 500;

  /**
   * @param aShell {@link Shell} родительское окно
   * @param aConnection {@link ISkConnection} открываемое соединение
   * @param aConnectionContext {@link ITsContext} контекст открываемого соединения, параметр
   *          {@link ISkConnection#open(ITsContextRo)}
   * @param aText String текст в диалоге
   * @param aTimeout long время (мсек) ожидания подключения. < 0: бесконечно.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unused" )
  public static void showConnProgressDialog( Shell aShell, ISkConnection aConnection,
      ITsContextRo aConnectionContext, String aText, long aTimeout ) {
    TsNullArgumentRtException.checkNulls( aShell, aConnection, aConnectionContext, aText );

    ITsThreadExecutor threadExecutor = ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.getRef( aConnectionContext );

    // Создание диалога прогресса выполнения запроса
    ConnectionProgressDialog progressDialog = new ConnectionProgressDialog( aShell, aText, aTimeout );
    progressDialog.setCancelHandler( () -> {
      // Пользователь отменил операцию
      progressDialog.cancel();
    } );
    try {
      // fork = false, cancelable = true
      progressDialog.run( true, true, aMonitor -> {
        while( aConnection.state() != ESkConnState.ACTIVE ) {
          if( progressDialog.isNeedCancel() ) {
            // Пользователь отменил операцию
            return;
          }
          threadExecutor.syncExec( () -> {
            try {
              aConnection.open( aConnectionContext );
            }
            catch( S5ConnectionException e ) {
              LoggerUtils.errorLogger().error( ERR_NO_CONNECT, e.getLocalizedMessage() );
            }
            try {
              Thread.sleep( ERR_CONNECTION_TIMEOUT );
            }
            catch( InterruptedException e ) {
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
