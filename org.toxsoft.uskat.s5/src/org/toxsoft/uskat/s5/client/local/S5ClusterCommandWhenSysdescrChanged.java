package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.uskat.s5.client.local.IS5LocalNoticeHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5RemoteSession;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.backend.messages.SkMessageWhenSysdescrChanged;

/**
 * Обработчик команды кластера: всем узлам вызвать у локальных соединений {@link SkMessageWhenSysdescrChanged}
 *
 * @author mvk
 */
public final class S5ClusterCommandWhenSysdescrChanged
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5SessionManager#tryCreateCallbackWriter(S5RemoteSession)}
   */
  public static final String WHEN_SYSDESCR_CHANGED_METHOD = FRONTEND_METHOD_PREFIX + "whenSysdescrChanged"; //$NON-NLS-1$

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
  public S5ClusterCommandWhenSysdescrChanged( IS5BackendCoreSingleton aBackend ) {
    backend = TsNullArgumentRtException.checkNull( aBackend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @return {@link IS5ClusterCommand} созданные команды
   */
  public static IS5ClusterCommand whenSysdescrChangedCommand() {
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return WHEN_SYSDESCR_CHANGED_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        return IStringMap.EMPTY;
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
    for( ISkFrontendRear frontend : backend.attachedFrontends() ) {
      if( frontend instanceof S5LocalBackend ) {
        SkMessageWhenSysdescrChanged.send( ((S5LocalBackend)frontend).frontend() );
      }
    }
    return TjUtils.TRUE;
  }
}
