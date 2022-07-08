package org.toxsoft.uskat.s5.server.backend.addons.commands;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.core.impl.SkCommand;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Реализация сессии расширения бекенда {@link IS5BaCommandsSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
class S5BaCommandsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaCommandsSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Поддержка сервера для формирования команд
   */
  @EJB
  private IS5BackendCommandSingleton commandsSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaCommandsSession() {
    super( ISkBackendHardConstant.BAINF_EVENTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaCommandsSession> doGetSessionView() {
    return IS5BaCommandsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaCommandsData baData = new S5BaCommandsData();
    S5BaCommandsData initData = aInitData.findBackendAddonData( IBaEvents.ADDON_ID, S5BaCommandsData.class );
    if( initData != null ) {
      baData.commands.setHandledCommandGwids( initData.commands.getHandledCommandGwids() );
    }
    frontend().frontendData().setBackendAddonData( IBaEvents.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaCommandsSession
  //
  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    return commandsSupport.sendCommand( aCmdGwid, aAuthorSkid, aArgs );
  }

  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    // Данные сессии
    S5BaCommandsData baData =
        frontend().frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
    // Реконфигурация набора
    baData.commands.setHandledCommandGwids( aGwids );
    // Сохранение измененной сессии в кластере сервера
    updateSessionData();
    // Вывод протокола
    if( logger().isSeverityOn( ELogSeverity.INFO ) || logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( String.format( "setHandledCommandGwids(...): sessionID = %s, changed executor list:", sessionID() ) ); //$NON-NLS-1$
      sb.append( String.format( "\n   === events (%d) === ", Integer.valueOf( baData.commands.getHandledCommandGwids().size() ) ) ); //$NON-NLS-1$
      for( Gwid gwid : baData.commands.getHandledCommandGwids() ) {
        sb.append( String.format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    commandsSupport.changeCommandState( aStateChangeInfo );
  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    return commandsSupport.listGloballyHandledCommandGwids();
  }

  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    TsNullArgumentRtException.checkNull( aCompletedCommand );
    commandsSupport.saveToHistory( aCompletedCommand );
  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return commandsSupport.queryObjCommands( aInterval, aGwid );
  }
}
