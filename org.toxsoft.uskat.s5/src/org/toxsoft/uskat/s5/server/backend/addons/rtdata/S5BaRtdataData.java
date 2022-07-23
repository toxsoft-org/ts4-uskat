package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaRtdata}.
 *
 * @author mvk
 */
public class S5BaRtdataData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  // ------------------------------------------------------------------------------------
  // Текущие данные
  //
  /**
   * Идентификаторы данных читаемых фронтендом
   */
  public final GwidList readCurrDataGwids = new GwidList();

  /**
   * Значения текущих данных готовых для передачи
   */
  public final IMapEdit<Gwid, IAtomicValue> currDataToSend = new ElemMap<>();

  /**
   * Таймаут (мсек) передачи текущих данных в бекенд
   */
  public long currDataToSendTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.defaultValue().asLong();

  /**
   * Время последней передачи текущих данных в бекенд
   */
  public long lastCurrDataToSendTime = System.currentTimeMillis();

  /**
   * Идентификаторы данных записываемые фронтендом
   */
  public final GwidList writeCurrDataGwids = new GwidList();

  // ------------------------------------------------------------------------------------
  // Хранимые данные
  //
  /**
   * Таймаут (мсек) передачи хранимых данных в бекенд
   */
  public long histDataToSendTimeout = IS5ConnectionParams.OP_HISTDATA_TIMEOUT.defaultValue().asLong();

  /**
   * Время последней передачи хранимых данных в бекенд
   */
  public long lastHistDataToSendTime = System.currentTimeMillis();

}
