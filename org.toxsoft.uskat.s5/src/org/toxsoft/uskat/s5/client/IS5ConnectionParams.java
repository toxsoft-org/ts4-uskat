package org.toxsoft.uskat.s5.client;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.create;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.create;
import static org.toxsoft.uskat.s5.client.IS5Resources.*;

import java.time.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.utils.progress.*;

/**
 * Параметры соединения с s5
 *
 * @author mvk
 */
public interface IS5ConnectionParams {

  /**
   * Префикс идентфикаторов подсистемы
   */
  String SYBSYSTEM_ID_PREFIX = IS5ServerHardConstants.S5_FULL_ID + ".client"; //$NON-NLS-1$

  /**
   * Параметр: Идентификатор сессии. Создается при создании нового соединения ISkConnection
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link Skid})
   */
  IDataDef OP_SESSION_ID = create( SYBSYSTEM_ID_PREFIX + ".sessionID", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_SESSION_ID, //
      TSID_DESCRIPTION, D_SESSION_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) );

  // ------------------------------------------------------------------------------------
  // Учетная запись пользователя (для remote)
  //
  // 2025-05-12 mvk ---
  // /**
  // * Параметр: Логин пользователя для подключения к серверу
  // * <p>
  // * Тип: {@link EAtomicType#STRING}
  // */
  // IDataDef OP_USERNAME = create( SYBSYSTEM_ID_PREFIX + ".username", STRING, //$NON-NLS-1$
  // TSID_NAME, N_USERNAME, //
  // TSID_DESCRIPTION, D_USERNAME, //
  // TSID_IS_NULL_ALLOWED, AV_FALSE, //
  // TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );
  //
  // /**
  // * Параметр: Пароль пользователя для подключения к серверу
  // * <p>
  // * Тип: {@link EAtomicType#STRING}
  // */
  // IDataDef OP_PASSWORD = create( SYBSYSTEM_ID_PREFIX + ".password", STRING, //$NON-NLS-1$
  // TSID_NAME, N_PASSWORD, //
  // TSID_DESCRIPTION, D_PASSWORD, //
  // TSID_IS_NULL_ALLOWED, AV_FALSE, //
  // TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );
  //
  // /**
  // * Параметр: Роль пользователя для подключения к серверу
  // * <p>
  // * Тип: {@link EAtomicType#VALOBJ} ({@link Skid})
  // */
  // IDataDef OP_ROLE = create( SYBSYSTEM_ID_PREFIX + ".role", VALOBJ, //$NON-NLS-1$
  // TSID_NAME, N_ROLE, //
  // TSID_DESCRIPTION, D_ROLE, //
  // TSID_IS_NULL_ALLOWED, AV_FALSE, //
  // TSID_DEFAULT_VALUE, AvUtils.avValobj( ISkUserServiceHardConstants.SKID_ROLE_GUEST ) );
  /**
   * Параметр: Логин пользователя для подключения к серверу
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_USERNAME = ISkConnectionConstants.ARGDEF_LOGIN;

  /**
   * Параметр: Пароль пользователя для подключения к серверу
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_PASSWORD = ISkConnectionConstants.ARGDEF_PASSWORD;

  /**
   * Параметр: Роль пользователя для подключения к серверу
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} ({@link Skid})
   */
  IDataDef OP_ROLE = ISkConnectionConstants.ARGDEF_ROLE;

  // ------------------------------------------------------------------------------------
  // Учетная запись пользователя (для local)
  //
  /**
   * Параметр: Имя модуля создавшего локальное подключение к бекенду
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_LOCAL_MODULE = create( SYBSYSTEM_ID_PREFIX + ".localModule", STRING, //$NON-NLS-1$
      TSID_NAME, N_LOCAL_MODULE, //
      TSID_DESCRIPTION, D_LOCAL_MODULE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Имя локального узла на котором создано локальное подключение к бекенду.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_LOCAL_NODE = create( SYBSYSTEM_ID_PREFIX + ".localNode", STRING, //$NON-NLS-1$
      TSID_NAME, N_LOCAL_NODE, //
      TSID_DESCRIPTION, D_LOCAL_NODE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  // ------------------------------------------------------------------------------------
  // Адресная информация для подключения к бекенду
  //
  /**
   * Параметр: Имя учетной записи (login) под которой произоводится подключение к серверу wildfly.
   * <p>
   * Под одной и той же учетной записью может работать несколько пользователей. Управление учетными записями
   * осуществляется средствами wildfly (add-user, application-users.properties, application-users.roles.properties).
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_WILDFLY_LOGIN = create( SYBSYSTEM_ID_PREFIX + ".wildfly_login", STRING, //$NON-NLS-1$
      TSID_NAME, N_WILDFLY_PASSWORD, //
      TSID_DESCRIPTION, D_WILDFLY_PASSWORD, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( "root" ) ); //$NON-NLS-1$

  /**
   * Параметр: Пароль учетной записи под которой произоводится подключение к серверу wildfly.
   * <p>
   * Под одной и той же учетной записью может работать несколько пользователей. Управление учетными записями
   * осуществляется средствами wildfly (add-user, application-users.properties, application-users.roles.properties).
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_WILDFLY_PASSWORD = create( SYBSYSTEM_ID_PREFIX + ".wildfly_password", STRING, //$NON-NLS-1$
      TSID_NAME, N_WILDFLY_PASSWORD, //
      TSID_DESCRIPTION, D_WILDFLY_PASSWORD, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( "1" ) ); //$NON-NLS-1$

  /**
   * Параметр: Список возможных точек подключения(адресов) к серверу в порядке понижения приоритета (первый основной, с
   * высшим приоритетом).
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link S5HostList})
   */
  IDataDef OP_HOSTS = create( SYBSYSTEM_ID_PREFIX + ".hosts", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_HOSTS, //
      TSID_DESCRIPTION, D_HOSTS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_KEEPER_ID, S5HostList.KEEPER_ID, //
      TSID_DEFAULT_VALUE, avValobj( new S5HostList() ) );

  // ------------------------------------------------------------------------------------
  // Информация передаваемая серверу о локальном клиенте
  //
  /**
   * Параметр: Адрес (IP или сетевое имя) хоста на котором работает программа клиента
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_CLIENT_ADDRESS = create( SYBSYSTEM_ID_PREFIX + ".clientAddress", STRING, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_ADDRESS, //
      TSID_DESCRIPTION, D_CLIENT_ADDRESS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Порт через который программа клиента подключается к серверу
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CLIENT_PORT = create( SYBSYSTEM_ID_PREFIX + ".clientPort", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_PORT, //
      TSID_DESCRIPTION, D_CLIENT_PORT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_0 );

  /**
   * Параметр: Имя программы клиента
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_CLIENT_PROGRAM = create( SYBSYSTEM_ID_PREFIX + ".clientProgram", STRING, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_PROGRAM, //
      TSID_DESCRIPTION, D_CLIENT_PROGRAM, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Версия программы клиента
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link TsVersion})
   */
  IDataDef OP_CLIENT_VERSION = create( SYBSYSTEM_ID_PREFIX + ".clientVersion", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_VERSION, //
      TSID_DESCRIPTION, D_CLIENT_VERSION, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new TsVersion( 1, 1, 2022, Month.JANUARY, 1 ) ) );

  // ------------------------------------------------------------------------------------
  // Таймауты соединения
  //
  /**
   * Параметр: Таймаут (мсек) подключения к серверу
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CONNECT_TIMEOUT = create( SYBSYSTEM_ID_PREFIX + ".connectTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_CONNECT_TIMEOUT, //
      TSID_DESCRIPTION, D_CONNECT_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 10000 ) );

  /**
   * Параметр: Таймаут (мсек) ожидания ожидания пакетов через соединение после которого производится разрыв соединения
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_FAILURE_TIMEOUT = create( SYBSYSTEM_ID_PREFIX + ".failureTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FAILURE_TIMEOUT, //
      TSID_DESCRIPTION, D_FAILURE_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 3000 ) );

  /**
   * Параметр: Минимальный интервал (мсек) передачи пакетов текущих данных через соединение. <=0 - передавать немедленно
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CURRDATA_TIMEOUT = create( SYBSYSTEM_ID_PREFIX + ".currdataTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_CURRDATA_TIMEOUT, //
      TSID_DESCRIPTION, D_CURRDATA_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 1000 ) );

  /**
   * Параметр: Минимальный интервал (мсек) передачи пакетов хранимых данных через соединение. <=0 - передавать
   * немедленно
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_HISTDATA_TIMEOUT = create( SYBSYSTEM_ID_PREFIX + ".histdataTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_HISTDATA_TIMEOUT, //
      TSID_DESCRIPTION, D_HISTDATA_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 60000 ) );

  /**
   * Параметр: Таймаут выполнения фонового процесса клиента.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_DOJOB_TIMEOUT = create( SYBSYSTEM_ID_PREFIX + ".doJobTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_DOJOB_TIMEOUT, //
      TSID_DESCRIPTION, D_DOJOB_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 10 ) );

  /**
   * Параметр: Максимальный размер буфера для накопления хранимых значений одного параметра в моменты отсутствия связи
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_HISTDATA_BUFFER_SIZE = create( SYBSYSTEM_ID_PREFIX + ".histdataBufferSize", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_HISTDATA_BUFFER_SIZE, //
      TSID_DESCRIPTION, D_HISTDATA_BUFFER_SIZE, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 259_200 ) ); // по умолчанию 3 дня секундных данных

  // ------------------------------------------------------------------------------------
  // Параметры контекста соединения
  //
  /**
   * Параметр: блокировка доступа к данным соединения
   * <p>
   * Тип: {@link S5Lockable}
   */
  // ITsContextRefDef<S5Lockable> REF_CONNECTION_LOCK = create( "connectionLock", S5Lockable.class, //$NON-NLS-1$
  // TSID_NAME, N_CONNECTION_LOCK, //
  // TSID_DESCRIPTION, D_CONNECTION_LOCK, //
  // TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр: Загрузчик классов используемый соединением
   * <p>
   * Тип: {@link ClassLoader}
   */
  ITsContextRefDef<ClassLoader> REF_CLASSLOADER = create( SYBSYSTEM_ID_PREFIX + ".classLoader", ClassLoader.class, //$NON-NLS-1$
      TSID_NAME, N_CLASSLOADER, //
      TSID_DESCRIPTION, D_CLASSLOADER, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр: Монитор работы используемый для соединения с s5-сервером
   * <p>
   * Тип: {@link IS5ProgressMonitor}
   */
  ITsContextRefDef<IS5ProgressMonitor> REF_MONITOR =
      create( SYBSYSTEM_ID_PREFIX + ".progressMonitor", IS5ProgressMonitor.class, //$NON-NLS-1$
          TSID_NAME, N_MONITOR, //
          TSID_DESCRIPTION, D_MONITOR, //
          TSID_IS_NULL_ALLOWED, AV_TRUE );

}
