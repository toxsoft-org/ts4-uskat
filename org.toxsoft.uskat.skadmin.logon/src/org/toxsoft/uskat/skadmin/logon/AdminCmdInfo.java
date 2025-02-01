package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
//import static ru.uskat.s5.client.remote.IS5RemoteBackendHardConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.logon.AdminLogonUtils.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.logon.rules.AdminCheckClientUtils.*;

import java.io.*;
import java.time.*;
import java.time.format.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.common.info.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;
import org.toxsoft.uskat.skadmin.logon.rules.*;

/**
 * Команда администрирования: вывести информацию о текущем состоянии сервера
 *
 * @author mvk
 */
public class AdminCmdInfo
    extends AbstractAdminCmd {

  /**
   * Имя каталога в skadmin расположения данных
   */
  private static final String DATA_DIR = "data"; //$NON-NLS-1$

  /**
   * Формат вывода времени
   */
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ); //$NON-NLS-1$

  /**
   * Формат вывода времени (с мсек)
   */
  private final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern( "yyyy-MM-dd hh:mm:ss.SSS" ); //$NON-NLS-1$

  /**
   * Конструктор
   */
  public AdminCmdInfo() {
    // Контекст: соединение с сервером
    addArg( CTX_SK_CONNECTION );
    // Интервал статистики
    addArg( ARG_INFO_INTERVAL );
    // Вывод информации о платформе
    addArg( ARG_INFO_PLATFORM );
    // Вывод информации о транзакциях
    addArg( ARG_INFO_TRANSACTIONS );
    // Вывод информации об открытых сессиях
    addArg( ARG_INFO_OPEN_SESSIONS );
    // Вывод информации об закрытых сессиях
    addArg( ARG_INFO_CLOSE_SESSIONS );
    // Вывод информации о топологии кластеров доступных клиенту
    addArg( ARG_INFO_TOPOLOGY );
    // Требование вывода информации о приеме/передаче хранимых данных клиентам
    addArg( ARG_INFO_HISTDATA );
    // Имя текстового файла (в каталоге data), в формате \"имя_клиента, ip-адрес\" для контроля связи с клиентом
    addArg( ARG_INFO_CHECKFILE );
    // Форматирование
    addArg( ARG_FORMAT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_INFO_ID;
  }

  @Override
  public String alias() {
    return CMD_INFO_ALIAS;
  }

  @Override
  public String description() {
    return CMD_INFO_DESCR;
  }

  @Override
  public String nmName() {
    return CMD_INFO_NAME;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    boolean showPlatform = argSingleValue( ARG_INFO_PLATFORM ).asBool();
    boolean showTransaction = argSingleValue( ARG_INFO_TRANSACTIONS ).asBool();
    boolean showOpenSessions = argSingleValue( ARG_INFO_OPEN_SESSIONS ).asBool();
    boolean showCloseSessions = argSingleValue( ARG_INFO_CLOSE_SESSIONS ).asBool();
    EStatisticInterval interval = EStatisticInterval.findById( argSingleValue( ARG_INFO_INTERVAL ).asString() );
    String check = argSingleValue( ARG_INFO_CHECKFILE ).asString();
    boolean topology = argSingleValue( ARG_INFO_TOPOLOGY ).asBool();
    boolean histdata = argSingleValue( ARG_INFO_HISTDATA ).asBool();
    boolean format = argSingleValue( ARG_FORMAT ).asBool();

    // Информация о сервере
    ISkBackendInfo info = connection.backendInfo();
    // Информация об открытых и завершенных сессиях пользователей
    S5SessionsInfos sessionsInfos = OP_BACKEND_SESSIONS_INFOS.getValue( info.params() ).asValobj();
    // Информация об открытых и завершенных транзакциях сервера
    S5TransactionInfos transactionsInfos = OP_BACKEND_TRANSACTIONS_INFOS.getValue( info.params() ).asValobj();
    // Вывод
    addResultInfo( MSG_INFO_CONNECT, connectionToString( info ) );
    addResultInfo( MSG_INFO_ID, info.id() );
    addResultInfo( MSG_INFO_NAME, info.nmName() );
    addResultInfo( MSG_INFO_DESCR, info.description() );
    addResultInfo( MSG_CONNECT_BACKEND );
    S5Module module = IS5ServerHardConstants.OP_BACKEND_MODULE.getValue( info.params() ).asValobj();
    addResultInfo( "\n" ); //$NON-NLS-1$
    addResultInfo( MSG_CONNECT_VERSION, module.version() );
    addResultInfo( MSG_CONNECT_DEPENDS );
    for( S5Module depend : module.depends() ) {
      addResultInfo( MSG_CONNECT_MODULE, depend.id(), depend.description(), depend.version() );
    }
    ZoneId zone = ZoneId.of( OP_BACKEND_ZONE_ID.getValue( info.params() ).asString() );
    ZonedDateTime startTime = getZonedDateTime( zone, OP_BACKEND_START_TIME.getValue( info.params() ).asLong() );
    ZonedDateTime currTime = getZonedDateTime( zone, OP_BACKEND_CURRENT_TIME.getValue( info.params() ).asLong() );

    addResultInfo( MSG_INFO_ZONE, zone );
    addResultInfo( MSG_INFO_START, startTime.format( dtf ) );
    addResultInfo( MSG_INFO_CURRENT, currTime.format( dtf ) );
    addResultInfo( MSG_INFO_SN_ACTIVE, Long.valueOf( sessionsInfos.openInfos().size() ) );
    addResultInfo( MSG_INFO_TX_ACTIVE, Long.valueOf( transactionsInfos.openInfos().size() ) );
    addResultInfo( MSG_INFO_TX_COMMITED, Long.valueOf( transactionsInfos.commitCount() ) );
    addResultInfo( MSG_INFO_TX_ROLLBACKED, Long.valueOf( transactionsInfos.rollbackCount() ) );

    if( showPlatform ) {
      // Вывод информации об операционной системе на которой работает сервер
      addResultInfo( MSG_INFO_OPERATION_SYSTEM_INFO, OP_BACKEND_PLATFORM_INFO.getValue( info.params() ).asString() );
      // Вывод информации об использовании heap памяти
      addResultInfo( MSG_INFO_HEAP_USAGE_INFO, OP_BACKEND_HEAP_MEMORY_USAGE.getValue( info.params() ).asString() );
      // Вывод информации об использовании non-heap памяти
      addResultInfo( MSG_INFO_NON_HEAP_USAGE_INFO,
          OP_BACKEND_NON_HEAP_MEMORY_USAGE.getValue( info.params() ).asString() );
    }
    if( showTransaction ) {
      // Вывод списка активных транзакций
      printTransactionInfos( MSG_INFO_TX_OPENED_LIST, transactionsInfos.openInfos(), currTime, format );
      // Вывод списка завершенных транзакций
      printTransactionInfos( MSG_INFO_TX_COMMITED_LIST, transactionsInfos.commitedInfos(), currTime, format );
      // Вывод списка отменных транзакций
      printTransactionInfos( MSG_INFO_TX_ROLLBACKED_LIST, transactionsInfos.rollbackedInfos(), currTime, format );
      // Вывод списка длительных транзакций
      printTransactionInfos( MSG_INFO_TX_LONGTIME_LIST, transactionsInfos.longTimeInfos(), currTime, format );
    }
    boolean needTopology = (topology && !showOpenSessions && !showCloseSessions);
    if( showOpenSessions || needTopology ) {
      // Вывод списка открытых сессий
      printOpenInfos( MSG_INFO_OPENED_LIST, sessionsInfos.openInfos(), topology, histdata, interval, currTime, check,
          format );
    }
    if( showCloseSessions || needTopology ) {
      // Вывод списка закрытых сессий
      printClosedInfos( MSG_INFO_CLOSED_LIST, sessionsInfos.closeInfos(), topology, currTime, format );
    }
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( ARG_INFO_INTERVAL.id() ) ) {
      IListEdit<IPlexyValue> values = new ElemArrayList<>( EStatisticInterval.values().length );
      for( int index = 0, n = EStatisticInterval.values().length; index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( EStatisticInterval.values()[index].id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывод списка транзакций в виде таблицы
   *
   * @param aHeaderText String текст заголовка таблицы
   * @param aTransactions {@link IList} список описаний транзакций
   * @param aServerTime {@link ZonedDateTime} текущее время сервера
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   */
  private void printTransactionInfos( String aHeaderText, IList<ITransactionInfo> aTransactions,
      ZonedDateTime aServerTime, boolean aFormat ) {
    // Часовой пояс сервера
    ZoneId zone = aServerTime.getZone();
    addResultInfo( aHeaderText );
    addResultInfo( MSG_INFO_LINE );
    addResultInfo( MSG_INFO_TX_LEGEND );
    addResultInfo( MSG_INFO_LINE );
    for( ITransactionInfo tx : aTransactions ) {
      String msg = (aFormat ? MSG_INFO_TX : MSG_INFO_TX_FREE);
      String className = (aFormat ? StridUtils.getLast( tx.className() ) : tx.className());
      String open = getZonedDateTime( zone, tx.openTime() ).format( dtf2 ).substring( 0, 19 );
      Long duration = Long.valueOf( tx.closeTime() - tx.openTime() );
      String method = tx.methodName();
      String args = tx.methodArgs();
      addResultInfo( msg, tx.session(), className, method, args, tx.status(), open, duration, tx.key(),
          tx.description() );
    }
    addResultInfo( MSG_INFO_LINE );
  }

  /**
   * Вывод списка описаний открытых сессий в виде таблицы
   *
   * @param aHeaderText String текст заголовка таблицы
   * @param aSessions {@link IList} список описаний сессий
   * @param aInterval {@link EStatisticInterval} интервал статитстики
   * @param aServerTime {@link ZonedDateTime} текущее время сервера
   * @param aCheckFile String файл контроля связи с клиентами
   * @param aTopology boolean <b>true</b> вывод топологии кластеров; <b>false</b> обычный вывода.
   * @param aHistData boolean <b>true</b> вывод информации о хранимых данных; <b>false</b> обычный вывод.
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   */
  private void printOpenInfos( String aHeaderText, IList<IS5SessionInfo> aSessions, boolean aTopology,
      boolean aHistData, EStatisticInterval aInterval, ZonedDateTime aServerTime, String aCheckFile, boolean aFormat ) {
    IList<IS5SessionInfo> sessions = new ElemArrayList<>( aSessions );
    // Имя файла с контрольной информацией о клиентах
    String appDir = contextParamValue( EAdminCmdContextNames.CTX_APPLICATION_DIR ).singleValue().asString();
    String dataDir = appDir + File.separatorChar + DATA_DIR;
    String filename = dataDir + File.separatorChar + aCheckFile;
    // Часовой пояс сервера
    ZoneId zone = aServerTime.getZone();
    // Проверка и если необходимо создание каталога 'data'
    File folder = new File( dataDir );
    if( !folder.exists() ) {
      folder.mkdir();
    }
    File file = new File( filename );
    if( !file.exists() ) {
      // Файл не существует, создаем новый
      writeCheckFile( file, sessions );
      addResultInfo( MSG_INFO_CREATE_CHECKFILE, file.getAbsolutePath() );
    }
    // Список невыполненных правил проверки
    IListEdit<IAdminCheckClientRule> unsatisfied = new ElemArrayList<>();
    // Валидация сессий
    IList<IVrList> validations = validation( aServerTime, file, sessions, unsatisfied );
    addResultInfo( aHeaderText );
    if( !aTopology ) {
      StringBuilder legend = new StringBuilder( "interval = " ).append( aInterval.id() ); //$NON-NLS-1$
      legend.append( aHistData ? "(хранимые данные) " : " ----------------------" ); //$NON-NLS-1$//$NON-NLS-2$
      addResultInfo( MSG_INFO_LINE_INTERVAL, legend.toString() );
      addResultInfo( MSG_INFO_SESSIONS_OPEN );
    }
    if( aTopology ) {
      addResultInfo( MSG_INFO_SESSIONS_LINE );
      addResultInfo( MSG_INFO_TOPOLOGIES_OPEN );
    }
    addResultInfo( MSG_INFO_SESSIONS_LINE );
    // Счетчики:
    int boxCount = 0;
    int rcpCount = 0;
    int rapCount = 0;
    int adminCount = 0;
    @SuppressWarnings( "unused" )
    int serverCount = 0;
    int recvCount = 0;
    int sendCount = 0;
    int errorCount = 0;
    for( int index = 0, n = aSessions.size(); index < n; index++ ) {
      IS5SessionInfo session = aSessions.get( index );
      IVrList validation = validations.get( index );
      String msg = (aFormat ? MSG_INFO_SESSION_OPEN : MSG_INFO_SESSIONS_OPEN_FREE);
      if( aTopology ) {
        msg = (aFormat ? MSG_INFO_TOPOLOGY_OPEN : MSG_INFO_TOPOLOGY_OPEN_FREE);
      }
      String id = sessionIDToString( session.sessionID(), aFormat );
      String open = getZonedDateTime( zone, session.openTime() ).format( dtf2 ).substring( 0, 19 );
      String login = session.login();
      String ip = session.remoteAddress();
      String port = String.valueOf( session.remotePort() );
      if( EMPTY_STRING.equals( ip ) ) {
        ip = "n/a"; //$NON-NLS-1$
      }
      if( session.remotePort() < 0 ) {
        port = "n/a"; //$NON-NLS-1$
      }
      S5ClusterTopology topology = session.clusterTopology();
      // Попытка удалить клиента из карты проверяемых клиентов
      IS5Statistic statistics = session.statistics();
      Integer sended = Integer.valueOf( 0 );
      Integer recevied = Integer.valueOf( 0 );
      Integer errors = Integer.valueOf( 0 );
      String comment = EMPTY_STRING;
      // Обработка статистики
      IOptionSet params = statistics.params( aInterval );
      if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_SENDED.id() ) ) {
        sended = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_SENDED.id() ) );
        sendCount += sended.intValue();
      }
      if( !aHistData && params.hasKey( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() ) ) {
        recevied = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() ) );
        recvCount += recevied.intValue();
      }
      if( aHistData && params.hasKey( IS5ServerHardConstants.STAT_SESSION_RECEVIED_HISTDATA.id() ) ) {
        recevied = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_RECEVIED_HISTDATA.id() ) );
        recvCount += recevied.intValue();
      }
      if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() ) ) {
        errors = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() ) );
        errorCount += errors.intValue();
      }
      EValidationResultType resType = validation.getWorstType();
      for( int resultIndex = 0, m = validation.items().size(); resultIndex < m; resultIndex++ ) {
        ValidationResult result = validation.items().get( resultIndex ).vr();
        comment += result.message();
        if( result.isWarning() && resType == EValidationResultType.OK ) {
          resType = EValidationResultType.WARNING;
        }
        if( resultIndex < m - 1 ) {
          comment += ", "; //$NON-NLS-1$
        }
      }

      // Параметры подключения клиента к серверу
      IOptionSet clientOptions = session.clientOptions();
      // Имя программы клиента
      String clientProgram = IS5ConnectionParams.OP_CLIENT_PROGRAM.getValue( clientOptions ).asString();
      // Версия клиента
      TsVersion clientVersion = IS5ConnectionParams.OP_CLIENT_VERSION.getValue( clientOptions ).asValobj();
      // if( unknownClientType == true ) {
      // // Неизвестный тип клиента
      // comment = ERR_INFO_UNKNOW_CLIENT;
      // if( aTopology == false ) {
      // addResultError( msg, id, open, login, ip, port, featuresIds, sended, recevied, errors, comment );
      // continue;
      // }
      // addResultError( msg, id, open, login, ip, port, topology, comment );
      // continue;
      // }
      switch( resType ) {
        case OK:
          if( !aTopology ) {
            addResultInfo( msg, id, open, login, ip, port, clientVersion, sended, recevied, errors, comment );
            break;
          }
          addResultInfo( msg, id, open, login, ip, port, topology, comment );
          break;
        case WARNING:
          if( !aTopology ) {
            addResultWarning( msg, id, open, login, ip, port, clientVersion, sended, recevied, errors, comment );
            break;
          }
          addResultWarning( msg, id, open, login, ip, port, topology, comment );
          break;
        case ERROR:
          if( !aTopology ) {
            addResultError( msg, id, open, login, ip, port, clientVersion, sended, recevied, errors, comment );
            break;
          }
          addResultError( msg, id, open, login, ip, port, topology, comment );
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    // Вывод не найденных клиентов:
    for( int index = 0, n = unsatisfied.size(); index < n; index++ ) {
      IAdminCheckClientRule rule = unsatisfied.get( index );
      StringBuilder sb = new StringBuilder();
      if( !aTopology ) {
        IStringList featureIds = rule.clientFeatureIds();
        for( int featureIndex = 0, m = featureIds.size(); featureIndex < m; featureIndex++ ) {
          sb.append( featureIds.get( featureIndex ) );
          if( featureIndex + 1 < m ) {
            sb.append( ',' );
          }
        }
      }
      String msg = (!aTopology ? MSG_INFO_NOT_FOUND : MSG_INFO_TOPOLOGY_NOT_FOUND);
      addResultError( msg, rule.login(), rule.ip(), Integer.valueOf( rule.port() ), sb.toString() );
    }
    addResultInfo( MSG_INFO_SESSIONS_LINE );
    // Вывод итога
    Integer all = Integer.valueOf( aSessions.size() );
    Integer box = Integer.valueOf( boxCount );
    Integer rcp = Integer.valueOf( rcpCount );
    Integer rap = Integer.valueOf( rapCount );
    Integer admin = Integer.valueOf( adminCount );
    Integer recv = Integer.valueOf( recvCount );
    Integer send = Integer.valueOf( sendCount );
    Integer errors = Integer.valueOf( errorCount );
    addResultInfo( MSG_INFO_RESUME, all, box, rcp, rap, admin, send, recv, errors );
    addResultInfo( MSG_INFO_SESSIONS_LINE );
  }

  /**
   * Вывод списка описаний закрытых сессий в виде таблицы
   *
   * @param aHeaderText String текст заголовка таблицы
   * @param aSessions {@link IList} список описаний сессий
   * @param aTopology boolean <b>true</b> вывод топологии кластеров; <b>false</b> обычный вывода.
   * @param aServerTime {@link ZonedDateTime} текущее время сервера
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   */
  private void printClosedInfos( String aHeaderText, IList<IS5SessionInfo> aSessions, boolean aTopology,
      ZonedDateTime aServerTime, boolean aFormat ) {
    // Часовой пояс сервера
    ZoneId zone = aServerTime.getZone();
    addResultInfo( aHeaderText );
    if( !aTopology ) {
      addResultInfo( MSG_INFO_LINE_INTERVAL, "interval = " + EStatisticInterval.ALL.id() ); //$NON-NLS-1$
      addResultInfo( MSG_INFO_SESSIONS_CLOSED );
    }
    if( aTopology ) {
      addResultInfo( MSG_INFO_SESSIONS_LINE );
      addResultInfo( MSG_INFO_TOPOLOGIES_CLOSED );
    }
    addResultInfo( MSG_INFO_SESSIONS_LINE );
    for( int index = 0, n = aSessions.size(); index < n; index++ ) {
      IS5SessionInfo session = aSessions.get( index );
      String id = sessionIDToString( session.sessionID(), aFormat );
      String open = getZonedDateTime( zone, session.openTime() ).format( dtf2 ).substring( 0, 19 );
      String close = EMPTY_STRING;
      if( session.closeTime() != TimeUtils.MAX_TIMESTAMP ) {
        close = getZonedDateTime( zone, session.closeTime() ).format( dtf2 ).substring( 0, 19 );
      }
      String login = session.login();

      String ip = session.remoteAddress();
      String port = String.valueOf( session.remotePort() );
      if( EMPTY_STRING.equals( ip ) ) {
        ip = "n/a"; //$NON-NLS-1$
      }
      if( session.remotePort() < 0 ) {
        port = "n/a"; //$NON-NLS-1$
      }
      S5ClusterTopology topology = session.clusterTopology();
      // Попытка удалить клиента из карты проверяемых клиентов
      IS5Statistic statistics = session.statistics();
      // IListEdit<ITypedOptionSetEdit<IFeatureOptions>> features = IClientInfoOptions.FEATURES.get( session.client() );
      // StringBuilder sb = new StringBuilder();
      // for( int featureIndex = 0, m = features.size(); featureIndex < m; featureIndex++ ) {
      // ITypedOptionSetEdit<IFeatureOptions> feature = features.get( featureIndex );
      // sb.append( IFeatureOptions.ID.getValue( feature ).asString() );
      // IAtomicValue value = IFeatureOptions.VALUE.getValue( feature );
      // if( value != IAtomicValue.NULL ) {
      // sb.append( '=' );
      // sb.append( value );
      // }
      // if( featureIndex + 1 < m ) {
      // sb.append( ',' );
      // }
      // }

      // Параметры подключения клиента к серверу
      IOptionSet clientOptions = session.clientOptions();
      // Имя программы клиента
      String clientProgram = IS5ConnectionParams.OP_CLIENT_PROGRAM.getValue( clientOptions ).asString();
      // Версия клиента
      TsVersion clientVersion = IS5ConnectionParams.OP_CLIENT_VERSION.getValue( clientOptions ).asValobj();

      // Обработка статистики
      IOptionSet params = statistics.params( EStatisticInterval.ALL );
      Integer sended = Integer.valueOf( 0 );
      Integer recevied = Integer.valueOf( 0 );
      Integer errors = Integer.valueOf( 0 );
      if( params.hasKey( IS5ServerHardConstants.STAT_SESSION_SENDED.id() ) ) {
        sended = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_SENDED.id() ) );
      }
      if( params.hasValue( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() ) ) {
        recevied = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_RECEVIED.id() ) );
      }
      if( params.hasValue( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() ) ) {
        errors = Integer.valueOf( params.getInt( IS5ServerHardConstants.STAT_SESSION_ERRORS.id() ) );
      }

      String comment = EMPTY_STRING;
      if( !aTopology ) {
        String msg = (aFormat ? MSG_INFO_SESSION_CLOSED : MSG_INFO_SESSIONS_CLOSED_FREE);
        addResultInfo( msg, id, open, close, login, ip, port, clientVersion, sended, recevied, errors, comment );
      }
      if( aTopology ) {
        String msg = (aFormat ? MSG_INFO_TOPOLOGY_CLOSED : MSG_INFO_TOPOLOGY_CLOSED_FREE);
        addResultInfo( msg, id, open, close, login, ip, port, topology, comment );
      }
    }
    addResultInfo( MSG_INFO_SESSIONS_LINE );
  }

  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   * @return String строка представляющая сессию
   */
  private static String sessionIDToString( Skid aSessionID, boolean aFormat ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    if( !aFormat ) {
      return s.substring( s.indexOf( '[' ) + 1, s.indexOf( ']' ) );
    }
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }

  /**
   * Возвращает время с учетом часового пояса
   *
   * @param aZone {@link ZoneId} часовой пояс
   * @param aTime long текущее время (мсек с начала эпохи)
   * @return ZonedDateTime время с учетом часового пояса
   */
  private static ZonedDateTime getZonedDateTime( ZoneId aZone, long aTime ) {
    return ZonedDateTime.of( Instant.ofEpochMilli( aTime ).atZone( aZone ).toLocalDateTime(), aZone );
  }

}
