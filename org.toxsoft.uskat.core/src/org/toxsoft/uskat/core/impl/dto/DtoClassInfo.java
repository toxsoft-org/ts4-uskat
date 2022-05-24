package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoClassInfo} implementation.
 *
 * @author hazard157
 */
public class DtoClassInfo
    extends StridableParameterized
    implements IDtoClassInfo {

  /**
   * The keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoClassInfo> KEEPER =
      new AbstractEntityKeeper<>( IDtoClassInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter sw, IDtoClassInfo aEntity ) {
          sw.writeSpace();
          sw.writeAsIs( aEntity.id() );
          sw.writeSeparatorChar();
          sw.writeSpace();
          if( !aEntity.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
            sw.writeAsIs( aEntity.parentId() );
            sw.writeSeparatorChar();
            sw.writeSpace();
          }
          OptionSetKeeper.KEEPER.write( sw, aEntity.params() );
          sw.writeSeparatorChar();
          sw.incNewLine();
          // attrs
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_ATTRS, true );
          StrioUtils.writeStridablesList( sw, aEntity.attrInfos(), DtoAttrInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // rivets
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_RIVETS, true );
          StrioUtils.writeStridablesList( sw, aEntity.rivetInfos(), DtoRivetInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // links
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_LINKS, true );
          StrioUtils.writeStridablesList( sw, aEntity.linkInfos(), DtoLinkInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // clobs
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_CLOBS, true );
          StrioUtils.writeStridablesList( sw, aEntity.clobInfos(), DtoClobInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // rtdata
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_RTDATA, true );
          StrioUtils.writeStridablesList( sw, aEntity.rtdataInfos(), DtoRtdataInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // cmds
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_CMDS, true );
          StrioUtils.writeStridablesList( sw, aEntity.cmdInfos(), DtoCmdInfo.KEEPER, true );
          sw.writeSeparatorChar();
          sw.writeEol();
          // events
          StrioUtils.writeKeywordHeader( sw, IDtoHardConstants.KW_EVENTS, true );
          StrioUtils.writeStridablesList( sw, aEntity.eventInfos(), DtoEventInfo.KEEPER, true );
          sw.decNewLine();
        }

        @Override
        protected IDtoClassInfo doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          String parentId = TsLibUtils.EMPTY_STRING;
          if( !id.equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
            parentId = aSr.readIdPath();
            aSr.ensureSeparatorChar();
          }
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          DtoClassInfo classInfo = new DtoClassInfo( id, parentId, params );
          aSr.ensureSeparatorChar();
          // attrs
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_ATTRS );
          StrioUtils.readStridablesList( aSr, classInfo.attrInfos(), DtoAttrInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // rivets
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_RIVETS );
          StrioUtils.readStridablesList( aSr, classInfo.rivetInfos(), DtoRivetInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // links
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_LINKS );
          StrioUtils.readStridablesList( aSr, classInfo.linkInfos(), DtoLinkInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // clobs
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_CLOBS );
          StrioUtils.readStridablesList( aSr, classInfo.clobInfos(), DtoClobInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // rtdata
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_RTDATA );
          StrioUtils.readStridablesList( aSr, classInfo.rtdataInfos(), DtoRtdataInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // cmds
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_CMDS );
          StrioUtils.readStridablesList( aSr, classInfo.cmdInfos(), DtoCmdInfo.KEEPER );
          aSr.ensureSeparatorChar();
          // events
          StrioUtils.ensureKeywordHeader( aSr, IDtoHardConstants.KW_EVENTS );
          StrioUtils.readStridablesList( aSr, classInfo.eventInfos(), DtoEventInfo.KEEPER );
          return classInfo;
        }
      };

  private final String parentId;

  private final IStridablesListEdit<DtoAttrInfo>   attrInfos   = new StridablesList<>();
  private final IStridablesListEdit<DtoClobInfo>   clobInfos   = new StridablesList<>();
  private final IStridablesListEdit<DtoRivetInfo>  rivetInfos  = new StridablesList<>();
  private final IStridablesListEdit<DtoLinkInfo>   linkInfos   = new StridablesList<>();
  private final IStridablesListEdit<DtoRtdataInfo> rtdataInfos = new StridablesList<>();
  private final IStridablesListEdit<DtoCmdInfo>    cmdInfos    = new StridablesList<>();
  private final IStridablesListEdit<DtoEventInfo>  eventInfos  = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aId String - the class ID (IDpath)
   * @param aParentId String - the parent ID (an IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public DtoClassInfo( String aId, String aParentId, IOptionSet aParams ) {
    super( aId, aParams );
    parentId = StridUtils.checkValidIdPath( aParentId );
  }

  /**
   * Constructor for root class with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID}.
   *
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoClassInfo( IOptionSet aParams ) {
    super( IGwHardConstants.GW_ROOT_CLASS_ID, aParams );
    parentId = TsLibUtils.EMPTY_STRING;
  }

  /**
   * Static copy constructor.
   *
   * @param aSource {@link IDtoClassInfo} - the source
   * @return {@link DtoClassInfo} - deep copy of source
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid source
   */
  public static DtoClassInfo createDeepCopy( IDtoClassInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    DtoClassInfo dto;
    if( aSource.parentId().isEmpty() ) {
      TsIllegalArgumentRtException.checkFalse( aSource.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) );
      dto = new DtoClassInfo( aSource.params() );
    }
    else {
      dto = new DtoClassInfo( aSource.id(), aSource.parentId(), aSource.params() );
    }

    // FIXME make deep copies!!!

    dto.attrInfos().setAll( aSource.attrInfos() );
    dto.clobInfos().setAll( aSource.clobInfos() );
    dto.rivetInfos().setAll( aSource.rivetInfos() );
    dto.rtdataInfos().setAll( aSource.rtdataInfos() );
    dto.linkInfos().setAll( aSource.linkInfos() );
    dto.cmdInfos().setAll( aSource.cmdInfos() );
    dto.eventInfos().setAll( aSource.eventInfos() );

    return dto;
  }

  /**
   * Creates {@link DtoClassInfo} from the USkat class info {@link ISkClassInfo}.
   * <p>
   * Depending on the argument value the returned instance will include either all properties of the source class or the
   * properties introduced in the source class (spo called 'self properties').
   *
   * @param aSkClass {@link ISkClassInfo} - the source
   * @param aOnlySelfProps boolean - the flag to exclude inherited properties
   * @return {@link DtoClassInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static DtoClassInfo createFromSk( ISkClassInfo aSkClass, boolean aOnlySelfProps ) {
    TsNullArgumentRtException.checkNull( aSkClass );
    DtoClassInfo dtoClass;
    if( aSkClass.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
      dtoClass = new DtoClassInfo( aSkClass.params() );
    }
    else {
      dtoClass = new DtoClassInfo( aSkClass.id(), aSkClass.parentId(), aSkClass.params() );
    }
    dtoClass.attrInfos().setAll( aSkClass.attrs().makeCopy( aOnlySelfProps ) );
    dtoClass.rivetInfos().setAll( aSkClass.rivets().makeCopy( aOnlySelfProps ) );
    dtoClass.clobInfos().setAll( aSkClass.clobs().makeCopy( aOnlySelfProps ) );
    dtoClass.rtdataInfos().setAll( aSkClass.rtdata().makeCopy( aOnlySelfProps ) );
    dtoClass.linkInfos().setAll( aSkClass.links().makeCopy( aOnlySelfProps ) );
    dtoClass.cmdInfos().setAll( aSkClass.cmds().makeCopy( aOnlySelfProps ) );
    dtoClass.eventInfos().setAll( aSkClass.events().makeCopy( aOnlySelfProps ) );
    return dtoClass;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassInfo
  //

  @Override
  public String parentId() {
    return parentId;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoAttrInfo> attrInfos() {
    return (IStridablesListEdit)attrInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoRivetInfo> rivetInfos() {
    return (IStridablesListEdit)rivetInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoRtdataInfo> rtdataInfos() {
    return (IStridablesListEdit)rtdataInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoLinkInfo> linkInfos() {
    return (IStridablesListEdit)linkInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoCmdInfo> cmdInfos() {
    return (IStridablesListEdit)cmdInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoEventInfo> eventInfos() {
    return (IStridablesListEdit)eventInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesListEdit<IDtoClobInfo> clobInfos() {
    return (IStridablesListEdit)clobInfos;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <T extends IDtoClassPropInfoBase> IStridablesList<T> propInfos( ESkClassPropKind aKind ) {
    TsNullArgumentRtException.checkNull( aKind );
    return switch( aKind ) {
      case ATTR -> (IStridablesList)attrInfos;
      case RIVET -> (IStridablesList)rivetInfos;
      case CLOB -> (IStridablesList)clobInfos;
      case RTDATA -> (IStridablesList)rtdataInfos;
      case LINK -> (IStridablesList)linkInfos;
      case CMD -> (IStridablesList)cmdInfos;
      case EVENT -> (IStridablesList)eventInfos;
    };
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @SuppressWarnings( "boxing" )
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder( super.toString() );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      IStridablesList<?> cp = propInfos( k );
      sb.append( String.format( ", %s[%d]", k.id(), cp.size() ) ); //$NON-NLS-1$
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = PRIME * result + attrInfos.hashCode();
    result = PRIME * result + rivetInfos.hashCode();
    result = PRIME * result + clobInfos.hashCode();
    result = PRIME * result + linkInfos.hashCode();
    result = PRIME * result + rtdataInfos.hashCode();
    result = PRIME * result + eventInfos.hashCode();
    result = PRIME * result + cmdInfos.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( super.equals( aThat ) ) {
      if( aThat instanceof DtoClassInfo that ) {
        return this.attrInfos.equals( that.attrInfos ) && //
            this.rivetInfos.equals( that.rivetInfos ) && //
            this.clobInfos.equals( that.clobInfos ) && //
            this.linkInfos.equals( that.linkInfos ) && //
            this.rtdataInfos.equals( that.rtdataInfos ) && //
            this.eventInfos.equals( that.eventInfos ) && //
            this.cmdInfos.equals( that.cmdInfos );
      }
    }
    return false;
  }

}
