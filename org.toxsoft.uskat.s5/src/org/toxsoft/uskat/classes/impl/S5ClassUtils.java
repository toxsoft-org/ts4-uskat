package org.toxsoft.uskat.classes.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.classes.impl.IS5Resources.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.supports.links.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.statistics.*;

/**
 * Константы пакета
 *
 * @author mvk
 */
public class S5ClassUtils {

  /**
   * Обновление описания sk-классов
   *
   * @param aBackendInfo {@link ISkBackendInfo} информация о бекенде
   * @param aSysdescrSupport {@link IS5BackendSysDescrSingleton} поддержка системного описания
   * @param aObjectsSupport {@link IS5BackendObjectsSingleton} поддержка объектов
   * @param aLinksSupport {@link IS5BackendLinksSingleton} поддержка связей объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void updateCoreSysdescr( ISkBackendInfo aBackendInfo, IS5BackendSysDescrSingleton aSysdescrSupport,
      IS5BackendObjectsSingleton aObjectsSupport, IS5BackendLinksSingleton aLinksSupport ) {
    TsNullArgumentRtException.checkNulls( aBackendInfo, aSysdescrSupport, aObjectsSupport, aLinksSupport );

    // Описания обновляемых классов
    IStridablesListEdit<IDtoClassInfo> dtoInfos = new StridablesList<>();
    // ISkNetNode
    dtoInfos.add( internalCreateNetNodeClassDto() );
    // ISkServer
    dtoInfos.add( internalCreateServerClassDto() );
    // ISkServerNode
    dtoInfos.add( internalCreateServerNodeClassDto() );
    // ISkServerBackend
    dtoInfos.add( internalCreateServerBackendClassDto() );
    // ISkServerHistorable
    dtoInfos.add( internalCreateServerHistorableClassDto() );
    // ISkUser
    dtoInfos.add( internalCreateUserClassDto() );
    // ISkSession
    dtoInfos.add( internalCreateSessionClassDto() );
    // Обновление описания классов
    aSysdescrSupport.writeClassInfos( IStringList.EMPTY, dtoInfos );

    // Создание пользователя root
    if( aObjectsSupport.findObject( ISkUserServiceHardConstants.SKID_USER_ROOT ) == null ) {
      IOptionSetEdit attrs = new OptionSet();
      attrs.setStr( AID_NAME, STR_ROOT_USER );
      attrs.setStr( AID_DESCRIPTION, STR_ROOT_USER_D );
      attrs.setStr( ISkUserServiceHardConstants.ATRID_PASSWORD_HASH,
          SkHelperUtils.getPasswordHashCode( ISkUserServiceHardConstants.INITIAL_ROOT_PASSWORD ) );
      IDtoObject root =
          new DtoObject( ISkUserServiceHardConstants.SKID_USER_ROOT, attrs, IStringMap.EMPTY, IStringMap.EMPTY );
      aObjectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( root ), true );
    }

    // Идентификатор сервера
    Skid serverId = OP_SERVER_ID.getValue( aBackendInfo.params() ).asValobj();
    // Идентификатор узла сервера
    Skid nodeId = OP_SERVER_NODE_ID.getValue( aBackendInfo.params() ).asValobj();

    // Проверка существования сервера
    // if( aObjectsSupport.findObject( serverId ) == null ) {
    IOptionSetEdit attrs = new OptionSet();
    DDEF_NAME.setValue( attrs, avStr( STR_CLASS_SERVER ) );
    DDEF_DESCRIPTION.setValue( attrs, avStr( STR_CLASS_SERVER_D ) );
    // Сервер не найден. Создание сервера
    IDtoObject server = new DtoObject( serverId, attrs, IStringMap.EMPTY, IStringMap.EMPTY );
    // aInterceptable = true
    aObjectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( server ), true );
    // }

    // Проверка существования узла сервера
    // if( aObjectsSupport.findObject( nodeId ) == null ) {
    DDEF_NAME.setValue( attrs, avStr( STR_CLASS_NETNODE ) );
    DDEF_DESCRIPTION.setValue( attrs, avStr( STR_CLASS_NETNODE_D ) );
    // Узел не найден. Создание узла
    IDtoObject node = new DtoObject( nodeId, attrs, IStringMap.EMPTY, IStringMap.EMPTY );
    // aInterceptable = true
    aObjectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( node ), true );
    // Установка связи
    Gwid linkGwid = Gwid.createLink( ISkServerNode.CLASS_ID, ISkServerNode.LNKID_SERVER );
    IDtoLinkFwd linkFwd = new DtoLinkFwd( linkGwid, nodeId, new SkidList( serverId ) );
    aLinksSupport.writeLinksFwd( new ElemArrayList<>( linkFwd ) );
    // }
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //
  private static IDtoClassInfo internalCreateNetNodeClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( //
        ISkNetNode.CLASS_ID, //
        IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_CLASS_NETNODE, //
            DDEF_DESCRIPTION, STR_CLASS_NETNODE_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );
    retValue.linkInfos().addAll( //
        DtoLinkInfo.create1( //
            ISkNetNode.LNKID_RESOURCES, //
            new SingleStringList( IGwHardConstants.GW_ROOT_CLASS_ID ), //
            // aMaxCount = 0, aIsExactCount = false, aIsEmptyProhibited = false, aIsDuplicatesProhibited = true
            new CollConstraint( 0, false, false, true ), //
            OptionSetUtils.createOpSet( //
                DDEF_NAME, STR_LNKID_NETNODE_RESOURCES, //
                DDEF_DESCRIPTION, STR_LNKID_NETNODE_RESOURCES_D ) ) //
    );
    retValue.rtdataInfos().addAll( //
        DtoRtdataInfo.create2( //
            ISkNetNode.RTDID_ONLINE, //
            DataType.create( VALOBJ, //
                TSID_FORMAT_STRING, FMT_BOOL_CHECK, //
                TSID_DEFAULT_VALUE, avValobj( EConnState.OFFLINE ) //
            ), //
            true, true, false, 1000, //
            TSID_NAME, STR_RTD_NETNODE_ONLINE, //
            TSID_DESCRIPTION, STR_RTD_NETNODE_ONLINE_D //
        ), //
        DtoRtdataInfo.create2( //
            ISkNetNode.RTDID_HEALTH, //
            DataType.create( INTEGER, //
                TSID_DEFAULT_VALUE, AV_0 //
            ), //
            true, true, false, 1000, //
            TSID_NAME, STR_RTD_NETNODE_HEALTH, //
            TSID_DESCRIPTION, STR_RTD_NETNODE_HEALTH_D //
        ) //
    );
    return retValue;
  }

  private static IDtoClassInfo internalCreateServerClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( //
        ISkServer.CLASS_ID, //
        ISkNetNode.CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_CLASS_SERVER, //
            DDEF_DESCRIPTION, STR_CLASS_SERVER_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );
    retValue.eventInfos().addAll( //
        DtoEventInfo.create1( ISkServer.EVID_LOGIN_FAILED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_LOGIN, //
                    TSID_DESCRIPTION, STR_EV_PARAM_LOGIN_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_IP, //
                    TSID_DESCRIPTION, STR_EV_PARAM_IP_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_LOGIN_FAILED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_LOGIN_FAILED_D //
            ) ), //
        DtoEventInfo.create1( ISkServer.EVID_SESSION_CREATED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_LOGIN, //
                    TSID_DESCRIPTION, STR_EV_PARAM_LOGIN_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_IP, //
                    TSID_DESCRIPTION, STR_EV_PARAM_IP_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_SESSION_ID, EAtomicType.VALOBJ, TSID_NAME, STR_EV_SESSION_ID, //
                    TSID_DESCRIPTION, STR_EV_SESSION_ID_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_SESSION_CREATED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_SESSION_CREATED_D//
            ) ), //
        DtoEventInfo.create1( ISkServer.EVID_SESSION_CLOSED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_LOGIN, //
                    TSID_DESCRIPTION, STR_EV_PARAM_LOGIN_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_IP, //
                    TSID_DESCRIPTION, STR_EV_PARAM_IP_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_SESSION_ID, EAtomicType.VALOBJ, TSID_NAME, STR_EV_SESSION_ID, //
                    TSID_DESCRIPTION, STR_EV_SESSION_ID_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_SESSION_CLOSED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_SESSION_CLOSED_D//
            ) ), //
        DtoEventInfo.create1( ISkServer.EVID_SESSION_BREAKED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_LOGIN, //
                    TSID_DESCRIPTION, STR_EV_PARAM_LOGIN_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_IP, //
                    TSID_DESCRIPTION, STR_EV_PARAM_IP_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_SESSION_ID, EAtomicType.VALOBJ, TSID_NAME, STR_EV_SESSION_ID, //
                    TSID_DESCRIPTION, STR_EV_SESSION_ID_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_SESSION_BREAKED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_SESSION_BREAKED_D//
            ) ), //
        DtoEventInfo.create1( ISkServer.EVID_SESSION_RESTORED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_LOGIN, //
                    TSID_DESCRIPTION, STR_EV_PARAM_LOGIN_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_IP, //
                    TSID_DESCRIPTION, STR_EV_PARAM_IP_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_SESSION_ID, EAtomicType.VALOBJ, TSID_NAME, STR_EV_SESSION_ID, //
                    TSID_DESCRIPTION, STR_EV_SESSION_ID_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_SESSION_RESTORED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_SESSION_RESTORED_D//
            ) ), //
        DtoEventInfo.create1( ISkServer.EVID_SYSDESCR_CHANGED, true, //
            new StridablesList<>( //
                DataDef.create( ISkServer.EVPID_USER, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_USER, //
                    TSID_DESCRIPTION, STR_EV_PARAM_USER_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_DESCR, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_DESCR, //
                    TSID_DESCRIPTION, STR_EV_PARAM_DESCR_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                DataDef.create( ISkServer.EVPID_EDITOR, EAtomicType.STRING, TSID_NAME, STR_EV_PARAM_EDITOR, //
                    TSID_DESCRIPTION, STR_EV_PARAM_EDITOR_D, //
                    TSID_IS_NULL_ALLOWED, AV_FALSE, //
                    TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
            ), //
            OptionSetUtils.createOpSet( //
                IAvMetaConstants.TSID_NAME, STR_EV_SYSDESCR_CHANGED, //
                IAvMetaConstants.TSID_DESCRIPTION, STR_EV_SYSDESCR_CHANGED_D //
            ) //
        )//
    );
    return retValue;
  }

  private static IDtoClassInfo internalCreateServerNodeClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( //
        ISkServerNode.CLASS_ID, //
        ISkNetNode.CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_CLASS_SERVERNODE, //
            DDEF_DESCRIPTION, STR_CLASS_SERVERNODE_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );
    retValue.linkInfos().addAll( //
        DtoLinkInfo.create1( //
            ISkServerNode.LNKID_SERVER, //
            new SingleStringList( ISkServer.CLASS_ID ), //
            new CollConstraint( 1, true, true, true ), //
            OptionSetUtils.createOpSet( //
                DDEF_NAME, STR_LNKID_SERVERNODE_SERVER, //
                DDEF_DESCRIPTION, STR_LNKID_SERVERNODE_SERVER_D ) ) //
    );
    retValue.rtdataInfos().addAll( listStatRtdInfos( IS5ServerHardConstants.STAT_BACKEND_NODE_PARAMS ) );
    return retValue;
  }

  private static IDtoClassInfo internalCreateServerBackendClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( //
        ISkServerBackend.CLASS_ID, //
        IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_CLASS_SERVERBACKEND, //
            DDEF_DESCRIPTION, STR_CLASS_SERVERBACKEND_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );
    retValue.linkInfos().addAll( //
        DtoLinkInfo.create1( //
            ISkServerBackend.LNKID_NODE, //
            new SingleStringList( ISkServerNode.CLASS_ID ), //
            new CollConstraint( 1, true, true, true ), //
            OptionSetUtils.createOpSet( //
                DDEF_NAME, STR_LNKID_SERVERBACKEND_NODE, //
                DDEF_DESCRIPTION, STR_LNKID_SERVERBACKEND_NODE_D //
            ) ) //
    );
    return retValue;
  }

  private static IDtoClassInfo internalCreateServerHistorableClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( //
        ISkServerHistorable.CLASS_ID, //
        ISkServerBackend.CLASS_ID, OptionSetUtils.createOpSet( //
            DDEF_NAME, STR_CLASS_SERVERHISTORABLE, //
            DDEF_DESCRIPTION, STR_CLASS_SERVERHISTORABLE_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );
    retValue.rtdataInfos().addAll( listStatRtdInfos( IS5ServerHardConstants.STAT_HISTORABLE_BACKEND_PARAMS ) );
    return retValue;
  }

  private static IDtoClassInfo internalCreateSessionClassDto() {
    DtoClassInfo retValue = new DtoClassInfo( ISkSession.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID, //
        OptionSetUtils.createOpSet( //
            IAvMetaConstants.TSID_NAME, STR_CLASS_SESSION, //
            IAvMetaConstants.TSID_DESCRIPTION, STR_CLASS_SESSION_D, //
            OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE, //
            OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS, AV_FALSE //
        ) );

    // ATRID_STARTTIME
    retValue.attrInfos().add( DtoAttrInfo.create1( ISkSession.ATRID_STARTTIME, DataType.create( TIMESTAMP, //
        TSID_NAME, STR_ATRID_SESSION_STARTTIME, //
        TSID_DESCRIPTION, STR_ATRID_SESSION_STARTTIME_D //
    ), //
        IOptionSet.NULL ) );
    // ATRID_ENDTIME
    retValue.attrInfos().add( DtoAttrInfo.create1( ISkSession.ATRID_ENDTIME, DataType.create( TIMESTAMP, //
        TSID_NAME, STR_ATRID_SESSION_ENDTIME, //
        TSID_DESCRIPTION, STR_ATRID_SESSION_ENDTIME_D //
    ), IOptionSet.NULL ) );
    // ATRID_BACKEND_SPECIFIC_PARAMS
    retValue.attrInfos().add( DtoAttrInfo.create1( ISkSession.ATRID_BACKEND_SPECIFIC_PARAMS, DataType.create( VALOBJ, //
        TSID_NAME, STR_ATRID_SESSION_BACKEND_SPECIFIC_PARAMS, //
        TSID_DESCRIPTION, STR_ATRID_SESSION_BACKEND_SPECIFIC_PARAMS_D, //
        TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
    ), IOptionSet.NULL ) );
    // ATRID_CONNECTION_CREATION_PARAMS
    retValue.attrInfos().add( DtoAttrInfo.create1( ISkSession.ATRID_CONNECTION_CREATION_PARAMS, DataType.create( VALOBJ, //
        TSID_NAME, STR_ATRID_SESSION_CONNECTION_CREATION_PARAMS, //
        TSID_DESCRIPTION, STR_ATRID_SESSION_CONNECTION_CREATION_PARAMS_D, //
        TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
    ), IOptionSet.NULL ) );

    retValue.linkInfos().addAll( //
        // LNKID_USER
        DtoLinkInfo.create2( ISkSession.LNKID_USER, //
            new SingleStringList( ISkUser.CLASS_ID ), new CollConstraint( 1, true, true, false ), //
            TSID_NAME, STR_LNKID_SESSION_USER, //
            TSID_DESCRIPTION, STR_LNKID_SESSION_USER_D //
        ) );

    // RTDID_STATE
    retValue.rtdataInfos().addAll( //
        DtoRtdataInfo.create1( ISkSession.RTDID_STATE, //
            DDEF_BOOLEAN, true, true, false, 1000, //
            OptionSetUtils.createOpSet( //
                TSID_NAME, STR_RTDID_SESSION_STATE, //
                TSID_DESCRIPTION, STR_RTDID_SESSION_STATE_D ) //
        ) );
    return retValue;
  }

  /**
   * Проверяет, если необходимо добавляет в класс указанного объекта, указанные параметры статистики
   *
   * @param aStatInfos {@link IStridablesList} список описаний параметров статистики
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IList<IDtoRtdataInfo> listStatRtdInfos( IStridablesList<S5StatisticParamInfo> aStatInfos ) {
    TsNullArgumentRtException.checkNulls( aStatInfos );
    IListEdit<IDtoRtdataInfo> retValue = new ElemLinkedList<>();
    for( S5StatisticParamInfo statInfo : aStatInfos ) {
      for( IS5StatisticInterval interval : statInfo.intervals() ) {
        String dataId = S5StatisticWriter.getDataId( interval, statInfo.id() );
        // Описание базового данного
        IDtoRtdataInfo info = DtoRtdataInfo.create1( //
            dataId, new DataType( statInfo.atomicType(), OptionSetUtils.createOpSet( //
                DDEF_DEFAULT_VALUE, statInfo.params().getValue( TSID_DEFAULT_VALUE ) ) ),
            true, true, true, interval.milli(), //
            OptionSetUtils.createOpSet( //
                DDEF_NAME, statInfo.nmName() + '(' + interval.nmName() + ')', //
                DDEF_DESCRIPTION, statInfo.description() + '(' + statInfo.description() + ')' ) );
        retValue.add( info );
      }
    }
    return retValue;
  }
}
