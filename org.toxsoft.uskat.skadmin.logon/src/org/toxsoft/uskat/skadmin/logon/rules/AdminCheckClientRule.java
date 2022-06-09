package org.toxsoft.uskat.skadmin.logon.rules;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.skadmin.logon.rules.IAdminResources.*;

import java.util.Arrays;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.statistics.EStatisticInterval;

/**
 * Правило проверки клиента
 *
 * @author mvk
 */
class AdminCheckClientRule
    implements IAdminCheckClientRule {

  private EClientRuleType type             = EClientRuleType.MAY_BE;
  private int             startTime        = -1;
  private int             endTime          = -1;
  private String          ip               = EMPTY_STRING;
  private int             port             = -1;
  private IStringListEdit clientFeatureIds = new StringArrayList();
  private int             sendedMin[];
  private int             sendedMax[];
  private int             receivedMin[];
  private int             receivedMax[];
  private int             queriesMin[];
  private int             queriesMax[];
  private int             errorsMax[];
  private String          login;

  /**
   * Конструктор
   */
  AdminCheckClientRule() {
    int intervalCount = EStatisticInterval.values().length;
    sendedMin = new int[intervalCount];
    sendedMax = new int[intervalCount];
    receivedMin = new int[intervalCount];
    receivedMax = new int[intervalCount];
    queriesMin = new int[intervalCount];
    queriesMax = new int[intervalCount];
    errorsMax = new int[intervalCount];
    Arrays.fill( sendedMin, -1 );
    Arrays.fill( sendedMax, -1 );
    Arrays.fill( receivedMin, -1 );
    Arrays.fill( receivedMax, -1 );
    Arrays.fill( queriesMin, -1 );
    Arrays.fill( queriesMax, -1 );
    Arrays.fill( errorsMax, -1 );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Установить тип правила
   *
   * @param aType {@link EClientRuleType} тип правила
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setType( EClientRuleType aType ) {
    TsNullArgumentRtException.checkNull( aType );
    type = aType;
  }

  /**
   * Устанавливает время (сек) от начала суток начала действия правила
   *
   * @param aTime int время(сек) от начала суток начала действия правила (включительно). < 0: не установлено
   */
  void setStartTime( int aTime ) {
    if( endTime >= 0 && aTime >= 0 && aTime > endTime ) {
      // Неверно указанное внутрисуточное время
      Integer st = Integer.valueOf( aTime );
      Integer et = Integer.valueOf( endTime );
      throw new TsIllegalArgumentRtException( MSG_ERR_WRONG_TIME, st, et );
    }
    startTime = aTime;
  }

  /**
   * Устанавливает время (сек) от начала суток завершения действия правила
   *
   * @param aTime int время(сек) от начала суток начала действия правила (невключительно). < 0: не установлено
   */
  void setEndTime( int aTime ) {
    if( startTime >= 0 && aTime >= 0 && startTime > aTime ) {
      // Неверно указанное внутрисуточное время
      Integer st = Integer.valueOf( startTime );
      Integer et = Integer.valueOf( aTime );
      throw new TsIllegalArgumentRtException( MSG_ERR_WRONG_TIME, st, et );
    }
    endTime = aTime;
  }

  /**
   * Установить ip-адрес клиента
   *
   * @param aIp String ip-адрес клиента. Пустая строка: любой ip
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setIp( String aIp ) {
    TsNullArgumentRtException.checkNull( aIp );
    ip = aIp;
  }

  /**
   * Установить порт клиента
   *
   * @param aPort int порт клиента. < 0: любой порт
   */
  void setPort( int aPort ) {
    port = aPort;
  }

  /**
   * Устанавливает login клиента
   *
   * @param aLogin String login клиента. Пустая строка: любой логин
   */
  void setLogin( String aLogin ) {
    TsNullArgumentRtException.checkNull( aLogin );
    login = aLogin;
  }

  /**
   * Добавить идентификатор особенности клиента.
   *
   * @param aFeatureIds {@link IStringList} список идентификаторов (ИД-пути) особенностей клиента
   */
  void addClientFeatureIds( IStringList aFeatureIds ) {
    TsNullArgumentRtException.checkNull( aFeatureIds );
    clientFeatureIds.addAll( aFeatureIds );
  }

  /**
   * Установить минимальное количество отправленных сообщений клиенту
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество отправленных сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setSendedMin( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    sendedMin[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить максимальное количество отправленных сообщений клиенту
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество отправленных сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setSendedMax( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    sendedMax[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить минимальное количество принятых сообщений от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество принятых сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setReceivedMin( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    receivedMin[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить максимальное количество принятых сообщений от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество принятых сообщений. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setReceivedMax( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    receivedMax[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить минимальное количество запросов от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество принятых запросов. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setQueriesMin( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    queriesMin[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить максимальное количество запросов от клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество принятых запросов. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setQueriesMax( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    queriesMax[aInterval.ordinal()] = aCount;
  }

  /**
   * Установить максимальное количество ошибок обработки запросов или данных клиента
   *
   * @param aInterval {@link EStatisticInterval} интервал времени по которому запрашиваются данные
   * @param aCount int количество ошибок обработки. < 0: не установлено
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setErrorsMax( EStatisticInterval aInterval, int aCount ) {
    TsNullArgumentRtException.checkNull( aInterval );
    errorsMax[aInterval.ordinal()] = aCount;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCheckClientRule
  //
  @Override
  public EClientRuleType type() {
    return type;
  }

  @Override
  public int startTime() {
    return startTime;
  }

  @Override
  public int endTime() {
    return endTime;
  }

  @Override
  public String ip() {
    return ip;
  }

  @Override
  public int port() {
    return port;
  }

  @Override
  public String login() {
    return login;
  }

  @Override
  public IStringList clientFeatureIds() {
    return clientFeatureIds;
  }

  @Override
  public int sendedMin( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return sendedMin[aInterval.ordinal()];
  }

  @Override
  public int sendedMax( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return sendedMax[aInterval.ordinal()];
  }

  @Override
  public int receivedMin( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return receivedMin[aInterval.ordinal()];
  }

  @Override
  public int receivedMax( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return receivedMax[aInterval.ordinal()];
  }

  @Override
  public int queriesMin( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return queriesMin[aInterval.ordinal()];
  }

  @Override
  public int queriesMax( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return queriesMax[aInterval.ordinal()];
  }

  @Override
  public int errorsMax( EStatisticInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    return errorsMax[aInterval.ordinal()];
  }

}
