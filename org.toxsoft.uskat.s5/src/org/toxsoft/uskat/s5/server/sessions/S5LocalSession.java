package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.log4j.LoggerWrapper.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Сессия пользователя создаваемая для локальных клиентов
 *
 * @author mvk
 */
public final class S5LocalSession
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  private final Skid            sessionID;
  private final IS5FrontendRear frontend;
  private final String          node;
  private final String          module;

  private transient ILogger logger;

  /**
   * Конструктор
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @param aFrontend {@link IS5FrontendRear} s5-frontend сессии пользователя
   * @param aNode String идентификатор узла кластера
   * @param aModule String имя модуля создавшего соединение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5LocalSession( Skid aSessionID, String aModule, String aNode, IS5FrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aNode, aModule, aFrontend );
    sessionID = aSessionID;
    module = aModule;
    node = aNode;
    frontend = aFrontend;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает идентификатор сессии {@link ISkSession}
   *
   * @return {@link Skid} идентификатор сессии
   */
  public Skid sessionID() {
    return sessionID;
  }

  /**
   * Возвращает имя модуля создавшего подключение
   *
   * @return String имя модуля
   */
  public String module() {
    return module;
  }

  /**
   * Возвращает идентификатор узла кластера на котором работает локальный пользователь
   *
   * @return String идентификатор узла кластера
   */
  public String node() {
    return node;
  }

  /**
   * Возвращает s5-frontend сессии пользователя
   *
   * @return {@link IS5FrontendRear} s5-frontend сессии
   */
  public IS5FrontendRear frontend() {
    return frontend;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return String.format( "[%s] %s@%s", sessionID, module, node ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + sessionID.hashCode();
    result = TsLibUtils.PRIME * result + module.hashCode();
    result = TsLibUtils.PRIME * result + node.hashCode();
    result = TsLibUtils.PRIME * result + frontend.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5LocalSession other = (S5LocalSession)aObject;
    if( !sessionID.equals( other.sessionID ) ) {
      return false;
    }
    if( !module.equals( other.module ) ) {
      return false;
    }
    if( !node.equals( other.node ) ) {
      return false;
    }
    if( !frontend.equals( other.frontend ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  @SuppressWarnings( "unused" )
  private ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }
}
