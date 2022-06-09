package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.cli.cmds.ConsoleCmdSignal.ESignalCmd;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: 'Остановить выполнение потока на указанное время'
 *
 * @author mvk
 */
public class ConsoleCmdTimeout
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdTimeout( IAdminConsole aConsole ) {
    super( aConsole );
    // Таймаут(мсек) удержания или ожидания сигнала или его значения
    addArg( ARG_TIMEOUT_VALUE_ID, ARG_TIMEOUT_VALUE_ALIAS, ARG_TIMEOUT_VALUE_NAME,
        createType( INTEGER, ARG_TIMEOUT_VALUE_DEFAULT ), ARG_TIMEOUT_VALUE_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return TIMEOUT_CMD_ID;
  }

  @Override
  public String alias() {
    return TIMEOUT_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return TIMEOUT_CMD_NAME;
  }

  @Override
  public String description() {
    return TIMEOUT_CMD_DESCR;
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
    // Аргументы команды
    long timeout = argSingleValue( ARG_TIMEOUT_VALUE_ID ).asInt();
    try {
      Thread.sleep( timeout );
      resultOk();
    }
    catch( Exception e ) {
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( ARG_SIGNAL_CMD_ID ) ) {
      IListEdit<IPlexyValue> values = new ElemArrayList<>( ESignalCmd.values().length );
      for( int index = 0, n = ESignalCmd.values().length; index < n; index++ ) {
        values.add( pvsStr( ESignalCmd.values()[index] ) );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}
