package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.lobs.ISkLobService;
import ru.uskat.legacy.IdPair;

/**
 * Синхронизация доступа к {@link ISkLobService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedLobService
    extends S5SynchronizedService<ISkLobService>
    implements ISkLobService {

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedLobService( S5SynchronizedConnection aConnection ) {
    this( (ISkLobService)aConnection.getUnsynchronizedService( ISkLobService.SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkLobService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedLobService( ISkLobService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkLobService aPrevTarget, ISkLobService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkLobService
  //
  @Override
  public IList<IdPair> listILobIds() {
    lockWrite( this );
    try {
      return target().listILobIds();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean hasLob( IdPair aId ) {
    lockWrite( this );
    try {
      return target().hasLob( aId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean writeClob( IdPair aId, String aValue ) {
    lockWrite( this );
    try {
      return target().writeClob( aId, aValue );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
    lockWrite( this );
    try {
      return target().copyClob( aSourceId, aDestId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public String readClob( IdPair aId ) {
    lockWrite( this );
    try {
      return target().readClob( aId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean removeLob( IdPair aId ) {
    lockWrite( this );
    try {
      return target().removeLob( aId );
    }
    finally {
      unlockWrite( this );
    }
  }
}
