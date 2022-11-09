package org.toxsoft.uskat.dataquality.lib;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Ярлык, тикет службы качества данных.
 * <p>
 * Тикет обязательно имеет значение по умолчанию {@link #defaultValue()}. Считается, что любой отслеживаемый ресурс,
 * который явно не содержит пометку этим ярлыком, все равно имеет пометку значением по умолчанию. Напомним, что значение
 * по умолчанимю может быть {@link IAtomicValue#NULL}.
 * <p>
 * Несколько отдельно стоят встроенные тикеты (у которых {@link #isBuiltin()} = <code>true</code>. Их нельзя создавать,
 * редактировать или удалять. Кроме того, они имеют гораничение на возморжноть изменения значения пометки пользователем.
 * Например, встроенный тикет с идентификатором {@link ISkDataQualityService#TICKET_ID_NO_CONNECTION} не позволяет
 * менять значение пометки.
 * <p>
 * Этот интерфейс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный (в контексте зарегистрированных тикетов) идентификатор тикета (ИД-путь);</li>
 * <li><b>description</b>() - удобочитаемое описание тикета;</li>
 * <li><b>nmName</b>() - краткое название тикета;</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkDataQualityTicket
    extends IStridable {

  /**
   * Тип данных.
   *
   * @return {@link IDataType} - тип данных
   */
  IDataType dataType();

  /**
   * Возвращает признак встроенного тикета.
   * <p>
   * Встроенные тикеты нельзя редактировать или удалять.
   *
   * @return boolean признак встроенного тикета
   */
  boolean isBuiltin();

  /**
   * Возвращает значение тикета из набора пометок конкретного ресурса.
   * <p>
   * Этот метод удобно использовать для извлечения значения из возвращаемого методом
   * {@link ISkDataQualityService#getResourceMarks(Gwid)} значения. Если ресурс не никак помечен ярлыком (то есть, набор
   * aMarks не содержит значение с идентификатором тикета {@link #id()}, то возвращает значение по умолчанию
   * {@link #defaultValue()}.
   *
   * @param aMarks {@link IOptionSet} набор пометок ресурса ярлыками
   * @return {@link IAtomicValue} значение пометки (а при отсутствии пометки - {@link #defaultValue()})
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IAtomicValue getMarkValue( IOptionSet aMarks );

  /**
   * Возвращает значение по умолчанию.
   *
   * @return {@link IAtomicValue} значение ярлыка по умолчанию
   */
  default IAtomicValue defaultValue() {
    return dataType().defaultValue();
  }

}
