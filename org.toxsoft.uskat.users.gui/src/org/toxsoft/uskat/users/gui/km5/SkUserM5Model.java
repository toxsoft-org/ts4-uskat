package org.toxsoft.uskat.users.gui.km5;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.users.gui.ISkUsersGuiSharedResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of {@link ISkUser}.
 *
 * @author hazard157
 */
class SkUserM5Model
    extends KM5ModelBasic<ISkUser> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - the connection
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkUserM5Model( ISkConnection aConn ) {
    super( ISkUser.CLASS_ID, ISkUser.class, aConn );
    setNameAndDescription( STR_N_USER, STR_D_USER );
    // attributes
    ISkClassInfo cinf = skSysdescr().getClassInfo( ISkUser.CLASS_ID );
    KM5AttributeFieldDef<ISkUser> login = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( AID_STRID ) );
    login.setFlags( M5FF_INVARIANT );
    login.setNameAndDescription( STR_N_FDEF_LOGIN, STR_D_FDEF_LOGIN );
    KM5AttributeFieldDef<ISkUser> active = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( ATRID_USER_IS_ENABLED ) );
    active.setNameAndDescription( STR_N_FDEF_ACTIVE, STR_D_FDEF_ACTIVE );
    active.setFlags( M5FF_COLUMN );
    KM5AttributeFieldDef<ISkUser> hidden = //
        new KM5AttributeFieldDef<>( cinf.attrs().list().getByKey( ATRID_USER_IS_HIDDEN ) );
    hidden.setNameAndDescription( STR_N_FDEF_HIDDEN, STR_D_FDEF_HIDDEN );
    hidden.setFlags( M5FF_COLUMN );
    // links
    KM5MultiLinkFieldDef roles = //
        new KM5MultiLinkFieldDef( cinf.links().list().getByKey( LNKID_USER_ROLES ) );
    NAME.setNameAndDescription( STR_N_FDEF_NAME, STR_D_FDEF_NAME );
    DESCRIPTION.setNameAndDescription( STR_N_FDEF_DESCR, STR_D_FDEF_DESCR );
    // add fields
    addFieldDefs( login, NAME, active, hidden, DESCRIPTION, roles );
    // panels creator
    setPanelCreator( new M5DefaultPanelCreator<>() {

      protected IM5CollectionPanel<ISkUser> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<ISkUser> aItemsProvider, IM5LifecycleManager<ISkUser> aLifecycleManager ) {
        OPDEF_IS_SUPPORTS_TREE.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<ISkUser> mpc = new SkUserMpc( aContext, model(), aItemsProvider, aLifecycleManager );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }
    } );
  }

  @Override
  protected IM5LifecycleManager<ISkUser> doCreateDefaultLifecycleManager() {
    ISkConnection master = domain().tsContext().get( ISkConnection.class );
    return new SkUserM5LifecycleManager( this, master );
  }

  @Override
  protected IM5LifecycleManager<ISkUser> doCreateLifecycleManager( Object aMaster ) {
    ISkConnection master = ISkConnection.class.cast( aMaster );
    return new SkUserM5LifecycleManager( this, master );
  }

}
