package org.toxsoft.uskat.classes;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Класс: сетевой узел.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkNetNode
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".NetNode";

  // ------------------------------------------------------------------------------------
  // Связи
  //
  /**
   * Связь: список дочерних сетевых узлов подключенных к узлу.
   */
  String LNKID_CHILDS = "childs";

  // -----------------------------------------------------------------------------------
  // Данные
  //
  //
  /**
   * Данное: с сетевым узлом установлена связь.
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  String RTDID_ONLINE = "online";

  /**
   * Данное: интегральная оценка состояния подключенных узлов. 0 - нет связи, 100 - все подключено и работает.
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  String RTDID_HEALTH = "health";
}
