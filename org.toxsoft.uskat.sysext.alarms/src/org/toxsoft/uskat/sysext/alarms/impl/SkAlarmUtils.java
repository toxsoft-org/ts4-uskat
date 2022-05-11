package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.av.math.EAvCompareOp;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.ILongList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.filters.*;

/**
 * Вспомогательные методы работы с тревогами.
 *
 * @author goga
 */
public final class SkAlarmUtils {

  /**
   * ИД-путь префикс идентификаторов фильтров над объектами типа {@link ISkAlarm}.
   */
  public static final String FILTER_ID_PREFIX_IDPATH = "ru.toxsoft.vj.server.alarms"; //$NON-NLS-1$

  /**
   * Создает параметры поли-фильтра из переданных аргументов.
   * <p>
   * Фильтра создается по следующим правилам:
   * <ul>
   * <li>каждый аргумент-список порождает единичные фильтры, объединенные по ИЛИ;</li>
   * <li>три набора фильтров (по количеству аргументов) объединяются по И;</li>
   * <li>если любой аргмент-список пустой, то соответствющий набор фильтров не создается.</li>
   * </ul>
   * Если все три аргумента пустые списки, то возвращается {@link IPolyFilterParams#NULL}.
   *
   * @param aAlarmDefIds {@link IStringList} - список идентификаторов типов тревог
   * @param aPriorities IList&lt;{@link EAlarmPriority}&gt; - список запрашиваемых приоритетов
   * @param aAuthorObjIds {@link ILongList} - список идентификаторов авторов тревог
   * @return {@link IPolyFilterParams} - параметры фильтра для выборки тревог
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IPolyFilterParams createQueryParams( IStringList aAlarmDefIds, IList<EAlarmPriority> aPriorities,
      ILongList aAuthorObjIds ) {
    TsNullArgumentRtException.checkNulls( aAlarmDefIds, aPriorities, aAuthorObjIds );
    IPolyFilterParams[] pfpArr = new IPolyFilterParams[] { null, null, null };
    // aAlarmDefIds
    if( !aAlarmDefIds.isEmpty() ) {
      for( int i = 0; i < aAlarmDefIds.size(); i++ ) {
        SingleFilterParams sfp = new SingleFilterParams( ISkAlarmFilterByDefId.FILTER_ID );
        IStdStridFilter.FILTER_KIND.setValue( sfp.params(), EStridFilterKind.WHOLE_STR );
        IStdStridFilter.COUNT_COMPARE_OP.setValue( sfp.params(), EAvCompareOp.EQ );
        IStdStridFilter.STRING_CONST.setValue( sfp.params(), DvUtils.avStr( aAlarmDefIds.get( i ) ) );
        if( pfpArr[0] == null ) {
          pfpArr[0] = PolyFilterParams.createSingle( sfp );
        }
        else {
          pfpArr[0] = PolyFilterParams.createPoly( sfp, EFilterOperation.OR, pfpArr[0] );
        }
      }
    }
    // aPriorities
    if( !aPriorities.isEmpty() ) {
      for( int i = 0; i < aPriorities.size(); i++ ) {
        SingleFilterParams sfp = new SingleFilterParams( ISkAlarmFilterByPriority.FILTER_ID );
        ISkAlarmFilterByPriority.COMPARE_OP.setValue( sfp.params(), EAvCompareOp.EQ );
        ISkAlarmFilterByPriority.PRIORITY_CONST.setValue( sfp.params(), aPriorities.get( i ) );
        if( pfpArr[1] == null ) {
          pfpArr[1] = PolyFilterParams.createSingle( sfp );
        }
        else {
          pfpArr[1] = PolyFilterParams.createPoly( sfp, EFilterOperation.OR, pfpArr[1] );
        }
      }
    }
    // aAuthorObjIds
    if( !aAuthorObjIds.isEmpty() ) {
      for( int i = 0; i < aAuthorObjIds.size(); i++ ) {
        SingleFilterParams sfp = new SingleFilterParams( ISkAlarmFilterByAuthorObjId.FILTER_ID );
        ISkAlarmFilterByAuthorObjId.AUTHOR_ID_CONST.setValue( sfp.params(),
            DvUtils.avInt( aAuthorObjIds.getItem( i ) ) );
        if( pfpArr[2] == null ) {
          pfpArr[2] = PolyFilterParams.createSingle( sfp );
        }
        else {
          pfpArr[2] = PolyFilterParams.createPoly( sfp, EFilterOperation.OR, pfpArr[2] );
        }
      }
    }
    // объединим фильтры по И
    IPolyFilterParams result = IPolyFilterParams.NULL;
    for( int i = 0; i < pfpArr.length; i++ ) {
      IPolyFilterParams pfp = pfpArr[i];
      if( pfp == null ) {
        continue;
      }
      if( result == IPolyFilterParams.NULL ) {
        result = pfp;
        continue;
      }
      result = PolyFilterParams.createPoly( result, EFilterOperation.AND, pfp );
    }
    return result;
  }

  /**
   * Запрет на создание экземпляров.
   */
  private SkAlarmUtils() {
    // nop
  }

