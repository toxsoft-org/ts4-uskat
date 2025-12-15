package org.toxsoft.uskat.s5.server.backend.addons.commands;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.*;

import javax.ejb.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.supports.commands.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

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
public class S5BaCommandsSession
    extends S5AbstractBackendAddonSession
    implements IS5BaCommandsSession, IS5BackendAddonSessionControl {

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
    super( ISkBackendHardConstant.BAINF_COMMANDS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaCommandsSession> doGetSessionView() {
    return IS5BaCommandsSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    S5BaCommandsData baData = new S5BaCommandsData();
    S5BaCommandsData initData = aInitData.findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
    if( initData != null ) {
      baData.commands.setHandledCommandGwids( initData.commands.getHandledCommandGwids() );
    }
    frontend().frontendData().setBackendAddonData( IBaCommands.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaCommandsSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    SkCommand retValue = commandsSupport.sendCommand( aCmdGwid, aAuthorSkid, aArgs );
    if( !retValue.isComplete() ) {
      // Данные сессии
      S5BaCommandsData baData =
          frontend().frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
      // Фиксация факта ожидания выполнения команды
      ValidationResult addResult = baData.commands.addExecutingCmd( retValue.instanceId() );
      // Запись в журнал результата добавления команды в очередь ожидания
      LoggerWrapper.resultToLog( logger(), addResult );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ValidationResult testCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    return commandsSupport.testCommand( aCmdGwid, aAuthorSkid, aArgs );
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    // Данные сессии
    S5BaCommandsData baData =
        frontend().frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
    // Реконфигурация набора
    baData.commands.setHandledCommandGwids( aGwids );
    // Сохранение измененной сессии в кластере сервера
    writeSessionData();
    // Вывод протокола
    if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
      // Вывод в журнал информации о регистрации ресурсов в сессии
      StringBuilder sb = new StringBuilder();
      sb.append( String.format( "setHandledCommandGwids(...): sessionID = %s, changed executor list:", sessionID() ) ); //$NON-NLS-1$
      sb.append( String.format( "\n   === commands (%d) === ", //$NON-NLS-1$
          Integer.valueOf( baData.commands.getHandledCommandGwids().size() ) ) );
      for( Gwid gwid : baData.commands.getHandledCommandGwids() ) {
        sb.append( String.format( "\n   %s", gwid ) ); //$NON-NLS-1$
      }
      logger().info( sb.toString() );
    }
    // Оповещение бекенда
    commandsSupport.setHandledCommandGwids( aGwids );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    commandsSupport.changeCommandState( aStateChangeInfo );
  }

  @Override
  public void changeTestState( String aInstanceId, ValidationResult aResult ) {
    TsNullArgumentRtException.checkNulls( aInstanceId, aResult );
    commandsSupport.changeTestState( aInstanceId, aResult );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    return commandsSupport.listGloballyHandledCommandGwids();
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    TsNullArgumentRtException.checkNull( aCompletedCommand );
    commandsSupport.saveToHistory( aCompletedCommand );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return commandsSupport.queryObjCommands( aInterval, aGwid );
  }
}
