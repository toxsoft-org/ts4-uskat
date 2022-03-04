package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.uskat.s5.client.local.IS5LocalNoticeHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.SkidListKeeper;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5RemoteSession;

import ru.uskat.backend.messages.SkMessageWhenObjectsChanged;

/**
 * Обработчик команды кластера: всем узлам вызвать у локальных соединений {@link SkMessageWhenObjectsChanged}
 *
 * @author mvk
 */
public final class S5ClusterCommandWhenObjectsChanged
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5SessionManager#tryCreateCallbackWriter(S5RemoteSession)}
   */
  public static final String WHEN_OBJECTS_CHANGED_METHOD = FRONTEND_METHOD_PREFIX + "whenObjectsChanged"; //$NON-NLS-1$

  /**
   * Аргумент: идентификаторы объектов изменивших свои атрибуты или удаленных из системы. {@link ISkidList#EMPTY} все
   * объекты системы
   * <p>
   * Тип: String (представляющее {@link ISkidList})
   */
  private static final String ARG_OBJECT_IDS = "objectIds"; //$NON-NLS-1$

  /**
   * s5-backend предоставляемый сервером
   */
  private IS5BackendCoreSingleton backend;

  /**
   * Журнал работы
   */
  // private final ILogger logger = l4jLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aBackend {@link IS5BackendCoreSingleton} s5-backend предоставляемый сервером
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandWhenObjectsChanged( IS5BackendCoreSingleton aBackend ) {
    backend = TsNullArgumentRtException.checkNull( aBackend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @return {@link IS5ClusterCommand} созданная команда
   * @param aObjectIds {@link ISkidList} идентификаторы объектов изменивших свои атрибуты или удаленных из системы.
   *          {@link ISkidList#EMPTY} все объекты системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IS5ClusterCommand whenObjectsChangedCommand( ISkidList aObjectIds ) {
    TsNullArgumentRtException.checkNull( aObjectIds );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return WHEN_OBJECTS_CHANGED_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        StringMap<ITjValue> retValue = new StringMap<>();
        retValue.put( ARG_OBJECT_IDS, TjUtils.createString( SkidListKeeper.KEEPER.ent2str( aObjectIds ) ) );
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
    ISkidList objectIds = SkidListKeeper.KEEPER.str2ent( aCommand.params().getByKey( ARG_OBJECT_IDS ).asString() );

    for( IS5FrontendRear frontend : backend.attachedFrontends() ) {
      if( frontend instanceof S5LocalBackend ) {
        SkMessageWhenObjectsChanged.send( ((S5LocalBackend)frontend).frontend(), objectIds );
      }
    }
    return TjUtils.TRUE;
  }
}
