package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminResources.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда администрирования: принудительно разорвать соединение с клиентом
 * <p>
 * После разрыва связи клиент может произвести повторное подключение.
 * <p>
 * Команда может быть использована для отладки логики разрыва/восстановления соединений клиентов
 *
 * @author mvk
 */
public class AdminCmdDisconnect
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdDisconnect() {
    // Контекст: соединение с сервером
    addArg( CTX_SK_CONNECTION );
    // Идентификатор сессии или его часть(поиск совпадения)
    addArg( ARG_DISCONNECT_SESSION );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_DISCONNECT_ID;
  }

  @Override
  public String alias() {
    return CMD_DISCONNECT_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_DISCONNECT_NAME;
  }

  @Override
  public String description() {
    return CMD_DISCONNECT_DESCR;
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
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    String session = argSingleValue( ARG_DISCONNECT_SESSION ).asString();

    // Информация о сервере
    ISkBackendInfo backendInfo = connection.backendInfo();
    // Информация об открытых и завершенных сессиях пользователей
    S5SessionsInfos sessionInfos = OP_BACKEND_SESSIONS_INFOS.getValue( backendInfo.params() ).asValobj();
    for( IS5SessionInfo info : sessionInfos.openInfos() ) {
      if( sessionIDToString( info.sessionID() ).equals( session ) ) {
        connection.close();
        resultOk();
        return;
      }
    }
    addResultError( ERR_DISCONNECT_SESSION_NOT_FOUND, session );
    resultFail();
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return String строка представляющая сессию
   */
  private static String sessionIDToString( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }
}
