package org.toxsoft.uskat.backend.s5.gui.utils;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;

class S5ConnectTask
    implements IRunnableWithProgress, ILongOpProgressCallback, Runnable {

  /**
   * The number of {@link Display#readAndDispatch()} calls required to guarantee updating of GUI elements.
   * <p>
   * Determined empirically.
   */
  private static final int READ_AND_DISPATCH_MAX = 512;

  /**
   * Milliseconds to wait after failed attempt to connection before next try.
   */
  private static final long ERR_CONNECTION_WAIT_MSECS = 300;

  private final S5ConnectDialog   dlg;
  private final ITsThreadExecutor threadExecutor;
  private final ISkConnection     conn;
  private final ITsContextRo      args;

  private IProgressMonitor   monitor;
  private TsRuntimeException fatalError;

  S5ConnectTask( S5ConnectDialog aDlg, ISkConnection aConn, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aDlg, aConn, aArgs );
    dlg = aDlg;
    threadExecutor = ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.getRef( aArgs );
    conn = aConn;
    args = aArgs;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //
  TsRuntimeException fatalErrorOrNull() {
    return fatalError;
  }

  // ------------------------------------------------------------------------------------
  // IRunnableWithProgress
  //

  @SuppressWarnings( "nls" )
  @Override
  public void run( IProgressMonitor aMonitor )
      throws InvocationTargetException,
      InterruptedException {
    monitor = aMonitor;
    // throw 'Invalid thread access' by dialog with fork true
    // for( int index = 0; index < READ_AND_DISPATCH_MAX; index++ ) {
    // Display.getDefault().readAndDispatch();
    // }
    while( true ) {
      if( dlg.isNeedCancel() ) {
        return;
      }
      threadExecutor.syncExec( this );
      if( fatalError != null ) {
        break;
      }
      if( conn.state() == ESkConnState.ACTIVE ) {
        break;
      }
      Thread.sleep( ERR_CONNECTION_WAIT_MSECS );
    }
    monitor.setTaskName( "Установка связи завершена" );
    aMonitor.done();
    if( conn.state() == ESkConnState.ACTIVE ) {
      LoggerUtils.defaultLogger().info( "Установлено соединение %s", conn ); //$NON-NLS-1$
    }
    if( fatalError != null ) {
      LoggerUtils.errorLogger().info( "Ошибка установки соединения : %s. Причина: %s", conn, //$NON-NLS-1$
          fatalError.getLocalizedMessage() );
    }
    return;
  }

  // ------------------------------------------------------------------------------------
  // ILongOpProgressCallback
  //
  @Override
  public boolean startWork( String aName, boolean aUndefined ) {
    if( monitor != null ) {
      readAndDispatch();
      monitor.beginTask( aName, -1 );
      readAndDispatch();
    }
    // cancellation is not supported.
    return false;
  }

  @Override
  public boolean updateWorkProgress( String aName, double aDonePercents ) {
    if( monitor != null ) {
      readAndDispatch();
      monitor.subTask( aName );
      readAndDispatch();
    }
    // cancellation is not supported.
    return false;
  }

  @Override
  public void finished( ValidationResult aStatus ) {
    if( monitor != null ) {
      readAndDispatch();
      monitor.done();
      readAndDispatch();
    }
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //

  @Override
  public void run() {
    fatalError = null;
    try {
      readAndDispatch();
      TsContext ctx = (TsContext)args;
      ctx.put( ISkConnectionConstants.REF_OP_PROGRESS.refKey(), this );
      conn.open( ctx );
      readAndDispatch();
    }
    catch( S5ConnectionException e ) {
      fatalError = e;
    }
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  private static void readAndDispatch() {
    for( int index = 0; index < READ_AND_DISPATCH_MAX; index++ ) {
      try {
        Display.getDefault().readAndDispatch();
      }
      catch( Throwable e ) {
        LoggerUtils.errorLogger().error( e );
      }
    }
  }
}
