package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Интерфейс исполнителя команд, которому сервис отдает команду на выполнение.
 * <p>
 * Исполнители должны регистрироваться в сервисе команд методом
 * {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)}.
 * <p>
 * Каждый исполнитель отвечает за свой набор объектов и команд, которые он готов выполнить. В нормальной системе,
 * зарегистрированные исполнители должны охватываеть возможность выполнения всех команд для всех объектов.
 *
 * @author goga
 * @author mvk
 */
public interface ISkCommandExecutor {

  /**
   * Выполняет команду, в основном же передает команду туда, где она будет физически исполнена.
   * <p>
   * Метод возвращает управление немедленно.
   *
   * @param aCmd {@link IDtoCommand} - команда на выполнение
   * @throws TsNullArgumentRtException аргумент = null
   */
  void executeCommand( IDtoCommand aCmd );

}
