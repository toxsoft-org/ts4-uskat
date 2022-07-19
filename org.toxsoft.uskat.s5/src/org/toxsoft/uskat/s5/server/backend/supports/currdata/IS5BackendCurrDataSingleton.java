package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5CurrDataInterceptor;

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
   * Prepares backend to supply current RTdata values in real-time.
   *
   * @param aRtdGwids {@link IList}&lt;{@link Gwid}&gt; - list of current RTdata concrete GWIDs
   */
  void configureCurrDataReader( IList<Gwid> aRtdGwids );

  /**
   * Prepares backend to receive current values for the specified RTdata.
   * <p>
   * Note: for unprepared GWIDs updating curtret valyes by {@link #writeCurrData(Gwid, IAtomicValue)} has no effect.
   *
   * @param aRtdGwids {@link IList}&lt;{@link Gwid}&gt; - list of current RTdata concrete GWIDs
   */
  void configureCurrDataWriter( IList<Gwid> aRtdGwids );

  /**
   * Updates the actual value of the current data.
   * <p>
   * Note: GWID must be prviously configured for writing by {@link #configureCurrDataWriter(IList)}.
   *
   * @param aGwid {@link Gwid} - concrete GWID of RTdata with {@link IDtoRtdataInfo#isCurr()} = <code>true</code>
   * @param aValue {@link IAtomicValue} - current value of RTdata
   */
  void writeCurrData( Gwid aGwid, IAtomicValue aValue );

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

  /**
   * Делает попытку разблокирования удаленного доступа к указанным данным
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов освобождаемых данных
   * @return boolean <b>true</b> данные освобождены;<b>false</b> данные не могут быть освобождены (еще используются)
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean remoteUnlockGwids( IList<Gwid> aGwids );

}
