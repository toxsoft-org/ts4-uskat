package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: 'Завершение работы с сохранением настроек консоли'
 *
 * @author mvk
 */
public class ConsoleCmdExit
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdExit( IAdminConsole aConsole ) {
    super( aConsole );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return EXIT_CMD_ID;
  }

  @Override
  public String alias() {
    return EXIT_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return EXIT_CMD_NAME;
  }

  @Override
  public String description() {
    return EXIT_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Сохранение контекста выполнения команд в файле
    // TODO: сделать команду сохранение параметров контекста
    // writeContextToFile( CONTEXT_FILENAME, getConsole().context() );
    // Завершение работы
    getConsole().close();
    resultOk();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgs ) {
    return IList.EMPTY;
  }
}
