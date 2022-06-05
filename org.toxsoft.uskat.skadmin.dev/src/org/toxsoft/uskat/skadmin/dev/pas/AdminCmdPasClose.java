package org.toxsoft.uskat.skadmin.dev.pas;

import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardResources.*;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: завершение работы с PAS-сервером (Public Access Server)
 *
 * @author mvk
 */
public class AdminCmdPasClose
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdPasClose() {
    // Контекст: PAS-клиент
    addArg( CTX_PAS_CLIENT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_PAS_CLOSE_ID;
  }

  @Override
  public String alias() {
    return CMD_PAS_CLOSE_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_PAS_CLOSE_NAME;
  }

  @Override
  public String description() {
    return CMD_PAS_CLOSE_DESCR;
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
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    try {
      PasClient<?> pasClient = argSingleRef( CTX_PAS_CLIENT );
      long startTime = System.currentTimeMillis();
      pasClient.close();
      long delta = (System.currentTimeMillis() - startTime) / 1000;
      addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
      resultOk();
    }
    catch( Throwable e ) {
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
