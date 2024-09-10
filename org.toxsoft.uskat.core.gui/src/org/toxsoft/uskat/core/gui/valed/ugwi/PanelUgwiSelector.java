package org.toxsoft.uskat.core.gui.valed.ugwi;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ISkResources.*;
import static org.toxsoft.uskat.core.gui.valed.ugwi.ValedUgwiSelector.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.utils.*;
import org.toxsoft.core.tsgui.utils.layout.BorderLayout;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.gui.ugwi.gui.*;

/**
 * GUI panel for {@link Ugwi} selection.
 * <p>
 *
 * @author dima
 */
public class PanelUgwiSelector
    extends AbstractTsDialogPanel<Ugwi, ITsGuiContext> {

  private Text                        fixedKindText;
  private ValedComboSelector<String>  ugwiKindsCombo;
  private Control                     selectPanel = null;
  private IGenericSelectorPanel<Ugwi> ugwSelector;
  private final ISkCoreApi            coreApi;
  private Composite                   selectBackPanel;
  private String                      currUgwiKindId;

  /**
   * Constructs panel as {@link TsDialog} content.
   *
   * @param aParent {@link Composite} - the parent composite
   * @param aOwnerDialog {@link TsDialog} - the owner dialog
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public PanelUgwiSelector( Composite aParent, TsDialog<Ugwi, ITsGuiContext> aOwnerDialog ) {
    super( aParent, aOwnerDialog );
    coreApi = skCoreApi( tsContext() );
    TsInternalErrorRtException.checkNull( coreApi );
    BorderLayout borderLayout = new BorderLayout();
    this.setLayout( borderLayout );

    // init Ugwi kind panel
    initKindPanel( this );
    // init selection of concrete panel
    initSelectPanel( this );

    btnEmpty = new Button( this, SWT.CHECK );
    btnEmpty.setLayoutData( BorderLayout.SOUTH );
    btnEmpty.setText( "Очистить (вернуть пустой UGWI)" );
    btnEmpty.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent aEvent ) {
        ugwSelector.getControl().setVisible( !btnEmpty.getSelection() );
      }
    } );
  }

  Button btnEmpty;

  private void initKindPanel( Composite aBkPanel ) {
    Composite kindBackPanel = new Composite( aBkPanel, SWT.NONE );

    // place to the nort of parent
    kindBackPanel.setLayoutData( BorderLayout.NORTH );
    // get selection options
    if( environ().params().hasValue( OPDEF_SINGLE_UGWI_KIND_ID ) ) {
      createSingleKindPanel( kindBackPanel );
      return;
    }
    else
      if( environ().params().hasValue( OPDEF_UGWI_KIND_IDS_LIST ) ) {
        createKindComboPanel( kindBackPanel );
        return;
      }
    throw new TsIllegalStateRtException( PANEL_ERR_MSG_NO_UGWI_KIND );
  }

  /**
   * Create panel to select Ugwi kind through combo
   *
   * @param aBkPanel - back panel
   */
  private void createKindComboPanel( Composite aBkPanel ) {
    GridLayout gl = new GridLayout( 2, false );
    aBkPanel.setLayout( gl );
    CLabel l = new CLabel( aBkPanel, SWT.LEFT );
    l.setText( STR_SINGLE_UGWI_KIND );
    IStringList ugwiKindList = environ().params().getValobj( OPDEF_UGWI_KIND_IDS_LIST );
    ITsVisualsProvider<String> visualsProvider = aItem -> aItem;
    ugwiKindsCombo = new ValedComboSelector<>( tsContext(), ugwiKindList, visualsProvider );
    ugwiKindsCombo.createControl( aBkPanel ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    currUgwiKindId = ugwiKindList.first();
    ugwiKindsCombo.setSelectedItem( currUgwiKindId );

    ugwiKindsCombo.eventer().addListener( ( aSource, aEditFinished ) -> {
      currUgwiKindId = ugwiKindsCombo.selectedItem();
      if( currUgwiKindId != null ) {
        ISkUgwiKind uKind = coreApi.ugwiService().listKinds().findByKey( currUgwiKindId );
        if( uKind != null ) {
          if( selectPanel != null ) {
            selectPanel.dispose();
            selectPanel = null;
          }
          // recreate panel for selection
          IUgwiKindGuiHelper ugwiHelper = coreApi.ugwiService().findHelper( currUgwiKindId, IUgwiKindGuiHelper.class );
          if( ugwSelector != null ) {
            ugwSelector.getControl().dispose();
          }
          ugwSelector = ugwiHelper.createUgwiSelectorPanel( environ() );
          selectPanel = ugwSelector.createControl( selectBackPanel );
          selectBackPanel.layout( true );
        }
      }
    } );
  }

  /**
   * Create panel to display fixed type of Ugwi
   *
   * @param aBackPanel - back panel
   */
  private void createSingleKindPanel( Composite aBackPanel ) {
    currUgwiKindId = environ().params().getStr( OPDEF_SINGLE_UGWI_KIND_ID );
    GridLayout gl = new GridLayout( 2, false );
    aBackPanel.setLayout( gl );
    CLabel l = new CLabel( aBackPanel, SWT.LEFT );
    l.setText( STR_SINGLE_UGWI_KIND );
    fixedKindText = new Text( aBackPanel, SWT.BORDER );
    fixedKindText.setEditable( false );
    fixedKindText.setLayoutData( new GridData( SWT.FILL, SWT.LEFT, true, false ) );
    fixedKindText.setText( currUgwiKindId );
  }

  protected void initSelectPanel( Composite aParent ) {
    selectBackPanel = new Composite( aParent, SWT.NONE );
    selectBackPanel.setLayoutData( BorderLayout.CENTER );
    selectBackPanel.setLayout( new BorderLayout() );
    IUgwiKindGuiHelper ugwiHelper = coreApi.ugwiService().findHelper( currUgwiKindId, IUgwiKindGuiHelper.class );
    ugwSelector = ugwiHelper.createUgwiSelectorPanel( environ() );
    selectPanel = ugwSelector.createControl( selectBackPanel );
  }

  @Override
  protected void doSetDataRecord( Ugwi aUgwi ) {
    if( aUgwi != null && !aUgwi.equals( Ugwi.NONE ) ) {
      if( environ().params().hasValue( OPDEF_SINGLE_UGWI_KIND_ID ) ) {
        // nop
      }
      else {
        ugwiKindsCombo.setSelectedItem( aUgwi.kindId() );
      }
      ugwSelector.setSelectedItem( aUgwi );
    }
  }

  @Override
  protected Ugwi doGetDataRecord() {
    if( btnEmpty.getSelection() ) {
      return Ugwi.NONE;
    }
    return ugwSelector.selectedItem();
  }

  @Override
  protected ValidationResult doValidate() {
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // Static methods to show dialog
  //

  /**
   * Invokes an UGWI selection dialog.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aInitUgwi {@link Ugwi} - initially selected UGWI or <code>null</code>
   * @param aSingleUgwiKindId {@link String} kind id
   * @return {@link Ugwi} - selected UGWI or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static final Ugwi selectUgwiSingleKind( ITsGuiContext aContext, Ugwi aInitUgwi, String aSingleUgwiKindId ) {
    TsNullArgumentRtException.checkNulls( aContext );
    OPDEF_SINGLE_UGWI_KIND_ID.setValue( aContext.params(), avStr( aSingleUgwiKindId ) );
    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, DLG_CAPTION_STR, DLG_TITLE_STR );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

  /**
   * Invokes an UGWI selection dialog.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aInitUgwi {@link Ugwi} - initially selected UGWI or <code>null</code>
   * @param aUgwiKindIdList {@link String} kind id
   * @return {@link Ugwi} - selected UGWI or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static final Ugwi selectUgwiListKinds( ITsGuiContext aContext, Ugwi aInitUgwi, IStringList aUgwiKindIdList ) {
    TsNullArgumentRtException.checkNulls( aContext );
    OPDEF_UGWI_KIND_IDS_LIST.setValue( aContext.params(), avValobj( aUgwiKindIdList ) );
    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, DLG_CAPTION_STR, DLG_TITLE_STR );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

}
