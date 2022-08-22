package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ITsValidator;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoFullObject;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.users.*;

/**
 * Синхронизация доступа к {@link ISkUserService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedUserService
    extends S5SynchronizedService<ISkUserService>
    implements ISkUserService {

  private final S5SynchronizedEventer<ISkUserServiceListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkUserServiceValidator> svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedUserService( S5SynchronizedConnection aConnection ) {
    this( (ISkUserService)aConnection.getUnsynchronizedService( ISkUserService.SERVICE_ID ), aConnection.nativeLock() );
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
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkUserService aPrevTarget, ISkUserService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkUserService
  //
  @Override
  public IStridablesList<ISkUser> listUsers() {
    lockWrite( this );
    try {
      return target().listUsers();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStridablesList<ISkRole> listRoles() {
    lockWrite( this );
    try {
      return target().listRoles();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser findUser( String aUserId ) {
    lockWrite( this );
    try {
      return target().findUser( aUserId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkRole findRole( String aRoleId ) {
    lockWrite( this );
    try {
      return target().findRole( aRoleId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser defineUser( IDtoFullObject aDtoUser ) {
    lockWrite( this );
    try {
      return target().defineUser( aDtoUser );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkRole defineRole( IDtoObject aDtoRole ) {
    lockWrite( this );
    try {
      return target().defineRole( aDtoRole );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeUser( String aLogin ) {
    lockWrite( this );
    try {
      target().removeUser( aLogin );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeRole( String aRoleId ) {
    lockWrite( this );
    try {
      target().removeRole( aRoleId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser setUserEnabled( String aLogin, boolean aEnabled ) {
    lockWrite( this );
    try {
      return target().setUserEnabled( aLogin, aEnabled );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser setUserHidden( String aLogin, boolean aHidden ) {
    lockWrite( this );
    try {
      return target().setUserHidden( aLogin, aHidden );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser setUserPassword( String aLogin, String aPassword ) {
    lockWrite( this );
    try {
      return target().setUserPassword( aLogin, aPassword );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkUser setUserRoles( String aUserId, IStridablesList<ISkRole> aRoles ) {
    lockWrite( this );
    try {
      return target().setUserRoles( aUserId, aRoles );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsValidator<String> passwordValidator() {
    lockWrite( this );
    try {
      return target().passwordValidator();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void addPasswordValidator( ITsValidator<String> aPasswordValidator ) {
    lockWrite( this );
    try {
      target().addPasswordValidator( aPasswordValidator );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsValidationSupport<ISkUserServiceValidator> svs() {
    return svs;
  }

  @Override
  public ITsEventer<ISkUserServiceListener> eventer() {
    return eventer;
  }
}
