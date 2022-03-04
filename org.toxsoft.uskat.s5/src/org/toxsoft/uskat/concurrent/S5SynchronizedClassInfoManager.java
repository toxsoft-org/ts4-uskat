package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.txtmatch.TextMatcher;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.core.api.sysdescr.*;

/**
 * Синхронизация доступа к {@link ISkClassInfoManager} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedClassInfoManager
    extends S5SynchronizedResource<ISkClassInfoManager>
    implements ISkClassInfoManager {

  private final S5SynchronizedEventer<ISkClassInfoManagerListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkClassInfoManagerValidator> svs;

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkClassInfoManager} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedClassInfoManager( ISkClassInfoManager aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkClassInfoManager aPrevTarget, ISkClassInfoManager aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkClassInfoManager
  //
  @Override
  public ISkClassInfo findClassInfo( String aClassId ) {
    lockWrite( this );
    try {
      return target().findClassInfo( aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkClassInfo getClassInfo( String aClassId ) {
    lockWrite( this );
    try {
      return target().getClassInfo( aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStridablesList<ISkClassInfo> listClasses() {
    lockWrite( this );
    try {
      return target().listClasses();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isAncestor( String aParentClassId, String aClassId ) {
    lockWrite( this );
    try {
      return target().isAncestor( aParentClassId, aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isOfClass( String aClassId, IStringList aClassIdsList ) {
    lockWrite( this );
    try {
      return target().isOfClass( aClassId, aClassIdsList );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStringList getAllDescendantIds( IStringList aClassIdsList ) {
    lockWrite( this );
    try {
      return target().getAllDescendantIds( aClassIdsList );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkClassHierarchy getHierarchy( String aClassId ) {
    lockWrite( this );
    try {
      return target().getHierarchy( aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ISkClassInfo defineClass( IDpuSdClassInfo aDpuClassInfo ) {
    lockWrite( this );
    try {
      return target().defineClass( aDpuClassInfo );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeClass( String aClassId ) {
    lockWrite( this );
    try {
      target().removeClass( aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkClassInfoManagerListener> eventer() {
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkClassInfoManagerValidator> svs() {
    return svs;
  }

  @Override
  public void claimOnClasses( String aServiceId, TextMatcher aRules ) {
    lockWrite( this );
    try {
      target().claimOnClasses( aServiceId, aRules );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public String getClassOwnerService( String aClassId ) {
    lockWrite( this );
    try {
      return target().getClassOwnerService( aClassId );
    }
    finally {
      unlockWrite( this );
    }
  }
}
