package org.toxsoft.uskat.onews.gui.panel;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.math.EAvCompareOp.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.onews.gui.ISkOneWsGuiSharedResources.*;
import static org.toxsoft.uskat.onews.lib.EOwsPermission.*;
import static org.toxsoft.uskat.onews.lib.IOneWsConstants.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.ctx.impl.*;
import org.toxsoft.core.tsgui.bricks.stdevents.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.utils.checkcoll.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.std.paramed.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.gui.glib.*;
import org.toxsoft.uskat.onews.gui.km5.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * OneWS profiles {@link IOneWsProfile} editor panel.
 * <p>
 * Contains {@link SashForm} with:
 * <ul>
 * <li>left - an editable list of profiles {@link ISkOneWsService#listProfiles()};</li>
 * <li>right, first tab - chekable tree of the abilities {@link ISkOneWsService#listKnownAbilities()};</li>
 * <li>right, second tab - chekable tree of {@link ISkUserService#listRoles()} with profile roles checked.</li>
 * </ul>
 *
 * @author hazard157
 */
public class PanelOneWsProfilesEditor
    extends AbstractSkStdEventsProducerLazyPanel<IOneWsProfile> {

  private IM5CollectionPanel<IOneWsProfile> profilesPanel  = null;
  private IM5CollectionPanel<IOneWsAbility> abilitiesPanel = null;
  private IM5CollectionPanel<ISkRole>       rolesPanel     = null;

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelOneWsProfilesEditor( ITsGuiContext aContext ) {
    this( aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelOneWsProfilesEditor( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext, aUsedConnId );
    selectionChangeEventHelper.addTsSelectionListener( this::whenProfileSelectionChanged );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private ISkOneWsService ows() {
    return skClobServ().coreApi().getService( ISkOneWsService.SERVICE_ID );
  }

  /**
   * Handles selection change in the profiles list on the left.
   * <p>
   * Has the same signature as {@link ITsSelectionChangeListener#onTsSelectionChanged(Object, Object)} to set as
   * listener of {@link IM5CollectionPanel#addTsSelectionListener(ITsSelectionChangeListener)}.
   *
   * @param aSource Object - the event source
   * @param aSelectedItem {@link IOneWsProfile} - selected profile or <code>null</code>
   */
  void whenProfileSelectionChanged( Object aSource, IOneWsProfile aSelectedItem ) {
    // update abilities panel
    abilitiesPanel.checkSupport().checksChangeEventer().pauseFiring();
    boolean enableEditing = false;
    // clear all checks
    abilitiesPanel.checkSupport().setAllItemsCheckState( false );
    // set only needed checks
    if( aSelectedItem != null ) {
      IListEdit<IOneWsAbility> checkedAbilities = new ElemArrayList<>();
      for( IOneWsAbility ability : ows().listKnownAbilities() ) {
        for( OneWsRule wsRule : aSelectedItem.rules() ) {
          if( wsRule.filter().accept( ability ) ) {
            if( wsRule.permission() == EOwsPermission.ALLOW ) {
              checkedAbilities.add( ability );
              continue;
            }
          }
        }
      }
      abilitiesPanel.checkSupport().setItemsCheckState( checkedAbilities, true );
      enableEditing = !aSelectedItem.isBuiltinProfile();
    }
    /**
     * FIXME disabling editing does not disable check state changing by mouse. Fix it!
     */
    abilitiesPanel.setEditable( enableEditing );
    abilitiesPanel.checkSupport().checksChangeEventer().resumeFiring( false );
    // update roles panel
    rolesPanel.checkSupport().checksChangeEventer().pauseFiring();
    enableEditing = false;
    // clear all checks
    rolesPanel.checkSupport().setAllItemsCheckState( false );
    // set only needed checks
    if( aSelectedItem != null ) {
      IListEdit<ISkRole> checkedRoles = new ElemArrayList<>();
      for( ISkRole r : skUserServ().listRoles() ) {
        IOneWsProfile p = ows().getProfileByRoleId( r.id() );
        if( aSelectedItem.id().equals( p.id() ) ) {
          checkedRoles.add( r );
        }
      }
      rolesPanel.checkSupport().setItemsCheckState( checkedRoles, true );
      enableEditing = true;
    }
    rolesPanel.setEditable( enableEditing );
    rolesPanel.checkSupport().checksChangeEventer().resumeFiring( false );
  }

  /**
   * Handles any ability chack state change in the abilities tree on the right.
   * <p>
   * Has the same signature as {@link IGenericChangeListener#onGenericChangeEvent(Object)} to set as listener of
   * {@link ITsCheckSupport#checksChangeEventer()}.
   *
   * @param aSource Object - the event source
   */
  void whenAbilitiesCheckStateChanged( Object aSource ) {
    IOneWsProfile currProfile = profilesPanel.selectedItem();
    if( currProfile == null ) {
      return;
    }
    IListEdit<OneWsRule> newRules = new ElemArrayList<>();
    /**
     * Test if all abilities are checked or no ability is checked and set one rule RULE_ALLOW_ALL or RULE_DENY_ALL
     * respectively. <br>
     * TODO what to do if there is no abilities known? #newRules will be empty and couse an exception in defineProfile()
     */
    IList<IOneWsAbility> checkedItems = abilitiesPanel.checkSupport().listCheckedItems( true );
    int allCount = ows().listKnownAbilities().size();
    int checkedCount = checkedItems.size();
    if( checkedCount == 0 ) {
      newRules.add( OneWsRule.RULE_DENY_ALL );
    }
    else {
      if( checkedCount == allCount ) {
        newRules.add( OneWsRule.RULE_ALLOW_ALL );
      }
      else {
        /**
         * For all checked items we'll create simple rule: ability with specified ID will be allowed.
         * <p>
         * This is too simple but working approcah used as for now (December 2022). However will have to work
         * onenhancement, more complex rules using not only ID of ability but also other #params() of it.
         */
        for( IOneWsAbility ability : checkedItems ) {
          ITsCombiFilterParams p = StdFilterOptionVsConst.makeFilterParams( TSID_ID, EQ, avStr( ability.id() ) );
          OneWsRule wsRule = new OneWsRule( ability.nmName(), p, ALLOW );
          newRules.add( wsRule );
        }
      }
    }
    ows().defineProfile( currProfile.id(), currProfile.attrs(), newRules );
  }

  /**
   * Handles any associated role chack state change in the roles tree on the right.
   * <p>
   * Has the same signature as {@link IGenericChangeListener#onGenericChangeEvent(Object)} to set as listener of
   * {@link ITsCheckSupport#checksChangeEventer()}.
   *
   * @param aSource Object - the event source
   */
  void whenRolesCheckStateChanged( Object aSource ) {
    IOneWsProfile leftSelectedProfile = profilesPanel.selectedItem();
    if( leftSelectedProfile == null ) {
      return;
    }
    // for each role if checked - associate to #leftSelectedProfile, else leave exising or reset to guest
    for( ISkRole role : skUserServ().listRoles() ) {
      IOneWsProfile currProfile = ows().getProfileByRoleId( role.id() );
      if( rolesPanel.checkSupport().getItemCheckState( role ) ) {
        // #role was just checked, associate #role <=> #leftSelectedProfile
        if( !currProfile.id().equals( leftSelectedProfile.id() ) ) {
          ows().setRoleProfile( role.id(), leftSelectedProfile.id() );
        }
      }
      else {
        // #role was just unchecked, reset #role, that is associate to the guest profile
        if( currProfile.id().equals( leftSelectedProfile.id() ) ) {
          ows().setRoleProfile( role.id(), OWS_ID_PROFILE_GUEST );
        }
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkStdEventsProducerLazyPanel
  //

  @Override
  protected void doInitGui( Composite aParent ) {
    // sash
    SashForm sfMain = new SashForm( aParent, SWT.HORIZONTAL );
    sfMain.setLayoutData( BorderLayout.CENTER );
    // left
    IM5Model<IOneWsProfile> profileModel = m5().getModel( OneWsProfileM5Model.MODEL_ID, IOneWsProfile.class );
    IM5LifecycleManager<IOneWsProfile> profileLm = profileModel.getLifecycleManager( skConn() );
    ITsGuiContext ctx1 = new TsGuiContext( tsContext() );
    profilesPanel = profileModel.panelCreator().createCollEditPanel( ctx1, profileLm.itemsProvider(), profileLm );
    profilesPanel.createControl( sfMain );
    profilesPanel.addTsSelectionListener( selectionChangeEventHelper );
    // right, TabFolder
    TabFolder rightFolder = new TabFolder( sfMain, SWT.TOP );
    // right, tab 1
    TabItem tabItem1 = new TabItem( rightFolder, SWT.NONE );
    tabItem1.setText( STR_N_TAB_ABILITIES );
    tabItem1.setToolTipText( STR_D_TAB_ABILITIES );
    IM5Model<IOneWsAbility> abilityModel = m5().getModel( OneWsAbilityM5Model.MODEL_ID, IOneWsAbility.class );
    IM5LifecycleManager<IOneWsAbility> abilityLm = abilityModel.getLifecycleManager( skConn() );
    ITsGuiContext ctx2 = new TsGuiContext( tsContext() );
    abilitiesPanel = abilityModel.panelCreator().createCollChecksPanel( ctx2, abilityLm.itemsProvider() );
    abilitiesPanel.createControl( rightFolder );
    tabItem1.setControl( abilitiesPanel.getControl() );
    abilitiesPanel.checkSupport().checksChangeEventer().addListener( this::whenAbilitiesCheckStateChanged );
    // right, tab 2
    TabItem tabItem2 = new TabItem( rightFolder, SWT.NONE );
    tabItem2.setText( STR_N_TAB_ROLES );
    tabItem2.setToolTipText( STR_D_TAB_ROLES );
    IM5Model<ISkRole> roleModel = m5().getModel( ISkRole.CLASS_ID, ISkRole.class );
    IM5LifecycleManager<ISkRole> roleLm = roleModel.getLifecycleManager( skConn() );
    ITsGuiContext ctx3 = new TsGuiContext( tsContext() );
    rolesPanel = roleModel.panelCreator().createCollChecksPanel( ctx3, roleLm.itemsProvider() );
    rolesPanel.createControl( rightFolder );
    tabItem2.setControl( rolesPanel.getControl() );
    rolesPanel.checkSupport().checksChangeEventer().addListener( this::whenRolesCheckStateChanged );
    // setup
    sfMain.setWeights( 5000, 5000 );
    abilitiesPanel.setEditable( false ); // TODO see FIXME above
    rolesPanel.setEditable( false ); // TODO see FIXME above
  }

  @Override
  protected IOneWsProfile doGetSelectedItem() {
    return profilesPanel.selectedItem();
  }

  @Override
  protected void doSetSelectedItem( IOneWsProfile aItem ) {
    profilesPanel.setSelectedItem( aItem );
  }

}
