package org.toxsoft.uskat.sysext.realtime.addon;

import java.io.Serializable;

import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionAddonInitData;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.core.api.cmds.ISkCommandExecutor;
import ru.uskat.core.api.cmds.ISkCommandService;

/**
 * Данные для инициализации расширения backend {@link ISkBackendAddonRealtime}.
 *
 * @author mvk
 */
public final class S5RealtimeInitData
    implements IS5SessionAddonInitData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификаторы текущих данных чтения на которые подписан клиент
   * <p>
   * Определение идентификаторов смотри {@link ISkBackendAddonRealtime#configureCurrDataReader(IGwidList, IGwidList)}
   */
  public final GwidList readCurrdataGwids = new GwidList();

  /**
   * Идентификаторы текущих данных записи на которые подписан клиент
   * <p>
   * Определение идентификаторов смотри {@link ISkBackendAddonRealtime#configureCurrDataWriter(IGwidList, IGwidList)}
   */
  public final GwidList writeCurrdataGwids = new GwidList();

  /**
   * Идентификаторы команд которые может выполнять клиент
   * <p>
   * Определение идентификаторов смотри {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)}
   */
  public final GwidList commandsGwids = new GwidList();

}
