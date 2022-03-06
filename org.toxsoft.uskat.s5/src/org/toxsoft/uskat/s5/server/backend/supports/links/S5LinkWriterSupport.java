package org.toxsoft.uskat.s5.server.backend.supports.links;

import java.lang.reflect.Constructor;

import javax.persistence.EntityManager;

import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.IMapEdit;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.IDpuLinkFwd;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Вспомогательный класс записи связей между объектами
 *
 * @author mvk
 */
final class S5LinkWriterSupport {

  /**
   * Менеджер постоянства
   */
  final EntityManager entityManager;

  /**
   * Читатель системного описания
   */
  final ISkSysdescrReader sysdescrReader;

  /**
   * Карта описаний классов по их идентификаторам.
   * <p>
   * Ключ: идентификатор класса левого объекта связи;<br>
   * Значение: описание класса
   */
  final IStringMapEdit<ISkClassInfo> classesByIds = new StringMap<>();

  /**
   * Карта классов реализаций ПРЯМЫХ связей объектов по идентификаторам их классов;
   * <p>
   * Ключ: Строковый идентификатор класса левого объекта связи;<br>
   * Значение: Класс реализации ПРЯМОЙ связи объекта.
   */
  final IStringMapEdit<Class<S5LinkFwdEntity>> implLinkFwdByIds = new StringMap<>();

  /**
   * Карта классов реализаций ОБРАТНЫХ связей объектов по идентификаторам их классов.
   * <p>
   * Ключ: Строковый идентификатор класса правого объекта связи;<br>
   * Значение: Класс реализации ОБРАТНОЙ связи объекта.
   */
  final IStringMapEdit<Class<S5LinkRevEntity>> implLinkRevByIds = new StringMap<>();

  /**
   * Карта конструкторов прямых связей.
   * <p>
   * Ключ: идентификатор класса левого объекта связи;<br>
   * Значение: конструктор обратной связи.
   */
  final IStringMapEdit<Constructor<S5LinkFwdEntity>> linkFwdContructors = new StringMap<>();

  /**
   * Карта конструкторов обратных связей.
   * <p>
   * Ключ: идентификатор класса правого объекта связи;<br>
   * Значение: конструктор обратной связи.
   */
  final IStringMapEdit<Constructor<S5LinkRevEntity>> linkRevContructors = new StringMap<>();

  /**
   * Карта обновляемых ПРЯМЫХ связей объектов по классам левого объекта связи.
   * <p>
   * Ключ: описание класса левого объекта связи;<br>
   * Значение: список пар изменившихся связей. Левое - старая редакция связи (может быть {@link S5LinkFwdEntity#NULL} ),
   * правое - новое
   */
  final IMapEdit<ISkClassInfo, IListEdit<Pair<IDpuLinkFwd, IDpuLinkFwd>>> updatedLinks = new ElemMap<>();

  /**
   * Конструктор
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5LinkWriterSupport( EntityManager aEntityManager, ISkSysdescrReader aSysdescrReader ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSysdescrReader );
    entityManager = aEntityManager;
    sysdescrReader = aSysdescrReader;
  }
}
