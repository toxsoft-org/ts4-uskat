package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

import ru.uskat.core.common.skobject.ISkObject;

/**
 * Класс s5: сервер/кластер.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ClassServer
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = IS5ServerHardConstants.S5_ID_START + "Server";

  /**
   * Идентификатор класса.
   */
  String CLASS_SERVER = CLASS_ID;

}
