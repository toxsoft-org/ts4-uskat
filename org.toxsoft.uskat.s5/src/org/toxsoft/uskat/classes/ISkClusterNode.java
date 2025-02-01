package org.toxsoft.uskat.classes;

import org.toxsoft.uskat.core.*;

/**
 * Класс: узел кластера сервера.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface ISkClusterNode
    extends ISkNetNode {

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
