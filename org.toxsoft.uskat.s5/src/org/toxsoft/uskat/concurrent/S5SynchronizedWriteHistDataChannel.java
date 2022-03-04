package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.rtdata.ISkWriteHistDataChannel;

/**
 * Синхронизация доступа к {@link ISkWriteHistDataChannel} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedWriteHistDataChannel
    extends S5SynchronizedRtdataChannel<ISkWriteHistDataChannel>
    implements ISkWriteHistDataChannel {

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedRtDataService} служба-собственник канала
   * @param aTarget {@link ISkWriteHistDataChannel} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedWriteHistDataChannel( S5SynchronizedRtDataService aOwner, ISkWriteHistDataChannel aTarget,
      ReentrantReadWriteLock aLock ) {
    super( aOwner, aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkWriteHistDataChannel
  //
  @Override
  public void writeValues( ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    lockWrite( this );
    try {
      target().writeValues( aInterval, aValues );
    }
    finally {
      unlockWrite( this );
    }
  }
}
