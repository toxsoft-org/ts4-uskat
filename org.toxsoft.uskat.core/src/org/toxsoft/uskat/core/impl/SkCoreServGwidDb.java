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
   * Internal cache of the clob.
   *
   * @author mvk
   */
  class ClobCache {

    private static final int MAX_SIZE = 256 * 1024;

    private final IMapEdit<IdChain, IList<Gwid>>            allSectionGwids = new ElemMap<>();
    private final IMapEdit<IdChain, IMapEdit<Gwid, String>> clobByGwids     = new ElemMap<>();

    ClobCache() {
      // nop
    }

    boolean has( IdChain aSectionId, Gwid aKey ) {
      return (find( aSectionId, aKey ) != null);
    }

    String find( IdChain aSectionId, Gwid aKey ) {
      IMapEdit<Gwid, String> cache = clobByGwids.findByKey( aSectionId );
      if( cache == null ) {
        return null;
      }
      return cache.findByKey( aKey );
    }

    void put( IdChain aSectionId, Gwid aKey, String aClob ) {
      IMapEdit<Gwid, String> cache = clobByGwids.findByKey( aSectionId );
      if( cache == null ) {
        cache = new ElemMap<>( TsCollectionsUtils.getMapBucketsCount( //
            TsCollectionsUtils.estimateOrder( MAX_SIZE ) ), TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );
        clobByGwids.put( aSectionId, cache );
      }
      if( cache.size() >= MAX_SIZE ) {
        Gwid removeGwid = cache.keys().first();
        cache.removeByKey( removeGwid );
        allSectionGwids.removeByKey( aSectionId );
      }
      cache.put( aKey, aClob );
      allSectionGwids.removeByKey( aSectionId );
    }

    IList<Gwid> findAllSectionGwids( IdChain aSectionId ) {
      return allSectionGwids.findByKey( aSectionId );
    }

    void addAllSectionGwids( IdChain aSectionId, IList<Gwid> aGwids ) {
      allSectionGwids.put( aSectionId, aGwids );
    }

    void removeSection( IdChain aSectionId ) {
      clobByGwids.removeByKey( aSectionId );
      allSectionGwids.removeByKey( aSectionId );
    }

    void removeClob( IdChain aSectionId, Gwid aKey ) {
      IMapEdit<Gwid, String> cache = clobByGwids.findByKey( aSectionId );
      if( cache != null ) {
        cache.removeByKey( aKey );
      }
      allSectionGwids.removeByKey( aSectionId );
    }

    void clear() {
      allSectionGwids.clear();
      clobByGwids.clear();
    }

  }

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

  final ClobCache clobCache = new ClobCache();
  final Eventer   eventer   = new Eventer();

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
        switch( op ) {
          case CREATE:
          case EDIT:
          case REMOVE:
            clobCache.removeClob( sectionId, key );
            break;
          case LIST:
            break;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
        eventer.fireGwidDbChangeEvent( sectionId, key, op );
        yield true;
      }
      default -> false;
    };
  }

  @Override
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    if( aIsActive ) {
      clobCache.clear();
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkGwidDbService
  //

  @Override
  public IList<IdChain> listSectionIds() {
    checkThread();
    return ba().baGwidDb().listSectionIds();
  }

  @Override
  public ISkGwidDbSection defineSection( IdChain aSectionId ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aSectionId );
    return new ISkGwidDbSection() {

      @Override
      public IList<Gwid> listKeys() {
        checkThread();
        IList<Gwid> keys = clobCache.findAllSectionGwids( aSectionId );
        if( keys != null ) {
          return keys;
        }
        keys = new GwidList( ba().baGwidDb().listKeys( aSectionId ) );
        clobCache.addAllSectionGwids( aSectionId, keys );
        return keys;
      }

      @Override
      public boolean hasClob( Gwid aKey ) {
        checkThread();
        TsNullArgumentRtException.checkNull( aKey );
        if( clobCache.find( aSectionId, aKey ) != null ) {
          return true;
        }
        return listKeys().hasElem( aKey );
      }

      @Override
      public void writeClob( Gwid aKey, String aValue ) {
        checkThread();
        TsNullArgumentRtException.checkNulls( aKey, aValue );
        checkKeyExistence( aKey );
        ba().baGwidDb().writeValue( aSectionId, aKey, aValue );
        clobCache.put( aSectionId, aKey, aValue );
      }

      @Override
      public String readClob( Gwid aKey ) {
        checkThread();
        TsNullArgumentRtException.checkNull( aKey );
        String clob = clobCache.find( aSectionId, aKey );
        if( clob != null ) {
          return clob;
        }
        clob = ba().baGwidDb().readValue( aSectionId, aKey );
        clobCache.put( aSectionId, aKey, clob );
        return clob;
      }

      @Override
      public void removeClob( Gwid aKey ) {
        checkThread();
        TsNullArgumentRtException.checkNull( aKey );
        ba().baGwidDb().removeValue( aSectionId, aKey );
        clobCache.removeClob( aSectionId, aKey );
      }

    };
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aSectionId );
    ba().baGwidDb().removeSection( aSectionId );
    clobCache.removeSection( aSectionId );
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
