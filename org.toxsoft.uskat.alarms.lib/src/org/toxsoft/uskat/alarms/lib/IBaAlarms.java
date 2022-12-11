package org.toxsoft.uskat.alarms.lib;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.alarms.lib.filters.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.alarms.lib.impl.SkAlarmUtils;

/**
 * Backend addon for alarms.
 *
 * @author mvk
 */
public interface IBaAlarms {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkAlarmServiceHardConstants.BAID_ALARMS;

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
   * @param aAlarmDef {@link ISkAlarmDef} - регистрируемый тип тревоги
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException тип с указанным идентификатором уже зарегистрирован
   */
  void registerAlarmDef( ISkAlarmDef aAlarmDef );

  // ------------------------------------------------------------------------------------
  // Работа с тревогами
  //

  // ------------------------------------------------------------------------------------
  // Подписка на алармы
  //
  /**
   * Устанавливает фильтры алармов в которых заинтересован клиент
   *
   * @param aFilters {@link IList}&lt;{@link ITsCombiFilterParams}&gt; список описаний фильтров
   */
  void setAlarmFilters( IList<ITsCombiFilterParams> aFilters );

  /**
   * Генерирует {@link ISkAlarm}
   *
   * @param aAlarmDefId String идентификтор аларма
   * @param aAuthorId {@link Skid} автор аларма
   * @param aUserId {@link Skid} пользователь системы сгенеривший аларм
   * @param aSublevel byte уточнение аларма
   * @param aAlarmFlacon {@link ISkAlarmFlacon} флакон
   * @return сгенерированный аларм
   */
  ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aAlarmFlacon );

  /**
   * Добавляет в историю тревоги элемент - отметку о прохождении нитки оповещения.
   *
   * @param aAlarmId long - идентификатор тревоги
   * @param aItem {@link ISkAlarmThreadHistoryItem}
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такой тревоги
   */
  void addThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem );

  // ------------------------------------------------------------------------------------
  // Работа с историей
  //

  /**
   * Осуществляет выборку запрошенных тревог за указанный интервал времени.
   * <p>
   * В результат попадают тревоги, которые удовлетворяют требованиям фильтра aQueryParams и метка времени
   * {@link ISkAlarm#timestamp()} попадает в запрошенный интервал времени (включая границы интервала). *
   * <p>
   * Аргумент aFilter определяется параметрами единичных фильтров фильтрами тревог:
   * <ul>
   * <li>{@link SkAlarmFilterByAuthor};</li>
   * <li>{@link SkAlarmFilterByDefId};</li>
   * <li>{@link SkAlarmFilterByLevel};</li>
   * <li>{@link SkAlarmFilterByMessage};</li>
   * <li>{@link SkAlarmFilterByPriority};</li>
   * <li>{@link SkAlarmFilterByHistory};</li>
   * <li>{@link SkAlarmFilterByTimestamp}.</li>
   * </ul>
   * Для упрощения можно использовать утилитные методы создания параметров фильтра из {@link SkAlarmUtils}. *
   *
   * @param aTimeInterval {@link ITimeInterval} - интервал времени запроса
   * @param aFilter {@link ITsCombiFilterParams} - параметры фильтра выборки тревог
   * @return {@link ITimedList}&lt;{@link ISkAlarm}&gt; - список выбранных тревог
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter );

}
