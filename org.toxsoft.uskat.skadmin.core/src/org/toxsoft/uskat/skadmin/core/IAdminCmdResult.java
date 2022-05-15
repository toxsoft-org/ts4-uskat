package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;

/**
 * Результат выполнения команды {@link IAdminCmdDef}.
 *
 * @author goga
 */
public interface IAdminCmdResult {

  /**
   * Определяет, успешно ли была завершена команда.
   * <p>
   * При успешном завершении команды возможны предупреждения и информационные сообщения, которые находятся в списке
   * {@link #validations()}.
   *
   * @return boolean - признак успешного выполнения команды
   */
  boolean isOk();

  /**
   * Значение результата выполнения команды
   *
   * @return Значение результата. Для команд без результата возвращается {@link IPlexyValue#NULL}
   * @throws TsIllegalStateRtException результат не был сформирован из-за ошибки
   */
  IPlexyValue result();

  /**
   * Возвращает сообщения - пояснения к результату выполнения команды.
   *
   * @return IList&lt;{@link ValidationResult}&gt; - список сообщений
   */
  IList<ValidationResult> validations();

}
