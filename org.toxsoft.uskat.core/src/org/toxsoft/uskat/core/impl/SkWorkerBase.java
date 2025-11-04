package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link ISkWorker} base implementation.
 *
 * @author mvk
 * @param <CONFIG> тип конфигурации компонента
 */
public class SkWorkerBase<CONFIG extends ISkWorkerConfig>
    extends Stridable
    implements ISkWorker, ISkCurrDataChangeListener {

  /**
   * Интервал (мсек) вывода статистики.
   */
  private static final long STAT_INTERVAL = 10 * 1000;

  private ITsContextRo                                  configContext;
  private ITsContext                                    sharedContext;
  private ISkCoreApi                                    coreApi;
  private ITsThreadExecutor                             threadExecutor;
  private CONFIG                                        configuration;
  private final IMapEdit<Gwid, ISkReadCurrDataChannel>  cdr = new ElemMap<>();
  private final IMapEdit<Gwid, ISkWriteCurrDataChannel> cdw = new ElemMap<>();

  private boolean stopped = true;

  /**
   * Таймер статистики
   */
  private final SkIntervalTimer statTimer = new SkIntervalTimer( STAT_INTERVAL );

  /**
   * Количество пакетов cdr.
   */
  private int statCurrDataRecevied;

  /**
   * Количество ВСЕХ полученных пакетов cdr.
   */
  private int statAllCurrDataRecevied;

  /**
   * Количество ошибок работы монитора.
   */
  private int statErrors;

  /**
   * Количество ВСЕХ ошибок работы монитора.
   */
  private int statAllErrors;

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Конструктор.
   *
   * @param aId String идентификатор компонента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkWorkerBase( String aId ) {
    super( aId );
  }

  // ------------------------------------------------------------------------------------
  // ISitrolTrainWorker
  //
  /**
   * Установить контекст компонента.
   * <p>
   * Вызывается после конструктора компонента, но до вызова {@link ISkWorker#start()}.
   *
   * @param aContext {@link ITsContext} контекст компонента
   * @throws TsNullArgumentRtException аргумент = null
   */
  @Override
  public final void setContext( ITsContextRo aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    configContext = aContext;
    sharedContext = ISkWorkerHardConstants.REFDEF_WORKER_SHARED_CONTEXT.getRef( aContext );
    coreApi = ISkWorkerHardConstants.REFDEF_WORKER_CORE_API.getRef( aContext );
    threadExecutor = SkThreadExecutorService.getExecutor( coreApi );
    logger = ISkWorkerHardConstants.REFDEF_WORKER_LOGGER.getRef( aContext );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public final void setConfiguration( ISkWorkerConfig aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    ISkWorkerConfig prevConfig = configuration;
    configuration = (CONFIG)aConfiguration;
    if( prevConfig != null && !aConfiguration.equals( prevConfig ) ) {
      doWorkerConfigChanged( prevConfig, aConfiguration );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkCurrDataChangeListener
  //

  @Override
  public final void onCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
    // statistics + logs
    statCurrDataRecevied++;
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      logger.debug( FMT_INFO_CURRDATA_RECEVIED, Integer.valueOf( aNewValues.size() ) );
    }
    doWorkerOnCurrData( aNewValues );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeWorkerComponent
  //

  @Override
  public final void start() {
    threadExecutor.syncExec( () -> {
      doWorkerStart();
      stopped = false;
    } );
  }

  @Override
  public final boolean queryStop() {
    if( stopped ) {
      return stopped;
    }
    threadExecutor.syncExec( () -> {
      doWorkerQueryStop();
      for( ISkReadCurrDataChannel ch : cdr().values() ) {
        ch.close();
      }
      for( ISkWriteCurrDataChannel ch : cdw().values() ) {
        ch.close();
      }
      stopped = true;
    } );
    return stopped;
  }

  @Override
  public final boolean isStopped() {
    return stopped;
  }

  @Override
  public final void destroy() {
    queryStop();
    stopped = true;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @SuppressWarnings( { "boxing" } )
  @Override
  public final void doJob() {
    writeStateInfo( this, "doJob", "state 1" ); //$NON-NLS-1$//$NON-NLS-2$
    if( stopped ) {
      writeStateInfo( this, "doJob", "state 2" ); //$NON-NLS-1$//$NON-NLS-2$
      return; // not yet started or already stopped
    }

    Thread currThread = Thread.currentThread();
    if( currThread == threadExecutor.thread() ) {
      writeStateInfo( this, "doJob", "state 3.1 (currThread == threadExecutor.thread())" ); //$NON-NLS-1$//$NON-NLS-2$
    }
    else {
      writeStateInfo( this, "doJob", "state 3.2 (currThread != threadExecutor.thread())" ); //$NON-NLS-1$//$NON-NLS-2$
    }
    // Выполнение фоновой обработки компонента
    threadExecutor.syncExec( () -> {
      writeStateInfo( this, "doJob", "state 4" ); //$NON-NLS-1$//$NON-NLS-2$
      doWorkerDoJob();
      writeStateInfo( this, "doJob", "state 5" ); //$NON-NLS-1$//$NON-NLS-2$
    } );
    writeStateInfo( this, "doJob", "state 6" ); //$NON-NLS-1$//$NON-NLS-2$

    if( statTimer.update() ) {
      // Формирование общей статистики
      statAllCurrDataRecevied += statCurrDataRecevied;
      statAllErrors += statErrors;
      // Журнал
      logger.debug( FMT_INFO_DOJOB_STAT, //
          statCurrDataRecevied, statAllCurrDataRecevied, //
          statErrors, statAllErrors //
      );
      // Сброс статистики за интервал
      statCurrDataRecevied = statErrors = 0;
    }
    writeStateInfo( this, "doJob", "state 7" ); //$NON-NLS-1$//$NON-NLS-2$
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + configuration.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof SkWorkerBase other) ) {
      return false;
    }
    if( !configuration.equals( other.configuration ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return configuration.id();
  }

  // ------------------------------------------------------------------------------------
  // methods for descendants
  //
  protected final ITsContextRo configContext() {
    return configContext;
  }

  protected ITsContext sharedContext() {
    return sharedContext;
  }

  protected final ISkCoreApi coreApi() {
    return coreApi;
  }

  protected final CONFIG configuration() {
    return configuration;
  }

  protected final ITsThreadExecutor threadExecutor() {
    return threadExecutor;
  }

  protected final IMapEdit<Gwid, ISkReadCurrDataChannel> cdr() {
    return cdr;
  }

  protected final IMapEdit<Gwid, ISkWriteCurrDataChannel> cdw() {
    return cdw;
  }

  protected final ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // methods for implementation
  //
  protected void doWorkerStart() {
    // nop
  }

  @SuppressWarnings( "unused" )
  protected void doWorkerConfigChanged( ISkWorkerConfig aPrevConfig, ISkWorkerConfig aNewConfig ) {
    // nop
  }

  @SuppressWarnings( "unused" )
  protected void doWorkerOnCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
    // nop
  }

  protected void doWorkerDoJob() {
    // nop
  }

  protected void doWorkerQueryStop() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  protected static void writeStateInfo( SkWorkerBase<?> aWorker, String aParamId, String aState ) {
    TsNullArgumentRtException.checkNulls( aWorker, aParamId, aState );
    long currTime = System.currentTimeMillis();
    String id = String.format( "%s.%s", aParamId, aWorker.id() ); //$NON-NLS-1$
    String message = String.format( "[%s] %s", TimeUtils.timestampToString( currTime ), aState ); //$NON-NLS-1$
    aWorker.sharedContext.params().setStr( id, message );
  }
}
