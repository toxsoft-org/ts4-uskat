package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.ctx.impl.IAskParent;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Синхронизация доступа к {@link ISkConnection} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedConnection
    extends S5SynchronizedResource<ISkConnection>
    implements ISkConnection {

  private S5SynchronizedCoreApi            coreApi;
  private IListEdit<ISkConnectionListener> listeners       = new ElemLinkedList<>();
  private ESkConnState                     manualState;
  private static int                       instanceCounter = 0;

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkConnection} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка соединения
   * @param aManualStateChange boolean<b>true</b> состояние соединения изменяется в ручную; <b>false</b>автоматическое
   *          изменение состояния.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private S5SynchronizedConnection( ISkConnection aTarget, ReentrantReadWriteLock aLock, boolean aManualStateChange ) {
    super( aTarget, aLock );
    if( aTarget.state() != ESkConnState.CLOSED ) {
      coreApi = new S5SynchronizedCoreApi( aTarget.coreApi(), nativeLock() );
    }
    manualState = (aManualStateChange ? ESkConnState.CLOSED : null);
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkConnection aPrevTarget, ISkConnection aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // Удаление блокировки из пула
    // 2022-06-15 mvk---
    // S5Lockable.removeLockableFromPool( aPrevTarget.mainLock() );
    // Дерегистрация слушателей старого соединения при автоматическом изменении состояния
    if( manualState == null ) {
      ISkConnection oldTarget = target();
      for( ISkConnectionListener listener : listeners ) {
        oldTarget.removeConnectionListener( listener );
      }
    }
    if( coreApi == null ) {
      coreApi = new S5SynchronizedCoreApi( aNewTarget.coreApi(), aNewLock );
      return;
    }
    // Замена цели API
    coreApi.changeTarget( aNewTarget.coreApi(), aNewLock );
    // Регистрация слушателей в новом соединении при автоматическом изменении состояния
    if( manualState == null ) {
      for( ISkConnectionListener listener : listeners ) {
        aNewTarget.addConnectionListener( listener );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает блокировку используемую соединением
   *
   * @return {@link S5Lockable} блокировка
   */
  public S5Lockable getLock() {
    return super.lock();
  }

  /**
   * Изменить состояние соединения
   *
   * @param aNewState {@link ESkConnState} новое состояние соединения
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException состояние соединения изменяется автоматически
   */
  public void changeState( ESkConnState aNewState ) {
    TsNullArgumentRtException.checkNull( aNewState );
    TsIllegalStateRtException.checkNull( manualState );
    if( manualState.equals( aNewState ) ) {
      return;
    }
    IListEdit<ISkConnectionListener> ll = new ElemLinkedList<>();
    lockWrite( this );
    try {
      ll = new ElemArrayList<>( listeners );
    }
    finally {
      unlockWrite( this );
    }
    // Оповещение слушателей "до"
    for( ISkConnectionListener l : ll ) {
      l.beforeSkConnectionStateChange( this, aNewState );
    }
    ESkConnState prevState = manualState;
    manualState = aNewState;
    // Оповещение слушателей "после"
    for( ISkConnectionListener l : ll ) {
      l.onSkConnectionStateChanged( this, prevState );
    }
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkConnection} защищаемое соединение
   * @return {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5SynchronizedConnection createSynchronizedConnection( ISkConnection aTarget ) {
    return createSynchronizedConnection( aTarget, new ReentrantReadWriteLock(), false, TsLibUtils.EMPTY_STRING );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkConnection} защищаемое соединение
   * @param aLock {@link ReentrantReadWriteLock} внешняя блокировка используемая соединением
   * @param aLockName String имя блокировки. Пустая строка - выбирается автоматически
   * @return {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5SynchronizedConnection createSynchronizedConnection( ISkConnection aTarget,
      ReentrantReadWriteLock aLock, String aLockName ) {
    return createSynchronizedConnection( aTarget, aLock, false, aLockName );
  }

  /**
   * Создать синхронизированное соединение
   *
   * @param aTarget {@link ISkConnection} защищаемое соединение
   * @param aLock {@link ReentrantReadWriteLock} внешняя блокировка используемая соединением
   * @param aManualStateChange boolean<b>true</b> состояние соединения изменяется в ручную; <b>false</b>автоматическое
   *          изменение состояния.
   * @param aLockName String имя блокировки. Пустая строка - выбирается автоматически
   * @return {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5SynchronizedConnection createSynchronizedConnection( ISkConnection aTarget,
      ReentrantReadWriteLock aLock, boolean aManualStateChange, String aLockName ) {
    TsNullArgumentRtException.checkNulls( aTarget, aLock, aLockName );
    // 2021-06-23 mvk
    // if( aTarget.state() != ESkConnState.CLOSED ) {
    // S5Lockable.addLockableIfNotExistToPool( aTarget.mainLock(), aTarget.sessionInfo().strid() );
    // }
    // Имя блокировки для идентификации при отладке
    String lockName = aLockName;
    if( lockName.length() == 0 ) {
      // Имя не задано
      lockName = (aTarget.state() != ESkConnState.CLOSED ? //
          aTarget.backendInfo().id() : "ClosedConnection") //$NON-NLS-1$
          + String.valueOf( instanceCounter++ );
    }
    S5Lockable.addLockableIfNotExistToPool( aLock, lockName );
    return new S5SynchronizedConnection( aTarget, aLock, aManualStateChange );
  }

  /**
   * Добавляет синхронизированную службу в {@link ISkCoreApi}
   *
   * @param aService {@link S5SynchronizedService} синхронизированная служба
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException соединение не активно
   */
  public void addService( S5SynchronizedService<?> aService ) {
    TsNullArgumentRtException.checkNull( aService );
    lockWrite( this );
    try {
      TsIllegalStateRtException.checkTrue( target().state() == ESkConnState.CLOSED );
      coreApi.addService( aService );
    }
    finally {
      unlockWrite( this );
    }
  }

  /**
   * Возвращает НЕзащищенную службу соединения
   * <p>
   * Используется в конструкторах защищенных служб для доступа к незащищенным службам
   *
   * @param aServiceId String идентификатор службы
   * @return {@link ISkService} незащищенная служба
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemNotFoundRtException служба не найдена
   */
  public ISkService getUnsynchronizedService( String aServiceId ) {
    TsNullArgumentRtException.checkNull( aServiceId );
    return target().coreApi().services().getByKey( aServiceId );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnection
  //
  @Override
  public ESkConnState state() {
    // 2020-09-15 mvk чтение состояния допустимо (по реализации) без блокировки ???
    // tryLock( this );
    // try {
    if( manualState != null ) {
      return manualState;
    }
    return target().state();
    // }
    // finally {
    // unlock( this );
    // }
  }

  @Override
  public void open( ITsContextRo aArgs ) {
    lockWrite( this );
    try {
      ITsContext ctx = createContextForConnection( aArgs );
      // Размещение в контексте блокировки доступа к соединению
      ctx.put( IS5ConnectionParams.REF_CONNECTION_LOCK.refKey(), lock() );
      // 2021-03-29 mvk ---
      // S5Lockable.addLockableIfNotExistToPool( target().mainLock(), target().sessionInfo().strid() );
      target().open( ctx );
      // 2021-03-29 mvk +++
      // 2022-06-15 mvk---
      // S5Lockable.addLockableIfNotExistToPool( target().mainLock(), target().sessionInfo().strid() );
      coreApi = new S5SynchronizedCoreApi( target().coreApi(), nativeLock() );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void close() {
    lockWrite( this );
    try {
      if( manualState != null ) {
        changeState( ESkConnState.CLOSED );
      }
      target().close();
      S5Lockable.removeLockableFromPool( nativeLock() );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkCoreApi coreApi() {
    lockWrite( this );
    try {
      TsIllegalStateRtException.checkTrue( target().state() == ESkConnState.CLOSED );
      return coreApi;
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkBackendInfo backendInfo() {
    lockWrite( this );
    try {
      return target().backendInfo();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addConnectionListener( ISkConnectionListener aListener ) {
    lockWrite( this );
    try {
      // Регистрация слушателя при автоматическом изменении состояния
      if( manualState == null ) {
        target().addConnectionListener( aListener );
      }
      if( !listeners.hasElem( aListener ) ) {
        listeners.add( aListener );
      }
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeConnectionListener( ISkConnectionListener aListener ) {
    lockWrite( this );
    try {
      // Дерегистрация слушателя при автоматическом изменении состояния
      if( manualState == null ) {
        target().removeConnectionListener( aListener );
      }
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsContext scope() {
    lockWrite( this );
    try {
      return target().scope();
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  private static ITsContext createContextForConnection( ITsContextRo aContext ) {
    ITsContext ctx = new TsContext( new IAskParent() {

      @Override
      public IAtomicValue findOp( String aId ) {
        return aContext.params().findByKey( aId );
      }

      @Override
      public Object findRef( String aKey ) {
        return aContext.find( aKey );
      }

    } );
    ctx.params().setAll( aContext.params() );
    return ctx;
  }
}
