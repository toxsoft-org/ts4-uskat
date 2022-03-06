package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.sysdescr.ISkGwidExtensionInfo;
import ru.uskat.core.api.sysdescr.ISkGwidManager;

/**
 * Синхронизация доступа к {@link ISkGwidManager} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedGwidManager
    extends S5SynchronizedResource<ISkGwidManager>
    implements ISkGwidManager {

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkGwidManager} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedGwidManager( ISkGwidManager aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkGwidManager aPrevTarget, ISkGwidManager aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkGwidManager
  //
  @Override
  public boolean isExisting( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().isExisting( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IGwidList expandMultiGwid( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().expandMultiGwid( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IList<ISkGwidExtensionInfo> extensionInfos() {
    lockWrite( this );
    try {
      return target().extensionInfos();
    }
    finally {
      unlockWrite( this );
    }
  }
}
