package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;

/**
 * {@link ISkGwidDbService} implementation.
 *
 * @author mvk
 */
public class SkCoreServGwidDb
    extends AbstractSkCoreService
    implements ISkGwidDbService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServGwidDb::new;

  /**
   * {@link ISkGwidDbService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkGwidDbServiceListener> {

    private final IMapEdit<IdChain, IMapEdit<Gwid, ECrudOp>> changedGwids = new ElemMap<>();

    @Override
    protected boolean doIsPendingEvents() {
      return !changedGwids.isEmpty();
    }

    @Override
    protected void doFirePendingEvents() {
      for( IdChain sectionId : changedGwids.keys() ) {
        IMap<Gwid, ECrudOp> sectionGwids = changedGwids.getByKey( sectionId );
        for( Gwid key : sectionGwids.keys() ) {
          reallyFireEvent( sectionId, key, sectionGwids.getByKey( key ) );
        }
      }
    }

    @Override
    protected void doClearPendingEvents() {
      changedGwids.clear();
    }

    private void reallyFireEvent( IdChain aSectionId, Gwid aKey, ECrudOp aOp ) {
      for( ISkGwidDbServiceListener l : listeners() ) {
        l.onGwidDbChange( SkCoreServGwidDb.this, aSectionId, aOp, aKey );
      }
    }

    public void fireGwidDbChangeEvent( IdChain aSectionId, Gwid aKey, ECrudOp aOp ) {
      if( isFiringPaused() ) {
        // put changed GWID at the end of the list
        IMapEdit<Gwid, ECrudOp> sectionGwids = changedGwids.findByKey( aSectionId );
        if( sectionGwids == null ) {
          sectionGwids = new ElemMap<>();
          changedGwids.put( aSectionId, sectionGwids );
        }
        sectionGwids.put( aKey, aOp );
        return;
      }
      reallyFireEvent( aSectionId, aKey, aOp );
    }

  }

  final Eventer eventer = new Eventer();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServGwidDb( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
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
      case BaMsgGwidDbChanged.MSG_ID -> {
        IdChain sectionId = BaMsgGwidDbChanged.BUILDER.getSectionId( aMessage );
        Gwid key = BaMsgGwidDbChanged.BUILDER.getKey( aMessage );
        ECrudOp op = BaMsgGwidDbChanged.BUILDER.getCrudOp( aMessage );
        eventer.fireGwidDbChangeEvent( sectionId, key, op );
        yield true;
      }
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // ISkGwidDbService
  //

  @Override
  public IList<IdChain> listSectionIds() {
    return ba().baGwidDb().listSectionIds();
  }

  @Override
  public ISkGwidDbSection defineSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    return new ISkGwidDbSection() {

      @Override
      public IList<Gwid> listKeys() {
        return ba().baGwidDb().listKeys( aSectionId );
      }

      @Override
      public boolean hasClob( Gwid aKey ) {
        TsNullArgumentRtException.checkNull( aKey );
        return listKeys().hasElem( aKey );
      }

      @Override
      public void writeClob( Gwid aKey, String aValue ) {
        TsNullArgumentRtException.checkNulls( aKey, aValue );
        checkKeyExistence( aKey );
        ba().baGwidDb().writeValue( aSectionId, aKey, aValue );
      }

      @Override
      public String readClob( Gwid aKey ) {
        TsNullArgumentRtException.checkNull( aKey );
        return ba().baGwidDb().readValue( aSectionId, aKey );
      }

      @Override
      public void removeClob( Gwid aKey ) {
        TsNullArgumentRtException.checkNull( aKey );
        ba().baGwidDb().removeValue( aSectionId, aKey );
      }

    };
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    TsNullArgumentRtException.checkNull( aSectionId );
    ba().baGwidDb().removeSection( aSectionId );
  }

  @Override
  public ITsEventer<ISkGwidDbServiceListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //

  private void checkKeyExistence( Gwid aKey ) {
    TsNullArgumentRtException.checkNull( aKey );
    if( aKey.isMulti() ) {
      throw new TsItemNotFoundRtException();
    }
    if( !coreApi().gwidService().exists( aKey ) ) {
      throw new TsItemNotFoundRtException();
    }
  }
}
