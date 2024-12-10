package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.uskat.s5.client.local.IS5LocalNoticeHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.IBaObjectsMessages;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Обработчик команды кластера: всем узлам вызвать у локальных соединений {@link IBaObjectsMessages}
 *
 * @author mvk
 */
public final class S5ClusterCommandWhenObjectsChanged
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5FrontendRear#onBackendMessage(GtMessage)}({@link IBaObjectsMessages})
   */
  public static final String WHEN_OBJECTS_CHANGED_METHOD = FRONTEND_METHOD_PREFIX + "whenObjectsChanged"; //$NON-NLS-1$

  /**
   * Тип операции над объектом
   * <p>
   * Тип: String (представляющее {@link ECrudOp})
   */
  private static final String ARG_CRUD_OP = "crudOp"; //$NON-NLS-1$

  /**
   * Идентификатор объекта. {@link Skid#NONE}: если {@link #ARG_CRUD_OP} == {@link ECrudOp#LIST}.
   * <p>
   * Тип: String (представляющее {@link Skid})
   */
  private static final String ARG_OBJ_SKID = "objId"; //$NON-NLS-1$

  /**
   * s5-backendSingleton предоставляемый сервером
   */
  private IS5BackendCoreSingleton backendSingleton;

  /**
   * Журнал работы
   */
  // private final ILogger logger = l4jLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aBackend {@link IS5BackendCoreSingleton} s5-backendSingleton предоставляемый сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandWhenObjectsChanged( IS5BackendCoreSingleton aBackend ) {
    backendSingleton = TsNullArgumentRtException.checkNull( aBackend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aCrudOp {@link ECrudOp} операция над объектом
   * @param aObjectId {@link Skid} Идентификатор объекта. {@link Skid#NONE}: если {@link #ARG_CRUD_OP} ==
   *          {@link ECrudOp#LIST}
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный идентификатор объекта {@link Skid#NONE}.
   */
  public static IS5ClusterCommand whenObjectsChangedCommand( ECrudOp aCrudOp, Skid aObjectId ) {
    TsNullArgumentRtException.checkNulls( aCrudOp, aObjectId );
    TsIllegalArgumentRtException.checkTrue( aObjectId == Skid.NONE && aCrudOp != ECrudOp.LIST );
    TsIllegalArgumentRtException.checkTrue( aObjectId != Skid.NONE && aCrudOp == ECrudOp.LIST );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return WHEN_OBJECTS_CHANGED_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        StringMap<ITjValue> retValue = new StringMap<>();
        retValue.put( ARG_CRUD_OP, TjUtils.createString( aCrudOp.id() ) );
        retValue.put( ARG_OBJ_SKID, TjUtils.createString( Skid.KEEPER.ent2str( aObjectId ) ) );
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
    if( !aCommand.method().equals( WHEN_OBJECTS_CHANGED_METHOD ) ) {
      return TjUtils.NULL;
    }
    ECrudOp crudOp = ECrudOp.findById( aCommand.params().getByKey( ARG_CRUD_OP ).asString() );
    Skid objectId = Skid.KEEPER.str2ent( aCommand.params().getByKey( ARG_OBJ_SKID ).asString() );
    for( IS5FrontendRear frontend : backendSingleton.attachedFrontends() ) {
      if( frontend.isLocal() ) {
        frontend.onBackendMessage( IBaObjectsMessages.makeMessage( crudOp, objectId ) );
      }
    }
    return TjUtils.TRUE;
  }
}
