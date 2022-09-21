package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSessionControl;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Абстрактная реализация сессии расширения backend
 *
 * @author mvk
 */
public abstract class S5AbstractBackendAddonSession
    extends Stridable
    implements SessionBean, IS5BackendAddonSession, IS5BackendAddonSessionControl {

  private static final long serialVersionUID = 157157L;

  /**
   * Контекст сессии
   */
  @Resource
  private SessionContext sessionContext;

  /**
   * Менеджер сессий
   */
  @EJB
  private IS5SessionManager sessionManager;

  /**
   * Идентификатор сессии {@link ISkSession#skid()}
   */
  private Skid sessionID;

  /**
   * Сессия предоставляющая backend ядра s5-сервера
   */
  private IS5BackendSessionControl backend;

  /**
   * Ссылка на собственный локальный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #control()}
   */
  private transient IS5BackendAddonSessionControl selfLocal;

  /**
   * Ссылка на собственный удаленный интерфейс.
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #session()}
   */
  private transient IS5BackendAddonSession selfRemote;

  /**
   * Признак того, что сессия готова к удалению
   */
  private boolean removeReady = false;

  /**
   * Счетчик статистической информации сессии
   */
  private transient IS5StatisticCounter statisticCounter;

  /**
   * Журнал работы
   * <p>
   * Прямой доступ запрещен(transient), используйте {@link #logger()}
   */
  private transient ILogger logger;

  /**
   * Конструктор для наследников.
   *
   * @param aInfo {@link IStridable} информация о расширении бекенда
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5AbstractBackendAddonSession( IStridable aInfo ) {
    super( aInfo );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса SessionBean
  //
  @Override
  public final void setSessionContext( SessionContext aContext )
      throws EJBException,
      RemoteException {
    // Установлен контекст сессии расширения
    logger().info( "setSessionContext(...): %s. aContext : %s", id(), aContext ); //$NON-NLS-1$
  }

  @Override
  public final void ejbRemove()
      throws EJBException,
      RemoteException {
    if( sessionID == null ) {
      // ???
      logger().error( "ejbRemove(...): sessionID == null" ); //$NON-NLS-1$
      return;
    }
    if( removeReady ) {
      // Штатное завершение работы сессии расширения
      logger().info( "ejbRemove(...): sessionID = %s", sessionID ); //$NON-NLS-1$
      return;
    }
    // Аварийное завершение работы сессии расширения: контейнер вывел компонент в пул, возможно по "грязному" исключению
    logger().error( "ejbRemove(...): unexpected remove. sessionID = %s", sessionID ); //$NON-NLS-1$
    // Попытка завершить сессию у наследника
    try {
      doBeforeClose();
    }
    catch( Throwable e ) {
      logger().error( e );
    }
    // Требуем от backend ядра закрыть сессию
    try {
      backend.removeAsync();
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  @Override
  public final void ejbActivate()
      throws EJBException,
      RemoteException {
    // Активизация сессии расширения
    logger().debug( "ejbActivate(...): sessionID = %s", sessionID ); //$NON-NLS-1$
  }

  @Override
  public final void ejbPassivate()
      throws EJBException,
      RemoteException {
    // Деактивизация сессии расширения
    logger().debug( "ejbPassivate(...): sessionID = %s", sessionID ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendAddonSessionControl
  //
  @Override
  public IS5BackendAddonSessionControl control() {
    if( selfLocal == null ) {
      selfLocal = sessionContext.getBusinessObject( IS5BackendAddonSessionControl.class );
    }
    return selfLocal;
  }

  @Override
  public IS5BackendAddonSession session() {
    if( selfRemote == null ) {
      selfRemote = sessionContext.getBusinessObject( doGetSessionView() );
    }
    return selfRemote;
  }

  @Override
  public void init( IS5BackendSessionControl aBackend, S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    TsNullArgumentRtException.checkNulls( aMessenger, aBackend, aInitData, aInitResult );
    sessionID = aInitData.sessionID();
    backend = aBackend;
    // if( aSessionID.equals( wildflySessionID ) == false ) {
    // // Недопустимый идентификатор сессии
    // throw new TsIllegalArgumentRtException( ERR_WRONG_SESSION, aSessionID, wildflySessionID );
    // }
    doAfterInit( aMessenger, aInitData, aInitResult );
  }

  @Override
  public String nodeName() {
    String nodeName = System.getProperty( JBOSS_NODE_NAME );
    return nodeName;
  }

  @Remove
  @Asynchronous
  @AccessTimeout( value = REMOVE_ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  // @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @TransactionAttribute( TransactionAttributeType.REQUIRES_NEW )
  @Override
  public void removeAsync() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5Verifiable
  //
  @Override
  public void verify() {
    // Завершение проверки сессии расширения backend
    logger().debug( "verify(...): sessionID = %s", sessionID ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IClosable
  //
  @Override
  public void close() {
    try {
      // Попытка завершить сессию у наследника только если была инициализация
      if( sessionID != null ) {
        doBeforeClose();
      }
      else {
        // doBeforeClose() не был вызван так сессия не инициализирована
        logger().error( "close(...): sessionID = %s", sessionID ); //$NON-NLS-1$
      }
    }
    catch( Throwable e ) {
      // Ошибки наследника игнорируются - удаление сессии нельзя остановить
      logger().error( e );
    }
    // Сессия готова к удалению
    removeReady = true;
    // Удаление бина сессии
    control().removeAsync();
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает текущий контекст сессии
   *
   * @return {@link SessionContext} контекст сессии
   */
  protected final SessionContext sessionContext() {
    return sessionContext;
  }

  /**
   * Возвращает фронтенд с которым работает сессия бекенда
   *
   * @return {@link IS5FrontendRear} фронтенд с которым работает сессия
   */
  protected final IS5FrontendRear frontend() {
    return sessionManager.getMessenger( sessionID() );
  }

  /**
   * Возвращает идентификатор сессии
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession#skid()}
   */
  protected final Skid sessionID() {
    return sessionID;
  }

  /**
   * Сохраняет изменные данные сессии в кластере сервера
   */
  protected final void writeSessionData() {
    // Данные писателя обратных вызовов записываются в кэш данных сессий (infinispan) + оповещается кластер
    sessionManager.writeSessionData( sessionManager.getMessenger( sessionID() ).sessionData() );
  }

  /**
   * Возвращает модуль формирования статистики
   *
   * @return {@link IS5StatisticCounter} статистика
   */
  protected final IS5StatisticCounter statisticCounter() {
    if( statisticCounter == null ) {
      statisticCounter = sessionManager.findStatisticCounter( sessionID() );
      TsInternalErrorRtException.checkNull( statisticCounter );
    }
    return statisticCounter;
  }

  /**
   * Возвращает общий журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Возвращает удаленый бизнес-интерфейс доступа к наследнику сессионного бина
   *
   * @return Class<? extends {@link IS5BackendAddonSession}> - удаленный интерфейс доступа
   */
  protected abstract Class<? extends IS5BackendAddonSession> doGetSessionView();

  /**
   * Вызывается в конце метода
   * {@link #init(IS5BackendSessionControl, S5SessionMessenger, IS5SessionInitData, S5SessionInitResult)} .
   * <p>
   * Выброшенные методом исключения передаются сессии backend, что приводит к провалу установления связи с сервером.
   *
   * @param aMessenger {@link S5SessionMessenger} приемопередатчик сообщений сессии
   * @param aInitData {@link IS5SessionInitData} данные для инициализации сессии
   * @param aInitResult {@link S5SessionInitResult} результаты инициализации сессии
   */
  protected void doAfterInit( S5SessionMessenger aMessenger, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // nop
  }

  /**
   * Вызывается из метода {@link #removeAsync()} перед началом процедуры отключения сессии (разрыва связи).
   * <p>
   * Выброшенные методом исключения игнорируются.
   */
  protected void doBeforeClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

}
