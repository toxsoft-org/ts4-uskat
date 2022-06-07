package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: 'Возвращает признак того, что указанный параметр существует в текущем контексте'
 *
 * @author mvk
 */
public class ConsoleCmdHasParam
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdHasParam( IAdminConsole aConsole ) {
    super( aConsole );
    // Раздел по которому требуется вывести информацию. Пустая строка: вывод по текущему разделу
    addArg( HAS_ARG_NAME_ID, HAS_ARG_NAME_ALIAS, HAS_ARG_NAME_NAME, PT_SINGLE_STRING, HAS_ARG_NAME_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return HAS_CMD_ID;
  }

  @Override
  public String alias() {
    return HAS_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return HAS_CMD_NAME;
  }

  @Override
  public String description() {
    return HAS_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return PT_SINGLE_BOOLEAN;
  }

  @Override
  public String resultDescription() {
    return HAS_CMD_RESULT_DESCR;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Раздел отображаемых команд
    String name = argSingleValue( HAS_ARG_NAME_ID ).asString();
    resultOk( pvsBool( contextParamValueOrNull( name ) != null ) );
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }
}
