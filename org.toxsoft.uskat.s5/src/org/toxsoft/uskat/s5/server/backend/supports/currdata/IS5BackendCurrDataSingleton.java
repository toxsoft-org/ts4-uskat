package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.*;
import org.toxsoft.uskat.s5.server.frontend.*;

/**
 * Локальный интерфейс синглетона запросов к текущим данным предоставляемым s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendCurrDataSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Изменяет набор текущих данных
   *
   * @param aRemoveRtdGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddRtdGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void reconfigure( IGwidList aRemoveRtdGwids, IMap<Gwid, IAtomicValue> aAddRtdGwids );

  /**
   * Конфигурирует, какие текущие РВданные хочет читать клиент.
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   * <p>
   * Метод возвращает карту. Ключами в карте являются уникальные int-ключи, назначаемыйе сервером запрошенному
   * РВданному. Значением в карте является {@link Gwid} идентификатор <code>всех</code> запрошенных клиентом данных. То
   * есть, значения {@link IIntMap#values()} в карте, это список все РВданных, сформированный согласно запросу - ранее
   * запрошенные данные минус <code>aToRemove</code> плюс <code>aToAdd</code>. При этом в карте отсутствуют
   * повторяющейся РВ данные.
   * <p>
   * Обратите внимание, что сохранение значения ключенй между вызовами метода не гарантируется. Один и тотже
   * {@link Gwid} может иметь разный ключ после каждого вызова.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aRtdGwids {@link IGwidList} - список идентификаторов данных читаемых клиентом
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта текущих значений;<br>
   *         Key: {@link Gwid} конкретный GWID текущего данного;<br>
   *         Value: {@link IAtomicValue} текущее значение данного.
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  IMap<Gwid, IAtomicValue> configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aRtdGwids );

  /**
   * Конфигурирует, какие текущие РВданные хочет писать клиент.
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента имеют совершенно разный смысл! Пустой
   * список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время, как
   * <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены из
   * списка интересующих клиента.
   * <p>
   * Метод возвращает карту. Ключами в карте являются уникальные int-ключи, назначаемыйе сервером запрошенному
   * РВданному. Значением в карте является {@link Gwid} идентификатор <code>всех</code> запрошенных клиентом данных. То
   * есть, значения {@link IIntMap#values()} в карте, это список все РВданных, сформированный согласно запросу - ранее
   * запрошенные данные минус <code>aToRemove</code> плюс <code>aToAdd</code>. При этом в карте отсутствуют
   * повторяющейся РВ данные.
   * <p>
   * Обратите внимание, что сохранение значения ключенй между вызовами метода не гарантируется. Один и тотже
   * {@link Gwid} может иметь разный ключ после каждого вызова.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aRtdGwids {@link IGwidList} - список идентификаторов данных записываемых клиентом
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  void configureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aRtdGwids );

  /**
   * Возвращает список идентификаторов текущих данных читаемых клиентами службы
   *
   * @return {@link IGwidList} список данных
   */
  IGwidList readRtdGwids();

  /**
   * Возвращает список идентификаторов текущих данных записываемых клиентами службы
   *
   * @return {@link IGwidList} список данных
   */
  IGwidList writeRtdGwids();

  /**
   * Возвращает значения текущих данных
   *
   * @param aRtGwids {@link IGwidList} список идентификаторов данных
   * @return {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта значений данных;<br>
   *         Ключ: идентификатор данного;<br>
   *         Значение: значение данного.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException текущего данного не существует
   */
  IMap<Gwid, IAtomicValue> readValues( IGwidList aRtGwids );

  /**
   * Записывает значения текущих данных в систему.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта значений данных;<br>
   *          Ключ: идентификатор данного;<br>
   *          Значение: значение данного.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void writeValues( IS5FrontendRear aFrontend, IMap<Gwid, IAtomicValue> aValues );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5CurrDataInterceptor } перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5CurrDataInterceptor } перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor );

}
