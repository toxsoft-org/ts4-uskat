package org.toxsoft.uskat.s5.server;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.server.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.statistics.EStatisticInterval.*;

import java.time.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.entities.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Константы реализации s5-сервера
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ServerHardConstants
    extends IS5HardConstants {

  // ------------------------------------------------------------------------------------
  // Идентификация
  //
  /**
   * Идентификатор модуля реализующего сервер.
   */
  String S5_SERVER_ID = "org.toxsoft.uskat.s5.server";

  /**
   * Имя модуля реализующего skat-s5 сервер.
   */
  String S5_SERVER_NAME = STR_N_S5_SERVER_INFO;

  /**
   * Описание модуля реализующего skat-s5 сервер.
   */
  String S5_SERVER_DESCR = STR_D_S5_SERVER_INFO;

  /**
   * Версия сервера.
   */
  TsVersion version = new TsVersion( 22, 1, 2024, Month.OCTOBER, 24 );

  // ------------------------------------------------------------------------------------
  // Опции s5-backend.
  //
  /**
   * IDpath prefix of the all s5 identifiers.
   */
  String S5_ID_PREFIX = "s5";

  /**
   * String prefix of the all s5 identifiers.
   */
  String S5_ID_START = S5_ID_PREFIX + ".";

  /**
   * Опция {@link ISkClassInfo#params()}: Полное имя java-класса реализующего хранение данных объекта.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_OBJECT_IMPL_CLASS = create( S5_ID_START + "ObjectImplClass", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_OBJECT_IMPL_CLASS, //
      TSID_DESCRIPTION, STR_D_OBJECT_IMPL_CLASS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( S5DefaultObjectEntity.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: Полное имя java-класса реализующего хранение данных ПРЯМОЙ связи объекта.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_FWD_LINK_IMPL_CLASS = create( S5_ID_START + "FwdLinkImplClass", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_FWD_LINK_IMPL_CLASS, //
      TSID_DESCRIPTION, STR_D_FWD_LINK_IMPL_CLASS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( S5DefaultLinkFwdEntity.class.getName() ) );

  /**
   * Опция {@link ISkClassInfo#params()}: Полное имя java-класса реализующего хранение данных ОБРАТНОЙ связи объекта.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_REV_LINK_IMPL_CLASS = create( S5_ID_START + "RevLinkImplClass", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_REV_LINK_IMPL_CLASS, //
      TSID_DESCRIPTION, STR_D_REV_LINK_IMPL_CLASS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( S5DefaultLinkRevEntity.class.getName() ) );

  // ------------------------------------------------------------------------------------
  // Параметры s5-backend
  //
  /**
   * String prefix of the all s5 backend identifiers.
   */
  String S5_BACKEND_ID_START = S5_ID_START;

  /**
   * Параметр {@link ISkBackendInfo#params()}: идентификатор сервера, объекта {@link IS5ClassServer}
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link Skid}
   */
  IDataDef OP_BACKEND_SERVER_ID = create( S5_BACKEND_ID_START + "ServerId", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_SERVER_ID, //
      TSID_DESCRIPTION, STR_D_BACKEND_SERVER_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: идентификатор узла сервера, объекта {@link IS5ClassNode}
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link Skid}
   */
  IDataDef OP_BACKEND_NODE_ID = create( S5_BACKEND_ID_START + "NodeId", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_NODE_ID, //
      TSID_DESCRIPTION, STR_D_BACKEND_NODE_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: версия s5-backend который предоставляет сервер
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link TsVersion}
   */
  IDataDef OP_BACKEND_VERSION = create( S5_BACKEND_ID_START + "Version", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_VERSION, //
      TSID_DESCRIPTION, STR_D_BACKEND_VERSION, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new TsVersion( 0, 0 ) ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: модуль(и его зависимости) реализующий s5-backend сервера
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link S5Module}
   */
  IDataDef OP_BACKEND_MODULE = create( S5_BACKEND_ID_START + "Module", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_MODULE, //
      TSID_DESCRIPTION, STR_D_BACKEND_MODULE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5Module( "fooModule" ) ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: Идентификатор зоны времени, по которому работает сервер
   * <p>
   * Тип: {@link EAtomicType#STRING} содержит {@link ZoneId#getId()}
   */
  IDataDef OP_BACKEND_ZONE_ID = create( S5_BACKEND_ID_START + "ZoneId", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_ZONE_ID, //
      TSID_DESCRIPTION, STR_D_BACKEND_ZONE_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( ZoneId.systemDefault().getId() ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: время запуска сервера
   * <p>
   * Тип: {@link EAtomicType#TIMESTAMP}
   */
  IDataDef OP_BACKEND_START_TIME = create( S5_BACKEND_ID_START + "StartTime", EAtomicType.TIMESTAMP, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_START_TIME, //
      TSID_DESCRIPTION, STR_D_BACKEND_START_TIME, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_TIME_START );

  /**
   * Параметр {@link ISkBackendInfo#params()}: текущее время сервера
   * <p>
   * Тип: {@link EAtomicType#TIMESTAMP}
   */
  IDataDef OP_BACKEND_CURRENT_TIME = create( S5_BACKEND_ID_START + "CurrentTime", EAtomicType.TIMESTAMP, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_CURRENT_TIME, //
      TSID_DESCRIPTION, STR_D_BACKEND_CURRENT_TIME, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_TIME_START );

  /**
   * Параметр {@link ISkBackendInfo#params()}: запрет формирования хранимых данных
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  IDataDef OP_BACKEND_DATA_WRITE_DISABLE = create( S5_BACKEND_ID_START + "DataWriteDisable", EAtomicType.BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_DATA_WRITE_DISABLE, //
      TSID_DESCRIPTION, STR_D_BACKEND_DATA_WRITE_DISABLE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_FALSE );

  /**
   * Параметр {@link ISkBackendInfo#params()}: количество суток хранения объектов {@link ISkSession} после их завершения
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_BACKEND_SESSION_KEEP_DAYS = create( S5_BACKEND_ID_START + "SessionsKeepDays", EAtomicType.INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_SESSION_KEEP_DAYS, //
      TSID_DESCRIPTION, STR_D_BACKEND_SESSION_KEEP_DAYS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avInt( 5 ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: Информация о текущей сессии пользователя
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link IS5SessionInfo}
   */
  IDataDef OP_BACKEND_SESSION_INFO = create( S5_BACKEND_ID_START + "S5SessionInfo", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_SESSION_INFO, //
      TSID_DESCRIPTION, STR_D_BACKEND_SESSION_INFO, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( IS5SessionInfo.NULL ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: Информация об открытых и завершенных сессиях пользователей
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link S5SessionsInfos}
   */
  IDataDef OP_BACKEND_SESSIONS_INFOS = create( S5_BACKEND_ID_START + "S5SessionsInfos", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_SESSIONS_INFOS, //
      TSID_DESCRIPTION, STR_D_BACKEND_SESSIONS_INFOS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5SessionsInfos( IList.EMPTY, IList.EMPTY ) ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: Информация об открытых и завершенных транзакциях сервера
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link S5TransactionInfos}
   */
  IDataDef OP_BACKEND_TRANSACTIONS_INFOS = create( S5_BACKEND_ID_START + "S5TransactionInfos", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_TRANSACTIONS_INFOS, //
      TSID_DESCRIPTION, STR_D_BACKEND_TRANSACTIONS_INFOS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE,
      avValobj( new S5TransactionInfos( 0, 0, IList.EMPTY, IList.EMPTY, IList.EMPTY, IList.EMPTY ) ) );

  /**
   * Параметр {@link ISkBackendInfo#params()}: текущая информация об использовании динамической памяти
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BACKEND_HEAP_MEMORY_USAGE = create( S5_BACKEND_ID_START + "HeapMemoryUsage", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_HEAP_MEMORY_USAGE, //
      TSID_DESCRIPTION, STR_D_BACKEND_HEAP_MEMORY_USAGE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  /**
   * Параметр {@link ISkBackendInfo#params()}: текущая информация об использовании статической памяти
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BACKEND_NON_HEAP_MEMORY_USAGE = create( S5_BACKEND_ID_START + "NonHeapMemoryUsage", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_NON_HEAP_MEMORY_USAGE, //
      TSID_DESCRIPTION, STR_D_BACKEND_NON_HEAP_MEMORY_USAGE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  /**
   * Параметр {@link ISkBackendInfo#params()}: текущая информация о состоянии платформы на которой работает сервер
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_BACKEND_PLATFORM_INFO = create( S5_BACKEND_ID_START + "PlatformInfo", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_BACKEND_PLATFORM_INFO, //
      TSID_DESCRIPTION, STR_D_BACKEND_PLATFORM_INFO, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  // ------------------------------------------------------------------------------------
  // Параметры s5-сессии
  //
  /**
   * String prefix of the all s5 session identifiers.
   */
  String S5_SESSION_ID_START = S5_ID_START + "session.";

  /**
   * Параметр {@link ISkSession#connectionCreationParams()}: IP-адрес или сетевое имя подключения серверу
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_SESSION_ADDRESS = create( S5_SESSION_ID_START + "Address", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_SESSION_ADDRESS, //
      TSID_DESCRIPTION, STR_D_SESSION_ADDRESS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( "localhost" ) );

  /**
   * Параметр {@link ISkSession#connectionCreationParams()}: Порт подключени к серверу
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_SESSION_PORT = create( S5_SESSION_ID_START + "Port", EAtomicType.INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_SESSION_PORT, //
      TSID_DESCRIPTION, STR_D_SESSION_PORT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( "8080" ) );

  /**
   * Параметр {@link ISkSession#backendSpecificParams()}: топология кластеров сервера доступная клиенту
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержит {@link S5ClusterTopology}
   */
  IDataDef OP_SESSION_CLUSTER_TOPOLOGY = create( S5_SESSION_ID_START + "Clusters", EAtomicType.VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_SESSION_CLUSTER_TOPOLOGY, //
      TSID_DESCRIPTION, STR_D_SESSION_CLUSTER_TOPOLOGY, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5ClusterTopology() ) );

  /**
   * Идентификатор параметра jvm (-Ds5.callback.port=...): номер порта используемый для создания обратных вызовов
   * удаленных сессий.
   * <p>
   * Тип: String
   */
  String OP_BACKEND_CALLBACK_PORT = "s5.callback.port";

  // ------------------------------------------------------------------------------------
  // Статистика сессии
  //
  /**
   * String prefix of the all s5 session identifiers.
   */
  String S5_SESSION_STAT_ID_START = S5_SESSION_ID_START + "statistics.";

  /**
   * Параметр статистики {@link IS5SessionInfo#statistics()}: Количество ошибок в сессии клиента
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_ERRORS = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "Errors", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_SESSION_ERRORS, //
      TSID_DESCRIPTION, STR_D_STAT_SESSION_ERRORS );

  /**
   * Параметр статистики {@link IS5SessionInfo#statistics()}: Количество сообщений отправленных клиенту
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_SENDED = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "Sended", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_SESSION_SENDED, //
      TSID_DESCRIPTION, STR_D_STAT_SESSION_SENDED );

  /**
   * Параметр статистики {@link IS5SessionInfo#statistics()}: Количество сообщений полученных от клиента
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_RECEVIED = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "Recevied", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_SESSION_RECEVIED, //
      TSID_DESCRIPTION, STR_D_STAT_SESSION_RECEVIED );

  /**
   * Параметр {@link IS5Statistic#params(IS5StatisticInterval)}: Количество сообщений с значениями текущими данных
   * полученных от клиента
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_RECEVIED_CURRDATA = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "ReceviedCurrdata", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_RECEVIED_CURRDATA, //
      TSID_DESCRIPTION, STR_D_STAT_RECEVIED_CURRDATA );

  /**
   * Параметр {@link IS5Statistic#params(IS5StatisticInterval)}: Количество сообщений с значениями хранимых данных
   * полученных от клиента
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_RECEVIED_HISTDATA = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "ReceviedHistdata", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_RECEVIED_HISTDATA, //
      TSID_DESCRIPTION, STR_D_STAT_RECEVIED_HISTDATA );

  /**
   * Параметр {@link IS5Statistic#params(IS5StatisticInterval)}: Количество сообщений с значениями текущими данными
   * переданные клиенту
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_SENDED_CURRDATA = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "SendedCurrdata", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_SENDED_CURRDATA, //
      TSID_DESCRIPTION, STR_D_STAT_SENDED_CURRDATA );

  /**
   * Параметр {@link IS5Statistic#params(IS5StatisticInterval)}: Количество сообщений с значениями хранимых данных
   * переданные клиенту
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_SESSION_SENDED_HISTDATA = S5StatisticParamInfo.create( //
      S5_SESSION_STAT_ID_START + "SendedHistdata", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( ALL, SECOND, MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_SENDED_HISTDATA, //
      TSID_DESCRIPTION, STR_D_STAT_SENDED_HISTDATA );

  /**
   * Список всех параметров статистики сессии {@link IS5SessionInfo#statistics()}
   */
  IStridablesList<S5StatisticParamInfo> STAT_SESSION_PARAMS = new StridablesList<>( //
      STAT_SESSION_SENDED, //
      STAT_SESSION_RECEVIED, //
      STAT_SESSION_RECEVIED_CURRDATA, //
      STAT_SESSION_RECEVIED_HISTDATA, //
      STAT_SESSION_SENDED_CURRDATA, //
      STAT_SESSION_SENDED_HISTDATA, //
      STAT_SESSION_ERRORS //
  );

  // ------------------------------------------------------------------------------------
  // Статистика узла бекенда (данные объектов {@link IS5ClassNode})
  //
  /**
   * String prefix of the all backend node identifiers.
   */
  String STAT_BACKEND_NODE_ID_START = S5_BACKEND_ID_START + "node.statistic.";

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): средняя загрузка операционной системы
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_LOAD_AVERAGE = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "LoadAverage", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_LOAD_AVERAGE, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_LOAD_AVERAGE );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): максимальная загрузка операционной системы
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_LOAD_MAX = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "LoadMax", //
      EStatisticFunc.MAX, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_LOAD_MAX, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_LOAD_MAX );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Объем свободной памяти операционной системы (байты)
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "FreePhysicalMemory", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Максимальный объем heap памяти (байты)
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_MAX_HEAP_MEMORY = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "MaxHeapMemory", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_MAX_HEAP_MEMORY, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_MAX_HEAP_MEMORY );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Используемый объем heap памяти (байты)
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_USED_HEAP_MEMORY = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "UsedHeapMemory", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_USED_HEAP_MEMORY, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_USED_HEAP_MEMORY );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Максимальный объем non-heap памяти (байты)
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "MaxNonHeapMemory", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Используемый объем non-heap памяти (байты)
   * <p>
   * Тип: {@link EAtomicType#FLOATING}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "UsedNonHeapMemory", //
      EStatisticFunc.AVERAGE, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.FLOATING, avFloat( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Максимальное количество открытых сессий на
   * интервале
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_OPEN_SESSION_MAX = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "OpenSessionsMax", //
      EStatisticFunc.MAX, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_OPEN_SESSION_MAX, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_OPEN_SESSION_MAX );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Максимальное количество открытых транзакций на
   * интервале
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_OPEN_TX_MAX = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "OpenTxMax", //
      EStatisticFunc.MAX, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_OPEN_TX_MAX, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_OPEN_TX_MAX );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Количество завершенных транзакций
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_COMMIT_TX = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "CommitTx", //
      EStatisticFunc.COUNT, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_COMMIT_TX, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_COMMIT_TX );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Количество откатов по транзакциям
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_ROLLBACK_TX = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "RollbackTx", //
      EStatisticFunc.COUNT, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_ROLLBACK_TX, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_ROLLBACK_TX );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Количество принятых pas-пакетов
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_PAS_RECEIVED = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PasReceived", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_PAS_RECEIVED, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_PAS_RECEIVED );

  /**
   * Параметр статистики узла бекенда (данное {@link IS5ClassNode}): Количество отправленных pas-пакетов
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_BACKEND_NODE_PAS_SEND = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PasSend", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_BACKEND_NODE_PAS_SEND, //
      TSID_DESCRIPTION, STR_D_STAT_BACKEND_NODE_PAS_SEND );

  /**
   * Список всех параметров статистики узла бекенда (данные {@link IS5ClassNode})
   */
  IStridablesList<S5StatisticParamInfo> STAT_BACKEND_NODE_PARAMS = new StridablesList<>( //
      STAT_BACKEND_NODE_LOAD_AVERAGE, //
      STAT_BACKEND_NODE_LOAD_MAX, //
      STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY, //
      STAT_BACKEND_NODE_MAX_HEAP_MEMORY, //
      STAT_BACKEND_NODE_USED_HEAP_MEMORY, //
      STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY, //
      STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY, //
      STAT_BACKEND_NODE_OPEN_SESSION_MAX, //
      STAT_BACKEND_NODE_OPEN_TX_MAX, //
      STAT_BACKEND_NODE_COMMIT_TX, //
      STAT_BACKEND_NODE_ROLLBACK_TX, //
      STAT_BACKEND_NODE_PAS_RECEIVED, //
      STAT_BACKEND_NODE_PAS_SEND //
  );

  // ------------------------------------------------------------------------------------
  // Статистика поддержки бекенда формирующий хранимые данные (данные объектов {@link IS5ClassHistorableBackend})
  //
  /**
   * String prefix of the all s5 session identifiers.
   */
  String STAT_HISTORABLE_BACKEND_ID_START = S5_BACKEND_ID_START + "node.dbms.statistic.";

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество записей (транзакций) в базу данных
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_WRITED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "WriteCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_WRITE_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_WRITE_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество загруженных блоков
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_LOADED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "LoadedCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_LOADED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_LOADED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}): Общее
   * время загрузки блоков (мсек)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_LOADED_TIME = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "LoadedTime", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_LOADED_TIME, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_LOADED_TIME );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество добавленных блоков
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_INSERTED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "InsertCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_INSERT_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_INSERT_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}): Общее
   * время добавления блоков (мсек)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_INSERTED_TIME = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "InsertTime", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_INSERT_TIME, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_INSERT_TIME );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество обновленных блоков
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_MERGED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "MergeCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_MERGE_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_MERGE_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}): Общее
   * время обновления блоков (мсек)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_MERGED_TIME = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "MergeTime", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_MERGE_TIME, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_MERGE_TIME );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество удаленных блоков
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_REMOVED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "RemovedCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_REMOVED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_REMOVED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}): Общее
   * время удаления блоков (мсек)
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_REMOVED_TIME = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "RemovedTime", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_REMOVED_TIME, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_REMOVED_TIME );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество ошибок записи блоков
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_ERROR_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "ErrorCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_ERROR_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_ERROR_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество выполненных дефрагментаций
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentCount", //
      EStatisticFunc.COUNT, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество проанализированных данных при поиске дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentLookupCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество обработанных данных при дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentThreadCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество обработанных данных при дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentValueCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество обновленных блоков (merged) при дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentMergedCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество удаленных (removed) блоков при дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentRemovedCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество ошибок дефрагментации
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "DefragmentErrorCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество выполненных обработок разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionTaskCount", //
      EStatisticFunc.COUNT, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество проверенных таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionLookupCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество операций обработки разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionThreadCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество добавленных разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionAddedCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество удаленных разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionRemoved", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество удаленных блоков при удалении разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionBlocksRemoved", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT );

  /**
   * Параметр статистики поддержки бекенда формирующий хранимые данные (данное {@link IS5ClassHistorableBackend}):
   * Количество ошибок обработки разделов таблиц
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  S5StatisticParamInfo STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT = S5StatisticParamInfo.create( //
      STAT_BACKEND_NODE_ID_START + "PartitionErrorCount", //
      EStatisticFunc.SUMMA, //
      new StridablesList<>( MINUTE, HOUR, DAY ), //
      EAtomicType.INTEGER, avInt( 0 ), //
      TSID_NAME, STR_N_STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT, //
      TSID_DESCRIPTION, STR_D_STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT );

  /**
   * Список всех параметров статистики поддержки бекенда формирующий хранимые данные (данные {@link IS5ClassNode})
   */
  IStridablesList<S5StatisticParamInfo> STAT_HISTORABLE_BACKEND_PARAMS = new StridablesList<>( //
      STAT_HISTORABLE_BACKEND_WRITED_COUNT, //
      STAT_HISTORABLE_BACKEND_LOADED_COUNT, //
      STAT_HISTORABLE_BACKEND_LOADED_TIME, //
      STAT_HISTORABLE_BACKEND_INSERTED_COUNT, //
      STAT_HISTORABLE_BACKEND_INSERTED_TIME, //
      STAT_HISTORABLE_BACKEND_MERGED_COUNT, //
      STAT_HISTORABLE_BACKEND_MERGED_TIME, //
      STAT_HISTORABLE_BACKEND_REMOVED_COUNT, //
      STAT_HISTORABLE_BACKEND_REMOVED_TIME, //
      STAT_HISTORABLE_BACKEND_ERROR_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT, //
      STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT, //
      STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT //
  );
}
