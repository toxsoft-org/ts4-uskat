package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Команда.
 * <p>
 * Жизненный цикл (ЖЦ) команда начинается с момента создания методом
 * {@link ISkCommandService#sendCommand(Gwid, Skid, IOptionSet)} и завершается после перехода в терминальное состояние,
 * Смотрите описание ЖЦ в комментарии к {@link ESkCommandState}. После завершения жизненного цикла команды она
 * сохраняетя в истории, откуда его можно получить методом {@link ISkCommandService#history()}.
 * <p>
 * Ссылка на экземпляр этого интерфейса, пока команда не завершена, сущствует в динственном экземпляре, полученном
 * методом {@link ISkCommandService#sendCommand(Gwid, Skid, IOptionSet)}. И эта ссылка является "живой", то есть, его
 * текущее состояние состояние {@link #state()} и история {@link #statesHistory()} обновляется в реальном времени. Таким
 * образом, изменение состояния команды можно отслуживать как подпиской на извещение {@link ISkCommandServiceListener},
 * так и периодическим опросом состояния команды методом {@link #state()}.
 *
 * @see ESkCommandState
 * @author hazard157
 */
public interface ISkCommand
    extends ITemporal<ISkCommand> {

  /**
   * Returns an unique in system command instance identifier.
   * <p>
   * This ID is unique among all command of all time in the particular system.
   *
   * @return String - identifier (an IDpath) of the command instance
   */
  String id();

  /**
   * Returns the command concrete GWID including the destination object skid and command ID.
   *
   * @return String - the event concrete GWID of kind {@link EGwidKind#GW_CMD}
   */
  Gwid cmdGwid();

  /**
   * Returns command author object identifier.
   *
   * @return {@link Skid} - the command author object
   */
  Skid authorSkid();

  /**
   * Returns the command arguments values.
   *
   * @return {@link IOptionSet} - the command arguments values
   */
  IOptionSet argValues();

  /**
   * Возвращает историю изменения состояния команды.
   *
   * @return {@link TimedList}&lt;{@link SkCommandState}&gt; - упорядоченный по времени список изменений
   */
  ITimedList<SkCommandState> statesHistory();

  /**
   * Returns the last state of this command.
   *
   * @return {@link ESkCommandState} - last state
   */
  default SkCommandState state() {
    return statesHistory().last();
  }

}
