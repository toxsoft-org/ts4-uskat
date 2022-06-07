package org.toxsoft.uskat.skadmin.cli.cmds;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Абстрактная реализация команды встроенной в консоль
 *
 * @author mvk
 */
public abstract class AbstractConsoleCmd
    extends AbstractAdminCmd {

  private final IAdminConsole console;

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} - консоль
   * @throws TsNullArgumentRtException аргумент = null
   */
  public AbstractConsoleCmd( IAdminConsole aConsole ) {
    TsNullArgumentRtException.checkNull( aConsole );
    console = aConsole;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает консоль в которой выполняется команда
   *
   * @return {@link IAdminConsole} - консоль команд
   */
  protected IAdminConsole getConsole() {
    return console;
  }

}
