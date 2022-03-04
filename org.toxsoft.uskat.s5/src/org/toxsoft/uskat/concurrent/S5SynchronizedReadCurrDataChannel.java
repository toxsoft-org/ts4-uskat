package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.rtdata.ISkReadCurrDataChannel;

/**
 * Синхронизация доступа к {@link ISkReadCurrDataChannel} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedReadCurrDataChannel
    extends S5SynchronizedRtdataChannel<ISkReadCurrDataChannel>
    implements ISkReadCurrDataChannel {

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedRtDataService} служба-собственник канала
   * @param aTarget {@link ISkReadCurrDataChannel} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedReadCurrDataChannel( S5SynchronizedRtDataService aOwner, ISkReadCurrDataChannel aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aOwner, aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkReadCurrDataChannel
  //
  @Override
  public IAtomicValue getValue() {
    lockWrite( this );
    try {
      return target().getValue();
    }
    finally {
      unlockWrite( this );
    }
  }

}
