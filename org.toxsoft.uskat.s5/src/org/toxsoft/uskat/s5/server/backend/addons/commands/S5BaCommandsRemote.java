package org.toxsoft.uskat.s5.server.backend.addons.commands;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;

/**
 * Remote {@link IBaCommands} implementation.
 *
 * @author mvk
 */
class S5BaCommandsRemote
    extends S5AbstractBackendAddonRemote<IS5BaCommandsSession>
    implements IBaCommands {

  /**
   * Данные конфигурации фронтенда для {@link IBaCommands}
   */
  private final S5BaCommandsData baData = new S5BaCommandsData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaCommandsRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_COMMANDS, IS5BaCommandsSession.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaCommands.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    if( aMessage.messageId().equals( S5BaBeforeConnectMessages.MSG_ID ) ) {
      owner().sessionInitData().setBackendAddonData( IBaCommands.ADDON_ID, baData );
    }
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaCommands
  //
  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    return session().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
  }

  @Override
  public ValidationResult testCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    return session().testCommand( aCmdGwid, aAuthorSkid, aArgs );
  }

  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    baData.commands.setHandledCommandGwids( aGwids );
    IS5BaCommandsSession session = findSession();
    if( session != null ) {
      session.setHandledCommandGwids( aGwids );
    }
  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    session().changeCommandState( aStateChangeInfo );
  }

  @Override
  public void changeTestState( String aInstanceId, ValidationResult aResult ) {
    TsNullArgumentRtException.checkNulls( aInstanceId, aResult );
    session().changeTestState( aInstanceId, aResult );
  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    return session().listGloballyHandledCommandGwids();
  }

  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    TsNullArgumentRtException.checkNull( aCompletedCommand );
    session().saveToHistory( aCompletedCommand );
  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return session().queryObjCommands( aInterval, aGwid );
  }
}
