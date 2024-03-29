package org.toxsoft.uskat.backend.memtext;

import java.util.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaClobs} implementation.
 *
 * @author hazard157
 */
class MtbBaClobs
    extends MtbAbstractAddon
    implements IBaClobs {

  private static final String KW_CLOB_DATA = "ClobData"; //$NON-NLS-1$

  private final IMapEdit<Gwid, String> clobsMap = new ElemMap<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaClobs( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLOBS );
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
    clobsMap.clear();
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    StrioUtils.writeMap( aSw, KW_CLOB_DATA, clobsMap, Gwid.KEEPER, StringKeeper.KEEPER, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    clobsMap.clear();
    StrioUtils.readMap( aSr, KW_CLOB_DATA, Gwid.KEEPER, StringKeeper.KEEPER, clobsMap );
  }

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    // determine which CLOBs to remove
    IListEdit<Gwid> gwidsToRemove = new ElemLinkedBundleList<>();
    for( String cid : aClassIds ) {
      for( Gwid g : clobsMap.keys() ) {
        if( g.classId().equals( cid ) ) {
          gwidsToRemove.add( g );
        }
      }
    }
    // remove CLOBs
    for( Gwid g : gwidsToRemove ) {
      clobsMap.removeByKey( g );
    }
  }

  // ------------------------------------------------------------------------------------
  // IBaClobs
  //

  @Override
  public String readClob( Gwid aGwid ) {
    internalCheck();
    return clobsMap.findByKey( aGwid );
  }

  @Override
  public void writeClob( Gwid aGwid, String aClob ) {
    internalCheck();
    if( !Objects.equals( clobsMap.findByKey( aGwid ), aClob ) ) {
      clobsMap.put( aGwid, aClob );
      setChanged();
      GtMessage msg = BaMsgClobsChanged.BUILDER.makeMessage( aGwid );
      owner().frontend().onBackendMessage( msg );
    }
  }

}
