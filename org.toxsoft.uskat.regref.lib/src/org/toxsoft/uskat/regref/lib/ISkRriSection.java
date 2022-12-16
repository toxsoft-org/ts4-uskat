package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.ISkEventService;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoLinkInfo;

/**
 * Раздел НСИ.
 *
 * @author goga
 */
public interface ISkRriSection
    extends IStridable, IParameterized {

  /**
   * Меняет свойства раздела.
   *
   * @param aName String - название раздела
   * @param aDescription String - описание раздела
   * @param aParams {@link IOptionSet} - значения параметров {@link ISkRriSection#params()}
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка
   *           {@link ISkRriSectionValidator#canSetSectionParams(ISkRriSection, String, String, IOptionSet)}
   */
  void setSectionProps( String aName, String aDescription, IOptionSet aParams );

  /**
   * Возвращает идентификатор объекта, скрытно реализуюший этот раздел.
   * <p>
   * В частности, идентификатор можно использовать для получения сообщений редактирования параметров НСИ средствами
   * службы {@link ISkEventService}. Напомним, что история правки НСИ (те же самые события) доступны через
   * {@link ISkRriHistory#querySectionEditingHistory(IQueryInterval, String)}.
   *
   * @return {@link Skid} - идентификатор объекта раздела
   */
  Skid getSectionSkid();

  // ------------------------------------------------------------------------------------
  // Работа с описаниями параметров

  /**
   * Определяет (создает новое или правит существующее) описание параметра НСИ - атрибута.
   *
   * @param aClassId String - идентификатор класса
   * @param aAttrDef {@link IDtoAttrInfo} - описание атрибута
   * @return {@link ISkRriParamInfo} - описание созданного/отредактированного параметра НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  ISkRriParamInfo defineAttrParam( String aClassId, IDtoAttrInfo aAttrDef );

  /**
   * Определяет (создает новое или правит существующее) описание параметра НСИ - связи.
   *
   * @param aClassId String - идентификатор класса
   * @param aLinkDef {@link IDtoLinkInfo} - описание связи
   * @return {@link ISkRriParamInfo} - описание созданного/отредактированного параметра НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalStateRtException параметр уже существет и является атрибутом
   * @throws TsUnsupportedFeatureRtException редактирование приведет к удалению значений параметров
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  ISkRriParamInfo defineLinkParam( String aClassId, IDtoLinkInfo aLinkDef );

  /**
   * Удаляет параметр (включая описание и значения для всех объектов).
   *
   * @param aClassId String - идентификатор класса
   * @param aParamId String - идентификатор объекта
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого параметра
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void removeParam( String aClassId, String aParamId );

  /**
   * Удаляет все параметры НСИ для указанного класса и его наследников.
   * <p>
   * Удаляются как описания, так и значения параметров.
   *
   * @param aClassId String - идентфиикатор класса
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void removeAll( String aClassId );

  /**
   * Удаляет все параметры всех классов раздела.
   *
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void clearAll();

  /**
   * Возвращает перечень идентификаторов классов, для которых этот раздел определяет параметры НСИ.
   *
   * @return {@link IStringList} - список идентификаторов классов хотя бы с одним параметром НСИ этого раздела
   */
  IStringList listClassIds();

  /**
   * Возвращает описания параметров этого раздела запрошенного класса.
   * <p>
   * Если класс существует, но для него не определены параметры НСИ, возвращает пустой список.
   *
   * @param aClassId String - идентификатор класса
   * @return {@link IStridablesList}&lt;{@link ISkRriParamInfo}&gt; - описания параметров
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого класса в описании системы
   */
  IStridablesList<ISkRriParamInfo> listParamInfoes( String aClassId );

  // ------------------------------------------------------------------------------------
  // Получение значений параметров

  /**
   * Возвращает значение одного параметра НСИ - атрибута.
   *
   * @param aObjId {@link Skid} - идентификатор объекта
   * @param aParamId String - идентификатор параметра НСИ
   * @return {@link IAtomicValue} - значение параметра НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого объекта в системе
   * @throws TsItemNotFoundRtException нет такого параметра в разделе
   * @throws TsUnsupportedFeatureRtException параметр является связью, не параметром
   */
  IAtomicValue getAttrParamValue( Skid aObjId, String aParamId );

  /**
   * Возвращает значение одного параметра НСИ - связи.
   *
   * @param aObjId {@link Skid} - идентификатор объекта
   * @param aParamId String - идентификатор параметра НСИ
   * @return {@link ISkidList} - список связанных объектов - значение параметра НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого объекта в системе
   * @throws TsItemNotFoundRtException нет такого параметра в разделе
   * @throws TsUnsupportedFeatureRtException параметр является атрибутом, не связью
   */
  ISkidList getLinkParamValue( Skid aObjId, String aParamId );

  /**
   * Возвращает значения всех параметров НСИ этого раздела всех запрошенных объектов.
   *
   * @param aObjIds {@link ISkidList} - запрошенные объекты
   * @return {@link ISkRriParamValues} - значения параметров
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException в аргументе содержатся несуществующие в системе объекты
   */
  ISkRriParamValues getParamValuesByObjs( ISkidList aObjIds );

  /**
   * Возвращает значения всех параметров НСИ этого раздела всех объектов запрошенного класса, без наследников.
   *
   * @param aClassId String - идентификатор класса
   * @return {@link ISkRriParamValues} - значения параметров
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого класса в системе
   */
  ISkRriParamValues getParamValuesByClassId( String aClassId );

  // ------------------------------------------------------------------------------------
  // Редактирование значений параметров

  /**
   * Задает значение одного параметра НСИ - атрибута.
   *
   * @param aObjId {@link Skid} - объект, чей параметры НСИ редактируется
   * @param aParamId String - идентификатор параметра
   * @param aValue {@link IAtomicValue} - новое значение атрибута
   * @param aReason String - причина изменения НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void setAttrParamValue( Skid aObjId, String aParamId, IAtomicValue aValue, String aReason );

  /**
   * Задает значение одного параметра НСИ - связи.
   *
   * @param aObjId {@link Skid} - объект, чей параметры НСИ редактируется
   * @param aParamId String - идентификатор параметра
   * @param aObjIds {@link ISkidList} - новое значение связи
   * @param aReason String - причина изменения НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void setLinkParamValue( Skid aObjId, String aParamId, ISkidList aObjIds, String aReason );

  /**
   * Изменяет несколько параметров за раз.
   * <p>
   * Генерирует событие редактирования сметкой текущего времени, автором - текущи пользователем и причиной
   * <code>aReason</code>.
   *
   * @param aValues {@link ISkRriParamValues} - нобор новых значений параметров
   * @param aReason String - причина изменения НСИ
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkRriSectionValidator}
   */
  void setParamValues( ISkRriParamValues aValues, String aReason );

  /**
   * Returns the section mutator methods pre-conditions validation helper.
   *
   * @return {@link ITsValidationSupport} - section changes validation support
   */
  ITsValidationSupport<ISkRriSectionValidator> svs();

  /**
   * Returns the section changes event firing helper.
   *
   * @return {@link ITsEventer} - event firing and listening helper
   */
  ITsEventer<ISkRriSectionListener> eventer();

}
