package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.sysdescr.*;

/**
 * Синхронизация доступа к {@link ISkSysdescr} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedSysdescrService
    extends S5SynchronizedService<ISkSysdescr>
    implements ISkSysdescr {

  private final S5SynchronizedDataTypesManager dataTypesManager;
  private final S5SynchronizedClassInfoManager classInfoManager;
  private final S5SynchronizedGwidManager      gwidManager;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5SynchronizedConnection} защищенное соединение
   * @throws TsNullArgumentRtException аругмент = null
   * @throws TsItemNotFoundRtException в соединении не найдена служба которую необходимо защитить
   */
  public S5SynchronizedSysdescrService( S5SynchronizedConnection aConnection ) {
    this( (ISkSysdescr)aConnection.getUnsynchronizedService( ISkSysdescr.SERVICE_ID ), aConnection.mainLock() );
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
    dataTypesManager = new S5SynchronizedDataTypesManager( aTarget.dataTypesManager(), aLock );
    classInfoManager = new S5SynchronizedClassInfoManager( aTarget.classInfoManager(), aLock );
    gwidManager = new S5SynchronizedGwidManager( aTarget.gwidManager(), aLock );
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkSysdescr aPrevTarget, ISkSysdescr aNewTarget, ReentrantReadWriteLock aNewLock ) {
    dataTypesManager.changeTarget( aNewTarget.dataTypesManager(), aNewLock );
    classInfoManager.changeTarget( aNewTarget.classInfoManager(), aNewLock );
    gwidManager.changeTarget( aNewTarget.gwidManager(), aNewLock );
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescr
  //
  @Override
  public ISkDataTypesManager dataTypesManager() {
    return dataTypesManager;
  }

  @Override
  public ISkClassInfoManager classInfoManager() {
    return classInfoManager;
  }

  @Override
  public ISkGwidManager gwidManager() {
    return gwidManager;
  }
}
