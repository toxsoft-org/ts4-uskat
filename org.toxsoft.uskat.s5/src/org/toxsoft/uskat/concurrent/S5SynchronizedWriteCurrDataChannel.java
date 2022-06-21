package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.rtdserv.ISkWriteCurrDataChannel;

/**
 * Синхронизация доступа к {@link ISkWriteCurrDataChannel} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedWriteCurrDataChannel
    extends S5SynchronizedRtdataChannel<ISkWriteCurrDataChannel>
    implements ISkWriteCurrDataChannel {

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedRtDataService} служба-собственник канала
   * @param aTarget {@link ISkWriteCurrDataChannel} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedWriteCurrDataChannel( S5SynchronizedRtDataService aOwner, ISkWriteCurrDataChannel aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aOwner, aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkWriteCurrDataChannel
  //
  @Override
  public void setValue( IAtomicValue aValue ) {
    lockWrite( this );
    try {
      target().setValue( aValue );
    }
    finally {
      unlockWrite( this );
    }
  }
}
