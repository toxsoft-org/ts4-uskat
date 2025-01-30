package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Класс s5: сервер/кластер.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkServer
    extends ISkObject {

  /**
   * Идентификатор класса.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".Server";

}
