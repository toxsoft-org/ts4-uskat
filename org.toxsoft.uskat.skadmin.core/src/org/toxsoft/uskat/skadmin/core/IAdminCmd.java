package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;

/**
 * Команда для выполнения
 *
 * @author mvk
 */
public interface IAdminCmd {

  /**
   * Описание команды
   *
   * @return {@link IAdminCmdDef} - описание команды
   */
  IAdminCmdDef cmdDef();

  /**
   * Установить контекст выполнения команды
   *
   * @param aContext {@link IAdminCmdContext} контекст выполнения команды
   */
  void setContext( IAdminCmdContext aContext );

  /**
   * Выполнить команду.
   *
   * @param aArgValues {@link IStringMap} - карта значений аргументов. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdArgDef#id()}.
   * @param aCallback {@link IAdminCmdCallback} - обратный вызов для слежения над процессом выполнения
   * @return {@link IAdminCmdResult} - результат выполнения
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException библиотека завершила работу
   * @throws TsItemNotFoundRtException команда не существует
   * @throws TsIllegalArgumentRtException параметры контекста не соответствуют описанию команды
   * @throws TsItemNotFoundRtException не найден аргумент для выполнения команды
   * @throws TsIllegalArgumentRtException тип аргумента не соответствует описанию аргументов команды
   */
  IAdminCmdResult exec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback );

  /**
   * Возвращает список возможных значений для аргумента команды с указанными аргументами.
   * <p>
   * В отличии от метода {@link #exec(IStringMap, IAdminCmdCallback)} в карте значений аргументов могут быть
   * представлены значения НЕ ВСЕX аргументов команды, а только доступные на текущий момент времени
   *
   * @param aArgId String - идентификатор аргумента {@link IAdminCmdArgDef#id()}
   * @param aArgValues {@link IStringMap} - карта значений аргументов. Ключ карты: идентификатор аргумента
   *          {@link IAdminCmdArgDef#id()}.
   * @return {@link IList}&lt;{@link IPlexyValue}&gt; - список возможных значений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException библиотека завершила работу
   * @throws TsItemNotFoundRtException не найден аргумент для выполнения команды
   * @throws TsIllegalArgumentRtException тип аргумента не соответствует описанию аргументов команды
   */
  IList<IPlexyValue> getPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues );

}
