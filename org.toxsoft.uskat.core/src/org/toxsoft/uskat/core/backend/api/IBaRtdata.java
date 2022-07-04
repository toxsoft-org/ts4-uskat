package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;

/**
 * Backend addon for current and historic RTdata.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaRtdata
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Rtdata"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Текущие данные

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
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта "уникальный ключ" - "GWID РВданного"
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  IIntMap<Gwid> configureCurrDataReader( IGwidList aToRemove, IGwidList aToAdd );

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
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список записываемых клиентом данных
   * @return {@link IIntMap}&lt;{@link Gwid}&gt; - карта "уникальный ключ" - "GWID РВданного"
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  IIntMap<Gwid> configureCurrDataWriter( IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Returns the single RTdata history for specified time interval.
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aGwid {@link Gwid} - valid concrete single RTdata GWID of one object
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of the queried entities
   */
  ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid );

}
