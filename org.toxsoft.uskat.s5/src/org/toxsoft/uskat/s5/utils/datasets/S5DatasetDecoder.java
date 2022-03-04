package org.toxsoft.uskat.s5.utils.datasets;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Перекодировщик индексов данных одного набора (дочерний набор) в индексы другого (родительский набор) и обратно.
 * <p>
 * Родительским набором считается набор который находится "ближе" к источнику данных и "дальше" от клиента. <br>
 * Дочерний набор наоборот: "ближе" к клиенту и "дальше" от источника данных.
 *
 * @author mvk
 */
public final class S5DatasetDecoder {

  /**
   * Карта родительских индексов по идентификаторам данных
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: индекс данного в родительском наборе (parentIndex).
   */
  private IIntMap<Gwid> dataset = IIntMap.EMPTY;

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает список идентификаторов данных набора
   *
   * @return {@link IGwidList} список идентификаторов данных
   */
  public IGwidList dataset() {
    return new GwidList( dataset.values() );
  }

  /**
   * Возвращает текущую конфигурацию набота текущих данных
   *
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта индексов набора.<br>
   *         Ключ: индекс данного в дочернем наборе (childIndex);<br>
   *         Значение: идентификатор данного.
   */
  public IIntMap<Gwid> configuration() {
    return dataset;
  }

  /**
   * Конфигурирует декодер.
   * <p>
   * Первый вызов {@link #configure(IIntMap)} проводит инициализацию декодера после которой индексы дочернего набора
   * будет теми же, что и родительского набора (декодирования не будет).
   * <p>
   * Если при любом последующем вызове {@link #configure(IIntMap)} будет обнаружено, что карта родительских индексов
   * изменилась, то будет создана карта для декодирования. Только после этого будет происходить декодирование индексов
   * при вызове {@link #parentToChild(IIntMap)} и/или {@link #childToParent(IIntMap)}.
   *
   * @param aCofiguration {@link IIntMap}&lt;{@link Gwid}&gt; карта индексов родительского набора.<br>
   *          Ключ: индекс данного в родительском наборе (parentIndex);<br>
   *          Значение: идентификатор данного.
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта индексов набора.<br>
   *         Ключ: индекс данного в дочернем наборе (childIndex);<br>
   *         Значение: идентификатор данного.
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  public IIntMap<Gwid> configure( IIntMap<Gwid> aCofiguration ) {
    TsNullArgumentRtException.checkNull( aCofiguration );
    dataset = aCofiguration;
    return dataset;
  }

  /**
   * Преобразует карту значений набора из дочерних индексов в индексы родительского набора
   *
   * @param aValues {@link IIntMap}&lt;{@link IAtomicValue}&gt; карта значений по дочерним индексам<br>
   *          Ключ: индекс данного в дочернем наборе;<br>
   *          Значение: значение данного.
   * @return {@link IIntMap}&lt;{@link IAtomicValue}&gt; карта значений по родительским индексам<br>
   *         Ключ: индекс данного в родительском наборе;<br>
   *         Значение: значение данного.
   */
  @SuppressWarnings( "static-method" )
  public IIntMap<IAtomicValue> childToParent( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    return aValues;
  }

  /**
   * Преобразует карту значений набора из родительских индексов в индексы дочернего набора
   *
   * @param aValues {@link IIntMap}&lt;{@link IAtomicValue}&gt; карта значений по родительским индексам<br>
   *          Ключ: индекс данного в родительском наборе;<br>
   *          Значение: значение данного.
   * @return {@link IIntMap}&lt;{@link IAtomicValue}&gt; карта значений по дочерним индексам<br>
   *         Ключ: индекс данного в дочернем наборе;<br>
   *         Значение: значение данного.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IIntMap<IAtomicValue> parentToChild( IIntMap<IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    return aValues;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
