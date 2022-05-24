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
            aSw.writeSeparatorChar();
          }
          aSw.decNewLine();
        }
        aSw.writeChar( CHAR_ARRAY_END );
        aSw.writeSeparatorChar();
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
    IMapEdit<Skid, IDtoLinkFwd> map = linksMap.removeByKey( aLinkGwid );
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

  private IMapEdit<Skid, IDtoLinkFwd> internalCreateSingleLinkMap() {
    return new ElemMap<>( //
        getMapBucketsCount( estimateOrder( 10_000 ) ), //
        getListInitialCapacity( estimateOrder( 10_000 ) ) );

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
    // TODO реализовать MtbBaLinks.getAllLinksFwd()
    throw new TsUnderDevelopmentRtException( "MtbBaLinks.getAllLinksFwd()" );
  }

  @Override
  public IDtoLinkRev findLinkRev( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    internalCheck();
    // TODO реализовать MtbBaLinks.findLinkRev()
    throw new TsUnderDevelopmentRtException( "MtbBaLinks.findLinkRev()" );
  }

  @Override
  public IList<IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    internalCheck();
    // TODO реализовать MtbBaLinks.getAllLinksRev()
    throw new TsUnderDevelopmentRtException( "MtbBaLinks.getAllLinksRev()" );
  }

  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    internalCheck();
    GwidList changedConcreteGwids = new GwidList();
    for( IDtoLinkFwd lf : aLinks ) {
      // remove link if enpty specified
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
        Gwid g = Gwid.createLink( lf.leftSkid().classId(), lf.leftSkid().strid(), lf.gwid().classId() );
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
