package org.toxsoft.uskat.s5.cron.addons;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.cron.supports.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

import jakarta.ejb.*;

/**
 * Сессия реализации службы {@link IBaCrone}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaCronSession
    extends S5AbstractBackendAddonSession
    implements IS5BaCronSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы
   */
  @EJB
  private IS5BackendCronSingleton schedulesSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaCronSession() {
    super( ISkCronHardConstants.BAINF_CRON );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaCronSession> doGetSessionView() {
    return IS5BaCronSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaCronData baData = new S5BaCronData();
    frontend().frontendData().setBackendAddonData( IBaCrone.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IBaCrone
  //
}
