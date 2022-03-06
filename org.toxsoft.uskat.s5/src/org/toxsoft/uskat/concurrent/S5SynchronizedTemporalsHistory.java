package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.common.helpers.ITemporalsHistory;

/**
 * Синхронизация доступа к {@link ITemporalsHistory} (декоратор)
 *
 * @author mvk
 * @param <T> - тип исторических данных
 */
public final class S5SynchronizedTemporalsHistory<T extends ITemporal<T>>
    extends S5SynchronizedResource<ITemporalsHistory<T>>
    implements ITemporalsHistory<T> {

  /**
   * Конструктор
   *
   * @param aTarget {@link ITemporalsHistory} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedTemporalsHistory( ITemporalsHistory<T> aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ITemporalsHistory<T> aPrevTarget, ITemporalsHistory<T> aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ITemporalsHistory
  //
  @Override
  public ITimedList<T> query( IQueryInterval aInterval, IGwidList aGwids ) {
    lockWrite( this );
    try {
      return target().query( aInterval, aGwids );
    }
    finally {
      unlockWrite( this );
    }
  }
}
