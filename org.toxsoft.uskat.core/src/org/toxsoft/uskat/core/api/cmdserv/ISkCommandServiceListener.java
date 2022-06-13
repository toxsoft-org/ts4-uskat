package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * Отслеживатель изменения состояния команды.
 *
 * @author goga
 */
public interface ISkCommandServiceListener {

  /**
   * Вызывается при изменении состояния команды.
   * <p>
   * Обратите внимание, что метод вызвается при созданиий (отправке) команды методом
   * {@link ISkCommandService#sendCommand(Gwid, Skid, IOptionSet)}, считая, что состояние команды изменилось с "не
   * существующего" на {@link ESkCommandState#SENDING}.
   *
   * @param aCommand {@link ISkCommand} - команда, состояние которого изменилось
   */
  void onCommandStateChanged( ISkCommand aCommand );

  /**
   * Вызывается при измненении списка {@link Gwid}-в, для которых служба команд является исполнителем.
   *
   * @param aExecutableCommandGwids {@link IGwidList} - то же, что и
   *          {@link ISkCommandService#getExcutableCommandGwids()}
   */
  void onExecutableCommandGwidsChanged( IGwidList aExecutableCommandGwids );

}
