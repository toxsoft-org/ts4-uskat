package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;

import ru.uskat.common.dpu.IDpuLinkFwd;
import ru.uskat.common.dpu.IDpuLinkRev;

/**
 * Backend предоставляемый для реализации функций доступа к управлению связями между объектами системы
 *
 * @author mvk
 */
public interface ISkBackendLinksManagement {

  /**
   * Находит связанные объекты.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IDpuLinkFwd} - связь (м.б. пустая) или <code>null</code> если нет такого класса/связи/объекта
   */
  IDpuLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid );

  /**
   * Возвращает запрошенную связь (связь "один-ко-многому").
   * <p>
   * Обратите внимание, что aClassId должен указывать на тот класс, в котором определена связь aLinkId, а не наследника.
   * Бекенд "тупой" - он ничего не знает о наследовании классов.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aLeftSkid {@link Skid} - идентификатор левого объекта связи
   * @return {@link IDpuLinkFwd} - связь (м.б. пустая)
   * @throws TsItemNotFoundRtException нет такого класса/связи/объекта
   */
  IDpuLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid );

  /**
   * Возвращает запрошенную обратную связь (связь "много-к-одному").
   * <p>
   * Обратите внимание, что aClassId должен указывать на тот класс, в котором определена связь aLinkId, а не наследника.
   * Бекенд "тупой" - он ничего не знает о наследовании классов.
   *
   * @param aClassId String - идентификатор класса связи
   * @param aLinkId String - идентификатор связи
   * @param aRightSkid {@link Skid} - идентификатор поавого объекта связи
   * @param aLeftClassIds {@link IStringList} - список ИД-ов классов левых объектов, пустой список = все классы
   * @return {@link IDpuLinkRev} - reverse link, may be empty
   */
  IDpuLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds );

  /**
   * Задает (или удаляет) связь.
   * <p>
   * Если список правых объектов пустой, то связь будет удалена.
   *
   * @param aLink {@link IDpuLinkFwd} - записываемая связь
   */
  void writeLink( IDpuLinkFwd aLink );

}
