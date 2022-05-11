package org.toxsoft.uskat.sysext.alarms.api;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmAtomicValuePredicate;
import org.toxsoft.uskat.sysext.alarms.api.generator.ISkAlarmProfile;

/**
 * Генератор алармов
 *
 * @author mvk
 */
public interface ISkAlarmGenerator
    extends ICooperativeMultiTaskable, ICloseable {

  /**
   * Добавить аларм для генерации
   *
   * @param aAuthorId {@link Skid} идентификтор объекта автора аларма
   * @param aSkAlarmDef {@link ISkAlarmDef} описание аларма
   * @param aPredicate {@link Predicate}&lt;{@link ISkAlarmProfile}&gt; условие формирования аларма
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException нет поставщика данных для проверки возникновения аларма
   * @throws TsIllegalArgumentRtException описания аларма нет в системе, генератору запрещено автоматически проводить
   *           регистрацию
   * @throws TsIllegalArgumentRtException попытка добавить аларм для генерации несколько раз
   */
  void addAlarm( Skid aAuthorId, ISkAlarmDef aSkAlarmDef, Predicate<ISkAlarmProfile> aPredicate );

  /**
   * Добавить аларм для генерации
   *
   * @param aAlarmId String идентификатор аларма
   * @param aAlarmPriority {@link EAlarmPriority} приоритет аларма
   * @param aMessage String сообщения для аларма
   * @param aObjId {@link Skid} идентификатор объекта для чтения текущего данного. Он же автор аларма
   * @param aDataId String идентификатор данного формирующего аларм
   * @param aValuePredicate {@link ISkAlarmAtomicValuePredicate} условие на значения для формирования аларма
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addAlarm( String aAlarmId, EAlarmPriority aAlarmPriority, String aMessage, Skid aObjId, String aDataId,
      ISkAlarmAtomicValuePredicate aValuePredicate );
}
