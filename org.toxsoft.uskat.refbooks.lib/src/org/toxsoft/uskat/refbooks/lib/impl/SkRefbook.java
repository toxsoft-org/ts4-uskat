package org.toxsoft.uskat.refbooks.lib.impl;

import static org.toxsoft.uskat.refbooks.lib.ISkRefbookServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.utils.*;
import org.toxsoft.uskat.refbooks.lib.*;

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
      MappedSkids slMap = new MappedSkids();
      ISkClassInfo itemClassInfo = coreApi().sysdescr().getClassInfo( itemClassId() );
      for( String lid : itemClassInfo.links().list().ids() ) {
        SkidList skList = new SkidList();

        // TODO почему REVERSE ???

        skList.addAll( aItem.getLinkRevSkids( itemClassId(), lid ) );
        slMap.map().put( lid, skList );

        // FIXME rivets

      }
      aEventBuilder.eventParams().setValobj( EVPRMID_OLD_LINKS, slMap );
    }
  }

  private void fillEventWithNewValues( SkEventBuilder aEventBuilder, ISkRefbookItem aItem ) {
    if( aItem != null ) {
      aEventBuilder.eventParams().setValobj( EVPRMID_NEW_ATTRS, aItem.attrs() );
      MappedSkids slMap = new MappedSkids();
      ISkSysdescr cim = coreApi().sysdescr();
      ISkClassInfo itemClassInfo = cim.getClassInfo( itemClassId() );
      for( String lid : itemClassInfo.links().list().ids() ) {
        SkidList skList = new SkidList();

        // TODO почему REVERSE ???

        skList.addAll( aItem.getLinkRevSkids( itemClassId(), lid ) );
        slMap.map().put( lid, skList );

        // FIXME rivets

      }
      aEventBuilder.eventParams().setValobj( EVPRMID_NEW_LINKS, slMap );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbook
  //

  @Override
  public String itemClassId() {
    return attrs().getStr( ATRID_ITEM_CLASS_ID );
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
  public <T extends ISkRefbookItem> IStridablesList<T> listItems() {
    return new StridablesList<>( coreApi().objService().listObjs( itemClassId(), false ) );
  }

  @Override
  public ISkRefbookItem defineItem( IDtoFullObject aItemInfo ) {
    // check preconditions
    ISkRefbookItem oldItem = coreApi().objService().find( new Skid( itemClassId(), aItemInfo.strid() ) );
    SkExtServiceRefbooks rbServ = (SkExtServiceRefbooks)coreApi().services().getByKey( ISkRefbookService.SERVICE_ID );
    TsValidationFailedRtException.checkError( rbServ.svs().validator().canDefineItem( this, aItemInfo, oldItem ) );
    // prepare event
    SkEventBuilder eventBuilder = new SkEventBuilder();
    eventBuilder.setEventGwid( Gwid.createEvent( classId(), strid(), EVID_REFBOOK_ITEM_CHANGE ) );
    fillEventWithOldValues( eventBuilder, oldItem );
    // create/edit item
    rbServ.pauseCoreValidation();
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
      rbServ.resumeCoreValidation();
    }
    // fire event
    ECrudOp op = oldItem != null ? ECrudOp.EDIT : ECrudOp.CREATE;
    eventBuilder.eventParams().setValobj( EVPRMID_CRUD_OP, op );
    eventBuilder.eventParams().setValobj( EVPRMID_ITEM_SKID, skid );
    rbServ.eventer.fireItemsChanged( strid(), new SingleItemList<>( eventBuilder.getEvent() ) );
    return rbItem;
  }

  @Override
  public void removeItem( String aItemId ) {
    // check preconditions
    SkExtServiceRefbooks rs = (SkExtServiceRefbooks)coreApi().services().getByKey( ISkRefbookService.SERVICE_ID );
    TsValidationFailedRtException.checkError( rs.svs().validator().canRemoveItem( this, aItemId ) );
    // prepare event
    ISkRefbookItem oldItem = coreApi().objService().find( new Skid( itemClassId(), aItemId ) );
    SkEventBuilder eventBuilder = new SkEventBuilder();
    eventBuilder.setEventGwid( Gwid.createEvent( classId(), strid(), EVID_REFBOOK_ITEM_CHANGE ) );
    fillEventWithOldValues( eventBuilder, oldItem );
    // remove item
    Skid skid = new Skid( itemClassId(), aItemId );
    rs.pauseCoreValidation();
    try {
      coreApi().objService().removeObject( skid );
    }
    finally {
      rs.resumeCoreValidation();
    }
    // inform on item removal event
    eventBuilder.eventParams().setValobj( EVPRMID_CRUD_OP, ECrudOp.REMOVE );
    eventBuilder.eventParams().setValobj( EVPRMID_ITEM_SKID, skid );
    rs.eventer.fireItemsChanged( strid(), new SingleItemList<>( eventBuilder.getEvent() ) );
  }

}
