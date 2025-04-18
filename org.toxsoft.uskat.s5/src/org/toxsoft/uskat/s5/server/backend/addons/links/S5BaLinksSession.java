package org.toxsoft.uskat.s5.server.backend.addons.links;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.links.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

/**
 * Реализация сессии расширения бекенда {@link IS5BaLinksSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class S5BaLinksSession
    extends S5AbstractBackendAddonSession
    implements IS5BaLinksSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи связей объектов системы
   */
  @EJB
  private IS5BackendLinksSingleton linksSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaLinksSession() {
    super( ISkBackendHardConstant.BAINF_LINKS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaLinksSession> doGetSessionView() {
    return IS5BaLinksSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaLinksSession
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

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void writeLinksFwd( IList<IDtoLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    linksSupport.writeLinksFwd( aLinks );
  }
}
