package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants.*;
import static org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils.*;
import static org.toxsoft.uskat.core.backend.api.IBaLinksMessages.*;

import java.util.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaLinks} implementation.
 *
 * @author hazard157
 */
public class MtbBaLinks
    extends MtbAbstractAddon
    implements IBaLinks {

  private static final String KW_LINKS_BY_GWIDS = "LinksByGwids"; //$NON-NLS-1$

  /**
   * Links map "link abstract GWID" - "left SKID" - "link (includes right SKIDs)"
   */
  private final IMapEdit<Gwid, IMapEdit<Skid, IDtoLinkFwd>> linksMap = new ElemMap<>( //
      getMapBucketsCount( estimateOrder( 1_000 ) ), //
      getListInitialCapacity( estimateOrder( 1_000 ) ) );

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaLinks( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_LINKS );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    linksMap.clear();
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    StrioUtils.writeKeywordHeader( aSw, KW_LINKS_BY_GWIDS, true );
    aSw.writeChar( CHAR_ARRAY_BEGIN );
    if( !linksMap.isEmpty() ) {
      aSw.incNewLine();
      // iterate over abstract links
      for( Gwid linkGwid : linksMap.keys() ) {
        Gwid.KEEPER.write( aSw, linkGwid );
        aSw.writeChar( CHAR_EQUAL );
        aSw.writeChar( CHAR_ARRAY_BEGIN );
        IMap<Skid, IDtoLinkFwd> map = linksMap.getByKey( linkGwid );
        if( !map.isEmpty() ) {
          aSw.incNewLine();
          // iterate over left SKIDs of current abstract link
          for( Skid leftSkid : map.keys() ) {
            Skid.KEEPER.write( aSw, leftSkid );
            aSw.writeChar( CHAR_EQUAL );
            IDtoLinkFwd lf = map.getByKey( leftSkid );
            Skid.KEEPER.writeColl( aSw, lf.rightSkids(), false );
            if( leftSkid != map.keys().last() ) {
              aSw.writeSeparatorChar();
              aSw.writeEol();
            }
          }
          aSw.decNewLine();
        }
        aSw.writeChar( CHAR_ARRAY_END );
        if( linkGwid != linksMap.keys().last() ) {
          aSw.writeSeparatorChar();
          aSw.writeEol();
        }
      }
      aSw.decNewLine();
    }
    aSw.writeChar( CHAR_ARRAY_END );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    linksMap.clear();
    StrioUtils.ensureKeywordHeader( aSr, KW_LINKS_BY_GWIDS );
    if( aSr.readArrayBegin() ) {
      do {
        Gwid linkGwid = Gwid.KEEPER.read( aSr );
        aSr.ensureChar( CHAR_EQUAL );
        if( aSr.readArrayBegin() ) {
          IMapEdit<Skid, IDtoLinkFwd> map = new ElemMap<>();
          do {
            Skid leftSkid = Skid.KEEPER.read( aSr );
            aSr.ensureChar( CHAR_EQUAL );
            IListEdit<Skid> rightSkids = Skid.KEEPER.readColl( aSr );
            IDtoLinkFwd lf = DtoLinkFwd.createDirect( linkGwid, leftSkid, SkidList.createDirect( rightSkids ) );
            map.put( leftSkid, lf );
          } while( aSr.readArrayNext() );
          linksMap.put( linkGwid, map );
        }
      } while( aSr.readArrayNext() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    /**
     * We'll remove left objects of specified classes, not the abstract links of specified classes.
     */
    // however if any abstract link will remain with empty content in #linksMap it will be removed
    IListEdit<Gwid> abstrcatLinksToRemoveDueToAnEmptyContent = new ElemArrayList<>();
    for( Gwid abstractLink : linksMap.keys() ) {
      IMapEdit<Skid, IDtoLinkFwd> map = linksMap.getByKey( abstractLink );
      // remove from map all SKIDs of specified classes
      IListEdit<Skid> skidsToRemove = new ElemLinkedBundleList<>();
      for( Skid skid : map.keys() ) {
        if( aClassIds.hasElem( skid.classId() ) ) {
          skidsToRemove.add( skid );
        }
      }
      for( Skid skid : skidsToRemove ) {
        map.removeByKey( skid );
      }
      if( map.isEmpty() ) {
        abstrcatLinksToRemoveDueToAnEmptyContent.add( abstractLink );
      }
    }
    // remove (if any) empty values for abstract link GWIDs in #linksMap
    for( Gwid g : abstrcatLinksToRemoveDueToAnEmptyContent ) {
      linksMap.removeByKey( g );
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Removes link from {@link #linksMap}.
   *
   * @param aLinkGwid {@link Gwid} - abstract GWID of the link to be removed
   * @param aLeftSkid {@link Skid} - left objectSKID of the link to be removed
   * @return {@link Gwid} - concrete GWID of removed link or <code>null</code> if nothing was removed
   */
  private Gwid internalRemoveLink( Gwid aLinkGwid, Skid aLeftSkid ) {
    Gwid concreteGwidOfTheRemovedLink = null;
    IMapEdit<Skid, IDtoLinkFwd> map = linksMap.findByKey( aLinkGwid );
    if( map != null ) {
      if( map.removeByKey( aLeftSkid ) != null ) {
        concreteGwidOfTheRemovedLink = Gwid.createLink( aLeftSkid.classId(), aLeftSkid.strid(), aLinkGwid.propId() );
        if( map.isEmpty() ) {
          linksMap.removeByKey( aLinkGwid );
        }
      }
    }
    return concreteGwidOfTheRemovedLink;
  }

  private static IMapEdit<Skid, IDtoLinkFwd> internalCreateSingleLinkMap() {
    return new ElemMap<>( //
        getMapBucketsCount( estimateOrder( 10_000 ) ), //
        getListInitialCapacity( estimateOrder( 10_000 ) ) );
  }

  private static IMapEdit<Gwid, IDtoLinkRev> internalCreateRevLinksMap() {
    return new ElemMap<>( //
        getMapBucketsCount( estimateOrder( 1_000 ) ), //
        getListInitialCapacity( estimateOrder( 1_000 ) ) );
  }

  // ------------------------------------------------------------------------------------
  // IBaLinks
  //

  @Override
  public IDtoLinkFwd findLinkFwd( Gwid aLinkGwid, Skid aLeftSkid ) {
    internalCheck();
    IMap<Skid, IDtoLinkFwd> map = linksMap.findByKey( aLinkGwid );
    if( map != null ) {
      return map.findByKey( aLeftSkid );
    }
    return null;
  }

  @Override
  public IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    internalCheck();
    IListEdit<IDtoLinkFwd> ll = new ElemLinkedBundleList<>();
    for( IMap<Skid, IDtoLinkFwd> map : linksMap.values() ) {
      for( Skid s : map.keys() ) {
        if( s.equals( aLeftSkid ) ) {
          ll.add( map.getByKey( s ) );
        }
      }
    }
    return ll;
  }

  @Override
  public IDtoLinkRev findLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    internalCheck();
    // get all data of the specified link
    IMap<Skid, IDtoLinkFwd> gwidLinks = linksMap.findByKey( aLinkGwid );
    if( gwidLinks == null ) {
      return null;
    }
    // iterate over found map
    DtoLinkRev result = new DtoLinkRev( aLinkGwid, aRightSkid, ISkidList.EMPTY );
    for( Skid leftSkid : gwidLinks.keys() ) {
      // consider only allowed class of left SKIDs
      if( aLeftClassIds.isEmpty() || aLeftClassIds.hasElem( leftSkid.classId() ) ) {
        IDtoLinkFwd link = gwidLinks.getByKey( leftSkid );
        if( link.rightSkids().hasElem( aRightSkid ) ) {
          result.leftSkids().add( leftSkid );
        }
      }
    }
    if( result.leftSkids().isEmpty() ) {
      return null;
    }
    return result;
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    internalCheck();
    IMapEdit<Gwid, IDtoLinkRev> result = internalCreateRevLinksMap();
    for( Gwid g : linksMap.keys() ) {
      IMap<Skid, IDtoLinkFwd> gwidLinks = linksMap.getByKey( g );
      DtoLinkRev revLink = new DtoLinkRev( g, aRightSkid, ISkidList.EMPTY );
      // iterate over all left SKIDs
      for( Skid leftSkid : gwidLinks.keys() ) {
        // consider only allowed class of left SKIDs
        IDtoLinkFwd lf = gwidLinks.getByKey( leftSkid );
        if( lf.rightSkids().hasElem( aRightSkid ) ) {
          revLink.leftSkids().add( leftSkid );
        }
      }
      if( !revLink.leftSkids().isEmpty() ) {
        result.put( g, revLink );
      }
    }
    return result;
  }

  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    internalCheck();
    GwidList changedConcreteGwids = new GwidList();
    for( IDtoLinkFwd lf : aLinks ) {
      // remove link if < specified
      if( lf.rightSkids().isEmpty() ) {
        Gwid g = internalRemoveLink( lf.gwid(), lf.leftSkid() );
        if( g != null && !changedConcreteGwids.hasElem( g ) ) {
          changedConcreteGwids.add( g );
        }
        continue;
      }
      // add/change link if needed
      IMapEdit<Skid, IDtoLinkFwd> map = linksMap.findByKey( lf.gwid() );
      if( map == null ) {
        map = internalCreateSingleLinkMap();
        linksMap.put( lf.gwid(), map );
      }
      if( !Objects.equals( lf, map.findByKey( lf.leftSkid() ) ) ) {
        map.put( lf.leftSkid(), lf );
        // 2025-11-18 mvk fix ---+++
        // Gwid g = Gwid.createLink( lf.leftSkid().classId(), lf.leftSkid().strid(), lf.gwid().classId() );
        Gwid g = Gwid.createLink( lf.leftSkid().classId(), lf.leftSkid().strid(), lf.linkId() );
        if( !changedConcreteGwids.hasElem( g ) ) {
          changedConcreteGwids.add( g );
        }
      }
    }
    // process if any change occured
    if( !changedConcreteGwids.isEmpty() ) {
      setChanged();
      GtMessage msg = makeMessage( changedConcreteGwids );
      owner().frontend().onBackendMessage( msg );
    }
  }

}
