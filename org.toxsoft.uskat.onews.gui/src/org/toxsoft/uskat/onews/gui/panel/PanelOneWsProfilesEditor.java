package org.toxsoft.uskat.onews.gui.panel;

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
import org.toxsoft.core.tslib.bricks.strid.more.*;
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
 * <li>right - chkable tree of the abilities {@link ISkOneWsService#listKnownAbilities()}.</li>
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

    // TODO PanelProfilesEditor.whenTsSelectionChanged()

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

    // TODO PanelProfilesEditor.whenAbilitiesCheckStateChanged()

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
