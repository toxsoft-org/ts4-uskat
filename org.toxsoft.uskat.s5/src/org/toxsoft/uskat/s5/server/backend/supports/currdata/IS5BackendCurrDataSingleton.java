package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.coll.primtypes.IIntMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataInterceptor;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

import ru.uskat.common.dpu.rt.events.SkCurrDataValues;

/**
 * Локальный доступ к поддержки расширения бекенда: "данные реального времени"
 *
 * @author mvk
 */
@Local
public interface IS5BackendCurrDataSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Изменяет набор текущих данных
   *
   * @param aRemovedGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddedGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void reconfigure( IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids );

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
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта "уникальный ключ" - "GWID РВданного"
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  IIntMap<Gwid> configureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

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
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список записываемых клиентом данных
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта "уникальный ключ" - "GWID РВданного"
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  IIntMap<Gwid> configureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Возвращает список идентификаторов текущих данных читаемых клиентами службы
   *
   * @return {@link IGwidList} список данных
   */
  IGwidList readCurrDataIds();

  /**
   * Возвращает список идентификаторов текущих данных записываемых клиентами службы
   *
   * @return {@link IGwidList} список данных
   */
  IGwidList writeCurrDataIds();

  /**
   * Возвращает значение текущего данного по идентификатору
   *
   * @param aGwid {@link Gwid} идентификатор текущего данного
   * @return {@link Pair}&lt;Integer,{@link IAtomicValue}&gt; пара: индекс - значение текущего данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException текущего данного не существует
   */
  Pair<Integer, IAtomicValue> getValue( Gwid aGwid );

  /**
   * Возвращает значения текущих данных по индексам в наборе
   *
   * @param aIndexes {@link IIntList} список индексов РВданных в наборе
   * @return {@link SkCurrDataValues} карта значений РВданных;<br>
   *         Ключ: индекс в наборе;<br>
   *         Значение: значение РВданного.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException текущего данного не существует
   */
  SkCurrDataValues getValues( IIntList aIndexes );

  /**
   * Записывает текущие данные в систему.
   * <p>
   * Аргументом является карта, в которой ключи, это назначенные ранее сервером int-ы, присланные в качестве ответа на
   * {@link #reconfigure(IGwidList, IMap)} или {@link #configureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}.
   *
   * @param aValues {@link IIntMap}&lt;{@link IAtomicValue}&gt; - записываемые значения
   */
  void writeCurrData( IIntMap<IAtomicValue> aValues );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5HistDataInterceptor } перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5HistDataInterceptor } перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeCurrDataInterceptor( IS5CurrDataInterceptor aInterceptor );

  /**
   * Делает попытку разблокирования удаленного доступа к указанным данным
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов освобождаемых данных
   * @return boolean <b>true</b> данные освобождены;<b>false</b> данные не могут быть освобождены (еще используются)
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean remoteUnlockGwids( IList<Gwid> aGwids );
}
