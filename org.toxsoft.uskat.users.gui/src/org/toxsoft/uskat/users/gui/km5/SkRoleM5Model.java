package org.toxsoft.uskat.users.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.users.gui.ISkUsersGuiSharedResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of {@link ISkRole}.
 *
 * @author hazard157
 */
class SkRoleM5Model
    extends KM5ModelBasic<ISkRole> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkRoleM5Model( ISkConnection aConn ) {
    super( ISkRole.CLASS_ID, ISkRole.class, aConn );
    setNameAndDescription( STR_N_ROLE, STR_D_ROLE );
    ISkClassInfo cinf = skSysdescr().getClassInfo( ISkRole.CLASS_ID );
    // attributes
    KM5AttributeFieldDef<ISkRole> roleId = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( AID_STRID ) );
    roleId.setFlags( M5FF_INVARIANT );
    roleId.setNameAndDescription( STR_N_ROLE_ID, STR_D_ROLE_ID );
    KM5AttributeFieldDef<ISkRole> active = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( ATRID_ROLE_IS_ENABLED ) );
    active.setNameAndDescription( STR_N_FDEF_ACTIVE, STR_D_FDEF_ACTIVE );
    active.setFlags( M5FF_COLUMN );
    KM5AttributeFieldDef<ISkRole> hidden = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( ATRID_ROLE_IS_HIDDEN ) );
    hidden.setNameAndDescription( STR_N_FDEF_HIDDEN, STR_D_FDEF_HIDDEN );
    hidden.setFlags( M5FF_COLUMN );
    // fields
    NAME.setNameAndDescription( STR_N_FDEF_NAME, STR_D_FDEF_NAME );
    DESCRIPTION.setNameAndDescription( STR_N_FDEF_DESCR, STR_D_FDEF_DESCR );
    addFieldDefs( roleId, NAME, active, hidden, DESCRIPTION );
  }

  @Override
  protected IM5LifecycleManager<ISkRole> doCreateDefaultLifecycleManager() {
    ISkConnection master = domain().tsContext().get( ISkConnection.class );
    return new SkRoleM5LifecycleManager( this, master );
  }

  @Override
  protected IM5LifecycleManager<ISkRole> doCreateLifecycleManager( Object aMaster ) {
    ISkConnection master = ISkConnection.class.cast( aMaster );
    return new SkRoleM5LifecycleManager( this, master );
  }

}
