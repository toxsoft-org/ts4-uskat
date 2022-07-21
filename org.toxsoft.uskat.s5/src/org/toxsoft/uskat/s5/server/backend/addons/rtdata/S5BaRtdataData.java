package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import java.io.Serializable;

import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaRtdata}.
 *
 * @author mvk
 */
public class S5BaRtdataData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификаторы данных читаемых фронтендом
   */
  public final GwidList readRtdGwids = new GwidList();

  /**
   * Идентификаторы данных записываемые фронтендом
   */
  public final GwidList writeRtdGwids = new GwidList();

}
