package org.toxsoft.uskat.alarms.lib.impl;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.filter.ITsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.ISkAlarm;
import org.toxsoft.uskat.alarms.lib.ISkAlarmServiceListener;
import org.toxsoft.uskat.alarms.lib.filters.*;

/**
 * Вспомогательные методы работы с тревогами.
 *
 * @author goga
 */
public final class SkAlarmUtils {

  /**
   * Запрет на создание экземпляров.
   */
  private SkAlarmUtils() {
    // nop
  }

  /**
   * Метод регистрирующий все существующие фильтры для алармов
   *
   * @param aFilterRegistry реестр фильтров
   * @throws TsNullArgumentRtException аругмент = null
   */
  public static void registerAlarmFilters( ITsFilterFactoriesRegistry<ISkAlarm> aFilterRegistry ) {
    TsNullArgumentRtException.checkNulls( aFilterRegistry );
    aFilterRegistry.register( SkAlarmFilterByAuthor.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByDefId.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByLevel.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByMessage.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByPriority.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByHistory.FACTORY );
    aFilterRegistry.register( SkAlarmFilterByTimestamp.FACTORY );
  }

  /**
   * Добавляет фильтр для слушателя
   *
   * @param aListeners {@link IMapEdit} редактируемая карта слушателей
   *          <p>
   *          Ключ: слушатель тревоги {@link ISkAlarmServiceListener};<br>
   *          Значение: список описаний фильтров {@link ITsCombiFilterParams} событий тревог.
   * @param aListener {@link ISkAlarmServiceListener} слушатель
   * @param aFilter {@link ITsCombiFilterParams} параметры фильтра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void addListenerFilter( IMapEdit<ISkAlarmServiceListener, IListEdit<ITsCombiFilterParams>> aListeners,
      ISkAlarmServiceListener aListener, ITsCombiFilterParams aFilter ) {
    TsNullArgumentRtException.checkNulls( aListeners, aListener, aFilter );
    IListEdit<ITsCombiFilterParams> filters = aListeners.findByKey( aListener );
    if( filters == null ) {
      // aAllowDuplicates = false
      filters = new ElemArrayList<>( false );
      aListeners.put( aListener, filters );
    }
    filters.add( aFilter );
  }

  /**
   * Удаляет фильтр для слушателя
   *
   * @param aListeners {@link IMapEdit} редактируемая карта слушателей
   *          <p>
   *          Ключ: слушатель тревоги {@link ISkAlarmServiceListener};<br>
   *          Значение: список описаний фильтров {@link ITsCombiFilterParams} событий тревог.
   * @param aListener {@link ISkAlarmServiceListener} слушатель
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void removeListenerFilter(
      IMapEdit<ISkAlarmServiceListener, IListEdit<ITsCombiFilterParams>> aListeners,
      ISkAlarmServiceListener aListener ) {
    TsNullArgumentRtException.checkNulls( aListeners, aListener );
    IListEdit<ITsCombiFilterParams> filters = aListeners.findByKey( aListener );
    if( filters != null ) {
      aListeners.removeByKey( aListener );
    }
  }

  /**
   * Удаляет фильтр для слушателя
   *
   * @param aListeners {@link IMapEdit} редактируемая карта слушателей
   *          <p>
   *          Ключ: слушатель тревоги {@link ISkAlarmServiceListener};<br>
   *          Значение: список описаний фильтров {@link ITsCombiFilterParams} событий тревог.
   * @return {@link IList}&lt;{@link ITsCombiFilterParams}&gt; список описаний фильтров
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IList<ITsCombiFilterParams> getListenerFilters(
      IMapEdit<ISkAlarmServiceListener, IListEdit<ITsCombiFilterParams>> aListeners ) {
    TsNullArgumentRtException.checkNull( aListeners );
    // aAllowDuplicates = false
    IListEdit<ITsCombiFilterParams> retValue = new ElemArrayList<>( false );
    for( IList<ITsCombiFilterParams> filters : aListeners.values() ) {
      retValue.addAll( filters );
    }
    return retValue;
  }

}
