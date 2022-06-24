package org.toxsoft.uskat.s5.server.backend.addons.classes;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClasses;
import org.toxsoft.uskat.core.backend.api.IBaClassesMessages;
import org.toxsoft.uskat.s5.client.remote.S5BackendRemote;
import org.toxsoft.uskat.s5.common.sysdescr.SkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.messages.IS5BaAfterConnectMessages;

/**
 * Remote {@link IBaClasses} implementation.
 *
 * @author mvk
 */
class S5BaClassesRemote
    extends S5AbstractBackendAddonRemote<IS5BaClassesSession>
    implements IBaClasses {

  /**
   * Читатель системного описания
   */
  private final SkSysdescrReader sysdescrReader;

  /**
   * Constructor.
   *
   * @param aOwner {@link S5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaClassesRemote( S5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES, IS5BaClassesSession.class );
    // Читатель системного описания
    sysdescrReader = new SkSysdescrReader( () -> session().readClassInfos() );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    if( aMessage.messageId().equals( IS5BaAfterConnectMessages.MSGID ) ) {
      // Подключение к серверу, обработка полученных классов
      sysdescrReader
          .setClassInfos( owner().sessionInitResult().getBaData( id(), S5BaClassesInitResult.class ).classInfos );
    }
    if( aMessage.messageId().equals( IBaClassesMessages.MSGID_SYSDESCR_CHANGE ) ) {
      // Изменились описания классов системы, обновление кэша
      sysdescrReader.invalidateCache();
    }
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaClasses
  //
  @Override
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    return sysdescrReader.readClassInfos();
  }

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    session().writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }

}
