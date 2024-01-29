package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * An linkId/SkidList pair - the SkidList identified by the String id.
 *
 * @author dima
 */
public final class LinkIdSkidList
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "LinkIdSkidList"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<LinkIdSkidList> KEEPER =
      new AbstractEntityKeeper<>( LinkIdSkidList.class, EEncloseMode.ENCLOSES_KEEPER_IMPLEMENTATION, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, LinkIdSkidList aEntity ) {
          aSw.writeAsIs( aEntity.linkId() );
          aSw.writeSeparatorChar();
          SkidListKeeper.KEEPER.write( aSw, aEntity.skidList() );
        }

        @Override
        protected LinkIdSkidList doRead( IStrioReader aSr ) {
          String linkId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          ISkidList skidList = SkidListKeeper.KEEPER.read( aSr );
          return new LinkIdSkidList( linkId, skidList );
        }
      };

  private final String    linkId;
  private final ISkidList skidList;

  /**
   * Constructor.
   *
   * @param aLinkId String - the ID of link
   * @param aSkidList {@link ISkidList} - the list of Skid's
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public LinkIdSkidList( String aLinkId, ISkidList aSkidList ) {
    linkId = aLinkId;
    skidList = TsNullArgumentRtException.checkNull( aSkidList );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the link ID.
   *
   * @return String - the ID of link
   */
  public String linkId() {
    return linkId;
  }

  /**
   * Returns the list of Skids.
   *
   * @return {@link ISkidList} - list of Skids
   */
  public ISkidList skidList() {
    return skidList;
  }

  // ------------------------------------------------------------------------------------
  // Static API
  //

  /**
   * Extracts content of {@link MappedSkids} as collection od {@link LinkIdSkidList}.
   *
   * @param aMappedSkids {@link MappedSkids} - the map of LinkId -> SkidList
   * @return {@link IStringMapEdit}&lt;{@link LinkIdSkidList}&gt; - the map "link ID" - "SkidList"
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static IStringMapEdit<LinkIdSkidList> makeLinkIdSkidListCollFromMappedSkid( MappedSkids aMappedSkids ) {
    TsNullArgumentRtException.checkNull( aMappedSkids );
    IStringMapEdit<LinkIdSkidList> map = new StringMap<>();
    for( String linkId : aMappedSkids.map().keys() ) {
      ISkidList skidList = aMappedSkids.map().getByKey( linkId );
      LinkIdSkidList liSl = new LinkIdSkidList( linkId, skidList );
      map.put( linkId, liSl );
    }
    return map;
  }

  /**
   * Copies content of {@link LinkIdSkidList} collection to the {@link MappedSkids}.
   * <p>
   * Existing options will be overwritten. Options not listed in <code>aIdVals</code> will remain intact.
   *
   * @param aLinkIdSkidList {@link ITsCollection}&lt;{@link String}&gt; - collection of linkIds
   * @param aMappedSkids {@link MappedSkids} - map of linkId -> SkidList
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void fillMappedSkidFromLinkIdSkidList( ITsCollection<LinkIdSkidList> aLinkIdSkidList,
      MappedSkids aMappedSkids ) {
    TsNullArgumentRtException.checkNulls( aLinkIdSkidList, aMappedSkids );
    for( LinkIdSkidList liSl : aLinkIdSkidList ) {
      aMappedSkids.ensureSkidList( liSl.linkId(), liSl.skidList() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return linkId + '=' + skidList.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof LinkIdSkidList that ) {
      return linkId.equals( that.linkId ) && skidList.equals( that.skidList );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = INITIAL_HASH_CODE;
    result = PRIME * result + linkId.hashCode();
    result = PRIME * result + skidList.hashCode();
    return result;
  }

}
