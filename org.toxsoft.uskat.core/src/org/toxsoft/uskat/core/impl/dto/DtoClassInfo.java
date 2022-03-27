package org.toxsoft.uskat.core.impl.dto;

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

}
