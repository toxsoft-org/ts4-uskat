package org.toxsoft.uskat.core.devapi.transactions;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link IDtoObject} rivet manager.
 *
 * @author mvk
 */
public interface IDtoObjectRivetManager {

  /**
   * Добавляет склепки указанного объекта на другие объекты.
   *
   * @param aObj {@link IDtoObject} добавленный объект.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  int createRivets( IDtoObject aObj );

  /**
   * Удаляет склепки указанного объекта на другие объекты.
   *
   * @param aObj {@link IDtoObject} удаляемый объект.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  int removeRivets( IDtoObject aObj );

  /**
   * Обновляет склепки указанного объекта на другие объекты.
   *
   * @param aPrevObj {@link IDtoObject} предыдущее состояние объекта.
   * @param aNewObj {@link IDtoObject} новое состояние объекта.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException предыдущее и новое состояние должны принадлежать одному объекту
   */
  int updateRivets( IDtoObject aPrevObj, IDtoObject aNewObj );

}
