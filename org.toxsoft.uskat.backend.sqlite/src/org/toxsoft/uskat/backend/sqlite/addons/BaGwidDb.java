package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaGwidDb} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaGwidDb
    extends AbstractAddon
    implements IBaGwidDb {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaGwidDb( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_GWID_DB );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // TODO doInit()
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    // TODO clear()
  }

  // ------------------------------------------------------------------------------------
  // IBaGwidDb
  //

  @Override
  public IList<IdChain> listSectionIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IList<Gwid> listKeys( IdChain aSectionId ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String readValue( IdChain aSectionId, Gwid aKey ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void writeValue( IdChain aSectionId, Gwid aKey, String aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeValue( IdChain aSectionId, Gwid aKey ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeSection( IdChain aSectionId ) {
    // TODO Auto-generated method stub

  }

}
