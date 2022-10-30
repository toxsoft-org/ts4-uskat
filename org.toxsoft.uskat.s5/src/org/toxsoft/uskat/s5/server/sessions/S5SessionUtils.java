package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5Resources.*;

import org.jboss.ejb.client.SessionID;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants;
import org.toxsoft.uskat.core.impl.SkCoreServUsers;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.legacy.ISkSystem;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSession;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
public class S5SessionUtils {

  private static final String STR_N_ROOT_USER       = "Root";                                      //$NON-NLS-1$
  private static final String STR_D_ROOT_USER       = "Root - superuser";                          //$NON-NLS-1$
  private static final String DEFAULT_ROOT_PASSWORD = S5BackendSession.getPasswordHashCode( "1" ); //$NON-NLS-1$

  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link SessionID} идентификатор сессии
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   * @return String строка представляющая сессию
   */
  public static String sessionIDToString( SessionID aSessionID, boolean aFormat ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    if( !aFormat ) {
      return s.substring( s.indexOf( '[' ) + 1, s.indexOf( ']' ) );
    }
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }

  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   * @return String строка представляющая сессию
   */
  public static String sessionIDToString( Skid aSessionID, boolean aFormat ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    if( !aFormat ) {
      return s.substring( s.indexOf( '[' ) + 1, s.indexOf( ']' ) );
    }
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Проверяет и, если необходимо, обновляет системное описание для работы менеджера сессий
   *
   * @param aCoreSupport {@link IS5BackendObjectsSingleton} поддержка ядра сервера
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void checkAndUpdateSysdecr( IS5BackendCoreSingleton aCoreSupport ) {
    TsNullArgumentRtException.checkNull( aCoreSupport );
    IS5BackendSysDescrSingleton sysdescrSupport =
        aCoreSupport.get( S5BackendSysDescrSingleton.BACKEND_SYSDESCR_ID, IS5BackendSysDescrSingleton.class );
    IS5BackendObjectsSingleton objectsSupport =
        aCoreSupport.get( S5BackendObjectsSingleton.BACKEND_OBJECTS_ID, IS5BackendObjectsSingleton.class );
    // Существующие классы
    IStridablesList<IDtoClassInfo> classes = sysdescrSupport.readClassInfos();
    // Добавляемые класы
    IStridablesListEdit<IDtoClassInfo> newClasses = new StridablesList<>();
    // Создание класса система
    if( !classes.hasKey( ISkSystem.CLASS_ID ) ) {
      // Создание класса ISkSystem
      DtoClassInfo dtoSystem = new DtoClassInfo( ISkSystem.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID, //
          OptionSetUtils.createOpSet( //
              IAvMetaConstants.TSID_NAME, STR_N_SYSTEM, //
              IAvMetaConstants.TSID_DESCRIPTION, STR_D_SYSTEM //
          ) );
      dtoSystem.eventInfos().addAll( //
          DtoEventInfo.create1( ISkSystem.EVID_LOGIN_FAILED, true, //
              new StridablesList<>( //
                  DataDef.create( ISkSystem.EVPID_LOGIN, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_LOGIN, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_LOGIN, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_IP, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_IP, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_IP, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
              ), //
              OptionSetUtils.createOpSet( //
                  IAvMetaConstants.TSID_NAME, STR_N_EV_LOGIN_FAILED, //
                  IAvMetaConstants.TSID_DESCRIPTION, STR_D_EV_LOGIN_FAILED //
              ) ), //
          DtoEventInfo.create1( ISkSystem.EVID_SYSDESCR_CHANGED, true, //
              new StridablesList<>( //
                  DataDef.create( ISkSystem.EVPID_USER, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_USER, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_USER, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_DESCR, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_DESCR, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_DESCR, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ), //
                  DataDef.create( ISkSystem.EVPID_EDITOR, EAtomicType.STRING, TSID_NAME, STR_N_EV_PARAM_EDITOR, //
                      TSID_DESCRIPTION, STR_D_EV_PARAM_EDITOR, //
                      TSID_IS_NULL_ALLOWED, AV_FALSE, //
                      TSID_DEFAULT_VALUE, AV_STR_EMPTY ) //
              ), //
              OptionSetUtils.createOpSet( //
                  IAvMetaConstants.TSID_NAME, STR_N_EV_SYSDESCR_CHANGED, //
                  IAvMetaConstants.TSID_DESCRIPTION, STR_D_EV_SYSDESCR_CHANGED //
              ) //
          )//
      );
      newClasses.add( dtoSystem );
    }
    // Создание класса пользователя
    if( !classes.hasKey( ISkUser.CLASS_ID ) ) {
      newClasses.add( SkCoreServUsers.internalCreateUserClassDto() );
    }
    // Создание класса сессия
    if( !classes.hasKey( ISkSession.CLASS_ID ) ) {
      // Создание класса ISkSession
      DtoClassInfo dtoSession = new DtoClassInfo( ISkSession.CLASS_ID, IGwHardConstants.GW_ROOT_CLASS_ID, //
          OptionSetUtils.createOpSet( //
              IAvMetaConstants.TSID_NAME, STR_N_SESSION, //
              IAvMetaConstants.TSID_DESCRIPTION, STR_D_SESSION //
          ) );

      // AID_STARTTIME
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_STARTTIME, DataType.create( TIMESTAMP, //
          TSID_NAME, STR_N_AID_STARTTIME, //
          TSID_DESCRIPTION, STR_D_AID_STARTTIME //
      ), //
          IOptionSet.NULL ) );
      // AID_ENDTIME
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_ENDTIME, DataType.create( TIMESTAMP, //
          TSID_NAME, STR_N_AID_ENDTIME, //
          TSID_DESCRIPTION, STR_D_AID_ENDTIME //
      ), IOptionSet.NULL ) );
      // AID_BACKEND_SPECIFIC_PARAMS
      dtoSession.attrInfos().add( DtoAttrInfo.create1( ISkSession.AID_BACKEND_SPECIFIC_PARAMS, DataType.create( VALOBJ, //
          TSID_NAME, STR_N_AID_BACKEND_SPECIFIC_PARAMS, //
          TSID_DESCRIPTION, STR_D_AID_BACKEND_SPECIFIC_PARAMS, //
          TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
          TSID_IS_NULL_ALLOWED, AV_FALSE, //
          TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
      ), IOptionSet.NULL ) );
      // AID_CONNECTION_CREATION_PARAMS
      dtoSession.attrInfos()
          .add( DtoAttrInfo.create1( ISkSession.AID_CONNECTION_CREATION_PARAMS, DataType.create( VALOBJ, //
              TSID_NAME, STR_N_AID_CONNECTION_CREATION_PARAMS, //
              TSID_DESCRIPTION, STR_D_AID_CONNECTION_CREATION_PARAMS, //
              TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID, //
              TSID_IS_NULL_ALLOWED, AV_FALSE, //
              TSID_DEFAULT_VALUE, avValobj( new OptionSet() ) //
          ), IOptionSet.NULL ) );
      // TODO:
      // dtoSession.linkInfos().addAll( ISkUserServiceConstants.LNKINF_USER );
      // dtoSession.rtdataInfos().addAll( ISkUserServiceConstants.RTDINF_STATE );
      // dtoSession.eventInfos().addAll( ISkUserServiceConstants.EVINF_STATE_CHANGED );
      newClasses.add( dtoSession );
    }
    if( newClasses.size() > 0 ) {
      sysdescrSupport.writeClassInfos( IStringList.EMPTY, newClasses );
    }
    // Создание пользователя root
    if( objectsSupport.findObject( ISkUserServiceHardConstants.SKID_USER_ROOT ) == null ) {
      IOptionSetEdit attrs = new OptionSet();
      attrs.setStr( AID_NAME, STR_N_ROOT_USER );
      attrs.setStr( AID_DESCRIPTION, STR_D_ROOT_USER );
      attrs.setStr( ISkUserServiceHardConstants.ATRID_PASSWORD_HASH, DEFAULT_ROOT_PASSWORD );
      IDtoObject root = new DtoObject( ISkUserServiceHardConstants.SKID_USER_ROOT, attrs, IStringMap.EMPTY );
      objectsSupport.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, new ElemArrayList<>( root ), true );
    }
  }
}
