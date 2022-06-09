package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: 'вывести на экран время'
 *
 * @author mvk
 */
public class ConsoleCmdTimeToString
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdTimeToString( IAdminConsole aConsole ) {
    super( aConsole );
    // Метка (мсек с начала эпохи) времени
    addArg( ARG_TIME_ID, ARG_TIME_ALIAS, ARG_TIME_NAME, PT_SINGLE_INTEGER, ARG_TIME_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return TIME_TO_STRING_CMD_ID;
  }

  @Override
  public String alias() {
    return TIME_TO_STRING_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return TIME_TO_STRING_CMD_NAME;
  }

  @Override
  public String description() {
    return TIME_TO_STRING_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return PT_SINGLE_STRING;
  }

  @Override
  public String resultDescription() {
    return TIME_TO_STRING_RESULT_DESCR;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Аргументы команды
    long timestamp = argSingleValue( ARG_TIME_ID ).asLong();
    resultOk( pvsStr( TimeUtils.timestampToString( timestamp ) ) );
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}
