package org.toxsoft.uskat.users.gui.km5;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.uskat.users.gui.ISkUsersGuiSharedResources.*;
import static org.toxsoft.uskat.users.gui.ISkUsersGuiConstants.*;

import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.dialogs.misc.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.viewers.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.panels.toolbar.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;
import org.toxsoft.uskat.users.gui.*;

/**
 * {@link IMultiPaneComponent} implementation for users collection viewer/editor.
 *
 * @author hazard157
 */
public class SkUserMpc
    extends MultiPaneComponentModown<ISkUser>
    implements ISkConnected {

  static final ITsNodeKind<ISkUser> NK_USER = new TsNodeKind<>( "LeafUser", ISkUser.class, false ); //$NON-NLS-1$
  static final ITsNodeKind<ISkRole> NK_ROLE = new TsNodeKind<>( "NodeRole", ISkRole.class, true );  //$NON-NLS-1$

  /**
   * Tree maker groups users by roles.
   *
   * @author hazard157
   */
  class TreeMakerByRole
      implements ITsTreeMaker<ISkUser> {

    // FIXME when hiding users also hide hidden roles

    // ------------------------------------------------------------------------------------
    // ITsTreeMaker
    //

    private IStringMapEdit<DefaultTsNode<ISkRole>> makeRolesRootsMap( ITsNode aRootNode ) {
      IStringMapEdit<DefaultTsNode<ISkRole>> retVal = new StringMap<>();
      IStridablesList<ISkRole> roles = skUserServ().listRoles();
      for( ISkRole role : roles ) {
        DefaultTsNode<ISkRole> roleNode = new DefaultTsNode<>( NK_ROLE, aRootNode, role );
        // присвоим красивую иконку и нормальное имя
        roleNode.setName( role.attrs().getStr( IM5Constants.FID_NAME ) );
        roleNode.setIconId( ICON_ROLE );
        retVal.put( role.id(), roleNode );
      }
      return retVal;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkUser> aUsers ) {
      IStringMapEdit<DefaultTsNode<ISkRole>> roots = makeRolesRootsMap( aRootNode );
      for( ISkUser user : aUsers ) {
        for( ISkRole role : user.listRoles() ) {
          DefaultTsNode<ISkRole> roleNode = roots.findByKey( role.id() );
          DefaultTsNode<ISkUser> userLeaf = new DefaultTsNode<>( NK_USER, roleNode, user );
          // присвоим красивую иконку и нормальное имя
          userLeaf.setName( user.attrs().getStr( IM5Constants.FID_NAME ) );
          userLeaf.setIconId( ICON_USER );
          roleNode.addNode( userLeaf );
        }
      }
      return (IList)roots.values();
    }

    @Override
    public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_USER;
    }

  }

  /**
   * Creates instance to edit entities.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aModel {@link IM5Model} - the model
   * @param aItemsProvider {@link IM5ItemsProvider} - the items provider or <code>null</code>
   * @param aLifecycleManager {@link IM5LifecycleManager} - the lifecycle manager or <code>null</code>
   */
  public SkUserMpc( ITsGuiContext aContext, IM5Model<ISkUser> aModel, IM5ItemsProvider<ISkUser> aItemsProvider,
      IM5LifecycleManager<ISkUser> aLifecycleManager ) {
    super( new M5TreeViewer<>( aContext, aModel, OPDEF_IS_SUPPORTS_CHECKS.getValue( aContext.params() ).asBool() ) );
    setItemProvider( aItemsProvider );
    setLifecycleManager( aLifecycleManager );
    //
    TreeModeInfo<ISkUser> tmiByRole = new TreeModeInfo<>( "ByRole", //$NON-NLS-1$
        STR_N_TMI_BY_ROLES, STR_D_TMI_BY_ROLES, ICON_ROLES_LIST, new TreeMakerByRole() );
    treeModeManager().addTreeMode( tmiByRole );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // MultiPaneComponentModown
  //

  @Override
  protected ITsToolbar doCreateToolbar( ITsGuiContext aContext, String aName, EIconSize aIconSize,
      IListEdit<ITsActionDef> aActs ) {
    aActs.add( ACDEF_SEPARATOR );
    aActs.add( ACDEF_CHANGE_PASSWORD );
    aActs.add( ACDEF_SEPARATOR );
    aActs.add( ACDEF_NO_HIDDEN_USERS );
    return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
  }

  @Override
  protected void doProcessAction( String aActionId ) {
    ISkUser selUser = selectedItem();
    switch( aActionId ) {
      case ISkUsersGuiConstants.ACTID_NO_HIDDEN_USERS: {
        if( toolbar().isActionChecked( ISkUsersGuiConstants.ACTID_NO_HIDDEN_USERS ) ) {
          tree().filterManager().setFilter( aObj -> !aObj.isHidden() );
        }
        else {
          tree().filterManager().setFilter( ITsFilter.ALL );
        }
        refresh();
        break;
      }
      case ACTID_CHANGE_PASSWORD: {
        if( selUser != null ) {
          String password = DialogEnterPassword.enterPassword( tsContext(), skUserServ().passwordValidator() );
          if( password != null ) {
            skUserServ().setUserPassword( selUser.login(), password );
          }
        }
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aActionId );
    }
  }

  @Override
  protected void doUpdateActionsState( boolean aIsAlive, boolean aIsSel, ISkUser aSel ) {
    toolbar().setActionEnabled( ACTID_CHANGE_PASSWORD, aIsSel );
  }

  @Override
  protected ISkUser doAddItem() {
    /**
     * Method should be overrident because password should be entered seperately from KSUser M5-model properties and
     * hence user creation can not be done through the lifecycle manager.
     */
    // edit user object to be created as IM5Bunch
    ITsDialogInfo cdi = TsDialogInfo.forCreateEntity( tsContext() );
    IM5BunchEdit<ISkUser> initVals = new M5BunchEdit<>( model() );
    doAdjustEntityCreationInitialValues( initVals );
    IM5Bunch<ISkUser> bunch = M5GuiUtils.editBunch( tsContext(), model(), initVals, cdi, lifecycleManager() );
    if( bunch == null ) {
      return null;
    }
    IDtoFullObject dtoUser = SkUserM5LifecycleManager.makeUserDto( bunch, coreApi() );
    // specify password
    String password = DialogEnterPassword.enterPassword( tsContext(), skUserServ().passwordValidator() );
    if( password == null ) {
      return null;
    }
    // create user by service, not by lifecycle manager
    return skUserServ().createUser( dtoUser, password );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return model().domain().tsContext().get( ISkConnection.class );
  }

}
