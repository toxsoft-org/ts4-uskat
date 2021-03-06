package org.toxsoft.uskat.s5.server.startup;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.server.IS5ImplementConstants;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceImplementation;

/**
 * Начальная, неизменяемая, проектно-зависимая конфигурация реализации бекенда сервера
 * <p>
 * Все параметры конфигурации {@link IParameterized} устанавливаются в параметрах бекенда
 * {@link ISkBackendInfo#params()}. Перечень и описание параметров находится в {@link IS5ServerHardConstants}.
 * <p>
 * В системе (в конечном проекте) должна существовать одна реализация синглетона этого интерфейса с именем
 * {@link IS5ImplementConstants#PROJECT_INITIAL_IMPLEMENT_SINGLETON}. Для упрощения реализации синглетона
 * {@link IS5ImplementConstants#PROJECT_INITIAL_IMPLEMENT_SINGLETON} может быть использована абстрактная реализация
 * {@link S5InitialImplementSingleton}.
 *
 * @author mvk
 */
public interface IS5InitialImplementation
    extends IParameterized {

  /**
   * Возвращает построителей расширений бекенда предоставляемые сервером
   *
   * @return {@link IStridablesList}&lt;{@link IS5BackendAddonCreator}&gt; список расширений бекенда
   */
  IStridablesList<IS5BackendAddonCreator> baCreators();

  /**
   * Возвращает cпецифичные для проекта параметры создания класса {@link ISkClassInfo#params()}
   * <p>
   * Проектно-специфичные параметры класса используются только при создании класса и впоследствии могут быть изменены
   * средствами {@link ISkSysdescr}.
   * <p>
   * Конкретные (проектные) реализации {@link IS5InitialImplementation} могут переопределять значения свойств более
   * приемлимых для реализации проекта, например:
   * <ul>
   * <li>Класс реализации объектов - {@link IS5ServerHardConstants#OP_OBJECT_IMPL_CLASS};</li>
   * <li>Класс реализации прямой связи объектов - {@link IS5ServerHardConstants#OP_FWD_LINK_IMPL_CLASS};</li>
   * <li>Класс реализации обратной связи объектов - {@link IS5ServerHardConstants#OP_REV_LINK_IMPL_CLASS}.</li>
   * </ul>
   *
   * @param aClassId {@link String} идентификатор класса
   * @return {@link IParameterized} параметры создания класса
   * @throws TsNullArgumentRtException аргумент = null
   */
  IParameterized projectSpecificCreateClassParams( String aClassId );

  /**
   * Возвращает описание реализации хранения указанного хранимого данного
   *
   * @param aGwid {@link Gwid} идентификатор хранимого данного
   * @param aType {@link EAtomicType} тип хранигого данного
   * @param aSync <b>true</b> синхронное данное; <b>false</b> асинхронное значение
   * @return {@link IS5SequenceImplementation} описание хранения. null: неопределяется в проектной конфигурации и
   *         выбирается по умолчанию
   */
  IS5SequenceImplementation findHistDataImplementation( Gwid aGwid, EAtomicType aType, boolean aSync );

  /**
   * Возвращает список всех реализаций хранения данных
   *
   * @return {@link IList}&lt;{@link IS5SequenceImplementation}&gt; список описаний (одно табличные и/или
   *         многотабличные). Пустой список: нет проектно-зависимых реализаций данных
   */
  IList<IS5SequenceImplementation> getHistDataImplementations();
}
