package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

import core.tslib.bricks.synchronize.ITsThreadExecutor;

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

  // /**
  // * Mandotary context parameter: dojob timeout (msec)
  // * <p>
  // * Тип: {@link EAtomicType#INTEGER}
  // */
  // public static final IDataDef OP_DOJOB_TIMEOUT = create( SERVICE_ID + ".DoJobTimeout", INTEGER, //$NON-NLS-1$
  // TSID_NAME, "Timeout", // //$NON-NLS-1$
  // TSID_DESCRIPTION, "Dojob timeout (msec)", // //$NON-NLS-1$
  // TSID_IS_NULL_ALLOWED, AV_FALSE, //
  // TSID_DEFAULT_VALUE, AvUtils.avInt( -1 ) );

  private final IDevCoreApi devCoreApi;
  // private int doJobTimeout;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkThreadExecutorService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    devCoreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //
  @Override
  final protected void doInit( ITsContextRo aArgs ) {
    // doJobTimeout = OP_DOJOB_TIMEOUT.getValue( aArgs.params() ).asInt();
    // // TODO: mvkd
    // if( doJobTimeout >= 0 ) {
    // // Автоматическое выполнение doJob
    // timerExec( doJobTimeout, this );
    // }
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
    return devCoreApi.executor().thread();
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.executor().asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.executor().syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.executor().timerExec( aMilliseconds, aRunnable );
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  // @Override
  // public void run() {
  // // Автоматическое выполнение doJob
  // doJob();
  // timerExec( doJobTimeout, this );
  // }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // Внимание! doJob может вызваться в ручную только из потока соединения (синхронизатора)
    TsIllegalStateRtException.checkFalse( thread().equals( Thread.currentThread() ) );
    // Фоновая работа синхронизатора. Обработка запросов asyncExec(...)
    devCoreApi.executor().doJob();
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
