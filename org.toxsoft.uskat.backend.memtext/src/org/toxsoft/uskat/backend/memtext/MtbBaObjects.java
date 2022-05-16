package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaObjects} implementation.
 *
 * @author hazard157
 */
public class MtbBaObjects
    extends MtbAbstractAddon
    implements IBaObjects {

  private final IStringMapEdit<IMapEdit<Skid, IDtoObject>> objsMap = new StringMap<>( 157 );

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaObjects( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_OBJECTS );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    // nop
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    for( String classId : objsMap.keys() ) {
      StrioUtils.writeKeywordHeader( aSw, classId, true );
      aSw.writeChar( CHAR_ARRAY_BEGIN );
      aSw.incNewLine();
      IList<IDtoObject> ll = objsMap.getByKey( classId ).values();
      for( IDtoObject dtoObj : ll ) {
        DtoObject.writeShortened( aSw, dtoObj );
        if( dtoObj != ll.last() ) {
          aSw.writeSeparatorChar();
          aSw.writeEol();
        }
      }
      aSw.decNewLine();
      aSw.writeChar( CHAR_ARRAY_END );
      if( classId != objsMap.keys().last() ) {
        aSw.writeSeparatorChar();
        aSw.writeEol();
      }
    }
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    // check if no objects in storage
    if( aSr.peekChar( EStrioSkipMode.SKIP_COMMENTS ) == CHAR_SET_END ) {
      objsMap.clear();
      return;
    }
    // read class by class
    IStringMapEdit<IMapEdit<Skid, IDtoObject>> allMap = new StringMap<>( 157 );
    while( true ) {
      String classId = aSr.readIdPath();
      aSr.ensureChar( CHAR_EQUAL );
      IMapEdit<Skid, IDtoObject> map = new ElemMap<>( 4111, TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );
      allMap.put( classId, map );
      if( aSr.readArrayBegin() ) {
        do {
          DtoObject dtoObj = DtoObject.readShortened( aSr, classId );
          map.put( dtoObj.skid(), dtoObj );
        } while( aSr.readArrayNext() );
      }
      if( aSr.peekChar( EStrioSkipMode.SKIP_COMMENTS ) == CHAR_ITEM_SEPARATOR ) {
        aSr.nextChar();
      }
      else {
        break;
      }
    }
    objsMap.setAll( allMap );
  }

  // ------------------------------------------------------------------------------------
  // IBaObjects
  //

  @Override
  public IDtoObject findObject( Skid aSkid ) {
    internalCheck();
    TsNullArgumentRtException.checkNull( aSkid );
    IMap<Skid, IDtoObject> map = objsMap.findByKey( aSkid.classId() );
    if( map != null ) {
      return map.findByKey( aSkid );
    }
    return null;
  }

  @Override
  public IList<IDtoObject> readObjects( IStringList aClassIds ) {
    internalCheck();
    // nothing requested - return an eempty string
    if( aClassIds.isEmpty() ) {
      return IList.EMPTY;
    }
    // optimize frequent queries of objects of one class
    if( aClassIds.size() == 1 ) {
      IMap<Skid, IDtoObject> map = objsMap.findByKey( aClassIds.first() );
      if( map != null ) {
        return map.values();
      }
      return IList.EMPTY;
    }
    // all objects of the requested classes
    IListEdit<IDtoObject> result =
        new ElemLinkedBundleList<>( getListInitialCapacity( estimateOrder( 10_000 ) ), false );
    for( String classId : aClassIds ) {
      IMap<Skid, IDtoObject> map = objsMap.findByKey( classId );
      if( map != null ) {
        result.addAll( map );
      }
    }
    return result;
  }

  @Override
  public IList<IDtoObject> readObjectsByIds( ISkidList aSkids ) {
    internalCheck();
    IListEdit<IDtoObject> result =
        new ElemLinkedBundleList<>( getListInitialCapacity( estimateOrder( aSkids.size() ) ), false );
    for( String classId : aSkids.classIds() ) {
      IMap<Skid, IDtoObject> map = objsMap.findByKey( classId );
      if( map != null ) {
        for( Skid skid : aSkids.listSkidsOfClass( classId ) ) {
          IDtoObject o = map.findByKey( skid );
          if( o != null ) {
            result.add( o );
          }
        }
      }
    }
    return result;
  }

  @Override
  public void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects ) {
    internalCheck();
    TsNullArgumentRtException.checkNull( aUpdateObjects );
    // prepare for frontend message
    ECrudOp eventOp = null;
    Skid eventSkid = null;
    int changesCount = 0;
    // delete objects to be removed
    if( aRemoveSkids != null ) {
      for( Skid skid : aRemoveSkids ) {
        IMapEdit<Skid, IDtoObject> map = objsMap.findByKey( skid.classId() );
        if( map != null ) {
          if( map.removeByKey( skid ) != null ) {
            ++changesCount;
            eventSkid = skid;
            setChanged();
          }
          if( map.isEmpty() ) { // if there are no more objects of class skid.classId(), remove map from objs
            objsMap.removeByKey( skid.classId() );
          }
        }
      }
    }
    else {
      if( !objsMap.isEmpty() ) {
        changesCount = 2; // any value >1 leads to generate ECrudOp.LIST event
        objsMap.clear();
        setChanged();
      }
    }
    // add/update objects
    if( !aUpdateObjects.isEmpty() ) {
      for( IDtoObject obj : aUpdateObjects ) {
        IMapEdit<Skid, IDtoObject> map = objsMap.findByKey( obj.skid().classId() );
        if( map == null ) {
          map = new ElemMap<>( 4111, TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );
          objsMap.put( obj.skid().classId(), map );
        }
        // determie if object will be updated or created
        IDtoObject oldObj = map.findByKey( obj.skid() );
        if( oldObj != null ) {
          // bypass object if it is not changed
          if( !obj.equals( oldObj ) ) {
            continue;
          }
          eventOp = ECrudOp.EDIT;
        }
        else {
          eventOp = ECrudOp.CREATE;
        }
        // update/create object
        map.put( obj.skid(), obj );
        ++changesCount;
        eventSkid = obj.skid();
        setChanged();
      }
    }
    // inform frontend
    switch( changesCount ) {
      case 0: { // no changes, nothing to inform about
        // nop
        break;
      }
      case 1: { // single change causes single object event
        GtMessage msg = IBaObjectsMessages.makeMessage( eventOp, eventSkid );
        owner().frontend().onBackendMessage( msg );
        break;
      }
      default: { // batch changes will fir ECrudOp.LIST event
        GtMessage msg = IBaObjectsMessages.makeMessage( ECrudOp.LIST, null );
        owner().frontend().onBackendMessage( msg );
        break;
      }
    }

  }

}
