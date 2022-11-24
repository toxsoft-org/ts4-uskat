package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.clobserv.*;

/**
 * Синхронизация доступа к {@link ISkClobService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedClobService
    extends S5SynchronizedService<ISkClobService>
    implements ISkClobService {

  private final S5SynchronizedEventer<ITsEventer<ISkClobServiceListener>, ISkClobServiceListener> eventer;
  private final S5SynchronizedValidationSupport<ISkClobServiceValidator>                          svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedClobService( S5SynchronizedConnection aConnection ) {
    this( (ISkClobService)aConnection.getUnsynchronizedService( ISkClobService.SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkClobService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedClobService( ISkClobService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkClobService aPrevTarget, ISkClobService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkClobService
  //
  @Override
  public String readClob( Gwid aGwid ) {
    lockWrite( this );
    try {
      return target().readClob( aGwid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    lockWrite( this );
    try {
      target().writeClob( aGwid, aClob );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsValidationSupport<ISkClobServiceValidator> svs() {
    return svs;
  }

  @Override
  public ITsEventer<ISkClobServiceListener> eventer() {
    return eventer;
  }

}
