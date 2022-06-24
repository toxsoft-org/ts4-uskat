package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;

import java.net.Socket;

import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.common.PasHandlerHolder;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.server.IPasServerChannelCreator;
import org.toxsoft.core.pas.server.PasServerChannel;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnGetBackendAddonInfos;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementation;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

import ru.uskat.core.api.users.ISkSession;

/**
 * Канал приема обратных вызовов сервера
 *
 * @author mvk
 */
public final class S5SessionCallbackChannel
    extends PasServerChannel {

  private final S5SessionCallbackServer  callbackServer;
  private final IS5InitialImplementation initialImplementation;
  private Skid                           sessionID = Skid.NONE;
  private boolean                        duplicate;
  private IS5StatisticCounter            statistic;

  /**
   * Фабрика каналов
   */
  @SuppressWarnings( "hiding" )
  static final IPasServerChannelCreator<S5SessionCallbackChannel> CREATOR = S5SessionCallbackChannel::new;

  /**
   * Конструктор.
   *
   * @param aContext {@link ITsContextRo} - контекст выполнения, общий для всех каналов и сервера
   * @param aSocket {@link Socket} сокет соединения
   * @param aHandlerHolder {@link PasHandlerHolder} хранитель обработчиков канала
   * @param aLogger {@link ILogger} журнал работы класса канала
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException ошибка создания читателя канала
   * @throws TsIllegalArgumentRtException ошибка создания писателя канала
   */
  S5SessionCallbackChannel( ITsContextRo aContext, Socket aSocket,
      PasHandlerHolder<? extends PasServerChannel> aHandlerHolder, ILogger aLogger ) {
    super( aContext, aSocket, aHandlerHolder, aLogger );
    callbackServer = aContext.get( S5SessionCallbackServer.class );
    initialImplementation = aContext.get( IS5InitialImplementation.class );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает идентификатор сессии в рамках которого работает канал
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}. {@link Skid#NONE}: нет сессии
   */
  Skid getSessionID() {
    return sessionID;
  }

  /**
   * Устанавливает идентификатор сессии в рамках которого работает канал
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}. {@link Skid#NONE}: нет сессии.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setSessionID( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    sessionID = aSessionID;
    if( aSessionID != Skid.NONE ) {
      callbackServer.onOpenChannel( this, aSessionID );
    }
  }

  /**
   * Установить признак того, что канал является дупликатом другого канала и подлежит удалению
   * <p>
   * TODO: На данный момент не ясно, является ли ошибкой или side effect появление дублирующих каналов при стрессовом
   * подключению клиентов к серверу
   *
   * @param aDuplicate boolean <b>true</b> данный канал является дублирующим и будет завершен без уведомления;
   *          <b>false</b> канал не является дублирующим
   */
  void setDuplicate( boolean aDuplicate ) {
    duplicate = aDuplicate;
  }

  /**
   * Регистрация обработчика уведомления
   *
   * @param aMethodName String имя метода уведомления
   * @param aHandler {@link IJSONNotificationHandler}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void registerNotificationHandler( String aMethodName, IJSONNotificationHandler<PasChannel> aHandler ) {
    TsNullArgumentRtException.checkNull( aMethodName );
    TsNullArgumentRtException.checkNull( aHandler );
    super.handlerHolder().registerNotificationHandler( aMethodName, aHandler );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов PasChannel
  //
  @Override
  protected boolean doInit() {
    // Передача информации о классе реализации бекенда сервера
    S5CallbackOnGetBackendAddonInfos.send( this, initialImplementation.baCreators() );
    // Базовая обработка
    return super.doInit();
  }

  @Override
  public void sendNotification( String aMethod, IStringMap<ITjValue> aParams ) {
    super.sendNotification( aMethod, aParams );
    if( statistic() != null ) {
      statistic().onEvent( IS5ServerHardConstants.STAT_SESSION_SENDED, AvUtils.AV_1 );
    }
    else {
      System.err.println( "sendNotification(...): statistic() == null. channel = " + this ); //$NON-NLS-1$
    }
  }

  @Override
  protected boolean doReceiveNotification( IJSONNotification aNotification ) {
    if( statistic() != null ) {
      statistic().onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED, AvUtils.AV_1 );
    }
    else {
      System.err.println( "doReceiveNotification(...): statistic() == null. channel = " + this ); //$NON-NLS-1$
    }
    // false: требуем продолжить обработку уведомления
    return false;
  }

  @Override
  protected void doClose() {
    if( !duplicate ) {
      callbackServer.onCloseChannel( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает статистику в которой формируются значения для канала
   *
   * @return {@link IS5StatisticCounter} статистика. null: неопределена
   */
  private IS5StatisticCounter statistic() {
    if( statistic != null ) {
      // 2021-04-13 mvk требуем всегда получать "свежий" счетчик, чтобы избежать ошибки "гонки потоков"
      // return statistic;
    }
    if( sessionID == null ) {
      // У канала нет идентификатора сессии
      logger().warning( ERR_CHANNEL_NO_WILDFLY_ID, this );
      return null;
    }
    IS5SessionManager sessionManager = callbackServer.sessionManager();
    // Идентификатор сессии
    statistic = sessionManager.findStatisticCounter( sessionID );
    if( statistic == null ) {
      // Не найдена статистика канала
      logger().error( ERR_CHANNEL_NO_STATISTIC, sessionID, this );
      return null;
    }
    return statistic;
  }
}
