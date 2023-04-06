package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Basic lifcycle manager of all KM5 modelled Sk-classes.
 * <p>
 * Like {@link KM5ModelBasic}, this is very basic implementation to be used with {@link KM5ModelBasic}. For subject area
 * classes use {@link KM5LifecycleManagerGeneric} instead.
 *
 * @see KM5LifecycleManagerGeneric
 * @see KM5ModelBasic
 * @author hazard157
 * @param <T> - modelled entity type
 * @param <M> - M5 master-object type
 */
public class KM5LifecycleManagerBasic<T extends ISkObject, M>
    extends M5LifecycleManager<T, M>
    implements ISkConnected {

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aCanCreate boolean - entity creation support flags
   * @param aCanEdit boolean - entity editing support flags
   * @param aCanRemove boolean - entity removingsupport flags
   * @param aEnumeratable boolean - entity listing support flags
   * @param aMaster &lt;M&gt; - master object, may be <code>null</code>
   */
  public KM5LifecycleManagerBasic( IM5Model<T> aModel, boolean aCanCreate, boolean aCanEdit, boolean aCanRemove,
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
