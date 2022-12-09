package org.toxsoft.uskat.alarms.lib;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.core.api.users.ISkUser;

/**
 * Тревога.
 * <p>
 * TODO описать службу тревог и применение тревоги
 *
 * @author goga
 */
public interface ISkAlarm
    extends ITemporal<ISkAlarm> {

  /**
   * Возвращает уникальный в системе идентификатор тервоги.
   *
   * @return long - идентификатор тревоги
   */
  long alarmId();

  /**
   * Возвращает приоритет тревоги.
   * <p>
   * Внутри заданного приоритета можно еще уточнить степень важности {@link #sublevel()}.
   *
   * @return {@link EAlarmPriority} - приоритет (важность) тревоги
   */
  EAlarmPriority priority();

  /**
   * Возвращает уточнение важности тревоги.
   * <p>
   * Числовое значение уточнения важности конкретной тревоги {@link ISkAlarm#sublevel()} (в пределах -127...+128) можно
   * сложить с базовым значением {@link EAlarmPriority#sublevelBase()} чтобы получить числовое значение приоритета.
   * Числовое значение приоритета принимает значения от 0 (малозначительное инфомационное сообщение) до 1280 (самая
   * критическая тревога).
   * <p>
   * Меньшие значения соответствуют понижению, а большие значения - повышению важности тревоги внутри заданного
   * приоритета {@link #priority()}.
   *
   * @return byte - уточнение важности тревоги в пределах -127...+128
   */
  byte sublevel();

  /**
   * Возвращает числовое значение уровеня важности (приоритета) тревоги.
   * <p>
   * Возвращаемое значение - просто сумма {@link EAlarmPriority#sublevelBase()} + {@link #sublevel()}.
   *
   * @return int - уровень важности (0..1280)
   */
  int level();

  /**
   * Возвращает идентификатор объекта - автора, сгенерировавшего тревогу.
   *
   * @return {@link Skid} - идентификатор объекта автора
   */
  Skid authorId();

  /**
   * Возвращает идентификатор объекта пользователя, от имени которого была создана тревога.
   * <p>
   * Возвращает идентификатор того {@link ISkUser#skid()} объекта, от имени котого был вызван метод
   * {@link ISkAlarmService#generateAlarm(String, Skid, Skid, byte, ISkAlarmFlacon)}.
   * <p>
   * Это поле автоматически заполяется службой тревог в теле метода
   * {@link ISkAlarmService#generateAlarm(String, Skid, Skid, byte, ISkAlarmFlacon)}.
   *
   * @return {@link Skid} - идентификатор пользователя
   */
  Skid userId();

  /**
   * Возвращает идентификатор типа тревоги {@link ISkAlarmDef#id()}.
   *
   * @return String - идентификатор тип этой тревоги
   */
  String alarmDefId();

  /**
   * Возвращает сообщение, отображаемый текст тревоги.
   *
   * @return String - текст тревоги
   */
  String message();

  /**
   * Возвращает срез данных, вызвавший тревогу.
   *
   * @return {@link ISkAlarmFlacon} - срез данных
   * @throws TsItemNotFoundRtException нет тревоги с таким идентификатором
   */
  ISkAlarmFlacon flacon();

  /**
   * Возвращает историю отображения и обработки (квитирования) тревоги.
   * <p>
   * В зависимости о параметров в описании {@link ISkAlarmDef}, история может не вестись, и будет возвращен пустой
   * список.
   *
   * @return {@link ITimedList}&lt;{@link ISkAlarmThreadHistoryItem}&gt; - список собйтий обработки тревоги
   */
  ITimedList<ISkAlarmThreadHistoryItem> history();

}
