package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * Синхронизация доступа к {@link ISkService} (декоратор)
 *
 * @author mvk
 * @param <T> тип службы
 */
public abstract class S5SynchronizedService<T extends ISkService>
    extends S5SynchronizedResource<T>
    implements ISkService {

  /**
   * Конструктор
   *
   * @param aTarget T защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedService( T aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkService
  //
  @Override
  public final String serviceId() {
    lockWrite( this );
    try {
      return target().serviceId();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public final ISkCoreApi coreApi() {
    lockWrite( this );
    try {
      return target().coreApi();
    }
    finally {
      unlockWrite( this );
    }
  }
}
