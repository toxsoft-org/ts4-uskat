package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkLinkService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServLinks
    extends AbstractSkCoreService
    implements ISkLinkService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServLinks::new;

  /**
   * The service validator {@link ISkObjectService#svs()} implementation.
   *
   * @author hazard157
   */
  static class ValidationSupport
      extends AbstractTsValidationSupport<ISkLinkServiceValidator>
      implements ISkLinkServiceValidator {

    @Override
    public ValidationResult canSetLink( IDtoLinkFwd aLink ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public ISkLinkServiceValidator validator() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  private final ISkLinkServiceValidator builtinValidator = aLink -> null;

  final ValidationSupport validationSupport = new ValidationSupport();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServLinks( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // ApiWrapAbstractSkService
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

  // ------------------------------------------------------------------------------------
  // ISkLinkService
  //

  @Override
  public IDtoLinkFwd getLinkFwd( Skid aLeftSkid, String aLinkId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStringMap<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IDtoLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void defineLink( Skid aLeftSkid, String aLinkId, ISkidList aRemovedSkids, ISkidList aAddedSkids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeLinks( Skid aLeftSkid ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITsEventer<ISkLinkServiceListener> eventer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITsValidationSupport<ISkLinkServiceValidator> svs() {
    // TODO Auto-generated method stub
    return null;
  }

}
