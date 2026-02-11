package org.toxsoft.uskat.backend.s5.gui.utils;

import java.lang.reflect.*;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Вспомогательные методы для работы с s5-бекендом в GUI
 *
 * @author mvk
 */
public class S5BackendGuiUtils {

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
    S5ConnectDialog dlg = new S5ConnectDialog( aShell, aCaption, aTimeout );
    S5ConnectTask task = new S5ConnectTask( dlg, aConnection, aArgs );
    try {
      // fork = false, cancelable = true
      dlg.run( false, true, task );
      TsRuntimeException fatalError = task.fatalErrorOrNull();
      if( fatalError != null ) {
        throw fatalError;
      }
    }
    catch( InvocationTargetException | InterruptedException ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    return aConnection.state().isOpen();
  }
}