  /**
   * Из представленного списка слушателей алармов и поступившего аларма формирует карту в которой ключами являются
   * слушатели,а значениями списки алармов которые они должны получить
   *
   * @param aListeners {@link IMap}&lt;{@link ISkAlarmServiceListener},{@link IList}&lt;{@link IPolyFilter}&gt;&gt;
   *          карта зарегистрированных слушателей алармов. Ключ: слушатель. Значение: список список фильтров алармов.
   * @param aSkAlarm {@link SkAlarm} аларм для извещения
   * @return {@link IMap}&lt;{@link ISkAlarmServiceListener},{@link IList}&lt;{@link ISkAlarm}&gt;&gt; карта алармов для
   *         каждого слушателя
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IMap<ISkAlarmServiceListener, IList<ISkAlarm>> createListenerAlarms(
      IMap<ISkAlarmServiceListener, IList<IPolyFilter>> aListeners, ISkAlarm aSkAlarm ) {
    TsNullArgumentRtException.checkNulls( aListeners, aSkAlarm );
    IMapEdit<ISkAlarmServiceListener, IList<ISkAlarm>> retValue = new ElemMap<>();
    for( ISkAlarmServiceListener listener : aListeners.keys() ) {
      IList<IPolyFilter> filters = aListeners.getByKey( listener );
      for( IPolyFilter filter : filters ) {
        if( filter == IPolyFilter.NULL || filter.accept( aSkAlarm ) ) {
          IListEdit<ISkAlarm> skAlarms = (IListEdit<ISkAlarm>)retValue.findByKey( listener );
          if( skAlarms == null ) {
            skAlarms = new ElemArrayList<>();
            retValue.put( listener, skAlarms );
          }
          skAlarms.add( aSkAlarm );
        }
      }
    }
    return retValue;
  }

  /**
   * Метод регистрирующий все существующие фильтры для алармов
   *
   * @param aFilterFactoryRegistry реестр фильтров
   */
  public static void registerAlarmFilters( FilterFactoriesRegistry aFilterFactoryRegistry ) {
    TsNullArgumentRtException.checkNulls( aFilterFactoryRegistry );
    // Регистрируем фабрики фильтров алармов
    aFilterFactoryRegistry.registerFactory( SkAlarmFilterByDefId.FACTORY );
    aFilterFactoryRegistry.registerFactory( SkAlarmFilterByLevel.FACTORY );
    aFilterFactoryRegistry.registerFactory( SkAlarmFilterByAuthorObjId.FACTORY );
    aFilterFactoryRegistry.registerFactory( SkAlarmFilterByPriority.FACTORY );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Добавляет фильтр для слушателя
   *
   * @param aListeners {@link IMapEdit} редактируемая карта слушателей
   *          <p>
   *          Ключ: слушатель тревоги {@link ISkAlarmServiceListener};<br>
   *          Значение: список описаний фильтров {@link IPolyFilterParams} событий тревог.
   * @param aListener {@link ISkAlarmServiceListener} слушатель
   * @param aSelection {@link IPolyFilterParams} параметры фильтра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void addListenerSelection( IMapEdit<ISkAlarmServiceListener, IListEdit<IPolyFilterParams>> aListeners,
      ISkAlarmServiceListener aListener, IPolyFilterParams aSelection ) {
    TsNullArgumentRtException.checkNulls( aListeners, aListener, aSelection );
    IListEdit<IPolyFilterParams> selections = aListeners.findByKey( aListener );
    if( selections == null ) {
      // aAllowDuplicates = false
      selections = new ElemArrayList<>( false );
      aListeners.put( aListener, selections );
    }
    selections.add( aSelection );
  }
}
