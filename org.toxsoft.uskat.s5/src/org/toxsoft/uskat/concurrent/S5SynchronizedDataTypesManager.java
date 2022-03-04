package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.core.api.sysdescr.*;

/**
 * Синхронизация доступа к {@link ISkDataTypesManager} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedDataTypesManager
    extends S5SynchronizedResource<ISkDataTypesManager>
    implements ISkDataTypesManager {

  private final S5SynchronizedEventer<ISkDataTypesManagerListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkDataTypesManagerValidator> svs;

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkDataTypesManager} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedDataTypesManager( ISkDataTypesManager aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkDataTypesManager aPrevTarget, ISkDataTypesManager aNewTarget,
      ReentrantReadWriteLock aNewLock ) {
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkDataTypesManager
  //
  @Override
  @SuppressWarnings( "deprecation" )
  public IStringMap<IDataDef> types() {
    lockWrite( this );
    try {
      return target().types();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IStridablesList<IDataDef> typeDefs() {
    lockWrite( this );
    try {
      return target().typeDefs();
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IDataDef findType( String aTypeId ) {
    lockWrite( this );
    try {
      return target().findType( aTypeId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IDataDef getType( String aTypeId ) {
    lockWrite( this );
    try {
      return target().getType( aTypeId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public boolean isBuiltinType( String aTypeId ) {
    lockWrite( this );
    try {
      return target().isBuiltinType( aTypeId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public IDataDef defineType( IDpuSdTypeInfo aDpuTypeInfo ) {
    lockWrite( this );
    try {
      return target().defineType( aDpuTypeInfo );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public void removeType( String aTypeId ) {
    lockWrite( this );
    try {
      target().removeType( aTypeId );
    }
    finally {
      unlockWrite( this );
    }
  }

  @Override
  public ITsEventer<ISkDataTypesManagerListener> eventer() {
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkDataTypesManagerValidator> svs() {
    return svs;
  }
}
