package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeListener;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Синхронизация доступа к {@link IGenericChangeEventer} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedGenericChangeEventer
    extends S5SynchronizedResource<IGenericChangeEventer>
    implements IGenericChangeEventer {

  /**
   * Конструктор
   *
   * @param aTarget {@link IGenericChangeEventer} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedGenericChangeEventer( IGenericChangeEventer aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( IGenericChangeEventer aPrevTarget, IGenericChangeEventer aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventer
  //
  @Override
  public void addListener( IGenericChangeListener aListener ) {
    lockWrite( this );
    try {
      target().addListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeListener( IGenericChangeListener aListener ) {
    lockWrite( this );
    try {
      target().removeListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void muteListener( IGenericChangeListener aListener ) {
    lockWrite( this );
    try {
      target().muteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void unmuteListener( IGenericChangeListener aListener ) {
    lockWrite( this );
    try {
      target().unmuteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void pauseFiring() {
    lockWrite( this );
    try {
      target().pauseFiring();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void resumeFiring( boolean aFireDelayed ) {
    lockWrite( this );
    try {
      target().resumeFiring( aFireDelayed );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isFiringPaused() {
    lockWrite( this );
    try {
      return target().isFiringPaused();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isPendingEvents() {
    lockWrite( this );
    try {
      return target().isPendingEvents();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void resetPendingEvents() {
    lockWrite( this );
    try {
      target().resetPendingEvents();
    }
    finally {
      unlockWrite( this );
    }
  }

}
