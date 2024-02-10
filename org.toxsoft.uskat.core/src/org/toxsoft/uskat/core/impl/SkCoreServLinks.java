package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.backend.api.IBaLinksMessages.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import java.util.Objects;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.AbstractTsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.SingleItemList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.dto.DtoLinkFwd;
import org.toxsoft.uskat.core.impl.dto.DtoLinkRev;

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
   * {@link ISkLinkService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkLinkServiceListener> {

    private final GwidList changedLinks = new GwidList();

    @Override
    protected void doClearPendingEvents() {
      changedLinks.clear();
    }

    @Override
    protected void doFirePendingEvents() {
      reallyFire( changedLinks );
      changedLinks.clear();
    }

    @Override
    protected boolean doIsPendingEvents() {
      return !changedLinks.isEmpty();
    }

    private void reallyFire( IGwidList aChangedLinks ) {
      for( ISkLinkServiceListener l : listeners() ) {
        try {
          l.onLinkChanged( coreApi(), aChangedLinks );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    void fireLinksChanged( IGwidList aChangedLink ) {
      if( isFiringPaused() ) {
        for( Gwid g : aChangedLink ) {
          // put last changed link at the end of the list
          changedLinks.remove( g );
          changedLinks.add( g );
        }
        return;
      }
      reallyFire( aChangedLink );
    }

  }

  /**
   * The service validator {@link ISkObjectService#svs()} implementation.
   *
   * @author hazard157
   */
  static class ValidationSupport
      extends AbstractTsValidationSupport<ISkLinkServiceValidator>
      implements ISkLinkServiceValidator {

    @Override
    public ISkLinkServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canSetLink( IDtoLinkFwd aOldLink, IDtoLinkFwd aNewLink ) {
      TsNullArgumentRtException.checkNull( aNewLink );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkLinkServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canSetLink( aOldLink, aNewLink ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  private final ISkLinkServiceValidator builtinValidator = ( aOldLink, aNewLink ) -> {
    String cid = aNewLink.leftSkid().classId();
    ISkClassInfo cInfo = sysdescr().getClassInfo( cid );
    IDtoLinkInfo lInfo = cInfo.links().list().getByKey( aNewLink.linkId() );
    // check that left object exists
    ISkObject left = coreApi().objService().find( aNewLink.leftSkid() );
    if( left == null ) {
      return ValidationResult.error( FMT_ERR_NO_LINK_LEFT_OBJ, aNewLink.leftSkid(), aNewLink.linkId() );
    }
    // check each of the right objectc
    for( Skid rSkid : aNewLink.rightSkids() ) {
      // right object class must exist in sysdescr
      ISkClassInfo rightClassInfo = sysdescr().findClassInfo( rSkid.classId() );
      if( rightClassInfo == null ) {
        return ValidationResult.error( FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS, rSkid, aNewLink.linkId() );
      }
      // right object c lass must be allowed in the link
      if( !rightClassInfo.isOfClass( lInfo.rightClassIds() ) ) {
        return ValidationResult.error( FMT_ERR_RIGHT_OBJ_INV_CLASS, rSkid, aNewLink.linkId() );
      }
      // right object itself must exist
      ISkObject rSkObj = coreApi().objService().find( rSkid );
      if( rSkObj == null ) {
        return ValidationResult.error( FMT_ERR_NO_LINK_RIGHT_OBJ_STRID, rSkid, aNewLink.linkId() );
      }
    }
    // check number of right objects against link constraints
    ValidationResult vr = lInfo.linkConstraint().validateErrorSize( aNewLink.rightSkids() );
    if( vr.isError() ) {
      return vr;
    }
    // check duplicate right objects against link constraints
    vr = ValidationResult.firstNonOk( vr, lInfo.linkConstraint().validateErrorDups( aNewLink.rightSkids() ) );
    if( vr.isError() ) {
      return vr;
    }
    // check right objects list's emptyness against link constraints
    return ValidationResult.firstNonOk( vr, lInfo.linkConstraint().validateWarnEmpty( aNewLink.rightSkids() ) );
  };

  final Eventer           eventer           = new Eventer();
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
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    eventer.clearListenersList();
    eventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    switch( aMessage.messageId() ) {
      case MSGID_LINKS_CHANGE: {
        IGwidList gwidList = extractGwidList( aMessage );
        eventer.fireLinksChanged( gwidList );
        return true;
      }
      default:
        return false;
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IDtoLinkFwd readFromBackend( Gwid aLinkGwid, Skid aLeftSkid ) {
    IDtoLinkFwd lf = ba().baLinks().findLinkFwd( aLinkGwid, aLeftSkid );
    if( lf != null ) {
      return lf;
    }
    return new DtoLinkFwd( aLinkGwid, aLeftSkid, ISkidList.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // ISkLinkService
  //

  @Override
  public IDtoLinkFwd getLinkFwd( Skid aLeftSkid, String aLinkId ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aLeftSkid, aLinkId );
    coreApi().papiCheckIsOpen();
    // check left object exists
    ISkObject left = coreApi().objService().find( aLeftSkid );
    if( left == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_LINK_LEFT_OBJ, aLeftSkid, aLinkId );
    }
    // find link's declaring class
    ISkClassInfo leftClassInfo = sysdescr().getClassInfo( aLeftSkid.classId() );
    ISkClassInfo declaringClassInfo = leftClassInfo.links().findSuperDeclarer( aLinkId );
    if( declaringClassInfo == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_LINK1, aLinkId, aLeftSkid.classId() );
    }
    // read link
    return readFromBackend( Gwid.createLink( declaringClassInfo.id(), aLinkId ), aLeftSkid );
  }

  @Override
  public IStringMap<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    // check left object exists
    ISkObject left = coreApi().objService().find( aLeftSkid );
    if( left == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_OBJ, aLeftSkid );
    }
    // iterate over all links
    ISkClassInfo leftClassInfo = sysdescr().getClassInfo( aLeftSkid.classId() );
    IStringMapEdit<IDtoLinkFwd> map = new StringMap<>();
    for( String linkId : leftClassInfo.links().list().ids() ) {
      ISkClassInfo declaringClassInfo = leftClassInfo.links().findSuperDeclarer( linkId );
      Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), linkId );
      IDtoLinkFwd lf = readFromBackend( linkGwid, aLeftSkid );
      map.put( linkId, lf );
    }
    return map;
  }

  @Override
  public IDtoLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid );
    coreApi().papiCheckIsOpen();
    // find declaring class and by the way check that link exists
    ISkClassInfo classInfo = sysdescr().getClassInfo( aClassId );
    if( classInfo == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_CLASS, aClassId );
    }
    ISkClassInfo declaringClassInfo = classInfo.links().findSuperDeclarer( aLinkId );
    if( declaringClassInfo == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_LINK1, aLinkId, aClassId );
    }
    // check object exists
    ISkObject right = coreApi().objService().find( aRightSkid );
    if( right == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_OBJ, aRightSkid );
    }
    Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), aLinkId );
    IDtoLinkRev lr = ba().baLinks().findLinkRev( linkGwid, aRightSkid, IStringList.EMPTY );
    if( lr == null ) {
      lr = new DtoLinkRev( linkGwid, aRightSkid, ISkidList.EMPTY );
    }
    return lr;
  }

  // @Override
  // FIXME public IMap<Gwid, ISkidList> getAllLinksRev( Skid aRightSkid ) {
  // // TODO реализовать SkCoreServLinks.getAllLinksRev()
  // throw new TsUnderDevelopmentRtException( "SkCoreServLinks.getAllLinksRev()" );
  // }

  @Override
  public IDtoLinkFwd defineLink( Skid aLeftSkid, String aLinkId, ISkidList aRemovedSkids, ISkidList aAddedSkids ) {
    checkThread();
    TsNullArgumentRtException.checkNulls( aLinkId, aLeftSkid, aAddedSkids );
    coreApi().papiCheckIsOpen();
    // check left object exists
    ISkObject left = coreApi().objService().find( aLeftSkid );
    if( left == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_LINK_LEFT_OBJ, aLeftSkid, aLinkId );
    }
    // find link's declaring class
    ISkClassInfo leftClassInfo = sysdescr().getClassInfo( aLeftSkid.classId() );
    ISkClassInfo declaringClassInfo = leftClassInfo.links().findSuperDeclarer( aLinkId );
    if( declaringClassInfo == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_LINK1, aLinkId, aLeftSkid.classId() );
    }
    // creale forward link to be written to backend
    Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), aLinkId );
    DtoLinkFwd newLink = new DtoLinkFwd( linkGwid, aLeftSkid, ISkidList.EMPTY );
    // first fill it from existing link if any
    IDtoLinkFwd oldLink = ba().baLinks().findLinkFwd( linkGwid, aLeftSkid );
    if( oldLink != null ) {
      newLink.rightSkids().setAll( oldLink.rightSkids() );
    }
    // remove right objects
    if( aRemovedSkids != null ) {
      for( Skid skid : aRemovedSkids ) {
        newLink.rightSkids().remove( skid );
      }
    }
    else {
      newLink.rightSkids().clear();
    }
    // add right objects without duplicates
    for( Skid skid : aAddedSkids ) {
      if( !newLink.rightSkids().hasElem( skid ) ) {
        newLink.rightSkids().add( skid );
      }
    }
    // write link only it was really changed
    if( !Objects.equals( oldLink, newLink ) ) {
      // validate link may be written
      TsValidationFailedRtException.checkError( validationSupport.canSetLink( oldLink, newLink ) );
      ba().baLinks().writeLinksFwd( new SingleItemList<IDtoLinkFwd>( newLink ) );
    }
    return newLink;
  }

  @Override
  public void removeLinks( Skid aLeftSkid ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aLeftSkid );
    coreApi().papiCheckIsOpen();
    // check left object exists
    ISkObject left = coreApi().objService().find( aLeftSkid );
    if( left == null ) {
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_OBJ, aLeftSkid );
    }
    // list if all link IDs
    ISkClassInfo objClassInfo = sysdescr().getClassInfo( aLeftSkid.classId() );
    IStringList allLinkIds = objClassInfo.links().list().ids();
    if( allLinkIds.isEmpty() ) {
      return;
    }
    // remove all links
    IListEdit<IDtoLinkFwd> ll = new ElemArrayList<>();
    for( String lid : allLinkIds ) {
      ISkClassInfo declaringClassInfo = objClassInfo.links().findSuperDeclarer( lid );
      Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), lid );
      IDtoLinkFwd link = new DtoLinkFwd( linkGwid, aLeftSkid, ISkidList.EMPTY );
      ll.add( link );
    }
    ba().baLinks().writeLinksFwd( ll );
  }

  @Override
  public ITsEventer<ISkLinkServiceListener> eventer() {
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkLinkServiceValidator> svs() {
    return validationSupport;
  }

}
