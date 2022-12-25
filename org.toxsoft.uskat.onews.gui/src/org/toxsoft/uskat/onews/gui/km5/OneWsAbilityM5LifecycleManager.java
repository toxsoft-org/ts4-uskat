package org.toxsoft.uskat.onews.gui.km5;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * Lifecycle manager for {@link OneWsAbilityM5Model}.
 * <p>
 * This LM has no CRUD, only lists available abilities from {@link ISkOneWsService#listKnownAbilities()}.
 *
 * @author dima
 */
public class OneWsAbilityM5LifecycleManager
    extends M5LifecycleManager<IOneWsAbility, ISkConnection> {

  /**
   * Constructor.
   *
   * @param aModel {@link IM5Model} - the model
   * @param aMaster {@link ISkConnection} - master-object, the Sk-connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public OneWsAbilityM5LifecycleManager( IM5Model<IOneWsAbility> aModel, ISkConnection aMaster ) {
    super( aModel, false, false, false, true, aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  ISkOneWsService ows() {
    ISkOneWsService ows = (ISkOneWsService)master().coreApi().getService( ISkOneWsService.SERVICE_ID );
    return ows;
  }

  // ------------------------------------------------------------------------------------
  // M5LifecycleManager
  //

  @Override
  protected IList<IOneWsAbility> doListEntities() {
    return ows().listKnownAbilities();
  }
}
