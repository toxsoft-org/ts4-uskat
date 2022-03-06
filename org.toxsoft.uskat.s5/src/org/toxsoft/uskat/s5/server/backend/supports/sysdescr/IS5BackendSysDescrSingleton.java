package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.core.api.sysdescr.ISkSysdescrDpuReader;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;
import ru.uskat.core.impl.SkGwidUtils.ISkClassHierarchyProvider;

/**
 * Поддержка доступа к системному описанию
 *
 * @author mvk
 */
@Local
public interface IS5BackendSysDescrSingleton
    extends ISkSysdescrDpuReader, IS5BackendSupportSingleton, ISkClassHierarchyProvider {

  // ------------------------------------------------------------------------------------
  // Чтение системного описания
  //
  /**
   * Возвращает читателя данных системного описания
   *
   * @return {@link ISkSysdescrReader} читатель данных
   */
  ISkSysdescrReader getReader();

  // ------------------------------------------------------------------------------------
  // Изменение системного описания
  //

  /**
   * Редактирует список зарегистрированных типов данных.
   *
   * @param aRemoveTypeIdsOrNull {@link IStringList} - список идентификаторов удаляемых типов или <code>null</code> для
   *          удаления <b>всех</b> типов данных.
   * @param aNewlyDefinedTypeInfos {@link IList}&lt;{@link IDpuSdTypeInfo}&gt; - добавляемые/обновляемые типы данных
   * @throws TsNullArgumentRtException aNewlyDefinedTypeInfos = null
   */
  void writeTypeInfos( IStringList aRemoveTypeIdsOrNull, IList<IDpuSdTypeInfo> aNewlyDefinedTypeInfos );

  /**
   * Редактирует список классов.
   *
   * @param aRemoveClassIdsOrNull {@link IStringList} - список идентификаторов удаляемых классов или <code>null</code>
   *          для удаления <b>всех</b> классов
   * @param aUpdateClassInfos {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; - добавляемые/обновляемые классы
   * @throws TsNullArgumentRtException aNewlyDefinedClassInfos = null
   */
  void writeClassInfos( IStringList aRemoveClassIdsOrNull, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos );

  // ------------------------------------------------------------------------------------
  // Дополнительные, необходимые функции
  //

  /**
   * Определяет, является ли aParentClassId предком класса aClassId.
   * <p>
   * Класс aParentClassId считается предком, если среди цепочки родителей класса aClassId есть aParentClassId.
   *
   * @param aParentClassId String - идентификатор родительского класса
   * @param aClassId String - идентификатор проверяемого класса
   * @return boolean - признак, что класс aParentClassId является предком aClassId<br>
   *         <b>true</b> - aParentClassId является предком aClassId;<br>
   *         <b>false</b> - среди потомков aParentClassId нет класса aClassId.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такого класса
   */
  boolean isAncestor( String aParentClassId, String aClassId );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над типами системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5TypesInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addTypeInterceptor( IS5TypesInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над типами системы.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5TypesInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeTypeInterceptor( IS5TypesInterceptor aInterceptor );

  /**
   * Добавляет перехватчика операций проводимых над классами системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   * <p>
   * Первыми вызываются интерсепторы с высшим приоритетом, потом с низшим при возникновении событий:
   * <ul>
   * <li>{@link IS5ClassesInterceptor#beforeCreateClass(IDpuSdClassInfo)};</li>
   * <li>{@link IS5ClassesInterceptor#beforeUpdateClass(IDpuSdClassInfo, IDpuSdClassInfo, IStridablesList)};</li>
   * <li>{@link IS5ClassesInterceptor#beforeDeleteClass(IDpuSdClassInfo)}.</li>
   * </ul>
   * Первыми вызываются интерсепторы с низшим приоритетом, потом с высшим при возникновении событий:
   * <ul>
   * <li>{@link IS5ClassesInterceptor#afterCreateClass(IDpuSdClassInfo)};</li>
   * <li>{@link IS5ClassesInterceptor#afterUpdateClass(IDpuSdClassInfo, IDpuSdClassInfo, IStridablesList)};</li>
   * <li>{@link IS5ClassesInterceptor#afterDeleteClass(IDpuSdClassInfo)}.</li>
   * </ul>
   *
   * @param aInterceptor {@link IS5ClassesInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addClassInterceptor( IS5ClassesInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над классами системы.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5ClassesInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeClassInterceptor( IS5ClassesInterceptor aInterceptor );
}
