package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.BackendAddonBase;
import org.toxsoft.uskat.core.backend.api.IBackendAddon;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Base implementation of local {@link IBackendAddon} for s5 backend.
 *
 * @author mvk
 */
public abstract class S5AbstractBackendAddonLocal
    extends BackendAddonBase<IS5BackendLocal>
    implements IS5BackendAddonLocal {

  /**
   * Constructor for subclasses.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @param aInfo {@link IStridable} - the addon info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException section name is not an IDpath
   */
  protected S5AbstractBackendAddonLocal( IS5BackendLocal aOwner, IStridable aInfo ) {
    super( aOwner, aInfo );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendAddonLocal
  //
  @Override
  public final IS5FrontendRear frontend() {
    return (IS5FrontendRear)owner().frontend();
  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // nop
  }

  @Override
  public void doJob() {
    // nop
  }

  @Override
  public abstract void close();

  // ------------------------------------------------------------------------------------
  // Package API
  //

  // /**
  // * Implementation must remove entitites related to the specified class IDs from the memory.
  // * <p>
  // * This method is called immediately before last save to the storage and implements MemText backend ability not to
  // * store garbage objects in the premanent storage (like connection session objects). However, saves during normal
  // * works will save all objects.
  // * <p>
  // * Method in base class does nothing there is no need to call superclass method when overriding.
  // *
  // * @param aClassIds {@link IStringList} - list of entities class IDs to be removed
  // */
  // void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
  // // nop
  // }

  // ------------------------------------------------------------------------------------
  // API for descendans
  //

  // /**
  // * Simply calls {@link MtbAbstractBackend#internalCheck()},
  // * <p>
  // * This method must be the first call in any API calls of the addon.
  // */
  // protected void internalCheck() {
  // owner().internalCheck();
  // }
  //
  // /**
  // * Simply calls {@link MtbAbstractBackend#setChanged()},
  // */
  // protected void setChanged() {
  // owner().setChanged();
  // }

}
