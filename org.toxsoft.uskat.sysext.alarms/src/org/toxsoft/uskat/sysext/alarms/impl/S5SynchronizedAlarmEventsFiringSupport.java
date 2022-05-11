package org.toxsoft.uskat.sysext.alarms.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmEventsFiringSupport;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmServiceListener;

/**
 * Синхронизация доступа к {@link ISkAlarmEventsFiringSupport} (декоратор)
 *
 * @author mvk
 */
public class S5SynchronizedAlarmEventsFiringSupport
    extends S5SynchronizedResource<ISkAlarmEventsFiringSupport>
    implements ISkAlarmEventsFiringSupport {

  private final IMapEdit<ISkAlarmServiceListener, IListEdit<ITsCombiFilterParams>> listeners = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aTarget {@link IServiceEventsFiringSupport} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedAlarmEventsFiringSupport( ISkAlarmEventsFiringSupport aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkAlarmEventsFiringSupport aPrevTarget, ISkAlarmEventsFiringSupport aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // Регистрация слушателей в новом соединении
    for( ISkAlarmServiceListener listener : listeners.keys() ) {
      for( IPolyFilterParams selection : listeners.getByKey( listener ) ) {
        aNewTarget.addListener( listener, selection );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkAlarmEventsFiringSupport
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
  public void addListener( ISkAlarmServiceListener aListener ) {
    lockWrite( this );
    try {
      target().addListener( aListener );
      SkAlarmUtils.addListenerSelection( listeners, aListener, IPolyFilterParams.NULL );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addListener( ISkAlarmServiceListener aListener, IPolyFilterParams aSelection ) {
    lockWrite( this );
    try {
      target().addListener( aListener, aSelection );
      SkAlarmUtils.addListenerSelection( listeners, aListener, aSelection );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeListener( ISkAlarmServiceListener aListener ) {
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
  public void muteListener( ISkAlarmServiceListener aListener ) {
    lockWrite( this );
    try {
      target().muteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void unmuteListener( ISkAlarmServiceListener aListener ) {
    lockWrite( this );
    try {
      target().unmuteListener( aListener );
    }
    finally {
      unlockWrite( this );
    }
  }
}
