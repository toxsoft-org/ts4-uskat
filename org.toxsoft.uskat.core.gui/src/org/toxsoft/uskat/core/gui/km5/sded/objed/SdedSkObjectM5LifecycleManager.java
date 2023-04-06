package org.toxsoft.uskat.core.gui.km5.sded.objed;

import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * LM class for this model.
 * <p>
 *
 * @author dima
 */
class SdedSkObjectM5LifecycleManager
    extends M5LifecycleManager<ISkObject, ISkConnection>
    implements ISkConnected {

  public SdedSkObjectM5LifecycleManager( IM5Model<ISkObject> aModel, ISkConnection aMaster ) {
    super( aModel, true, true, true, true, aMaster );
    TsNullArgumentRtException.checkNull( aMaster );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  @Override
  protected ValidationResult doBeforeRemove( ISkObject aEntity ) {
    return skSysdescr().svs().validator().canRemoveClass( aEntity.id() );
  }

  @Override
  protected void doRemove( ISkObject aEntity ) {
    skSysdescr().removeClass( aEntity.id() );
  }

  @Override
  protected IList<ISkObject> doListEntities() {
    return master().coreApi().objService().listObjs( "SkObject", true );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return master();
  }

}
