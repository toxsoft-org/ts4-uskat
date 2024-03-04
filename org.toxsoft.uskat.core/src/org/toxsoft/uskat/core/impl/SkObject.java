package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link ISkObject} implementation.
 * <p>
 * Notes on subclassing:
 * <ul>
 * <li>it is legal to add some initialization code to the subclass constructor. However {@link #coreApi()} is not set in
 * constructor. {@link #doPostConstruct()} is the right place to prerform initialization which needs
 * {@link #coreApi()};</li>
 * <li>TODO ???.</li>
 * </ul>
 *
 * @author hazard157
 */
public class SkObject
    implements ISkObject {

  /**
   * Default creator singleton, creates instances of the class {@link SkObject}.
   */
  static final ISkObjectCreator<SkObject> DEFAULT_CREATOR = SkObject::new;

  private ISkCoreApi           coreApi;
  private final Skid           skid;
  private final IOptionSetEdit attrs  = new OptionSet();
  private final MappedSkids    rivets = new MappedSkids();

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   */
  protected SkObject( Skid aSkid ) {
    if( aSkid == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aSkid == Skid.NONE ) {
      throw new TsIllegalArgumentRtException();
    }
    skid = aSkid;
    attrs.setValobj( AID_SKID, skid );
    attrs.setStr( AID_CLASS_ID, skid.classId() );
    attrs.setStr( AID_STRID, skid.strid() );
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  /**
   * Sets reference to the core API, is called from {@link SkCoreServObject}.
   *
   * @param aSkatApi {@link ISkCoreApi} - the core API
   */
  void papiSetCoreApi( ISkCoreApi aSkatApi ) {
    coreApi = aSkatApi;
    doPostConstruct();
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return skid.strid();
  }

  @Override
  final public String nmName() {
    return attrs.getStr( AID_NAME, TsLibUtils.EMPTY_STRING );
  }

  @Override
  final public String description() {
    return attrs.getStr( AID_DESCRIPTION, TsLibUtils.EMPTY_STRING );
  }

  // ------------------------------------------------------------------------------------
  // ISkObject
  //

  @Override
  final public Skid skid() {
    return skid;
  }

  @Override
  final public String classId() {
    return skid.classId();
  }

  @Override
  final public String strid() {
    return skid.strid();
  }

  @Override
  final public IOptionSetEdit attrs() {
    return attrs;
  }

  @Override
  final public MappedSkids rivets() {
    return rivets;
  }

  @Override
  final public ISkCoreApi coreApi() {
    return coreApi;
  }

  @Override
  public Skid getSingleLinkSkid( String aLinkId ) {
    IDtoLinkFwd lf = coreApi.linkService().getLinkFwd( skid, aLinkId );
    return lf.rightSkids().findOnly();
  }

  @Override
  public <T extends ISkObject> T getSingleLinkObj( String aLinkId ) {
    IDtoLinkFwd lf = coreApi.linkService().getLinkFwd( skid, aLinkId );
    Skid s = lf.rightSkids().findOnly();
    if( s != null ) {
      return coreApi.objService().find( s );
    }
    return null;
  }

  @Override
  public ISkidList getLinkSkids( String aLinkId ) {
    return coreApi.linkService().getLinkFwd( skid, aLinkId ).rightSkids();
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public <T extends ISkObject> IList<T> getLinkObjs( String aLinkId ) {
    ISkidList sl = coreApi.linkService().getLinkFwd( skid, aLinkId ).rightSkids();
    if( sl.isEmpty() ) {
      return IList.EMPTY;
    }
    return (IList)coreApi.objService().getObjs( sl );
  }

  @Override
  public ISkidList getLinkRevSkids( String aClassId, String aLinkId ) {
    return coreApi.linkService().getLinkRev( aClassId, aLinkId, skid ).leftSkids();
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public <T extends ISkObject> IList<T> getLinkRevObjs( String aClassId, String aLinkId ) {
    ISkidList sl = coreApi.linkService().getLinkRev( aClassId, aLinkId, skid ).leftSkids();
    if( sl.isEmpty() ) {
      return IList.EMPTY;
    }
    return (IList)coreApi.objService().getObjs( sl );
  }

  @Override
  public <T extends ISkObject> IList<T> getRivetRevObjs( String aClassId, String aRivetId ) {
    // TODO реализовать SkObject.getRivetRevObjs()
    throw new TsUnderDevelopmentRtException( "SkObject.getRivetRevObjs()" );
  }

  @Override
  public ISkidList getRivetRevSkids( String aClassId, String aRivetId ) {
    // TODO реализовать SkObject.getRivetRevSkids()
    throw new TsUnderDevelopmentRtException( "SkObject.getRivetRevSkids()" );
  }

  @Override
  public String getClob( String aClobId, String aDefaultValue ) {
    Gwid gwid = Gwid.createClob( classId(), strid(), aClobId );
    String clobValue = coreApi.clobService().readClob( gwid );
    return clobValue.isEmpty() ? aDefaultValue : clobValue;
  }

  // ------------------------------------------------------------------------------------
  // Overrideable
  //

  @Override
  public String readableName() {
    return nmName().isBlank() ? id() : nmName();
  }

  /**
   * Subclass may perform ore API dependent initialization.
   * <p>
   * This method is called by {@link SkCoreServObject} immediately after constructor when {@link #coreApi()} has been
   * set.
   * <p>
   * Does nothing in base class, there is no need to call superclass method when overriding.
   */
  protected void doPostConstruct() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return skid.toString();
  }

  @Override
  final public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof ISkObject that ) {
      return this.skid.equals( that.skid() );
    }
    return false;
  }

  @Override
  final public int hashCode() {
    return skid.hashCode();
  }

}
