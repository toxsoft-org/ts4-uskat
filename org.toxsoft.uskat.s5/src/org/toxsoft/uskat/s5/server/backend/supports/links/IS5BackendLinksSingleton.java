package org.toxsoft.uskat.s5.server.backend.supports.links;

import java.util.List;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.impl.ISkBackendLinksManagement;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSupportSingleton;

import ru.uskat.common.dpu.IDpuLinkFwd;

/**
 * Локальный интерфейс синглетона {@link ISkBackendLinksManagement} предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendLinksSingleton
    extends ISkBackendLinksManagement, IS5BackendSupportSingleton {

  /**
   * Возвращает все ПРЯМЫЕ связи всех объектов указанного класса без учета наследников
   *
   * @param aClassId String - идентификатор класса левого объекта связи
   * @return {@link List}&lt;{@link IDpuLinkFwd}&gt; список прямых связей (один ко многим)
   * @throws TsNullArgumentRtException аргумент = null
   */
  List<IDpuLinkFwd> getLinks( String aClassId );

  /**
   * Возвращает ПРЯМЫЕ связи всех объектов указанного класса без учета наследников
   *
   * @param aClassId String - идентификатор класса левого объекта связи
   * @param aLinkId String - идентификатор связи
   * @return {@link List}&lt;{@link IDpuLinkFwd}&gt; список прямых связей (один ко многим)
   * @throws TsNullArgumentRtException аргумент = null
   */
  List<IDpuLinkFwd> getLinks( String aClassId, String aLinkId );

  /**
   * Задает (или удаляет) связи между объектами.
   * <p>
   * Если список правых объектов пустой, то связь будет удалена.
   *
   * @param aLinks {@link IList}&lt;{@link IDpuLinkFwd}&gt; - список устанавливаемых связей между объектами
   * @param aInterceptionEnabled boolean <b>true</b>перехват разрешен; <b>false</b>перехват запрещен.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void writeLinks( IList<IDpuLinkFwd> aLinks, boolean aInterceptionEnabled );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над связями между объектами системы.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5LinksInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addLinksInterceptor( IS5LinksInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над связями между объектами системы.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5LinksInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeLinksInterceptor( IS5LinksInterceptor aInterceptor );

}
