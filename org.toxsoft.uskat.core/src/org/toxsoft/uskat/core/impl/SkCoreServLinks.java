package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;

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

    private final IMapEdit<Skid, IStringListEdit> changedLinks = new ElemMap<>();

    @Override
    protected void doClearPendingEvents() {
      changedLinks.clear();
    }

    @Override
    protected void doFirePendingEvents() {
      doFire( changedLinks );
      changedLinks.clear();
    }

    @Override
    protected boolean doIsPendingEvents() {
      return !changedLinks.isEmpty();
    }

    private void doFire( IMap<Skid, IStringListEdit> aChangedLinks ) {
      for( Skid skid : aChangedLinks.keys() ) {
        IStringMapEdit<Skid> map = new StringMap<>();
        for( String lid : aChangedLinks.getByKey( skid ) ) {
          map.put( lid, skid );
        }
        for( ISkLinkServiceListener l : listeners() ) {
          try {
            l.onLinkChanged( coreApi(), map );
          }
          catch( Exception ex ) {
            LoggerUtils.errorLogger().error( ex );
          }
        }
      }
    }

    void fireLinkChanged( String aLinkId, Skid aLeftSkid ) {
      if( isFiringPaused() ) {
        IStringListEdit sl = changedLinks.findByKey( aLeftSkid );
        if( sl == null ) {
          sl = new StringArrayList();
        }
        if( !sl.hasElem( aLinkId ) ) {
          sl.add( aLinkId );
        }
        changedLinks.put( aLeftSkid, sl );
        return;
      }
      IMapEdit<Skid, IStringListEdit> map = new ElemMap<>();
      map.put( aLeftSkid, new StringArrayList( aLinkId ) );
      doFire( map );
    }

    void fireLinksChanged( IStringList aLinkIds, Skid aLeftSkid ) {
      if( isFiringPaused() ) {
        IStringListEdit sl = changedLinks.findByKey( aLeftSkid );
        if( sl == null ) {
          sl = new StringArrayList();
        }
        for( String lid : aLinkIds ) {
          if( !sl.hasElem( lid ) ) {
            sl.add( lid );
          }
          changedLinks.put( aLeftSkid, sl );
        }
        return;
      }
      IMapEdit<Skid, IStringListEdit> map = new ElemMap<>();
      map.put( aLeftSkid, new StringArrayList( aLinkIds ) );
      doFire( map );
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
    public ValidationResult canSetLink( IDtoLinkFwd aLink ) {
      TsNullArgumentRtException.checkNull( aLink );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkLinkServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canSetLink( aLink ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  private final ISkLinkServiceValidator builtinValidator = aLink -> {
    String cid = aLink.leftSkid().classId();
    ISkClassInfo cInfo = sysdescr().getClassInfo( cid );
    IDtoLinkInfo lInfo = cInfo.links().list().getByKey( aLink.linkId() );
    // check that left object exists
    ISkObject left = coreApi().objService().find( aLink.leftSkid() );
    if( left == null ) {
      return ValidationResult.error( FMT_ERR_NO_LINK_LEFT_OBJ, aLink.leftSkid(), aLink.linkId() );
    }
    // check each of the right objectc
    for( Skid rSkid : aLink.rightSkids() ) {
      // right object class must exist in sysdescr
      ISkClassInfo rightClassInfo = sysdescr().findClassInfo( rSkid.classId() );
      if( rightClassInfo == null ) {
        return ValidationResult.error( FMT_ERR_NO_LINK_RIGHT_OBJ_CLASS, rSkid, aLink.linkId() );
      }
      // right object c lass must be allowed in the link
      if( rightClassInfo.isOfClass( lInfo.rightClassIds() ) ) {
        return ValidationResult.error( FMT_ERR_RIGHT_OBJ_INV_CLASS, rSkid, aLink.linkId() );
      }
      // right object itself must exist
      ISkObject rSkObj = coreApi().objService().find( rSkid );
      if( rSkObj == null ) {
        return ValidationResult.error( FMT_ERR_NO_LINK_RIGHT_OBJ_STRID, rSkid, aLink.linkId() );
      }
    }
    // check number of right objects against link constraints
    ValidationResult vr = lInfo.linkConstraint().checkErrorSize( aLink.rightSkids() );
    if( vr.isError() ) {
      return vr;
    }
    // check duplicate right objects against link constraints
    vr = ValidationResult.firstNonOk( vr, lInfo.linkConstraint().checkErrorDups( aLink.rightSkids() ) );
    if( vr.isError() ) {
      return vr;
    }
    // check right objects list's emptyness against link constraints
    return ValidationResult.firstNonOk( vr, lInfo.linkConstraint().checkWarnEmpty( aLink.rightSkids() ) );
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
  // ApiWrapAbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    // TODO реализовать SkCoreServLinks.onBackendMessage()
    throw new TsUnderDevelopmentRtException( "SkCoreServLinks.onBackendMessage()" );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IDtoLinkFwd readFromBackend( String aDeclaringClassId, String aLinkId, Skid aLeftSkid ) {
    IDtoLinkFwd lf = ba().baLinks().findLinkFwd( aDeclaringClassId, aLinkId, aLeftSkid );
    if( lf != null ) {
      return lf;
    }
    Gwid linkGwid = Gwid.createLink( aDeclaringClassId, aLinkId );
    return new DtoLinkFwd( linkGwid, aLeftSkid, ISkidList.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // ISkLinkService
  //

  @Override
  public IDtoLinkFwd getLinkFwd( Skid aLeftSkid, String aLinkId ) {
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
    return readFromBackend( declaringClassInfo.id(), aLinkId, aLeftSkid );
  }

  @Override
  public IStringMap<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
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
      IDtoLinkFwd lf = readFromBackend( declaringClassInfo.id(), linkId, aLeftSkid );
      map.put( linkId, lf );
    }
    return map;
  }

  @Override
  public IDtoLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid ) {
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
    // проверка наличия объекта
    ISkObject right = coreApi().objService().find( aRightSkid );
    if( right == null ) {
      Gwid linkGwid = Gwid.createLink( aClassId, aLinkId );
      throw new TsItemNotFoundRtException( FMT_ERR_NO_SUCH_OBJ, aRightSkid );
    }
    IDtoLinkRev lr = ba().baLinks().findLinkRev( declaringClassInfo.id(), aLinkId, aRightSkid, IStringList.EMPTY );
    if( lr == null ) {
      Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), aLinkId );
      lr = new DtoLinkRev( linkGwid, aRightSkid, ISkidList.EMPTY );
    }
    return lr;
  }

  @Override
  public IMap<Gwid, ISkidList> getAllLinksRev( Skid aRightSkid ) {
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
    return eventer;
  }

  @Override
  public ITsValidationSupport<ISkLinkServiceValidator> svs() {
    return validationSupport;
  }

}
