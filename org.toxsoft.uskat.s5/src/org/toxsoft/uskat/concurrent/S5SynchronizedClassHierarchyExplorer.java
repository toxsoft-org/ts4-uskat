package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassHierarchyExplorer;

/**
 * Синхронизация доступа к {@link ITsValidationSupport} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedClassHierarchyExplorer
    extends S5SynchronizedResource<ISkClassHierarchyExplorer>
    implements ISkClassHierarchyExplorer {

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkClassHierarchyExplorer} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedClassHierarchyExplorer( ISkClassHierarchyExplorer aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkClassHierarchyExplorer aPrevTarget, ISkClassHierarchyExplorer aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkClassHierarchyExplorer
  //
  @Override
  public boolean isSuperclassOf( String aClassId, String aSubclassId ) {
    lockWrite( this );
    try {
      return target().isSuperclassOf( aClassId, aSubclassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isAssignableFrom( String aClassId, String aSubclassId ) {
    lockWrite( this );
    try {
      return target().isAssignableFrom( aClassId, aSubclassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isSubclassOf( String aClassId, String aSuperclassId ) {
    lockWrite( this );
    try {
      return target().isSubclassOf( aClassId, aSuperclassId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isAssignableTo( String aClassId, String aSuperclassId ) {
    lockWrite( this );
    try {
      return target().isAssignableTo( aClassId, aSuperclassId );
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
  public String findCommonRootClassId( IStringList aClassIds ) {
    lockWrite( this );
    try {
      return target().findCommonRootClassId( aClassIds );
    }
    finally {
      unlockWrite( this );
    }
  }
}
