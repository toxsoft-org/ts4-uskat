package org.toxsoft.uskat.alarms.s5.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.EAlarmPriority;
import org.toxsoft.uskat.alarms.lib.ISkAlarmDef;

/**
 * Генератор алармов
 *
 * @author mvk
 */
public interface IS5AlarmGenerator
    extends ICooperativeMultiTaskable, ICloseable {

  /**
   * Добавить аларм для генерации
   *
   * @param aAuthorId {@link Skid} идентификтор объекта автора аларма
   * @param aSkAlarmDef {@link ISkAlarmDef} описание аларма
   * @param aPredicate {@link Predicate}&lt;{@link IS5AlarmProfile}&gt; условие формирования аларма
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException нет поставщика данных для проверки возникновения аларма
   * @throws TsIllegalArgumentRtException описания аларма нет в системе, генератору запрещено автоматически проводить
   *           регистрацию
   * @throws TsIllegalArgumentRtException попытка добавить аларм для генерации несколько раз
   */
  void addAlarm( Skid aAuthorId, ISkAlarmDef aSkAlarmDef, Predicate<IS5AlarmProfile> aPredicate );

  /**
   * Добавить аларм для генерации
   *
   * @param aAlarmId String идентификатор аларма
   * @param aAlarmPriority {@link EAlarmPriority} приоритет аларма
   * @param aMessage String сообщения для аларма
   * @param aObjId {@link Skid} идентификатор объекта для чтения текущего данного. Он же автор аларма
   * @param aDataId String идентификатор данного формирующего аларм
   * @param aValuePredicate {@link IS5AlarmAtomicValuePredicate} условие на значения для формирования аларма
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addAlarm( String aAlarmId, EAlarmPriority aAlarmPriority, String aMessage, Skid aObjId, String aDataId,
      IS5AlarmAtomicValuePredicate aValuePredicate );

}
