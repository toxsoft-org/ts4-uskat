package org.toxsoft.uskat.s5.server.sessions;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSessionControl;
import org.toxsoft.uskat.s5.server.frontend.S5FrontendData;

/**
 * Данные сессии пользователя удаленных клиентов
 *
 * @author mvk
 */
public final class S5SessionData
    implements Comparable<S5SessionData>, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IS5SessionInfoEdit       info;
  private final IS5BackendSessionControl backend;
  private final S5FrontendData           frontendData;

  private transient ILogger logger;

  /**
   * Конструктор
   *
   * @param aSessionInfo {@link IS5SessionInfo} описание сессии
   * @param aBackend {@link IS5BackendSessionControl} s5-backend сессии пользователя
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SessionData( IS5SessionInfo aSessionInfo, IS5BackendSessionControl aBackend ) {
    info = TsNullArgumentRtException.checkNull( new S5SessionInfo( aSessionInfo ) );
    backend = TsNullArgumentRtException.checkNull( aBackend );
    frontendData = new S5FrontendData();
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает описание сессии пользователя
   *
   * @return {@link IS5SessionInfoEdit} описание сессии с возможностью редактирования
   */
  public IS5SessionInfoEdit info() {
    return info;
  }

  /**
   * Возвращает s5-backend сессии пользователя
   *
   * @return {@link IS5BackendSessionControl} s5-backend сессии
   */
  public IS5BackendSessionControl backend() {
    return backend;
  }

  /**
   * Возвращает конфигурационные данные frontend
   *
   * @return {@link S5FrontendData} конфигурацию frontend
   */
  public S5FrontendData frontendData() {
    return frontendData;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return String.format( "%s %s", info.toString(), frontendData.toString() ); //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    return info.hashCode();
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
    return info.equals( ((S5SessionData)aObject).info );
  }

  // ------------------------------------------------------------------------------------
  // Реализация Comparable&lt;S5SessionData&gt;
  //
  @Override
  public int compareTo( S5SessionData aOther ) {
    TsNullArgumentRtException.checkNull( aOther );
    IS5SessionInfo sessionInfo2 = aOther.info;
    long openTime = info.openTime();
    long closeTime = info.closeTime();
    long openTime2 = sessionInfo2.openTime();
    long closeTime2 = sessionInfo2.closeTime();
    if( closeTime == MAX_TIMESTAMP && closeTime2 == MAX_TIMESTAMP ) {
      return (openTime < openTime2 ? -1 : openTime > openTime2 ? 1 : 0);
    }
    if( closeTime != MAX_TIMESTAMP && closeTime2 != MAX_TIMESTAMP ) {
      return (closeTime < closeTime2 ? -1 : closeTime > closeTime2 ? 1 : 0);
    }
    return (closeTime != MAX_TIMESTAMP ? 1 : -1);
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
