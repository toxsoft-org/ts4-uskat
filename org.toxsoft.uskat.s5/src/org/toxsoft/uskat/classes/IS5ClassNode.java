package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Класс s5: узел кластера сервера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ClassNode
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = IS5ServerHardConstants.S5_ID_START + "Node";

  /**
   * Идентификатор класса.
   */
  String CLASS_NODE = CLASS_ID;

  // ------------------------------------------------------------------------------------
  // Связи
  //
  /**
   * Связь: сервер/кластер, в рамках которого работает узел {@link IS5ClassServer}.
   */
  String LNKID_SERVER = "server";

  // -----------------------------------------------------------------------------------
  // Данные
  //
  //
}
