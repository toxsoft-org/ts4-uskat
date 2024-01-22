package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.gwids.ISkGwidService;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * {@link ISkGwidService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServGwids
    extends AbstractSkCoreService
    implements ISkGwidService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServGwids::new;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServGwids( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // ISkGwidService
  //

  @Override
  public boolean covers( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNulls( aGeneral, aTested, aKind );
    if( !aTested.isMulti() ) {
      return coversSingle( aGeneral, aTested, aKind );
    }
    // consider only class properties
    if( aGeneral.kind() != EGwidKind.GW_CLASS && aGeneral.kind() != aKind.gwidKind() ) {
      return false;
    }
    if( aTested.kind() != EGwidKind.GW_CLASS && aTested.kind() != aKind.gwidKind() ) {
      return false;
    }
    // tested GWID is not subclass of the general GWID - return false
    if( sysdescr().hierarchy().isAssignableFrom( aGeneral.classId(), aTested.classId() ) ) {
      return false;
    }

    // TODO SkCoreServGwids.covers()

    return false;
  }

  @Override
  public boolean coversSingle( Gwid aGeneral, Gwid aTested, ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNulls( aGeneral, aTested, aKind );
    TsIllegalArgumentRtException.checkTrue( aTested.isMulti() );
    // consider only class properties
    if( aGeneral.kind() != EGwidKind.GW_CLASS && aGeneral.kind() != aKind.gwidKind() ) {
      return false;
    }
    if( aTested.kind() != EGwidKind.GW_CLASS && aTested.kind() != aKind.gwidKind() ) {
      return false;
    }
    // test for the specified object SKID if needed
    if( !aGeneral.isAbstract() && !aGeneral.isStridMulti() ) {
      // GWIDs are of different objects, return false
      if( !aGeneral.skid().equals( aTested.skid() ) ) {
        return false;
      }
    }
    // tested GWID is not subclass of the general GWID - return false
    // TODO: mvk ---+++
    // if( sysdescr().hierarchy().isAssignableFrom( aGeneral.classId(), aTested.classId() ) ) {
    if( !sysdescr().hierarchy().isAssignableFrom( aGeneral.classId(), aTested.classId() ) ) {
      return false;
    }
    // if property ID is specified then check for it
    if( aGeneral.kind() == aKind.gwidKind() && !aGeneral.isPropMulti() ) {
      // GWIDs are of different property IDs, return false
      if( !aGeneral.propId().equals( aTested.propId() ) ) {
        return false;
      }
    }
    // OK, turns out, tested GWID is covered by general one
    return true;
  }

  @Override
  public boolean updateGwidsOfIntereset( IListEdit<Gwid> aList, Gwid aToAdd, ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNulls( aList, aToAdd, aKind );
    // check if list already has more general GWID when new one
    for( Gwid g : aList ) {
      if( covers( g, aToAdd, aKind ) ) {
        return false;
      }
    }
    // remove from list GWIDs covered by new one
    IListEdit<Gwid> toRemove = new ElemArrayList<>( aList.size() );
    for( Gwid g : aList ) {
      if( covers( aToAdd, g, aKind ) ) {
        toRemove.add( g );
      }
    }
    for( Gwid g : toRemove ) {
      aList.remove( g );
    }
    // add a new GWID
    aList.add( aToAdd );
    return true;
  }

  @Override
  public boolean exists( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    // check if class exists
    ISkClassInfo cinf = sysdescr().findClassInfo( aGwid.classId() );
    if( cinf == null ) {
      return false;
    }

    // FIXME multi-GWIDs check?

    // check if class propery/sub-propery exists

    switch( aGwid.kind() ) {
      case GW_CLASS: {
        break;
      }
      case GW_ATTR: {
        if( cinf.attrs().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.attrs().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_CLOB: {
        if( cinf.clobs().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.clobs().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_CMD: {
        if( cinf.cmds().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.cmds().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_CMD_ARG: {
        IDtoCmdInfo cmdInfo = cinf.cmds().list().findByKey( aGwid.propId() );
        if( cmdInfo == null ) {
          return false;
        }
        if( cmdInfo.argDefs().size() > 0 && aGwid.isSubPropMulti() ) {
          break;
        }
        if( !cmdInfo.argDefs().hasKey( aGwid.subPropId() ) ) {
          return false;
        }
        break;
      }
      case GW_EVENT: {
        if( cinf.events().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.events().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_EVENT_PARAM: {
        IDtoEventInfo evInfo = cinf.events().list().findByKey( aGwid.propId() );
        if( evInfo == null ) {
          return false;
        }
        if( evInfo.paramDefs().size() > 0 && aGwid.isSubPropMulti() ) {
          break;
        }
        if( !evInfo.paramDefs().hasKey( aGwid.subPropId() ) ) {
          return false;
        }
        break;
      }
      case GW_LINK: {
        if( cinf.links().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.links().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_RIVET: {
        if( cinf.rivets().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.rivets().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      case GW_RTDATA: {
        if( cinf.rtdata().list().size() > 0 && aGwid.isPropMulti() ) {
          break;
        }
        if( !cinf.rtdata().list().hasKey( aGwid.propId() ) ) {
          return false;
        }
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aGwid.kind().id() );
    }
    // check if object exists
    if( !aGwid.isAbstract() ) {
      if( objServ().find( aGwid.skid() ) == null ) {
        return false;
      }
    }
    return true;
  }

  @Override
  public GwidList expandGwid( Gwid aGwid ) {
    GwidList gl = new GwidList();

    if( aGwid.isMulti() ) {
      ISkidList objIds = expandObjIds( coreApi().objService(), aGwid );
      if( !aGwid.isProp() && !aGwid.isSubProp() ) {
        // aGwid не может быть здесь абстрактным
        TsNullArgumentRtException.checkNull( objIds );
        return expandGwid( gl, aGwid, objIds, null );
      }
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( aGwid.classId() );
      IStringList propIds = expandPropIds( classInfo, aGwid );
      return expandGwid( gl, aGwid, objIds, propIds );
    }
    if( exists( aGwid ) ) {
      gl.add( aGwid );
    }
    return gl;
  }

  private static ISkidList expandObjIds( ISkObjectService aObjService, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aObjService, aGwid );
    if( aGwid.isAbstract() ) {
      return null;
    }
    if( aGwid.isStridMulti() ) {
      // aIncludeSubclasses = true
      return aObjService.listSkids( aGwid.classId(), true );
    }
    return new SkidList( aGwid.skid() );
  }

  private static IStringList expandPropIds( ISkClassInfo aClassInfo, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aGwid );
    IStringListEdit retValue = new StringArrayList();
    switch( aGwid.kind() ) {
      case GW_ATTR:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoAttrInfo info : aClassInfo.attrs().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_RIVET:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoRivetInfo info : aClassInfo.rivets().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_LINK:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoLinkInfo info : aClassInfo.links().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_CLOB:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoClobInfo info : aClassInfo.clobs().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_CMD:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoCmdInfo info : aClassInfo.cmds().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_EVENT:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoEventInfo info : aClassInfo.events().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_RTDATA:
        if( !aGwid.isPropMulti() ) {
          retValue.add( aGwid.propId() );
          break;
        }
        for( IDtoRtdataInfo info : aClassInfo.rtdata().list() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_CMD_ARG:
        if( !aGwid.isSubPropMulti() ) {
          retValue.add( aGwid.subPropId() );
          break;
        }
        for( IDataDef info : aClassInfo.cmds().list().getByKey( aGwid.propId() ).argDefs() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_EVENT_PARAM:
        if( !aGwid.isSubPropMulti() ) {
          retValue.add( aGwid.subPropId() );
          break;
        }
        for( IDataDef info : aClassInfo.events().list().getByKey( aGwid.propId() ).paramDefs() ) {
          retValue.add( info.id() );
        }
        break;
      case GW_CLASS:
        throw new TsInternalErrorRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();

    }
    return retValue;
  }

  private static GwidList expandGwid( GwidList aGwids, Gwid aGwid, ISkidList aObjIds, IStringList aPropIds ) {
    TsNullArgumentRtException.checkNulls( aGwids, aGwid );
    String classId = aGwid.classId();
    // Формирование результата
    switch( aGwid.kind() ) {
      case GW_CLASS:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            aGwids.add( Gwid.createObj( objId ) );
          }
          break;
        }
        aGwids.add( aGwid );
        break;
      case GW_ATTR:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createAttr( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createAttr( classId, id ) );
        }
        break;
      case GW_RIVET:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createRivet( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createRivet( classId, id ) );
        }
        break;
      case GW_LINK:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createLink( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createLink( classId, id ) );
        }
        break;
      case GW_CLOB:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createClob( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createClob( classId, id ) );
        }
        break;
      case GW_CMD:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createCmd( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createCmd( classId, id ) );
        }
        break;
      case GW_EVENT:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createEvent( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createEvent( classId, id ) );
        }
        break;
      case GW_RTDATA:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createRtdata( classId, objId.strid(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createRtdata( classId, id ) );
        }
        break;
      case GW_CMD_ARG:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createCmdArg( classId, objId.strid(), aGwid.propId(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createCmdArg( classId, aGwid.propId(), id ) );
        }
        break;
      case GW_EVENT_PARAM:
        if( aObjIds != null ) {
          for( Skid objId : aObjIds ) {
            for( String id : aPropIds ) {
              aGwids.add( Gwid.createEventParam( classId, objId.strid(), aGwid.propId(), id ) );
            }
          }
          break;
        }
        for( String id : aPropIds ) {
          aGwids.add( Gwid.createEventParam( classId, aGwid.propId(), id ) );
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();

    }
    return aGwids;
  }
}
