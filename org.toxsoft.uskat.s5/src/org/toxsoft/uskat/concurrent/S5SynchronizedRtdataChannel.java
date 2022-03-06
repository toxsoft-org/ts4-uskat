package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.rtdata.ISkRtdataChannel;
import ru.uskat.core.api.rtdata.ISkWriteCurrDataChannel;

/**
 * Синхронизация доступа к {@link ISkWriteCurrDataChannel} (декоратор)
 *
 * @author mvk
 * @param <T> тип канала
 */
public class S5SynchronizedRtdataChannel<T extends ISkRtdataChannel>
    extends S5SynchronizedResource<T>
    implements ISkRtdataChannel {

  private final S5SynchronizedRtDataService owner;

  /**
   * Конструктор
   *
   * @param aOwner {@link S5SynchronizedRtDataService} служба-собственник канала
   * @param aTarget T защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedRtdataChannel( S5SynchronizedRtDataService aOwner, T aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    owner = TsNullArgumentRtException.checkNull( aOwner );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( T aPrevTarget, T aNewTarget, ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkRtdataChannel
  //
  @Override
  public final Gwid gwid() {
    return target().gwid();
  }

  @Override
  public final boolean isOk() {
    lockWrite( this );
    try {
      return target().isOk();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public final void close() {
    lockWrite( this );
    try {
      owner.removeChannel( this );
      target().close();
    }
    finally {
      unlockWrite( this );
    }
  }
}
