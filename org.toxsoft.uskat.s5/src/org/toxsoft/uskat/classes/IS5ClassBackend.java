package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

import ru.uskat.core.common.skobject.ISkObject;

/**
 * Класс s5: бекенд службы работающий в рамках узла кластера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ClassBackend
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = IS5ServerHardConstants.S5_ID_START + "Backend";

  /**
   * Идентификатор класса.
   */
  String CLASS_SERVER_BACKEND = CLASS_ID;

  // ------------------------------------------------------------------------------------
  // Связи
  //
  /**
   * Связь: узел кластера, в рамках которого работает бекенд {@link IS5ClassNode}.
   */
  String LNKID_NODE = "node";
}
