package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * Синхронизация доступа к {@link ISkCommandService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedCommandService
    extends S5SynchronizedService<ISkCommandService>
    implements ISkCommandService {

  private final S5SynchronizedGenericChangeEventer      eventer;
  private final IMapEdit<ISkCommandExecutor, IGwidList> executors = new ElemMap<>();

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
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.globallyHandledGwidsEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkCommandService aPrevTarget, ISkCommandService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.globallyHandledGwidsEventer(), aNewLock );
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
      ISkCommand cmd = target().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
      S5SynchronizedCommand retValue = new S5SynchronizedCommand( cmd, nativeLock() );
      return retValue;
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
  public ITimedList<IDtoCompletedCommand> queryObjCommands( IQueryInterval aInterval, Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().queryObjCommands( aInterval, aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    lockWrite( this );
    try {
      return target().listGloballyHandledCommandGwids();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGenericChangeEventer globallyHandledGwidsEventer() {
    return eventer;
  }
}
