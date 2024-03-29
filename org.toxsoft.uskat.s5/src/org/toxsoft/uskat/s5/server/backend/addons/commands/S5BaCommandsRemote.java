package org.toxsoft.uskat.s5.server.backend.addons.commands;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.core.impl.SkCommand;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.messages.S5BaBeforeConnectMessages;

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
