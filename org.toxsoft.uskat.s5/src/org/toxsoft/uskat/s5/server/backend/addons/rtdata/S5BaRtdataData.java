package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import java.io.*;
import java.util.concurrent.atomic.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.frontend.*;

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
   * Идентификаторы данных передаваемые в бекенд
   */
  public final GwidList currdataGwidsToBackend = new GwidList();

  /**
   * Значения текущих данных готовых для передачи в бекенд
   */
  transient public final IMapEdit<Gwid, IAtomicValue> currdataToBackend = new ElemMap<>();

  /**
   * Время последней передачи текущих данных в бекенд
   */
  transient public long lastCurrdataToBackendTime = System.currentTimeMillis();

  /**
   * Идентификаторы данных передавамые в фронтенд
   */
  public final GwidList currdataGwidsToFrontend = new GwidList();

  /**
   * Значения текущих данных готовых для передачи в фроненд
   * <p>
   * non-transient чтобы на сервере не требовалась инициализация
   */
  public final IMapEdit<Gwid, IAtomicValue> currdataToFrontend = new ElemMap<>();

  /**
   * Время последней передачи текущих данных в фронтенд
   * <p>
   * non-transient чтобы на сервере не требовалась инициализация
   */
  public long lastCurrdataToFrontendTime = System.currentTimeMillis();

  /**
   * Счетчик отправленных фронтенду пакетов текущих данных.
   */
  public AtomicInteger currdataEditionCounter = new AtomicInteger();

  /**
   * Счетчик отправленных бекенду пакетов текущих данных.
   */
  public AtomicInteger currdataToBackendCounter = new AtomicInteger();

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
  transient public final IMapEdit<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> histdataToBackend =
      new ElemMap<>();

  /**
   * Время последней передачи хранимых данных в бекенд
   */
  transient public long lastHistdataToBackendTime = System.currentTimeMillis();

}
