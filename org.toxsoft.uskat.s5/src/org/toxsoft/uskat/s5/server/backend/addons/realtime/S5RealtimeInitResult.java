package org.toxsoft.uskat.s5.server.backend.addons.realtime;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.primtypes.IIntMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionAddonInitResult;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Данные результата инициализации расширения backend {@link ISkBackendAddonRealtime}.
 *
 * @author mvk
 */
public final class S5RealtimeInitResult
    implements IS5SessionAddonInitResult, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Карта набора чтения текущих данных
   * <p>
   * Ключ: индекс данного в системе;<br>
   * Значение: идентификатор данного.
   */
  public final IIntMapEdit<Gwid> readCurrdataDataset = new IntMap<>();

  /**
   * Карта набора записи текущих данных
   * <p>
   * Ключ: индекс данного в системе;<br>
   * Значение: идентификатор данного.
   */
  public final IIntMapEdit<Gwid> writeCurrdataDataset = new IntMap<>();
}
