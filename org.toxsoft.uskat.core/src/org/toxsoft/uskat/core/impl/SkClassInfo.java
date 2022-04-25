package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link ISkClassInfo} implementation.
 *
 * @author hazard157
 */
class SkClassInfo
    extends StridableParameterizedSer
    implements ISkClassInfo {

  private static final long serialVersionUID = 157157L;

  private final ISkClassInfo         parentInfo;
  private final SkClassHierarchyInfo hierarchyInfo;

  private final SkClassProps<IDtoAttrInfo>   attrs;
  private final SkClassProps<IDtoRivetInfo>  rivets;
  private final SkClassProps<IDtoClobInfo>   clobs;
  private final SkClassProps<IDtoRtdataInfo> rtdata;
  private final SkClassProps<IDtoLinkInfo>   links;
  private final SkClassProps<IDtoCmdInfo>    cmds;
  private final SkClassProps<IDtoEventInfo>  events;

  private final IMap<ESkClassPropKind, SkClassProps<?>> propsMap;

  /**
   * Constructor.
   *
   * @param aId String - class identifier (IDpath)
   * @param aParentInfo {@link ISkClassInfo} - parent class info
   * @param aParams {@link IOptionSet} - {@link ISkClassInfo#params()} values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  SkClassInfo( String aId, ISkClassInfo aParentInfo, IOptionSet aParams ) {
    this( aId, aParentInfo );
    TsNullArgumentRtException.checkNulls( aParentInfo, aParams );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      propsMap.getByKey( k ).papiInit( this );
    }
  }

  /**
   * Construcor for root class creation in {@link #createRootClassInfo()}.
   */
  private SkClassInfo() {
    this( IGwHardConstants.GW_ROOT_CLASS_ID, null );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      propsMap.getByKey( k ).papiInit( this );
    }
  }

  /**
   * Basic constructor.
   *
   * @param aId String - the class ID
   * @param aParent {@link ISkClassInfo} - the parent info
   */
  private SkClassInfo( String aId, ISkClassInfo aParent ) {
    super( aId );
    parentInfo = aParent;
    hierarchyInfo = new SkClassHierarchyInfo( this, parentInfo );
    attrs = new SkClassProps<>( ESkClassPropKind.ATTR );
    rivets = new SkClassProps<>( ESkClassPropKind.RIVET );
    clobs = new SkClassProps<>( ESkClassPropKind.CLOB );
    rtdata = new SkClassProps<>( ESkClassPropKind.RTDATA );
    links = new SkClassProps<>( ESkClassPropKind.LINK );
    cmds = new SkClassProps<>( ESkClassPropKind.CMD );
    events = new SkClassProps<>( ESkClassPropKind.EVENT );
    IMapEdit<ESkClassPropKind, SkClassProps<?>> map = new ElemMap<>();
    map.put( ESkClassPropKind.ATTR, attrs );
    map.put( ESkClassPropKind.RIVET, rivets );
    map.put( ESkClassPropKind.CLOB, clobs );
    map.put( ESkClassPropKind.RTDATA, rtdata );
    map.put( ESkClassPropKind.LINK, links );
    map.put( ESkClassPropKind.CMD, cmds );
    map.put( ESkClassPropKind.EVENT, events );
    propsMap = map;
  }

  static SkClassInfo createRootClassInfo() {
    IDtoClassInfo dto = SkUtils.createRootClassDto();
    SkClassInfo cinf = new SkClassInfo();
    TsInternalErrorRtException.checkFalse( cinf.id().equals( dto.id() ) );
    cinf.params().setAll( dto.params() );
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      cinf.propsMap.getByKey( k ).papiSetSelf( dto.propInfos( k ) );
    }
    return cinf;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void papiClearHierarchyCache() {
    hierarchyInfo.papiClearCache();
    for( ESkClassPropKind k : ESkClassPropKind.asList() ) {
      propsMap.getByKey( k ).papiClearCache();
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkClassInfo
  //

  @Override
  public ISkClassHierarchyInfo hierarchy() {
    return hierarchyInfo;
  }

  @Override
  public SkClassProps<IDtoAttrInfo> attrs() {
    return attrs;
  }

  @Override
  public SkClassProps<IDtoRivetInfo> rivets() {
    return rivets;
  }

  @Override
  public SkClassProps<IDtoClobInfo> clobs() {
    return clobs;
  }

  @Override
  public SkClassProps<IDtoLinkInfo> links() {
    return links;
  }

  @Override
  public SkClassProps<IDtoRtdataInfo> rtdata() {
    return rtdata;
  }

  @Override
  public SkClassProps<IDtoEventInfo> events() {
    return events;
  }

  @Override
  public SkClassProps<IDtoCmdInfo> cmds() {
    return cmds;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <T extends IDtoClassPropInfoBase> SkClassProps<T> props( ESkClassPropKind aKind ) {
    return (SkClassProps)propsMap.getByKey( aKind );
  }

}
