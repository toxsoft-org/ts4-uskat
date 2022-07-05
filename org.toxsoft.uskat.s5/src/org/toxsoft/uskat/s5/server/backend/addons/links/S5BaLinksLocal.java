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
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5BackendLinksSingleton;

/**
 * Local {@link IBaLinks} implementation.
 *
 * @author mvk
 */
class S5BaLinksLocal
    extends S5AbstractBackendAddonLocal
    implements IBaLinks {

  /**
   * Поддержка сервера для чтения/записи связей объектов системы
   */
  private final IS5BackendLinksSingleton linksSupport;

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaLinksLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_LINKS );
    // Синглтон поддержки чтения/записи системного описания
    linksSupport =
        aOwner.backendSingleton().get( S5BackendLinksSingleton.BACKEND_LINKS_ID, IS5BackendLinksSingleton.class );
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
    return linksSupport.findLinkFwd( aLinkGwid, aLeftSkid );
  }

  @Override
  public IList<IDtoLinkFwd> getAllLinksFwd( Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNull( aLeftSkid );
    return linksSupport.getAllLinksFwd( aLeftSkid );
  }

  @Override
  public IDtoLinkRev findLinkRev( Gwid aLinkGwid, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aLinkGwid, aRightSkid, aLeftClassIds );
    return linksSupport.findLinkRev( aLinkGwid, aRightSkid, aLeftClassIds );
  }

  @Override
  public IMap<Gwid, IDtoLinkRev> getAllLinksRev( Skid aRightSkid ) {
    TsNullArgumentRtException.checkNull( aRightSkid );
    return linksSupport.getAllLinksRev( aRightSkid );
  }

  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    linksSupport.writeLinksFwd( aLinks );
  }
}
