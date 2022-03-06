package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.users.*;

/**
 * Синхронизация доступа к {@link ISkUserService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedUserService
    extends S5SynchronizedService<ISkUserService>
    implements ISkUserService {

  private final S5SynchronizedValidationSupport<ISkUserServiceValidator> svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedUserService( S5SynchronizedConnection aConnection ) {
    this( (ISkUserService)aConnection.getUnsynchronizedService( ISkUserService.SERVICE_ID ), aConnection.mainLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkUserService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedUserService( ISkUserService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkUserService aPrevTarget, ISkUserService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkUserService
  //
  @Override
  public IStridablesList<ISkUser> list() {
    lockWrite( this );
    try {
      return target().list();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser find( String aLogin ) {
    lockWrite( this );
    try {
      return target().find( aLogin );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser defineUser( IDpuObject aUserDpu ) {
    lockWrite( this );
    try {
      return target().defineUser( aUserDpu );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser disableUser( String aLogin, boolean aDisabled ) {
    lockWrite( this );
    try {
      return target().disableUser( aLogin, aDisabled );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser setPassword( String aLogin, String aPassword ) {
    lockWrite( this );
    try {
      return target().setPassword( aLogin, aPassword );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ValidationResult validatePassowrd( String aPassword ) {
    lockWrite( this );
    try {
      return target().validatePassowrd( aPassword );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void deleteUser( String aLogin ) {
    lockWrite( this );
    try {
      target().deleteUser( aLogin );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsValidationSupport<ISkUserServiceValidator> svs() {
    return svs;
  }

}
