package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.backend.ISkBackendInfo;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.ISkBackend;
import ru.uskat.core.api.ISkService;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.connection.*;

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
   * @param aManualStateChange boolean<b>true</b> состояние соединения изменяется в ручную; <b>false</b>автоматическое
   *          изменение состояния.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private S5SynchronizedConnection( ISkConnection aTarget, boolean aManualStateChange ) {
    super( aTarget, aTarget.mainLock() );
    if( aTarget.state() != ESkConnState.CLOSED ) {
      coreApi = new S5SynchronizedCoreApi( aTarget.coreApi(), lock() );
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
    S5Lockable.removeLockableFromPool( aPrevTarget.mainLock() );
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
      l.beforeSkConnectionStateChanged( this, aNewState );
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
    return createSynchronizedConnection( aTarget, false );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkConnection} защищаемое соединение
   * @param aManualStateChange boolean<b>true</b> состояние соединения изменяется в ручную; <b>false</b>автоматическое
   *          изменение состояния.
   * @return {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5SynchronizedConnection createSynchronizedConnection( ISkConnection aTarget,
      boolean aManualStateChange ) {
    TsNullArgumentRtException.checkNull( aTarget );
    // 2021-06-23 mvk
    // if( aTarget.state() != ESkConnState.CLOSED ) {
    // S5Lockable.addLockableIfNotExistToPool( aTarget.mainLock(), aTarget.sessionInfo().strid() );
    // }
    // Имя блокировки для идентификации при отладке
    String lockName = (aTarget.state() != ESkConnState.CLOSED ? aTarget.sessionInfo().strid()
        : "ClosedConnection" + String.valueOf( instanceCounter++ )); //$NON-NLS-1$
    S5Lockable.addLockableIfNotExistToPool( aTarget.mainLock(), lockName );
    return new S5SynchronizedConnection( aTarget, aManualStateChange );
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
      // 2021-03-29 mvk ---
      // S5Lockable.addLockableIfNotExistToPool( target().mainLock(), target().sessionInfo().strid() );
      target().open( aArgs );
      // 2021-03-29 mvk +++
      S5Lockable.addLockableIfNotExistToPool( target().mainLock(), target().sessionInfo().strid() );
      coreApi = new S5SynchronizedCoreApi( target().coreApi(), lock() );
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
      S5Lockable.removeLockableFromPool( target().mainLock() );
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
  public ISkBackendInfo serverInfo() {
    lockWrite( this );
    try {
      return target().serverInfo();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkSession sessionInfo() {
    lockWrite( this );
    try {
      return target().sessionInfo();
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
  public <T extends ISkBackend> T getBackend() {
    // Метода не должно быть в API ISkConnection
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public ReentrantReadWriteLock mainLock() {
    return target().mainLock();
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
}
