package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link ISkObject} implementation.
 * <p>
 * Notes on subclassing:
 * <ul>
 * <li>it is legal to add som initialization code to the subclass constructor. However {@link #coreApi()} is not set in
 * constructor. {@link #doPostConstruct()} is the right place to prerform initialization which needs
 * {@link #coreApi()};</li>
 * <li>TODO ???.</li>
 * </ul>
 *
 * @author hazard157
 */
public class SkObject
    implements ISkObject {

  private ISkCoreApi                      coreApi;
  private final Skid                      skid;
  private final IOptionSetEdit            attrs  = new OptionSet();
  private final IStringMapEdit<ISkidList> rivets = new StringMap<>();

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

  /**
   * Sets reference to the core API, is called from {@link SkObjectService}.
   *
   * @param aSkatApi {@link ISkCoreApi} - the core API
   */
  void papiSetSkatApi( ISkCoreApi aSkatApi ) {
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
  final public IOptionSet attrs() {
    return attrs;
  }

  @Override
  final public IStringMap<ISkidList> rivets() {
    return rivets;
  }

  @Override
  final public ISkCoreApi coreApi() {
    return coreApi;
  }

  @Override
  public Skid getSingleLinkSkid( String aLinkId ) {
    // TODO реализовать SkObject.getSingleLinkSkid()
    throw new TsUnderDevelopmentRtException( "SkObject.getSingleLinkSkid()" );
  }

  @Override
  public <T extends ISkObject> T getSingleLink( String aLinkId ) {
    // TODO реализовать SkObject.getSingleLink()
    throw new TsUnderDevelopmentRtException( "SkObject.getSingleLink()" );
  }

  @Override
  public ISkidList getLinkSkids( String aLinkId ) {
    // TODO реализовать SkObject.getLinkSkids()
    throw new TsUnderDevelopmentRtException( "SkObject.getLinkSkids()" );
  }

  @Override
  public <T extends ISkObject> IMap<Skid, T> getLink( String aLinkId ) {
    // TODO реализовать SkObject.getLink()
    throw new TsUnderDevelopmentRtException( "SkObject.getLink()" );
  }

  @Override
  public <T extends ISkObject> IList<T> getLinkObjs( String aLinkId ) {
    // TODO реализовать SkObject.getLinkObjs()
    throw new TsUnderDevelopmentRtException( "SkObject.getLinkObjs()" );
  }

  @Override
  public ISkidList getLinkRevSkids( String aClassId, String aLinkId ) {
    // TODO реализовать SkObject.getLinkRevSkids()
    throw new TsUnderDevelopmentRtException( "SkObject.getLinkRevSkids()" );
  }

  @Override
  public <T extends ISkObject> IMap<Skid, T> getLinkRev( String aClassId, String aLinkId ) {
    // TODO реализовать SkObject.getLinkRev()
    throw new TsUnderDevelopmentRtException( "SkObject.getLinkRev()" );
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
   * This method is called by {@link SkObjectService} after construcotr but sfter setting {@link #coreApi()}.
   * инициализирована.
   * <p>
   * Does nothing in base class, there is no need to call superclass method when overriding.
   */
  protected void doPostConstruct() {
    // nop
  }

}
