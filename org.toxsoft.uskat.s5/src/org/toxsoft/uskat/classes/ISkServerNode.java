package org.toxsoft.uskat.classes;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Класс s5: узел кластера сервера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkServerNode
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".ServerNode";

  // ------------------------------------------------------------------------------------
  // Связи
  //
  /**
   * Связь: сервер/кластер, в рамках которого работает узел {@link ISkServer}.
   */
  String LNKID_SERVER = "server";

  // -----------------------------------------------------------------------------------
  // Данные
  //
  //
  /**
   * Данное: с узлом сервера установлена связь.
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  String RTDID_ONLINE = "online";

  /**
   * Данное: интегральная оценка состояния подключенных к узлу ресурсов. 0 - нет связи, 100 - все подключено и работает.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_HEALTH = "health";
}
