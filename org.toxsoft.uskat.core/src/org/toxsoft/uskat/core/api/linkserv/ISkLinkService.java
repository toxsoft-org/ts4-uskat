package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;

/**
 * Service to manage links between objects.
 *
 * @author goga
 */
public interface ISkLinkService
    extends ISkService {

  /**
   * Service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + "LinkService"; //$NON-NLS-1$

  // TODO TRANSLATE

  // FIXME review service API

  /**
   * Возвращает прямую связь.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта
   * @param aLinkId String - идентификатор связи
   * @return {@link IDtoLinkFwd} - прямая связь, не бывает <code>null</code>
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такой связи или такого объекта в системе
   */
  IDtoLinkFwd getLink( Skid aLeftSkid, String aLinkId );

  /**
   * Возвращает обратную связь.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор правого объекта
   * @return {@link IDtoLinkFwd} - обратная связь
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemNotFoundRtException нет такой связи или такого объекта в системе
   */
  IDtoLinkRev getLinkRev( String aClassId, String aLinkId, Skid aRightSkid );

  /**
   * Определяет список связанных объектов.
   * <p>
   * Метод сначала удаляет из связи правые объекты перечисленные в <code>aRemovedSkids</code>, а потом добавляет в связь
   * правые объекты, перечисленные в <code>aAddedSkids</code>.
   * <p>
   * Обратите внимание на <b>тотально</b> разный смысл двух значений аргумента <b><code>aRemovedSkids</code></b>:
   * <ul>
   * <li>значение <code>null</code> приводит к полному <b>удалению всех</b> связанных объектов;</li>
   * <li>значение {@link ISkidList#EMPTY} вообще не изменяет связь.</li>
   * </ul>
   * <p>
   * Один метод позволяет создать, удалить и редактировать связь. Для упрощения использования метода ниже определены
   * inline методы.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта
   * @param aLinkId String - идентификатор связи
   * @param aRemovedSkids {@link ISkidList} - удаляемые из связи объекты или <code>null</code> для удаления <b>всех</b>
   * @param aAddedSkids {@link ISkidList} - добавляемые в связь объекты
   * @throws TsNullArgumentRtException любой аргумент (кроме aRemovedSkids) = null
   * @throws TsItemNotFoundRtException не существует такой связи
   * @throws TsItemNotFoundRtException не существует одного из упомянутых объектов
   * @throws TsValidationFailedRtException не прошла какая-либо проверка {@link ISkLinkServiceValidator}
   */
  // TODO сделать void defineLink(IDpuLinkFwd aLink );
  void defineLink( Skid aLeftSkid, String aLinkId, ISkidList aRemovedSkids, ISkidList aAddedSkids );

  /**
   * Удаляет все связи объекта, как будто .
   * <p>
   * Обратите внимание, что удаления связей и задание связи с 0 объектами не одно и то же. Хотя, для одной связи
   * результатом обоих дествий будет пустой список правых объектов. Например, связи, которая должна содержать ровно один
   * объект, нельзя задавать 0 правых объектов - это нарушение правил. А удалить такую связь можно, фактически,
   * восстанавливая объект до состояния момента создания. Напомним, только что созданный методом
   * {@link ISkObjectService#defineObject(IDpuObject)} имеет только пустые связи.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта
   * @throws TsNullArgumentRtException любой аргумент (кроме aRemovedSkids) = null
   * @throws TsItemNotFoundRtException нет такого объекта
   */
  void removeLinks( Skid aLeftSkid );

  /**
   * Возвращает средство работы с событиями от службы.
   *
   * @return {@link ITsEventer} - средство работы с событиями от службы
   */
  ITsEventer<ISkLinkServiceListener> eventer();

  /**
   * Возвращает средсва валидации вызовов методов редактирования службы.
   *
   * @return {@link ITsValidationSupport} - поддержка валидации
   */
  ITsValidationSupport<ISkLinkServiceValidator> svs();

  // ------------------------------------------------------------------------------------
  // inline методы для удобства

  /**
   * Возвращает прямую связь.
   *
   * @param aLinkConcreteGwid {@link Gwid} - конкретный идентификатор связи
   * @return {@link IDtoLinkFwd} - прямая связь
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException аргумент не конкретный GWID связи
   * @throws TsItemNotFoundRtException нет такой связи или такого объекта в системе
   */
  default IDtoLinkFwd getLink( Gwid aLinkConcreteGwid ) {
    TsNullArgumentRtException.checkNull( aLinkConcreteGwid );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.isMulti() );
    TsIllegalArgumentRtException.checkTrue( aLinkConcreteGwid.kind() != EGwidKind.GW_LINK );
    return getLink( aLinkConcreteGwid.skid(), aLinkConcreteGwid.propId() );
  }

  /**
   * Задает связь.
   *
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта
   * @param aLinkId String - идентификатор связи
   * @param aNewSkids {@link ISkidList} - правые объекты связи
   * @throws TsNullArgumentRtException любой аргумент (кроме aRemovedSkids) = null
   * @throws TsItemNotFoundRtException не существует такой связи
   * @throws TsItemNotFoundRtException не существует одного из упомянутых объектов
   * @throws TsValidationFailedRtException не прошла какая-либо проверка {@link ISkLinkServiceValidator}
   */
  default void setLink( Skid aLeftSkid, String aLinkId, ISkidList aNewSkids ) {
    defineLink( aLeftSkid, aLinkId, null, aNewSkids );
  }

}
