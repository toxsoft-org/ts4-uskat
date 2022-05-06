package org.toxsoft.uskat.sysext.realtime;

import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;

import ru.uskat.backend.addons.batchops.EOrphanProcessing;
import ru.uskat.backend.addons.batchops.ISkBackendAddonBatchOperations;
import ru.uskat.common.dpu.container.IDpuContainer;
import ru.uskat.common.dpu.container.IDpuIdContainer;
import ru.uskat.legacy.StdFilterStringMatcher;

/**
 * Служба пакетных операции.
 * <p>
 * Пакетное обновление позволяет в рамках одной транзакции обновить содержимое системы.
 * <p>
 * Возможности службы доступны только когда бекенд поддерживает расширение {@link ISkBackendAddonBatchOperations}.
 *
 * @author goga
 */
public interface ISkBatchOperationService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = SK_SYSEXT_SERVICE_ID_PREFIX + "BatchOperations"; //$NON-NLS-1$

  /**
   * Определяет, доступны ли возможности службы.
   *
   * @return boolean - признак того, что можно вызывать {@link #batchUpdate(IDpuIdContainer, IDpuContainer)}
   */
  boolean isAvailable();

  /**
   * Осуществляет пакетное обновление содержимого.
   * <p>
   * Метод сначала осуществялет удаление всех перечисленных в <code>aToRemove</code> аргументе сущностей, а потом
   * создание/обновление перечисленных в <code>aAddAndUpdate</code> сущностей. При этом, если перечисленные у удалению
   * сущсноти уже отсутствуют, то это не считается ошибкой, максимум - предупреждением.
   * <p>
   * Метод гарантирует, что либо все запрошенные действия будут выполнены (в рамках одной транзакции), либо ни одно
   * изменение не будет сделано. Поэтоому в названии и присутствует словно "пакетный" (bacth).
   * <p>
   * Если метод не смог выполнить действие, то в возвращаемом списке будет присутствовать хотя бы одна ошибка, и
   * значение {@link IValResList#isOk()} = <code>false</code>. Реализация может внести список сразу много ошибок, если
   * запрошенное пакетное обновление невозможно выполнить. Что касается предупреждении, то их может быть много и в
   * случае успешного обновления, и в случае невыполнения никаких действий.
   * <p>
   * Таким образом, о том, было ли выпонено запрошенное обновление, говорит значение {@link IValResList#isOk()}:
   * <b>true</b> - означает выполнение обновления, <b>false</b> - никаких изменений с содержимом не было сделано.
   *
   * @param aToRemove {@link IDpuIdContainer} - сущности, которые должны быть удалены
   * @param aAddAndUpdate {@link IDpuContainer} - сущности, которы должны быть добавлены и обновлены
   * @return {@link IValResList} - результат выполнения, может содержать множество ошибок и предупреждении
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsUnsupportedFeatureRtException возможности службы недоступны, {@link #isAvailable()} = <code>false</code>
   */
  IValResList batchUpdate( IDpuIdContainer aToRemove, IDpuContainer aAddAndUpdate );

  /**
   * Осуществляет пакетное чтение содержимого в контейнер.
   * <p>
   * В качестве правил выборки передаются параметры комбинированного фильтра, порождающий фильтр для строковых данных
   * {@link ITsFilter}&lt;<b>String</b>&gt;. Предполагается, что комбиноированный фильтр состоит из единичных фаильтров
   * {@link StdFilterStringMatcher}.
   * <p>
   * Если соответствующую часть не надо считывать, то в качестве аргумента следует передать
   * {@link ITsCombiFilterParams#NONE}, а если надо включить все, то {@link ITsCombiFilterParams#ALL}.
   * <p>
   * Использование опции:
   * <ul>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_INCLUDE_SYSTEM_ENTITIES} - читаемое хранилище может содержать
   * некторые служебные сущности, не имеющие отношение к предметной области (например, объекты сессии Sk-соединения).
   * Данная опция позволяет управлять тем, будут ли такие сущности включены в контейнер. Кокретный перечень сущностей,
   * которые считаются "системными", зависит от самого бекенда;</li>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_INCLUDE_CLASS_INFOS} - аргумент <code>aClassIdsFilter</code>
   * выбирает классы. Соответственно, предполагается, что в контейнер будут включены описания классов, объекты этих
   * классов (со значениями атрибутов) и связы выбранных объектов. Так вот, если этот признак сброшен, что
   * {@link IDpuContainer#classInfos()} останется пустым;</li>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_INCLUDE_OBJECTS} - аналогично вышеуказанному,
   * {@link IDpuContainer#objs()} останется пустым;</li>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_INCLUDE_LINKS} - аналогично вышеуказанному,
   * {@link IDpuContainer#links()} останется пустым;</li>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_ORPHAN_CLASSES} - указывает, как обрабатывать "сиротские" классы.
   * "Сиротскими" считаются те классы, у которых в {@link IDpuContainer#classInfos()} попали не все родители и/или не
   * все наследники. В зависимости от значения этого параметра, такие классы:
   * <ul>
   * <li>{@link EOrphanProcessing#NONE} - останутся как есть;</li>
   * <li>{@link EOrphanProcessing#REMOVE} - не будут включены в контейнер (и соответственно, пропадут объекты и
   * связи);</li>
   * <li>{@link EOrphanProcessing#ENRICH} - будут добавлены описания родительских и насоледных классов, но объекты будут
   * добавлены только самого класса и наследников, не родительских классов;</li>
   * </ul>
   * </li>
   * <li>{@link ISkBackendAddonBatchOperations#OPDEF_ORPHAN_LINKS} - указывает, как обрабатывать "сиротские" связи.
   * "Сиротскими" считаются те связи выбранных объектов, в которых в качестве правого объекта указан {@link Skid}, а
   * соответствующий объект отсутствует в выборке. В зависимости от значения этого параметра, такие паавые объекты:
   * <ul>
   * <li>{@link EOrphanProcessing#NONE} - остаются, как есть;</li>
   * <li>{@link EOrphanProcessing#REMOVE} - будут удалены из связей;</li>
   * <li>{@link EOrphanProcessing#ENRICH} - FIXME что делать? добавлять соответствующие объекты? а если их классы
   * отсутствуют в контейнере? не приведет ли это, что всё равно будет включено всё? TODO может сказать, что ENRICH не
   * используется?.</li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param aReadOptions {@link IOptionSet} - опции чтения (из выше перечисленных <code>OPDEF_XXX</code>)
   * @param aClassIdsFilter {@link ITsCombiFilterParams} - фильтр выборки классов по идентификаторам
   * @param aDataTypeIdsFilter {@link ITsCombiFilterParams} - фильтр выборки типов данных по идентификаторам
   * @param aClobIdsFilter {@link ITsCombiFilterParams} - фильтр выборки CLOB-ов по идентификаторам
   * @return {@link IDpuContainer} - считанное содержимое
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IDpuContainer batchRead( IOptionSet aReadOptions, ITsCombiFilterParams aClassIdsFilter,
      ITsCombiFilterParams aDataTypeIdsFilter, ITsCombiFilterParams aClobIdsFilter );
}
