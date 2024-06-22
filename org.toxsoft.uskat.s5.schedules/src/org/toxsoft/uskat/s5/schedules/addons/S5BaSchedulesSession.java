package org.toxsoft.uskat.s5.schedules.addons;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.uskat.s5.schedules.lib.IBaSchedules;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedulesHardConstants;
import org.toxsoft.uskat.s5.schedules.supports.IS5BackendSchedulesSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Сессия реализации службы {@link IBaSchedules}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@SuppressWarnings( "unused" )
public class S5BaSchedulesSession
    extends S5AbstractBackendAddonSession
    implements IS5BaSchedulesSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка бекенда службы
   */
  @EJB
  private IS5BackendSchedulesSingleton schedulesSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaSchedulesSession() {
    super( ISkSchedulesHardConstants.BAINF_SCHEDULES );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaSchedulesSession> doGetSessionView() {
    return IS5BaSchedulesSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaSchedulesData baData = new S5BaSchedulesData();
    frontend().frontendData().setBackendAddonData( IBaSchedules.ADDON_ID, baData );
  }

  @Override
  protected void doBeforeClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IBaSchedules
  //
}
