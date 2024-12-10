package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.uskat.s5.client.local.IS5LocalNoticeHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.IBaClassesMessages;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Обработчик команды кластера: всем узлам вызвать у локальных соединений {@link IBaClassesMessages}
 *
 * @author mvk
 */
public final class S5ClusterCommandWhenSysdescrChanged
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5FrontendRear#onBackendMessage(GtMessage)}({@link IBaClassesMessages})
   */
  public static final String WHEN_SYSDESCR_CHANGED_METHOD = FRONTEND_METHOD_PREFIX + "whenSysdescrChanged"; //$NON-NLS-1$

  /**
   * Тип операции над классом
   * <p>
   * Тип: String (представляющее {@link ECrudOp})
   */
  private static final String ARG_CRUD_OP = "crudOp"; //$NON-NLS-1$

  /**
   * Идентификатор класса. {@link TsLibUtils#EMPTY_STRING}: если {@link #ARG_CRUD_OP} == {@link ECrudOp#LIST}.
   * <p>
   * Тип: String
   */
  private static final String ARG_CLASS_ID = "classId"; //$NON-NLS-1$

  /**
   * s5-backend предоставляемый сервером
   */
  private IS5BackendCoreSingleton backendSingleton;

  /**
   * Конструктор
   *
   * @param aBackendSingleton {@link IS5BackendCoreSingleton} s5-backend предоставляемый сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandWhenSysdescrChanged( IS5BackendCoreSingleton aBackendSingleton ) {
    backendSingleton = TsNullArgumentRtException.checkNull( aBackendSingleton );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aCrudOp {@link ECrudOp} операция над объектом
   * @param aClassId String Идентификатор класса. {@link TsLibUtils#EMPTY_STRING}: если {@link #ARG_CRUD_OP} ==
   *          {@link ECrudOp#LIST}
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный идентификатор объекта {@link Skid#NONE}.
   */
  public static IS5ClusterCommand whenObjectsChangedCommand( ECrudOp aCrudOp, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aCrudOp, aClassId );
    TsIllegalArgumentRtException.checkTrue( aClassId.length() == 0 && aCrudOp != ECrudOp.LIST );
    TsIllegalArgumentRtException.checkTrue( aClassId.length() != 0 && aCrudOp == ECrudOp.LIST );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return WHEN_SYSDESCR_CHANGED_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        StringMap<ITjValue> retValue = new StringMap<>();
        retValue.put( ARG_CRUD_OP, TjUtils.createString( aCrudOp.id() ) );
        retValue.put( ARG_CLASS_ID, TjUtils.createString( aClassId ) );
        return retValue;
      }
    };
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterCommandHandler
  //
  @Override
  public ITjValue handleClusterCommand( IS5ClusterCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    if( !aCommand.method().equals( WHEN_SYSDESCR_CHANGED_METHOD ) ) {
      return TjUtils.NULL;
    }
    ECrudOp crudOp = ECrudOp.findById( aCommand.params().getByKey( ARG_CRUD_OP ).asString() );
    String classId = aCommand.params().getByKey( ARG_CLASS_ID ).asString();
    for( IS5FrontendRear frontend : backendSingleton.attachedFrontends() ) {
      if( frontend.isLocal() ) {
        frontend.onBackendMessage( IBaClassesMessages.makeMessage( crudOp, classId ) );
      }
    }
    return TjUtils.TRUE;
  }
}
