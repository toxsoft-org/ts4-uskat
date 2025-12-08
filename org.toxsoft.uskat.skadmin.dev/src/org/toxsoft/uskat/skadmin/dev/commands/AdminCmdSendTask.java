package org.toxsoft.uskat.skadmin.dev.commands;

import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Задача выполнения команды
 *
 * @author mvk
 */
class AdminCmdSendTask
    implements Runnable {

  private final AdminCmdSend owner;
  private final Gwid         cmdGwid;
  private final IOptionSet   args;
  private final Skid         authorId;

  private ValidationResult result;

  AdminCmdSendTask( AdminCmdSend aOwner, Gwid aCmdGwid, IOptionSet aArgs, Skid aAuthorId ) {
    TsNullArgumentRtException.checkNulls( aOwner, aCmdGwid, aArgs, aAuthorId );
    owner = aOwner;
    cmdGwid = aCmdGwid;
    args = new OptionSet( aArgs );
    authorId = aAuthorId;
  }

  ValidationResult resultOrNull() {
    return result;
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  @Override
  public void run() {
    ISkCoreApi coreApi = owner.coreApi();
    String classId = cmdGwid.classId();
    String cmdId = cmdGwid.propId();
    ISkSysdescr sysdescr = coreApi.sysdescr();
    ISkCommandService commandService = coreApi.cmdService();
    ISkClassInfo classInfo = sysdescr.getClassInfo( classId );
    IDtoCmdInfo cmdInfo = classInfo.cmds().list().findByKey( cmdId );

    // check command arguments are valid
    for( IDataDef argInfo : cmdInfo.argDefs() ) {
      if( !args.hasKey( argInfo.id() ) ) {
        result = ValidationResult.error( MSG_COMMAND_ARG_NOT_FOUND, cmdGwid, argInfo.id() );
        return;
      }
    }
    try {
      ISkCommand cmd = commandService.sendCommand( cmdGwid, authorId, args );
      // Установка слушателя команды
      cmd.stateEventer().addListener( owner );
      // Команда отправлена на выполнение
      owner.println( MSG_COMMAND_SEND, cmd.instanceId(), cmd.cmdGwid() );
    }
    catch( Throwable e ) {
      result = ValidationResult.error( ERR_CANT_EXECUTE, cmdGwid, e.getLocalizedMessage() );
    }
  }

}
