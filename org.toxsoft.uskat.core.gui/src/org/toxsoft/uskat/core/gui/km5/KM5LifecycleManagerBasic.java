package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Basic lifecycle manager of all KM5 modeled Sk-classes.
 * <p>
 * Like {@link KM5ModelBasic}, this is very basic implementation to be used with {@link KM5ModelBasic}. For subject area
 * classes use {@link KM5LifecycleManagerGeneric} instead.
 *
 * @see KM5LifecycleManagerGeneric
 * @see KM5ModelBasic
 * @author hazard157
 * @param <T> - modeled entity type
 */
public class KM5LifecycleManagerBasic<T extends ISkObject>
    extends M5LifecycleManager<T, ISkConnection>
    implements ISkConnected {

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model}&lt;T&gt; - the model
   * @param aCanCreate boolean - entity creation support flags
   * @param aCanEdit boolean - entity editing support flags
   * @param aCanRemove boolean - entity removing support flags
   * @param aEnumeratable boolean - entity listing support flags
   * @param aMaster {@link ISkConnection} - master object, may be <code>null</code>
   */
  public KM5LifecycleManagerBasic( IM5Model<T> aModel, boolean aCanCreate, boolean aCanEdit, boolean aCanRemove,
      boolean aEnumeratable, ISkConnection aMaster ) {
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
  final public ISkConnection skConn() {
    return master();
  }

}
