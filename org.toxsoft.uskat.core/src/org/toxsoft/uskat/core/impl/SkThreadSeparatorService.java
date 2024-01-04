package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

import core.tslib.bricks.synchronize.ITsThreadSynchronizer;

/**
 * Implementation of thread separation service.
 * <p>
 * Solves the tasks of sharing data access between client threads and uskat.
 *
 * @author mvk
 */
public class SkThreadSeparatorService
    extends AbstractSkService
    implements ITsThreadSynchronizer, Runnable {

  /**
   * The service ID.
   */
  public static final String SERVICE_ID = "SkThreadSeparatorService"; //$NON-NLS-1$

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkThreadSeparatorService::new;

  /**
   * Mandotary context parameter: dojob timeout (msec)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  public static final IDataDef OP_DOJOB_TIMEOUT = create( SERVICE_ID + ".DoJobTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, "Timeout", // //$NON-NLS-1$
      TSID_DESCRIPTION, "Dojob timeout (msec)", // //$NON-NLS-1$
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.avInt( 100 ) );

  private final IDevCoreApi devCoreApi;
  private int               doJobTimeout;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected SkThreadSeparatorService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    devCoreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //
  @Override
  final protected void doInit( ITsContextRo aArgs ) {
    doJobTimeout = OP_DOJOB_TIMEOUT.getValue( aArgs.params() ).asInt();
    // TODO: mvkd
    if( doJobTimeout >= 0 ) {
      // Автоматическое выполнение doJob
      timerExec( doJobTimeout, this );
    }
  }

  @Override
  final protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ITsThreadSynchronizer
  //
  @Override
  public Thread thread() {
    return devCoreApi.synchronizer().thread();
  }

  @Override
  public void asyncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.synchronizer().asyncExec( aRunnable );
  }

  @Override
  public void syncExec( Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.synchronizer().syncExec( aRunnable );
  }

  @Override
  public void timerExec( int aMilliseconds, Runnable aRunnable ) {
    TsNullArgumentRtException.checkNull( aRunnable );
    devCoreApi.synchronizer().timerExec( aMilliseconds, aRunnable );
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  @Override
  public void run() {
    // Автоматическое выполнение doJob
    doJob();
    timerExec( doJobTimeout, this );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // Внимание! doJob может вызваться в ручную только из потока соединения (синхронизатора)
    TsIllegalStateRtException.checkFalse( thread().equals( Thread.currentThread() ) );
    // Отработка сообщений от backend
    devCoreApi.doJobInCoreMainThread();
    // Фоновая работа синхронизатора. Обработка запросов asyncExec(...)
    devCoreApi.synchronizer().doJob();
  }

}
