package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.links.*;

/**
 * Синхронизация доступа к {@link ISkLinkService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedLinkService
    extends S5SynchronizedService<ISkLinkService>
    implements ISkLinkService {

  private final S5SynchronizedEventer<ISkLinkServiceListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkLinkServiceValidator> svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedLinkService( S5SynchronizedConnection aConnection ) {
    this( (ISkLinkService)aConnection.getUnsynchronizedService( ISkLinkService.SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkLinkService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedLinkService( ISkLinkService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkLinkService aPrevTarget, ISkLinkService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkLinkService
  //
  @Override
  public ISkLinkFwd getLink( Skid aLeftSkid, String aLinkId ) {
    lockWrite( this );
    try {
      return target().getLink( aLeftSkid, aLinkId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid ) {
    lockWrite( this );
    try {
      return target().getLinkRev( aClassId, aLinkId, aRightSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void defineLink( Skid aLeftSkid, String aLinkId, ISkidList aRemovedSkids, ISkidList aAddedSkids ) {
    lockWrite( this );
    try {
      target().defineLink( aLeftSkid, aLinkId, aRemovedSkids, aAddedSkids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeLinks( Skid aLeftSkid ) {
    lockWrite( this );
    try {
      target().removeLinks( aLeftSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkLinkServiceListener> eventer() {
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkLinkServiceValidator> svs() {
    return svs;
  }
}
