package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: сравнение двух параметров консоли
 *
 * @author mvk
 */
public class ConsoleCmdIsEquals
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdIsEquals( IAdminConsole aConsole ) {
    super( aConsole );
    // Аргументы
    // Идентификатор класса объекта
    addArg( IS_EQUAL_ARG_PAR1_ID, IS_EQUAL_ARG_PAR1_ALIAS, IS_EQUAL_ARG_PAR1_NAME, PT_SINGLE_STRING,
        IS_EQUAL_ARG_PAR1_DESCR );
    // Имя объекта класса
    addArg( IS_EQUAL_ARG_PAR2_ID, IS_EQUAL_ARG_PAR2_ALIAS, IS_EQUAL_ARG_PAR2_NAME, PT_SINGLE_STRING,
        IS_EQUAL_ARG_PAR2_DESCR );
    // @formatter:on
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return IS_EQUAL_CMD_ID;
  }

  @Override
  public String alias() {
    return IS_EQUAL_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return IS_EQUAL_CMD_NAME;
  }

  @Override
  public String description() {
    return IS_EQUAL_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return PT_SINGLE_BOOLEAN;
  }

  @Override
  public String resultDescription() {
    return IS_EQUAL_CMD_RESULT_DESCR;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Аргументы команды
    String argParam1 = argSingleValue( IS_EQUAL_ARG_PAR1_ID ).asString();
    String argParam2 = argSingleValue( IS_EQUAL_ARG_PAR2_ID ).asString();
    try {
      IPlexyValue val1 = contextParamValue( argParam1 );
      IPlexyValue val2 = contextParamValue( argParam2 );
      resultOk( pvsBool( val1.equals( val2 ) ) );
    }
    catch( RuntimeException e ) {
      addResultError( e );
      resultFail();
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
