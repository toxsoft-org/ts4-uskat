package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.backend.api.IBaLinksMessages.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import java.util.*;

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
import org.toxsoft.core.tslib.utils.logs.*;
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
   * Internal cache of the links.
   */
  class LinksCache {

    private static final int MAX_SIZE = 256 * 1024;

    private final IMapEdit<Gwid, IMapEdit<Skid, IDtoLinkFwd>> objsByLinkGwids = new ElemMap<>();
    private final GwidList                                    allObjGwids     = new GwidList();

    LinksCache() {
      // nop
    }

    boolean has( Gwid aLinkGwid, Skid aSkid ) {
      IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( aLinkGwid );
      if( cache == null ) {
        return false;
      }
      return cache.hasKey( aSkid );
    }

    IDtoLinkFwd find( Gwid aLinkGwid, Skid aSkid ) {
      IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( aLinkGwid );
      if( cache == null ) {
        return null;
      }
      return cache.findByKey( aSkid );
    }

    @SuppressWarnings( "unchecked" )
    <T extends IDtoLinkFwd> IList<T> listObjs( Gwid aLinkGwid ) {
      IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( aLinkGwid );
      return (cache == null ? IList.EMPTY : (IList<T>)cache.values());
    }

    IDtoLinkFwd put( Gwid aLinkGwid, IDtoLinkFwd aLink ) {
      IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( aLinkGwid );
      if( cache == null ) {
        cache = new ElemMap<>( TsCollectionsUtils.getMapBucketsCount( //
            TsCollectionsUtils.estimateOrder( MAX_SIZE ) ), TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );
        objsByLinkGwids.put( aLinkGwid, cache );
      }
      if( cache.size() >= MAX_SIZE ) {
        Skid removeObjId = cache.keys().first();
        cache.removeByKey( removeObjId );
        allObjGwids.remove( aLinkGwid );
      }
      cache.put( aLink.leftSkid(), aLink );
      return aLink;
    }

    boolean hasAllObjGwids( Gwid aLinkGwid ) {
      return allObjGwids.hasElem( aLinkGwid );
    }

    void addAllObjGwids( Gwid aLinkGwid ) {
      if( !allObjGwids.hasElem( aLinkGwid ) ) {
        allObjGwids.add( aLinkGwid );
      }
    }

    void removeAllObjGwids( String aClassId ) {
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( aClassId );
      ISkClassProps<IDtoLinkInfo> linkInfos = classInfo.links();
      for( IDtoLinkInfo linkInfo : linkInfos.list() ) {
        String linkId = linkInfo.id();
        Gwid linkGwid = Gwid.createLink( linkInfos.findSuperDeclarer( linkId ).id(), linkId );
        allObjGwids.remove( linkGwid );
      }
    }

    void removeByClassId( String aClassId ) {
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( aClassId );
      ISkClassProps<IDtoLinkInfo> linkInfos = classInfo.links();
      for( IDtoLinkInfo linkInfo : linkInfos.list() ) {
        String linkId = linkInfo.id();
        Gwid linkGwid = Gwid.createLink( linkInfos.findSuperDeclarer( linkId ).id(), linkId );
        IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( linkGwid );
        if( cache != null ) {
          cache.clear();
        }
      }
      removeAllObjGwids( aClassId );
    }

    void removeByObjId( Skid aSkid ) {
      String classId = aSkid.classId();
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( classId );
      ISkClassProps<IDtoLinkInfo> linkInfos = classInfo.links();
      for( IDtoLinkInfo linkInfo : linkInfos.list() ) {
        String linkId = linkInfo.id();
        Gwid linkGwid = Gwid.createLink( linkInfos.findSuperDeclarer( linkId ).id(), linkId );
        IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( linkGwid );
        if( cache != null ) {
          cache.removeByKey( aSkid );
        }
      }
      removeAllObjGwids( classId );
    }

    void removeByConcreteLink( Gwid aConcreteLink ) {
      String classId = aConcreteLink.classId();
      Gwid linkGwid = Gwid.createLink( classId, aConcreteLink.propId() );
      IMapEdit<Skid, IDtoLinkFwd> cache = objsByLinkGwids.findByKey( linkGwid );
      if( cache != null ) {
        cache.removeByKey( aConcreteLink.skid() );
      }
      removeAllObjGwids( classId );
    }

    void clear() {
      objsByLinkGwids.clear();
      allObjGwids.clear();
    }
  }

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
          logger.error( ex );
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

  final LinksCache        linksCache        = new LinksCache();
  final Eventer           eventer           = new Eventer();
  final ValidationSupport validationSupport = new ValidationSupport();
  private final ILogger   logger            = LoggerUtils.getLogger( getClass() );

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
    return switch( aMessage.messageId() ) {
      case MSGID_LINKS_CHANGE -> {
        IGwidList gwidList = extractGwidList( aMessage );
        for( Gwid gwid : gwidList ) {
          linksCache.removeByConcreteLink( gwid );
        }
        eventer.fireLinksChanged( gwidList );
        yield true;
      }
      default -> false;
    };
  }

  @Override
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    // 2026-02-05 mvk +++
    if( aIsActive ) {
      // TODO: 2026-04-08 mvk: нужно ли вместо очистки кэша сделать пакетный запрос для обновления ???. Если да, тоо
      // делать это по параметру конфигурации или безусловно?
      linksCache.clear();
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //
  private IList<IDtoLinkFwd> readFromBackend( IGwidList aLinkGwids ) {
    GwidList baLinkGwids = new GwidList();
    for( Gwid linkGwid : aLinkGwids ) {
      if( !linksCache.hasAllObjGwids( linkGwid ) ) {
        baLinkGwids.add( linkGwid );
      }
    }
    if( baLinkGwids.size() > 0 ) {
      IList<IDtoLinkFwd> baLinks = ba().baLinks().getAllLinksFwd( baLinkGwids );
      for( IDtoLinkFwd baLink : baLinks ) {
        linksCache.put( baLink.gwid(), baLink );
      }
      for( Gwid baLinkGwid : baLinkGwids ) {
        linksCache.addAllObjGwids( baLinkGwid );
      }
    }
    IListEdit<IDtoLinkFwd> retValue = new ElemLinkedList<>();
    for( Gwid linkGwid : aLinkGwids ) {
      retValue.addAll( linksCache.objsByLinkGwids.getByKey( linkGwid ) );
    }
    return retValue;
  }

  private IDtoLinkFwd readFromBackend( Gwid aLinkGwid, Skid aLeftSkid ) {
    IDtoLinkFwd lf = linksCache.find( aLinkGwid, aLeftSkid );
    if( lf != null ) {
      return lf;
    }
    lf = ba().baLinks().findLinkFwd( aLinkGwid, aLeftSkid );
    if( lf == null ) {
      lf = new DtoLinkFwd( aLinkGwid, aLeftSkid, ISkidList.EMPTY );
    }
    linksCache.put( aLinkGwid, lf );
    return lf;
  }

  // ------------------------------------------------------------------------------------
  // ISkLinkService
  //
  @Override
  public IMap<Skid, IStringMap<IDtoLinkFwd>> getLinkFwds( IStringList aClassIds ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aClassIds );
    GwidList baLinkGwids = new GwidList();
    for( String classId : aClassIds ) {
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( classId );
      ISkClassProps<IDtoLinkInfo> linkInfos = classInfo.links();
      for( IDtoLinkInfo linkInfo : linkInfos.list() ) {
        String linkId = linkInfo.id();
        Gwid linkGwid = Gwid.createLink( linkInfos.findSuperDeclarer( linkId ).id(), linkId );
        if( baLinkGwids.hasElem( linkGwid ) ) {
          continue;
        }
        baLinkGwids.add( linkGwid );
      }
    }
    IList<IDtoLinkFwd> links = readFromBackend( baLinkGwids );
    IMapEdit<Skid, IStringMap<IDtoLinkFwd>> retValue = new ElemMap<>();
    for( IDtoLinkFwd link : links ) {
      Skid objId = link.leftSkid();
      IStringMapEdit<IDtoLinkFwd> objLinks = (IStringMapEdit<IDtoLinkFwd>)retValue.findByKey( objId );
      if( objLinks == null ) {
        objLinks = new StringMap<>();
        retValue.put( objId, objLinks );
      }
      objLinks.put( link.linkId(), link );
    }
    return retValue;
  }

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
    // trace0
    long trace0 = System.currentTimeMillis();
    // read link
    IDtoLinkFwd retValue = readFromBackend( Gwid.createLink( declaringClassInfo.id(), aLinkId ), aLeftSkid );
    logger().info( FMT_MSG_GET_LINK_FWD, aLeftSkid, aLinkId, Long.valueOf( System.currentTimeMillis() - trace0 ) );
    return retValue;
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
    // trace0
    long trace0 = System.currentTimeMillis();
    // iterate over all links
    ISkClassInfo leftClassInfo = sysdescr().getClassInfo( aLeftSkid.classId() );
    IStringMapEdit<IDtoLinkFwd> map = new StringMap<>();
    for( String linkId : leftClassInfo.links().list().ids() ) {
      ISkClassInfo declaringClassInfo = leftClassInfo.links().findSuperDeclarer( linkId );
      Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), linkId );
      IDtoLinkFwd lf = readFromBackend( linkGwid, aLeftSkid );
      map.put( linkId, lf );
    }
    logger().info( FMT_MSG_GET_ALL_LINKS_FWD, aLeftSkid, Integer.valueOf( map.size() ),
        Long.valueOf( System.currentTimeMillis() - trace0 ) );
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
    // trace0
    long trace0 = System.currentTimeMillis();
    IDtoLinkRev lr = ba().baLinks().findLinkRev( linkGwid, aRightSkid, IStringList.EMPTY );
    logger().info( FMT_MSG_GET_LINK_REV, aClassId, aLinkId, aRightSkid,
        Long.valueOf( System.currentTimeMillis() - trace0 ) );
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
    // trace0
    long trace0 = System.currentTimeMillis();
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
      ba().baLinks().writeLinksFwd( new SingleItemList<>( newLink ) );
    }
    logger().info( FMT_MSG_DEFINE_LINK, aLeftSkid, aLinkId, aLinkId, aRemovedSkids, aAddedSkids,
        Long.valueOf( System.currentTimeMillis() - trace0 ) );
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
    // trace0
    long trace0 = System.currentTimeMillis();
    // remove all links
    IListEdit<IDtoLinkFwd> ll = new ElemArrayList<>();
    for( String lid : allLinkIds ) {
      ISkClassInfo declaringClassInfo = objClassInfo.links().findSuperDeclarer( lid );
      Gwid linkGwid = Gwid.createLink( declaringClassInfo.id(), lid );
      IDtoLinkFwd link = new DtoLinkFwd( linkGwid, aLeftSkid, ISkidList.EMPTY );
      ll.add( link );
    }
    ba().baLinks().writeLinksFwd( ll );
    logger().info( FMT_MSG_REMOVE_LINK, aLeftSkid, Integer.valueOf( ll.size() ),
        Long.valueOf( System.currentTimeMillis() - trace0 ) );
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
