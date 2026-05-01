package org.toxsoft.uskat.core.gui.km5.sded2.skobj;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded2.skobj.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link MultiPaneComponent} implementation for panels of the model {@link Sded2SkObjectM5Model}.
 * <p>
 * Note: his method expects that {@link #lifecycleManager()} is of concrete class
 * {@link Sded2SkObjectM5LifecycleManager} which has method {@link Sded2SkObjectM5LifecycleManager#classId() classId()}.
 * defined. THence, this panel displays the objects of the single class, without subclasses.
 * <p>
 * This MPC displays collection of {@link ISkObject} modeled by {@link Sded2SkObjectM5Model#MODEL_ID}. However, wHen
 * invoking CRUD operation methods {@link #doAddItem()}, {@link #doAddCopyItem(ISkObject)},
 * {@link #doEditItem(ISkObject)} and {@link #doRemoveItem(ISkObject)}, this implementation uses Sk-class specific
 * M5-model for object editing.
 * <p>
 * So different models are used for objects list display and for objects removal.
 *
 * @author hazard157
 */
class Sded2SkObjectMpc
    extends MultiPaneComponentModown<ISkObject> {

  public Sded2SkObjectMpc( ITsGuiContext aContext, IM5Model<ISkObject> aModel,
      IM5ItemsProvider<ISkObject> aItemsProvider ) {
    super( aContext, aModel, aItemsProvider );
  }

  public Sded2SkObjectMpc( ITsGuiContext aContext, IM5Model<ISkObject> aModel,
      IM5ItemsProvider<ISkObject> aItemsProvider, IM5LifecycleManager<ISkObject> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Returns the class ID and lifecycle manager specific to the Sk-class of the edited objects.
   * <p>
   * This method expects that {@link #lifecycleManager()} is of concrete class {@link Sded2SkObjectM5LifecycleManager}
   * which has method {@link Sded2SkObjectM5LifecycleManager#classId() classId()} defined.
   *
   * @return {@link Pair}&gt;String;{@link IM5LifecycleManager}&lt; - pair "class ID" - "LM of the lass ID"
   */
  private Pair<String, IM5LifecycleManager<ISkObject>> getSkClassSpecificData() {
    if( lifecycleManager() instanceof Sded2SkObjectM5LifecycleManager selfLm ) {
      String classId = selfLm.classId();
      // important: get the same M5 domain as used by the M5-model
      IM5Domain m5 = model().domain();
      IM5Model<ISkObject> specificModel = m5.getModel( classId, ISkObject.class );
      IM5LifecycleManager<ISkObject> specificLm = specificModel.getLifecycleManager( selfLm.master() );
      return new Pair<>( selfLm.classId(), specificLm );
    }
    // TODO move to L10n
    throw new TsInternalErrorRtException( FMT_MPC_EXPECTS_SPECIFIC_LM, this.getClass().getSimpleName(),
        Sded2SkObjectM5LifecycleManager.class.getSimpleName() );
  }

  // ------------------------------------------------------------------------------------
  // MultiPaneComponentModown
  //

  @Override
  protected ISkObject doAddItem() {
    ITsDialogInfo cdi = doCreateDialogInfoToAddItem();
    Pair<String, IM5LifecycleManager<ISkObject>> p = getSkClassSpecificData();
    IM5LifecycleManager<ISkObject> lm = p.right();
    IM5BunchEdit<ISkObject> initVals = lm.createNewItemValues();
    initVals.set( AID_CLASS_ID, avStr( p.left() ) );
    return M5GuiUtils.askCreate( tsContext(), lm.model(), initVals, cdi, lm );
  }

  @Override
  protected ISkObject doAddCopyItem( ISkObject aSrcItem ) {
    ITsDialogInfo cdi = doCreateDialogInfoToAddItem();
    Pair<String, IM5LifecycleManager<ISkObject>> p = getSkClassSpecificData();
    IM5LifecycleManager<ISkObject> lm = p.right();
    IM5BunchEdit<ISkObject> initVals = new M5BunchEdit<>( lm.model() );
    initVals.fillFrom( aSrcItem, false ); // leave originalEntity = null
    initVals.set( AID_CLASS_ID, avStr( p.left() ) );
    String copyStrid = StridUtils.createIdPathCopy( initVals.getAsAv( AID_STRID ).asString() );
    initVals.set( AID_STRID, avStr( copyStrid ) );
    return M5GuiUtils.askCreate( tsContext(), lm.model(), initVals, cdi, lm );
  }

  @Override
  protected ISkObject doEditItem( ISkObject aItem ) {
    ITsDialogInfo cdi = doCreateDialogInfoToEditItem( aItem );
    Pair<String, IM5LifecycleManager<ISkObject>> p = getSkClassSpecificData();
    IM5LifecycleManager<ISkObject> lm = p.right();
    return M5GuiUtils.askEdit( tsContext(), lm.model(), aItem, cdi, lm );
  }

  @Override
  protected boolean doRemoveItem( ISkObject aItem ) {
    Pair<String, IM5LifecycleManager<ISkObject>> p = getSkClassSpecificData();
    return M5GuiUtils.askRemove( tsContext(), model(), aItem, getShell(), p.right() );
  }

}
