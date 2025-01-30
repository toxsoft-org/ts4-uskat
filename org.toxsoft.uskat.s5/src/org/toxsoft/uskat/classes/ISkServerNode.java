package org.toxsoft.uskat.classes;

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
}
