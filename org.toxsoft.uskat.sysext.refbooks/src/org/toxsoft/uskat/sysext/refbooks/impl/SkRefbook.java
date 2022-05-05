package org.toxsoft.uskat.sysext.refbooks.impl;

import static org.toxsoft.uskat.sysext.refbooks.ISkRefbookServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.impl.SingleItemList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.s5.legacy.SkLinksMap;
import org.toxsoft.uskat.sysext.refbooks.*;

import ru.uskat.common.dpu.impl.DpuObject;
import ru.uskat.common.utils.SkEventBuilder;
import ru.uskat.core.api.links.ISkLinkService;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.SkHelperUtils;
import ru.uskat.core.common.skobject.ISkObjectCreator;
import ru.uskat.core.impl.SkObject;

/**
 * {@link ISkRefbook} implementation.
 *
 * @author goga
 */
class SkRefbook
    extends SkObject
    implements ISkRefbook {

  static final ISkObjectCreator<SkRefbook> CREATOR = SkRefbook::new;

  SkRefbook( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // Implementation
  //

  private void fillEventWithOldValues( SkEventBuilder aEventBuilder, ISkRefbookItem aItem ) {
    if( aItem != null ) {
      aEventBuilder.eventParams().setValobj( EVPRMID_OLD_ATTRS, aItem.attrs() );
      SkLinksMap slMap = new SkLinksMap();
      ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
      ISkClassInfo itemClassInfo = cim.getClassInfo( itemClassId() );
      for( String lid : itemClassInfo.linkInfos().ids() ) {
        SkidList skList = new SkidList();
        skList.addAll( aItem.getLinkRevSkids( itemClassId(), lid ) );
        slMap.mapEdit().put( lid, skList );
      }
      aEventBuilder.eventParams().setValobj( EVPRMID_OLD_LINKS, slMap );
    }
  }

  private void fillEventWithNewValues( SkEventBuilder aEventBuilder, ISkRefbookItem aItem ) {
    if( aItem != null ) {
      aEventBuilder.eventParams().setValobj( EVPRMID_NEW_ATTRS, aItem.attrs() );
      SkLinksMap slMap = new SkLinksMap();
      ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
      ISkClassInfo itemClassInfo = cim.getClassInfo( itemClassId() );
      for( String lid : itemClassInfo.linkInfos().ids() ) {
        SkidList skList = new SkidList();
        skList.addAll( aItem.getLinkRevSkids( itemClassId(), lid ) );
        slMap.mapEdit().put( lid, skList );
      }
      aEventBuilder.eventParams().setValobj( EVPRMID_NEW_LINKS, slMap );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbook
  //

  @Override
  public String itemClassId() {
    return attrs().getStr( OP_ATTR_ITEM_CLASS_ID );
  }

  @Override
  public <T extends ISkRefbookItem> T findItem( String aItemId ) {
    return coreApi().objService().find( new Skid( itemClassId(), aItemId ) );
  }

  @Override
  public ISkidList listItemIds() {
    return coreApi().objService().listSkids( itemClassId(), false );
  }

  @Override
  public <T extends ISkRefbookItem> IList<T> listItems() {
    return coreApi().objService().listObjs( itemClassId(), false );
  }

  @Override
  public ISkRefbookItem defineItem( ISkRefbookDpuItemInfo aItemInfo, IStringMap<ISkidList> aLinks ) {
    // check preconditions
    ISkRefbookItem oldItem = coreApi().objService().find( new Skid( itemClassId(), aItemInfo.strid() ) );
    SkRefbookService rs = (SkRefbookService)coreApi().services().getByKey( ISkRefbookService.SERVICE_ID );
    TsValidationFailedRtException.checkError( rs.svs().validator().canDefineItem( this, aItemInfo, aLinks, oldItem ) );
    // prepare event
    SkEventBuilder eventBuilder = new SkEventBuilder();
    eventBuilder.setEventGwid( Gwid.createEvent( classId(), strid(), EVID_REFBOOK_ITEM_CHANGE ) );
    fillEventWithOldValues( eventBuilder, oldItem );
    // create/edit item
    rs.pauseExternalValidation();
    Skid skid = new Skid( itemClassId(), aItemInfo.strid() );
    ISkRefbookItem rbItem;
    try {
      DpuObject dpuItem = new DpuObject( skid, aItemInfo.attrs() );
      rbItem = coreApi().objService().defineObject( dpuItem );
      // links
      ISkLinkService ls = coreApi().linkService();
      for( String lid : aLinks.keys() ) {
        ls.defineLink( skid, lid, null, aLinks.getByKey( lid ) );
      }
      fillEventWithNewValues( eventBuilder, rbItem );
    }
    finally {
      rs.resumeExternalValidation();
    }
    // fire event
    ECrudOp op = oldItem != null ? ECrudOp.EDIT : ECrudOp.CREATE;
    eventBuilder.eventParams().setValobj( EVPRMID_CRUD_OP, op );
    eventBuilder.eventParams().setValobj( EVPRMID_ITEM_SKID, skid );
    rs.eventer.fireItemsChanged( strid(), new SingleItemList<>( eventBuilder.getEvent() ) );
    return rbItem;
  }

  @Override
  public void removeItem( String aItemId ) {
    // check preconditions
    SkRefbookService rs = (SkRefbookService)coreApi().services().getByKey( ISkRefbookService.SERVICE_ID );
    TsValidationFailedRtException.checkError( rs.svs().validator().canRemoveItem( this, aItemId ) );
    // prepare event
    ISkRefbookItem oldItem = coreApi().objService().find( new Skid( itemClassId(), aItemId ) );
    SkEventBuilder eventBuilder = new SkEventBuilder();
    eventBuilder.setEventGwid( Gwid.createEvent( classId(), strid(), EVID_REFBOOK_ITEM_CHANGE ) );
    fillEventWithOldValues( eventBuilder, oldItem );
    // remove item
    Skid skid = new Skid( itemClassId(), aItemId );
    rs.pauseExternalValidation();
    try {
      coreApi().objService().removeObject( skid );
    }
    finally {
      rs.resumeExternalValidation();
    }
    // inform on item removal event
    eventBuilder.eventParams().setValobj( EVPRMID_CRUD_OP, ECrudOp.REMOVE );
    eventBuilder.eventParams().setValobj( EVPRMID_ITEM_SKID, skid );
    rs.eventer.fireItemsChanged( strid(), new SingleItemList<>( eventBuilder.getEvent() ) );
  }

  @Override
  public SkRefbookDpuInfo getRefbookInfoDpu() {
    ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
    ISkClassInfo itemClassInfo = cim.getClassInfo( itemClassId() );
    SkRefbookDpuInfo dpu = new SkRefbookDpuInfo( id(), nmName(), description() );
    for( ISkAttrInfo ainf : itemClassInfo.attrInfos() ) {
      dpu.itemAttrInfos().add( SkHelperUtils.info2dpu( ainf ) );
    }
    for( ISkLinkInfo linf : itemClassInfo.linkInfos() ) {
      dpu.itemLinkInfos().add( SkHelperUtils.info2dpu( linf ) );
    }
    return dpu;
  }

}
