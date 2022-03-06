package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Синхронизация доступа к {@link ITsValidationSupport} (декоратор)
 *
 * @author mvk
 * @param <V> - конкретный интерфейс валидатора
 */
public final class S5SynchronizedValidationSupport<V>
    extends S5SynchronizedResource<ITsValidationSupport<V>>
    implements ITsValidationSupport<V> {

  private final IListEdit<V> validators = new ElemLinkedList<>();

  /**
   * Конструктор
   *
   * @param aTarget {@link ITsValidationSupport} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedValidationSupport( ITsValidationSupport<V> aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ITsValidationSupport<V> aPrevTarget, ITsValidationSupport<V> aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // Регистрация валидаторов в новом соединении
    for( V validator : validators ) {
      aNewTarget.addValidator( validator );
    }
  }

  // ------------------------------------------------------------------------------------
  // IServiceValidationSupport
  //
  @Override
  public V validator() {
    lockWrite( this );
    try {
      return target().validator();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addValidator( V aValidator ) {
    lockWrite( this );
    try {
      target().addValidator( aValidator );
      if( !validators.hasElem( aValidator ) ) {
        validators.add( aValidator );
      }
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeValidator( V aValidator ) {
    lockWrite( this );
    try {
      target().removeValidator( aValidator );
      validators.remove( aValidator );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void pauseValidator( V aValidator ) {
    lockWrite( this );
    try {
      target().pauseValidator( aValidator );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void resumeValidator( V aValidator ) {
    lockWrite( this );
    try {
      target().resumeValidator( aValidator );
    }
    finally {
      unlockWrite( this );
    }
  }
}
