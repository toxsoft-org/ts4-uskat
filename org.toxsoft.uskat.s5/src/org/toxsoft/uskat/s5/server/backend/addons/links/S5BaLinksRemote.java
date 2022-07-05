package org.toxsoft.uskat.s5.server.backend.addons.links;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkFwd;
import org.toxsoft.uskat.core.api.linkserv.IDtoLinkRev;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaLinks;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

/**
 * Remote {@link IBaLinks} implementation.
 *
 * @author mvk
 */
class S5BaLinksRemote
    extends S5AbstractBackendAddonRemote<IS5BaLinksSession>
    implements IBaLinks {

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaLinksRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_LINKS, IS5BaLinksSession.class );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaLinks
  //
  @Override
  public IDtoLinkFwd findLinkFwd( Gwid aLinkGwid, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aLinkGwid, aLeftSkid );
    return session().findLinkFwd( aLinkGwid, aLeftSkid );
  }

  @Override
  public IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNull( aLeftSkid );
    return session().getAllLinksFwd( aLeftSkid );
  }

  @Override
  public IDtoLinkRev findLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aLinkGwid, aRightSkid, aLeftClassIds );
    return session().findLinkRev( aLinkGwid, aRightSkid, aLeftClassIds );
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    TsNullArgumentRtException.checkNull( aRightSkid );
    return session().getAllLinksRev( aRightSkid );
  }

  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    session().writeLinksFwd( aLinks );
  }

}
