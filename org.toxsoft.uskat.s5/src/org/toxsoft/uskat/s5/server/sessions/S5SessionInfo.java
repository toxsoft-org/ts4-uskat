package org.toxsoft.uskat.s5.server.sessions;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.IS5Resources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.statistics.*;

/**
 * Реализация информации о backend-сессии пользователя.
 *
 * @author mvk
 */
public final class S5SessionInfo
    implements IS5SessionInfoEdit, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "S5SessionInfo"; //$NON-NLS-1$

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5SessionInfo> KEEPER =
      new AbstractEntityKeeper<>( S5SessionInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, S5SessionInfo aEntity ) {
          Skid.KEEPER.write( aSw, aEntity.sessionID() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.remoteAddress() );
          aSw.writeSeparatorChar();
          aSw.writeInt( aEntity.remotePort() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.openTime() );
          aSw.writeSeparatorChar();
          aSw.writeQuotedString( aEntity.login() );
          aSw.writeSeparatorChar();
          S5ClusterTopology.KEEPER.write( aSw, aEntity.clusterTopology() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.clientOptions() );
          aSw.writeSeparatorChar();
          S5Statistic.KEEPER.write( aSw, (S5Statistic)aEntity.statistics() );
          aSw.writeSeparatorChar();
          aSw.writeLong( aEntity.closeTime() );
          aSw.writeSeparatorChar();
          aSw.writeBoolean( aEntity.closeByRemote() );
          aSw.writeSeparatorChar();
        }

        @Override
        protected S5SessionInfo doRead( IStrioReader aSr ) {
          Skid sessionID = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          String remoteAddress = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          int remotePort = aSr.readInt();
          aSr.ensureSeparatorChar();
          long openTime = aSr.readLong();
          aSr.ensureSeparatorChar();
          String login = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          S5ClusterTopology clusterTopology = S5ClusterTopology.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IOptionSet client = OptionSetKeeper.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          S5Statistic statistics = S5Statistic.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          long closeTime = aSr.readLong();
          aSr.ensureSeparatorChar();
          boolean closeByRemote = aSr.readBoolean();
          aSr.ensureSeparatorChar();
          S5SessionInfo retValue = new S5SessionInfo( openTime, login, client, statistics );
          retValue.setSessionID( sessionID );
          retValue.setRemoteAddress( remoteAddress, remotePort );
          retValue.setClusterTopology( clusterTopology );
          retValue.closeTime = closeTime;
          retValue.closeByRemote = closeByRemote;
          return retValue;
        }
      };

  private Skid              sessionID     = Skid.NONE;
  private String            remoteAddress = TsLibUtils.EMPTY_STRING;
  private int               remotePort    = -1;
  private long              openTime;
  private String            login;
  private S5ClusterTopology clusterTopology;
  private IOptionSet        clientOptions;
  private S5Statistic       statistics;
  private volatile long     closeTime     = TimeUtils.MAX_TIMESTAMP;
  private volatile boolean  closeByRemote = false;
  private static ILogger    logger        = getLogger( S5SessionInfo.class );

  static {
    // Попытка заставить провести инициализацию открытых констант класса
    logger.debug( "%s = %s", KEEPER_ID, KEEPER ); //$NON-NLS-1$
  }

  /**
   * Конструктор
   *
   * @param aUserLogin String учетное имя пользователя открывшего сессию сервера
   * @param aClientOptions {@link IOptionSet} параметры клиента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SessionInfo( String aUserLogin, IOptionSet aClientOptions ) {
    this( System.currentTimeMillis(), aUserLogin, aClientOptions, new S5Statistic( STAT_SESSION_PARAMS ) );
  }

  /**
   * Конструктор
   *
   * @param aOpenTime long метка время создания сессии
   * @param aUserLogin String учетное имя пользователя открывшего сессию сервера
   * @param aClientOptions {@link IOptionSet} информация о пользователе сессии
   * @param aStatistics {@link S5Statistic} статистика работы сессии
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionInfo( long aOpenTime, String aUserLogin, IOptionSet aClientOptions, S5Statistic aStatistics ) {
    TsNullArgumentRtException.checkNulls( aUserLogin, aClientOptions, aStatistics );
    openTime = aOpenTime;
    login = aUserLogin;
    clusterTopology = new S5ClusterTopology();
    clientOptions = new OptionSet( aClientOptions );
    statistics = aStatistics;
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IS5SessionInfo} исходное описание сессии
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SessionInfo( IS5SessionInfo aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    sessionID = aSource.sessionID();
    remoteAddress = aSource.remoteAddress();
    remotePort = aSource.remotePort();
    openTime = aSource.openTime();
    login = aSource.login();
    clusterTopology = new S5ClusterTopology( aSource.clusterTopology().nodes() );
    clientOptions = new OptionSet( aSource.clientOptions() );
    statistics = (S5Statistic)aSource.statistics();
    closeTime = aSource.closeTime();
    closeByRemote = aSource.closeByRemote();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInfoEdit
  //
  @Override
  public void setSessionID( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    sessionID = aSessionID;
  }

  @Override
  public void setRemoteAddress( String aRemoteAddress, int aRemotePort ) {
    TsNullArgumentRtException.checkNull( aRemoteAddress );
    remoteAddress = aRemoteAddress;
    remotePort = aRemotePort;
  }

  @Override
  public void setClusterTopology( S5ClusterTopology aClusterTopology ) {
    TsNullArgumentRtException.checkNull( aClusterTopology );
    clusterTopology.setAll( aClusterTopology );
  }

  @Override
  public void setCloseByRemote( boolean aCloseByRemote ) {
    closeByRemote = aCloseByRemote;
  }

  /**
   * Закрывает сессию пользователя
   */
  @Override
  public void close() {
    closeTime = System.currentTimeMillis();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInfo
  //
  @Override
  public String remoteAddress() {
    return remoteAddress;
  }

  @Override
  public int remotePort() {
    return remotePort;
  }

  @Override
  public long openTime() {
    return openTime;
  }

  @Override
  public long closeTime() {
    return closeTime;
  }

  @Override
  public String login() {
    return login;
  }

  @Override
  public Skid sessionID() {
    return sessionID;
  }

  @Override
  public S5ClusterTopology clusterTopology() {
    return clusterTopology;
  }

  @Override
  public IOptionSet clientOptions() {
    return clientOptions;
  }

  @Override
  public IS5Statistic statistics() {
    return statistics;
  }

  @Override
  public boolean closeByRemote() {
    return closeByRemote;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return format( TO_STRING_FORMAT, login, remoteAddress, Integer.valueOf( remotePort ), sessionID );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + sessionID.hashCode();
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
    IS5SessionInfo other = (IS5SessionInfo)aObject;
    if( !sessionID.equals( other.sessionID() ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Фиксация ошибки в сессии
   *
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий сервера
   * @param aSessionID {@link Skid} идентификатор сессии
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void onErrorEvent( IS5SessionManager aSessionManager, Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionManager );
    TsNullArgumentRtException.checkNull( aSessionID );
    IS5StatisticCounter statistic = aSessionManager.findStatisticCounter( aSessionID );
    if( statistic == null ) {
      // статистика не найдена
      logger.warning( ERR_SESSION_NOT_FOUND, "onErrorEvent(...)", aSessionID ); //$NON-NLS-1$
      return;
    }
    onErrorEvent( statistic );
  }

  /**
   * Фиксация в сессии факта передачи сообщения фронтенду
   *
   * @param aStatistic {@link IS5StatisticCounter}
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException счетчик завершил работу
   */
  public static void onSendEvent( IS5StatisticCounter aStatistic ) {
    TsNullArgumentRtException.checkNull( aStatistic );
    TsIllegalStateRtException.checkTrue( aStatistic.isClosed() );
    aStatistic.onEvent( IS5ServerHardConstants.STAT_SESSION_SENDED, AvUtils.AV_1 );
  }

  /**
   * Фиксация в сессии факта приема сообщения от фронтенда
   *
   * @param aStatistic {@link IS5StatisticCounter}
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException счетчик завершил работу
   */
  public static void onReceviedEvent( IS5StatisticCounter aStatistic ) {
    TsNullArgumentRtException.checkNull( aStatistic );
    TsIllegalStateRtException.checkTrue( aStatistic.isClosed() );
    aStatistic.onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED, AvUtils.AV_1 );
  }

  /**
   * Фиксация ошибки в сессии
   *
   * @param aStatistic {@link IS5StatisticCounter}
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException счетчик завершил работу
   */
  public static void onErrorEvent( IS5StatisticCounter aStatistic ) {
    TsNullArgumentRtException.checkNull( aStatistic );
    TsIllegalStateRtException.checkTrue( aStatistic.isClosed() );
    aStatistic.onEvent( IS5ServerHardConstants.STAT_SESSION_ERRORS, AvUtils.AV_1 );
  }
}
