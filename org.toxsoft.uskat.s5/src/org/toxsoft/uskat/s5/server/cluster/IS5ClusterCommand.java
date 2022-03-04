package org.toxsoft.uskat.s5.server.cluster;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;

/**
 * Уведомление передаваемое узлам кластера
 *
 * @author mvk
 */
public interface IS5ClusterCommand {

  /**
   * Возвращает метод уведомления
   *
   * @return String имя метода уведомления
   */
  String method();

  /**
   * Возвращает карту параметров уведомления
   *
   * @return {@link IStringMap} карта параметров уведомления.<br>
   *         Ключ: строковый идентификатор параметра;<br>
   *         Значение: значение параметра в формате {@link ITjValue}
   */
  IStringMap<ITjValue> params();
}
