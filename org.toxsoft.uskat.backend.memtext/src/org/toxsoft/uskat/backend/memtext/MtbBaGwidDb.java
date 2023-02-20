package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaGwidDb} implementation.
 *
 * @author hazard157
 */
public class MtbBaGwidDb
    extends MtbAbstractAddon
    implements IBaGwidDb {

  /**
   * CLOBs as "section ID" - "IdChain.canonicalString()" - "CLOB".
   */
  private final IStringMapEdit<IStringMapEdit<String>> clobsMap = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaGwidDb( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_GWID_DB );
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
    // TODO Auto-generated method stub
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    // TODO Auto-generated method stub
  }

  // ------------------------------------------------------------------------------------
  // IBaGwidDb
  //

  @Override
  public IList<IdChain> listSectionIds() {
    // TODO реализовать MtbBaGwidDb.listSectionIds()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.listSectionIds()" );
  }

  @Override
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    // TODO реализовать MtbBaGwidDb.listKeys()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.listKeys()" );
  }

  @Override
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    // TODO реализовать MtbBaGwidDb.readValue()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.readValue()" );
  }

  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    // TODO реализовать MtbBaGwidDb.writeValue()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.writeValue()" );
  }

  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    // TODO реализовать MtbBaGwidDb.removeValue()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.removeValue()" );
  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    // TODO реализовать MtbBaGwidDb.removeSection()
    throw new TsUnderDevelopmentRtException( "MtbBaGwidDb.removeSection()" );
  }

}
