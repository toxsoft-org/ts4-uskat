package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkObjectService} implementation.
 *
 * @author hazard157
 */
class SkObjectService
    extends AbstractSkCoreService
    implements ISkObjectService {

  /**
   * Internal cache of the objects.
   *
   * @author hazard157
   */
  static class ObjsCache {

    private static final int MAX_SIZE = 256 * 1024;

    private final IMapEdit<Skid, SkObject> cache = new ElemMap<>( TsCollectionsUtils.getMapBucketsCount( //
        TsCollectionsUtils.estimateOrder( MAX_SIZE ) ), TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );

    ObjsCache() {
      // nop
    }

    boolean has( Skid aSkid ) {
      return cache.hasKey( aSkid );
    }

    SkObject find( Skid aSkid ) {
      return cache.findByKey( aSkid );
    }

    SkObject put( SkObject aObject ) {
      if( cache.size() >= MAX_SIZE ) {
        cache.removeByKey( cache.keys().first() );
      }
      cache.put( aObject.skid(), aObject );
      return aObject;
    }

    void remove( Skid aSkid ) {
      cache.removeByKey( aSkid );
    }

    void clear() {
      cache.clear();
    }

  }

  /**
   * Objects creators storage.
   *
   * @author hazard157
   */
  static class ObjsCreators {

    private final IStringMapEdit<ISkObjectCreator<?>>        objCreatorsByIds   = new StringMap<>( 157 );
    private final IMapEdit<TextMatcher, ISkObjectCreator<?>> objCreatorsByRules = new ElemMap<>( 157, 32 );

    // ------------------------------------------------------------------------------------
    // API
    //

    ISkObjectCreator<?> getCreator( String aClassId ) {
      ISkObjectCreator<? extends SkObject> creator = objCreatorsByIds.findByKey( aClassId );
      if( creator == null ) {
        for( TextMatcher rule : objCreatorsByRules.keys() ) {
          if( rule.match( aClassId ) ) {
            creator = objCreatorsByRules.getByKey( rule );
            break;
          }
        }
      }
      if( creator == null ) {
        creator = SkObject.DEFAULT_CREATOR;
      }
      return creator;
    }

    public void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      StridUtils.checkValidIdPath( aClassId );
      TsItemAlreadyExistsRtException.checkTrue( objCreatorsByIds.hasKey( aClassId ) );
      objCreatorsByIds.put( aClassId, aCreator );
    }

    public void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      TsItemAlreadyExistsRtException.checkTrue( objCreatorsByRules.hasKey( aRule ) );
      objCreatorsByRules.put( aRule, aCreator );
    }

    public void unregisterObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      ISkObjectCreator<?> creator = objCreatorsByIds.findByKey( aClassId );
      if( creator != null && creator != aCreator ) {
        throw new TsIllegalStateRtException();
      }
      objCreatorsByIds.removeByKey( aClassId );
    }

    public void unregisterObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      ISkObjectCreator<?> creator = objCreatorsByRules.findByKey( aRule );
      if( creator != null && creator != aCreator ) {
        throw new TsIllegalStateRtException();
      }
      objCreatorsByRules.removeByKey( aRule );
    }

  }

  final ObjsCache    objsCache    = new ObjsCache();
  final ObjsCreators objsCreators = new ObjsCreators();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkObjectService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doClose() {
    // TODO Auto-generated method stub

  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private SkObject fromDto( IDtoObject aObjectDto, ISkClassInfo aClassInfo ) {
    String classId = aObjectDto.skid().classId();
    ISkObjectCreator<? extends SkObject> creator = objsCreators.getCreator( classId );
    SkObject sko = creator.createObject( aObjectDto.skid() );
    sko.papiSetCoreApi( coreApi() );
    // initialize attributes from aObjectDto or by default values
    for( IDtoAttrInfo ainf : aClassInfo.attrs().list() ) {
      if( !isSkSysAttrId( ainf.id() ) ) {
        IAtomicValue av = aObjectDto.attrs().findValue( ainf.id() );
        if( av == null ) {
          av = defaultValue( ainf );
        }
        sko.attrs().setValue( ainf, av );
      }
    }
    // TODO initialize rivets from aObjectDto or by Skid.NONE
    return sko;
  }

  // ------------------------------------------------------------------------------------
  // ISkObjectService
  //

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> T find( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    coreApi().papiCheckIsOpen();
    SkObject sko = objsCache.find( aSkid );
    if( sko != null ) {
      return (T)sko;
    }
    IDtoObject dpu = coreApi().l10n().l10nObject( ba().baObjects().findObject( aSkid ) );
    if( dpu != null ) {
      ISkClassInfo cInfo = coreApi().sysdescr().getClassInfo( aSkid.classId() );
      sko = fromDto( dpu, cInfo );
      return (T)objsCache.put( sko );
    }
    return null;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> T get( Skid aSkid ) {
    ISkObject sko = find( aSkid );
    TsItemNotFoundRtException.checkNull( sko, FMT_ERR_NO_SUCH_OBJ, aSkid.toString() );
    return (T)sko;
  }

  @Override
  public ISkidList listSkids( String aClassId, boolean aIncludeSubclasses ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkObjList listObjs( String aClassId, boolean aIncludeSubclasses ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkObjList getObjs( ISkidList aSkids ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends ISkObject> T defineObject( IDtoObject aDtoObject ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkObjList defineObjects( IList<IDtoObject> aDtoObjects ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeObject( Skid aSkid ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeObjects( ISkidList aSkids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITsValidationSupport<ISkObjectServiceValidator> svs() {
    // TODO Auto-generated method stub
    return null;
  }

}
