package org.toxsoft.uskat.core.gui.ugwi.valed;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ISkResources.*;
import static org.toxsoft.uskat.core.gui.ugwi.valed.ValedUgwiSelectorFactory.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.panels.generic.*;
import org.toxsoft.core.tsgui.utils.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.valed.controls.basic.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.gui.*;
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
  private ISkCoreApi                  coreApi;
  private Composite                   selectBackPanel;
  private String                      currUgwiKindId;

  /**
   * Конструктор панели, предназначенной для вставки в диалог {@link TsDialog}.
   *
   * @param aParent Composite - родительская компонента
   * @param aOwnerDialog TsDialog - родительский диалог
   */
  public PanelUgwiSelector( Composite aParent, TsDialog<Ugwi, ITsGuiContext> aOwnerDialog ) {
    super( aParent, aOwnerDialog );
    coreApi = ISkCoreGuiConstants.REFDEF_SK_VALED_CORE_API.getRef( environ() );
    TsInternalErrorRtException.checkNull( coreApi );
    BorderLayout borderLayout = new BorderLayout();
    this.setLayout( borderLayout );

    // init Ugwi kind panel
    initKindPanel( this );
    // init selection of concrete panel
    initSelectPanel( this );
  }

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
          }
          // recreate panel for selection
          IUgwiKindGuiHelper ugwiHelper = coreApi.ugwiService().findHelper( currUgwiKindId, IUgwiKindGuiHelper.class );
          if( ugwSelector != null ) {
            ugwSelector.getControl().dispose();
          }
          ugwSelector = ugwiHelper.createUgwiSelectorPanel( environ() );
          selectPanel = ugwSelector.createControl( selectBackPanel );
          aBkPanel.layout( true );
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
   * Выводит диалог выбора Ugwi.
   * <p>
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aInitUgwi {@link Ugwi} для инициализации
   * @param aCoreApi {@link ISkCoreApi} core API
   * @param aSingleUgwiKindId {@link String} kind id
   * @return {@link Ugwi} - выбранный параметр <b>null</b> в случает отказа от редактирования
   */
  public static final Ugwi selectUgwiSingleKind( ITsGuiContext aContext, Ugwi aInitUgwi, ISkCoreApi aCoreApi,
      String aSingleUgwiKindId ) {
    TsNullArgumentRtException.checkNulls( aContext, aCoreApi );
    ISkCoreGuiConstants.REFDEF_SK_VALED_CORE_API.setRef( aContext, aCoreApi );
    OPDEF_SINGLE_UGWI_KIND_ID.setValue( aContext.params(), avStr( aSingleUgwiKindId ) );

    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, DLG_CAPTION_STR, DLG_TITLE_STR );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

  /**
   * Выводит диалог выбора Ugwi.
   * <p>
   *
   * @param aContext {@link ITsGuiContext} - контекст
   * @param aInitUgwi {@link Ugwi} для инициализации
   * @param aCoreApi {@link ISkCoreApi} core API
   * @param aUgwiKindIdList {@link String} kind id
   * @return {@link Ugwi} - выбранный параметр <b>null</b> в случает отказа от редактирования
   */
  public static final Ugwi selectUgwiListKinds( ITsGuiContext aContext, Ugwi aInitUgwi, ISkCoreApi aCoreApi,
      IStringList aUgwiKindIdList ) {
    TsNullArgumentRtException.checkNulls( aContext, aCoreApi );
    ISkCoreGuiConstants.REFDEF_SK_VALED_CORE_API.setRef( aContext, aCoreApi );
    OPDEF_UGWI_KIND_IDS_LIST.setValue( aContext.params(), avValobj( aUgwiKindIdList ) );

    IDialogPanelCreator<Ugwi, ITsGuiContext> creator = PanelUgwiSelector::new;
    ITsDialogInfo dlgInfo = new TsDialogInfo( aContext, DLG_CAPTION_STR, DLG_TITLE_STR );
    TsDialog<Ugwi, ITsGuiContext> d = new TsDialog<>( dlgInfo, aInitUgwi, aContext, creator );
    return d.execData();
  }

}
