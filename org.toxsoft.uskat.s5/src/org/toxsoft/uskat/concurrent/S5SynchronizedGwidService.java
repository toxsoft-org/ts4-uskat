package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.gwids.ISkGwidService;
import org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind;

/**
 * Синхронизация доступа к {@link ISkGwidService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedGwidService
    extends S5SynchronizedService<ISkGwidService>
    implements ISkGwidService {

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedGwidService( S5SynchronizedConnection aConnection ) {
    this( (ISkGwidService)aConnection.getUnsynchronizedService( ISkGwidService.SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkGwidService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedGwidService( ISkGwidService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkGwidService aPrevTarget, ISkGwidService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkGwidService
  //
  @Override
  public boolean covers( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind ) {
    lockWrite( this );
    try {
      return target().covers( aGeneral, aTested, aKind );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean coversSingle( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind ) {
    lockWrite( this );
    try {
      return target().coversSingle( aGeneral, aTested, aKind );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean updateGwidsOfIntereset( IListEdit<Gwid> aList, Gwid aToAdd, ESkClassPropKind aKind ) {
    lockWrite( this );
    try {
      return target().updateGwidsOfIntereset( aList, aToAdd, aKind );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean exists( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().exists( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public GwidList expandGwid( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().expandGwid( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

}
