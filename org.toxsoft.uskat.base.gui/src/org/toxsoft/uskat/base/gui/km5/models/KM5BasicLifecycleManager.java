package org.toxsoft.uskat.base.gui.km5.models;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Базовый класс для облегчения реализации менеджеров ЖЦ S5-сущностей.
 * <p>
 * По аналогии с {@link KM5BasicModel}, это низкоуровневый класс, для использования с моделями обычных {@link ISkObject}
 * предметной области, используйте {@link KM5GenericLifecycleManager}.
 *
 * @author goga
 * @param <T> - конкретный класс моделируемой S5-сущности
 * @param <M> - класс мастер-объекта
 */
public class KM5BasicLifecycleManager<T extends ISkObject, M>
    extends M5LifecycleManager<T, M>
    implements ISkConnected {

  /**
   * Конструктор.
   *
   * @param aModel {@link IM5Model} - модель
   * @param aCanCreate boolean - признак, что сущности можно создавать
   * @param aCanEdit boolean - признак, что сущности можно редактировать
   * @param aCanRemove boolean - признак, что сущности можно удалять
   * @param aEnumeratable boolean - признак, что сущности можне перечислть в {@link #itemsProvider}
   * @param aMaster &ltM&gt; - мастер-объект
   * @throws TsNullArgumentRtException aModel = null
   */
  public KM5BasicLifecycleManager( IM5Model<T> aModel, boolean aCanCreate, boolean aCanEdit, boolean aCanRemove,
      boolean aEnumeratable, M aMaster ) {
    super( aModel, aCanCreate, aCanEdit, aCanRemove, aEnumeratable, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected IList<T> doListEntities() {
    return skObjServ().listObjs( model().id(), true );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return tsContext().get( ISkConnection.class );
  }

}
