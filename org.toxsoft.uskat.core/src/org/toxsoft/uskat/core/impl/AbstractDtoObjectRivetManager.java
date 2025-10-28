package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.transactions.*;

/**
 * {@link IDtoObjectRivetManager} abstract implementation.
 *
 * @author mvk
 */
public abstract class AbstractDtoObjectRivetManager
    implements IDtoObjectRivetManager {

  private final ITsThreadExecutor executor;
  private final ILogger           logger;

  /**
   * Constructor.
   *
   * @param aLogger {@link ILogger} logger
   * @throws TsNullArgumentRtException any arg = null
   */
  protected AbstractDtoObjectRivetManager( ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aLogger );
    executor = null;
    logger = aLogger;
  }

  /**
   * Constructor.
   *
   * @param aExecutor {@link ITsThreadExecutor} executor
   * @param aLogger {@link ILogger} logger
   * @throws TsNullArgumentRtException any arg = null
   */
  protected AbstractDtoObjectRivetManager( ITsThreadExecutor aExecutor, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aExecutor, aLogger );
    executor = aExecutor;
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // IDtoObjectRivetManager
  //

  @Override
  public final int createRivets( IDtoObject aObj ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aObj );
    int retValue = 0;
    ISkClassInfo classInfo = doGetClassInfo( aObj.classId() );
    Skid leftObjId = aObj.skid();
    IMappedSkids rivets = aObj.rivets();
    ReverseEditor reverseEditor = new ReverseEditor();
    // Необходимо пройти по всем объектам склепок и добавить обратные склепки на добавленный объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      reverseEditor.setRivet( rivetClassId, rivetId );

      ISkidList rightObjIds = rivets.map().getByKey( rivetId );
      for( Skid rightObjId : rightObjIds ) {
        if( rightObjId == Skid.NONE ) {
          continue;
        }
        IDtoObject rightObj = doFindObject( rightObjId );
        if( rightObj == null ) {
          rightObj = doFindObject( rightObjId );
          throw new TsItemNotFoundRtException( ERR_RR_ROBJ_NOT_FOUND, M_CREATE_RR, leftObjId, rivetClassId, rivetId,
              rightObjId );
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = true
        SkidList skidListEdit = reverseEditor.setRightObj( rightObj, true );
        if( skidListEdit.hasElem( leftObjId ) ) {
          logger.error( ERR_RR_ALREADY_EXIST, M_CREATE_RR, rightObjId, rivetId, leftObjId );
          continue;
        }
        skidListEdit.add( leftObjId );
        if( reverseEditor.flush() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  @Override
  public final int removeRivets( IDtoObject aObj ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aObj );
    int retValue = 0;
    ISkClassInfo classInfo = doGetClassInfo( aObj.classId() );
    Skid leftObjId = aObj.skid();
    IMappedSkids rivets = aObj.rivets();
    ReverseEditor reverseEditor = new ReverseEditor();
    // Необходимо пройти по всем объектам склепок и удалить обратные склепки на удаляемый объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      reverseEditor.setRivet( rivetClassId, rivetId );

      ISkidList rightObjIds = rivets.map().getByKey( rivetId );
      for( Skid rightObjId : rightObjIds ) {
        if( rightObjId == Skid.NONE ) {
          continue;
        }
        IDtoObject rightObj = doFindObject( rightObjId );
        if( rightObj == null ) {
          logger.error( ERR_RR_ROBJ_NOT_FOUND, M_REMOVE_RR, leftObjId, rivetClassId, rivetId, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList newSkidList = reverseEditor.setRightObj( rightObj, false );
        if( newSkidList.remove( leftObjId ) < 0 ) {
          logger.error( ERR_RR_LOBJ_NOT_FOUND, M_REMOVE_RR, rightObjId, rivetClassId, rivetId, leftObjId );
          continue;
        }
        if( reverseEditor.flush() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  @Override
  public final int updateRivets( IDtoObject aPrevObj, IDtoObject aNewObj ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aPrevObj, aNewObj );
    TsIllegalArgumentRtException.checkFalse( aPrevObj.skid().equals( aNewObj.skid() ) );
    int retValue = 0;
    ISkClassInfo classInfo = doGetClassInfo( aPrevObj.classId() );
    // Идентфикатор объекта
    Skid leftObjId = aPrevObj.skid();
    // Предыдущее состояние склепок
    IMappedSkids prevRivets = aPrevObj.rivets();
    // Новое состояние склепок
    IMappedSkids newRivets = aNewObj.rivets();
    // Редактор обратных склепок правых объектов
    ReverseEditor reverseEditor = new ReverseEditor();
    // Необходимо пройти по всем объектам склепок и удалить обратные склепки на удаляемый объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      reverseEditor.setRivet( rivetClassId, rivetId );

      ISkidList prevRightObjIds = prevRivets.map().getByKey( rivetId );
      ISkidList newRightObjIds = newRivets.map().getByKey( rivetId );
      // Удаление объектов из склепок
      // 2025-10-28 mvk ---+++
      // for( Skid rightObjId : subtract( new SkidList( prevRightObjIds ), newRightObjIds ) ) {
      for( Skid rightObjId : prevRightObjIds ) {
        if( rightObjId == Skid.NONE ) {
          continue;
        }
        IDtoObject rightObj = doFindObject( rightObjId );
        if( rightObj == null ) {
          logger.error( ERR_RR_ROBJ_NOT_FOUND, M_UPDATE_RR, leftObjId, rivetClassId, rivetId, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList newSkidList = reverseEditor.setRightObj( rightObj, false );
        if( newSkidList.remove( leftObjId ) < 0 ) {
          logger.debug( ERR_RR_LOBJ_NOT_FOUND, M_UPDATE_RR, rightObjId, rivetClassId, rivetId, leftObjId );
          continue;
        }
        if( reverseEditor.flush() ) {
          retValue++;
        }
      }
      // Добавление объектов в склепки
      // 2025-10-28 mvk ---+++
      // for( Skid rightObjId : subtract( new SkidList( newRightObjIds ), prevRightObjIds ) ) {
      for( Skid rightObjId : newRightObjIds ) {
        if( rightObjId == Skid.NONE ) {
          continue;
        }
        IDtoObject rightObj = doFindObject( rightObjId );
        if( rightObj == null ) {
          throw new TsItemNotFoundRtException( ERR_RR_ROBJ_NOT_FOUND, M_UPDATE_RR, leftObjId, rivetClassId, rivetId,
              rightObjId );
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList newSkidList = reverseEditor.setRightObj( rightObj, false );
        if( newSkidList.hasElem( leftObjId ) ) {
          logger.error( ERR_RR_ALREADY_EXIST, M_UPDATE_RR, rightObjId, rivetId, leftObjId );
          continue;
        }
        newSkidList.add( leftObjId );
        if( reverseEditor.flush() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // abstract methods
  //

  protected abstract ISkClassInfo doGetClassInfo( String aClassId );

  protected abstract IDtoObject doFindObject( Skid aObjId );

  protected abstract void doWriteRivetRevs( IDtoObject aObj, IStringMapEdit<IMappedSkids> aRivetRevs );

  // ------------------------------------------------------------------------------------
  // private methods & classes
  //

  /**
   * Редактор ОБРАТНЫХ склепок объекта.
   *
   * @author mvk
   */
  private class ReverseEditor {

    private ISkidList prevSkidList = null;
    private SkidList  newSkidList  = new SkidList();

    private String rivetClassId;
    private String rivetId;

    private IDtoObject obj;

    /**
     * Установка описания редактируемой склепки
     *
     * @param aRivetClassId String класс в котором определяется склепка
     * @param aRivetId String идентификатор склепки
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    void setRivet( String aRivetClassId, String aRivetId ) {
      TsNullArgumentRtException.checkNulls( aRivetClassId, aRivetId );
      rivetClassId = aRivetClassId;
      rivetId = aRivetId;
      prevSkidList = null;
      newSkidList.clear();
    }

    /**
     * Установка объекта для редактирования (правый объект в склепке).
     *
     * @param aRightObj {@link IDtoObject} объект с обратными склепками
     * @param aCreating boolean <b>true</b> редактор нового объекта; редактор существующего объекта.
     * @return {@link SkidList} редактируемый список идентификторов объектов входящих в обратную склепку.
     * @throws TsNullArgumentRtException любой аргумент = null
     * @throws TsIllegalArgumentRtException не установлено описание редактируемой склепки
     */
    SkidList setRightObj( IDtoObject aRightObj, boolean aCreating ) {
      TsNullArgumentRtException.checkNull( aRightObj );
      TsIllegalStateRtException.checkNull( rivetClassId );
      setRivet( rivetClassId, rivetId );
      obj = null;
      // Попытка инициализировать редактор предыдущими значениями
      IStringMap<IMappedSkids> rivetRevs = aRightObj.rivetRevs();
      IMappedSkids mappedSkids = rivetRevs.findByKey( rivetClassId );
      if( mappedSkids == null && !aCreating ) {
        logger.debug( ERR_RR_EDITOR_CLASS_NOT_FOUND, aRightObj.skid(), rivetClassId, rivetId );
      }
      if( mappedSkids != null ) {
        prevSkidList = mappedSkids.map().findByKey( rivetId );
        if( prevSkidList == null && !aCreating ) {
          logger.debug( ERR_RR_EDITOR_RIVET_NOT_FOUND, aRightObj.skid(), rivetClassId, rivetId );
        }
        if( prevSkidList != null ) {
          newSkidList.setAll( prevSkidList );
        }
      }
      obj = aRightObj;

      return newSkidList;
    }

    /**
     * Если необходимо, сохраняет изменения в объекте {@link #setRightObj(IDtoObject, boolean)}.
     *
     * @return boolean <b>true</b> данные были сохранены. <b>false</b> данные не изменились.
     */
    boolean flush() {
      TsIllegalStateRtException.checkNull( obj );
      boolean retValue = false;
      if( !newSkidList.equals( prevSkidList ) ) {
        IStringMapEdit<IMappedSkids> newRivetRevs = new StringMap<>( obj.rivetRevs() );
        MappedSkids mappedSkids = (MappedSkids)newRivetRevs.findByKey( rivetClassId );
        if( mappedSkids == null ) {
          mappedSkids = new MappedSkids();
          newRivetRevs.put( rivetClassId, mappedSkids );
        }
        mappedSkids.map().put( rivetId, new SkidList( newSkidList ) );

        if( newSkidList.size() == 0 ) {
          mappedSkids.map().removeByKey( rivetId );
        }
        if( mappedSkids.map().size() == 0 ) {
          newRivetRevs.removeByKey( rivetClassId );
        }

        doWriteRivetRevs( obj, newRivetRevs );
        retValue = true;
      }
      obj = null;
      return retValue;
    }
  }

  /**
   * @throws TsIllegalStateRtException invalid thread access
   */
  final public void checkThread() {
    Thread currentThread = Thread.currentThread();
    if( executor != null && executor.thread() != currentThread ) {
      String owner = executor.thread().getName();
      String current = Thread.currentThread().getName();
      throw new TsIllegalStateRtException( FMT_ERR_INVALID_THREAD_ACCESS, owner, current );
    }
  }

}
