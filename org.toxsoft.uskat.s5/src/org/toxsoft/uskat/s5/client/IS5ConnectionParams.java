package org.toxsoft.uskat.s5.client;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.create;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.create;
import static org.toxsoft.uskat.s5.client.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRefDef;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.uskat.s5.common.S5HostList;
import org.toxsoft.uskat.s5.utils.progress.IS5ProgressMonitor;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Параметры соединения с s5
 *
 * @author mvk
 */
public interface IS5ConnectionParams {

  /**
   * Параметр: Идентификатор сессии. Создается при создании нового соединения ISkConnection
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link Skid})
   */
  IDataDef OP_SESSION_ID = create( "sessionID", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_SESSION_ID, //
      TSID_DESCRIPTION, D_SESSION_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) );

  // ------------------------------------------------------------------------------------
  // Учетная запись пользователя (для remote)
  //
  /**
   * Параметр: Имя пользователя
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_USERNAME = create( "username", STRING, //$NON-NLS-1$
      TSID_NAME, N_USERNAME, //
      TSID_DESCRIPTION, D_USERNAME, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Пароль пользователя
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_PASSWORD = create( "password", STRING, //$NON-NLS-1$
      TSID_NAME, N_PASSWORD, //
      TSID_DESCRIPTION, D_PASSWORD, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  // ------------------------------------------------------------------------------------
  // Учетная запись пользователя (для local)
  //
  /**
   * Параметр: Имя модуля создавшего локальное подключение к бекенду
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_LOCAL_MODULE = create( "localModule", STRING, //$NON-NLS-1$
      TSID_NAME, N_LOCAL_MODULE, //
      TSID_DESCRIPTION, D_LOCAL_MODULE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Имя локального узла на котором создано локальное подключение к бекенду.
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_LOCAL_NODE = create( "localNode", STRING, //$NON-NLS-1$
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
  IDataDef OP_WILDFLY_LOGIN = create( "wildfly_login", STRING, //$NON-NLS-1$
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
  IDataDef OP_WILDFLY_PASSWORD = create( "wildfly_password", STRING, //$NON-NLS-1$
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
  IDataDef OP_HOSTS = create( "hosts", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_HOSTS, //
      TSID_DESCRIPTION, D_HOSTS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new S5HostList() ) );

  // ------------------------------------------------------------------------------------
  // Информация передаваемая серверу о локальном клиенте
  //
  /**
   * Параметр: Адрес (IP или сетевое имя) хоста на котором работает программа клиента
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_CLIENT_ADDRESS = create( "clientAddress", STRING, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_ADDRESS, //
      TSID_DESCRIPTION, D_CLIENT_ADDRESS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Порт через который программа клиента подключается к серверу
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CLIENT_PORT = create( "clientPort", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_PORT, //
      TSID_DESCRIPTION, D_CLIENT_PORT, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_0 );

  /**
   * Параметр: Имя программы клиента
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef OP_CLIENT_PROGRAM = create( "clientProgram", STRING, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_PROGRAM, //
      TSID_DESCRIPTION, D_CLIENT_PROGRAM, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Параметр: Версия программы клиента
   * <p>
   * Тип: {@link EAtomicType#VALOBJ}({@link TsVersion})
   */
  IDataDef OP_CLIENT_VERSION = create( "clientVersion", VALOBJ, //$NON-NLS-1$
      TSID_NAME, N_CLIENT_VERSION, //
      TSID_DESCRIPTION, D_CLIENT_VERSION, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avValobj( new TsVersion( 0, 0 ) ) );

  // ------------------------------------------------------------------------------------
  // Таймауты соединения
  //
  /**
   * Параметр: Таймаут (мсек) подключения к серверу
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CONNECT_TIMEOUT = create( "connectTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_CONNECT_TIMEOUT, //
      TSID_DESCRIPTION, D_CONNECT_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 10000 ) );

  /**
   * Параметр: Таймаут (мсек) ожидания ожидания пакетов через соединение после которого производится разрыв соединения
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_FAILURE_TIMEOUT = create( "failureTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_FAILURE_TIMEOUT, //
      TSID_DESCRIPTION, D_FAILURE_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 3000 ) );

  /**
   * Параметр: Минимальный интервал (мсек) передачи пакетов текущих данных через соединение. <=0 - передавать немедленно
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef OP_CURRDATA_TIMEOUT = create( "currdataTimeout", INTEGER, //$NON-NLS-1$
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
  IDataDef OP_HISTDATA_TIMEOUT = create( "histdataTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, N_HISTDATA_TIMEOUT, //
      TSID_DESCRIPTION, D_HISTDATA_TIMEOUT, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, avInt( 60000 ) );

  // ------------------------------------------------------------------------------------
  // Параметры контекста соединения
  //
  /**
   * Параметр: блокировка доступа к данным соединения
   * <p>
   * Тип: {@link S5Lockable}
   */
  ITsContextRefDef<S5Lockable> REF_CONNECTION_LOCK = create( "connectionLock", S5Lockable.class, //$NON-NLS-1$
      TSID_NAME, N_CONNECTION_LOCK, //
      TSID_DESCRIPTION, D_CONNECTION_LOCK, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр: Загрузчик классов используемый соединением
   * <p>
   * Тип: {@link ClassLoader}
   */
  ITsContextRefDef<ClassLoader> REF_CLASSLOADER = create( "classLoader", ClassLoader.class, //$NON-NLS-1$
      TSID_NAME, N_CLASSLOADER, //
      TSID_DESCRIPTION, D_CLASSLOADER, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );

  /**
   * Параметр: Монитор работы используемый для соединения с s5-сервером
   * <p>
   * Тип: {@link IS5ProgressMonitor}
   */
  ITsContextRefDef<IS5ProgressMonitor> REF_MONITOR = create( "progressMonitor", IS5ProgressMonitor.class, //$NON-NLS-1$
      TSID_NAME, N_MONITOR, //
      TSID_DESCRIPTION, D_MONITOR, //
      TSID_IS_NULL_ALLOWED, AV_TRUE );

}
