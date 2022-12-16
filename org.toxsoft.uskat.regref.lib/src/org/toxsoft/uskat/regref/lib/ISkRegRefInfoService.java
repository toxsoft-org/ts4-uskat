package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.regref.lib.impl.ISkRegRefServiceHardConstants;

/**
 * Служба работы с НСИ.
 * <p>
 * RRI (Regulatoy and reference information) = НСИ ((Нормативно-Справочная Информация).
 * <p>
 * <b>Нормативно-Справочная Информация</b> - это <i><b>параметры НСИ</b></i>, связанные с объектами системы. Параметр
 * НСИ имеет <i><b>описание</b></i>, связанное с опаределенным классом системы, и <i><b>значения</b></i> для каждого
 * объекта класса. Таким образом, каждый объект системы кроме свойств своего класса (атрибуты, данные, связи, команды,
 * события) получает набор параметров НСИ. Параметры НСИ содержатся (сгруппированы) в тематических
 * <i><b>разделах</b></i>. Разбиение на разделы определяется разработчиком системы (например, по разным ИУСам, или по
 * нормативным документам).
 * <p>
 * Внимание: служба работает только с теми объектами, которые имеют уникальный строковый идентификатор
 * {@link ISkObject#strid()}. Служба <b>не</b> позволяет получить лоступ к НСИ объектов по <code>long</code>
 * идентификатору.
 *
 * @author goga
 */
public interface ISkRegRefInfoService
    extends ISkService {

  /**
   * Идентификатор службы.
   */
  String SERVICE_ID = ISkRegRefServiceHardConstants.SERVICE_ID;

  /**
   * Возвращает все разделы, существующие в системе.
   * <p>
   * Ссылки на разделы не меняются за время работы службы (если раздел не был удален).
   *
   * @return {@link IStridablesList}&lt;{@link ISkRriSection}&gt; - список всех разделов
   */
  IStridablesList<ISkRriSection> listSections();

  /**
   * Находит раздел.
   *
   * @param aSectionId String - идентификатор раздела
   * @return {@link ISkRriSection} - раздел или <code>null</code>
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   */
  ISkRriSection findSection( String aSectionId );

  /**
   * Возвращает существующий раздел.
   *
   * @param aSectionId String - идентификатор раздела
   * @return {@link ISkRriSection} - раздел
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого раздела
   */
  ISkRriSection getSection( String aSectionId );

  /**
   * Создает раздел НСИ.
   *
   * @param aId String - идентификатор (ИД-путь) раздела
   * @param aName String - название раздела
   * @param aDescription String - описание раздела
   * @param aParams {@link IOptionSet} - значения параметров {@link ISkRriSection#params()}
   * @return {@link ISkRriSection} - созданный раздел
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка
   *           {@link ISkRegRefInfoServiceValidator#canCreateSection(String, String, String, IOptionSet)}
   */
  ISkRriSection createSection( String aId, String aName, String aDescription, IOptionSet aParams );

  /**
   * Удаялет раздел НСИ со всеми описаниями и значениями параметров.
   *
   * @param aSectionId String - идентификатор раздела НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка
   *           {@link ISkRegRefInfoServiceValidator#canRemoveSection(String)}
   */
  void removeSection( String aSectionId );

  /**
   * Возвращает историю редактирования НСИ.
   *
   * @return {@link ISkRriHistory} - доступ к истории редактирования НСИ
   */
  ISkRriHistory history();

  /**
   * Returns the service mutator methods pre-conditions validation helper.
   *
   * @return {@link ITsValidationSupport} - service changes validation support
   */
  ITsValidationSupport<ISkRegRefInfoServiceValidator> svs();

  /**
   * Returns the service changes event firing helper.
   *
   * @return {@link ITsEventer} - event firing and listening helper
   */
  ITsEventer<ISkRegRefInfoServiceListener> eventer();

}
