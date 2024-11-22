package org.toxsoft.uskat.s5.common.sysdescr;

import static org.toxsoft.uskat.s5.common.sysdescr.ISkResources.*;

import java.util.concurrent.locks.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Реализация {@link ISkSysdescrReader}
 *
 * @author mvk
 */
public class SkSysdescrReader
    implements ISkSysdescrReader {

  /**
   * Читатель dto-данных
   */
  private final ISkSysdescrDtoReader dtoReader;

  /**
   * Список классов объектов закэшированные читателем
   * <p>
   * Ключ: идентификатор класса;<br>
   * Значение: описание класса
   */
  private IStridablesListEdit<IDtoClassInfo> dtoClassInfos;

  /**
   * Карта классов объектов закэшированные читателем
   * <p>
   * Ключ: идентификатор класса;<br>
   * Значение: описание класса
   */
  private IStringMapEdit<ISkClassInfo> classInfos;

  /**
   * Блокировка доступа к {@link #dtoClassInfos}, {@link #classInfos}
   */
  private final ReentrantReadWriteLock classesLock = new ReentrantReadWriteLock();

  /**
   * Журнал
   */
  private final ILogger logger = LoggerWrapper.getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aSysdescrDpuReader {@link ISkSysdescrDtoReader} dpu-читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public SkSysdescrReader( ISkSysdescrDtoReader aSysdescrDpuReader ) {
    TsNullArgumentRtException.checkNull( aSysdescrDpuReader );
    dtoReader = aSysdescrDpuReader;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Устанавливает описание существующих в системе классов
   *
   * @param aDtoClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; список DPU-описаний классов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void setClassInfos( IStridablesList<IDtoClassInfo> aDtoClassInfos ) {
    TsNullArgumentRtException.checkNull( aDtoClassInfos );
    classesLock.writeLock().lock();
    try {
      dtoClassInfos = new StridablesList<>( aDtoClassInfos );
      classInfos = new StringMap<>();
      IStridablesList<ISkClassInfo> cil = SkCoreUtils.makeHierarchyTreeOfSkClasses( dtoClassInfos );
      for( String classId : cil.ids() ) {
        classInfos.put( classId, cil.getByKey( classId ) );
        logger.info( "setClassInfos(): classInfos added class = %s", classId ); //$NON-NLS-1$
      }
    }
    finally {
      classesLock.writeLock().unlock();
    }
  }

  /**
   * Сбрасывает кэш
   */
  public void invalidateCache() {
    classesLock.writeLock().lock();
    try {
      dtoClassInfos = null;
      classInfos = null;
      logger.info( "invalidateCache(): dtoClassInfos = null, classInfos = null" ); //$NON-NLS-1$
    }
    finally {
      classesLock.writeLock().unlock();
    }
  }

  /**
   * Сбрасывает кэш если необходимо
   *
   * @param aRemoveClassIds {@link IStringList} - идентификаторы удаляемых классов
   * @param aUpdateClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - обновляемые или добавляемые классы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void invalidateCacheIfNeed( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    IStridablesList<IDtoClassInfo> cacheClassInfos = readClassInfos();
    boolean needInvalidate = (aRemoveClassIds.size() > 0);
    // Определение признака необходимости сброса кэша системного описания
    for( IDtoClassInfo updateClassInfo : aUpdateClassInfos ) {
      if( needInvalidate ) {
        break;
      }
      IDtoClassInfo cacheClassInfo = cacheClassInfos.findByKey( updateClassInfo.id() );
      if( cacheClassInfo == null || !cacheClassInfo.equals( updateClassInfo ) ) {
        needInvalidate = true;
      }
    }
    if( needInvalidate ) {
      invalidateCache();
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescrDtoReader
  //
  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    classesLock.readLock().lock();
    try {
      if( dtoClassInfos != null ) {
        return new StridablesList<>( dtoClassInfos );
      }
    }
    finally {
      classesLock.readLock().unlock();
    }
    setClassInfos( dtoReader.readClassInfos() );
    return readClassInfos();
  }

  // ------------------------------------------------------------------------------------
  // ISkSysdescrReader
  //
  @Override
  public ISkClassInfo findClassInfo( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    classesLock.readLock().lock();
    try {
      if( classInfos != null ) {
        return classInfos.findByKey( aClassId );
      }
    }
    finally {
      classesLock.readLock().unlock();
    }
    setClassInfos( dtoReader.readClassInfos() );
    return findClassInfo( aClassId );
  }

  @Override
  public ISkClassInfo getClassInfo( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    ISkClassInfo retValue = findClassInfo( aClassId );
    if( retValue == null ) {
      // Класс не найден
      throw new TsNullArgumentRtException( FMT_ERR_CLASS_NOT_FOUND, aClassId );
    }
    return retValue;
  }

  @Override
  public IStringMap<ISkClassInfo> getClassInfos() {
    classesLock.readLock().lock();
    try {
      if( classInfos != null ) {
        return new StringMap<>( classInfos );
      }
    }
    finally {
      classesLock.readLock().unlock();
    }
    setClassInfos( dtoReader.readClassInfos() );
    return getClassInfos();
  }

  @Override
  public IStringMap<ISkClassInfo> getClassInfos( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    classesLock.readLock().lock();
    try {
      if( classInfos != null ) {
        ISkClassInfo classInfo = findClassInfo( aClassId );
        if( classInfo == null ) {
          return IStringMap.EMPTY;
        }
        IStringMapEdit<ISkClassInfo> retValue = new StringMap<>();
        retValue.put( aClassId, classInfo );
        do {
          classInfo = getClassInfo( classInfo.parentId() );
          retValue.put( classInfo.id(), classInfo );
        } while( !classInfo.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) );
        return retValue;
      }
    }
    finally {
      classesLock.readLock().unlock();
    }
    setClassInfos( dtoReader.readClassInfos() );
    return getClassInfos( aClassId );
  }
}
