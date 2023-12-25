package org.toxsoft.uskat.s5.server.backend.addons.classes;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaClasses;
import org.toxsoft.uskat.core.backend.api.IBaClassesMessages;
import org.toxsoft.uskat.s5.common.sysdescr.SkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton;

/**
 * Local {@link IBaClasses} implementation.
 *
 * @author mvk
 */
class S5BaClassesLocal
    extends S5AbstractBackendAddonLocal
    implements IBaClasses {

  /**
   * Читатель системного описания
   */
  private final SkSysdescrReader sysdescrReader;

  /**
   * Поддержка сервера для чтения/записи системного описания
   */
  private final IS5BackendSysDescrSingleton sysdescrSupport;

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaClassesLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_CLASSES );
    // Синглтон поддержки чтения/записи системного описания
    sysdescrSupport = aOwner.backendSingleton().get( S5BackendSysDescrSingleton.BACKEND_SYSDESCR_ID,
        IS5BackendSysDescrSingleton.class );
    // Читатель системного описания
    sysdescrReader = new SkSysdescrReader( () -> sysdescrSupport.readClassInfos() );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
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
    sysdescrSupport.writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }
}
