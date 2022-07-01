package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassHierarchyExplorer;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrDtoReader;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

/**
 * Поддержка доступа к системному описанию
 *
 * @author mvk
 */
@Local
public interface IS5BackendSysDescrSingleton
    extends ISkSysdescrDtoReader, ISkClassHierarchyExplorer, IS5BackendSupportSingleton {

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
   * Редактирует список классов.
   *
   * @param aRemoveClassIdsOrNull {@link IStringList} - список идентификаторов удаляемых классов или <code>null</code>
   *          для удаления <b>всех</b> классов
   * @param aUpdateClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - добавляемые/обновляемые классы
   * @throws TsNullArgumentRtException aNewlyDefinedClassInfos = null
   */
  void writeClassInfos( IStringList aRemoveClassIdsOrNull, IStridablesList<IDtoClassInfo> aUpdateClassInfos );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над классами системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   * <p>
   * Первыми вызываются интерсепторы с высшим приоритетом, потом с низшим при возникновении событий:
   * <ul>
   * <li>{@link IS5ClassesInterceptor#beforeCreateClass(IDtoClassInfo)};</li>
   * <li>{@link IS5ClassesInterceptor#beforeUpdateClass(IDtoClassInfo, IDtoClassInfo, IStridablesList)};</li>
   * <li>{@link IS5ClassesInterceptor#beforeDeleteClass(IDtoClassInfo)}.</li>
   * </ul>
   * Первыми вызываются интерсепторы с низшим приоритетом, потом с высшим при возникновении событий:
   * <ul>
   * <li>{@link IS5ClassesInterceptor#afterCreateClass(IDtoClassInfo)};</li>
   * <li>{@link IS5ClassesInterceptor#afterUpdateClass(IDtoClassInfo, IDtoClassInfo, IStridablesList)};</li>
   * <li>{@link IS5ClassesInterceptor#afterDeleteClass(IDtoClassInfo)}.</li>
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
