package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaLinks} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaLinks
    extends AbstractAddon
    implements IBaLinks {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaLinks( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_LINKS );
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
  // IBaLinks
  //

  @Override
  public IDtoLinkFwd findLinkFwd( Gwid aLinkGwid, Skid aLeftSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IDtoLinkRev findLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    // TODO Auto-generated method stub

  }

}
