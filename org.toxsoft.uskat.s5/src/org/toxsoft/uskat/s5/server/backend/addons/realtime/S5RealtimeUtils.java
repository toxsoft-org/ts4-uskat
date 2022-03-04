package org.toxsoft.uskat.s5.server.backend.addons.realtime;

import static org.toxsoft.uskat.s5.server.backend.addons.realtime.IS5Resources.*;

import java.util.Map;

import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;
import ru.uskat.common.dpu.rt.events.DpuWriteHistData;
import ru.uskat.common.dpu.rt.events.DpuWriteHistDataValues;

/**
 * Вспомогательные методы
 *
 * @author mvk
 */
public class S5RealtimeUtils {

  /**
   * Префикс идентификаторов JSON-уведомления {@link IJSONNotification#method()} используемый для передачи запросов
   * backend s5-сессиям
   */
  public static final String REALTIME_METHOD_PREFIX = "ru.toxsoft.realtime."; //$NON-NLS-1$

  /**
   * Возвращает данные фронтенда "реальное время"
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд
   * @return {@link S5RealtimeFrontendData} данные фронтенда. null: данные не существуют
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static S5RealtimeFrontendData getRealtimeFrontendData( IS5FrontendRear aFrontend ) {
    return aFrontend.frontendData().getAddonData( ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID,
        S5RealtimeFrontendData.class );
  }

  /**
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aMessage String начальная строка
   * @param aDataIds {@link Map}{@link Gwid},&lt;Integer&gt;
   *          <p>
   *          Ключ: {@link Gwid}-идентификатор текущих данных;<br>
   *          Значение: {@link Integer} целочисленный индекс данного в контейнере текущих данных.
   * @param aDataIndexes {@link Map}&lt;Integer,{@link Gwid}&gt; карта индексов данных.
   *          <p>
   *          Ключ: {@link Integer} целочисленный индекс данного в контейнере текущих данных;<br>
   *          Значение: {@link Gwid}-идентификатор текущих данных
   * @param aValues {@link Map} карта значений.
   *          <p>
   *          Ключ: {@link Integer} целочисленный индекс текущего данного;<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @return String строка представления значений текущих данных
   */
  public static String toStr( String aMessage, Map<Gwid, Integer> aDataIds, Map<Integer, Gwid> aDataIndexes,
      Map<Integer, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aDataIds, aDataIndexes, aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ) ) );
    for( Integer index : aValues.keySet() ) {
      Gwid gwid = getGwid( index, aDataIds, aDataIndexes );
      if( gwid != null ) {
        IAtomicValue value = aValues.get( index );
        sb.append( String.format( MSG_CURRDATA_VALUE, gwid, value ) );
      }
    }
    return sb.toString();
  }

  /**
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aIndex {@link Integer} индекс в наборе данных
   * @param aDataIds {@link Map}{@link Gwid},&lt;Integer&gt;
   *          <p>
   *          Ключ: {@link Gwid}-идентификатор текущих данных;<br>
   *          Значение: {@link Integer} целочисленный индекс данного в контейнере текущих данных.
   * @param aDataIndexes {@link Map}&lt;Integer,{@link Gwid}&gt; карта индексов данных.
   *          <p>
   *          Ключ: {@link Integer} целочисленный индекс данного в контейнере текущих данных;<br>
   *          Значение: {@link Gwid}-идентификатор текущих данных
   * @return String строка представления значений текущих данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException не найдено текущее данное с указанным индексом
   */
  public static Gwid getGwid( Integer aIndex, Map<Gwid, Integer> aDataIds, Map<Integer, Gwid> aDataIndexes ) {
    TsNullArgumentRtException.checkNulls( aIndex, aDataIds, aDataIds );
    Gwid retValue = aDataIndexes.get( aIndex );
    if( retValue != null ) {
      return retValue;
    }
    for( Gwid gwid : aDataIds.keySet() ) {
      if( aDataIds.get( gwid ).equals( aIndex ) ) {
        aDataIndexes.put( aIndex, gwid );
        return gwid;
      }
    }
    // Не найдено текущее данное с указанным индексом
    throw new TsItemNotFoundRtException( ERR_DATA_NOT_FOUND, aIndex );
  }

  /**
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aMessage String начальная строка
   * @param aDatasetIndexes {@link IIntMap}&lt;{@link Gwid}&gt; индексы набора текущих данных.
   *          <p>
   *          Ключ: целочисленный индекс текущего данного ;<br>
   *          Значение: {@link Gwid}-идентификатор текущих данных
   * @param aValues {@link IIntMap} карта значений.
   *          <p>
   *          Ключ: целочисленный индекс текущего данного<br>
   *          Значение: {@link IAtomicValue} значение текущего данного
   * @return String строка представления значений текущих данных
   */
  public static String toStr( String aMessage, IIntMap<Gwid> aDatasetIndexes, IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aDatasetIndexes, aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ) ) );
    for( int index : aValues.keys() ) {
      IAtomicValue value = aValues.getByKey( index );
      Gwid foundGwid = aDatasetIndexes.findByKey( index );
      if( foundGwid != null ) {
        sb.append( String.format( MSG_CURRDATA_VALUE, foundGwid, value ) );
      }
    }
    return sb.toString();
  }

  /**
   * Возвращает строку представляющую значения хранимых данных
   *
   * @param aMessage String начальная строка
   * @param aValues {@link DpuWriteHistData} значения хранмых данных для записи.
   * @return String строка представления значений данных
   */
  public static String toStr( String aMessage, DpuWriteHistData aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( aMessage, Integer.valueOf( aValues.size() ) ) );
    for( Gwid gwid : aValues.keys() ) {
      DpuWriteHistDataValues dataValues = aValues.getByKey( gwid );
      ITimeInterval interval = dataValues.interval();
      Integer count = Integer.valueOf( dataValues.values().size() );
      sb.append( String.format( MSG_HISTDATA_VALUE, gwid, interval, count ) );
    }
    return sb.toString();
  }
}
