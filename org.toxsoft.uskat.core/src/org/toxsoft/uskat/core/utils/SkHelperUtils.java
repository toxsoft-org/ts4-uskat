package org.toxsoft.uskat.core.utils;

import java.security.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Helper methods for the USkat core.
 * <p>
 * Methods of this class perform common tasks for applications using {@link ISkCoreApi}.
 *
 * @author hazard157
 */
public class SkHelperUtils {

  /**
   * Returns the data type constraint for the attribute.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoAttrInfo#dataType()}.
   *
   * @param aInfo {@link IDtoAttrInfo} - the attribute info
   * @param aConstraintId String - the constraint ID
   * @return {@link IAtomicValue} - the constraint value or {@link IAtomicValue#NULL} if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoAttrInfo aInfo, String aConstraintId ) {
    return getConstraint( aInfo, aConstraintId, IAtomicValue.NULL );
  }

  /**
   * Returns the data type constraint for the attribute.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoAttrInfo#dataType()}.
   *
   * @param aInfo {@link IDtoAttrInfo} - the attribute info
   * @param aConstraintId String - the constraint ID
   * @param aDefaultValue {@link IAtomicValue} - value to return when no constraint found
   * @return {@link IAtomicValue} - the constraint value or <code>aDefaultValue1</code> if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoAttrInfo aInfo, String aConstraintId, IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aInfo, aConstraintId, aDefaultValue );
    IAtomicValue av = aInfo.params().getValue( aConstraintId, IAtomicValue.NULL );
    if( av == IAtomicValue.NULL ) {
      av = aInfo.dataType().params().getValue( aConstraintId, aDefaultValue );
    }
    return av;
  }

  /**
   * Returns the data type constraint for the RTdata.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoRtdataInfo#dataType()}.
   *
   * @param aInfo {@link IDtoRtdataInfo} - the RTdata info
   * @param aConstraintId String - the constraint ID
   * @return {@link IAtomicValue} - the constraint value or {@link IAtomicValue#NULL} if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoRtdataInfo aInfo, String aConstraintId ) {
    return getConstraint( aInfo, aConstraintId, IAtomicValue.NULL );
  }

  /**
   * Returns the data type constraint for the RTdata.
   * <p>
   * Data type constraints are stored in {@link IDataType#params()} and {@link IDtoRtdataInfo#params()}. The constraint
   * in {@link IDtoRtdataInfo#params()} has priority ("overrides") constraint with the same ID from
   * {@link IDtoRtdataInfo#dataType()}.
   *
   * @param aInfo {@link IDtoRtdataInfo} - the RTdata info
   * @param aConstraintId String - the constraint ID
   * @param aDefaultValue {@link IAtomicValue} - value to return when no constraint found
   * @return {@link IAtomicValue} - the constraint value or <code>aDefaultValue1</code> if none found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IAtomicValue getConstraint( IDtoRtdataInfo aInfo, String aConstraintId, IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aInfo, aConstraintId, aDefaultValue );
    IAtomicValue av = aInfo.params().getValue( aConstraintId, IAtomicValue.NULL );
    if( av == IAtomicValue.NULL ) {
      av = aInfo.dataType().params().getValue( aConstraintId, aDefaultValue );
    }
    return av;
  }

  /**
   * Returns hexadecimal string representation of the argument MD5 digest.
   *
   * @param aPassword String - non-blank password string
   * @return String - hexadecimal string representation of the hash digets
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is a blank string
   */
  public static String getPasswordHashCode( String aPassword ) {
    TsErrorUtils.checkNonBlank( aPassword );
    MessageDigest md;
    try {
      md = MessageDigest.getInstance( "MD5" ); //$NON-NLS-1$
    }
    catch( NoSuchAlgorithmException e ) {
      throw new TsInternalErrorRtException( e );
    }
    md.update( aPassword.getBytes() );
    return TsMiscUtils.bytesToHexStr( md.digest() );
  }

