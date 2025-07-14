package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.devapi.transactions.*;

/**
 * {@link IDtoObjectManager} abstract implementation.
 *
 * @author mvk
 */
abstract public class AbstractDtoObjectManager
    implements IDtoObjectManager {

  private final ITsThreadExecutor                            executor;
  private final IMapEdit<Skid, IDtoObject>                   removingObjs = new ElemMap<>();
  private final IMapEdit<Skid, Pair<IDtoObject, IDtoObject>> updatingObjs = new ElemMap<>();
  private final IMapEdit<Skid, IDtoObject>                   creatingObjs = new ElemMap<>();

  private boolean findInProcess = false;

  /**
   * Constructor.
   */
  public AbstractDtoObjectManager() {
    executor = null;
  }

  /**
   * Constructor.
   *
   * @param aExecutor {@link ITsThreadExecutor} executor
   * @throws TsNullArgumentRtException arg = null
   */
  public AbstractDtoObjectManager( ITsThreadExecutor aExecutor ) {
    executor = TsNullArgumentRtException.checkNull( aExecutor );
  }

  // ------------------------------------------------------------------------------------
  // IDtoObjectManager
  //

  @Override
  public final IDtoObject find( Skid aObjId ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aObjId );
    if( findInProcess ) {
      return null;
    }
    findInProcess = true;
    try {
      if( removingObjs.hasKey( aObjId ) ) {
        return null;
      }
      IDtoObject retValue = creatingObjs.findByKey( aObjId );
      if( retValue != null ) {
        return retValue;
      }
      Pair<IDtoObject, IDtoObject> changedObj = updatingObjs.findByKey( aObjId );
      if( changedObj != null ) {
        return changedObj.right();
      }
      return doLoadFromDb( aObjId );
    }
    finally {
      findInProcess = false;
    }
  }

  @Override
  public final void persist( IDtoObject aDtoObj ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removingObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_REMOVED, objId );
    }
    if( updatingObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    if( creatingObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    IDtoObject dtoObject = doLoadFromDb( objId );
    if( dtoObject != null ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    creatingObjs.put( objId, aDtoObj );

    doPersist( aDtoObj );
  }

  @Override
  public final IDtoObject merge( IDtoObject aDtoObj ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removingObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_REMOVED, objId );
    }
    Pair<IDtoObject, IDtoObject> prevChanged = updatingObjs.findByKey( objId );
    if( prevChanged != null ) {
      updatingObjs.put( objId, new Pair<>( prevChanged.left(), aDtoObj ) );
      doMerge( aDtoObj );
      return aDtoObj;
    }
    if( creatingObjs.hasKey( objId ) ) {
      creatingObjs.put( objId, aDtoObj );
      doMerge( aDtoObj );
      return aDtoObj;
    }
    IDtoObject prevObj = doLoadFromDb( objId );
    if( prevObj == null ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_NOT_FOUND, objId );
    }
    updatingObjs.put( objId, new Pair<>( prevObj, aDtoObj ) );
    doMerge( aDtoObj );

    return aDtoObj;
  }

  @Override
  public final boolean remove( IDtoObject aDtoObj ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removingObjs.hasKey( objId ) ) {
      return false;
    }
    boolean needRemove = false;
    boolean found = false;
    if( !found && updatingObjs.hasKey( objId ) ) {
      updatingObjs.removeByKey( objId );
      needRemove = true;
      found = true;
    }
    if( !found && creatingObjs.hasKey( objId ) ) {
      creatingObjs.removeByKey( objId );
      needRemove = false;
      found = true;
    }
    if( !found ) {
      IDtoObject dtoObject = doLoadFromDb( objId );
      if( dtoObject != null ) {
        needRemove = true;
        found = true;
      }
    }
    if( needRemove ) {
      removingObjs.put( objId, aDtoObj );
    }
    doRemove( aDtoObj );

    return needRemove;
  }

  @Override
  public final boolean wasRemoved( Skid aSkid ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aSkid );
    return (removingObjs.hasKey( aSkid ));
  }

  // ------------------------------------------------------------------------------------
  // public API
  //

  /**
   * Returns map of the removing objects.
   *
   * @return {@link IMap}&lt;{@link Skid}, {@link IDtoObject}&gt; map of the removing object.
   */
  public final IMap<Skid, IDtoObject> removingObjs() {
    return removingObjs;
  }

  /**
   * Returns map of the updating objects.
   *
   * @return {@link IMap}&lt;{@link Skid},{@link IDtoObject}&gt; map of the updating object.<br>
   *         key: {@link IDtoObject#skid()};<br>
   *         value: {@link Pair#left()} is the prev object state, {@link Pair#right()} is the new object state.
   */
  public final IMap<Skid, Pair<IDtoObject, IDtoObject>> updatingObjs() {
    return updatingObjs;
  }

  /**
   * Returns map of the creating objects.
   *
   * @return {@link IMap}&lt;{@link Skid}, {@link IDtoObject}&gt; map of the creating object.
   */
  public final IMap<Skid, IDtoObject> creatingObjs() {
    return creatingObjs;
  }

  // ------------------------------------------------------------------------------------
  // abstract methods
  //

  /**
   * Load object from persistent storage.
   *
   * @param aObjId {@link Skid} object id.
   * @return {@link IDtoObject} the loaded object or null if obj is not found.
   */
  abstract IDtoObject doLoadFromDb( Skid aObjId );

  // ------------------------------------------------------------------------------------
  // methods for implementation
  //

  @SuppressWarnings( "unused" )
  protected void doPersist( IDtoObject aDtoObj ) {
    // nop
  }

  @SuppressWarnings( "unused" )
  protected void doMerge( IDtoObject aDtoObj ) {
    // nop
  }

  @SuppressWarnings( "unused" )
  protected void doRemove( IDtoObject aDtoObj ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //

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
