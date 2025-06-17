package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Object Persistence Manager (similar to EntityManager).
 *
 * @author mvk
 */
abstract public class AbstractSkObjectManager {

  private final IMapEdit<Skid, IDtoObject> removedObjs = new ElemMap<>();
  private final IMapEdit<Skid, IDtoObject> changedObjs = new ElemMap<>();
  private final IMapEdit<Skid, IDtoObject> createdObjs = new ElemMap<>();

  /**
   * Constructor.
   */
  public AbstractSkObjectManager() {
  }

  // ------------------------------------------------------------------------------------
  // public API
  //

  /**
   * Find object.
   *
   * @param aObjId {@link Skid} object id.
   * @return {@link IDtoObject} obj or null
   * @throws TsNullArgumentRtException arg = null
   */
  public final IDtoObject find( Skid aObjId ) {
    TsNullArgumentRtException.checkNull( aObjId );
    if( removedObjs.hasKey( aObjId ) ) {
      return null;
    }
    IDtoObject retValue = createdObjs.findByKey( aObjId );
    if( retValue != null ) {
      return retValue;
    }
    retValue = changedObjs.findByKey( aObjId );
    if( retValue != null ) {
      return retValue;
    }
    return loadFromDb( aObjId );
  }

  /**
   * Persist object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @throws TsNullArgumentRtException arg = null
   * @throws TsIllegalArgumentRtException obj is already been removed
   * @throws TsIllegalArgumentRtException obj is already exist
   */
  public final void persist( IDtoObject aDtoObj ) {
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removedObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_REMOVED, objId );
    }
    if( changedObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    if( createdObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    IDtoObject dtoObject = loadFromDb( objId );
    if( dtoObject != null ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_EXIST, objId );
    }
    createdObjs.put( objId, aDtoObj );
  }

  /**
   * Merge object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @return {@link IDtoObject} merged object.
   * @throws TsNullArgumentRtException arg = null
   * @throws TsIllegalArgumentRtException obj is already been removed
   * @throws TsIllegalArgumentRtException obj is not found
   */
  public final IDtoObject merge( IDtoObject aDtoObj ) {
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removedObjs.hasKey( objId ) ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_ALREADY_REMOVED, objId );
    }
    if( changedObjs.hasKey( objId ) ) {
      changedObjs.put( objId, aDtoObj );
      return aDtoObj;
    }
    if( createdObjs.hasKey( objId ) ) {
      createdObjs.put( objId, aDtoObj );
      return aDtoObj;
    }
    IDtoObject dtoObject = loadFromDb( objId );
    if( dtoObject == null ) {
      throw new TsIllegalArgumentRtException( ERR_OBJ_NOT_FOUND, objId );
    }
    changedObjs.put( objId, aDtoObj );
    return aDtoObj;
  }

  /**
   * Remove object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @return boolean <b>true</b> object was removed; <b>false</b> object is not found.
   * @throws TsNullArgumentRtException arg = null
   */
  public final boolean remove( IDtoObject aDtoObj ) {
    TsNullArgumentRtException.checkNull( aDtoObj );
    Skid objId = aDtoObj.skid();
    if( removedObjs.hasKey( objId ) ) {
      return false;
    }
    boolean needRemove = false;
    boolean found = false;
    if( !found && changedObjs.hasKey( objId ) ) {
      changedObjs.removeByKey( objId );
      needRemove = true;
      found = true;
    }
    if( !found && createdObjs.hasKey( objId ) ) {
      createdObjs.removeByKey( objId );
      needRemove = false;
      found = true;
    }
    if( !found ) {
      IDtoObject dtoObject = loadFromDb( objId );
      if( dtoObject != null ) {
        needRemove = true;
        found = true;
      }
    }
    if( needRemove ) {
      removedObjs.put( objId, aDtoObj );
    }
    return needRemove;
  }

  /**
   * Returns map of the removed objects.
   *
   * @return {@link IMap}&lt;{@link Skid}, {@link IDtoObject}&gt; map of the removed object.
   */
  public final IMap<Skid, IDtoObject> removedObjs() {
    return removedObjs;
  }

  /**
   * Returns map of the changed objects.
   *
   * @return {@link IMap}&lt;{@link Skid}, {@link IDtoObject}&gt; map of the changed object.
   */
  public final IMap<Skid, IDtoObject> changedObjs() {
    return changedObjs;
  }

  /**
   * Returns map of the created objects.
   *
   * @return {@link IMap}&lt;{@link Skid}, {@link IDtoObject}&gt; map of the created object.
   */
  public final IMap<Skid, IDtoObject> createdObjs() {
    return createdObjs;
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
  abstract IDtoObject loadFromDb( Skid aObjId );
}
