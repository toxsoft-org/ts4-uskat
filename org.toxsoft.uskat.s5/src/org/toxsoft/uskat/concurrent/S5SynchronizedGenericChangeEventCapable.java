package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventCapable;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Синхронизация доступа к {@link IGenericChangeEventCapable} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedGenericChangeEventCapable
    extends S5SynchronizedResource<IGenericChangeEventCapable>
    implements IGenericChangeEventCapable {

  private final S5SynchronizedGenericChangeEventer eventer;

  /**
   * Конструктор
   *
   * @param aTarget {@link IGenericChangeEventCapable} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedGenericChangeEventCapable( IGenericChangeEventCapable aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedGenericChangeEventer( aTarget.genericChangeEventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( IGenericChangeEventCapable aPrevTarget, IGenericChangeEventCapable aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //
  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return eventer;
  }

}
