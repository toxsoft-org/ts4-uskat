package org.toxsoft.uskat.base.gui.km5.models;

import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Моедль непосредственно объектов класса {@link ISkObject}.
 * <p>
 * Внимание: реализация моделей Sk-сущностей должна наследоватья не от этого класса, а от {@link KM5GenericM5Model}.
 *
 * @author goga
 */
public final class SkObjectKM5Model
    extends KM5BasicModel<ISkObject> {

  // TODO надо доработать модель SkObjectM5Model

  /**
   * Конструктор.
   *
   * @param aConn {@link ISkConnection} - соединение с сервером, используемое моделью
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   */
  public SkObjectKM5Model( ISkConnection aConn ) {
    super( IGwHardConstants.GW_ROOT_CLASS_ID, ISkObject.class, aConn );
    addFieldDefs( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
  }

}
