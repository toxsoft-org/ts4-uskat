package org.toxsoft.uskat.sysext.alarms.api;

import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByDefId;
import org.toxsoft.uskat.sysext.alarms.api.filters.ISkAlarmFilterByLevel;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.uskat.core.api.ISkService;

/**
 * Служба тревог.
 * <p>
 * <b>Тревога</b> - это сущность которая создается при определенном сочетаний состояний предметной области (зеленого
 * мира) и единственное использование - донести информацию до человека. У этого определения две части, которые требуют
 * пояснения:
 * <ul>
 * <li>тревога {@link ISkAlarm} создается единомоментно (она сама не имеет продолжительности!) и говорит о том, что в
 * указанный момент времени {@link ISkAlarm#timestamp()} было некоторое сочетание условий (примеры: "превышено
 * напряжение боле чем на 20%", "слишком длинный анодный эффект", "пользователь вошел в систему в запрещенное время
 * 01:00-06:00" и т.п.). С момента создания тревога (как например и событие) не меняется. У тревоги нет собственного
 * "состояния", "статуса" или каких-либо других изменяемых свойств. Просто тревога говорит "тогда-то было то-то", и
 * все.</li>
 * <li>единственное назначение и смысл существования тревоги донести до человека - пользователя системы информацию о
 * том, что-то случилось, то есть, донести до человека информацию, которая содежится в тревоге {@link ISkAlarm}.
 * Информация может быть донесена множеством разных способов: GUI АРМа (статусная строка, список тревог/событий, панель
 * уведомлений, диалог и т.п.), звуковыми сообщениями (оператору за АРМом, громкие объявления в цехе и т.п.),
 * SMS-сообщениями, email-уведомлениями и др.</li>
 * </ul>
 * <p>
 * Перед тем как сделать обзор API службы тревог, следует отметить одно важное понятие, которое напрямую не отражено в
 * API, но необходимо для понимания работы с тревогами. Речь идет о "нитки извещения" (announce thread). <b>Нить (нитка)
 * извещения</b> - это последовательность действий, предпринимаемый каждым отдельным <b>модулем (способом) доставки</b>
 * тревоги до человека. Нитка извещения содержит действие по доставке сообщения человеку и (опционально) получения
 * подтверждения. Сразу после появления (создаия, генерации) тревоги разные модули могут начать несколько ниток
 * извещения. Например:
 * <ul>
 * <li>GUI АРМа выводит модальный диалог, и считает подтверждением доставки тревоги факт закрытия диалога;</li>
 * <li>GUI АРМа выводит тревогу в панели "обрати внимание" и держит там заданное время. Если пользователь щелкнул на
 * тревоге, считает тревогу доставленным, если тревогу убирается по тайм-ауту, считает, что оператора не было за АРМом;
 * </li>
 * <li>модель SMS отправляет сообщения нужным людям несколько раз, с заданным интервалом до получения подтверждения о
 * прочтении;</li>
 * <li>аналогично, модель электронной почти может сделать рассылки, также с возможным ожиданием подтверждения;</li>
 * <li>модуль голосового извещения ставит сообщения в очередь, и после фзического объявления считает сообщение
 * доставленным.</li>
 * </ul>
 * <p>
 * <p>
 * <h1><b>API службы тревог</b></h1>
 * <p>
 * <h2>Описание тревог {@link ISkAlarmDef}</h2>
 * <p>
 * Каждая конкретная тревога дожна быть создана по "шаблону", то есть по известному описанию {@link ISkAlarmDef}. Список
 * изветных описаний возвращается методом {@link #listAlarmDefs()}. Описание имеет идентификатор
 * {@link ISkAlarmDef#id()}, который назвается идентификатором типа тревоги. Описание по идентификатору типа можно
 * получить еще и методом {@link #findAlarmDef(String)}. Единственный способ сообщить системе о типе тревоги -
 * зарегистрировать описание методом {@link #registerAlarmDef(ISkAlarmDef)}.
 * <p>
 * <h2>Работа с тревогами в реальном времени</h2>
 * <p>
 * Любой клиент может создать (сгенерировать) тревогу известного типа методом
 * {@link #generateAlarm(String, Skid, Skid, byte, ISkAlarmFlacon)}. Появление тревоги отслеживается слушателем
 * {@link ISkAlarmServiceListener#onAlarm(ISkAlarm)}. Модели доставки, обнаружив появление нужной тревоги инициируют
 * нить извещения. По мере исполнения нити извещения, модули логируют этапы исполнения методом
 * {@link #addAnnounceThreadHistoryItem(long, ISkAnnounceThreadHistoryItem)}. Информацию о том, как проходил процесс
 * извещения по всем ниткам можно получить методом {@link #getAlarmHistory(long)}.
 * <p>
 * Информация о разнородных данных, сочетание которых привело к возникновеню тревоги упаковывается в один флакон
 * {@link ISkAlarmFlacon}, и сохраняется вместе с тревогой. Получить эту информацию можно методом
 * {@link #getAlarmFlacon(long)} .
 * <p>
 * <h2>Работа с историей тревог</h2>
 * <p>
 * История тревог включает в себя как получение тревог, которые были в определенном интервале времени методом
 * {@link #queryAlarms(ITimeInterval, ITsCombiFilterParams)}, так и получение истории извещения (доставки) каждой
 * тревоги методом {@link #getAlarmHistory(long)}.
 *
 * @author goga
 */
public interface ISkAlarmService
    extends ISkService {

  /**
   * Идентификатор службы.
   */
  String SERVICE_ID = SK_SYSEXT_SERVICE_ID_PREFIX + "AlarmService"; //$NON-NLS-1$

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

  /**
   * Возвращает средство работы с событиями от службы тревог.
   *
   * @return {@link ISkAlarmEventsFiringSupport} - средство работы с событиями от службы тревог
   */
  ISkAlarmEventsFiringSupport eventer();

}
