package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * {@link ISkObject} implementation.
 * <p>
 * Notes on subclassing:
 * <ul>
 * <li>it is legal to add some initialization code to the subclass constructor. However {@link #coreApi()} is not set in
 * constructor. {@link #doPostConstruct()} is the right place to perform initialization which needs
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

  private ISkCoreApi                         coreApi;
  private final Skid                         skid;
  private final IOptionSetEdit               attrs     = new OptionSet();
  private final MappedSkids                  rivets    = new MappedSkids();
  private final IStringMapEdit<IMappedSkids> rivetRevs = new StringMap<>();

  /**
   * The map of the RTdata ID <-> RTdata GWID.
   * <p>
   * The map is created at first call and used by the methods {@link #readRtdataIfOpen(String)} and
   * {@link #writeRtdataIfOpen(String, IAtomicValue)}. The purpose of this method is to eliminate {@link Gwid} creation
   * on every call of the method.
   */
  private transient IStringMapEdit<Gwid> rtDataGwids = null;

  /**
   * Constructor.
   *
   * @param aSkid {@link Skid} - the object SKID
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
  // implementation
  //

  private Gwid internalRtDataIdToCachedGwid( String aRtdataId ) {
    if( rtDataGwids == null ) {
      rtDataGwids = new StringMap<>();
    }
    Gwid rtdGwid = rtDataGwids.findByKey( aRtdataId );
    if( rtdGwid == null ) {
      rtdGwid = Gwid.createRtdata( classId(), strid(), aRtdataId );
      rtDataGwids.put( aRtdataId, rtdGwid );
    }
    return rtdGwid;
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
  final public IStringMapEdit<IMappedSkids> rivetRevs() {
    return rivetRevs;
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

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> IList<T> getRivetRevObjs( String aClassId, String aRivetId ) {
    ISkidList objIds = getRivetRevSkids( aClassId, aRivetId );
    return (IList<T>)coreApi.objService().getObjs( objIds );
  }

  @Override
  public ISkidList getRivetRevSkids( String aClassId, String aRivetId ) {
    IMappedSkids mappedSkids = rivetRevs.findByKey( aClassId );
    if( mappedSkids == null ) {
      return ISkidList.EMPTY;
    }
    ISkidList retValue = mappedSkids.map().findByKey( aRivetId );
    return (retValue != null ? retValue : ISkidList.EMPTY);
  }

  @Override
  public String getClob( String aClobId, String aDefaultValue ) {
    Gwid gwid = Gwid.createClob( classId(), strid(), aClobId );
    String clobValue = coreApi.clobService().readClob( gwid );
    return clobValue.isEmpty() ? aDefaultValue : clobValue;
  }

  @Override
  public void setClob( String aClobId, String aClobString ) {
    TsNullArgumentRtException.checkNull( aClobString );
    Gwid gwid = Gwid.createClob( classId(), strid(), aClobId );
    coreApi.clobService().writeClob( gwid, aClobString );
  }

  @Override
  public IAtomicValue readRtdataIfOpen( String aRtdataId ) {
    Gwid rtdGwid = internalRtDataIdToCachedGwid( aRtdataId );
    ISkReadCurrDataChannel ch = coreApi.rtdService().findReadCurrDataChannel( rtdGwid );
    if( ch != null ) {
      return ch.getValue();
    }
    return null;
  }

  @Override
  public boolean writeRtdataIfOpen( String aRtdataId, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    Gwid rtdGwid = internalRtDataIdToCachedGwid( aRtdataId );
    ISkWriteCurrDataChannel ch = coreApi.rtdService().findWriteCurrDataChannel( rtdGwid );
    if( ch != null ) {
      ch.setValue( aValue );
      return true;
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
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