  /**
   * Returns a reasonable length displayable string of object names.
   * <p>
   * For an empty list an empty string is returned.
   *
   * @param aObjects {@link ITsCollection}&lt;{@link ISkObject}&gt; - objects
   * @return String - human readable string
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static String makeObjsListReadableName( ITsCollection<ISkObject> aObjects ) {
    TsNullArgumentRtException.checkNull( aObjects );
    IList<ISkObject> ll;
    if( aObjects instanceof IList ) {
      ll = (IList<ISkObject>)aObjects;
    }
    else {
      ll = new ElemArrayList<>( aObjects );
    }
    switch( ll.size() ) {
      case 0:
        return TsLibUtils.EMPTY_STRING;
      case 1:
      case 2:
      case 3: {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < ll.size(); i++ ) {
          sb.append( ll.get( i ).readableName() );
          if( i < ll.size() - 1 ) {
            sb.append( ", " ); //$NON-NLS-1$
          }
        }
        return sb.toString();
      }
      default: {
        StringBuilder sb = new StringBuilder();
        sb.append( ll.get( 0 ).readableName() );
        sb.append( ", " ); //$NON-NLS-1$
        sb.append( ll.get( 1 ).readableName() );
        sb.append( ", ... " ); //$NON-NLS-1$
        sb.append( ll.last().readableName() );
        return sb.toString();
      }
    }
  }

  /**
   * Get SKIDs of listed objects.
   *
   * @param aObjs {@link IList}&lt;{@link ISkObject}&gt; - list of objects
   * @return {@link SkidList} - list of SKIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static SkidList objsToSkids( IList<? extends ISkObject> aObjs ) {
    TsNullArgumentRtException.checkNull( aObjs );
    SkidList ll = new SkidList();
    if( aObjs instanceof ITsFastIndexListTag<? extends ISkObject> fl ) {
      for( int i = 0, n = fl.size(); i < n; i++ ) {
        ll.add( fl.get( i ).skid() );
      }
    }
    else {
      for( ISkObject o : aObjs ) {
        ll.add( o.skid() );
      }
    }
    return ll;
  }

  /**
   * Get the <b>concrete</b> identifiers ({@link Gwid#isAbstract()} == <b>false</b>) from the provided input list.
   *
   * @param aCoreApi {@link ISkCoreApi} uskat API
   * @param aInputs {@link IGwidList} input list of identifiers
   * @param aOutputKind {@link EGwidKind} kind of output identifiers.
   * @return {@link IGwidList} output list of identifiers
   * @throws TsNullArgumentRtException any argument = <b>null</b>.
   */
  public static IGwidList expandGwids( ISkCoreApi aCoreApi, IGwidList aInputs, EGwidKind aOutputKind ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aInputs );
    ISkSysdescr sysdescr = aCoreApi.sysdescr();
    ISkLinkService linkService = aCoreApi.linkService();
    ISkGwidService gwidService = aCoreApi.gwidService();
    GwidList retValue = new GwidList();
    for( Gwid g : aInputs ) {
      switch( g.kind() ) {
        case GW_CLASS: {
          switch( aOutputKind ) {
            case GW_CLASS:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_ATTR:
            case GW_LINK:
            case GW_RIVET:
            case GW_RTDATA:
            case GW_EVENT:
            case GW_CMD:
              retValue.addAll( expandGwids( sysdescr, gwidService.expandGwid( g ).objIds(), aOutputKind ) );
              break;
            case GW_CMD_ARG:
            case GW_EVENT_PARAM:
            case GW_CLOB:
              throw new TsUnderDevelopmentRtException();
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        }
        case GW_LINK: {
          switch( aOutputKind ) {
            case GW_LINK:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_ATTR:
            case GW_RIVET:
            case GW_RTDATA:
            case GW_EVENT:
            case GW_CMD:
              for( Gwid link : gwidService.expandGwid( g ) ) {
                retValue.addAll( expandGwids( sysdescr, linkService.getLinkFwd( link ).rightSkids(), aOutputKind ) );
              }
              break;
            case GW_CLASS:
            case GW_CMD_ARG:
            case GW_EVENT_PARAM:
            case GW_CLOB:
              throw new TsUnderDevelopmentRtException();
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        }
        case GW_ATTR: {
          switch( aOutputKind ) {
            case GW_ATTR:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_CLASS:
            case GW_LINK:
            case GW_RIVET:
            case GW_RTDATA:
            case GW_EVENT:
            case GW_EVENT_PARAM:
            case GW_CMD:
            case GW_CMD_ARG:
            case GW_CLOB:
              // ignored
              break;
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        }
        case GW_RTDATA:
          switch( aOutputKind ) {
            case GW_RTDATA:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_CLASS:
            case GW_ATTR:
            case GW_LINK:
            case GW_RIVET:
            case GW_EVENT:
            case GW_EVENT_PARAM:
            case GW_CMD:
            case GW_CMD_ARG:
            case GW_CLOB:
              // ignored
              break;
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        case GW_EVENT:
          switch( aOutputKind ) {
            case GW_EVENT:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_CLASS:
            case GW_ATTR:
            case GW_LINK:
            case GW_RIVET:
            case GW_RTDATA:
            case GW_EVENT_PARAM:
            case GW_CMD:
            case GW_CMD_ARG:
            case GW_CLOB:
              // ignored
              break;
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        case GW_CMD:
          switch( aOutputKind ) {
            case GW_CMD:
              retValue.addAll( gwidService.expandGwid( g ) );
              break;
            case GW_CLASS:
            case GW_ATTR:
            case GW_LINK:
            case GW_RIVET:
            case GW_RTDATA:
            case GW_EVENT:
            case GW_EVENT_PARAM:
            case GW_CMD_ARG:
            case GW_CLOB:
              // ignored
              break;
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          break;
        case GW_RIVET:
        case GW_CMD_ARG:
        case GW_EVENT_PARAM:
        case GW_CLOB:
          throw new TsIllegalArgumentRtException();
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return retValue;
  }

  private static IGwidList expandGwids( ISkSysdescr aSysdescr, ISkidList aObjIds, EGwidKind aOutputKind ) {
    GwidList retValue = new GwidList();
    for( Skid objId : aObjIds ) {
      switch( aOutputKind ) {
        case GW_CLASS:
          retValue.add( Gwid.createObj( objId ) );
          break;
        case GW_ATTR:
          IStridablesList<IDtoAttrInfo> rtAttrInfos = aSysdescr.getClassInfo( objId.classId() ).attrs().list();
          for( String id : rtAttrInfos.keys() ) {
            retValue.add( Gwid.createAttr( objId, id ) );
          }
          break;
        case GW_LINK:
          IStridablesList<IDtoLinkInfo> rtLinkInfos = aSysdescr.getClassInfo( objId.classId() ).links().list();
          for( String id : rtLinkInfos.keys() ) {
            retValue.add( Gwid.createLink( objId, id ) );
          }
          break;
        case GW_RIVET:
          IStridablesList<IDtoRivetInfo> rtRivetInfos = aSysdescr.getClassInfo( objId.classId() ).rivets().list();
          for( String id : rtRivetInfos.keys() ) {
            retValue.add( Gwid.createRivet( objId, id ) );
          }
          break;
        case GW_RTDATA:
          IStridablesList<IDtoRtdataInfo> rtDataInfos = aSysdescr.getClassInfo( objId.classId() ).rtdata().list();
          for( String id : rtDataInfos.keys() ) {
            retValue.add( Gwid.createRtdata( objId, id ) );
          }
          break;
        case GW_EVENT:
          IStridablesList<IDtoEventInfo> rtEventInfos = aSysdescr.getClassInfo( objId.classId() ).events().list();
          for( String id : rtEventInfos.keys() ) {
            retValue.add( Gwid.createEvent( objId, id ) );
          }
          break;
        case GW_CMD:
          IStridablesList<IDtoCmdInfo> rtCmdInfos = aSysdescr.getClassInfo( objId.classId() ).cmds().list();
          for( String id : rtCmdInfos.keys() ) {
            retValue.add( Gwid.createCmd( objId, id ) );
          }
          break;
        case GW_CLOB:
        case GW_CMD_ARG:
        case GW_EVENT_PARAM:
          throw new TsUnderDevelopmentRtException();
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return retValue;
  }

  /**
   * Prohibit inheritance.
   */
  private SkHelperUtils() {
    // nop
  }

}
