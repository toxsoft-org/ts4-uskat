package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.utils.ITemporalsHistory;

/**
 * Синхронизация доступа к {@link ISkCommandService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedCommandService
    extends S5SynchronizedService<ISkCommandService>
    implements ISkCommandService {

  private final S5SynchronizedEventer<ISkCommandServiceListener> eventer;
  private final S5SynchronizedTemporalsHistory<ISkCommand>       history;
  private final IMapEdit<ISkCommandExecutor, IGwidList>          executors = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedCommandService( S5SynchronizedConnection aConnection ) {
    this( (ISkCommandService)aConnection.getUnsynchronizedService( SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkCommandService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedCommandService( ISkCommandService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    history = new S5SynchronizedTemporalsHistory<>( target().history(), nativeLock() );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkCommandService aPrevTarget, ISkCommandService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    history.changeTarget( aNewTarget.history(), aNewLock );
    for( ISkCommandExecutor executor : executors.keys() ) {
      aNewTarget.registerExecutor( executor, executors.getByKey( executor ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkCommandService
  //
  @Override
  public ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    lockWrite( this );
    try {
      return target().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void registerExecutor( ISkCommandExecutor aExecutor, IGwidList aCmdGwids ) {
    lockWrite( this );
    try {
      target().registerExecutor( aExecutor, aCmdGwids );
      executors.put( aExecutor, aCmdGwids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList getExcutableCommandGwids() {
    lockWrite( this );
    try {
      return target().getExcutableCommandGwids();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void unregisterExecutor( ISkCommandExecutor aExecutor ) {
    lockWrite( this );
    try {
      target().unregisterExecutor( aExecutor );
      executors.removeByKey( aExecutor );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    lockWrite( this );
    try {
      target().changeCommandState( aStateChangeInfo );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITemporalsHistory<ISkCommand> history() {
    return history;
  }

  @Override
  public ITsEventer<ISkCommandServiceListener> eventer() {
    return eventer;
  }
}
