package org.toxsoft.uskat.s5.server.backend.addons.commands;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.core.impl.SkCommand;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.commands.impl.S5BackendCommandSingleton;

/**
 * Local {@link IBaCommands} implementation.
 *
 * @author mvk
 */
class S5BaCommandsLocal
    extends S5AbstractBackendAddonLocal
    implements IBaCommands {

  /**
   * Поддержка сервера обработки команд
   */
  private final IS5BackendCommandSingleton commandsSupport;

  /**
   * Данные конфигурации фронтенда для {@link IBaCommands}
   */
  private final S5BaCommandsData baData = new S5BaCommandsData();

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaCommandsLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_COMMANDS );
    // Синглтон поддержки чтения/записи системного описания
    commandsSupport = aOwner.backendSingleton().get( S5BackendCommandSingleton.BACKEND_COMMANDS_ID,
        IS5BackendCommandSingleton.class );
    // Установка конфигурации фронтенда
    frontend().frontendData().setBackendAddonData( IBaCommands.ADDON_ID, baData );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
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
    SkCommand retValue = commandsSupport.sendCommand( aCmdGwid, aAuthorSkid, aArgs );
    if( !retValue.isComplete() ) {
      // Фиксация факта ожидания выполнения команды
      ValidationResult addResult = baData.commands.addExecutingCmd( retValue.instanceId() );
      // Запись в журнал результата добавления команды в очередь ожидания
      LoggerWrapper.resultToLog( logger(), addResult );
    }
    return retValue;
  }

  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    baData.commands.setHandledCommandGwids( aGwids );
    // Оповещение бекенда
    commandsSupport.setHandledCommandGwids( aGwids );
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
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return commandsSupport.queryObjCommands( aInterval, aGwid );
  }
}
