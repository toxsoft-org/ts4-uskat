package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommand;
import org.toxsoft.uskat.core.api.cmdserv.SkCommandState;

/**
 * Синхронизация доступа к {@link ISkCommand} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedCommand
    extends S5SynchronizedResource<ISkCommand>
    implements ISkCommand {

  private final S5SynchronizedTimedList<SkCommandState> history;
  private final S5SynchronizedGenericChangeEventer      eventer;

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkCommand} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedCommand( ISkCommand aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    history = new S5SynchronizedTimedList<>( aTarget.statesHistory(), aLock );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.stateEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkCommand aPrevTarget, ISkCommand aNewTarget, ReentrantReadWriteLock aNewLock ) {
    history.changeTarget( aNewTarget.statesHistory(), aNewLock );
    eventer.changeTarget( aNewTarget.stateEventer(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkCommand
  //
  @Override
  public long timestamp() {
    lockWrite( this );
    try {
      return target().timestamp();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public int compareTo( ISkCommand aO ) {
    lockWrite( this );
    try {
      return target().compareTo( aO );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public String instanceId() {
    lockWrite( this );
    try {
      return target().instanceId();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public Gwid cmdGwid() {
    lockWrite( this );
    try {
      return target().cmdGwid();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public Skid authorSkid() {
    lockWrite( this );
    try {
      return target().authorSkid();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IOptionSet argValues() {
    lockWrite( this );
    try {
      return target().argValues();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITimedList<SkCommandState> statesHistory() {
    return history;
  }

  @Override
  public IGenericChangeEventer stateEventer() {
    return eventer;
  }

}
