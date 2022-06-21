package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;

/**
 * Синхронизация доступа к {@link ISkSysdescr} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedSysdescrService
    extends S5SynchronizedService<ISkSysdescr>
    implements ISkSysdescr {

  private final S5SynchronizedClassHierarchyExplorer                  hierarchy;
  private final S5SynchronizedEventer<ISkSysdescrListener>            eventer;
  private final S5SynchronizedValidationSupport<ISkSysdescrValidator> svs;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedSysdescrService( S5SynchronizedConnection aConnection ) {
    this( (ISkSysdescr)aConnection.getUnsynchronizedService( ISkSysdescr.SERVICE_ID ), aConnection.nativeLock() );
    aConnection.addService( this );
  }

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkSysdescr} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedSysdescrService( ISkSysdescr aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    hierarchy = new S5SynchronizedClassHierarchyExplorer( aTarget.hierarchy(), aLock );
    eventer = new S5SynchronizedEventer<>( aTarget.eventer(), aLock );
    svs = new S5SynchronizedValidationSupport<>( aTarget.svs(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkSysdescr aPrevTarget, ISkSysdescr aNewTarget, ReentrantReadWriteLock aNewLock ) {
    hierarchy.changeTarget( aNewTarget.hierarchy(), aNewLock );
    eventer.changeTarget( aNewTarget.eventer(), aNewLock );
    svs.changeTarget( aNewTarget.svs(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescr
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
  public ISkClassInfo defineClass( IDtoClassInfo aDtoClassInfo ) {
    lockWrite( this );
    try {
      return target().defineClass( aDtoClassInfo );
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
  public ISkClassHierarchyExplorer hierarchy() {
    return hierarchy;
  }

  @Override
  public ITsValidationSupport<ISkSysdescrValidator> svs() {
    return svs;
  }

  @Override
  public ITsEventer<ISkSysdescrListener> eventer() {
    return eventer;
  }
}
