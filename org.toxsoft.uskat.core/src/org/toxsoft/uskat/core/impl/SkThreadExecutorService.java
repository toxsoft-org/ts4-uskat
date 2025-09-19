package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * Implementation of {@link ITsThreadExecutor} as uskat-service.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
public class SkThreadExecutorService
    extends AbstractSkService
    implements ITsThreadExecutor {

  /**
   * The service ID.
   */
  public static final String SERVICE_ID = "SkThreadExecutorService"; //$NON-NLS-1$

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkThreadExecutorService::new;

  /**
   * Thread executor
   */
  private final ITsThreadExecutor threadExecutor;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkThreadExecutorService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    threadExecutor = aCoreApi.executor();
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //
  @Override
  final protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  final protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadExecutor
  //
  @Override
  public Thread thread() {
    return threadExecutor.thread();
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    threadExecutor.asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    threadExecutor.syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    threadExecutor.timerExec( aMilliseconds, aRunnable );
  }

  @Override
  public void setLogger( ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aLogger );
    threadExecutor.setLogger( aLogger );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // Внимание! doJob может вызваться в ручную только из потока соединения (синхронизатора)
    TsIllegalStateRtException.checkFalse( thread().equals( Thread.currentThread() ) );
    // Фоновая работа синхронизатора. Обработка запросов asyncExec(...)
    threadExecutor.doJob();
  }

  // ------------------------------------------------------------------------------------
  // utilites
  //
  /**
   * Returns executor of API calls in one thread.
   *
   * @param aCoreApi {@link ISkCoreApi} core API
   * @return {@link ITsThreadExecutor} executor
   * @throws TsNullArgumentRtException any null agrument
   */
  public static ITsThreadExecutor getExecutor( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    ITsThreadExecutor service = (ITsThreadExecutor)aCoreApi.services().getByKey( SkThreadExecutorService.SERVICE_ID );
    return service;
  }
}
