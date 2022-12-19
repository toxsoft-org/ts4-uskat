package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.devapi.gwiddb.*;

/**
 * Синхронизация доступа к {@link ISkGwidDbService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedGwidDbService
    extends S5SynchronizedService<ISkGwidDbService>
    implements ISkGwidDbService {

  private final S5SynchronizedEventer<ITsEventer<ISkGwidDbServiceListener>, ISkGwidDbServiceListener> eventer;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedGwidDbService( S5SynchronizedConnection aConnection ) {
    this( (ISkGwidDbService)aConnection.getUnsynchronizedService( ISkGwidDbService.SERVICE_ID ),
        aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkGwidDbService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedGwidDbService( ISkGwidDbService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkGwidDbService aPrevTarget, ISkGwidDbService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkGwidDbService
  //

  @Override
  public IList<IdChain> listSectionIds() {
    lockWrite( this );
    try {
      return target().listSectionIds();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkGwidDbSection defineSection( IdChain aSectionId ) {
    lockWrite( this );
    try {
      return target().defineSection( aSectionId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    lockWrite( this );
    try {
      target().removeSection( aSectionId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkGwidDbServiceListener> eventer() {
    return eventer;
  }

}
