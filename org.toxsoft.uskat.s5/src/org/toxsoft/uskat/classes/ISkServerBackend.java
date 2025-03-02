package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Класс: бекенд службы работающий в рамках узла кластера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkServerBackend
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".ServerBackend";

  // ------------------------------------------------------------------------------------
  // Связи
  //
  /**
   * Связь: узел кластера, в рамках которого работает бекенд {@link ISkServerNode}.
   */
  String LNKID_NODE = "lnkNode";
}
