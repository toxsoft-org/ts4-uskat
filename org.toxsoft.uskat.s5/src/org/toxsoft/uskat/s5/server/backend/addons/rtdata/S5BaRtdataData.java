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
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.frontend.*;

/**
 * Данные конфигурации фронтенда для {@link IBaRtdata}.
 *
 * @author mvk
 */
public final class S5BaRtdataData
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
  private final GwidList currdataGwidsToBackend = new GwidList();

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
  private final GwidList currdataGwidsToFrontend = new GwidList();

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

  /**
   * Журнал работы.
   */
  transient private ILogger logger;

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

  // ------------------------------------------------------------------------------------
  // public API
  //
  public IGwidList currdataGwidsToBackend() {
    return currdataGwidsToBackend;
  }

  public void configureCurrdataGwidsToBackend( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove == null ) {
      currdataGwidsToBackend.clear();
    }
    if( aToRemove != null ) {
      for( Gwid g : aToRemove ) {
        currdataGwidsToBackend.remove( g );
      }
    }
    for( Gwid g : aToAdd ) {
      if( !currdataGwidsToBackend.hasElem( g ) ) {
        currdataGwidsToBackend.add( g );
      }
    }
    if( aToRemove == null || aToRemove.size() > 0 || aToAdd.size() > 0 ) {
      logger().info( "configureCurrdataGwidsToBackend(...): aToRemove = %s, aToAdd = %s, toBackend = %s", aToRemove,
          aToAdd, currdataGwidsToBackend );
    }
  }

  public IGwidList currdataGwidsToFrontend() {
    return currdataGwidsToFrontend;
  }

  public void configureCurrdataGwidsToFrontend( IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNull( aToAdd );
    if( aToRemove == null ) {
      currdataGwidsToFrontend.clear();
    }
    if( aToRemove != null ) {
      for( Gwid g : aToRemove ) {
        currdataGwidsToFrontend.remove( g );
      }
    }
    for( Gwid g : aToAdd ) {
      if( !currdataGwidsToFrontend.hasElem( g ) ) {
        currdataGwidsToFrontend.add( g );
      }
    }
    if( aToRemove == null || aToRemove.size() > 0 || aToAdd.size() > 0 ) {
      logger().info( "configureCurrdataGwidsToBackend(...): aToRemove = %s, aToAdd = %s, toFrontend = %s", aToRemove,
          aToAdd, currdataGwidsToFrontend );
    }
  }

  // ------------------------------------------------------------------------------------
  // private API
  //
  private ILogger logger() {
    if( logger == null ) {
      logger = LoggerUtils.getLogger( getClass() );
    }
    return logger;
  }
}
