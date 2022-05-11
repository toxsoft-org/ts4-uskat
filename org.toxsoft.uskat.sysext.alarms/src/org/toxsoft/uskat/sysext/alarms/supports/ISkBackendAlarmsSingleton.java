package org.toxsoft.uskat.sysext.alarms.supports;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.sysext.alarms.addon.ISkBackendAddonAlarm;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByDefId;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByLevel;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

/**
 * Поддержка расширения бекенда для службы качества данных {@link ISkBackendAddonAlarm}.
 *
 * @author mvk
 */
@Local
public interface ISkBackendAlarmsSingleton
    extends IS5BackendSupportSingleton {

  // ------------------------------------------------------------------------------------
  // Работа с описаниями тревог
  //
  /**
   * Возвращает список известных тревог.
   *
   * @return IStridablesList&lt;{@link ISkAlarmDef}&gt; - список известных тревог
   */
  IStridablesList<ISkAlarmDef> listAlarmDefs();

  /**
   * Находит описание тревоги по идентификатору типа.
   *
   * @param aAlarmDefId String - идентификатор типа тревоги
   * @return {@link ISkAlarmDef} - найденное описание или null, если нет такового
   * @throws TsNullArgumentRtException аргумент = null
   */
  ISkAlarmDef findAlarmDef( String aAlarmDefId );

  /**
   * регистрирует тип тревоги.
   *
   * @param aSkAlarmDef {@link ISkAlarmDef} - регистрируемый тип тревоги
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException тип с указанным идентификатором уже зарегистрирован
   */
  void registerAlarmDef( ISkAlarmDef aSkAlarmDef );

  // ------------------------------------------------------------------------------------
  // Работа с тревогами
  //
  /**
   * Генерирует {@link ISkAlarm}
   *
   * @param aAlarmDefId String идентификтор аларма
   * @param aAuthorId {@link Skid} автор аларма
   * @param aUserId {@link Skid} пользователь системы сгенеривший аларм
   * @param aSublevel byte уточнение аларма
   * @param aSkAlarmFlacon {@link ISkAlarmFlacon} флакон
   * @return сгенерированный аларм
   */
  ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon );

  /**
   * Добавляет в историю тревоги элемент - отметку о прохождении нитки оповещения.
   *
   * @param aAlarmId long - идентификатор тревоги
   * @param aItem {@link ISkAnnounceThreadHistoryItem}
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такой тревоги
   */
  void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem );

  /**
   * Возвращает срез данных, вызвавший тревогу.
   *
   * @param aAlarmId long - идентификатор тревоги
   * @return {@link ISkAlarmFlacon} - срез данных
   * @throws TsItemNotFoundRtException нет тревоги с таким идентификатором
   */
  ISkAlarmFlacon getAlarmFlacon( long aAlarmId );

  /**
   * Возвращает историю отображения и обработки (квитирования) тревоги.
   * <p>
   * В зависимости о параметров в описании {@link ISkAlarmDef}, история может не вестись, и будет возвращен пустой
   * список.
   *
   * @param aAlarmId long - идентификатор тревоги
   * @return {@link ITimedList}&lt;{@link ISkAnnounceThreadHistoryItem}&gt; - список собйтий обработки тревоги
   */
  ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId );

  // ------------------------------------------------------------------------------------
  // Работа с историей
  //

  /**
   * Осуществляет выборку запрошенных тревог за указанный интервал времени.
   * <p>
   * В результат попадают тревоги, которые удовлетворяют требованиям фильтра aQueryParams и метка времени
   * {@link ISkAlarm#timestamp()} попадает в запрошенный интервал времени (включая границы интервала). *
   * <p>
   * Аргумент aQueryParams определяются параметрами единичных фильтров {@link ISkAlarmFilterByDefId},
   * {@link ISkAlarmFilterByLevel} и другими фильтрами тревог. Для упрощения можно использовать утилитные методы
   * создания параметров фильтра из {@link SkAlarmUtils}.
   * <p>
   * Получить срез и историю изменения состояния тревог можно метдами {@link #getAlarmFlacon(long)} и
   * {@link #getAlarmHistory(long)}.
   *
   * @param aTimeInterval {@link ITimeInterval} - интервал времени запроса
   * @param aQueryParams {@link ITsCombiFilterParams} - параметры фильтра выборки тревог
   * @return {@link ITimedList}&lt;{@link ISkAlarm}&gt; - список выбранных тревог
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams );
}
