package org.toxsoft.uskat.sysext.batchop.addon;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.sysext.batchop.addon.ISkResources.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5BackendLobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5BackendAddonSession;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

import ru.uskat.backend.ISkBackendInfo;
import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.*;
import ru.uskat.common.dpu.container.*;
import ru.uskat.core.api.ISkBackend;
import ru.uskat.core.api.ISkExtServicesProvider;
import ru.uskat.core.common.helpers.batchop.SkBatchOperationsSupport;
import ru.uskat.legacy.IdPair;

/**
 * Сессия реализации расширения бекенда {@link ISkBackendAddonBatchOperations}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
public class SkBatchOperationsSession
    extends S5BackendAddonSession
    implements ISkBatchOperationsRemote, ISkBatchOperationsSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Менеджер сессий
   */
  @EJB
  private IS5SessionManager sessionManager;

  /**
   * backend системного описания
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления объектами
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * backend управления связями между объектами
   */
  @EJB
  private IS5BackendLinksSingleton linksBackend;

  /**
   * backend управления большими объектами объектами (Large OBject - LOB) системы
   */
  @EJB
  private IS5BackendLobsSingleton lobsBackend;

  /**
   * Пустой конструктор.
   */
  public SkBatchOperationsSession() {
    super( SK_BACKEND_ADDON_ID, STR_N_BACKEND_BATCH );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BackendAddonSession> doGetLocalView() {
    return ISkBatchOperationsSession.class;
  }

  @Override
  protected Class<? extends IS5BackendAddonRemote> doGetRemoteView() {
    return ISkBatchOperationsRemote.class;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonBatchOperations
  //
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public IValResList batchUpdate( IDpuIdContainer aToRemove, IDpuContainer aAddAndUpdate ) {
    TsNullArgumentRtException.checkNulls( aToRemove, aAddAndUpdate );
    // Выполнение запроса
    IValResList retValue = SkBatchOperationsSupport.batchUpdate( new InternalBackend(), aToRemove, aAddAndUpdate );
    if( retValue.results().size() > 0 ) {
      logger().info( retValue.results().first().message() );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public IValResList batchUpdate( IDpuIdContainer aToRemove, String aAddAndUpdate ) {
    TsNullArgumentRtException.checkNulls( aToRemove, aAddAndUpdate );
    IDpuContainer dpuContainer = DpuContainerKeeper.KEEPER.str2ent( aAddAndUpdate );
    return batchUpdate( aToRemove, dpuContainer );
  }

  @Override
  public IDpuContainer batchRead( IOptionSet aReadOptions, ITsCombiFilterParams aClassIdsFilter,
      ITsCombiFilterParams aDataTypeIdsFilter, ITsCombiFilterParams aClobIdsFilter ) {
    TsNullArgumentRtException.checkNulls( aReadOptions, aClassIdsFilter, aDataTypeIdsFilter, aClobIdsFilter );
    return SkBatchOperationsSupport.batchRead( sysdescrBackend.getReader(), backend(), aReadOptions, aClassIdsFilter,
        aDataTypeIdsFilter, aClobIdsFilter );
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация бекенда
  //
  @SuppressWarnings( "synthetic-access" )
  private class InternalBackend
      implements ISkBackend {

    @Override
    public void close() {
      // nop
    }

    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public ISkBackendInfo getInfo() {
      return backend().getInfo();
    }

    @Override
    public IStridablesList<IDpuSdTypeInfo> readTypeInfos() {
      return sysdescrBackend.readTypeInfos();
    }

    @Override
    public void writeTypeInfos( IStringList aRemoveTypeIds, IList<IDpuSdTypeInfo> aUpdateTypeInfos ) {
      TsNullArgumentRtException.checkNulls( aRemoveTypeIds, aUpdateTypeInfos );
      sysdescrBackend.writeTypeInfos( aRemoveTypeIds, aUpdateTypeInfos );
    }

    @Override
    public IStridablesList<IDpuSdClassInfo> readClassInfos() {
      return sysdescrBackend.readClassInfos();
    }

    @Override
    public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos ) {
      TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
      sysdescrBackend.writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
    }

    @Override
    public IDpuObject findObject( Skid aSkid ) {
      TsNullArgumentRtException.checkNull( aSkid );
      return objectsBackend.findObject( aSkid );
    }

    @Override
    public IList<IDpuObject> readObjects( IStringList aClassIds ) {
      TsNullArgumentRtException.checkNull( aClassIds );
      return objectsBackend.readObjects( aClassIds );
    }

    @Override
    public IList<IDpuObject> readObjectsByIds( ISkidList aSkids ) {
      TsNullArgumentRtException.checkNull( aSkids );
      return objectsBackend.readObjectsByIds( aSkids );
    }

    @Override
    public void writeObjects( ISkidList aRemoveSkids, IList<IDpuObject> aUpdateObjects ) {
      TsNullArgumentRtException.checkNulls( aRemoveSkids, aUpdateObjects );
      // Передатчик обратных вызовов (frontend)
      S5SessionCallbackWriter callbackWriter = sessionManager.getCallbackWriter( sessionID() );
      // aInterceptable = true
      objectsBackend.writeObjects( callbackWriter, aRemoveSkids, aUpdateObjects, true );
    }

    @Override
    public IDpuLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
      TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
      return linksBackend.findLink( aClassId, aLinkId, aLeftSkid );
    }

    @Override
    public IDpuLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
      TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
      return linksBackend.readLink( aClassId, aLinkId, aLeftSkid );
    }

    @Override
    public IDpuLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
      TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid, aLeftClassIds );
      return linksBackend.readReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds );
    }

    @Override
    public void writeLink( IDpuLinkFwd aLink ) {
      TsNullArgumentRtException.checkNull( aLink );
      linksBackend.writeLink( aLink );
    }

    @Override
    public void writeLinks( IList<IDpuLinkFwd> aLinks ) {
      TsNullArgumentRtException.checkNull( aLinks );
      linksBackend.writeLinks( aLinks, true );
    }

    @Override
    public IList<IdPair> listLobIds() {
      return lobsBackend.listLobIds();
    }

    @Override
    public void writeClob( IdPair aId, String aData ) {
      TsNullArgumentRtException.checkNulls( aId, aData );
      lobsBackend.writeClob( aId, aData );
    }

    @Override
    public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
      TsNullArgumentRtException.checkNulls( aSourceId, aDestId );
      return lobsBackend.copyClob( aSourceId, aDestId );
    }

    @Override
    public String readClob( IdPair aId ) {
      TsNullArgumentRtException.checkNull( aId );
      return lobsBackend.readClob( aId );
    }

    @Override
    public void removeLob( IdPair aId ) {
      TsNullArgumentRtException.checkNull( aId );
      lobsBackend.removeClob( aId );
    }

    @Override
    public ISkExtServicesProvider getExtServicesProvider() {
      throw new TsUnsupportedFeatureRtException();
    }

    @Override
    public <T> T getBackendAddon( String aAddonId, Class<T> aAddonInterface ) {
      throw new TsUnsupportedFeatureRtException();
    }
  }
}
