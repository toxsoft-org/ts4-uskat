package org.toxsoft.uskat.s5.server.backend.supports.objects;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.objserv.ISkObjectService;

/**
 * Локальный интерфейс синглетона поддержки службы {@link ISkObjectService} предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendObjectsSingleton
    extends IS5BackendSupportSingleton {

  /**
   * Найти объект с указанным идентификатором
   *
   * @param aSkid {@link Skid} идентификатор объекта
   * @return {@link IDpuObject} данные объекта. null: объект не найден
   */
  IDpuObject findObject( Skid aSkid );

  /**
   * Возвращает список объектов указанных классов
   *
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @return {@link IList}&lt;{link IDpuObject}&gt; список найденных объектов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException класс не найден
   */
  IList<IDpuObject> readObjects( IStringList aClassIds );

  /**
   * Возвращает список объектов с указанными идентификаторами
   * <p>
   * Если указан идентификатор несуществующего объекта, то он молча игнорируется
   *
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @return {@link IList}&lt;{link IDpuObject}&gt; список найденных объектов
   * @throws TsNullArgumentRtException аргумент = null
   */
  IList<IDpuObject> readObjectsByIds( ISkidList aSkids );

  /**
   * Сохранить/обновить/удалить объекты(значения их атрибутов) системы
   * <p>
   * Если в списке удаляемых объектов aRemovedSkids есть несуществующие объекты, то они игнорируются.
   *
   * @param aFrontend {@link IS5FrontendRear} frontend выполняющий операцию по изменению объектов системы
   * @param aRemovedSkids {@link ISkidList} список идентификаторов удаляемых объектов
   * @param aObjects {@link IList}&lt; {@link IDpuObject}&gt; список создаваемых или обновляемых объектов
   * @param aInterceptable boolean <b>true</b>перехват разрешен; <b>false</b>перехват запрещен.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый тип значения атрибутов объекта
   */
  void writeObjects( IS5FrontendRear aFrontend, ISkidList aRemovedSkids, IList<IDpuObject> aObjects,
      boolean aInterceptable );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над объектами системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5ObjectsInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addObjectsInterceptor( IS5ObjectsInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над объектами системы.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5ObjectsInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeObjectsInterceptor( IS5ObjectsInterceptor aInterceptor );

}
