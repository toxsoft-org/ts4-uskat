package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.txtmatch.TextMatcher;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Синхронизация доступа к {@link ISkObjectService} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedObjectService
    extends S5SynchronizedService<ISkObjectService>
    implements ISkObjectService {

  private final S5SynchronizedEventer<ISkObjectServiceListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkObjectServiceValidator> svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedObjectService( S5SynchronizedConnection aConnection ) {
    this( (ISkObjectService)aConnection.getUnsynchronizedService( ISkObjectService.SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkObjectService} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedObjectService( ISkObjectService aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkObjectService aPrevTarget, ISkObjectService aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkObjectService
  //
  @Override
  public <T extends ISkObject> T find( Skid aSkid ) {
    lockWrite( this );
    try {
      return target().find( aSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public <T extends ISkObject> T get( Skid aSkid ) {
    lockWrite( this );
    try {
      return target().get( aSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkidList listSkids( String aClassId, boolean aIncludeDescendants ) {
    lockWrite( this );
    try {
      return target().listSkids( aClassId, aIncludeDescendants );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public <T extends ISkObject> IList<T> listObjs( String aClassId, boolean aIncludeDescendants ) {
    lockWrite( this );
    try {
      return target().listObjs( aClassId, aIncludeDescendants );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IList<ISkObject> getObjs( ISkidList aSkids ) {
    lockWrite( this );
    try {
      return target().getObjs( aSkids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public <T extends ISkObject> T defineObject( IDtoObject aDtoObject ) {
    lockWrite( this );
    try {
      return target().defineObject( aDtoObject );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeObject( Skid aSkid ) {
    lockWrite( this );
    try {
      target().removeObject( aSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeObjects( ISkidList aSkids ) {
    lockWrite( this );
    try {
      target().removeObjects( aSkids );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
    lockWrite( this );
    try {
      target().registerObjectCreator( aClassId, aCreator );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkidList getRivetRev( String aClassId, String aRivetId, Skid aRightSkid ) {
    lockWrite( this );
    try {
      return target().getRivetRev( aClassId, aRivetId, aRightSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IMap<Gwid, ISkidList> getAllRivetsRev( Skid aRightSkid ) {
    lockWrite( this );
    try {
      return target().getAllRivetsRev( aRightSkid );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
    lockWrite( this );
    try {
      target().registerObjectCreator( aRule, aCreator );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkObjectServiceListener> eventer() {
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkObjectServiceValidator> svs() {
    return svs;
  }
}
