package org.toxsoft.uskat.onews.gui.panel;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.math.EAvCompareOp.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.onews.lib.EOwsPermission.*;

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
import org.toxsoft.uskat.base.gui.glib.*;
import org.toxsoft.uskat.onews.gui.km5.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * OneWS profiles {@link IOneWsProfile} editor panel.
 * <p>
 * Contains {@link SashForm} with:
 * <ul>
 * <li>left - an editable list of profiles {@link ISkOneWsService#listProfiles()};</li>
 * <li>right - chekable tree of the abilities {@link ISkOneWsService#listKnownAbilities()}.</li>
 * </ul>
 *
 * @author hazard157
 */
public class PanelOneWsProfilesEditor
    extends AbstractSkStdEventsProducerLazyPanel<IOneWsProfile> {

  private IM5CollectionPanel<IOneWsProfile> profilesPanel  = null;
  private IM5CollectionPanel<IOneWsAbility> abilitiesPanel = null;

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
     * For all checked items we'll create simple rule: ability with specified ID will be allowed.
     * <p>
     * TODO This is too simple but working approcah used as for now (December 2022). However will have to work
     * onenhancement, more complex rules using not only ID of ability but also other #params() of it.
     */
    for( IOneWsAbility ability : abilitiesPanel.checkSupport().listCheckedItems( true ) ) {
      ITsCombiFilterParams p = StdFilterOptionVsConst.makeFilterParams( TSID_ID, EQ, avStr( ability.id() ) );
      OneWsRule wsRule = new OneWsRule( ability.nmName(), p, ALLOW );
      newRules.add( wsRule );
    }
    ows().defineProfile( currProfile.id(), currProfile.attrs(), newRules );
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
    // right
    IM5Model<IOneWsAbility> abilityModel = m5().getModel( OneWsAbilityM5Model.MODEL_ID, IOneWsAbility.class );
    IM5LifecycleManager<IOneWsAbility> abilityLm = abilityModel.getLifecycleManager( skConn() );
    ITsGuiContext ctx2 = new TsGuiContext( tsContext() );
    abilitiesPanel = abilityModel.panelCreator().createCollChecksPanel( ctx2, abilityLm.itemsProvider() );
    abilitiesPanel.createControl( sfMain );
    abilitiesPanel.checkSupport().checksChangeEventer().addListener( this::whenAbilitiesCheckStateChanged );
    // setup
    sfMain.setWeights( 5000, 5000 );
    abilitiesPanel.setEditable( false ); // TODO see FIXME above
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
