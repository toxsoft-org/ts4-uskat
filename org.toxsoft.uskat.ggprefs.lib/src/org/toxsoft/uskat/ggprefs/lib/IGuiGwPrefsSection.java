package org.toxsoft.uskat.ggprefs.lib;

import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Раздел настроек.
 * <p>
 * Ссылка на этот раздел находится в контексте приложения.
 *
 * @author goga
 */
public interface IGuiGwPrefsSection
    extends IStridableParameterized {

  /**
   * Определяет опции с привязкой к сущности зеленого мира.
   * <p>
   * Аргумент aGwid должен быть вида {@link EGwidKind#GW_CLASS}, абстрактным или конкретным. Конкретный GWID
   * {@link Gwid#isAbstract()} = <code>false</code> добавляет опцию, связанную с конкретным объектом. А абстрактный GWID
   * определяет опцию, которая будет у всех объектов этого класса (естественно, включая наследников).
   *
   * @param aGwid {@link Gwid} - GWID идентификатор сущности
   * @param aOpDefs {@link IStridablesList}&lt;{@link IDataDef}&gt; - список описании параметров
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такой сущности в системе
   * @throws TsItemAlreadyExistsRtException опция с указанным идентификатором уже есть в иерархии настроек
   */
  void bindOptions( Gwid aGwid, IStridablesList<IDataDef> aOpDefs );

  /**
   * Возвращает описания опции настроек GUI указанного объекта.
   *
   * @param aObjSkid {@link Skid} - SKID запрашиваемого объекта
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - список описании опции
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IStridablesList<IDataDef> listOptionDefs( Skid aObjSkid );

  /**
   * Возвращает значения опции настроек GUI указанного объекта.
   *
   * @param aObjSkid {@link Skid} - SKID запрашиваемого объекта
   * @return {@link IOptionSet} - значения опции
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IOptionSet getOptions( Skid aObjSkid );

  /**
   * Задает значения опции для указанного объекта.
   * <p>
   * В аргументе межет содержаться не все опции, а только часть. А несуществующие в {@link #listOptionDefs(Skid)}
   * значения молча игнорируются.
   * <p>
   * Обычно программисту не нужно самому вызывать этот метод и задавать значения опции. Это дело пользователя
   * редактировать значения опции. Редактирование происходит средствами {GuiGwPrefsUtils}, откуда и вызывается этот
   * метод.
   *
   * @param aObjSkid {@link Skid} - SKID объекта, чьи настроки GUI будут изменены
   * @param aOps {@link IOptionSet} - новые значения части или всех опции
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws AvTypeCastRtException тип какого-либо значения не соответствует описанию
   */
  void setOptions( Skid aObjSkid, IOptionSet aOps );

  /**
   * Возвращает средство управления извещениями.
   *
   * @return {@link IGuiGwPrefsSectionEventer} - упраления слушателями
   */
  IGuiGwPrefsSectionEventer eventer();

}
