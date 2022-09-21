package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.Pair;
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
   * Таймаут (мсек) передачи текущих данных в бекенд
   */
  public long currdataTimeout = IS5ConnectionParams.OP_CURRDATA_TIMEOUT.defaultValue().asLong();

  /**
   * Идентификаторы данных передаваемые в бекендт
   */
  public final GwidList currdataGwidsToBackend = new GwidList();

  /**
   * Значения текущих данных готовых для передачи в бекенд
   */
  public final IMapEdit<Gwid, IAtomicValue> currdataToBackend = new ElemMap<>();

  /**
   * Время последней передачи текущих данных в бекенд
   */
  public long lastCurrdataToBackendTime = System.currentTimeMillis();

  /**
   * Идентификаторы данных передавамые в фронтенд
   */
  public final GwidList currdataGwidsToFrontend = new GwidList();

  /**
   * Значения текущих данных готовых для передачи в фроненд
   */
  public final IMapEdit<Gwid, IAtomicValue> currdataToFrontend = new ElemMap<>();

  /**
   * Время последней передачи текущих данных в фронтенд
   */
  public long lastCurrdataToFrontendTime = System.currentTimeMillis();

  // ------------------------------------------------------------------------------------
  // Хранимые данные
  //
  /**
   * Таймаут (мсек) передачи хранимых данных в бекенд
   */
  public long histdataTimeout = IS5ConnectionParams.OP_HISTDATA_TIMEOUT.defaultValue().asLong();

  /**
   * Значения хранимых данных готовых для передачи
   */
  public final IMapEdit<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> histdataToBackend = new ElemMap<>();

  /**
   * Время последней передачи хранимых данных в бекенд
   */
  public long lastHistdataToBackendTime = System.currentTimeMillis();

}
