package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Синхронизация доступа к {@link ITsEventer} (декоратор)
 *
 * @author mvk
 * @param <L> - интерфейс слушателя службы
 */
public final class S5SynchronizedEventer<L>
    extends S5SynchronizedResource<ITsEventer<L>>
    implements ITsEventer<L> {

  private final IListEdit<L> listeners = new ElemLinkedList<>();

  /**
   * Конструктор
   *
   * @param aTarget {@link ITsEventer} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedEventer( ITsEventer<L> aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ITsEventer<L> aPrevTarget, ITsEventer<L> aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // Регистрация слушателей в новом соединении
    for( L listener : listeners ) {
      aNewTarget.addListener( listener );
    }
  }

  // ------------------------------------------------------------------------------------
  // ITsEventer
  //
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

  @Override
  public void addListener( L aListener ) {
    lockWrite( this );
    try {
      target().addListener( aListener );
      if( !listeners.hasElem( aListener ) ) {
        listeners.add( aListener );
      }
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeListener( L aListener ) {
    lockWrite( this );
    try {
      target().removeListener( aListener );
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void muteListener( L aListener ) {
    lockWrite( this );
    try {
      target().muteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void unmuteListener( L aListener ) {
    lockWrite( this );
    try {
      target().unmuteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isListenerMuted( L aListener ) {
    lockWrite( this );
    try {
      return target().isListenerMuted( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

}
