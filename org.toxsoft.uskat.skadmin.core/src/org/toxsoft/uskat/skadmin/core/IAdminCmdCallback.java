package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;

/**
 * Интерфейс обратного вызова для наблюдения за процессом выполнения команды {@link IAdminCmdDef}.
 * <p>
 * Если пользователь желает огранизовать показ прогресс-диалога или точек в консоли, то начинать показ надо в методе
 * {@link #beforeStart(IList, long, boolean)}, а не сразу после начала выполнения команды методом
 * {@link IAdminCmdLibrary#exec(String, IStringMap, IAdminCmdCallback)} . Дело в том, что сначала производятся проверки
 * некоторых предусловии, и метод {@link IAdminCmdLibrary#exec(String, IStringMap, IAdminCmdCallback)} может немедленно
 * завершится, не успепв вызвать ни один метод этого интерфейса. Также, реализация может проследить, что если задача
 * выполяется с точки зрения человека мгновенно (например, менее 0,2 секунды), вообще не вызывать методы этого
 * интерфейса.
 * <p>
 * После вызова {@link #beforeStart(IList, long, boolean)} будет вызыватся метод
 * {@link #onNextStep(IList, long, long, boolean)} (0 или более раз), и обязательно - метод
 * {@link #afterEnd(IAdminCmdResult)}.
 *
 * @author hazard157
 */
public interface IAdminCmdCallback {

  /**
   * Пустой callback
   */
  IAdminCmdCallback NULL = new NullCallback();

  /**
   * Запрос дополнительного значения для выполнения команды
   *
   * @param aType {@link IPlexyType} - тип значения команды
   * @param aPossibleValues {@link IList}&lt; {@link IAtomicValue}&gt; - список возможных единичных значений. Пустой
   *          список: значения не ограничены
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt - сообщения поясняющие смысл запроса
   * @return {@link IPlexyValue} значение. {@link IPlexyValue#NULL} - клиент не может представить значение
   */
  IPlexyValue getValue( IPlexyType aType, IList<IPlexyValue> aPossibleValues, IList<ValidationResult> aMessages );

  /**
   * Вызывается перед началом выполнения задачи.
   * <p>
   * Если в начале невозможно прикинуть кол-во шагов (aStepsCount=0), то в дальнейшем, ситуация может стать более
   * определенной и надо следить за аргументами метода {@link #onNextStep(IList, long, long, boolean)}.
   *
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt - сообщения поясняющие состояние перед выполнением
   *          команды
   * @param aStepsCount long - ожидаемое количество шагов выполнения (0 означает, что количество шагов непредсказуемо)
   * @param aStartDefault boolean <b>true</b> задача предлагает начать выполнение; <b>false</b> задача предлагает
   *          отменить выполнение
   * @return boolean - реализация должна вернуть false, чтобы предотвратить начало процесса генерации кода.
   */
  boolean beforeStart( IList<ValidationResult> aMessages, long aStepsCount, boolean aStartDefault );

  /**
   * Вызывается каждый раз, <b>перед</b> началом выполнения очередного шага.
   *
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt - сообщения поясняющие текущее состояние выполнения
   *          команды
   * @param aCurrStep long - номер текущего шага (в пределах 1..aStepsCount)
   * @param aStepsCount long - ожидаемое количество шагов выполнения (0 означает, что количество шагов непредсказуемо)
   * @param aCancelable boolean <b>true</b> операция может быть остановлена если реализация метода вернет true;
   *          <b>false</b> операция в данный момент не может быть остановлена, но задача может учесть требование на
   *          остановку согласно своей логике обработки.
   * @return boolean - реализация должна вернуть false, чтобы прекратить выполнение задачи. При формировании результата
   *         реализация должна учитываеть параметр aCancelable
   */
  boolean onNextStep( IList<ValidationResult> aMessages, long aCurrStep, long aStepsCount, boolean aCancelable );

  /**
   * Вызывается после завершения .
   * <p>
   * Этот метод обязательно вызыватеся только после вызова {@link #beforeStart(IList, long, boolean)}. Если
   * {@link IAdminCmdLibrary#exec(String, IStringMap, IAdminCmdCallback)} завершит работу до вызова
   * {@link #beforeStart(IList, long, boolean)}, то и этот метод не будет вызван.
   *
   * @param aResults {@link IAdminCmdResult} - те же результаты, которые возвращает
   *          {@link IAdminCmdLibrary#exec(String, IStringMap, IAdminCmdCallback)}
   */
  void afterEnd( IAdminCmdResult aResults );

  /**
   * Пустой callback
   */
  class NullCallback
      implements IAdminCmdCallback {

    @Override
    public IPlexyValue getValue( IPlexyType aValueType, IList<IPlexyValue> aPossibleValues,
        IList<ValidationResult> aMessages ) {
      return IPlexyValue.NULL;
    }

    @Override
    public boolean beforeStart( IList<ValidationResult> aMessages, long aStepsCount, boolean aStartDefault ) {
      return aStartDefault;
    }

    @Override
    public boolean onNextStep( IList<ValidationResult> aMessages, long aCurrStep, long aStepsCount,
        boolean aCancelable ) {
      return true;
    }

    @Override
    public void afterEnd( IAdminCmdResult aResults ) {
      // nop
    }
  }
}
