package org.toxsoft.uskat.s5.server.backend.addons.classes;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.common.sysdescr.SkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendRemote;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonRemote;

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
   * @param aOwner {@link IS5BackendRemote} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaClassesRemote( IS5BackendRemote aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES, IS5BaClassesSession.class );
    // Читатель системного описания
    sysdescrReader = new SkSysdescrReader( () -> session().readClassInfos() );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    if( BackendMsgActiveChanged.INSTANCE.isOwnMessage( aMessage )
        && BackendMsgActiveChanged.INSTANCE.getActive( aMessage ) ) {
      // Подключение к серверу, обработка полученных классов
      sysdescrReader
          .setClassInfos( owner().sessionInitResult().getBackendAddonData( id(), S5BaClassesData.class ).classInfos );
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
    // Проверка и если необходимо обновление кэша
    sysdescrReader.invalidateCacheIfNeed( aRemoveClassIds, aUpdateClassInfos );
    // Запись в бекенд
    session().writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }

}
